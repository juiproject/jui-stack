package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.edit.Commands;
import com.effacy.jui.text.type.edit.EditorState;
import com.effacy.jui.text.type.edit.History;
import com.effacy.jui.text.type.edit.Positions;
import com.effacy.jui.text.type.edit.Selection;
import com.effacy.jui.text.type.edit.Transaction;
import com.effacy.jui.text.type.edit.step.SetBlockTypeStep;
import com.effacy.jui.text.ui.editor.IEditorCommands;
import com.effacy.jui.text.ui.editor.IEditorContext;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.KeyboardEvent;

/**
 * Transaction-based rich text editor component.
 * <p>
 * Renders a {@link FormattedText} document as editable content and dispatches
 * all mutations through the transaction system ({@link Commands},
 * {@link EditorState}, {@link History}). The DOM is fully re-rendered after
 * each transaction, ensuring the view always matches the model.
 * <p>
 * Block-type-specific behaviour (rendering, event routing, format handling) is
 * encapsulated in {@link IBlockHandler} implementations registered in the
 * {@link #handlers} list. Extend the editor with new block types by
 * implementing {@code IBlockHandler} and adding an instance to the list in the
 * constructor.
 * <p>
 * Usage:
 * <pre>
 * Editor editor = new Editor();
 * editor.load(myDocument);
 * // ... add to a panel ...
 * FormattedText result = editor.value();
 * </pre>
 */
public class Editor extends Component<Editor.Config> {

    /************************************************************************
     * State.
     ************************************************************************/

    /**
     * The current editor state: document and selection. Mutated by applying
     * transactions returned from {@link EditorState#apply}. The editor's
     * transaction methods ensure that all mutations go through the transaction
     * system and that the document and selection are always in sync with the view.
     */
    private EditorState state;

    /**
     * History of transactions for undo/redo. Stores inverse transactions returned
     * to the editor by {@link EditorState#apply}.
     */
    private History history;

    /************************************************************************
     * DOM references.
     ************************************************************************/

    private Element editorEl;

    /**
     * Guards against selection sync during rendering.
     */
    private boolean rendering;

    /**
     * Listener for editor state changes (selection, block type, formats).
     * The containing control uses this to update toolbars, manage floating
     * behaviour, etc.
     */
    private IStateListener stateListener;

    /************************************************************************
     * Block handler registry.
     ************************************************************************/

    /**
     * Ordered list of block handlers. For each operation the editor iterates
     * this list and delegates to the first handler whose {@link IBlockHandler#accepts}
     * returns {@code true}.
     */
    private final List<IBlockHandler> handlers = new ArrayList<>();

    /**
     * Context object exposed to all block handlers, providing lazy access to
     * the editor's services. Fields like {@code editorEl} are read at call
     * time (not at construction time) so the context is safe to create eagerly.
     */
    private final IEditorContext ctx = new IEditorContext() {

        @Override
        public Element editorEl() {
            return editorEl;
        }

        @Override
        public EditorState state() {
            return state;
        }

        @Override
        public void applyTransaction(Transaction tr) {
            Editor.this.applyTransaction(tr);
        }

        @Override
        public void applyTransactionSilent(Transaction tr) {
            Editor.this.applyTransactionSilent(tr);
        }

        @Override
        public void syncSelectionFromDom() {
            Editor.this.syncSelectionFromDom();
        }

        @Override
        public Map<FormatType, String> formatClasses() {
            return FORMAT_CLASSES;
        }

        @Override
        public ILocalCSS styles() {
            return Editor.this.styles();
        }

        @Override
        public IListIndexFormatter listIndexFormatter() {
            return config().listIndexFormatter;
        }

        @Override
        public void renderLine(Element parent, FormattedLine line) {
            Editor.this.renderLine(parent, line);
        }

        @Override
        public void notifyCellSelection(Set<FormatType> activeFormats) {
            Editor.this.updateToolbarForCellSelection(activeFormats);
        }
    };

    /************************************************************************
     * Configuration.
     ************************************************************************/

    /**
     * Configuration for the editor. Use fluent methods to customise, then pass
     * to the {@link Editor#Editor(Config)} constructor.
     */
    public static class Config extends Component.Config {

        boolean paragraphAfterHeading = true;
        IListIndexFormatter listIndexFormatter = Editor::defaultListIndex;
        boolean debugLog;

        /**
         * Configures whether pressing Enter at the end of a heading (H1–H3)
         * creates a new paragraph instead of continuing the heading (default
         * {@code true}).
         */
        public Config paragraphAfterHeading(boolean enable) {
            this.paragraphAfterHeading = enable;
            return this;
        }

        /**
         * Configures the formatter used to generate ordered list markers.
         */
        public Config listIndexFormatter(IListIndexFormatter formatter) {
            this.listIndexFormatter = formatter;
            return this;
        }

        /**
         * Enables debug logging of the document model and selection to the
         * browser console after every transaction and selection change.
         */
        public Config debugLog(boolean enable) {
            this.debugLog = enable;
            return this;
        }
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    public Editor() {
        this(new Config());
    }

    public Editor(Config config) {
        super(config);
        FormattedText doc = new FormattedText()
            .block(BlockType.PARA, b -> b.line(""));
        state = EditorState.create(doc);
        history = new History();
        handlers.add(new EquationBlockHandler());
        handlers.add(new DiagramBlockHandler());
        handlers.add(new TableBlockHandler());
        handlers.add(new StandardBlockHandler());
    }

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            root.attr("contenteditable", "true");
        }).build(ctx -> {
            editorEl = el;
            render();
            attachEventListeners();
        });
    }

    /************************************************************************
     * Public API.
     ************************************************************************/

    /**
     * Loads a document into the editor, replacing any current content.
     *
     * @param doc
     *            the document to load.
     */
    public void load(FormattedText doc) {
        if (doc == null)
            doc = new FormattedText().block(BlockType.PARA, b -> b.line(""));
        state = EditorState.create(doc);
        history.clear();
        if (editorEl != null)
            render();
    }

    /**
     * Returns the current document.
     */
    public FormattedText value() {
        return state.doc();
    }

    /**
     * Listener for editor state changes. The containing control implements
     * this to receive state updates and forward them to toolbars, manage
     * floating behaviour, etc.
     */
    public interface IStateListener {

        /**
         * Called when the editor's selection or content changes. The listener
         * should update toolbar state and handle any positional behaviour
         * (e.g. floating toolbar show/hide).
         *
         * @param blockType
         *                       the block type of the anchor block.
         * @param activeFormats
         *                       the set of inline formats active at the
         *                       current cursor or range.
         * @param rangeSelected
         *                       {@code true} if the selection is a range.
         */
        void onStateUpdate(BlockType blockType, Set<FormatType> activeFormats, boolean rangeSelected);

        /**
         * Called when the selection is in a cell-editing context (e.g.
         * table cell). Block-type information is not meaningful.
         *
         * @param activeFormats
         *                      the set of inline formats active at the cell
         *                      selection.
         */
        void onCellStateUpdate(Set<FormatType> activeFormats);
    }

    /**
     * Binds a state listener to this editor and returns the command
     * interface that can be used to drive the editor (e.g. from toolbar
     * buttons).
     * <p>
     * May be called before or after the editor is rendered. If called after,
     * an initial state update is sent immediately.
     *
     * @param listener
     *                 the listener for state changes.
     * @return the command interface for driving the editor.
     */
    public IEditorCommands bind(IStateListener listener) {
        this.stateListener = listener;
        if (editorEl != null)
            updateToolbarState();
        return new IEditorCommands() {

            @Override
            public void toggleFormat(FormatType type) {
                handleFormatToggle(type);
            }

            @Override
            public void setBlockType(BlockType type) {
                syncSelectionFromDom();
                applyTransaction(Commands.setBlockType(state, type));
            }

            @Override
            public void toggleBlockType(BlockType type) {
                syncSelectionFromDom();
                applyTransaction(Commands.toggleBlockType(state, type));
            }

            @Override
            public void insertTable(int rows, int cols) {
                syncSelectionFromDom();
                Selection preSel = state.selection();
                int preBlock = preSel.isCursor() ? preSel.anchorBlock() : preSel.fromBlock();
                applyTransaction(Commands.insertTable(state, rows, cols));
                handlerFor(BlockType.TABLE).focusBlock(preBlock + 1, ctx);
            }

            @Override
            public void insertEquation() {
                syncSelectionFromDom();
                Selection preSel = state.selection();
                int preBlock = preSel.isCursor() ? preSel.anchorBlock() : preSel.fromBlock();
                applyTransaction(Commands.insertEquation(state));
                handlerFor(BlockType.EQN).focusBlock(preBlock + 1, ctx);
            }

            @Override
            public void insertDiagram() {
                syncSelectionFromDom();
                Selection preSel = state.selection();
                int preBlock = preSel.isCursor() ? preSel.anchorBlock() : preSel.fromBlock();
                applyTransaction(Commands.insertDiagram(state));
                handlerFor(BlockType.DIA).focusBlock(preBlock + 1, ctx);
            }

            @Override
            public void insertText(String text) {
                if ((text == null) || text.isEmpty())
                    return;
                syncSelectionFromDom();
                applyTransaction(Commands.insertText(state, text));
            }

            @Override
            public void syncSelection() {
                syncSelectionFromDom();
            }

            @Override
            public String currentLink() {
                syncSelectionFromDom();
                return extractLinkUrl(state.selection());
            }

            @Override
            public void applyLink(String url) {
                applyTransaction(Commands.updateLink(state, url));
            }

            @Override
            public void removeLink() {
                applyTransaction(Commands.removeLink(state));
            }

            @Override
            public void applyVariable(String name, String label) {
                syncSelectionFromDom();
                applyTransaction(Commands.insertVariable(state, name, label));
            }
        };
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    /**
     * Full re-render of the document into the editor element. Each block is
     * delegated to the appropriate {@link IBlockHandler}.
     */
    private void render() {
        rendering = true;
        try {
            editorEl.innerHTML = "";
            List<FormattedBlock> blocks = state.doc().getBlocks();
            handlers.forEach(h -> h.beginRender(ctx));
            for (int i = 0; i < blocks.size(); i++) {
                Element el = handlerFor(blocks.get(i).getType()).render(blocks.get(i), i, ctx);
                editorEl.appendChild(el);
            }
        } finally {
            rendering = false;
        }
        restoreSelection();
        ensureCursorVisible();
        updateToolbarState();
        handlers.forEach(h -> h.afterRender(ctx));
    }

    /**
     * Renders a single line's formatted content into a parent element.
     */
    private void renderLine(Element parent, FormattedLine line) {
        line.sequence().forEach(segment -> {
            if (segment.variable()) {
                Element chip = DomGlobal.document.createElement("span");
                chip.classList.add(styles().variable());
                chip.setAttribute("contenteditable", "false");
                chip.textContent = segment.text();
                parent.appendChild(chip);
                // Append an empty text node after the CEF span so that the
                // browser has a text-node cursor target. Without this,
                // _resolvePosition falls back to an element-child reference
                // that prevents beforeinput from firing for text insertion.
                parent.appendChild(DomGlobal.document.createTextNode(""));
            } else if (segment.formatting().length == 0) {
                parent.appendChild(DomGlobal.document.createTextNode(segment.text()));
            } else if (segment.contains(FormatType.A)) {
                Element a = DomGlobal.document.createElement("a");
                String href = segment.link();
                if (href != null) {
                    a.setAttribute("href", href);
                    if (href.startsWith("http"))
                        a.setAttribute("target", "_blank");
                }
                a.textContent = segment.text();
                parent.appendChild(a);
            } else {
                Element span = DomGlobal.document.createElement("span");
                for (FormatType fmt : segment.formatting()) {
                    String cls = FORMAT_CLASSES.get(fmt);
                    if (cls != null)
                        span.classList.add(cls);
                }
                span.appendChild(DomGlobal.document.createTextNode(segment.text()));
                parent.appendChild(span);
            }
        });
    }

    /************************************************************************
     * Selection synchronisation.
     ************************************************************************/

    /**
     * Restores the DOM selection from the editor state.
     */
    private void restoreSelection() {
        Selection sel = state.selection();
        if (sel.isCursor())
            EditorSupport.setCursor(editorEl, sel.anchorBlock(), sel.anchorOffset());
        else
            EditorSupport.setSelection(editorEl, sel.anchorBlock(), sel.anchorOffset(), sel.headBlock(), sel.headOffset());
    }

    /**
     * Scrolls the editor so that the block containing the cursor is visible.
     */
    private void ensureCursorVisible() {
        Selection sel = state.selection();
        int blockIdx = sel.anchorBlock();
        if ((blockIdx < 0) || (blockIdx >= editorEl.childElementCount))
            return;
        elemental2.dom.HTMLElement blockEl = (elemental2.dom.HTMLElement) editorEl.childNodes.item(blockIdx);
        int blockBottom = blockEl.offsetTop + blockEl.offsetHeight;
        double viewBottom = editorEl.scrollTop + editorEl.clientHeight;
        if (blockBottom > viewBottom)
            editorEl.scrollTop = blockBottom - editorEl.clientHeight;
        else if (blockEl.offsetTop < editorEl.scrollTop)
            editorEl.scrollTop = blockEl.offsetTop;
    }

    /**
     * Reads the DOM selection and updates the editor state and toolbar.
     * When the selection is inside a non-block area (e.g. a table cell),
     * {@link EditorSupport#readSelection} returns {@code null}; handlers
     * are consulted via {@link IBlockHandler#handleSelectionChange} so they
     * can update the toolbar for the cell context.
     */
    private void syncSelectionFromDom() {
        if (rendering)
            return;
        int[] sel = EditorSupport.readSelection(editorEl);
        if (sel == null) {
            // Cursor is in a non-block area (e.g. a table cell) — let handlers update the toolbar.
            for (IBlockHandler h : handlers) {
                if (h.handleSelectionChange(ctx))
                    return;
            }
            return;
        }
        int numBlocks = state.doc().getBlocks().size();
        if ((sel[0] < 0) || (sel[0] >= numBlocks) || (sel[2] < 0) || (sel[2] >= numBlocks))
            return;
        state.setSelection(new Selection(sel[0], sel[1], sel[2], sel[3]));
        updateToolbarState();
    }

    /**
     * Updates toolbar button active states to reflect the current selection.
     * Block type buttons highlight based on the anchor block's type; format
     * buttons highlight based on whether the format is active at the cursor
     * or across the entire range selection.
     */
    private void updateToolbarState() {
        if (stateListener == null)
            return;
        Selection sel = state.selection();
        int blockIdx = sel.anchorBlock();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIdx < 0) || (blockIdx >= blocks.size()))
            return;
        FormattedBlock blk = blocks.get(blockIdx);
        Set<FormatType> active = java.util.EnumSet.noneOf(FormatType.class);
        for (FormatType ft : FormatType.values()) {
            if (isFormatActive(sel, ft))
                active.add(ft);
        }
        stateListener.onStateUpdate(blk.getType(), active, !sel.isCursor());
    }

    /**
     * Notifies the state listener for the cell-editing context.
     */
    private void updateToolbarForCellSelection(Set<FormatType> activeFormats) {
        if (stateListener != null)
            stateListener.onCellStateUpdate(activeFormats);
    }

    /**
     * Checks whether a format type is active at the current selection. For a
     * cursor, checks the character immediately before the cursor. For a range,
     * checks whether the entire range has the format.
     */
    private boolean isFormatActive(Selection sel, FormatType type) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if (sel.isCursor()) {
            int block = sel.anchorBlock();
            int offset = sel.anchorOffset();
            FormattedBlock blk = blocks.get(block);
            if (offset > 0)
                return blk.hasFormat(offset - 1, 1, type);
            if (Positions.contentSize(blk) > 0)
                return blk.hasFormat(0, 1, type);
            return false;
        }
        int fromBlock = sel.fromBlock();
        int toBlock = sel.toBlock();
        for (int i = fromBlock; i <= toBlock; i++) {
            int start = (i == fromBlock) ? sel.fromOffset() : 0;
            int end = (i == toBlock) ? sel.toOffset() : Positions.contentSize(blocks.get(i));
            int len = end - start;
            if ((len > 0) && !blocks.get(i).hasFormat(start, len, type))
                return false;
        }
        return true;
    }

    /************************************************************************
     * Transaction dispatch.
     ************************************************************************/

    /**
     * Applies or removes a format toggle. Handlers are given the first chance
     * to handle the toggle (e.g. TableBlockHandler applies it within a cell).
     * Falls back to the standard transaction path when no handler claims it.
     */
    private void handleFormatToggle(FormatType type) {
        for (IBlockHandler h : handlers) {
            if (h.handleFormatToggle(type, ctx))
                return;
        }
        syncSelectionFromDom();
        applyTransaction(Commands.toggleFormat(state, type));
    }

    /**
     * Extracts the link URL at the anchor position of the given selection, or
     * {@code null} if no link exists there.
     */
    private String extractLinkUrl(Selection sel) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int blockIdx = sel.anchorBlock();
        if ((blockIdx < 0) || (blockIdx >= blocks.size()))
            return null;
        FormattedBlock blk = blocks.get(blockIdx);
        int target = sel.anchorOffset();
        int lineStart = 0;
        for (FormattedLine line : blk.getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                int absStart = lineStart + fmt.getIndex();
                int absEnd = absStart + fmt.getLength();
                if ((target >= absStart) && (target < absEnd) && fmt.getFormats().contains(FormatType.A)) {
                    if (fmt.getMeta() != null)
                        return fmt.getMeta().get(FormattedLine.META_LINK);
                }
            }
            lineStart += line.length() + 1;
        }
        return null;
    }

    /**
     * Applies a transaction, pushes inverse to history, and re-renders.
     * Calls {@link IBlockHandler#beforeApplyTransaction} on all handlers first
     * so that any in-progress edits (e.g. cell text) are flushed to the model.
     */
    private void applyTransaction(Transaction tr) {
        if (tr == null)
            return;
        handlers.forEach(h -> h.beforeApplyTransaction(ctx));
        Transaction inverse = state.apply(tr);
        history.push(inverse);
        if (config().debugLog)
            debugLogState("applyTransaction");
        render();
    }

    /**
     * Applies a transaction and pushes its inverse to history without
     * re-rendering. Used by handlers that update the DOM directly (e.g.
     * cell formatting) and do not need a full re-render.
     */
    private void applyTransactionSilent(Transaction tr) {
        if (tr == null)
            return;
        Transaction inverse = state.apply(tr);
        history.push(inverse);
    }

    /************************************************************************
     * Event handling.
     ************************************************************************/

    private void attachEventListeners() {
        editorEl.addEventListener("keydown", evt -> handleKeyDown((KeyboardEvent) evt));
        editorEl.addEventListener("beforeinput", evt -> handleBeforeInput(evt));
        editorEl.addEventListener("paste", evt -> handlePaste(evt));
        DomGlobal.document.addEventListener("selectionchange", evt -> syncSelectionFromDom());
    }

    /**
     * Handles keyboard shortcuts (undo/redo, format toggles, indent).
     * Handlers are consulted first; if one returns {@code true} the event is
     * consumed and the editor's default logic is skipped.
     */
    private void handleKeyDown(KeyboardEvent ke) {
        if (config().debugLog)
            DomGlobal.console.log("[Editor:keydown] key=" + ke.key + " ctrl=" + (ke.ctrlKey || ke.metaKey) + " alt=" + ke.altKey + " shift=" + ke.shiftKey);
        for (IBlockHandler h : handlers) {
            if (h.handleKeyDown(ke, ctx))
                return;
        }
        boolean ctrl = ke.ctrlKey || ke.metaKey;
        boolean shift = ke.shiftKey;

        // Undo / redo.
        if (ctrl && "z".equals(ke.key) && !shift) {
            ke.preventDefault();
            if (history.undo(state))
                render();
            return;
        }
        if ((ctrl && shift && ("z".equals(ke.key) || "Z".equals(ke.key)))
                || (ctrl && "y".equals(ke.key))) {
            ke.preventDefault();
            if (history.redo(state))
                render();
            return;
        }

        // Format toggles.
        if (ctrl && !shift) {
            FormatType fmt = null;
            if ("b".equals(ke.key))
                fmt = FormatType.BLD;
            else if ("i".equals(ke.key))
                fmt = FormatType.ITL;
            else if ("u".equals(ke.key))
                fmt = FormatType.UL;
            if (fmt != null) {
                ke.preventDefault();
                syncSelectionFromDom();
                applyTransaction(Commands.toggleFormat(state, fmt));
                return;
            }
        }

        // Indent / outdent.
        if ("Tab".equals(ke.key)) {
            ke.preventDefault();
            syncSelectionFromDom();
            if (shift)
                applyTransaction(Commands.outdent(state));
            else
                applyTransaction(Commands.indent(state));
        }
    }

    /**
     * Handles content-mutating input events. All browser-native mutations are
     * prevented; the equivalent is performed through the transaction system.
     * Handlers are consulted first and may consume the event (e.g. to allow
     * native input inside table cells).
     */
    private void handleBeforeInput(elemental2.dom.Event evt) {
        if (config().debugLog)
            DomGlobal.console.log("[Editor:beforeinput] inputType=" + EditorSupport.getInputType(evt) + " data=" + EditorSupport.getInputData(evt));
        for (IBlockHandler h : handlers) {
            if (h.handleBeforeInput(evt, ctx))
                return;
        }
        evt.preventDefault();
        syncSelectionFromDom();

        String inputType = EditorSupport.getInputType(evt);
        if (inputType == null)
            return;

        switch (inputType) {
            case "insertText": {
                String data = EditorSupport.getInputData(evt);
                if ((data != null) && !data.isEmpty())
                    applyTransaction(Commands.insertText(state, data));
                break;
            }
            case "insertParagraph": {
                Selection sel = state.selection();
                int blockIdx = sel.isCursor() ? sel.anchorBlock() : sel.fromBlock();
                FormattedBlock blk = state.doc().getBlocks().get(blockIdx);
                int offset = sel.isCursor() ? sel.anchorOffset() : sel.fromOffset();

                // Empty list item: convert to paragraph instead of splitting.
                if (sel.isCursor()
                        && blk.getType().is(BlockType.NLIST, BlockType.OLIST)
                        && (Positions.contentSize(blk) == 0)) {
                    applyTransaction(Commands.setBlockType(state, BlockType.PARA));
                    break;
                }

                Transaction splitTr = Commands.splitBlock(state);
                if (splitTr != null) {
                    // Heading at end → new block becomes paragraph.
                    if (config().paragraphAfterHeading
                            && blk.getType().is(BlockType.H1, BlockType.H2, BlockType.H3)
                            && (offset >= Positions.contentSize(blk))) {
                        splitTr.step(new SetBlockTypeStep(blockIdx + 1, BlockType.PARA));
                    }
                }
                applyTransaction(splitTr);
                break;
            }
            case "insertLineBreak":
                applyTransaction(Commands.insertLineBreak(state));
                break;
            case "deleteContentBackward": {
                Selection sel2 = state.selection();
                if (sel2.isCursor() && (sel2.anchorOffset() == 0)) {
                    FormattedBlock blk2 = state.doc().getBlocks().get(sel2.anchorBlock());
                    if (blk2.getIndent() > 0) {
                        applyTransaction(Commands.outdent(state));
                        break;
                    }
                    // List item at indent 0: exit list (convert to paragraph).
                    if (blk2.getType().is(BlockType.NLIST, BlockType.OLIST)) {
                        applyTransaction(Commands.setBlockType(state, BlockType.PARA));
                        break;
                    }
                    // Join with previous block (cross-type allowed).
                    applyTransaction(Commands.forceJoinWithPrevious(state));
                    break;
                }
                // Atomic variable deletion: if cursor is inside or at the
                // end of a variable, delete the entire variable as a unit.
                if (sel2.isCursor()) {
                    int[] varRange = findVariableContaining(sel2.anchorBlock(), sel2.anchorOffset());
                    if (varRange != null) {
                        state.setSelection(Selection.range(sel2.anchorBlock(), varRange[0], sel2.anchorBlock(), varRange[1]));
                        applyTransaction(Commands.deleteSelection(state));
                        break;
                    }
                }
                applyTransaction(Commands.deleteCharBefore(state));
                break;
            }
            case "deleteContentForward": {
                // Atomic variable deletion: if cursor is inside or at the
                // start of a variable, delete the entire variable as a unit.
                Selection selFwd = state.selection();
                if (selFwd.isCursor()) {
                    int[] varRange = findVariableAt(selFwd.anchorBlock(), selFwd.anchorOffset());
                    if (varRange != null) {
                        state.setSelection(Selection.range(selFwd.anchorBlock(), varRange[0], selFwd.anchorBlock(), varRange[1]));
                        applyTransaction(Commands.deleteSelection(state));
                        break;
                    }
                }
                Transaction tr3 = Commands.deleteCharAfter(state);
                if (tr3 != null) {
                    applyTransaction(tr3);
                    break;
                }
                // deleteCharAfter returns null at cross-type block boundary.
                applyTransaction(Commands.forceJoinWithNext(state));
                break;
            }
            case "deleteWordBackward":
                applyTransaction(Commands.deleteWordBefore(state));
                break;
            case "deleteWordForward":
                applyTransaction(Commands.deleteWordAfter(state));
                break;
            case "deleteByCut":
                applyTransaction(Commands.deleteSelection(state));
                break;
            default:
                // All other input types are prevented (no-op).
                break;
        }
    }

    /**
     * Handles paste events. Reads plain text from the clipboard and inserts
     * it via the transaction system. Handlers are consulted first and may
     * consume the event (e.g. to allow native paste inside table cells).
     */
    private void handlePaste(elemental2.dom.Event evt) {
        for (IBlockHandler h : handlers) {
            if (h.handlePaste(evt, ctx))
                return;
        }
        evt.preventDefault();
        syncSelectionFromDom();
        String text = EditorSupport.getClipboardText(evt);
        if ((text == null) || text.isEmpty())
            return;
        // Normalize line endings (Windows \r\n and old Mac \r).
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        applyTransaction(Commands.pasteText(state, text));
    }

    /************************************************************************
     * Debug logging.
     ************************************************************************/

    private void debugLogState(String context) {
        Selection sel = state.selection();
        String selStr = sel.isCursor()
            ? "cursor block=" + sel.anchorBlock() + " offset=" + sel.anchorOffset()
            : "range anchor=" + sel.anchorBlock() + ":" + sel.anchorOffset()
                + " head=" + sel.headBlock() + ":" + sel.headOffset();
        DomGlobal.console.log("[Editor:" + context + "] " + selStr + "\n" + state.doc().debug());
    }

    /************************************************************************
     * Variable boundary helpers (atomic deletion).
     ************************************************************************/

    /**
     * Returns the {@code [start, end)} block-level range of the variable
     * that contains, or is immediately adjacent to, {@code offset} in the
     * given block.
     * <p>
     * A match occurs when {@code offset} falls strictly inside the variable
     * range (absStart &lt; offset &lt; absEnd) or exactly at a boundary
     * (absStart == offset or absEnd == offset). This handles the case where
     * the browser positions the cursor anywhere within the variable chip
     * text rather than at a precise boundary.
     *
     * @return {@code [absStart, absEnd]} or {@code null} if no variable
     *         spans the given offset.
     */
    private int[] findVariableContaining(int blockIdx, int offset) {
        FormattedBlock blk = state.doc().getBlocks().get(blockIdx);
        int lineStart = 0;
        for (FormattedLine line : blk.getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                if ((fmt.getMeta() == null) || !fmt.getMeta().containsKey(FormattedLine.META_VARIABLE))
                    continue;
                int absStart = lineStart + fmt.getIndex();
                int absEnd = absStart + fmt.getLength();
                if ((offset > absStart) && (offset <= absEnd))
                    return new int[]{absStart, absEnd};
            }
            lineStart += line.length() + 1;
        }
        return null;
    }

    /**
     * Like {@link #findVariableContaining(int, int)} but includes the case
     * where the cursor is exactly at the start of the variable (for
     * forward-delete).
     */
    private int[] findVariableAt(int blockIdx, int offset) {
        FormattedBlock blk = state.doc().getBlocks().get(blockIdx);
        int lineStart = 0;
        for (FormattedLine line : blk.getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                if ((fmt.getMeta() == null) || !fmt.getMeta().containsKey(FormattedLine.META_VARIABLE))
                    continue;
                int absStart = lineStart + fmt.getIndex();
                int absEnd = absStart + fmt.getLength();
                if ((offset >= absStart) && (offset < absEnd))
                    return new int[]{absStart, absEnd};
            }
            lineStart += line.length() + 1;
        }
        return null;
    }

    /************************************************************************
     * Handler registry helpers.
     ************************************************************************/

    /**
     * Returns the first registered handler that accepts the given block type,
     * or the last handler in the registry as a fallback.
     */
    private IBlockHandler handlerFor(BlockType type) {
        for (IBlockHandler h : handlers) {
            if (h.accepts(type))
                return h;
        }
        return handlers.get(handlers.size() - 1);
    }

    /************************************************************************
     * List index formatting.
     ************************************************************************/

    /**
     * Formats the counter value for an ordered list item at a given indent
     * level. The returned string is used as the visible list marker.
     */
    @FunctionalInterface
    public interface IListIndexFormatter {

        /**
         * Formats a list counter value.
         *
         * @param indent
         *               the indent level (0–5).
         * @param counter
         *                the sequential counter value (1-based).
         * @return the display string (e.g. "1", "a", "iii").
         */
        String format(int indent, int counter);
    }

    /**
     * Default formatter: numeric at level 0, lowercase alpha at level 1,
     * lowercase roman at level 2, then cycles.
     */
    private static String defaultListIndex(int indent, int counter) {
        switch (indent % 3) {
            case 0:
                return String.valueOf(counter);
            case 1:
                return toLetter(counter);
            case 2:
                return toRoman(counter);
            default:
                return String.valueOf(counter);
        }
    }

    private static String toLetter(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            n--;
            sb.insert(0, (char) ('a' + (n % 26)));
            n /= 26;
        }
        return sb.toString();
    }

    private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_SYMBOLS = {"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};

    private static String toRoman(int n) {
        if (n <= 0)
            return String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (n >= ROMAN_VALUES[i]) {
                sb.append(ROMAN_SYMBOLS[i]);
                n -= ROMAN_VALUES[i];
            }
        }
        return sb.toString();
    }

    /************************************************************************
     * Format type to CSS class mapping.
     ************************************************************************/

    private static final Map<FormatType, String> FORMAT_CLASSES = new HashMap<>();
    static {
        FORMAT_CLASSES.put(FormatType.BLD, "fmt_bold");
        FORMAT_CLASSES.put(FormatType.ITL, "fmt_italic");
        FORMAT_CLASSES.put(FormatType.UL, "fmt_underline");
        FORMAT_CLASSES.put(FormatType.STR, "fmt_strike");
        FORMAT_CLASSES.put(FormatType.SUP, "fmt_superscript");
        FORMAT_CLASSES.put(FormatType.SUB, "fmt_subscript");
        FORMAT_CLASSES.put(FormatType.CODE, "fmt_code");
        FORMAT_CLASSES.put(FormatType.HL, "fmt_highlight");
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IComponentCSS {

        String block();

        String listBullet();

        String listNumber();

        String variable();

    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .component {
            outline: none;
            min-height: 2em;
            cursor: text;
            padding: 0.5em;
            flex: 1;
            overflow: auto;
        }
        .component:focus {
            outline: none;
        }
        .component .block {
            margin: 0 0 0.15em 0;
            padding: 2px 0;
            min-height: 1em;
            white-space: pre-wrap;
        }
        .component .listBullet {
            position: relative;
            padding-left: 1.5em;
        }
        .component .listBullet::before {
            position: absolute;
            left: 0.35em;
            content: '\\2022';
        }
        .component .listNumber {
            position: relative;
            padding-left: 1.5em;
        }
        .component .listNumber::before {
            position: absolute;
            left: 0.15em;
            content: attr(data-list-index) '.';
        }
        .component h1 {
            font-size: 1.8em;
            font-weight: 500;
            margin: 0 0 0.15em 0;
        }
        .component h2 {
            font-size: 1.5em;
            font-weight: 500;
            margin: 0 0 0.15em 0;
        }
        .component h3 {
            font-size: 1.25em;
            font-weight: 500;
            margin: 0 0 0.15em 0;
        }
        .component p {
            margin: 0 0 0.15em 0;
        }
        .component .indent1 { margin-left: 1.5em; }
        .component .indent2 { margin-left: 3em; }
        .component .indent3 { margin-left: 4.5em; }
        .component .indent4 { margin-left: 6em; }
        .component .indent5 { margin-left: 7.5em; }
        .component .fmt_bold { font-weight: 600; }
        .component .fmt_italic { font-style: italic; }
        .component .fmt_underline { text-decoration: underline; }
        .component .fmt_strike { text-decoration: line-through; }
        .component .fmt_strike.fmt_underline { text-decoration: underline line-through; }
        .component .fmt_superscript { vertical-align: super; font-size: 0.8em; }
        .component .fmt_subscript { vertical-align: sub; font-size: 0.8em; }
        .component .fmt_highlight { background-color: #F5EB72; }
        .component .fmt_code {
            font-family: "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace;
            line-height: normal;
            background: rgba(135,131,120,.15);
            color: #EB5757;
            border-radius: 4px;
            font-size: 85%;
            padding: 0.2em 0.4em;
        }
        .component .variable {
            background: #e0e7ff;
            color: #3730a3;
            padding: 1px 6px;
            border-radius: 3px;
            font-size: 0.85em;
            font-weight: 500;
            display: inline;
            user-select: all;
            cursor: default;
        }
    """)
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create(LocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
