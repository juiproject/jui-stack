package com.effacy.jui.text.ui.editor2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * Default toolbar for the editor. Renders as a JUI component with
 * configurable {@link ITool} instances.
 * <p>
 * The toolbar is responsible only for its inner representation (buttons,
 * active states, layout). All positional concerns (fixed, floating,
 * containment styling) are managed by the containing control.
 * <p>
 * Tools are configured via {@link Config#tools(ITool...)}. Standard tools
 * are available as constants on {@link Tools}. Custom tools can be created
 * by implementing {@link ITool} directly or using the factory methods on
 * {@link Tools}.
 */
public class EditorToolbar extends Component<EditorToolbar.Config> implements IEditorToolbar {

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

        List<ITool> tools;
        Style style = Style.STANDARD;

        /**
         * Configures which tools to display. When not called (or called with
         * no arguments), all standard tools are displayed (see
         * {@link Tools#all()}).
         */
        public Config tools(ITool... tools) {
            if ((tools != null) && (tools.length > 0))
                this.tools = Arrays.asList(tools);
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

        @Override
        @SuppressWarnings("unchecked")
        public EditorToolbar build(LayoutData... data) {
            return build (new EditorToolbar (this), data);
        }
    }

    /************************************************************************
     * State.
     ************************************************************************/

    private IEditorCommands commands;

    /**
     * Handles returned by rendered tools, for state updates.
     */
    private List<ITool.Handle> handles = new ArrayList<>();

    /************************************************************************
     * Construction.
     ************************************************************************/

    public EditorToolbar() {
        this(new Config());
    }

    public EditorToolbar(Config config) {
        super(config);
        if (config.tools == null)
            config.tools = Arrays.asList(Tools.all());
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
        for (ITool.Handle handle : handles)
            handle.updateState(activeBlockType, activeFormats);
    }

    @Override
    public void updateCellState(Set<FormatType> activeFormats) {
        for (ITool.Handle handle : handles)
            handle.updateCellState(activeFormats);
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            root.style(styles().toolbar());
            ITool.Context ctx = new ITool.Context() {

                @Override
                public IDomInsertableContainer<?> parent() {
                    return root;
                }

                @Override
                public IEditorCommands commands() {
                    return commands;
                }

                @Override
                public ILocalCSS styles() {
                    return EditorToolbar.this.styles();
                }
            };
            for (ITool tool : config().tools) {
                ITool.Handle handle = tool.render(ctx);
                if (handle != null)
                    handles.add(handle);
            }
        }).build();
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
        }
        .toolbar {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: var(--jui-toolbar-gap);
            padding: var(--jui-toolbar-padding);
            background: var(--jui-toolbar-bg);
            flex-shrink: 0;
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
