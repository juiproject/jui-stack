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

import com.effacy.jui.core.client.IClosable;
import com.effacy.jui.core.client.IOpenAware;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.CardFitLayoutCreator;
import com.effacy.jui.core.client.component.layout.ILayout;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Border;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.modal.Modal.IModalController;

import elemental2.dom.Element;

/**
 * Base class for creating panels.
 */
public abstract class PanelBase<C extends PanelBase.Config<C>> extends Component<C> implements IClosable, IOpenAware {
    
    /**
     * Configuration for the panel.
     */
    public static class Config<C extends Config<C>> extends Component.Config {

        /**
         * See {@link #padding(Insets)}.
         */
        protected Insets padding;

        /**
         * See {@link #margin(Insets)}.
         */
        protected Insets margin;

        /**
         * See {@link #border(Border)}.
         */
        protected Border border;

        /**
         * See {@link #color(String)}.
         */
        protected String color;

        /**
         * See {@link #scrollable(boolean)}.
         */
        protected boolean scrollable;

        /**
         * See {@link #width(Length)}.
         */
        protected Length width;

        /**
         * See {@link #widthMax(Length)}.
         */
        protected Length widthMax;

        /**
         * See {@link #layout(ILayout)}.
         */
        protected ILayout layout;

        /**
         * Apply internal padding to the panel contents area.
         * 
         * @param padding
         *                the padding to apply.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C padding(Insets padding) {
            this.padding = padding;
            return (C) this;
        }

        /**
         * Apply margin to the panel (relevant where there is a border).
         * 
         * @param margin
         *               the margin to apply.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C margin(Insets margin) {
            this.margin = margin;
            return (C) this;
        }

        /**
         * Configures the border style to apply.
         * 
         * @param border
         *               the border style.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C border(Border border) {
            this.border = border;
            return (C) this;
        }

        /**
         * Applies a background color for the contents area.
         * 
         * @param color
         *              the color to apply.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C color(String color) {
            this.color = color;
            return (C) this;
        }

        /**
         * Determines if the panel contents is scrollable. This will force the panel to
         * be full height (which means that if it is contained in another scrollable
         * panel the parent will scroll not this).
         * 
         * @param scrollable
         *                   {@code true} if to be scrollable.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C scrollable(boolean scrollable) {
            this.scrollable = scrollable;
            return (C) this;
        }

        /**
         * Convenience for <code>scrollable(true)</code>.
         * 
         * @return this configuration instance.
         * @see {@link #scrollable(boolean)}.
         */
        public C scrollable() {
            return scrollable (true);
        }

        /**
         * Assigns the contents layout.
         * <p>
         * The default layout is {@link CardFitLayout} with full fitting (absolute
         * positioning of the child). However, if the panel is scrollable then the
         * default is again {@link CardFitLayout} but this time without full fitting
         * vertically (only horizontally).
         * 
         * @param layout
         *               the layout.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C layout(ILayout layout) {
            this.layout = layout;
            return (C) this;
        }

        /**
         * Applies a fixed width to the panel.
         * 
         * @param width
         *              the width.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C width(Length width) {
            this.width = width;
            return (C) this;
        }

        /**
         * Applies a fixed maximum width to the panel.
         * 
         * @param widthMax
         *              the maximum width.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public C widthMax(Length widthMax) {
            this.widthMax = widthMax;
            return (C) this;
        }

    }


    /**
     * The contents area reference.
     */
    protected static final String REGION_CONTENTS = "CONTENTS";

    /**
     * The CSS styles.
     */
    protected ILocalCSS styles;

    /**
     * Construct from config.
     * 
     * @param config
     *               the configuration.
     */
    protected PanelBase(C config, ILocalCSS styles) {
        super (config);
        this.styles = styles;

        // Assign layout to the content area. See comment on Config#layout(...).
        ILayout layout = config.layout;
        if (layout == null)
            layout = CardFitLayoutCreator.create (!config.scrollable);
        findRegionPoint (REGION_CONTENTS).setLayout (layout);
    }

    /**
     * Adds a component to the contents of the panel.
     * 
     * @param <C>
     *            the component type
     * @param cpt
     *            the component
     * @return the passed component.
     */
    public <C extends IComponent> C add(C cpt) {
        findRegionPoint (REGION_CONTENTS).add (cpt);
        return cpt;
    }

    /**
     * Adds a component to the contents of the panel.
     * 
     * @param <C>
     *              the component type
     * @param cpt
     *              the component
     * @param hints
     *              layout hints (specific to the registered layout).
     * @return the passed component.
     */
    public <C extends IComponent> C add(C cpt, LayoutData hints) {
        findRegionPoint (REGION_CONTENTS).add (cpt, hints);
        return cpt;
    }

    /**
     * Makes the passed component the active one with respect to the layout.
     * <p>
     * The default layout for a panel is {@link CardFitLayout} and this supports
     * {@link ILayout#activate(IComponent)}. If another layout is declared then this
     * will only have meaning for that layout.
     * 
     * @param cpt
     *            the component.
     */
    public void activate(IComponent cpt) {
        findRegionPoint (REGION_CONTENTS).getLayout ().activate (cpt);
    }

    @Override
    public void close() {
        // Fires a close event.
        fireEvent (IModalController.class).close ();
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (
            Div.$ ()
                .by ("contents")
                .style (styles().contents())
                .use (n -> findRegionPoint (REGION_CONTENTS).setElement ((Element) n))
        ).build(dom -> {
            Element contentsEl = dom.first("contents");
            configurePostRender (el, contentsEl, contentsEl, contentsEl);
        });
    }

    /**
     * Used by {@link #scrollToTop()}.
     */
    protected Element contentsEl;

    /**
     * Scroll the contents to the top of the page.
     * <p>
     * This is deferred until the end of the browser event loop to ensure any other
     * changes are put in place.
     * <p>
     * The target element is resolved from the last element of
     * {@link #configurePostRender(Element, Element, Element, Element)}. If this is
     * overridden then you can set it directly via the {@link #contentsEl} member.
     */
    public void scrollToTop() {
        TimerSupport.defer (()-> {
            if (contentsEl != null)
                JQuery.$ (contentsEl).scrollTop (0);
        });   
    }

    /**
     * Applies configuration to the root and contents.
     * 
     * @param rootEl
     *                   the component root element.
     * @param marginEl
     *                   the element to apply margin to.
     * @param borderEl
     *                   the element to apply the border to.
     * @param contentsEl
     *                   the contents element.
     */
    protected void configurePostRender(Element rootEl, Element marginEl, Element borderEl, Element contentsEl) {
        this.contentsEl = contentsEl;
        if (config ().width != null)
            CSS.WIDTH.apply (rootEl, config ().width);
        if (config ().widthMax != null)
            CSS.MAX_WIDTH.apply (rootEl, config ().widthMax);
        if (config ().scrollable)
            rootEl.classList.add (styles ().scrollable ());
        if (config ().margin != null)
            config ().margin.margin (marginEl);
        if (config ().border != null)
            config ().border.apply (borderEl);
        if (config ().padding != null) {
            if (config().scrollable)
                config ().padding.padding (contentsEl);
            else
                config ().padding.padding (rootEl);
        }
        if (!StringSupport.empty (config ().color))
            CSS.BACKGROUND_COLOR.with (Color.raw (config ().color)).apply (contentsEl);
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return styles;
    }

    public static interface ILocalCSS extends IComponentCSS {

        /**
         * Scrollable.
         */
        public String scrollable();

        /**
         * Contents.
         */
        public String contents();

    }
}
