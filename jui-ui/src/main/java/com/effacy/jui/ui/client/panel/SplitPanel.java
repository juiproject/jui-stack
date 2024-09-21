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
package com.effacy.jui.ui.client.panel;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.ILayout;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

public class SplitPanel extends PanelBase<SplitPanel.Config> {

    public static class Config extends PanelBase.Config<Config> {

        /**
         * Styles for the panel.
         */
        public interface Style {

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();


            /**
             * Convenience to create a style.
             * 
             * @param styles
             *               the CSS styles.
             * @return the associated style.
             */
            public static Style create(ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                };
            }

            public static final Style STANDARD = create (StandardLocalCSS.instance ());

        }

        /**
         * The defauly style to apply. This can be changed by assigning directly.
         */
        public static Style DEFAULT_STYLE = Style.STANDARD;

        /**
         * See {@link #style(Style)}.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #vertical(boolean)}.
         */
        private boolean vertical;

        /**
         * See {@link #reverse(boolean)}.
         */
        private boolean reverse;

        /**
         * See {@link #separator(boolean)}.
         */
        private boolean separator;

        /**
         * See {@link #otherLayout(ILayout)}.
         */
        private ILayout otherLayout;

        /**
         * Applies an alternative style to the panel.
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Convenience for <code>vertical(true)</code>.
         * 
         * @return this configuration instance.
         */
        public Config vertical() {
            return vertical (true);
        }

        /**
         * Determines if the split is along the vertical.
         * 
         * @param veritical
         *                  {@code true} if it is.
         * @return this configuration instance.
         */
        public Config vertical(boolean vertical) {
            this.vertical = vertical;
            return this;
        }

        /**
         * Convenience for <code>reverse(true)</code>.
         * 
         * @return this configuration instance.
         */
        public Config reverse() {
            return reverse (true);
        }

        /**
         * Determines if the "grow" content area is reversed.
         * 
         * @param reverse
         *                {@code true} if it is.
         * @return this configuration instance.
         */
        public Config reverse(boolean reverse) {
            this.reverse = reverse;
            return this;
        }

        /**
         * Determines if there is a separator.
         * 
         * @param separator
         *                  {@code true} if there is one.
         * @return this configuration instance.
         */
        public Config separator(boolean separator) {
            this.separator = separator;
            return this;
        }

        /**
         * Convenience for <code>separator(true)</code>.
         * 
         * @return this configuration instance.
         */
        public Config separator() {
            return separator (true);
        }

        /**
         * Assigns the other contents layout.
         * 
         * @param layout
         *               the layout.
         * @return this configuration instance.
         */
        public Config otherLayout(ILayout otherLayout) {
            this.otherLayout = otherLayout;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.panel.Panel.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @SuppressWarnings("unchecked")
        @Override
        public SplitPanel build(LayoutData... data) {
            return (SplitPanel) super.build (new SplitPanel (this), data);
        }

    }

    private static final String REGION_OTHER = "OTHER";

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    protected SplitPanel(SplitPanel.Config config) {
        super (config, config.style.styles());

        // Assign layout to the "other" content area.
        ILayout layout = config.otherLayout;
        if (layout == null)
            layout = new CardFitLayout.Config (false).build ();
        findRegionPoint (REGION_OTHER).setLayout (layout);
    }

    @Override
    public Config config() {
        return (Config) super.config ();
    }

    /**
     * Adds a component to the other contents of the panel.
     * 
     * @param <C>
     *            the component type
     * @param cpt
     *            the component
     * @return the passed component.
     */
    public <C extends IComponent> C addOther(C cpt) {
        findRegionPoint (REGION_OTHER).add (cpt);
        return cpt;
    }

    /**
     * Adds a component to the other contents of the panel.
     * 
     * @param <C>
     *              the component type
     * @param cpt
     *              the component
     * @param hints
     *              layout hints (specific to the registered layout).
     * @return the passed component.
     */
    public <C extends IComponent> C addOther(C cpt, LayoutData hints) {
        findRegionPoint (REGION_OTHER).add (cpt, hints);
        return cpt;
    }

    /**
     * Makes the passed component the active one with respect to the layout in the
     * other contents.
     * <p>
     * The default layout for a panel is {@link CardFitLayout} and this supports
     * {@link ILayout#activate(IComponent)}. If another layout is declared then this
     * will only have meaning for that layout.
     * 
     * @param cpt
     *            the component.
     */
    public void activateOther(IComponent cpt) {
        findRegionPoint (REGION_OTHER).getLayout ().activate (cpt);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (
            Div.$ ().by("outer").style (styles ().outer()).$ (
                Div.$ ().by ("contents").style (styles().contents())
                    .use (n -> findRegionPoint (REGION_CONTENTS).setElement ((Element) n)),
                Div.$ ().by ("other").style (styles().other())
                    .use (n -> findRegionPoint (REGION_OTHER).setElement ((Element) n))
            )
        ).build (dom -> {
            Element outerEl = dom.first("outer");
            Element contentsEl = dom.first("contents");
            configurePostRender (el, outerEl, outerEl, contentsEl);
        });
    }

    @Override
    protected void configurePostRender(Element rootEl, Element marginEl, Element borderEl, Element contentsEl) {
        if (config ().vertical) {
            if (config ().reverse)
                rootEl.classList.add (styles ().vert_rev ());
            else
                rootEl.classList.add (styles ().vert ());
        } else {
            if (config ().reverse)
                rootEl.classList.add (styles ().hort_rev ());
            else
                rootEl.classList.add (styles ().hort ());
        }
        if (config ().separator)
            rootEl.classList.add (styles ().separator ());
        super.configurePostRender (rootEl, marginEl, borderEl, contentsEl);
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return (ILocalCSS) super.styles();
    }

    public static interface ILocalCSS extends PanelBase.ILocalCSS {

        /**
         * The outer wrapper (carries border).
         */
        public String outer();

        /**
         * Other content area.
         */
        public String other();

        public String vert();

        public String vert_rev();

        public String hort();

        public String hort_rev();

        public String separator();

    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/panel/SplitPanel.css",
        "com/effacy/jui/ui/client/panel/SplitPanel_Override.css"
    })
    public static abstract class StandardLocalCSS implements ILocalCSS {

        private static StandardLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardLocalCSS) GWT.create (StandardLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
    
}
