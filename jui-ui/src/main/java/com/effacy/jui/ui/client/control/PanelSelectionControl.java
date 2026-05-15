package com.effacy.jui.ui.client.control;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * A multi-selection control rendered as a grid of tiles. Each tile represents
 * a value that can be toggled on or off. Tiles may declare a dependency on
 * another tile — a dependent tile is non-selectable (and visually locked)
 * until its required tile is selected, and is automatically deselected when
 * the required tile is deselected.
 * <p>
 * The control has no chrome of its own (no title, no frame). Wrap it in an
 * enclosing component such as
 * {@link com.effacy.jui.ui.client.fragments.Accordion} if a titled or
 * collapsible presentation is required.
 * <p>
 * The value type is {@code Set<V>}. The iteration order of the returned set
 * matches the order tiles were added.
 * <p>
 * All visual properties are themeable via CSS custom properties prefixed
 * {@code --cpt-panelselection-} (see the component stylesheet for the full
 * list). The default palette is neutral; consumers re-theme by overriding
 * the relevant tokens.
 */
public class PanelSelectionControl<V> extends Control<Set<V>, PanelSelectionControl.Config<V>> {

    /************************************************************************
     * Configuration.
     ************************************************************************/

    public static class Config<V> extends Control.Config<Set<V>, Config<V>> {

        @FunctionalInterface
        public interface Variant {

            /**
             * Configures the control for the given variant.
             */
            public void configure(Config<?> config);
        }

        /**
         * A single tile in the panel.
         */
        static record Tile<V> (V value, String label, String description, V requires) {}

        /**
         * See {@link #columns(int)}.
         */
        private int columns = 3;

        /**
         * See {@link #tile(Object, String, String, Object)}.
         */
        private List<Tile<V>> tiles = new ArrayList<>();

        /**
         * See {@link #tile(Object, String, String, Object)}.
         */
        private ILocalCSS styles;

        /**
         * Applies a predefined variant configuration to the control. Variants are a
         * convenient way to reuse common configurations (e.g. a set of tiles) across
         * multiple instances of the control.
         * 
         * @param variant
         *                the variant to apply.
         * @return this config for chaining.
         */
        public Config<V> variant(Variant variant) {
            if (variant != null)
                variant.configure(this);
            return this;
        }

        /**
         * Number of columns in the tile grid (default {@code 3}).
         */
        public Config<V> columns(int columns) {
            if (columns > 0)
                this.columns = columns;
            return this;
        }

        /**
         * Adds a tile with no dependency.
         */
        public Config<V> tile(V value, String label, String description) {
            return tile(value, label, description, null);
        }

        /**
         * Adds a tile that requires another tile to be selected before it can
         * be selected.
         *
         * @param value
         *                    the value associated with the tile.
         * @param label
         *                    the display label.
         * @param description
         *                    (optional) secondary description text.
         * @param requires
         *                    (optional) the value of another tile that must be
         *                    selected for this tile to be selectable.
         */
        public Config<V> tile(V value, String label, String description, V requires) {
            tiles.add(new Tile<>(value, label, description, requires));
            return this;
        }

        /**
         * Custom styles for the control. If not set, default styles are used.
         * 
         * @param styles
         *               the styles to use.
         * @return this config for chaining.
         */
        public Config<V> styles(ILocalCSS styles) {
            this.styles = styles;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public PanelSelectionControl<V> build(LayoutData... data) {
            return build(new PanelSelectionControl<V>(this), data);
        }
    }

    /************************************************************************
     * Construction.
     ************************************************************************/

    public PanelSelectionControl(Config<V> config) {
        super(config);
    }

    /************************************************************************
     * Value management.
     ************************************************************************/

    private Set<V> currentValue = new LinkedHashSet<>();

    @Override
    protected Set<V> prepareValueForAssignment(Set<V> value) {
        Set<V> result = new LinkedHashSet<>();
        if (value != null)
            result.addAll(value);
        normalize(result);
        return result;
    }

    @Override
    protected Set<V> valueFromSource() {
        return new LinkedHashSet<>(currentValue);
    }

    @Override
    protected void valueToSource(Set<V> value) {
        currentValue.clear();
        if (value != null)
            currentValue.addAll(value);
        refresh();
    }

    /************************************************************************
     * Dependency rules.
     ************************************************************************/

    private Config.Tile<V> findTile(V value) {
        for (Config.Tile<V> t : config().tiles) {
            if (Objects.equals(t.value(), value))
                return t;
        }
        return null;
    }

    private boolean isLocked(V value) {
        Config.Tile<V> t = findTile(value);
        if ((t == null) || (t.requires() == null))
            return false;
        return !currentValue.contains(t.requires());
    }

    private void normalize(Set<V> set) {
        boolean changed;
        do {
            changed = false;
            for (V v : new ArrayList<>(set)) {
                Config.Tile<V> t = findTile(v);
                if ((t != null) && (t.requires() != null) && !set.contains(t.requires())) {
                    set.remove(v);
                    changed = true;
                }
            }
        } while (changed);
    }

    private void toggle(V value) {
        if (isReadOnly() || isDisabled())
            return;
        if (isLocked(value))
            return;
        if (currentValue.contains(value)) {
            currentValue.remove(value);
            for (Config.Tile<V> t : config().tiles) {
                if (Objects.equals(t.requires(), value))
                    currentValue.remove(t.value());
            }
        } else
            currentValue.add(value);
        refresh();
        modified();
    }

    /************************************************************************
     * Rendering.
     ************************************************************************/

    private Map<V,Element> tileEls = new LinkedHashMap<>();

    @Override
    protected INodeProvider buildNode(Element el, Config<V> data) {
        return Wrap.$(el).$(root -> {
            root.css("grid-template-columns: repeat(" + data.columns + ", 1fr);");
            for (Config.Tile<V> t : data.tiles)
                renderTile(root, t);
        }).build(tree -> refresh());
    }

    private void renderTile(ElementBuilder parent, Config.Tile<V> t) {
        Div.$(parent).style(styles().tile()).$(div -> {
            div.use(n -> tileEls.put(t.value(), (Element) n));
            div.onclick(e -> toggle(t.value()));
            Span.$(div).style(styles().box()).$(box -> Em.$(box).style(FontAwesome.check()));
            Div.$(div).$(text -> {
                Div.$(text).style(styles().nm()).text(t.label());
                if (!StringSupport.empty(t.description()))
                    Div.$(text).style(styles().ds()).text(t.description());
            });
        });
    }

    private void refresh() {
        tileEls.forEach((v, el) -> {
            if (currentValue.contains(v))
                el.classList.add(styles().on());
            else
                el.classList.remove(styles().on());
            if (isLocked(v))
                el.classList.add(styles().locked());
            else
                el.classList.remove(styles().locked());
        });
    }

    /************************************************************************
     * CSS.
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        if(config().styles != null)
            return config().styles;
        return LocalCSS.instance();
    }

    public static interface ILocalCSS extends IControlCSS {

        String tile();

        String box();

        String nm();

        String ds();

        String on();

        String locked();
    }

    @CssResource(value = {
        IComponentCSS.COMPONENT_CSS,
        IControlCSS.CONTROL_CSS
    }, stylesheet = """
        .component {
            --cpt-panelselection-gap: 0.75em;
            --cpt-panelselection-tile-padding: 0.5em 0.75em;
            --cpt-panelselection-tile-radius: 8px;
            --cpt-panelselection-tile-gap: 0.625em;
            --cpt-panelselection-tile-bg: #ffffff;
            --cpt-panelselection-tile-border: #d4d4d4;
            --cpt-panelselection-tile-hover-bg: #fafafa;
            --cpt-panelselection-tile-hover-border: #b4b4b4;
            --cpt-panelselection-label-color: #222222;
            --cpt-panelselection-description-color: #6b6b6b;
            --cpt-panelselection-label-size: 1em;
            --cpt-panelselection-description-size: 0.875em;
            --cpt-panelselection-box-size: 1em;
            --cpt-panelselection-box-radius: 3px;
            --cpt-panelselection-box-bg: #ffffff;
            --cpt-panelselection-box-border: #b4b4b4;
            --cpt-panelselection-on-bg: #eef4fb;
            --cpt-panelselection-on-border: #3b78c0;
            --cpt-panelselection-on-box-bg: #3b78c0;
            --cpt-panelselection-on-box-border: #3b78c0;
            --cpt-panelselection-on-check-color: #ffffff;
            --cpt-panelselection-locked-bg: #f4f4f4;
            --cpt-panelselection-locked-border: #e5e5e5;
            --cpt-panelselection-locked-label-color: #9a9a9a;
            --cpt-panelselection-locked-description-color: #b0b0b0;
            --cpt-panelselection-locked-box-bg: #ededed;
            --cpt-panelselection-locked-box-border: #cfcfcf;

            display: grid;
            gap: var(--cpt-panelselection-gap);
        }
        .component .tile {
            display: flex;
            align-items: flex-start;
            gap: var(--cpt-panelselection-tile-gap);
            border: 1px solid var(--cpt-panelselection-tile-border);
            border-radius: var(--cpt-panelselection-tile-radius);
            padding: var(--cpt-panelselection-tile-padding);
            background: var(--cpt-panelselection-tile-bg);
            cursor: pointer;
            transition: border-color 0.15s ease, background 0.15s ease;
        }
        .component .tile:hover {
            border-color: var(--cpt-panelselection-tile-hover-border);
            background: var(--cpt-panelselection-tile-hover-bg);
        }
        .component .tile.on {
            border-color: var(--cpt-panelselection-on-border);
            background: var(--cpt-panelselection-on-bg);
        }
        .component .tile.locked {
            cursor: not-allowed;
            border-color: var(--cpt-panelselection-locked-border);
            background: var(--cpt-panelselection-locked-bg);
        }
        .component .tile.locked:hover {
            border-color: var(--cpt-panelselection-locked-border);
            background: var(--cpt-panelselection-locked-bg);
        }
        .component .tile.locked .nm {
            color: var(--cpt-panelselection-locked-label-color);
        }
        .component .tile.locked .ds {
            color: var(--cpt-panelselection-locked-description-color);
        }
        .component .tile.locked .box {
            border-color: var(--cpt-panelselection-locked-box-border);
            background: var(--cpt-panelselection-locked-box-bg);
        }
        .component .tile .box {
            width: var(--cpt-panelselection-box-size);
            height: var(--cpt-panelselection-box-size);
            border-radius: var(--cpt-panelselection-box-radius);
            border: 1.5px solid var(--cpt-panelselection-box-border);
            flex-shrink: 0;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-top: 0.1em;
            background: var(--cpt-panelselection-box-bg);
        }
        .component .tile .box em {
            color: transparent;
            font-size: 0.75em;
        }
        .component .tile.on .box {
            background: var(--cpt-panelselection-on-box-bg);
            border-color: var(--cpt-panelselection-on-box-border);
        }
        .component .tile.on .box em {
            color: var(--cpt-panelselection-on-check-color);
        }
        .component .tile .nm {
            font-size: var(--cpt-panelselection-label-size);
            font-weight: 600;
            color: var(--cpt-panelselection-label-color);
            line-height: 1.2;
        }
        .component .tile .ds {
            font-size: var(--cpt-panelselection-description-size);
            color: var(--cpt-panelselection-description-color);
            margin-top: 0.2em;
        }
        .component.read_only .tile,
        .component.disabled .tile {
            cursor: default;
            opacity: 0.7;
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