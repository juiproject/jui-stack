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
import com.effacy.jui.text.ui.type.ContentStyle;
import com.effacy.jui.text.ui.type.ListIndex;
import com.effacy.jui.text.ui.type.ILinkHandler;
import com.effacy.jui.text.ui.type.LinkHandlers;
import com.effacy.jui.text.ui.type.LinkSupport;
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
    /**
     * How the editor treats a click on a link.
     */
    public enum LinkInteraction {

        /**
         * Clicking a link positions the caret inside it for inline editing (the default;
         * suits a surface with a distinct edit mode).
         */
        EDIT,

        /**
         * Clicking a link opens it (via the configured {@code linkOpenHandler}, else a
         * new browser tab); hovering reveals a small card to open or edit the link. Suits
         * an always-editable surface with no separate view mode.
         */
        NAVIGATE;
    }

    public static class Config extends Component.Config {

        boolean paragraphAfterHeading = true;
        IListIndexFormatter listIndexFormatter = Editor::defaultListIndex;
        boolean debugLog;
        String placeholder;
        IFileUploadHandler fileUpload;
        LinkInteraction linkInteraction = LinkInteraction.EDIT;
        ILinkHandler linkHandler = LinkHandlers.standard();
        ContentStyle contentStyle = ContentStyle.compact();

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

        /**
         * Configures how a click on a link is treated (default {@link LinkInteraction#EDIT}).
         * See {@link LinkInteraction}.
         */
        public Config linkInteraction(LinkInteraction linkInteraction) {
            this.linkInteraction = (linkInteraction == null) ? LinkInteraction.EDIT : linkInteraction;
            return this;
        }

        /**
         * Configures the link handler — what happens when a link is activated in
         * {@link LinkInteraction#NAVIGATE} mode (a click, or the hover card's <em>Open</em>).
         * <p>
         * Defaults to {@link LinkHandlers#standard()} (external links open in a new tab; in-page
         * {@code #anchor} links scroll within the content and never reach an SPA hash router;
         * other schemes are left to the browser). Pass {@link LinkHandlers#standard(ILinkHandler)}
         * with an application fallback to route custom schemes (e.g. an internal {@code doc:}
         * reference). This is the same {@link ILinkHandler} the read-only renderer / {@code FText}
         * accepts, so links behave identically in both.
         *
         * @param linkHandler
         *                    the handler ({@code null} lets links follow their {@code href}).
         * @return this configuration.
         */
        public Config linkHandler(ILinkHandler linkHandler) {
            this.linkHandler = linkHandler;
            return this;
        }

        /**
         * Configures the content presentation (default is the compact stylesheet). See
         * {@link ContentStyle} — pass a standard configuration ({@code ContentStyle.document()})
         * or your own.
         */
        public Config contentStyle(ContentStyle contentStyle) {
            this.contentStyle = (contentStyle == null) ? ContentStyle.compact () : contentStyle;
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
            // Scope the content to the shared richtext stylesheet and layer the content style's
            // token overrides — one call does both. This is the same FormattedTextStyles sheet the
            // read-only renderer uses (so the two present identically), and the same ContentStyle is
            // usable on a read-only presentation (see FText), so a style means the same everywhere.
            // No overrides == the stylesheet's compact defaults.
            ContentStyle contentStyle = (data.contentStyle != null) ? data.contentStyle : ContentStyle.compact();
            HTMLElement styleEl = Js.uncheckedCast(el);
            contentStyle.apply(styleEl);
            // In NAVIGATE mode links are clickable — mark the root so links get a pointer cursor.
            if (data.linkInteraction == LinkInteraction.NAVIGATE)
                el.classList.add(styles().navigate());
            render();
            attachEventListeners();
        });
    }

    /************************************************************************
     * Public API.
     ************************************************************************/

    /**
     * Places the caret in the editor.
     * <p>
     * The base implementation focuses the component's <em>managed focus
     * element</em>, and this component registers none — its editable surface is the
     * root itself, carrying {@code contenteditable}, which the focus manager knows
     * nothing about. So the inherited {@code focus()} resolved to no element and
     * moved no caret, and a caller had no supported way to put the cursor in a rich
     * editor at all.
     * <p>
     * That is not a cosmetic gap. Focus not taken is focus that cannot be lost, so
     * everything hung off losing it — the {@code focus} styling, and any
     * {@link Config#onFocusLost} flush on the enclosing control — never ran either.
     * <p>
     * Focusing the element natively also re-establishes the editor's own selection
     * state: the {@code focus} listener installed at render syncs it from the DOM.
     */
    @Override
    public void focus() {
        if (editorEl == null)
            return;
        HTMLElement el = Js.uncheckedCast(editorEl);
        el.focus();
    }

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
            public String currentLinkLabel() {
                syncSelectionFromDom();
                return extractLinkLabel(state.selection());
            }

            @Override
            public void applyLink(String url) {
                doApplyLink(url, null);
            }

            @Override
            public void applyLink(String url, String label) {
                doApplyLink(url, label);
            }

            @Override
            public void removeLink() {
                doRemoveLink();
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
     * The elements from the previous render that may be re-appended rather than rebuilt,
     * keyed by their {@link IBlockHandler#renderKey(FormattedBlock)}. Only atomic blocks
     * offer a key; see that method for why, and for what a key has to cover.
     */
    private Map<String, Element> reusable = new HashMap<>();

    /**
     * Full re-render of the document into the editor element. Each block is
     * delegated to the appropriate {@link IBlockHandler}.
     * <p>
     * A block whose handler offers a {@link IBlockHandler#renderKey(FormattedBlock) render
     * key} matching one from the previous pass keeps the element it already has: it is
     * detached with the rest, then re-appended in place of a fresh render. Every
     * transaction comes through here, so for an atomic block that is the difference
     * between rendering on its own source changing and rendering on every keystroke in the
     * document. It also settles the flicker on the asynchronous ones — a Mermaid render in
     * flight completes into the element that is still mounted, rather than into one that
     * was discarded a keystroke later.
     */
    private void render() {
        // The DOM (and any selected image element) is rebuilt, so dismiss the overlay.
        hideImageOverlay();
        rendering = true;
        try {
            // The previous pass's reusable elements. Detaching them (below) leaves the
            // subtrees intact, so a re-appended one brings its rendering — a decoded
            // image, laid-out SVG — with it.
            Map<String, Element> prior = reusable;
            reusable = new HashMap<>();
            editorEl.innerHTML = "";
            List<FormattedBlock> blocks = state.doc().getBlocks();
            handlers.forEach(h -> h.beginRender(ctx));
            Map<String, Integer> headingSlugs = new HashMap<>();
            for (int i = 0; i < blocks.size(); i++) {
                FormattedBlock block = blocks.get(i);
                IBlockHandler handler = handlerFor(block.getType());
                String key = handler.renderKey(block);
                Element el = null;
                if (key != null) {
                    // Removed, not read: two blocks with the same key are distinct blocks
                    // and cannot share one element, so the second renders afresh.
                    el = prior.remove(key);
                    if (el != null)
                        el.setAttribute("data-block-index", String.valueOf(i));
                }
                if (el == null)
                    el = handler.render(block, i, ctx);
                // First one wins, so a repeated block offers the same element for reuse
                // each pass rather than the two taking turns being the rebuilt one.
                if ((key != null) && !reusable.containsKey(key))
                    reusable.put(key, el);
                assignHeadingId(el, block, headingSlugs);
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
     * Gives a rendered heading a slug id so an in-page {@code #anchor} click scrolls to it in the
     * editor too (mirrors the read-only renderer). Repeated heading text is de-duplicated
     * ({@code slug}, {@code slug-1}, …). Regenerated on each full render.
     */
    private void assignHeadingId(Element el, FormattedBlock block, Map<String, Integer> slugs) {
        if (!block.getType().is(BlockType.H1, BlockType.H2, BlockType.H3, BlockType.H4, BlockType.H5))
            return;
        String base = LinkHandlers.slug(block.flatten());
        if (base.isEmpty())
            return;
        Integer seen = slugs.get(base);
        String id = (seen == null) ? base : (base + "-" + seen);
        slugs.put(base, (seen == null) ? 1 : (seen + 1));
        el.setAttribute("id", id);
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
        // Selection change is a document-level event, so this fires for every
        // editor on the page whenever the cursor moves in any of them. Without
        // this guard an editor treats a selection belonging to a sibling as its
        // own "not in a block" case below and runs its handlers over it, so two
        // editors on one surface drive each other's toolbars. Harmless while
        // there is only ever one, which is why it went unnoticed.
        if (!EditorSupport.containsSelection(editorEl))
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
     * Deactivates every toolbar button.
     * <p>
     * A null block type matches no block tool and an empty format set matches no
     * format tool, so each handle turns itself off — no separate "clear" path is
     * needed on the tools themselves.
     */
    private void clearToolbarState() {
        if (stateListener == null)
            return;
        stateListener.onStateUpdate(null, java.util.EnumSet.noneOf(FormatType.class), false);
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
     * Extracts the display text (label) of the link at the anchor position, or the
     * selected text when the selection is a (single-block) range with no link. Returns
     * {@code null} when there is nothing to label. Used to pre-fill the link panel and
     * to decide whether an apply changed the label.
     */
    private String extractLinkLabel(Selection sel) {
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int blockIdx = sel.anchorBlock();
        if ((blockIdx < 0) || (blockIdx >= blocks.size()))
            return null;
        FormattedBlock blk = blocks.get(blockIdx);
        // Cursor inside a link run: the run's text.
        int target = sel.anchorOffset();
        int lineStart = 0;
        for (FormattedLine line : blk.getLines()) {
            for (FormattedLine.Format fmt : line.getFormatting()) {
                int absStart = lineStart + fmt.getIndex();
                int absEnd = absStart + fmt.getLength();
                if ((target >= absStart) && (target < absEnd) && fmt.getFormats().contains(FormatType.A)) {
                    String t = line.getText();
                    int s = fmt.getIndex();
                    int e = Math.min(fmt.getIndex() + fmt.getLength(), t.length());
                    if ((s >= 0) && (s < e))
                        return t.substring(s, e);
                }
            }
            lineStart += line.length() + 1;
        }
        // Range selection with no link: the selected text is the label.
        if (!sel.isCursor() && (sel.fromBlock() == sel.toBlock())) {
            String flat = blk.flatten();
            int from = sel.fromOffset();
            int to = sel.toOffset();
            if ((from >= 0) && (to <= flat.length()) && (from < to))
                return flat.substring(from, to);
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
        // While an IME is composing, what is on screen is the IME's working text rather
        // than settled input, so native typing stands down and the ordinary prevented
        // path applies throughout (see tryNativeInsert).
        editorEl.addEventListener("compositionstart", evt -> composing = true);
        editorEl.addEventListener("compositionend", evt -> composing = false);
        editorEl.addEventListener("paste", evt -> handlePaste(evt));
        editorEl.addEventListener("dragover", evt -> handleDragOver(evt));
        editorEl.addEventListener("drop", evt -> handleDrop(evt));
        editorEl.addEventListener("click", evt -> handleEditorClick(evt));
        // Link navigate / hover-card (NAVIGATE mode only).
        editorEl.addEventListener("mouseover", evt -> handleEditorMouseOver(evt));
        editorEl.addEventListener("mouseout", evt -> handleEditorMouseOut(evt));
        // Dismiss the image overlay on a pointer-down outside it (and outside the image).
        DomGlobal.document.addEventListener("mousedown", evt -> handleDocumentMouseDown(evt));
        DomGlobal.document.addEventListener("selectionchange", evt -> syncSelectionFromDom());
        // The toolbar reflects the selection, and a selection the user cannot see
        // is not something to reflect: without this the buttons keep the state
        // they held when focus left, so an editor sitting idle on the page shows
        // bold and bullet-list lit up as though they were about to apply to
        // something. Re-established on focus by the next selection sync.
        editorEl.addEventListener("blur", evt -> clearToolbarState());
        editorEl.addEventListener("focus", evt -> syncSelectionFromDom());
        // Keep the overlay aligned to the image while scrolling would be involved; the
        // simplest robust behaviour is to dismiss it on scroll.
        DomGlobal.document.addEventListener("scroll", evt -> {
            hideImageOverlay();
            hideLinkCard();
        }, true);
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
        // NAVIGATE mode: a click on a link activates it (via the link handler) rather than
        // placing the caret. LinkSupport suppresses the default nav (always for #anchors, so
        // an SPA hash router is never triggered).
        if (config().linkInteraction == LinkInteraction.NAVIGATE) {
            Element anchor = anchorAncestor(target);
            if (anchor != null) {
                LinkSupport.handleClick(evt, editorEl, config().linkHandler);
                return;
            }
        }
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
        imageOverlay = classed(overlayCss().imgOverlay());
        imageOverlay.appendChild(handle("nw", "left:-5px;top:-5px;"));
        imageOverlay.appendChild(handle("ne", "right:-5px;top:-5px;"));
        imageOverlay.appendChild(handle("sw", "left:-5px;bottom:-5px;"));
        imageOverlay.appendChild(handle("se", "right:-5px;bottom:-5px;"));
        HTMLElement toolbar = classed(overlayCss().imgToolbar());
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
        HTMLElement h = classed(overlayCss().imgHandle());
        // Dynamic per-corner bits (cursor + which corner it sits at) stay inline.
        h.setAttribute("style", "cursor:" + corner + "-resize;" + position);
        h.addEventListener("mousedown", evt -> startResize(corner, (MouseEvent) evt));
        return h;
    }

    private HTMLElement alignButton(String align, String label) {
        HTMLElement b = classed(overlayCss().imgBtn());
        b.textContent = label;
        b.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            commitImageAlign(align);
        });
        return b;
    }

    private HTMLElement marginButton(String label, int delta) {
        HTMLElement b = classed(overlayCss().imgBtn(), overlayCss().imgBtnMuted());
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

    /** A {@code <div>} carrying the given (token-driven) overlay CSS class(es). */
    private HTMLElement classed(String... classNames) {
        HTMLElement el = Js.cast(DomGlobal.document.createElement("div"));
        for (String cls : classNames)
            el.classList.add(cls);
        return el;
    }

    private EditorOverlayCSS overlayCss() {
        return EditorOverlayCSS.Styles.instance();
    }

    /************************************************************************
     * Link interaction (NAVIGATE mode: click-to-open + hover card).
     *
     * In {@link LinkInteraction#NAVIGATE} mode a click on a link opens it (rather than
     * placing the caret) and hovering a link shows a small floating card with the URL
     * and Open / Edit actions. Edit targets the hovered link in the model and opens the
     * {@link LinkPanel} (URL + label). The card mirrors the image overlay: a fixed body
     * element positioned relative to the link, dismissed on scroll / re-render / leave.
     ************************************************************************/

    /** The floating link card (lazily created, appended to {@code body}). */
    private HTMLElement linkCard;

    /** The URL text element within {@link #linkCard}. */
    private HTMLElement linkCardUrl;

    /** The link the card currently targets (pending or shown), or {@code null}. */
    private Element hoveredLink;

    /** Whether the card is currently displayed (as opposed to a pending, delayed show). */
    private boolean linkCardVisible;

    /** Delay (ms) before a hovered link reveals its card, so a passing pointer doesn't flash it. */
    private static final int LINK_CARD_SHOW_DELAY = 450;

    /** Pending show timer (setTimeout id), {@code -1} when none. */
    private double linkCardShowTimer = -1;

    /** Pending hide timer (setTimeout id), {@code -1} when none — a small grace so the
     *  pointer can travel from the link to the card without the card vanishing. */
    private double linkCardHideTimer = -1;

    /** Applies (or updates) a link's URL and, when it changed, its display text. */
    private void doApplyLink(String url, String label) {
        Transaction tr;
        // Only rewrite the display text when a non-empty label differs from what is
        // there now — otherwise keep updateLink, which preserves any inline formatting.
        String current = extractLinkLabel(state.selection());
        if ((label != null) && !label.isEmpty() && !label.equals(current))
            tr = Commands.updateLinkContent(state, url, label);
        else {
            tr = Commands.updateLink(state, url);
            // Open space (no selection, no link under the cursor): insert the label
            // (or the URL itself) as the linked text.
            if (tr == null)
                tr = Commands.insertLink(state, url, label);
        }
        if (tr != null)
            applyTransaction(tr);
    }

    private void doRemoveLink() {
        applyTransaction(Commands.removeLink(state));
    }

    /** The nearest ancestor {@code <a href>} of {@code el} within the editor, else null. */
    private Element anchorAncestor(Element el) {
        Element cur = el;
        while ((cur != null) && (cur != editorEl)) {
            if ("A".equalsIgnoreCase(cur.tagName) && cur.hasAttribute("href"))
                return cur;
            cur = cur.parentElement;
        }
        return null;
    }

    /** Activates a link via the configured handler; falls back to opening in a new tab. */
    private void openLink(Element anchor, String href) {
        if ((href == null) || href.isEmpty())
            return;
        ILinkHandler handler = config().linkHandler;
        if ((handler != null) && handler.activate(href, anchor, editorEl))
            return;
        DomGlobal.window.open(href, "_blank");
    }

    private void handleEditorMouseOver(Event evt) {
        if (config().linkInteraction != LinkInteraction.NAVIGATE)
            return;
        Element anchor = anchorAncestor(Js.cast(evt.target));
        if (anchor != null)
            scheduleShowLinkCard(anchor);
    }

    private void handleEditorMouseOut(Event evt) {
        if ((config().linkInteraction != LinkInteraction.NAVIGATE) || (hoveredLink == null))
            return;
        Node related = Js.uncheckedCast(((MouseEvent) evt).relatedTarget);
        // Keep the card while the pointer is still on the link or has moved onto the card.
        if ((related != null) && (hoveredLink.contains(related) || ((linkCard != null) && linkCard.contains(related))))
            return;
        // Leaving the link: drop a not-yet-shown (delayed) card outright; if it is already
        // shown, hide it after a short grace so the pointer can reach the card.
        cancelShowLinkCard();
        if (linkCardVisible)
            scheduleHideLinkCard();
        else
            hoveredLink = null;
    }

    /** Schedules the card for a hovered link after {@link #LINK_CARD_SHOW_DELAY}. */
    private void scheduleShowLinkCard(Element a) {
        cancelHideLinkCard();
        // Same link already pending or shown — nothing to do.
        if (a == hoveredLink)
            return;
        hoveredLink = a;
        // Moving directly from one link's card to another: switch without re-delaying.
        if (linkCardVisible) {
            showLinkCard();
            return;
        }
        cancelShowLinkCard();
        linkCardShowTimer = DomGlobal.setTimeout(ignored -> showLinkCard(), LINK_CARD_SHOW_DELAY);
    }

    private void showLinkCard() {
        cancelShowLinkCard();
        cancelHideLinkCard();
        if (hoveredLink == null)
            return;
        ensureLinkCard();
        String href = hoveredLink.getAttribute("href");
        linkCardUrl.textContent = (href == null) ? "" : href;
        linkCard.style.setProperty("display", "flex");
        linkCardVisible = true;
        positionLinkCard();
    }

    private void hideLinkCard() {
        cancelShowLinkCard();
        cancelHideLinkCard();
        hoveredLink = null;
        linkCardVisible = false;
        if (linkCard != null)
            linkCard.style.setProperty("display", "none");
    }

    private void scheduleHideLinkCard() {
        cancelHideLinkCard();
        linkCardHideTimer = DomGlobal.setTimeout(ignored -> hideLinkCard(), 200);
    }

    private void cancelHideLinkCard() {
        if (linkCardHideTimer >= 0) {
            DomGlobal.clearTimeout(linkCardHideTimer);
            linkCardHideTimer = -1;
        }
    }

    private void cancelShowLinkCard() {
        if (linkCardShowTimer >= 0) {
            DomGlobal.clearTimeout(linkCardShowTimer);
            linkCardShowTimer = -1;
        }
    }

    private void ensureLinkCard() {
        if (linkCard != null)
            return;
        linkCard = classed(overlayCss().linkCard());
        linkCardUrl = classed(overlayCss().linkCardUrl());
        linkCard.appendChild(linkCardUrl);
        HTMLElement actions = classed(overlayCss().linkCardActions());
        actions.appendChild(linkCardButton("Open", () -> openHoveredLink()));
        actions.appendChild(linkCardButton("Edit", () -> editHoveredLink()));
        linkCard.appendChild(actions);
        // Keep the card open while the pointer is over it; hide when it leaves.
        linkCard.addEventListener("mouseover", evt -> cancelHideLinkCard());
        linkCard.addEventListener("mouseout", evt -> {
            Node related = Js.uncheckedCast(((MouseEvent) evt).relatedTarget);
            if ((related == null) || !linkCard.contains(related))
                scheduleHideLinkCard();
        });
        DomGlobal.document.body.appendChild(linkCard);
    }

    private HTMLElement linkCardButton(String label, Runnable action) {
        HTMLElement b = classed(overlayCss().linkCardBtn());
        b.textContent = label;
        b.addEventListener("mousedown", evt -> {
            evt.preventDefault();
            action.run();
        });
        return b;
    }

    /** Positions the (fixed) card just above the hovered link, flipping below if tight. */
    private void positionLinkCard() {
        if ((linkCard == null) || (hoveredLink == null))
            return;
        DOMRect r = hoveredLink.getBoundingClientRect();
        double top = r.top - linkCard.offsetHeight - 6;
        if (top < 4)
            top = r.bottom + 6;
        linkCard.style.setProperty("left", r.left + "px");
        linkCard.style.setProperty("top", top + "px");
    }

    private void openHoveredLink() {
        if (hoveredLink != null)
            openLink(hoveredLink, hoveredLink.getAttribute("href"));
    }

    private void editHoveredLink() {
        Element a = hoveredLink;
        if (a == null)
            return;
        String url = a.getAttribute("href");
        String label = a.textContent;
        // Target this link in the model so a subsequent apply/remove acts on it.
        selectLinkInModel(a);
        Selection target = state.selection();
        hideLinkCard();
        LinkPanel.show(a, url, label, (LinkPanel.IAnchorSource) null, 0, new LinkPanel.ILinkPanelCallback() {

            @Override
            public void onApply(String u) {
                onApply(u, null);
            }

            @Override
            public void onApply(String u, String l) {
                state.setSelection(target);
                doApplyLink(u, l);
            }

            @Override
            public void onRemove() {
                state.setSelection(target);
                doRemoveLink();
            }
        });
    }

    /**
     * Places the model cursor inside the given link (by positioning the DOM caret within
     * its text and syncing) so a subsequent link command targets it.
     */
    private void selectLinkInModel(Element a) {
        Node text = a.firstChild;
        elemental2.dom.Selection sel = DomGlobal.document.getSelection();
        if (sel == null)
            return;
        if ((text != null) && (text.nodeType == Node.TEXT_NODE)) {
            int len = (text.textContent == null) ? 0 : text.textContent.length();
            sel.collapse(text, Math.min(1, len));
        } else
            sel.collapse(a, 0);
        syncSelectionFromDom();
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

    /************************************************************************
     * Native typing.
     *
     * The controlled-contenteditable rule is that the browser never mutates the
     * DOM — every input is prevented and re-performed as a transaction, after
     * which the document is re-rendered. That is what makes the DOM provably match
     * the model, and it is worth keeping for everything structural.
     *
     * It is a poor deal for ordinary typing. A character typed into a paragraph
     * costs a rebuild of every block in the document, so the cost of a keystroke
     * grows with the length of the piece being written — which is exactly backwards.
     *
     * The observation this rests on: for a caret sitting plainly inside plain text,
     * what the browser is about to do and what the model says are the same thing.
     * Where that can be established beforehand, the default action is allowed to
     * stand and the model is moved to match — no prevention, no render, no cost in
     * the size of the document. Where it cannot, nothing changes.
     *
     * "Where it cannot" is deliberately generous, because a wrong answer here is a
     * DOM and a model that disagree, and the guarantee above is worth more than the
     * keystrokes. The conditions are enumerated in tryNativeInsert; and because
     * enumerated conditions are a thing one gets wrong, every native insertion is
     * checked afterwards against the model and resyncs if it does not agree.
     ************************************************************************/

    /** An IME composition is in progress (see {@link #tryNativeInsert(String)}). */
    private boolean composing;

    /**
     * Lets the browser's own insertion stand for a keystroke that is provably
     * equivalent to the transaction it would otherwise be turned into, updating the
     * model to match without a re-render.
     * <p>
     * Declines — leaving the caller to prevent the event and go through the normal
     * path — for anything where the two could differ:
     * <ul>
     * <li><b>A composition</b> in progress: the text on screen is the IME's, not
     * settled input, and the model has no business tracking it a character at a time.</li>
     * <li><b>Anything but plain characters</b>: a newline is a block operation, not
     * a character in a line.</li>
     * <li><b>A range selection</b>: that is a delete and an insert, and how a browser
     * takes a selection apart across element boundaries is its own business.</li>
     * <li><b>A non-text block</b>: atomic blocks take no caret and a table's cells are
     * the table handler's, which has already had its chance at the event.</li>
     * <li><b>A block holding an image or a variable</b>: those are zero-length formats
     * over a sentinel character, with an insertion rule of their own and a
     * {@code contenteditable="false"} span in the DOM to go with it.</li>
     * <li><b>A caret that is not plainly within plain text</b>: see
     * {@link #caretInPlainText()}, which is where the formatting question is settled.</li>
     * </ul>
     *
     * @param data
     *             the text the browser is about to insert.
     * @return {@code true} if the browser's insertion was allowed to stand.
     */
    private boolean tryNativeInsert(String data) {
        if (composing)
            return false;
        if ((data == null) || data.isEmpty())
            return false;
        if ((data.indexOf('\n') >= 0) || (data.indexOf('\r') >= 0))
            return false;
        Selection sel = state.selection();
        if (!sel.isCursor())
            return false;
        List<FormattedBlock> blocks = state.doc().getBlocks();
        int blockIndex = sel.anchorBlock();
        if ((blockIndex < 0) || (blockIndex >= blocks.size()))
            return false;
        FormattedBlock block = blocks.get(blockIndex);
        if (atomicBlock(block.getType()) || (block.getType() == BlockType.TABLE))
            return false;
        for (FormattedLine line : block.getLines()) {
            for (FormattedLine.Format format : line.getFormatting()) {
                if (format.getLength() == 0)
                    return false;
            }
        }
        if (!caretInPlainText())
            return false;
        Transaction tr = Commands.insertText(state, data);
        if (tr == null)
            return false;
        // Silent: the DOM the browser is about to produce is the render, so producing
        // it again would only take the caret away from where the browser put it.
        applyTransactionSilent(tr);
        updatePlaceholder();
        verifyNativeInsert(blockIndex);
        return true;
    }

    /**
     * Whether the DOM caret sits where a native insertion must land exactly where the
     * model puts it.
     * <p>
     * The question is entirely about formatting boundaries. The model's rule (see
     * {@code FormattedLine.insert}) is that text typed at the trailing edge of a format
     * joins it and text typed at its leading edge does not; the browser's rule is
     * whichever node the caret happens to be in, which is not always the same answer.
     * Rather than model the disagreement, this declines every position at which one is
     * possible:
     * <ul>
     * <li>The caret must be in a <b>text node</b> that is a <b>direct child of the
     * block</b> — never inside a span or a link, where the browser would make the
     * character part of the formatting.</li>
     * <li>Within that node, an <b>interior</b> position is unambiguous. An <b>edge</b>
     * is allowed only when there is no neighbour on that side for the character to join
     * instead — which keeps the commonest case of all, typing at the end of a
     * paragraph, on the fast path.</li>
     * </ul>
     *
     * @return {@code true} if a native insertion here is equivalent to the model's.
     */
    private boolean caretInPlainText() {
        elemental2.dom.Selection sel = DomGlobal.document.getSelection();
        if ((sel == null) || (sel.rangeCount == 0) || !sel.isCollapsed)
            return false;
        Node node = sel.anchorNode;
        if ((node == null) || (node.nodeType != Node.TEXT_NODE))
            return false;
        // A block element is a direct child of the editor, so this says "the text is in
        // the block itself" — and, just as importantly, not nested in anything.
        Node parent = node.parentNode;
        if ((parent == null) || (parent.parentNode != editorEl))
            return false;
        int offset = sel.anchorOffset;
        int length = (node.textContent == null) ? 0 : node.textContent.length();
        if (offset <= 0)
            return parent.firstChild == node;
        if (offset >= length)
            return parent.lastChild == node;
        return true;
    }

    /**
     * Checks, once the browser has actually inserted, that the block it inserted into
     * holds as many characters as the model says it should — and re-renders from the
     * model if it does not.
     * <p>
     * {@link #tryNativeInsert(String)} decides in advance that the browser will do a
     * particular thing. This is the standing check on that judgement: a case that was
     * not thought of, or a browser that normalises something on the way in, shows up
     * here as a length that does not agree and is corrected on the spot rather than
     * being typed on top of. The model is the authority, so the correction is simply
     * to render it.
     * <p>
     * Deferred to a timeout because the insertion has not happened yet — this is still
     * inside {@code beforeinput}.
     *
     * @param blockIndex
     *                   the block that was inserted into.
     */
    private void verifyNativeInsert(int blockIndex) {
        DomGlobal.setTimeout(args -> {
            if (rendering)
                return;
            List<FormattedBlock> blocks = state.doc().getBlocks();
            if ((blockIndex < 0) || (blockIndex >= blocks.size()))
                return;
            if (blockIndex >= editorEl.childElementCount)
                return;
            // charCount takes a Node, so the block is used as it comes off childNodes —
            // the same indexing ensureCursorVisible relies on.
            Node blockEl = editorEl.childNodes.item(blockIndex);
            if (blockEl == null)
                return;
            if (EditorSupport.charCount(blockEl) == Positions.contentSize(blocks.get(blockIndex)))
                return;
            if (config().debugLog)
                DomGlobal.console.log("[Editor:nativeInsert] block " + blockIndex + " diverged — re-rendering");
            render();
        }, 0);
    }

    /**
     * Handles content-mutating input events. Save for the plain typing that
     * {@link #tryNativeInsert(String)} lets through, all browser-native mutations are
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
        String inputType = EditorSupport.getInputType(evt);
        // The model has to know where the caret is either way; reading it changes
        // nothing, so it is as safe before the default action is decided as after.
        syncSelectionFromDom();

        // Plain typing into plain text is left to the browser and the model brought
        // into step behind it (see tryNativeInsert). Everything else is prevented and
        // performed through the transaction system.
        if ("insertText".equals(inputType) && tryNativeInsert(EditorSupport.getInputData(evt)))
            return;

        evt.preventDefault();
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
        // Delegated so the editor and the read-only renderer cannot number the
        // same list differently. See ListIndex.
        return ListIndex.format(indent, counter);
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

        /** Modifier applied to the editor root in {@link LinkInteraction#NAVIGATE} mode. */
        String navigate();

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
            line-height: var(--jui-richtext-line-height, inherit);
        }
        /* --jui-richtext-list-indent is deliberately not defaulted here, nor as a
           var() fallback in the list calc()s below: a zero length does not survive
           this stylesheet pipeline in either position ("0em" becomes a unitless
           "0", which is a type error inside calc() and invalidates the whole
           declaration). ContentStyle.apply() writes it as an inline style at
           runtime instead, and buildNode always applies one — compact() when none
           is configured — so it is always defined here. */
        .component:focus {
            outline: none;
        }
        /* In NAVIGATE mode a link is clickable (opens), so show the pointer cursor over it
           rather than the text caret inherited from .component. */
        .component.navigate a {
            cursor: pointer;
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
            padding: var(--jui-richtext-block-spacing, 2px) 0;
            min-height: 1em;
            white-space: pre-wrap;
        }
        .component .block:first-child { margin-top: 0; }
        /* The base list indent (0 by default; set by the document style) is folded into the
           marker padding and the marker's own offset, so it composes with .indentN nesting
           and the bullet stays aligned. Mirrors FormattedTextStyles' read-only list rules. */
        .component .listBullet {
            position: relative;
            padding-left: calc(1.5em + var(--jui-richtext-list-indent));
        }
        .component .listBullet::before {
            position: absolute;
            left: calc(0.35em + var(--jui-richtext-list-indent));
            content: '\\2022';
        }
        .component .listNumber {
            position: relative;
            padding-left: calc(1.5em + var(--jui-richtext-list-indent));
        }
        .component .listNumber::before {
            position: absolute;
            left: calc(0.15em + var(--jui-richtext-list-indent));
            content: attr(data-list-index) '.';
        }
        /* A list item is a paragraph (carries .block + .listX), so it would otherwise inherit
           the prose block padding and paragraph margin — which the document style makes roomy,
           pushing items too far apart. Give list items their own tight vertical rhythm via a
           dedicated token so the gap between them stays compact regardless of prose spacing.
           Selector qualified with .block (specificity (0,3,0)) so it reliably wins over the
           shared sheet's .richtext > .block padding (which also scopes the editor root). */
        .component .block.listBullet, .component .block.listNumber {
            padding-top: var(--jui-richtext-list-spacing, 3px);
            padding-bottom: var(--jui-richtext-list-spacing, 3px);
            margin-top: 0;
            margin-bottom: 0;
        }
        /* Paragraph spacing is editor-specific (the read-only renderer spaces paragraphs
           differently), so it stays here. Headings, inline formats (fmt_*), quotes, code
           blocks and indent margins are all provided by the shared richtext stylesheet
           (FormattedTextStyles), scoped via the richtext class on the editor root. */
        .component p {
            margin: 0 0 var(--jui-richtext-para-spacing, 0.2em) 0;
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
