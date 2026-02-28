package com.effacy.jui.text.ui.editor2;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedText;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * JUI control that composes an {@link Editor} with a toolbar (either
 * {@link EditorToolbar} or {@link FloatingToolbar}) to provide a complete
 * rich text editing experience with value management and dirty detection.
 * <p>
 * Usage:
 * <pre>
 * FormattedTextEditor editor = new FormattedTextEditor(new FormattedTextEditor.Config()
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

        boolean toolbarBelow;
        boolean floatingToolbar;
        EditorToolbar.Config toolbarConfig;
        Editor.Config editorConfig;

        /**
         * Places the toolbar below the editor area instead of above (default
         * is above).
         */
        public Config toolbarBelow(boolean below) {
            this.toolbarBelow = below;
            return this;
        }

        /**
         * Uses a floating toolbar that appears on text selection instead of a
         * fixed toolbar.
         */
        public Config floatingToolbar(boolean enable) {
            this.floatingToolbar = enable;
            return this;
        }

        /**
         * Configures the toolbar (tool selection, etc).
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

        if (data.floatingToolbar) {
            // Floating toolbar — no fixed toolbar component in the DOM.
            FloatingToolbar floatingTb = new FloatingToolbar(tbConfig);
            editor.bind(floatingTb);
            return Wrap.$(el).$(root -> {
                root.style(styles().component());
                Cpt.$(root, editor);
            }).build();
        }

        // Fixed toolbar.
        EditorToolbar fixedTb = new EditorToolbar(tbConfig);
        editor.bind(fixedTb);
        return Wrap.$(el).$(root -> {
            root.style(styles().component());
            if (!data.toolbarBelow) {
                Cpt.$(root, fixedTb);
                Cpt.$(root, editor);
            } else {
                Cpt.$(root, editor);
                Cpt.$(root, fixedTb);
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
