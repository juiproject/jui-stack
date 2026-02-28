package com.effacy.jui.text.ui.editor2;

import java.util.Set;
import java.util.function.Supplier;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.ui.editor2.IEditorToolbar.Position;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Range;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

/**
 * JUI control that composes an {@link Editor} with an {@link IEditorToolbar}
 * to provide a complete rich text editing experience with value management
 * and dirty detection.
 * <p>
 * The control mediates between the editor and the toolbar: the editor
 * notifies state changes via {@link Editor.IStateListener}, and the control
 * forwards them to the toolbar while managing any positional behaviour
 * (e.g. floating toolbar show/hide/positioning).
 * <p>
 * The toolbar is supplied via a {@link Supplier} of {@link IEditorToolbar},
 * allowing custom implementations. Position is configured on this control
 * via {@link Config#position(Position)} and determines layout and
 * containment styling (borders between toolbar and editor).
 * <p>
 * Usage:
 * <pre>
 * FormattedTextEditor editor = new FormattedTextEditor(new FormattedTextEditor.Config()
 *     .position(Position.BOTTOM)
 *     .editor(new Editor.Config()
 *         .linkOptions(MyApp::filterLinks)
 *         .variableOptions(MyApp::filterVariables))
 *     .toolbar(new EditorToolbar.Config()
 *         .tools(Tool.BOLD, Tool.ITALIC, Tool.UNDERLINE, Tool.H1, Tool.H2)));
 * editor.setValue(Value.of(myDocument));
 * </pre>
 */
public class FormattedTextEditor extends Control<FormattedText, FormattedTextEditor.Config> {

    /**
     * Configuration for the formatted text editor.
     */
    public static class Config extends Control.Config<FormattedText, Config> {

        Position position = Position.TOP;
        Supplier<IEditorToolbar> toolbarSupplier;
        Editor.Config editorConfig;
        Length height;

        /**
         * Sets the toolbar position. Defaults to {@link Position#TOP}.
         */
        public Config position(Position position) {
            if (position != null)
                this.position = position;
            return this;
        }

        /**
         * Supplies the toolbar implementation. The supplier is invoked once
         * during rendering.
         */
        public Config toolbar(Supplier<IEditorToolbar> supplier) {
            this.toolbarSupplier = supplier;
            return this;
        }

        /**
         * Convenience overload that creates a standard {@link EditorToolbar}
         * from the given config.
         */
        public Config toolbar(EditorToolbar.Config config) {
            this.toolbarSupplier = () -> new EditorToolbar(config);
            return this;
        }

        /**
         * Configures the underlying editor (link options, variable options,
         * debug logging, etc).
         */
        public Config editor(Editor.Config config) {
            this.editorConfig = config;
            return this;
        }

        /**
         * Sets the minimum height of the editor. Overrides the default of
         * 500px.
         */
        public Config height(Length height) {
            this.height = height;
            return this;
        }
    }

    /************************************************************************
     * State.
     ************************************************************************/

    private Editor editor;

    /**
     * Wrapper element for the floating toolbar (only used when position is
     * {@link Position#FLOATING}). The toolbar component is rendered inside
     * this div, which is styled with {@code position: fixed} and shown/hidden
     * by the control.
     */
    private Element floatingWrapEl;

    /************************************************************************
     * Construction.
     ************************************************************************/

    public FormattedTextEditor() {
        this(new Config());
    }

    public FormattedTextEditor(Config config) {
        super(config);
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        Editor.Config editorConfig = (data.editorConfig != null) ? data.editorConfig : new Editor.Config();
        IEditorToolbar toolbar = (data.toolbarSupplier != null) ? data.toolbarSupplier.get() : new EditorToolbar();

        editor = new Editor(editorConfig);

        // The control mediates between editor and toolbar: the editor
        // reports state changes, the control forwards them to the toolbar
        // and manages floating behaviour.
        IEditorCommands commands = editor.bind(new Editor.IStateListener() {

            @Override
            public void onStateUpdate(BlockType blockType, Set<FormatType> activeFormats, boolean rangeSelected) {
                toolbar.updateState(blockType, activeFormats, rangeSelected);
                if (data.position == Position.FLOATING) {
                    if (rangeSelected)
                        showFloatingToolbar();
                    else
                        hideFloatingToolbar();
                }
            }

            @Override
            public void onCellStateUpdate(Set<FormatType> activeFormats) {
                toolbar.updateCellState(activeFormats);
                if (data.position == Position.FLOATING)
                    hideFloatingToolbar();
            }
        });
        toolbar.bind(commands);

        if (data.position == Position.FLOATING) {
            // Floating toolbar: rendered inside the component DOM tree in a
            // fixed-position wrapper. The wrapper is hidden by default and
            // shown/positioned by the control when a range is selected.
            return Wrap.$(el).$(root -> {
                root.style(styles().component());
                if (data.height != null)
                    root.css(CSS.MIN_HEIGHT, data.height);
                Cpt.$(root, editor);
                Div.$(root).style(styles().floating()).use(n -> floatingWrapEl = (Element) n).$(wrap -> {
                    Cpt.$(wrap, toolbar);
                });
            }).build();
        }

        // Fixed toolbar (TOP or BOTTOM).
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            if (data.height != null)
                root.css(CSS.MIN_HEIGHT, data.height);
            if (data.position == Position.BOTTOM) {
                Cpt.$(root, editor);
                Div.$(root).style(styles().separator());
                Cpt.$(root, toolbar);
            } else {
                Cpt.$(root, toolbar);
                Div.$(root).style(styles().separator());
                Cpt.$(root, editor);
            }
        }).build();
    }

    /************************************************************************
     * Floating toolbar management.
     ************************************************************************/

    /**
     * Shows the floating toolbar wrapper above the current browser selection.
     */
    private void showFloatingToolbar() {
        if (floatingWrapEl == null)
            return;
        elemental2.dom.Selection sel = DomGlobal.document.getSelection();
        if ((sel == null) || (sel.rangeCount == 0)) {
            hideFloatingToolbar();
            return;
        }
        Range range = sel.getRangeAt(0);
        if (range.collapsed) {
            hideFloatingToolbar();
            return;
        }
        JsRect rect = Js.uncheckedCast(range.getBoundingClientRect());
        elemental2.dom.HTMLElement wrap = Js.uncheckedCast(floatingWrapEl);
        wrap.style.display = "flex";

        // Position above the selection, centered horizontally. Use
        // requestAnimationFrame so the browser has laid out the toolbar and
        // we can measure its dimensions.
        DomGlobal.requestAnimationFrame(time -> {
            double panelWidth = floatingWrapEl.getBoundingClientRect().width;
            double panelHeight = floatingWrapEl.getBoundingClientRect().height;
            double left = rect.left + (rect.width / 2) - (panelWidth / 2);
            double top = rect.top - panelHeight - 8;
            // Keep within viewport bounds.
            if (left < 4)
                left = 4;
            if (top < 4)
                top = rect.bottom + 8;
            wrap.style.setProperty("left", left + "px");
            wrap.style.setProperty("top", top + "px");
        });
    }

    /**
     * Hides the floating toolbar wrapper.
     */
    private void hideFloatingToolbar() {
        if (floatingWrapEl != null) {
            elemental2.dom.HTMLElement wrap = Js.uncheckedCast(floatingWrapEl);
            wrap.style.display = "none";
        }
    }

    /************************************************************************
     * Value management.
     ************************************************************************/

    @Override
    protected FormattedText valueFromSource() {
        if (editor == null)
            return null;
        return editor.value();
    }

    @Override
    protected void valueToSource(FormattedText value) {
        if (editor != null)
            editor.load(value);
    }

    @Override
    protected FormattedText clone(FormattedText value) {
        if (value == null)
            return null;
        return value.clone();
    }

    @Override
    protected boolean equals(FormattedText v1, FormattedText v2) {
        if (v1 == v2)
            return true;
        if ((v1 == null) || (v2 == null))
            return false;
        return v1.computeHash() == v2.computeHash();
    }

    @Override
    protected boolean empty(FormattedText value) {
        if (value == null)
            return true;
        return value.empty();
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
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IControlCSS {

        String separator();

        String floating();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS,
        IControlCSS.CONTROL_CSS
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
        .component .separator {
            height: 1px;
            background: #ddd;
            flex-shrink: 0;
        }
        .component .floating {
            position: fixed;
            display: none;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            background: #fff;
            z-index: 10000;
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
