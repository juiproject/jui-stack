package com.effacy.jui.text.ui.editor2;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Range;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * Default toolbar for the editor. Renders as a JUI component with
 * configurable tool buttons.
 * <p>
 * Supports two display modes:
 * <ul>
 * <li><b>Fixed</b> (default) — rendered inline as part of the editor
 * layout.</li>
 * <li><b>Floating</b> — rendered into a body-level container and
 * positioned above the current text selection. Shows automatically when
 * a range is selected; hides when the selection collapses to a
 * cursor.</li>
 * </ul>
 * <p>
 * Each button sends commands to the editor via {@link IEditorCommands}
 * (bound during {@link #bind(IEditorCommands)}). The editor sends state
 * updates back via {@link #updateState(BlockType, Set, boolean)} and
 * {@link #updateCellState(Set)}.
 * <p>
 * Usage (fixed):
 * <pre>
 * EditorToolbar toolbar = new EditorToolbar(new EditorToolbar.Config()
 *     .tools(Tool.BOLD, Tool.ITALIC, Tool.UNDERLINE, Tool.H1, Tool.H2, Tool.LINK));
 * </pre>
 * <p>
 * Usage (floating):
 * <pre>
 * EditorToolbar toolbar = new EditorToolbar(new EditorToolbar.Config()
 *     .floating(true)
 *     .tools(Tool.BOLD, Tool.ITALIC, Tool.UNDERLINE));
 * </pre>
 */
public class EditorToolbar extends Component<EditorToolbar.Config> implements IEditorToolbar {

    /**
     * Available toolbar tools. Each maps to a specific
     * {@link IEditorCommands} action.
     */
    public enum Tool {
        BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, SUBSCRIPT, SUPERSCRIPT, CODE, HIGHLIGHT,
        H1, H2, H3, PARAGRAPH,
        BULLET_LIST, NUMBERED_LIST,
        TABLE,
        LINK, VARIABLE
    }

    /**
     * Configuration for the toolbar.
     */
    public static class Config extends Component.Config {

        /**
         * Style pack for the toolbar. Provides the localised CSS used to
         * render toolbar buttons and layout. Custom styles can be created
         * externally by implementing {@link ILocalCSS} with a different
         * stylesheet and wrapping via {@link Style#create(ILocalCSS)}.
         */
        public interface Style {

            public ILocalCSS styles();

            public static Style create(ILocalCSS styles) {
                return () -> styles;
            }

            public static final Style STANDARD = create(LocalCSS.instance());
        }

        Set<Tool> tools;
        boolean floating;
        Style style = Style.STANDARD;

        /**
         * Configures which tools to display. When not called (or called with
         * no arguments), all tools are displayed.
         */
        public Config tools(Tool... tools) {
            if ((tools != null) && (tools.length > 0))
                this.tools = EnumSet.copyOf(Arrays.asList(tools));
            return this;
        }

        /**
         * Enables floating mode. When floating, the toolbar renders into a
         * body-level container and appears above the current text selection.
         * It shows when a range is selected and hides when the selection
         * collapses.
         */
        public Config floating(boolean floating) {
            this.floating = floating;
            return this;
        }

        /**
         * Sets the style pack for the toolbar. When not called, the
         * {@link Style#STANDARD} style is used.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }
    }

    /************************************************************************
     * Body-level container for floating toolbars.
     ************************************************************************/

    private static final String FLOATING_CONTAINER_ID = "floating-toolbar-container";
    static {
        Element container = DomSupport.createElement("div");
        container.id = FLOATING_CONTAINER_ID;
        DomGlobal.document.body.appendChild(container);
    }

    /************************************************************************
     * State.
     ************************************************************************/

    private IEditorCommands commands;

    /**
     * Toolbar button elements keyed by format type, for active-state tracking.
     */
    private Map<FormatType, Element> formatButtons = new EnumMap<>(FormatType.class);

    /**
     * Toolbar button elements keyed by block type, for active-state tracking.
     */
    private Map<BlockType, Element> blockTypeButtons = new EnumMap<>(BlockType.class);

    /************************************************************************
     * Tool → format/block type mappings.
     ************************************************************************/

    private static final Map<Tool, FormatType> TOOL_FORMAT = new EnumMap<>(Tool.class);
    static {
        TOOL_FORMAT.put(Tool.BOLD, FormatType.BLD);
        TOOL_FORMAT.put(Tool.ITALIC, FormatType.ITL);
        TOOL_FORMAT.put(Tool.UNDERLINE, FormatType.UL);
        TOOL_FORMAT.put(Tool.STRIKETHROUGH, FormatType.STR);
        TOOL_FORMAT.put(Tool.SUBSCRIPT, FormatType.SUB);
        TOOL_FORMAT.put(Tool.SUPERSCRIPT, FormatType.SUP);
        TOOL_FORMAT.put(Tool.CODE, FormatType.CODE);
        TOOL_FORMAT.put(Tool.HIGHLIGHT, FormatType.HL);
    }

    private static final Map<Tool, BlockType> TOOL_BLOCK = new EnumMap<>(Tool.class);
    static {
        TOOL_BLOCK.put(Tool.H1, BlockType.H1);
        TOOL_BLOCK.put(Tool.H2, BlockType.H2);
        TOOL_BLOCK.put(Tool.H3, BlockType.H3);
        TOOL_BLOCK.put(Tool.PARAGRAPH, BlockType.PARA);
    }

    private static final Map<Tool, BlockType> TOOL_TOGGLE_BLOCK = new EnumMap<>(Tool.class);
    static {
        TOOL_TOGGLE_BLOCK.put(Tool.BULLET_LIST, BlockType.NLIST);
        TOOL_TOGGLE_BLOCK.put(Tool.NUMBERED_LIST, BlockType.OLIST);
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    public EditorToolbar() {
        this(new Config());
    }

    public EditorToolbar(Config config) {
        super(config);
        if (config.tools == null)
            config.tools = EnumSet.allOf(Tool.class);
    }

    /************************************************************************
     * IEditorToolbar.
     ************************************************************************/

    @Override
    public void bind(IEditorCommands commands) {
        this.commands = commands;
        if ((config().floating) && !isRendered())
            bind(FLOATING_CONTAINER_ID, false);
    }

    @Override
    public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats, boolean rangeSelected) {
        for (Map.Entry<BlockType, Element> entry : blockTypeButtons.entrySet()) {
            if (entry.getKey() == activeBlockType)
                entry.getValue().classList.add(styles().tbtnActive());
            else
                entry.getValue().classList.remove(styles().tbtnActive());
        }
        for (Map.Entry<FormatType, Element> entry : formatButtons.entrySet()) {
            if (activeFormats.contains(entry.getKey()))
                entry.getValue().classList.add(styles().tbtnActive());
            else
                entry.getValue().classList.remove(styles().tbtnActive());
        }
        if (config().floating) {
            if (rangeSelected)
                showAboveSelection();
            else
                dismiss();
        }
    }

    @Override
    public void updateCellState(Set<FormatType> activeFormats) {
        blockTypeButtons.values().forEach(btn -> btn.classList.remove(styles().tbtnActive()));
        for (Map.Entry<FormatType, Element> entry : formatButtons.entrySet()) {
            if (activeFormats.contains(entry.getKey()))
                entry.getValue().classList.add(styles().tbtnActive());
            else
                entry.getValue().classList.remove(styles().tbtnActive());
        }
        if (config().floating)
            dismiss();
    }

    /************************************************************************
     * Floating mode: show / hide / positioning.
     ************************************************************************/

    private void showAboveSelection() {
        if (!isRendered())
            return;
        elemental2.dom.Selection sel = DomGlobal.document.getSelection();
        if ((sel == null) || (sel.rangeCount == 0)) {
            dismiss();
            return;
        }
        Range range = sel.getRangeAt(0);
        if (range.collapsed) {
            dismiss();
            return;
        }
        JsRect rect = Js.uncheckedCast(range.getBoundingClientRect());
        elemental2.dom.HTMLElement root = Js.uncheckedCast(getRoot());
        root.style.display = "flex";

        // Position above the selection, centered horizontally. Use
        // requestAnimationFrame so the browser has laid out the toolbar and
        // we can measure its dimensions.
        DomGlobal.requestAnimationFrame(time -> {
            double panelWidth = getRoot().getBoundingClientRect().width;
            double panelHeight = getRoot().getBoundingClientRect().height;
            double left = rect.left + (rect.width / 2) - (panelWidth / 2);
            double top = rect.top - panelHeight - 8;
            // Keep within viewport bounds.
            if (left < 4)
                left = 4;
            if (top < 4)
                top = rect.bottom + 8;
            root.style.setProperty("left", left + "px");
            root.style.setProperty("top", top + "px");
        });
    }

    private void dismiss() {
        if (isRendered()) {
            elemental2.dom.HTMLElement root = Js.uncheckedCast(getRoot());
            root.style.display = "none";
        }
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            root.style(styles().toolbar());
            if (config().floating)
                root.style(styles().floating());

            // Format toggles.
            formatButton(root, Tool.BOLD, "B", "Bold (Ctrl+B)");
            formatButton(root, Tool.ITALIC, "I", "Italic (Ctrl+I)");
            formatButton(root, Tool.UNDERLINE, "U", "Underline (Ctrl+U)");
            formatButton(root, Tool.STRIKETHROUGH, "S", "Strikethrough");
            formatButton(root, Tool.SUBSCRIPT, "x\u2082", "Subscript");
            formatButton(root, Tool.SUPERSCRIPT, "x\u00B2", "Superscript");
            formatButton(root, Tool.CODE, "<>", "Code");
            formatButton(root, Tool.HIGHLIGHT, "H", "Highlight");

            // Block type controls.
            if (hasAnyTool(Tool.H1, Tool.H2, Tool.H3, Tool.PARAGRAPH))
                separator(root);
            blockButton(root, Tool.H1, "H1", "Heading 1");
            blockButton(root, Tool.H2, "H2", "Heading 2");
            blockButton(root, Tool.H3, "H3", "Heading 3");
            blockButton(root, Tool.PARAGRAPH, "\u00B6", "Paragraph");

            // List controls.
            if (hasAnyTool(Tool.BULLET_LIST, Tool.NUMBERED_LIST))
                separator(root);
            toggleBlockButton(root, Tool.BULLET_LIST, "\u2022", "Bullet List");
            toggleBlockButton(root, Tool.NUMBERED_LIST, "1.", "Numbered List");

            // Table insertion.
            if (hasTool(Tool.TABLE)) {
                separator(root);
                Button.$(root).style(styles().tbtn()).text("\u229E").attr("title", "Insert Table")
                    .on(e -> { e.stopEvent(); if (commands != null) commands.insertTable(2, 3); }, UIEventType.ONMOUSEDOWN);
            }

            // Link.
            if (hasTool(Tool.LINK)) {
                separator(root);
                Button.$(root).style(styles().tbtn()).text("\u26D3").attr("title", "Link")
                    .on((e, n) -> { e.stopEvent(); if (commands != null) commands.insertLink((Element) n); }, UIEventType.ONMOUSEDOWN);
            }

            // Variable.
            if (hasTool(Tool.VARIABLE)) {
                separator(root);
                Button.$(root).style(styles().tbtn()).text("{ }").attr("title", "Variable")
                    .on((e, n) -> { e.stopEvent(); if (commands != null) commands.insertVariable((Element) n); }, UIEventType.ONMOUSEDOWN);
            }
        }).build();
    }

    /************************************************************************
     * Button builder helpers.
     ************************************************************************/

    private void formatButton(IDomInsertableContainer<?> parent, Tool tool, String label, String tooltip) {
        if (!hasTool(tool))
            return;
        FormatType ft = TOOL_FORMAT.get(tool);
        Button.$(parent).style(styles().tbtn()).text(label).attr("title", tooltip)
            .use(n -> formatButtons.put(ft, (Element) n))
            .on(e -> { e.stopEvent(); if (commands != null) commands.toggleFormat(ft); }, UIEventType.ONMOUSEDOWN);
    }

    private void blockButton(IDomInsertableContainer<?> parent, Tool tool, String label, String tooltip) {
        if (!hasTool(tool))
            return;
        BlockType bt = TOOL_BLOCK.get(tool);
        Button.$(parent).style(styles().tbtn()).text(label).attr("title", tooltip)
            .use(n -> blockTypeButtons.put(bt, (Element) n))
            .on(e -> { e.stopEvent(); if (commands != null) commands.setBlockType(bt); }, UIEventType.ONMOUSEDOWN);
    }

    private void toggleBlockButton(IDomInsertableContainer<?> parent, Tool tool, String label, String tooltip) {
        if (!hasTool(tool))
            return;
        BlockType bt = TOOL_TOGGLE_BLOCK.get(tool);
        Button.$(parent).style(styles().tbtn()).text(label).attr("title", tooltip)
            .use(n -> blockTypeButtons.put(bt, (Element) n))
            .on(e -> { e.stopEvent(); if (commands != null) commands.toggleBlockType(bt); }, UIEventType.ONMOUSEDOWN);
    }

    private void separator(IDomInsertableContainer<?> parent) {
        Span.$(parent).style(styles().tbtnSep());
    }

    private boolean hasTool(Tool tool) {
        return config().tools.contains(tool);
    }

    private boolean hasAnyTool(Tool... candidates) {
        for (Tool t : candidates) {
            if (config().tools.contains(t))
                return true;
        }
        return false;
    }

    /************************************************************************
     * JsInterop helpers (floating mode).
     ************************************************************************/

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    private static class JsRect {
        public double left, top, bottom, width, height;
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return config().style.styles();
    }

    public static interface ILocalCSS extends IComponentCSS {

        String toolbar();

        String floating();

        String tbtn();

        String tbtnActive();

        String tbtnSep();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .component {
            --jui-toolbar-bg: #fafafa;
            --jui-toolbar-gap: 2px;
            --jui-toolbar-padding: 4px 6px;
            --jui-toolbar-border: #ddd;
            --jui-toolbar-btn-color: #444;
            --jui-toolbar-btn-size: 0.85em;
            --jui-toolbar-btn-lineheight: 1.4;
            --jui-toolbar-btn-padding: 4px 8px;
            --jui-toolbar-btn-border-radius: 4px;
            --jui-toolbar-btn-hover-bg: #e8e8e8;
            --jui-toolbar-btn-active-bg: #dbeafe;
            --jui-toolbar-btn-active-color: #1d4ed8;
            --jui-toolbar-sep-color: #ccc;
            --jui-toolbar-float-bg: #fff;
            --jui-toolbar-float-border: #e5e7eb;
            --jui-toolbar-float-radius: 6px;
            --jui-toolbar-float-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
        }
        .toolbar {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: var(--jui-toolbar-gap);
            padding: var(--jui-toolbar-padding);
            border-bottom: 1px solid var(--jui-toolbar-border);
            background: var(--jui-toolbar-bg);
            flex-shrink: 0;
        }
        .floating {
            position: fixed;
            display: none;
            border-bottom: none;
            border: 1px solid var(--jui-toolbar-float-border);
            border-radius: var(--jui-toolbar-float-radius);
            box-shadow: var(--jui-toolbar-float-shadow);
            background: var(--jui-toolbar-float-bg);
            z-index: 10000;
        }
        .tbtn {
            border: none;
            background: none;
            cursor: pointer;
            padding: var(--jui-toolbar-btn-padding);
            border-radius: var(--jui-toolbar-btn-border-radius);
            font-size: var(--jui-toolbar-btn-size);
            font-weight: 500;
            color: var(--jui-toolbar-btn-color);
            line-height: var(--jui-toolbar-btn-lineheight);
        }
        .tbtn:hover {
            background: var(--jui-toolbar-btn-hover-bg);
        }
        .tbtnActive {
            background: var(--jui-toolbar-btn-active-bg);
            color: var(--jui-toolbar-btn-active-color);
        }
        .tbtnSep {
            width: 1px;
            height: 1.2em;
            background: var(--jui-toolbar-sep-color);
            margin: 0 4px;
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
