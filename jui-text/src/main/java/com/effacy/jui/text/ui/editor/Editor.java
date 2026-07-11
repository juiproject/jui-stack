package com.effacy.jui.text.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
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
import com.effacy.jui.text.ui.type.FormattedTextStyles;
import com.effacy.jui.text.type.edit.Transaction;
import com.effacy.jui.text.type.edit.step.DeleteBlockStep;
import com.effacy.jui.text.type.edit.step.SetBlockTypeStep;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.DOMRect;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MouseEvent;
import elemental2.dom.Node;
import jsinterop.base.Js;

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
        String placeholder;
        IFileUploadHandler fileUpload;

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

        /**
         * Sets placeholder text shown when the editor is empty (a single empty paragraph).
         * When {@code null} (the default) no placeholder is shown.
         */
        public Config placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Configures a handler for files introduced into the editor (pasted from the
         * clipboard or dropped in). When set, an image file is uploaded via the handler
         * and inserted as an inline image at the cursor (using the returned URL as its
         * {@code src}); any other file is inserted as a link labelled with its file
         * name. When {@code null} (the default) pasted/dropped files are ignored.
         */
        public Config fileUpload(IFileUploadHandler handler) {
            this.fileUpload = handler;
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
        handlers.add(new FenceBlockHandler());
        handlers.add(new TableBlockHandler());
        handlers.add(new StandardBlockHandler());
    }

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            root.attr("contenteditable", "true");
            if (data.placeholder != null)
                root.attr("data-placeholder", data.placeholder);
        }).build(ctx -> {
            editorEl = el;
            // Scope the content to the shared richtext stylesheet (also injects it), so headings,
            // inline formats, quotes and code render from FormattedTextStyles — the same sheet the
            // read-only renderer uses — rather than being duplicated here.
            el.classList.add(FormattedTextStyles.styles().richtext());
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
        // A null or empty document has no blocks, but editing assumes at least one block
        // exists (e.g. the insert step indexes block 0). Seed a single empty paragraph.
        if ((doc == null) || doc.empty())
            doc = new FormattedText().block(BlockType.PARA, b -> b.line(""));
        // A trailing atomic block (fence/diagram/equation) or table leaves the caret
        // nowhere to land after it — ensure an editable paragraph follows.
        List<FormattedBlock> blocks = doc.getBlocks();
        if (!blocks.isEmpty()) {
            BlockType last = blocks.get(blocks.size() - 1).getType();
            if (atomicBlock(last) || (last == BlockType.TABLE))
                doc.block(BlockType.PARA, b -> b.line(""));
        }
        state = EditorState.create(doc);
        history.clear();
        if (editorEl != null)
            render();
    }

    /**
     * Block types that render atomically ({@code contenteditable="false"}) — the caret
     * cannot be placed inside them, so deletion treats them as a unit (see the
     * {@code deleteContentBackward}/{@code deleteContentForward} handling). Tables are
     * deliberately not included (they are editable within, and deleting a whole table on
     * a single keystroke would be too destructive).
     */
    private static boolean atomicBlock(BlockType type) {
        return type.is(BlockType.FENCE, BlockType.DIA, BlockType.EQN);
    }

    /**
     * Returns the current document.
     */
    public FormattedText value() {
        // Flush any DOM-only edits (e.g. table cells edited natively via contenteditable, which
        // otherwise sync only on blur) so the returned value reflects the current DOM.
        handlers.forEach(h -> h.syncFromDom(ctx));
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

        /**
         * Called when the editor content has been mutated (e.g. text
         * insertion, deletion, format change, undo/redo). The containing
         * control should use this to propagate modification events.
         */
        default void onContentChanged() {
            // Default no-op for backward compatibility.
        }
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
            public void insertFence(String info) {
                syncSelectionFromDom();
                Selection preSel = state.selection();
                int preBlock = preSel.isCursor() ? preSel.anchorBlock() : preSel.fromBlock();
                applyTransaction(Commands.insertFence(state, info));
                handlerFor(BlockType.FENCE).focusBlock(preBlock + 1, ctx);
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
            public boolean hasRangeSelection() {
                syncSelectionFromDom();
                return !state.selection().isCursor();
            }

            @Override
            public String currentLink() {
                syncSelectionFromDom();
                return extractLinkUrl(state.selection());
            }

            @Override
            public void applyLink(String url) {
                applyLink(url, null);
            }

            @Override
            public void applyLink(String url, String label) {
                Transaction tr = Commands.updateLink(state, url);
                // Open space (no selection, no link under the cursor): insert the
                // label (or the URL itself) as the linked text.
                if (tr == null)
                    tr = Commands.insertLink(state, url, label);
                applyTransaction(tr);
            }

            @Override
            public void removeLink() {
                applyTransaction(Commands.removeLink(state));
            }

            @Override
            public String currentComment() {
                syncSelectionFromDom();
                return extractCommentReference(state.selection());
            }

            @Override
            public void applyComment(String reference) {
                applyTransaction(Commands.applyComment(state, reference));
            }

            @Override
            public void removeComment() {
                applyTransaction(Commands.removeComment(state));
            }

            @Override
            public void removeComment(String reference) {
                applyTransaction(Commands.removeComment(state, reference));
            }

            @Override
            public void applyVariable(String name, String label) {
                syncSelectionFromDom();
                applyTransaction(Commands.insertVariable(state, name, label));
            }

            @Override
            public String currentImage() {
                syncSelectionFromDom();
                return extractImageSrc(state.selection());
            }

            @Override
            public void applyImage(String src) {
                applyTransaction(Commands.insertImage(state, src));
            }

            @Override
            public void removeImage() {
                applyTransaction(Commands.removeImage(state));
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
        // The DOM (and any selected image element) is rebuilt, so dismiss the overlay.
        hideImageOverlay();
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
        updatePlaceholder();
    }

    /**
     * Toggles the {@code data-empty} marker (paired with {@code data-placeholder} in CSS)
     * so the placeholder shows only when the document is blank — a single empty paragraph.
     */
    private void updatePlaceholder() {
        if ((editorEl == null) || (config().placeholder == null))
            return;
        if (isBlank())
            editorEl.setAttribute("data-empty", "true");
        else
            editorEl.removeAttribute("data-empty");
    }

    private boolean isBlank() {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if (blocks.size() != 1)
            return false;
        FormattedBlock blk = blocks.get(0);
        if ((blk.getType() != BlockType.PARA) || (Positions.contentSize(blk) != 0))
            return false;
        // A zero-length format (e.g. an inline image) is content even though it adds no
        // characters, so a block carrying one is not blank.
        for (FormattedLine line : blk.getLines()) {
            if (!line.getFormatting().isEmpty())
                return false;
        }
        return true;
    }

    /**
     * Renders a single line's formatted content into a parent element.
     */
    private void renderLine(Element parent, FormattedLine line) {
        line.sequence().forEach(segment -> {
            if (segment.variable()) {
                Element chip = DomGlobal.document.createElement("span");
                chip.classList.add("variable");
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
            } else if (segment.image()) {
                Element img = DomGlobal.document.createElement("img");
                String src = segment.meta().get(FormattedLine.META_IMAGE);
                if ((src != null) && !src.isEmpty())
                    img.setAttribute("src", src);
                // Alt is meta; the segment text is the image's sentinel character
                // (never rendered).
                String alt = segment.meta().get(FormattedLine.META_ALT);
                if ((alt != null) && !alt.isEmpty())
                    img.setAttribute("alt", alt);
                img.setAttribute("contenteditable", "false");
                applyImageAttributes(img, segment.meta());
                parent.appendChild(img);
                parent.appendChild(DomGlobal.document.createTextNode(""));
            } else if (segment.contains(FormatType.A)) {
                Element a = DomGlobal.document.createElement("a");
                String href = segment.link();
                if (href != null) {
                    a.setAttribute("href", href);
                    if (href.startsWith("http"))
                        a.setAttribute("target", "_blank");
                }
                applyCommentReference(a, segment);
                a.textContent = segment.text();
                parent.appendChild(a);
            } else {
                Element span = DomGlobal.document.createElement("span");
                for (FormatType fmt : segment.formatting()) {
                    String cls = FORMAT_CLASSES.get(fmt);
                    if (cls != null)
                        span.classList.add(cls);
                }
                applyCommentReference(span, segment);
                span.appendChild(DomGlobal.document.createTextNode(segment.text()));
                parent.appendChild(span);
            }
        });
    }

    /**
     * Marks the element with the segment's comment reference (as a
     * {@code data-comment} attribute) when the segment carries a comment
     * anchor — the hook external comment surfaces use to locate and wire up
     * the anchored range.
     */
    private void applyCommentReference(Element el, FormattedLine.TextSegment segment) {
        if (!segment.contains(FormatType.CMT) || !segment.hasMeta())
            return;
        String reference = segment.meta().get(FormattedLine.META_COMMENT);
        if ((reference != null) && !reference.isEmpty())
            el.setAttribute("data-comment", reference);
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
     * Extracts the comment reference at the anchor position of the given
     * selection, or {@code null} if no comment anchor exists there.
     */
    private String extractCommentReference(Selection sel) {
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
                if ((target >= absStart) && (target < absEnd) && fmt.getFormats().contains(FormatType.CMT)) {
                    if (fmt.getMeta() != null)
                        return fmt.getMeta().get(FormattedLine.META_COMMENT);
                }
            }
            lineStart += line.length() + 1;
        }
        return null;
    }

    private String extractImageSrc(Selection sel) {
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
                if ((absStart == target) && fmt.getFormats().contains(FormatType.IMG)) {
                    if (fmt.getMeta() != null)
                        return fmt.getMeta().get(FormattedLine.META_IMAGE);
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
        if (stateListener != null)
            stateListener.onContentChanged();
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
        if (stateListener != null)
            stateListener.onContentChanged();
    }

    /************************************************************************
     * Event handling.
     ************************************************************************/

    private void attachEventListeners() {
        editorEl.addEventListener("keydown", evt -> handleKeyDown((KeyboardEvent) evt));
        editorEl.addEventListener("beforeinput", evt -> handleBeforeInput(evt));
        editorEl.addEventListener("paste", evt -> handlePaste(evt));
        editorEl.addEventListener("dragover", evt -> handleDragOver(evt));
        editorEl.addEventListener("drop", evt -> handleDrop(evt));
        editorEl.addEventListener("click", evt -> handleEditorClick(evt));
        // Dismiss the image overlay on a pointer-down outside it (and outside the image).
        DomGlobal.document.addEventListener("mousedown", evt -> handleDocumentMouseDown(evt));
        DomGlobal.document.addEventListener("selectionchange", evt -> syncSelectionFromDom());
        // Keep the overlay aligned to the image while scrolling would be involved; the
        // simplest robust behaviour is to dismiss it on scroll.
        DomGlobal.document.addEventListener("scroll", evt -> hideImageOverlay(), true);
    }

    /************************************************************************
     * Image selection overlay (drag-resize handles + block-align toolbar).
     *
     * When an inline image is clicked a floating overlay is shown over it with
     * four corner handles (aspect-locked resize) and a small toolbar (left /
     * centre / right block alignment). Resizing is a pure-DOM live preview; a
     * single transaction ({@link Commands#setImageAttributes}) is committed on
     * release. The overlay is dismissed on any re-render, scroll, or outside
     * pointer-down.
     ************************************************************************/

    /** The floating overlay frame (lazily created, appended to {@code body}). */
    private HTMLElement imageOverlay;

    /** The image element the overlay currently targets, or {@code null}. */
    private Element selectedImage;

    /** The corner being dragged ({@code nw}/{@code ne}/{@code sw}/{@code se}). */
    private String resizeCorner;

    private double resizeStartX;
    private double resizeStartW;
    private double resizeAspect;

    private EventListener resizeMoveListener;
    private EventListener resizeUpListener;

    /**
     * Applies the width/height/(block) alignment meta of an image segment to its
     * rendered {@code <img>}.
     */
    private void applyImageAttributes(Element img, Map<String, String> meta) {
        if (meta == null)
            return;
        String width = meta.get(FormattedLine.META_WIDTH);
        if ((width != null) && !width.isEmpty())
            img.setAttribute("width", width);
        String height = meta.get(FormattedLine.META_HEIGHT);
        if ((height != null) && !height.isEmpty())
            img.setAttribute("height", height);
        String style = imageStyle(meta.get(FormattedLine.META_ALIGN), meta.get(FormattedLine.META_MARGIN));
        if (style != null)
            img.setAttribute("style", style);
    }

    /**
     * Inline style for an image's margin and block alignment. Margin applies to all
     * sides; a block alignment then overrides the horizontal margin on the auto
     * side(s) (the image sits on its own line, aligned via auto margins).
     */
    private String imageStyle(String align, String margin) {
        boolean hasAlign = (align != null) && !align.isEmpty();
        boolean hasMargin = (margin != null) && !margin.isEmpty();
        if (!hasAlign && !hasMargin)
            return null;
        StringBuilder sb = new StringBuilder();
        if (hasMargin)
            sb.append("margin:").append(margin).append("px;");
        if (hasAlign) {
            sb.append("display:block;");
            if ("center".equals(align))
                sb.append("margin-left:auto;margin-right:auto;");
            else if ("right".equals(align))
                sb.append("margin-left:auto;");
            else
                sb.append("margin-right:auto;");
        }
        return sb.toString();
    }

    private void handleEditorClick(Event evt) {
        Element target = Js.cast(evt.target);
        if ((target != null) && "IMG".equalsIgnoreCase(target.tagName))
            showImageOverlay(target);
        else
            hideImageOverlay();
    }

    private void handleDocumentMouseDown(Event evt) {
        if (imageOverlay == null)
            return;
        Node target = Js.cast(evt.target);
        if (target == null)
            return;
        // Ignore pointer-downs on the image itself or within the overlay.
        if ((selectedImage != null) && ((target == selectedImage) || selectedImage.contains(target)))
            return;
        if (imageOverlay.contains(target))
            return;
        hideImageOverlay();
    }

    private void showImageOverlay(Element img) {
        ensureOverlay();
        selectedImage = img;
        imageOverlay.style.setProperty("display", "block");
        positionOverlay();
    }

    private void hideImageOverlay() {
        selectedImage = null;
        endResize();
        if (imageOverlay != null)
            imageOverlay.style.setProperty("display", "none");
    }

    /**
     * Lazily builds the overlay frame with its four corner handles and the
     * alignment toolbar.
     */
    private void ensureOverlay() {
        if (imageOverlay != null)
            return;
        imageOverlay = styled("position:fixed;display:none;pointer-events:none;border:1px solid #4a90d9;box-sizing:border-box;z-index:1000;");
        imageOverlay.appendChild(handle("nw", "left:-5px;top:-5px;"));
        imageOverlay.appendChild(handle("ne", "right:-5px;top:-5px;"));
        imageOverlay.appendChild(handle("sw", "left:-5px;bottom:-5px;"));
        imageOverlay.appendChild(handle("se", "right:-5px;bottom:-5px;"));
        HTMLElement toolbar = styled("position:absolute;top:-30px;left:0;display:flex;gap:2px;pointer-events:auto;");
        toolbar.appendChild(alignButton("left", "←"));
        toolbar.appendChild(alignButton("center", "↔"));
        toolbar.appendChild(alignButton("right", "→"));
        toolbar.appendChild(marginButton("−", -4));
        toolbar.appendChild(marginButton("+", 4));
        imageOverlay.appendChild(toolbar);
        DomGlobal.document.body.appendChild(imageOverlay);
    }

    /** Positions the (fixed) overlay frame over the selected image. */
    private void positionOverlay() {
        if ((imageOverlay == null) || (selectedImage == null))
            return;
        DOMRect r = selectedImage.getBoundingClientRect();
        imageOverlay.style.setProperty("left", r.left + "px");
        imageOverlay.style.setProperty("top", r.top + "px");
        imageOverlay.style.setProperty("width", r.width + "px");
        imageOverlay.style.setProperty("height", r.height + "px");
    }

    private HTMLElement handle(String corner, String position) {
        HTMLElement h = styled("position:absolute;width:10px;height:10px;background:#fff;border:1px solid #4a90d9;box-sizing:border-box;pointer-events:auto;cursor:" + corner + "-resize;" + position);
        h.addEventListener("mousedown", evt -> startResize(corner, (MouseEvent) evt));
        return h;
    }

    private HTMLElement alignButton(String align, String label) {
        HTMLElement b = styled("pointer-events:auto;cursor:pointer;background:#4a90d9;color:#fff;border:none;border-radius:2px;width:22px;height:22px;font-size:12px;line-height:22px;text-align:center;");
        b.textContent = label;
        b.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            commitImageAlign(align);
        });
        return b;
    }

    private HTMLElement marginButton(String label, int delta) {
        HTMLElement b = styled("pointer-events:auto;cursor:pointer;background:#7a7a7a;color:#fff;border:none;border-radius:2px;width:22px;height:22px;font-size:14px;line-height:22px;text-align:center;");
        b.textContent = label;
        b.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            adjustImageMargin(delta);
        });
        return b;
    }

    private void startResize(String corner, MouseEvent evt) {
        if (selectedImage == null)
            return;
        evt.preventDefault();
        DOMRect r = selectedImage.getBoundingClientRect();
        resizeCorner = corner;
        resizeStartX = evt.clientX;
        resizeStartW = r.width;
        resizeAspect = (r.height > 0) ? (r.width / r.height) : 1.0;
        resizeMoveListener = e -> onResizeMove((MouseEvent) e);
        resizeUpListener = e -> endResize();
        DomGlobal.document.addEventListener("mousemove", resizeMoveListener);
        DomGlobal.document.addEventListener("mouseup", resizeUpListener);
    }

    private void onResizeMove(MouseEvent evt) {
        if (selectedImage == null)
            return;
        double dx = evt.clientX - resizeStartX;
        double sign = resizeCorner.endsWith("e") ? 1.0 : -1.0;
        double newW = resizeStartW + (sign * dx);
        if (newW < 20)
            newW = 20;
        double newH = (resizeAspect > 0) ? (newW / resizeAspect) : newW;
        selectedImage.setAttribute("width", String.valueOf((int) newW));
        selectedImage.setAttribute("height", String.valueOf((int) newH));
        positionOverlay();
    }

    private void endResize() {
        if (resizeMoveListener != null) {
            DomGlobal.document.removeEventListener("mousemove", resizeMoveListener);
            resizeMoveListener = null;
        }
        if (resizeUpListener != null) {
            DomGlobal.document.removeEventListener("mouseup", resizeUpListener);
            resizeUpListener = null;
        }
        if ((resizeCorner != null) && (selectedImage != null)) {
            DOMRect r = selectedImage.getBoundingClientRect();
            resizeCorner = null;
            commitImageSize((int) r.width, (int) r.height);
        }
        resizeCorner = null;
    }

    private void commitImageSize(int width, int height) {
        Element img = selectedImage;
        if (img == null)
            return;
        selectImageInModel(img);
        applyTransaction(Commands.setImageAttributes(state, width, height, null, null));
    }

    private void commitImageAlign(String align) {
        Element img = selectedImage;
        if (img == null)
            return;
        selectImageInModel(img);
        applyTransaction(Commands.setImageAttributes(state, null, null, align, null));
    }

    /** Steps the selected image's margin by {@code delta} pixels (clamped at 0). */
    private void adjustImageMargin(int delta) {
        Element img = selectedImage;
        if (img == null)
            return;
        selectImageInModel(img);
        int current = 0;
        String cur = imageMetaAtCursor(FormattedLine.META_MARGIN);
        if (cur != null) {
            try {
                current = Integer.parseInt(cur);
            } catch (NumberFormatException e) {
                current = 0;
            }
        }
        int next = Math.max(0, current + delta);
        applyTransaction(Commands.setImageAttributes(state, null, null, null, next));
    }

    /**
     * Reads a meta value of the image format at the current model cursor (used to read
     * the current margin before stepping it).
     */
    private String imageMetaAtCursor(String key) {
        Selection sel = state.selection();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int blockIdx = sel.anchorBlock();
        if ((blockIdx < 0) || (blockIdx >= blocks.size()))
            return null;
        int target = sel.anchorOffset();
        int lineStart = 0;
        for (FormattedLine line : blocks.get(blockIdx).getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                if (((lineStart + fmt.getIndex()) == target) && fmt.getFormats().contains(FormatType.IMG))
                    return (fmt.getMeta() != null) ? fmt.getMeta().get(key) : null;
            }
            lineStart += line.length() + 1;
        }
        return null;
    }

    /**
     * Places the model cursor on the given image (by positioning the DOM caret
     * immediately before it and syncing) so a subsequent image command targets it.
     */
    private void selectImageInModel(Element img) {
        Node parent = img.parentNode;
        if (parent == null)
            return;
        int idx = 0;
        Node n = parent.firstChild;
        while ((n != null) && (n != img)) {
            idx++;
            n = n.nextSibling;
        }
        elemental2.dom.Selection sel = DomGlobal.document.getSelection();
        if (sel != null)
            sel.collapse(parent, idx);
        syncSelectionFromDom();
    }

    private HTMLElement styled(String cssText) {
        HTMLElement el = Js.cast(DomGlobal.document.createElement("div"));
        el.setAttribute("style", cssText);
        return el;
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
            if (history.undo(state)) {
                render();
                if (stateListener != null)
                    stateListener.onContentChanged();
            }
            return;
        }
        if ((ctrl && shift && ("z".equals(ke.key) || "Z".equals(ke.key)))
                || (ctrl && "y".equals(ke.key))) {
            ke.preventDefault();
            if (history.redo(state)) {
                render();
                if (stateListener != null)
                    stateListener.onContentChanged();
            }
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

                // Empty list item / quote line: convert to paragraph instead of splitting
                // (so a trailing Enter exits the list or quote).
                if (sel.isCursor()
                        && blk.getType().is(BlockType.NLIST, BlockType.OLIST, BlockType.QUOTE)
                        && (Positions.contentSize(blk) == 0)) {
                    applyTransaction(Commands.setBlockType(state, BlockType.PARA));
                    break;
                }

                Transaction splitTr = Commands.splitBlock(state);
                if (splitTr != null) {
                    // Heading at end → new block becomes paragraph.
                    if (config().paragraphAfterHeading
                            && blk.getType().is(BlockType.H1, BlockType.H2, BlockType.H3, BlockType.H4, BlockType.H5)
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
                    // List item / quote at indent 0: exit (convert to paragraph).
                    if (blk2.getType().is(BlockType.NLIST, BlockType.OLIST, BlockType.QUOTE)) {
                        applyTransaction(Commands.setBlockType(state, BlockType.PARA));
                        break;
                    }
                    // Atomic previous block (fence/diagram/equation): the caret cannot
                    // enter it, so delete it as a unit rather than attempting a join.
                    if (sel2.anchorBlock() > 0) {
                        FormattedBlock prev = state.doc().getBlocks().get(sel2.anchorBlock() - 1);
                        if (atomicBlock(prev.getType())) {
                            Transaction tr2 = Transaction.create();
                            tr2.step(new DeleteBlockStep(sel2.anchorBlock() - 1));
                            tr2.setSelection(Selection.cursor(sel2.anchorBlock() - 1, 0));
                            applyTransaction(tr2);
                            break;
                        }
                        // Table previous block: traverse into its last cell rather than
                        // joining (the table is deleted via its context menus).
                        if (prev.getType() == BlockType.TABLE) {
                            handlerFor(BlockType.TABLE).focusBlockEnd(sel2.anchorBlock() - 1, ctx);
                            break;
                        }
                    }
                    // Join with previous block (cross-type allowed).
                    applyTransaction(Commands.forceJoinWithPrevious(state));
                    break;
                }
                if (sel2.isCursor()) {
                    // Images occupy a single sentinel character, so deleteCharBefore
                    // naturally removes an image when the caret sits after it (and
                    // only then) — no image special-case is needed.
                    // Atomic variable deletion: if cursor is inside or at the
                    // end of a variable, delete the entire variable as a unit.
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
                Selection selFwd = state.selection();
                if (selFwd.isCursor()) {
                    // Images occupy a single sentinel character, so deleteCharAfter
                    // naturally removes an image when the caret sits before it — no
                    // image special-case is needed.
                    // Atomic variable deletion: if cursor is inside or at the
                    // start of a variable, delete the entire variable as a unit.
                    int[] varRange = findVariableAt(selFwd.anchorBlock(), selFwd.anchorOffset());
                    if (varRange != null) {
                        state.setSelection(Selection.range(selFwd.anchorBlock(), varRange[0], selFwd.anchorBlock(), varRange[1]));
                        applyTransaction(Commands.deleteSelection(state));
                        break;
                    }
                    // Atomic next block (fence/diagram/equation): deleting forward from
                    // the end of the current block removes it as a unit. A table is
                    // traversed into (first cell) rather than deleted.
                    FormattedBlock curBlk = state.doc().getBlocks().get(selFwd.anchorBlock());
                    if ((selFwd.anchorOffset() >= Positions.contentSize(curBlk))
                            && (selFwd.anchorBlock() + 1 < state.doc().getBlocks().size())) {
                        FormattedBlock next = state.doc().getBlocks().get(selFwd.anchorBlock() + 1);
                        if (atomicBlock(next.getType())) {
                            Transaction trAtomic = Transaction.create();
                            trAtomic.step(new DeleteBlockStep(selFwd.anchorBlock() + 1));
                            trAtomic.setSelection(Selection.cursor(selFwd.anchorBlock(), selFwd.anchorOffset()));
                            applyTransaction(trAtomic);
                            break;
                        }
                        if (next.getType() == BlockType.TABLE) {
                            handlerFor(BlockType.TABLE).focusBlock(selFwd.anchorBlock() + 1, ctx);
                            break;
                        }
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
        // Pasted files (e.g. a screenshot or a document from the clipboard) are
        // uploaded via the configured handler and embedded at the cursor.
        IFileUploadHandler fileUpload = config().fileUpload;
        if (fileUpload != null) {
            elemental2.dom.File file = firstClipboardFile(evt);
            if (file != null) {
                evt.preventDefault();
                syncSelectionFromDom();
                uploadAndEmbed(fileUpload, file);
                return;
            }
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

    /**
     * Extracts the first file from a paste event's clipboard, or {@code null} if
     * the clipboard carries no file.
     */
    private elemental2.dom.File firstClipboardFile(elemental2.dom.Event evt) {
        elemental2.dom.ClipboardEvent ce = Js.uncheckedCast(evt);
        return firstFile(ce.clipboardData);
    }

    /**
     * Extracts the first file from a data transfer (clipboard or drag), or
     * {@code null} if it carries none.
     */
    private elemental2.dom.File firstFile(elemental2.dom.DataTransfer dt) {
        if ((dt == null) || (dt.files == null))
            return null;
        for (elemental2.dom.File file : dt.files.asList()) {
            if (file != null)
                return file;
        }
        return null;
    }

    /**
     * Uploads a file via the handler and embeds it at the current selection: an image
     * file becomes an inline image (the returned URL as its {@code src}); any other
     * file becomes a link labelled with its file name. A failed (or rejected) upload
     * leaves the document unchanged — the handler surfaces any user-facing message.
     */
    private void uploadAndEmbed(IFileUploadHandler fileUpload, elemental2.dom.File file) {
        boolean image = (file.type != null) && file.type.startsWith("image/");
        String name = file.name;
        fileUpload.upload(file, url -> {
            if ((url == null) || url.isEmpty())
                return;
            if (image)
                applyTransaction(Commands.insertImage(state, url));
            else
                applyTransaction(Commands.insertLink(state, url, name));
        }, err -> {
            // Best effort: a failed upload leaves the document unchanged.
        });
    }

    /**
     * Accepts a file drag (so the {@code drop} fires) when a file handler is
     * configured; other drags are left to their default handling.
     */
    private void handleDragOver(elemental2.dom.Event evt) {
        if ((config().fileUpload != null) && isFileDrag(evt))
            evt.preventDefault();
    }

    /**
     * Handles a dropped file: uploads it via the configured handler and embeds it at
     * the drop point (an inline image for image files, a link otherwise).
     */
    private void handleDrop(elemental2.dom.Event evt) {
        IFileUploadHandler fileUpload = config().fileUpload;
        if ((fileUpload == null) || !isFileDrag(evt))
            return;
        // We accepted the file drag on dragover; prevent the browser from opening it.
        evt.preventDefault();
        elemental2.dom.DragEvent de = Js.cast(evt);
        elemental2.dom.File file = firstFile(de.dataTransfer);
        if (file == null)
            return;
        placeCaretAtPoint(de.clientX, de.clientY);
        syncSelectionFromDom();
        uploadAndEmbed(fileUpload, file);
    }

    /**
     * Determines whether a drag event carries files (as opposed to text or other
     * content).
     */
    private boolean isFileDrag(elemental2.dom.Event evt) {
        elemental2.dom.DragEvent de = Js.cast(evt);
        elemental2.dom.DataTransfer dt = de.dataTransfer;
        if ((dt == null) || (dt.types == null))
            return false;
        for (int i = 0; i < dt.types.length; i++) {
            if ("Files".equals(dt.types.getAt(i)))
                return true;
        }
        return false;
    }

    /**
     * Moves the caret to the given viewport point (the drop location), so a dropped
     * image is inserted where it lands. Falls back to the current selection where the
     * browser does not support {@code caretPositionFromPoint}.
     */
    private void placeCaretAtPoint(double x, double y) {
        try {
            elemental2.dom.CaretPosition pos = DomGlobal.document.caretPositionFromPoint((int) x, (int) y);
            if ((pos == null) || (pos.offsetNode == null))
                return;
            elemental2.dom.Selection sel = DomGlobal.document.getSelection();
            if (sel != null)
                sel.collapse(pos.offsetNode, (int) pos.offset);
        } catch (Throwable e) {
            // Fall back to the current selection.
        }
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
        FORMAT_CLASSES.put(FormatType.CMT, "fmt_comment");
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
            position: relative;
        }
        .component:focus {
            outline: none;
        }
        .component[data-placeholder][data-empty]::before {
            content: attr(data-placeholder);
            position: absolute;
            top: calc(0.5em + 2px);
            left: 0.5em;
            color: var(--jui-ftext-placeholder-color, #9aa0a6);
            pointer-events: none;
        }
        .component .block {
            padding: 2px 0;
            min-height: 1em;
            white-space: pre-wrap;
        }
        .component .block:first-child { margin-top: 0; }
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
        /* Paragraph spacing is editor-specific (the read-only renderer spaces paragraphs
           differently), so it stays here. Headings, inline formats (fmt_*), quotes, code
           blocks and indent margins are all provided by the shared richtext stylesheet
           (FormattedTextStyles), scoped via the richtext class on the editor root. */
        .component p {
            margin: 0 0 0.2em 0;
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
