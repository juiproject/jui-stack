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
        toolbarButton(toolbar, "B", "Bold (Ctrl+B)", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.BLD));
        });
        toolbarButton(toolbar, "I", "Italic (Ctrl+I)", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.ITL));
        });
        toolbarButton(toolbar, "U", "Underline (Ctrl+U)", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.UL));
        });
        toolbarButton(toolbar, "S", "Strikethrough", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.STR));
        });
        toolbarButton(toolbar, "x\u2082", "Subscript", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.SUB));
        });
        toolbarButton(toolbar, "x\u00B2", "Superscript", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.SUP));
        });
        toolbarButton(toolbar, "<>", "Code", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.CODE));
        });
        toolbarButton(toolbar, "H", "Highlight", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleFormat(state, FormatType.HL));
        });

        // Separator.
        toolbarSeparator(toolbar);

        // Block type controls.
        toolbarButton(toolbar, "H1", "Heading 1", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.H1));
        });
        toolbarButton(toolbar, "H2", "Heading 2", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.H2));
        });
        toolbarButton(toolbar, "H3", "Heading 3", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.H3));
        });
        toolbarButton(toolbar, "\u00B6", "Paragraph", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.setBlockType(state, BlockType.PARA));
        });

        // Separator.
        toolbarSeparator(toolbar);

        // List controls.
        toolbarButton(toolbar, "\u2022", "Bullet List", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleBlockType(state, BlockType.NLIST));
        });
        toolbarButton(toolbar, "1.", "Numbered List", () -> {
            syncSelectionFromDom();
            applyTransaction(Commands.toggleBlockType(state, BlockType.OLIST));
        });
    }

    /**
     * Creates a single toolbar button. Uses {@code mousedown} with
     * {@code preventDefault} to avoid stealing focus from the editor.
     */
    private void toolbarButton(Element parent, String label, String tooltip, Runnable action) {
        Element btn = DomGlobal.document.createElement("button");
        btn.classList.add(styles().tbtn());
        btn.textContent = label;
        btn.setAttribute("title", tooltip);
        btn.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            action.run();
        });
        parent.appendChild(btn);
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
    }

    /**
     * Creates a DOM element for a single block.
     */
    private Element renderBlock(FormattedBlock block, int index) {
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
            content: '\2022';
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
