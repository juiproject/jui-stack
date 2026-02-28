package com.effacy.jui.text.ui.editor2;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.core.client.component.IComponentCSS;
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
 * A floating toolbar that appears above the current text selection. Hides
 * automatically when the selection collapses to a cursor.
 * <p>
 * Not a JUI component — creates its own floating DOM element appended to
 * {@code document.body}, following the same pattern as {@link LinkPanel} and
 * {@link VariablePanel}.
 * <p>
 * Accepts the same {@link EditorToolbar.Config} as the fixed toolbar for
 * configuring which tools are available.
 */
public class FloatingToolbar implements IEditorToolbar {

    /************************************************************************
     * State.
     ************************************************************************/

    private Set<EditorToolbar.Tool> tools;
    private IEditorCommands commands;
    private Element panelEl;
    private boolean visible;

    /**
     * Format button elements keyed by format type, for active-state tracking.
     */
    private Map<FormatType, Element> formatButtons = new EnumMap<>(FormatType.class);

    /**
     * Block type button elements keyed by block type, for active-state tracking.
     */
    private Map<BlockType, Element> blockTypeButtons = new EnumMap<>(BlockType.class);

    /************************************************************************
     * Tool → format/block type mappings (shared with EditorToolbar).
     ************************************************************************/

    private static final Map<EditorToolbar.Tool, FormatType> TOOL_FORMAT = new EnumMap<>(EditorToolbar.Tool.class);
    static {
        TOOL_FORMAT.put(EditorToolbar.Tool.BOLD, FormatType.BLD);
        TOOL_FORMAT.put(EditorToolbar.Tool.ITALIC, FormatType.ITL);
        TOOL_FORMAT.put(EditorToolbar.Tool.UNDERLINE, FormatType.UL);
        TOOL_FORMAT.put(EditorToolbar.Tool.STRIKETHROUGH, FormatType.STR);
        TOOL_FORMAT.put(EditorToolbar.Tool.SUBSCRIPT, FormatType.SUB);
        TOOL_FORMAT.put(EditorToolbar.Tool.SUPERSCRIPT, FormatType.SUP);
        TOOL_FORMAT.put(EditorToolbar.Tool.CODE, FormatType.CODE);
        TOOL_FORMAT.put(EditorToolbar.Tool.HIGHLIGHT, FormatType.HL);
    }

    private static final Map<EditorToolbar.Tool, BlockType> TOOL_BLOCK = new EnumMap<>(EditorToolbar.Tool.class);
    static {
        TOOL_BLOCK.put(EditorToolbar.Tool.H1, BlockType.H1);
        TOOL_BLOCK.put(EditorToolbar.Tool.H2, BlockType.H2);
        TOOL_BLOCK.put(EditorToolbar.Tool.H3, BlockType.H3);
        TOOL_BLOCK.put(EditorToolbar.Tool.PARAGRAPH, BlockType.PARA);
    }

    private static final Map<EditorToolbar.Tool, BlockType> TOOL_TOGGLE_BLOCK = new EnumMap<>(EditorToolbar.Tool.class);
    static {
        TOOL_TOGGLE_BLOCK.put(EditorToolbar.Tool.BULLET_LIST, BlockType.NLIST);
        TOOL_TOGGLE_BLOCK.put(EditorToolbar.Tool.NUMBERED_LIST, BlockType.OLIST);
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    public FloatingToolbar() {
        this(new EditorToolbar.Config());
    }

    public FloatingToolbar(EditorToolbar.Config config) {
        this.tools = (config.tools != null) ? config.tools : EnumSet.allOf(EditorToolbar.Tool.class);
    }

    /************************************************************************
     * IEditorToolbar.
     ************************************************************************/

    @Override
    public void bind(IEditorCommands commands) {
        this.commands = commands;
        ensurePanel();
    }

    @Override
    public void updateState(BlockType activeBlockType, Set<FormatType> activeFormats, boolean rangeSelected) {
        updateButtonStates(activeBlockType, activeFormats);
        if (rangeSelected)
            showAboveSelection();
        else
            hide();
    }

    @Override
    public void updateCellState(Set<FormatType> activeFormats) {
        // Clear block buttons and update format buttons.
        blockTypeButtons.values().forEach(btn -> btn.classList.remove(styles().tbtnActive()));
        for (Map.Entry<FormatType, Element> entry : formatButtons.entrySet()) {
            if (activeFormats.contains(entry.getKey()))
                entry.getValue().classList.add(styles().tbtnActive());
            else
                entry.getValue().classList.remove(styles().tbtnActive());
        }
        // Cell selections with a range still get the floating toolbar.
        // We don't have rangeSelected info here, so just hide to be safe.
        hide();
    }

    /************************************************************************
     * Panel management.
     ************************************************************************/

    private void ensurePanel() {
        if (panelEl != null)
            return;
        panelEl = DomGlobal.document.createElement("div");
        panelEl.classList.add(styles().floatingToolbar());
        buildButtons(panelEl);
        DomGlobal.document.body.appendChild(panelEl);
        hide();
    }

    private void showAboveSelection() {
        ensurePanel();
        elemental2.dom.Selection sel = DomGlobal.document.getSelection();
        if ((sel == null) || (sel.rangeCount == 0)) {
            hide();
            return;
        }
        Range range = sel.getRangeAt(0);
        if (range.collapsed) {
            hide();
            return;
        }
        JsRect rect = Js.uncheckedCast(range.getBoundingClientRect());
        elemental2.dom.HTMLElement panel = Js.uncheckedCast(panelEl);
        panel.style.display = "flex";
        visible = true;

        // Position above the selection, centered horizontally.
        // Use a small timeout so the browser has laid out the panel and we can
        // measure its width.
        DomGlobal.requestAnimationFrame(time -> {
            double panelWidth = panelEl.getBoundingClientRect().width;
            double panelHeight = panelEl.getBoundingClientRect().height;
            double left = rect.left + (rect.width / 2) - (panelWidth / 2);
            double top = rect.top - panelHeight - 8;
            // Keep within viewport bounds.
            if (left < 4)
                left = 4;
            if (top < 4)
                top = rect.bottom + 8;
            panel.style.setProperty("left", left + "px");
            panel.style.setProperty("top", top + "px");
        });
    }

    private void hide() {
        if (panelEl != null) {
            elemental2.dom.HTMLElement panel = Js.uncheckedCast(panelEl);
            panel.style.display = "none";
        }
        visible = false;
    }

    /************************************************************************
     * Button building.
     ************************************************************************/

    private void updateButtonStates(BlockType activeBlockType, Set<FormatType> activeFormats) {
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

    private boolean hasTool(EditorToolbar.Tool tool) {
        return tools.contains(tool);
    }

    private boolean hasAnyTool(EditorToolbar.Tool... candidates) {
        for (EditorToolbar.Tool t : candidates) {
            if (tools.contains(t))
                return true;
        }
        return false;
    }

    private void buildButtons(Element toolbar) {
        // Format toggles.
        buildFormatButton(toolbar, EditorToolbar.Tool.BOLD, "B", "Bold (Ctrl+B)");
        buildFormatButton(toolbar, EditorToolbar.Tool.ITALIC, "I", "Italic (Ctrl+I)");
        buildFormatButton(toolbar, EditorToolbar.Tool.UNDERLINE, "U", "Underline (Ctrl+U)");
        buildFormatButton(toolbar, EditorToolbar.Tool.STRIKETHROUGH, "S", "Strikethrough");
        buildFormatButton(toolbar, EditorToolbar.Tool.SUBSCRIPT, "x\u2082", "Subscript");
        buildFormatButton(toolbar, EditorToolbar.Tool.SUPERSCRIPT, "x\u00B2", "Superscript");
        buildFormatButton(toolbar, EditorToolbar.Tool.CODE, "<>", "Code");
        buildFormatButton(toolbar, EditorToolbar.Tool.HIGHLIGHT, "H", "Highlight");

        if (hasAnyTool(EditorToolbar.Tool.H1, EditorToolbar.Tool.H2, EditorToolbar.Tool.H3, EditorToolbar.Tool.PARAGRAPH))
            separator(toolbar);

        buildBlockButton(toolbar, EditorToolbar.Tool.H1, "H1", "Heading 1");
        buildBlockButton(toolbar, EditorToolbar.Tool.H2, "H2", "Heading 2");
        buildBlockButton(toolbar, EditorToolbar.Tool.H3, "H3", "Heading 3");
        buildBlockButton(toolbar, EditorToolbar.Tool.PARAGRAPH, "\u00B6", "Paragraph");

        if (hasAnyTool(EditorToolbar.Tool.BULLET_LIST, EditorToolbar.Tool.NUMBERED_LIST))
            separator(toolbar);

        buildToggleBlockButton(toolbar, EditorToolbar.Tool.BULLET_LIST, "\u2022", "Bullet List");
        buildToggleBlockButton(toolbar, EditorToolbar.Tool.NUMBERED_LIST, "1.", "Numbered List");

        if (hasTool(EditorToolbar.Tool.TABLE))
            separator(toolbar);

        if (hasTool(EditorToolbar.Tool.TABLE)) {
            button(toolbar, "\u229E", "Insert Table", () -> {
                if (commands != null)
                    commands.insertTable(2, 3);
            });
        }

        if (hasTool(EditorToolbar.Tool.LINK))
            separator(toolbar);

        if (hasTool(EditorToolbar.Tool.LINK)) {
            Element btn = button(toolbar, "\u26D3", "Link", null);
            btn.addEventListener("mousedown", evt -> {
                evt.preventDefault();
                if (commands != null)
                    commands.insertLink(btn);
            });
        }

        if (hasTool(EditorToolbar.Tool.VARIABLE)) {
            separator(toolbar);
            Element btn = button(toolbar, "{ }", "Variable", null);
            btn.addEventListener("mousedown", evt -> {
                evt.preventDefault();
                if (commands != null)
                    commands.insertVariable(btn);
            });
        }
    }

    private void buildFormatButton(Element toolbar, EditorToolbar.Tool tool, String label, String tooltip) {
        if (!hasTool(tool))
            return;
        FormatType ft = TOOL_FORMAT.get(tool);
        if (ft == null)
            return;
        Element btn = button(toolbar, label, tooltip, () -> {
            if (commands != null)
                commands.toggleFormat(ft);
        });
        formatButtons.put(ft, btn);
    }

    private void buildBlockButton(Element toolbar, EditorToolbar.Tool tool, String label, String tooltip) {
        if (!hasTool(tool))
            return;
        BlockType bt = TOOL_BLOCK.get(tool);
        if (bt == null)
            return;
        Element btn = button(toolbar, label, tooltip, () -> {
            if (commands != null)
                commands.setBlockType(bt);
        });
        blockTypeButtons.put(bt, btn);
    }

    private void buildToggleBlockButton(Element toolbar, EditorToolbar.Tool tool, String label, String tooltip) {
        if (!hasTool(tool))
            return;
        BlockType bt = TOOL_TOGGLE_BLOCK.get(tool);
        if (bt == null)
            return;
        Element btn = button(toolbar, label, tooltip, () -> {
            if (commands != null)
                commands.toggleBlockType(bt);
        });
        blockTypeButtons.put(bt, btn);
    }

    private Element button(Element parent, String label, String tooltip, Runnable action) {
        Element btn = DomGlobal.document.createElement("button");
        btn.classList.add(styles().tbtn());
        btn.textContent = label;
        btn.setAttribute("title", tooltip);
        if (action != null) {
            btn.addEventListener("mousedown", evt -> {
                evt.preventDefault();
                action.run();
            });
        }
        parent.appendChild(btn);
        return btn;
    }

    private void separator(Element parent) {
        Element sep = DomGlobal.document.createElement("span");
        sep.classList.add(styles().tbtnSep());
        parent.appendChild(sep);
    }

    /************************************************************************
     * JsInterop helpers.
     ************************************************************************/

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    private static class JsRect {
        public double left, top, bottom, width, height;
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    private static IFloatingToolbarCSS styles() {
        return FloatingToolbarCSS.instance();
    }

    public static interface IFloatingToolbarCSS extends IComponentCSS {

        String floatingToolbar();

        String tbtn();

        String tbtnActive();

        String tbtnSep();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS
    }, stylesheet = """
        .floatingToolbar {
            position: fixed;
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 2px;
            padding: 4px 6px;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            z-index: 10000;
            font-size: 0.875em;
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
    public static abstract class FloatingToolbarCSS implements IFloatingToolbarCSS {

        private static FloatingToolbarCSS STYLES;

        public static IFloatingToolbarCSS instance() {
            if (STYLES == null) {
                STYLES = (FloatingToolbarCSS) GWT.create(FloatingToolbarCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
