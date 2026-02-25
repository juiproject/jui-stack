package com.effacy.jui.text.ui.editor2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
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
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.KeyboardEvent;
import jsinterop.base.Js;

/**
 * Transaction-based rich text editor component.
 * <p>
 * Renders a {@link FormattedText} document as editable content and dispatches
 * all mutations through the transaction system ({@link Commands},
 * {@link EditorState}, {@link History}). The DOM is fully re-rendered after
 * each transaction, ensuring the view always matches the model.
 * <p>
 * Usage:
 * <pre>
 * Editor editor = new Editor();
 * editor.load(myDocument);
 * // ... add to a panel ...
 * FormattedText result = editor.value();
 * </pre>
 */
public class Editor extends SimpleComponent {

    /************************************************************************
     * State.
     ************************************************************************/

    private EditorState state;
    private History history;

    /**
     * When {@code true}, pressing Enter at the end of a heading block (H1–H3)
     * creates a new paragraph instead of another heading.
     */
    private boolean paragraphAfterHeading = true;

    /**
     * Formats the display label for ordered list items.
     */
    private IListIndexFormatter listIndexFormatter = Editor::defaultListIndex;

    /************************************************************************
     * DOM references.
     ************************************************************************/

    private Element editorEl;

    /**
     * Guards against selection sync during rendering.
     */
    private boolean rendering;

    /**
     * Toolbar button elements keyed by format type, for active-state tracking.
     */
    private Map<FormatType, Element> formatButtons = new HashMap<>();

    /**
     * Toolbar button elements keyed by block type, for active-state tracking.
     */
    private Map<BlockType, Element> blockTypeButtons = new HashMap<>();

    /************************************************************************
     * Construction.
     ************************************************************************/

    public Editor() {
        FormattedText doc = new FormattedText()
            .block(BlockType.PARA, b -> b.line(""));
        state = EditorState.create(doc);
        history = new History();
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            Div.$(root).style(styles().toolbar()).by("toolbar");
            Div.$(root).style(styles().editor()).attr("contenteditable", "true").by("editor");
        }).build(ctx -> {
            editorEl = ctx.first("editor");
            buildToolbar(ctx.first("toolbar"));
            render();
            attachEventListeners();
        });
    }

    /************************************************************************
     * Public API.
     ************************************************************************/

    /**
     * Configures whether pressing Enter at the end of a heading (H1–H3)
     * creates a new paragraph instead of continuing the heading.
     *
     * @param enable
     *               {@code true} to convert (default), {@code false} to
     *               retain heading type.
     * @return this editor for chaining.
     */
    public Editor paragraphAfterHeading(boolean enable) {
        this.paragraphAfterHeading = enable;
        return this;
    }

    /**
     * Configures the formatter used to generate ordered list markers. The
     * default cycles through numeric (1, 2, 3), lowercase alpha (a, b, c),
     * and lowercase roman (i, ii, iii) by indent level.
     *
     * @param formatter
     *                  the formatter (if {@code null}, reverts to default).
     * @return this editor for chaining.
     */
    public Editor listIndexFormatter(IListIndexFormatter formatter) {
        this.listIndexFormatter = (formatter != null) ? formatter : Editor::defaultListIndex;
        return this;
    }

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

    /************************************************************************
     * Toolbar.
     ************************************************************************/

    /**
     * Builds the toolbar buttons into the given container element.
     */
    private void buildToolbar(Element toolbar) {
        // Format toggles.
        formatButtons.put(FormatType.BLD, toolbarButton(toolbar, "B", "Bold (Ctrl+B)", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.BLD));
        }));
        formatButtons.put(FormatType.ITL, toolbarButton(toolbar, "I", "Italic (Ctrl+I)", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.ITL));
        }));
        formatButtons.put(FormatType.UL, toolbarButton(toolbar, "U", "Underline (Ctrl+U)", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.UL));
        }));
        formatButtons.put(FormatType.STR, toolbarButton(toolbar, "S", "Strikethrough", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.STR));
        }));
        formatButtons.put(FormatType.SUB, toolbarButton(toolbar, "x\u2082", "Subscript", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.SUB));
        }));
        formatButtons.put(FormatType.SUP, toolbarButton(toolbar, "x\u00B2", "Superscript", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.SUP));
        }));
        formatButtons.put(FormatType.CODE, toolbarButton(toolbar, "<>", "Code", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.CODE));
        }));
        formatButtons.put(FormatType.HL, toolbarButton(toolbar, "H", "Highlight", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.HL));
        }));

        // Separator.
        toolbarSeparator(toolbar);

        // Block type controls.
        blockTypeButtons.put(BlockType.H1, toolbarButton(toolbar, "H1", "Heading 1", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.H1));
        }));
        blockTypeButtons.put(BlockType.H2, toolbarButton(toolbar, "H2", "Heading 2", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.H2));
        }));
        blockTypeButtons.put(BlockType.H3, toolbarButton(toolbar, "H3", "Heading 3", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.H3));
        }));
        blockTypeButtons.put(BlockType.PARA, toolbarButton(toolbar, "\u00B6", "Paragraph", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.PARA));
        }));

        // Separator.
        toolbarSeparator(toolbar);

        // List controls.
        blockTypeButtons.put(BlockType.NLIST, toolbarButton(toolbar, "\u2022", "Bullet List", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleBlockType(state, BlockType.NLIST));
        }));
        blockTypeButtons.put(BlockType.OLIST, toolbarButton(toolbar, "1.", "Numbered List", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleBlockType(state, BlockType.OLIST));
        }));

        // Separator.
        toolbarSeparator(toolbar);

        // Table insertion.
        toolbarButton(toolbar, "\u229E", "Insert Table", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.insertTable(state, 2, 3));
        });
    }

    /**
     * Creates a single toolbar button. Uses {@code mousedown} with
     * {@code preventDefault} to avoid stealing focus from the editor.
     *
     * @return the created button element.
     */
    private Element toolbarButton(Element parent, String label, String tooltip, Runnable action) {
        Element btn = DomGlobal.document.createElement("button");
        btn.classList.add(styles().tbtn());
        btn.textContent = label;
        btn.setAttribute("title", tooltip);
        btn.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            action.run();
        });
        parent.appendChild(btn);
        return btn;
    }

    /**
     * Creates a visual separator in the toolbar.
     */
    private void toolbarSeparator(Element parent) {
        Element sep = DomGlobal.document.createElement("span");
        sep.classList.add(styles().tbtnSep());
        parent.appendChild(sep);
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    /**
     * Full re-render of the document into the editor element.
     */
    private void render() {
        rendering = true;
        try {
            editorEl.innerHTML = "";
            List<FormattedBlock> blocks = state.doc().getBlocks();
            int[] listCounters = new int[6];
            boolean prevWasOlist = false;
            int prevOlistIndent = 0;
            for (int i = 0; i < blocks.size(); i++) {
                FormattedBlock blk = blocks.get(i);
                if (blk.getType() == BlockType.OLIST) {
                    int ind = blk.getIndent();
                    if (!prevWasOlist) {
                        for (int j = 0; j < listCounters.length; j++)
                            listCounters[j] = 0;
                    } else if (ind > prevOlistIndent) {
                        for (int j = prevOlistIndent + 1; j < listCounters.length; j++)
                            listCounters[j] = 0;
                    }
                    listCounters[ind]++;
                    prevOlistIndent = ind;
                    prevWasOlist = true;
                } else {
                    prevWasOlist = false;
                }
                Element el = renderBlock(blk, i);
                if (blk.getType() == BlockType.OLIST)
                    el.setAttribute("data-list-index", listIndexFormatter.format(blk.getIndent(), listCounters[blk.getIndent()]));
                editorEl.appendChild(el);
            }
        } finally {
            rendering = false;
        }
        restoreSelection();
        updateToolbarState();
    }

    /**
     * Creates a DOM element for a single block.
     */
    private Element renderBlock(FormattedBlock block, int index) {
        if (block.getType() == BlockType.TABLE)
            return renderTable(block, index);

        Element el = createBlockElement(block.getType());
        el.setAttribute("data-block-index", String.valueOf(index));
        el.classList.add(styles().block());
        if (block.getIndent() > 0)
            el.classList.add("indent" + block.getIndent());

        List<FormattedLine> lines = block.getLines();
        boolean empty = lines.isEmpty()
            || ((lines.size() == 1) && (lines.get(0).length() == 0));

        if (empty) {
            // Empty blocks need a BR for contenteditable cursor placement.
            el.appendChild(DomGlobal.document.createElement("br"));
        } else {
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0)
                    el.appendChild(DomGlobal.document.createElement("br"));
                renderLine(el, lines.get(i));
            }
            // Trailing BR needed when the last line is empty so the browser
            // can position the cursor on the new line.
            if (lines.get(lines.size() - 1).length() == 0) {
                Element br = DomGlobal.document.createElement("br");
                br.setAttribute("data-trailing", "true");
                el.appendChild(br);
            }
        }
        return el;
    }

    /**
     * Renders a TABLE block as a non-editable wrapper containing a
     * {@code <table>} element and hover controls for adding rows/columns.
     */
    private Element renderTable(FormattedBlock table, int index) {
        Element wrapper = DomGlobal.document.createElement("div");
        wrapper.classList.add(styles().tableWrapper());
        wrapper.setAttribute("contenteditable", "false");
        wrapper.setAttribute("data-block-index", String.valueOf(index));

        // Read metadata.
        int headers = 0;
        String headersStr = table.meta("headers");
        if (headersStr != null) {
            try {
                headers = Integer.parseInt(headersStr);
            } catch (NumberFormatException e) {
                // Ignore.
            }
        }
        String alignStr = table.meta("align");
        String[] align = (alignStr != null) ? alignStr.split(",") : null;

        // Build <table>.
        Element tableEl = DomGlobal.document.createElement("table");
        tableEl.classList.add(styles().table());
        int rowIndex = 0;
        for (FormattedBlock row : table.getBlocks()) {
            if (row.getType() != BlockType.TROW)
                continue;
            Element tr = DomGlobal.document.createElement("tr");
            int cellIndex = 0;
            for (FormattedBlock cell : row.getBlocks()) {
                if (cell.getType() != BlockType.TCELL)
                    continue;
                boolean isHeader = (rowIndex < headers);
                Element td = DomGlobal.document.createElement(isHeader ? "th" : "td");
                td.classList.add(styles().tableCell());
                if ((align != null) && (cellIndex < align.length)) {
                    if ("C".equals(align[cellIndex].trim()))
                        ((elemental2.dom.HTMLElement) td).style.set("text-align", "center");
                    else if ("R".equals(align[cellIndex].trim()))
                        ((elemental2.dom.HTMLElement) td).style.set("text-align", "right");
                }
                // Render cell content.
                List<FormattedLine> lines = cell.getLines();
                if ((lines == null) || lines.isEmpty()) {
                    td.innerHTML = "&nbsp;";
                } else {
                    boolean hasContent = false;
                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).length() > 0)
                            hasContent = true;
                        if (i > 0)
                            td.appendChild(DomGlobal.document.createElement("br"));
                        renderLine(td, lines.get(i));
                    }
                    if (!hasContent)
                        td.innerHTML = "&nbsp;";
                }
                tr.appendChild(td);
                cellIndex++;
            }
            tableEl.appendChild(tr);
            rowIndex++;
        }
        wrapper.appendChild(tableEl);

        // Add column control (right edge).
        Element addCol = DomGlobal.document.createElement("div");
        addCol.classList.add(styles().tableAddCol());
        addCol.textContent = "+";
        addCol.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            applyTransaction(Commands.addTableColumn(state, index));
        });
        wrapper.appendChild(addCol);

        // Add row control (bottom edge).
        Element addRow = DomGlobal.document.createElement("div");
        addRow.classList.add(styles().tableAddRow());
        addRow.textContent = "+";
        addRow.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            evt.stopPropagation();
            applyTransaction(Commands.addTableRow(state, index));
        });
        wrapper.appendChild(addRow);

        return wrapper;
    }

    /**
     * Maps block type to HTML element tag.
     */
    private Element createBlockElement(BlockType type) {
        if (type == BlockType.H1)
            return DomGlobal.document.createElement("h1");
        if (type == BlockType.H2)
            return DomGlobal.document.createElement("h2");
        if (type == BlockType.H3)
            return DomGlobal.document.createElement("h3");
        Element el = DomGlobal.document.createElement("p");
        if (type == BlockType.NLIST)
            el.classList.add(styles().listBullet());
        else if (type == BlockType.OLIST)
            el.classList.add(styles().listNumber());
        return el;
    }

    /**
     * Renders a single line's formatted content into a parent element.
     */
    private void renderLine(Element parent, FormattedLine line) {
        line.sequence().forEach(segment -> {
            if (segment.formatting().length == 0) {
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
            EditorSupport2.setCursor(editorEl, sel.anchorBlock(), sel.anchorOffset());
        else
            EditorSupport2.setSelection(editorEl, sel.anchorBlock(), sel.anchorOffset(), sel.headBlock(), sel.headOffset());
    }

    /**
     * Reads the DOM selection and updates the editor state.
     */
    private void syncSelectionFromDom() {
        if (rendering)
            return;
        int[] sel = EditorSupport2.readSelection(editorEl);
        if (sel == null)
            return;
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
        Selection sel = state.selection();
        int blockIdx = sel.anchorBlock();
        List<FormattedBlock> blocks = state.doc().getBlocks();
        if ((blockIdx < 0) || (blockIdx >= blocks.size()))
            return;
        FormattedBlock blk = blocks.get(blockIdx);

        // Block type buttons — one active at a time.
        for (Map.Entry<BlockType, Element> entry : blockTypeButtons.entrySet()) {
            if (blk.getType() == entry.getKey())
                entry.getValue().classList.add(styles().tbtnActive());
            else
                entry.getValue().classList.remove(styles().tbtnActive());
        }

        // Format buttons — based on format at cursor or across selection.
        for (Map.Entry<FormatType, Element> entry : formatButtons.entrySet()) {
            if (isFormatActive(sel, entry.getKey()))
                entry.getValue().classList.add(styles().tbtnActive());
            else
                entry.getValue().classList.remove(styles().tbtnActive());
        }
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
     * Applies a transaction, pushes inverse to history, and re-renders.
     */
    private void applyTransaction(Transaction tr) {
        if (tr == null)
            return;
        Transaction inverse = state.apply(tr);
        history.push(inverse);
        render();
    }

    /************************************************************************
     * Event handling.
     ************************************************************************/

    private void attachEventListeners() {
        editorEl.addEventListener("keydown", evt -> handleKeyDown(Js.uncheckedCast(evt)));
        editorEl.addEventListener("beforeinput", evt -> handleBeforeInput(evt));
        editorEl.addEventListener("paste", evt -> handlePaste(evt));
        DomGlobal.document.addEventListener("selectionchange", evt -> syncSelectionFromDom());
    }

    /**
     * Handles keyboard shortcuts (undo/redo, format toggles, indent).
     */
    private void handleKeyDown(KeyboardEvent ke) {
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
     */
    private void handleBeforeInput(elemental2.dom.Event evt) {
        evt.preventDefault();
        syncSelectionFromDom();

        String inputType = EditorSupport2.getInputType(evt);
        if (inputType == null)
            return;

        switch (inputType) {
            case "insertText": {
                String data = EditorSupport2.getInputData(evt);
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
                    if (paragraphAfterHeading
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
                applyTransaction(Commands.deleteCharBefore(state));
                break;
            }
            case "deleteContentForward": {
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
     * it via the transaction system.
     */
    private void handlePaste(elemental2.dom.Event evt) {
        evt.preventDefault();
        syncSelectionFromDom();
        String text = EditorSupport2.getClipboardText(evt);
        if ((text == null) || text.isEmpty())
            return;
        // Normalize line endings (Windows \r\n and old Mac \r).
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        applyTransaction(Commands.pasteText(state, text));
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

        String toolbar();

        String tbtn();

        String tbtnActive();

        String tbtnSep();

        String editor();

        String block();

        String listBullet();

        String listNumber();

        String tableWrapper();

        String table();

        String tableCell();

        String tableAddCol();

        String tableAddRow();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .component {
            position: relative;
            border: 1px solid #ddd;
            border-radius: 6px;
            display: flex;
            flex-direction: column;
            overflow: auto;
            min-height: 500px;
        }
        .toolbar {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 2px;
            padding: 4px 6px;
            border-bottom: 1px solid #ddd;
            background: #fafafa;
            flex-shrink: 0;
        }
        .tbtn {
            border: none;
            background: none;
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.85em;
            font-weight: 500;
            color: #444;
            line-height: 1.4;
        }
        .tbtn:hover {
            background: #e8e8e8;
        }
        .tbtnActive {
            background: #dbeafe;
            color: #1d4ed8;
        }
        .tbtnSep {
            width: 1px;
            height: 1.2em;
            background: #ccc;
            margin: 0 4px;
        }
        .editor {
            outline: none;
            min-height: 2em;
            cursor: text;
            padding: 0.5em;
            flex: 1;
            overflow: auto;
        }
        .editor:focus {
            outline: none;
        }
        .block {
            margin: 0 0 0.15em 0;
            padding: 2px 0;
            min-height: 1em;
            white-space: pre-wrap;
        }
        .listBullet {
            position: relative;
            padding-left: 1.5em;
        }
        .listBullet::before {
            position: absolute;
            left: 0.35em;
            content: '\\2022';
        }
        .listNumber {
            position: relative;
            padding-left: 1.5em;
        }
        .listNumber::before {
            position: absolute;
            left: 0.15em;
            content: attr(data-list-index) '.';
        }
        .editor h1 {
            font-size: 1.8em;
            font-weight: 500;
            margin: 0 0 0.15em 0;
        }
        .editor h2 {
            font-size: 1.5em;
            font-weight: 500;
            margin: 0 0 0.15em 0;
        }
        .editor h3 {
            font-size: 1.25em;
            font-weight: 500;
            margin: 0 0 0.15em 0;
        }
        .editor p {
            margin: 0 0 0.15em 0;
        }
        .editor .indent1 { margin-left: 1.5em; }
        .editor .indent2 { margin-left: 3em; }
        .editor .indent3 { margin-left: 4.5em; }
        .editor .indent4 { margin-left: 6em; }
        .editor .indent5 { margin-left: 7.5em; }
        .editor .fmt_bold { font-weight: 600; }
        .editor .fmt_italic { font-style: italic; }
        .editor .fmt_underline { text-decoration: underline; }
        .editor .fmt_strike { text-decoration: line-through; }
        .editor .fmt_strike.fmt_underline { text-decoration: underline line-through; }
        .editor .fmt_superscript { vertical-align: super; font-size: 0.8em; }
        .editor .fmt_subscript { vertical-align: sub; font-size: 0.8em; }
        .editor .fmt_highlight { background-color: #F5EB72; }
        .editor .fmt_code {
            font-family: "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace;
            line-height: normal;
            background: rgba(135,131,120,.15);
            color: #EB5757;
            border-radius: 4px;
            font-size: 85%;
            padding: 0.2em 0.4em;
        }
        .tableWrapper {
            position: relative;
            margin: 0.5em 0;
            padding-right: 24px;
            padding-bottom: 24px;
            user-select: none;
        }
        .table {
            border-collapse: collapse;
            width: 100%;
        }
        .tableCell {
            border: 1px solid #ddd;
            padding: 6px 10px;
            min-width: 40px;
            min-height: 1.4em;
            vertical-align: top;
        }
        .tableAddCol {
            position: absolute;
            right: 0;
            top: 0;
            bottom: 24px;
            width: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.15s;
            background: #f0f0f0;
            border-radius: 0 4px 4px 0;
            color: #666;
            font-size: 1.1em;
        }
        .tableWrapper:hover .tableAddCol {
            opacity: 1;
        }
        .tableAddCol:hover {
            background: #dbeafe;
            color: #1d4ed8;
        }
        .tableAddRow {
            position: absolute;
            left: 0;
            right: 24px;
            bottom: 0;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.15s;
            background: #f0f0f0;
            border-radius: 0 0 4px 4px;
            color: #666;
            font-size: 1.1em;
        }
        .tableWrapper:hover .tableAddRow {
            opacity: 1;
        }
        .tableAddRow:hover {
            background: #dbeafe;
            color: #1d4ed8;
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
