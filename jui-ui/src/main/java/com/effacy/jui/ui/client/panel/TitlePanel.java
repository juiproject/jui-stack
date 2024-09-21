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

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.H4;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

public class TitlePanel extends PanelBase<TitlePanel.Config> {

    public static class Config extends PanelBase.Config<Config> {

        /**
         * The style of title.
         */
        public enum Style {
            /**
             * The title section is split (with a line).
             */
            SPLIT,

            /**
             * The title section appears above.
             */
            ABOVE;
        }

        /**
         * See {@link #style(Style)}.
         */
        private Style style = Style.SPLIT;

        /**
         * See {@link #title(String)}.
         */
        private String title;

        /**
         * See {@link #subtitle(String)}.
         */
        private String subtitle;

        /**
         * See {@link #icon(String)}.
         */
        private String icon;

        /**
         * See {@link #scale(double)}.
         */
        private double scale = -1.0;

        /**
         * Specifies the style to apply to the panel.
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

        public Config title(String title) {
            this.title = title;
            return this;
        }

        public Config subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Config icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Scale factor for the title size.
         * 
         * @param scale
         *              the scale factor.
         * @return this configuration instance.
         */
        public Config scale(double scale) {
            this.scale = scale;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.panel.Panel.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @SuppressWarnings("unchecked")
        @Override
        public TitlePanel build(LayoutData... data) {
            return (TitlePanel) super.build (new TitlePanel (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    protected TitlePanel(TitlePanel.Config config) {
        super (config, LocalCSS.instance());
    }

    @Override
    public Config config() {
        return (Config) super.config ();
    }


    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).style (styles ().outer()).$ (
            // Title bar for the panel
            Div.$ ().style (styles().title()).$ (title -> {
                if (config().scale > 0.0)
                    title.css("font-size: " + config().scale + "em;");
                // Icon if there is one declared.
                if (!StringSupport.empty(config().icon)) {
                    Em.$(title).style (config().icon).$ (icon -> {
                        if (!StringSupport.empty(config().subtitle))
                            icon.style (styles().large());
                    });
                }
                // Title block (including subtitle if present)
                Div.$ (title).$ (
                    H2.$().text (config().title),
                    H4.$().text (config().subtitle).iff (!StringSupport.empty(config().subtitle))
                );
            }),
            // Contents of the panel
            Div.$ ().style (styles().contents())
        ).build(dom -> {
            Element outerEl = dom.first("outer");
            Element contentsEl = dom.first("contents");
            configurePostRender (el, outerEl, outerEl, contentsEl);
        });
    }

    @Override
    protected void configurePostRender(Element rootEl, Element marginEl, Element borderEl, Element contentsEl) {
        rootEl.classList.add (styles ().theme ());
        if (config ().style == Config.Style.ABOVE) {
            // Above theme.
            rootEl.classList.add (styles ().style_above ());
            super.configurePostRender (rootEl, marginEl, borderEl, contentsEl);
        } else {
            // Split theme.
            rootEl.classList.add (styles ().style_split ());
            super.configurePostRender (rootEl, marginEl, borderEl, contentsEl);
        }
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return (ILocalCSS) super.styles();
    }

    public static interface ILocalCSS extends Panel.ILocalCSS {

        /**
         * For applying a theme.
         */
        public String theme();

        public String style_split();

        public String style_above();

        /**
         * The outer wrapper (carries border).
         */
        public String outer();

        /**
         * Title area.
         */
        public String title();

        /**
         * Modifier (meaningful in context).
         */
        public String large();

    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/panel/TitlePanel.css",
        "com/effacy/jui/ui/client/panel/TitlePanel_Override.css"
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
