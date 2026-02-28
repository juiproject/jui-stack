package com.effacy.jui.text.ui.editor2;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.SimpleComponent;
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

import elemental2.dom.Element;

/**
 * Default fixed toolbar for the editor. Renders as a JUI component with
 * configurable tool buttons.
 * <p>
 * Each button sends commands to the editor via {@link IEditorCommands} (bound
 * during {@link #bind(IEditorCommands)}). The editor sends state updates back
 * via {@link #updateState(BlockType, Set, boolean)} and
 * {@link #updateCellState(Set)}.
 * <p>
 * Usage:
 * <pre>
 * EditorToolbar toolbar = new EditorToolbar(new EditorToolbar.Config()
 *     .tools(Tool.BOLD, Tool.ITALIC, Tool.UNDERLINE, Tool.H1, Tool.H2, Tool.LINK));
 * editor.bind(toolbar);
 * </pre>
 */
public class EditorToolbar extends SimpleComponent implements IEditorToolbar {

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
    public static class Config {

        Set<Tool> tools;

        /**
         * Configures which tools to display. When not called (or called with
         * no arguments), all tools are displayed.
         */
        public Config tools(Tool... tools) {
            if ((tools != null) && (tools.length > 0))
                this.tools = EnumSet.copyOf(Arrays.asList(tools));
            return this;
        }
    }

    /************************************************************************
     * State.
     ************************************************************************/

    private Set<Tool> tools;

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
        this.tools = (config.tools != null) ? config.tools : EnumSet.allOf(Tool.class);
    }

    /************************************************************************
     * IEditorToolbar.
     ************************************************************************/

    @Override
    public void bind(IEditorCommands commands) {
        this.commands = commands;
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
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            root.style(styles().toolbar());

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
        return tools.contains(tool);
    }

    private boolean hasAnyTool(Tool... candidates) {
        for (Tool t : candidates) {
            if (tools.contains(t))
                return true;
        }
        return false;
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
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
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
