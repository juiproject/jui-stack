/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.core.client.component.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.IShowHideListener;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.Config.Zone;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Logger;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * Layout that will arrange components horizontally into zones. Within each zone
 * components are arranged horizontally aligned either left, right or centrally.
 * The zones are distributed fully across the container and may either be of
 * equal width, of a width that is forced by the components they contain or
 * proportionally based on a specification of zone widths.
 * <p>
 * The default specifies two zones, the contents of the first zone are left
 * aligned while those of the second zone are right aligned.
 * <p>
 * To assign a component to a specific zone one should add the component with
 * the appropriate zone information in its {@link ActionBarLayoutData}.
 * 
 * @author Jeremy Buckley
 */
public class ActionBarLayout extends Layout {

    /**
     * Convenience to create layout data for a given zone.
     * 
     * @param zone
     *             the zone.
     * @return the layout data.
     */
    public static Data data(int zone) {
        return new Data (zone);
    }

    /**
     * Layout configuration.
     */
    public static class Config {

        /**
         * See {@link #valign(VAlignment)}.
         */
        private VAlignment valign;

        /**
         * See {@link #insets(Insets)}.
         */
        private Insets insets;

        /**
         * See {@link #zone(Zone...)} (and others).
         */
        private List<Zone> zones = new ArrayList<Zone> ();

        /**
         * Default constructor.
         */
        public Config() {
            // Nothing.
        }

        /**
         * Construct with a standard set of zones and item spacing within zones. For one
         * zone align will be to the right. For two zones alignment will be left
         * followed by right. For more than two zones the central zones will be oriented
         * centrally.
         * 
         * @param zones
         *                the number of zones.
         * @param spacing
         *                the inter-item spacing in a zone.
         */
        public Config(int zones, Length spacing) {
            for (int i = 0; i < zones; i++) {
                if (i == 0)
                    this.zones.add (new Zone (HAlignment.LEFT, spacing));
                else if (i < (zones - 1))
                    this.zones.add (new Zone (HAlignment.CENTER, spacing));
                else
                    this.zones.add (new Zone (HAlignment.RIGHT, spacing));
            }
        }

        /**
         * Apply the passed zone(s).
         * 
         * @param zones
         *              the zone(s).
         * @return this configuration instance.
         */
        public Config zone(Zone... zones) {
            for (Zone zone : zones) {
                if (zone == null)
                    continue;
                this.zones.add (zone);
            }
            return this;
        }

        /**
         * Adds a zone with the given configuration data.
         * 
         * @param align
         *              the horizontal alignment to apply.
         * @return this configuration instance.
         */
        public Config zone(HAlignment align) {
            if (align != null)
                this.zones.add (new Zone (align));
            return this;
        }

        /**
         * Adds a zone with the given configuration data.
         * 
         * @param align
         *                the horizontal alignment to apply.
         * @param spacing
         *                the spacing between items in a zone.
         * @return this configuration instance.
         */
        public Config zone(HAlignment align, int spacing) {
            if (align != null)
                this.zones.add (new Zone (align, spacing));
            return this;
        }

        /**
         * Adds a zone with the given configuration data.
         * 
         * @param align
         *                the horizontal alignment to apply.
         * @param spacing
         *                the spacing between items in a zone.
         * @return this configuration instance.
         */
        public Config zone(HAlignment align, Length spacing) {
            if (align != null)
                this.zones.add (new Zone (align, spacing));
            return this;
        }

        /**
         * Inserts to realize as padding around the elements in the layout.
         * 
         * @param insets
         *               the insets to apply.
         * @return this configuration instance.
         */
        public Config insets(Insets insets) {
            this.insets = insets;
            return this;
        }

        /**
         * Vertical alignment of zones within the layout (default is center).
         * 
         * @param valign
         *               the alignment to apply.
         * @return this configuration instance.
         */
        public Config valign(VAlignment valign) {
            this.valign = valign;
            return this;
        }

        /**
         * Builds an instance of the layout.
         * 
         * @return the layout.
         */
        public ActionBarLayout build() {
            if (zones.isEmpty ())
                zones.add (new Zone (HAlignment.LEFT));
            return new ActionBarLayout (this);
        }

        /**
         * A descriptor for a zone. Each zone that makes up an action bar layout has an
         * alignment for components in that zone (left, middle or right) and the zone
         * may specify a width (represented as a fraction of the total width of the
         * container). Spacing between the components may also be specified (the default
         * being 4 pixels) along with the vertical alignment.
         */
        public static class Zone {

            /**
             * See {@link Zone#Zone(HAlignment)}.
             */
            public static Zone $(HAlignment align) {
                return new Zone (align);
            }

            /**
             * See {@link Zone#Zone(HAlignment, int)}.
             */
            public static Zone $(HAlignment align, int spacing) {
                return new Zone (align, spacing);
            }

            /**
             * See {@link Zone#Zone(HAlignment, Length)}.
             */
            public static Zone $(HAlignment align, Length length) {
                return new Zone (align, length);
            }

            /**
             * The zone content horizontal alignment.
             */
            private HAlignment align = HAlignment.LEFT;

            /**
             * The zone vertical alignment.
             */
            private VAlignment verticalAlign = VAlignment.MIDDLE;

            /**
             * The spacing between elements in the zone.
             */
            private Length spacing = Length.px (4);

            /**
             * Construct with alignment.
             * 
             * @param align
             *              the zone alignment.
             */
            public Zone(HAlignment align) {
                this.align = (align != null) ? align : HAlignment.LEFT;
            }

            /**
             * Construct with alignment and width.
             * 
             * @param align
             *                the zone alignment.
             * @param spacing
             *                the spacing (in px) between the components in the zone.
             */
            public Zone(HAlignment align, int spacing) {
                this (align, Length.px (spacing));
            }

            /**
             * Construct with alignment and width.
             * 
             * @param align
             *                the zone alignment.
             * @param spacing
             *                the spacing between the components in the zone.
             */
            public Zone(HAlignment align, Length spacing) {
                this.align = (align != null) ? align : HAlignment.LEFT;
                this.spacing = (spacing == null) ? Length.px (4) : spacing;
            }

            /**
             * Set the vertical alignment for the zone (default is
             * {@link VAlignment#MIDDLE}).
             * 
             * @param verticalAlign
             *                      the vertical alignment to apply.
             */
            public Zone verticalAlignment(VAlignment verticalAlign) {
                this.verticalAlign = (verticalAlign != null) ? verticalAlign : VAlignment.MIDDLE;
                return this;
            }

            /**
             * Determines the vertical alignment for the zone.
             * 
             * @return The vertical alignment.
             */
            public VAlignment getVerticalAlignment() {
                return verticalAlign;
            }

            /**
             * Determines the spacing between components in the zone.
             * 
             * @return The spacing between components in pixels.
             */
            public Length getSpacing() {
                return spacing;
            }
        }
    }

    /**
     * Layout data for the {@link ActionBarLayout}. The layout specifies zones that
     * components may be placed into and the layout data allows for the targeting of
     * a component for a specific zone.
     */
    public static class Data extends LayoutData {

        /**
         * The zone that the component should reside in.
         */
        private int zone;

        /**
         * Default constructor.
         */
        public Data(int zone) {
            zone (zone);
        }

        /**
         * Gets the target zone for the component.
         * 
         * @return The target zone (indexed from 0 left to right).
         */
        public int getZone() {
            return zone;
        }

        /**
         * Sets the zone.
         * 
         * @param zone
         *             the zone.
         * @return this data instance.
         * @see ActionBarLayoutData#getZone() for details.
         */
        public Data zone(int zone) {
            this.zone = zone;
            return this;
        }

    }

    /**
     * An alignment for horizontal positioning.
     */
    public enum HAlignment {

        /**
         * Left align.
         */
        LEFT,

        /**
         * Right align.
         */
        RIGHT,

        /**
         * Center align.
         */
        CENTER;
    }

    /**
     * Alignment for vertical positioning.
     */
    public enum VAlignment {

        /**
         * Top alignment.
         */
        TOP,

        /**
         * Middle alignment.
         */
        MIDDLE,

        /**
         * Bottom alignment.
         */
        BOTTOM;

    }

    /**
     * Standard layout factory implementation for this layout.
     */
    public static final ILayoutFactory FACTORY = new ILayoutFactory () {

        @Override
        public ILayout create() {
            return new ActionBarLayout.Config (2, Length.px (4)).build ();
        }

    };

    /**
     * The elements (which are table rows) that contain the component of each zone.
     */
    private List<ZoneElement> zoneElements;

    /**
     * Configuration data for the layout.
     */
    private Config config;

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    protected ActionBarLayout(ActionBarLayout.Config config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.StyleLayout#onLayout(com.effacy.gwt.common.client.dom.Elem)
     */
    @Override
    protected void onLayout(Element target) {
        int zoneLen = config.zones.size ();

        final ILocalCSS styles = LocalCSS.instance ();
        target.classList.add (styles.layout ());
        if (config.insets != null)
            config.insets.padding (target);
        if (config.valign == VAlignment.TOP)
            target.classList.add (styles.top ());
        else if (config.valign == VAlignment.BOTTOM)
            target.classList.add (styles.bottom ());

        // Build up the zone structure.
        if (zoneElements == null) {
            ITemplateBuilder.<List<Zone>>renderer ("ActionBarLayout", builder -> {
                builder.loop (d -> d).div (item -> {
                    item.addClassName (styles.zone ());
                    item.id (Provider.loop (ctx -> "item-" + ctx.getIndex ()));
                });
            }).render (Js.cast (target), config.zones);
            zoneElements = new ArrayList<ZoneElement> ();
            Map<String, Element> zoneElById = new HashMap<> ();
            for (Element el : JQuery.$(target).find("div." + styles.zone ()).get ())
                zoneElById.put (el.id, el);
            for (int i = 0; i < zoneLen; i++) {
                Element zoneEl = zoneElById.get ("item-" + i);
                if (zoneEl == null)
                    continue;
                this.zoneElements.add (new ZoneElement (config.zones.get (i), zoneEl));
            }
        }

        // Allocate components to the zone elements. This will render and / or
        // move components.
        for (IComponent item : getItems ()) {
            LayoutData data = getLayoutData (item);
            if (data instanceof ActionBarLayout.Data) {
                int zoneIndex = ((ActionBarLayout.Data) data).getZone ();
                if ((zoneIndex >= 0) && (zoneIndex < zoneLen))
                    zoneElements.get (zoneIndex).add (item);
                else
                    Logger.log ("!!!ActionBarLayout item specified invalid zone (" + item.getClass ().getName () + ")");
            } else {
                zoneElements.get (0).add (item);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.StyleLayout#onComponentRemove(com.effacy.jui.core.client.component.IComponent)
     */
    @Override
    protected void onComponentRemove(IComponent component) {
        super.onComponentRemove (component);
        this.zoneElements = null;
    }

    /**
     * Zone element.
     */
    protected class ZoneElement {

        /**
         * The element that holds the zone.
         */
        private Element el;

        /**
         * The components in the zone element.
         */
        private List<IComponent> components = new ArrayList<IComponent> ();

        /**
         * Construct with configuration.
         * 
         * @param config
         *               the configuration for the zone.
         * @param el
         *               the zone element (where items will be placed).
         */
        public ZoneElement(Zone config, Element el) {
            this.el = el;
            if (config.getSpacing () != null)
                CSS.GAP.with (config.spacing).apply (el);
            if (config.align == HAlignment.LEFT)
                el.classList.add (LocalCSS.instance ().left ());
            else if (config.align == HAlignment.RIGHT)
                el.classList.add (LocalCSS.instance ().right ());
        }

        /**
         * Adds a component to the zone.
         * 
         * @param component
         *                  the component to add.
         */
        public void add(IComponent component) {
            if (!this.components.contains (component)) {
                this.components.add (component);
                if ((component.getRoot () != null) && DomSupport.isChildOf (Js.cast (component.getRoot ()), el))
                    return;
                final Element target = appendItem (el);
                if (component.getRoot () == null)
                    component.render (Js.cast (target), -1);
                else
                    DomSupport.insertChild (target, component.getRoot (), -1);

                // Here we properly show and hide the component container so that we don't end
                // up with extra gaps.
                component.addListener(new IShowHideListener() {

                    @Override
                    public void onShow(IComponent cpt) {
                        JQuery.$ (target).show ();
                    }

                    @Override
                    public void onHide(IComponent cpt) {
                        JQuery.$ (target).hide ();
                    }
                    
                });
            }
        }

        /**
         * Inserts a cell into the passed element (which is expected to be a table row).
         * 
         * @param zone
         *             the zone element to append the item to.
         */
        protected Element appendItem(Element zone) {
            ILocalCSS styles = LocalCSS.instance ();
            Element itemEl = DomGlobal.document.createElement ("div");
            itemEl.classList.add (styles.item ());
            DomSupport.insertChild (zone, itemEl, zone.childElementCount);
            return itemEl;
        }
    }

    /**
     * CSS styles for the layout.
     */
    public static interface ILocalCSS extends CssDeclaration {

        /**
         * The top-level element for the layout.
         */
        public String layout();

        /**
         * A zone of the layout.
         */
        public String zone();

        /**
         * A single layout item (in a zone).
         */
        public String item();

        /**
         * Align to the left.
         */
        public String left();

        /**
         * Align to the right.
         */
        public String right();

        /**
         * Align to the top.
         */
        public String top();

        /**
         * Align to the bottom.
         */
        public String bottom();
    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/core/client/component/layout/ActionBarLayout.css",
        "com/effacy/jui/core/client/component/layout/ActionBarLayout_Override.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
