package com.effacy.jui.text.ui.editor2;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedText;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * JUI control that composes an {@link Editor} with an {@link EditorToolbar}
 * to provide a complete rich text editing experience with value management
 * and dirty detection.
 * <p>
 * Toolbar position is configured via
 * {@link EditorToolbar.Config#position(EditorToolbar.Position)}: {@code TOP}
 * (default), {@code BOTTOM}, or {@code FLOATING}.
 * <p>
 * Usage:
 * <pre>
 * FormattedTextEditor editor = new FormattedTextEditor(new FormattedTextEditor.Config()
 *     .editor(new Editor.Config()
 *         .linkOptions(MyApp::filterLinks)
 *         .variableOptions(MyApp::filterVariables))
 *     .toolbar(new EditorToolbar.Config()
 *         .position(Position.BOTTOM)
 *         .tools(Tool.BOLD, Tool.ITALIC, Tool.UNDERLINE, Tool.H1, Tool.H2)));
 * editor.setValue(Value.of(myDocument));
 * </pre>
 */
public class FormattedTextEditor extends Control<FormattedText, FormattedTextEditor.Config> {

    /**
     * Configuration for the formatted text editor.
     */
    public static class Config extends Control.Config<FormattedText, Config> {

        EditorToolbar.Config toolbarConfig;
        Editor.Config editorConfig;
        Length height;

        /**
         * Configures the toolbar (tool selection, position, etc).
         */
        public Config toolbar(EditorToolbar.Config config) {
            this.toolbarConfig = config;
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
        EditorToolbar.Config tbConfig = (data.toolbarConfig != null) ? data.toolbarConfig : new EditorToolbar.Config();

        editor = new Editor(editorConfig);
        EditorToolbar toolbar = new EditorToolbar(tbConfig);
        editor.bind(toolbar);

        if (tbConfig.position == EditorToolbar.Position.FLOATING) {
            // Floating toolbar renders into a body-level container (not in
            // the component DOM tree).
            return Wrap.$(el).$(root -> {
                root.style(styles().component());
                if (data.height != null)
                    root.css(CSS.MIN_HEIGHT, data.height);
                Cpt.$(root, editor);
            }).build();
        }

        // Fixed toolbar (TOP or BOTTOM).
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            if (data.height != null)
                root.css(CSS.MIN_HEIGHT, data.height);
            if (tbConfig.position == EditorToolbar.Position.BOTTOM) {
                Cpt.$(root, editor);
                Cpt.$(root, toolbar);
            } else {
                Cpt.$(root, toolbar);
                Cpt.$(root, editor);
            }
        }).build();
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
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IControlCSS {
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
