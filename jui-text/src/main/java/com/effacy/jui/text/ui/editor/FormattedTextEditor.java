package com.effacy.jui.text.ui.editor;

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
import com.effacy.jui.text.ui.editor.FormattedTextEditor.Config.ToolbarBehaviour;
import com.effacy.jui.text.type.FormattedText;
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
 *     .editor(new Editor.Config())
 *     .toolbar(new EditorToolbar.Config()
 *         .tools(Tools.BOLD, Tools.ITALIC, Tools.UNDERLINE, Tools.H1, Tools.H2,
 *                Tools.link("Link", "Link", MyApp::filterLinks),
 *                Tools.variable("Var", "Variable", MyApp::filterVariables))));
 * editor.setValue(Value.of(myDocument));
 * </pre>
 */
public class FormattedTextEditor extends Control<FormattedText, FormattedTextEditor.Config> {

    /**
     * Toolbar position relative to the editor area. Used by the containing
     * control to decide layout; the toolbar itself does not use this.
     */
    @FunctionalInterface
    public interface Position {

        public static final Position TOP = config -> {};
        public static final Position TOP_SEPARATE = config -> {
            config.css("""
                gap: 0.5em;
                --jui-formattededitor-border-toolbar-radius: var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-toolbar-bottom: 1px solid var(--jui-formattededitor-border);
                --jui-formattededitor-border-editor-radius: var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-editor-top: 1px solid var(--jui-formattededitor-border);
            """);
        };
        public static final Position TOP_HOVER = config -> {
            config.css("""
                --jui-formattededitor-border-toolbar-radius: var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-toolbar-bottom: 1px solid var(--jui-formattededitor-border);
                --jui-formattededitor-border-editor-radius: var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-editor-top: 1px solid var(--jui-formattededitor-border);
                --jui-formattededitor-border-toolbar-bg: #fff;
            """);
            config.toolbarBehaviour(ToolbarBehaviour.HOVER);
        };
        public static final Position TOP_TRANSPARENT = config -> {
            config.css("""
                --jui-formattededitor-border-toolbar-radius: var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-toolbar-bottom: 1px solid var(--jui-formattededitor-border);
                --jui-formattededitor-border-editor-radius: var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-editor-top: 1px solid var(--jui-formattededitor-border);
                --jui-formattededitor-border-toolbar-bg: #fff;
            """);
            config.toolbarBehaviour(ToolbarBehaviour.HOVER);
            config.borderless();
        };
        public static final Position BOTTOM = config -> {
            config.css("""
                flex-direction: column-reverse;
                --jui-formattededitor-border-toolbar-radius: 0 0 var(--jui-formattededitor-border-radius) var(--jui-formattededitor-border-radius);
                --jui-formattededitor-border-editor-radius: var(--jui-formattededitor-border-radius) var(--jui-formattededitor-border-radius) 0 0;
                --jui-formattededitor-border-editor-top: 1px solid var(--jui-formattededitor-border);
                --jui-formattededitor-border-editor-bottom: none;
            """);
        };
        public static final Position FLOATING = config -> {
            config.toolbarBehaviour(ToolbarBehaviour.FLOATING);
        };

        public void configure(FormattedTextEditor.Config config);
    }

    /**
     * Configuration for the formatted text editor.
     */
    public static class Config extends Control.Config<FormattedText, Config> {

        public enum ToolbarBehaviour {
            FIXED, FLOATING, HOVER;
        }

        private Supplier<IEditorToolbar> toolbarSupplier;
        private Editor.Config editorConfig;
        private Length height;
        private Length contentMinHeight;
        private ToolbarBehaviour toolbarBehaviour = ToolbarBehaviour.FIXED;
        private boolean nofocus;
        private boolean borderless;
        private String placeholder;
        private boolean detachedToolbar;

        /**
         * Applies a standard configuration.
         */
        public Config position(Position position) {
            if (position != null)
                position.configure(this);
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

        /**
         * Sets a minimum height on the editor <em>content</em> (editable) area, rather than
         * on the whole control as {@link #height(Length)} does.
         * <p>
         * The editable region is made at least this tall and fills the space, so clicking
         * anywhere within it places the cursor (activating the control). The control then
         * sizes to its content, so a containing panel need not carry its own minimum height.
         *
         * @param contentMinHeight
         *                         the minimum content height.
         * @return this configuration instance.
         */
        public Config contentMinHeight(Length contentMinHeight) {
            this.contentMinHeight = contentMinHeight;
            return this;
        }

        /**
         * Configures the toolbar behaviour. When set to
         * {@link ToolbarBehaviour#FLOATING}, the toolbar is rendered in a
         * fixed-position wrapper that is shown above the current selection when a range
         * is selected, and hidden otherwise. When set to
         * {@link ToolbarBehaviour#FIXED}, the toolbar is rendered in the normal
         * document flow according to the position configuration (e.g. above or below
         * the editor area).
         */
        public Config toolbarBehaviour(ToolbarBehaviour toolbarBehaviour) {
            if (toolbarBehaviour != null)
                this.toolbarBehaviour = toolbarBehaviour;
            return this;
        }

        /**
         * Suppress the focus CSS styling.
         */
        public Config noFocus() {
            this.nofocus = true;
            return this;
        }

        /**
         * Remove borders between editor and toolbar, for a more seamless look.
         */
        public Config borderless() {
            this.borderless = true;
            return this;
        }

        /**
         * Sets placeholder text shown when the editor is empty (forwarded to the underlying
         * {@link Editor}). When {@code null} (the default) no placeholder is shown.
         *
         * @param placeholder
         *                    the placeholder text.
         * @return this configuration instance.
         */
        public Config placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * Detaches the toolbar from this control: the toolbar (supplied via
         * {@link #toolbar(Supplier)}) is still bound to the editor, but it is <em>not</em>
         * rendered inside the control — the host renders it wherever it likes (e.g. a
         * full-width strip above the editor). The control renders only the editor area.
         *
         * @return this configuration instance.
         */
        public Config detachedToolbar() {
            this.detachedToolbar = true;
            return this;
        }

        /**
         * A reusable visual variant — a named bundle of configuration giving the editor a
         * particular look. Apply with {@link Config#variant(Variant)}.
         */
        @FunctionalInterface
        public interface Variant {

            /**
             * Seamless: removes every border (toolbar and editor, in all states including
             * hover) and hides the focus highlight, so the editor sits flush inside its own
             * container (e.g. a card or panel that provides the visual boundary).
             */
            public static final Variant SEAMLESS = config -> {
                config.css("""
                    --jui-formattededitor-border-toolbar-side: none;
                    --jui-formattededitor-border-toolbar-top: none;
                    --jui-formattededitor-border-toolbar-bottom: none;
                    --jui-formattededitor-border-toolbar-radius: 0;
                    --jui-formattededitor-border-editor-side: none;
                    --jui-formattededitor-border-editor-top: none;
                    --jui-formattededitor-border-editor-bottom: none;
                    --jui-formattededitor-border-editor-radius: 0;
                """);
                config.noFocus();
            };

            void configure(Config config);
        }

        /**
         * Applies a {@link Variant} (a reusable look) to this configuration.
         *
         * @param variant
         *                the variant to apply (ignored if {@code null}).
         * @return this configuration instance.
         */
        public Config variant(Variant variant) {
            if (variant != null)
                variant.configure(this);
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
        if (data.placeholder != null)
            editorConfig.placeholder(data.placeholder);
        IEditorToolbar toolbar = (data.toolbarSupplier != null) ? data.toolbarSupplier.get() : new EditorToolbar();

        
        // The control mediates between editor and toolbar: the editor
        // reports state changes, the control forwards them to the toolbar
        // and manages floating behaviour.
        editor = new Editor(editorConfig);
        IEditorCommands commands = editor.bind(new Editor.IStateListener() {

            @Override
            public void onStateUpdate(BlockType blockType, Set<FormatType> activeFormats, boolean rangeSelected) {
                toolbar.updateState(blockType, activeFormats, rangeSelected);
                if (data.toolbarBehaviour == Config.ToolbarBehaviour.FLOATING) {
                    if (rangeSelected)
                        showFloatingToolbar();
                    else
                        hideFloatingToolbar();
                }
            }

            @Override
            public void onCellStateUpdate(Set<FormatType> activeFormats) {
                toolbar.updateCellState(activeFormats);
                if (data.toolbarBehaviour == Config.ToolbarBehaviour.FLOATING)
                    hideFloatingToolbar();
            }

            @Override
            public void onContentChanged() {
                DomGlobal.setTimeout(args -> modified(), 0);
            }
        });
        toolbar.bind(commands);

        if (data.toolbarBehaviour == Config.ToolbarBehaviour.FLOATING) {
            // Floating toolbar: rendered inside the component DOM tree in a
            // fixed-position wrapper. The wrapper is hidden by default and
            // shown/positioned by the control when a range is selected.
            return Wrap.$(el).$(root -> {
                if (data.height != null)
                    root.css(CSS.MIN_HEIGHT, data.height);
                else if (data.contentMinHeight != null)
                    // The editor is the root's flex child, so it fills this minimum.
                    root.css(CSS.MIN_HEIGHT, data.contentMinHeight);
                if (data.nofocus)
                    root.style(styles().nofocus());
                if (data.borderless)
                    root.style(styles().borderless());
                Cpt.$(root, editor);
                Div.$(root).style(styles().floating()).use(n -> floatingWrapEl = (Element) n).$(wrap -> {
                    Cpt.$(wrap, toolbar);
                });
            }).build(dom -> attachFocusListeners(el));
        }

        // Fixed toolbar (TOP or BOTTOM).
        return Wrap.$(el).$(root -> {
            if (data.height != null)
                root.css(CSS.MIN_HEIGHT, data.height);
            else if (data.contentMinHeight != null)
                // The content area carries the minimum height; the control sizes to it.
                root.css(CSS.MIN_HEIGHT, Length.px(0));
            if (data.toolbarBehaviour == Config.ToolbarBehaviour.HOVER)
                root.style(styles().hover());
            if (data.nofocus)
                root.style(styles().nofocus());
            if (data.borderless)
                root.style(styles().borderless());
            // When detached, the toolbar is bound (above) but placed by the host, not here.
            if (!data.detachedToolbar)
                Div.$(root).style(styles().toolbar()).$(toolbar);
            var editorArea = Div.$(root).style(styles().editor());
            if (data.contentMinHeight != null)
                editorArea.css(CSS.MIN_HEIGHT, data.contentMinHeight);
            editorArea.$(editor);
        }).build(dom -> attachFocusListeners(el));
    }

    /************************************************************************
     * Focus management.
     ************************************************************************/

    /**
     * Attaches focusin/focusout listeners to the root element so that a
     * focus CSS class is toggled when the editor (or any child such as the
     * toolbar) receives or loses focus.
     */
    private void attachFocusListeners(Element root) {
        root.addEventListener("focusin", evt -> root.classList.add(styles().focus()));
        root.addEventListener("focusout", evt -> root.classList.remove(styles().focus()));
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

        String toolbar();

        String editor();

        String floating();

        String hover();

        String nofocus();

        String borderless();

    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS,
        IControlCSS.CONTROL_CSS
    }, stylesheet = """
        .component {
            --jui-formattededitor-border: #ddd;
            --jui-formattededitor-border-radius: 6px;
            --jui-formattededitor-border-toolbar-side: 1px solid var(--jui-formattededitor-border);
            --jui-formattededitor-border-toolbar-top: 1px solid var(--jui-formattededitor-border);
            --jui-formattededitor-border-toolbar-bottom: 1px solid var(--jui-formattededitor-border);
            --jui-formattededitor-border-toolbar-radius: var(--jui-formattededitor-border-radius) var(--jui-formattededitor-border-radius) 0 0;
            --jui-formattededitor-border-toolbar-bg: transparent;
            --jui-formattededitor-border-toolbar-hover-top: -40px;
            --jui-formattededitor-border-toolbar-shadow: none;
            --jui-formattededitor-border-editor-side: 1px solid var(--jui-formattededitor-border);
            --jui-formattededitor-border-editor-top: none;
            --jui-formattededitor-border-editor-bottom: 1px solid var(--jui-formattededitor-border);
            --jui-formattededitor-border-editor-radius: 0 0 var(--jui-formattededitor-border-radius) var(--jui-formattededitor-border-radius);
        }
        .component {
            position: relative;
            display: flex;
            flex-direction: column;
            min-height: 500px;
        }
        .component .toolbar {
            border-left: var(--jui-formattededitor-border-toolbar-side);
            border-right: var(--jui-formattededitor-border-toolbar-side);
            border-top: var(--jui-formattededitor-border-toolbar-top);
            border-bottom: var(--jui-formattededitor-border-toolbar-bottom);
            border-radius: var(--jui-formattededitor-border-toolbar-radius);
            background: var(--jui-formattededitor-border-toolbar-bg);
            box-shadow: var(--jui-formattededitor-border-toolbar-shadow);
            overflow: hidden;
        }
        .component .editor {
            border: 1px solid transparent;
            display: flex;
            flex-direction: column;
        }
        .component:not(.borderless) .editor {
            border-left: var(--jui-formattededitor-border-editor-side);
            border-right: var(--jui-formattededitor-border-editor-side);
            border-top: var(--jui-formattededitor-border-editor-top);
            border-bottom: var(--jui-formattededitor-border-editor-bottom);
            border-radius: var(--jui-formattededitor-border-editor-radius);
        }
        .component:hover .editor {
            border-left: var(--jui-formattededitor-border-editor-side);
            border-right: var(--jui-formattededitor-border-editor-side);
            border-top: var(--jui-formattededitor-border-editor-top);
            border-bottom: var(--jui-formattededitor-border-editor-bottom);
            border-radius: var(--jui-formattededitor-border-editor-radius);
        }
        .component .floating {
            position: fixed;
            display: none;
            border: 1px solid #e5e7eb;
            border-radius: var(--jui-formattededitor-border-radius);
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 1px 4px rgba(0, 0, 0, 0.06);
            background: #fff;
            z-index: 10000;
        }
        .component.hover .toolbar {
            position: absolute;
            top: var(--jui-formattededitor-border-toolbar-hover-top);
            width: 100%;
            display: none;
        }
        .component.hover.focus .toolbar {
            display: block;
        }
        .component.focus:not(.nofocus) {
            border-color: var(--jui-formattededitor-focus-border, var(--jui-ctl-focus));
            border-radius: var(--jui-formattededitor-focus-border-radius, var(--jui-formattededitor-border-radius));
            box-shadow: var(--jui-formattededitor-focus-shadow, 0 0 0 2px var(--jui-ctl-focus-offset));
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
