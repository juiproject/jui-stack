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

import com.effacy.jui.core.client.IActivateAware;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.CardFitLayout.Config.Effect;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.css.Position;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.navigation.NavigationHandler;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

/**
 * The fit layout forces the contained component to extend to the bounds of the
 * enclosing container (the client width of the container element and the style
 * height of the container element).
 * <p>
 * Note that a default configuration can be set (see
 * {@link Config#DEFAULT_CONFIG}) at the application (top) level and all newly
 * created configurations will be based on this.
 * 
 * @author Jeremy Buckley
 */
public class CardFitLayout extends Layout {
    
    /**
    * Convenience to construct layout configuration.
    * 
    * @return the configuration.
    */
   public static CardFitLayout.Config $() {
       return new CardFitLayout.Config ();
   }

   /**
    * Convenience to create layout data.
    * 
    * @return the layout data.
    */
   public static CardFitLayoutData data() {
       return new CardFitLayoutData ();
   }

    /**
     * Configuration for the layout.
     */
    public static class Config {

        /**
         * The default configuration to apply to all newly created configurations. This
         * can be set at the application level to configure the default.
         */
        public static Config DEFAULT_CONFIG = null;

        /**
         * Enumerates the various possible transition effects.
         */
        public enum Effect {
            /**
             * No fade effect.
             */
            NONE,

            /**
             * Fades in.
             */
            FADE_IN,

            /**
             * Fades in even when activating an already activated component.
             */
            FADE_IN_ALL;
        }

        /**
         * See {@link #fitWidth(boolean)}.
         */
        protected boolean fitWidth = true;

        /**
         * See {@link #fitHeight(boolean)}.
         */
        protected boolean fitHeight = true;

        /**
         * See {@link #usePercentForWidth(boolean)}.
         */
        protected boolean usePercentForWidth = false;

        /**
         * See {@link #usePercentForHeight(boolean)}.
         */
        protected boolean usePercentForHeight = false;

        /**
         * Baseline layout data.
         */
        protected CardFitLayoutData baseline = new CardFitLayoutData ();

        /**
         * Default configuration.
         * <p>
         * If {@link #DEFAULT_CONFIG} is set then that will be used to pre-populate the
         * configuration.
         */
        public Config() {
            this (DEFAULT_CONFIG);
        }

        /**
         * Copy constructor.
         * 
         * @param copy
         *             the config to copy.
         */
        public Config(Config copy) {
            if (copy != null) {
                this.fitHeight = copy.fitHeight;
                this.fitWidth = copy.fitWidth;
                this.usePercentForHeight = copy.usePercentForHeight;
                this.usePercentForWidth = copy.usePercentForWidth;
                this.baseline = new CardFitLayoutData (copy.baseline);
            }
        }

        /**
         * Configure with fit guidance.
         * 
         * @param fitWidthAndHeight
         *                          {@code true} if to fit both dimensions.
         */
        public Config(boolean fitWidthAndHeight) {
            this.fitWidth = this.fitHeight = fitWidthAndHeight;
        }

        /**
         * Configure with fit guidance.
         * 
         * @param fitWidth
         *                  {@code true} if to fit width.
         * @param fitHeight
         *                  {@code true} if to fit height.
         */
        public Config(boolean fitWidth, boolean fitHeight) {
            this.fitWidth = fitWidth;
            this.fitHeight = fitHeight;
        }

        /**
         * Expand the contents of the child to fit the width of the containment region.
         * <p>
         * The default behaviour is to use absolute positioning of the child and fix the
         * left and right insets. If {@link #usePercentForWidth(boolean)} is set then a
         * percentage will be used. For the former if the parent has no positioning the
         * positioning will be set to relative.
         * 
         * @param fitWidth
         *                 {@code true} if to fit on width.
         * @return this configuration instance.
         */
        public Config fitWidth(boolean fitWidth) {
            this.fitWidth = fitWidth;
            return this;
        }

        /**
         * See {@link #fitWidth(boolean)} but applies to height.
         */
        public Config fitHeight(boolean fitHeight) {
            this.fitHeight = fitHeight;
            return this;
        }

        /**
         * See {@link #fitWidth(boolean)} for details of how this is used.
         */
        public Config usePercentForWidth(boolean usePercentForWidth) {
            this.usePercentForWidth = usePercentForWidth;
            return this;
        }

        /**
         * See {@link #fitHeight(boolean)} for details of how this is used.
         */
        public Config usePercentForHeight(boolean usePercentForHeight) {
            this.usePercentForHeight = usePercentForHeight;
            return this;
        }

        /**
         * Determines if a transition effect should be used when activating components.
         * <p>
         * See also {@link CardFitLayoutData#effect(Effect)} which provides a
         * component-level override.
         * 
         * @param effect
         *               the transition effect to apply.
         * @return this configuration instance.
         */
        public Config effect(Effect effect) {
            if (effect != null)
                this.baseline.effect (effect);
            return this;
        }

        /**
         * Builds a layout from this configuration.
         * 
         * @return the layout instance.
         */
        public CardFitLayout build() {
            return new CardFitLayout (this);
        }
    }

    /**
     * Data used to modify an items position in the {@link VertLayout}.
     * 
     * @author Jeremy Buckley
     */
    public static class CardFitLayoutData extends LayoutData {

        /**
         * See {@link #effect(Effect)}.
         */
        private Effect effect = null;

        /**
         * Empty constructor.
         */
        public CardFitLayoutData() {
            // Nothing.
        }

        /**
         * Copy constructor.
         * 
         * @param copy
         *             the data to copy.
         */
        public CardFitLayoutData(CardFitLayoutData copy) {
            if (copy != null) {
                effect = copy.effect;
            }
        }

        /**
         * Getter for {@link #effect(Separator)}.
         */
        public Effect getEffect() {
            return effect;
        }

        /**
         * Assigns a separator.
         * 
         * @param effect
         *               the effect to apply.
         * @return this configuration instance.
         */
        public CardFitLayoutData effect(Effect effect) {
            this.effect = effect;
            return this;
        }

        /**
         * Composes this layout data with a set of overrides (generally provided by a
         * component). The result is a new layout data that combines the two.
         * 
         * @param override
         *                 the overriding data.
         * @return the composed data.
         */
        public CardFitLayoutData compose(CardFitLayoutData override) {
            if (override == null)
                return this;
            CardFitLayoutData resolved = new CardFitLayoutData (this);
            if (override.effect != null)
                resolved.effect = override.effect;
            return resolved;
        }

    }

    /**
     * The active item.
     */
    protected IComponent activeItem;

    /**
     * Configuration for the layout.
     */
    protected Config config;

    /**
     * Standard layout factory implementation for this layout.
     */
    public static final ILayoutFactory FACTORY = new ILayoutFactory () {

        @Override
        public ILayout create() {
            return new CardFitLayout ();
        }

    };

    /**
     * Default constructor that fits against all dimensions.
     */
    public CardFitLayout() {
        super ();
        this.config = new Config ();
    }

    /**
     * Construct with indications of which dimensions to enforce.
     * 
     * @param fitWidth
     *                  {@code true} if the width of the child should be set to
     *                  match the width of the container.
     * @param fitHeight
     *                  {@code true} if the height of the child should be set to
     *                  match the height of the container.
     */
    public CardFitLayout(Config config) {
        super ();
        this.config = config;
    }

    /**
     * Returns the active component.
     * 
     * @return The currently active component.
     */
    public IComponent getActiveItem() {
        return activeItem;
    }

    /**
     * Sets the active (visible) item in the layout.
     * <p>
     * If the component implements {@link IActivateAware} then
     * {@link IActivateAware#onActivated()} will be invoked.
     * <p>
     * This return a promise which reflects that the activation can be asynchronous.
     * This will typically occur when code splitting has been employed and the
     * JavaScript for the component needs to be downloaded prior to instantiated and
     * activation. The feature is used during navigation (see
     * {@link NavigationHandler}) to properly activate nested items.
     * 
     * @param component
     *                  the active component.
     * @see com.effacy.jui.core.client.component.layout.ILayout#activate(com.effacy.jui.core.client.component.IComponent)
     */
    public Promise<ActivateOutcome> activate(IComponent component) {
        if (layoutTarget() == null)
            return Promise.create (ActivateOutcome.NOT_PRESENT);
        if (!layoutTarget().getItems ().contains (component))
            return Promise.create (ActivateOutcome.NOT_PRESENT);
        CardFitLayoutData layoutData = config.baseline.compose (((CardFitLayoutData) getLayoutData (component)));
        this.getLayoutData (component);
        if (activeItem == component) {
            // We assume that the activation is for a purpose so re-activate the
            // item if it is activation aware.
            if (layoutData.effect == Effect.FADE_IN_ALL)
                fade (layoutTarget().getLayoutTarget ());
            if (activeItem instanceof IActivateAware)
                ((IActivateAware) activeItem).onReActivated ();
            return Promise.create (ActivateOutcome.ALREADY_ACTIVATED);
        }
        if (activeItem != null)
            activeItem.hide ();
        activeItem = component;
        if (activeItem instanceof IActivateAware)
            ((IActivateAware) activeItem).onActivated ();
        if (activeItem != null) {
            if ((layoutData.effect == Effect.FADE_IN) || (layoutData.effect == Effect.FADE_IN_ALL))
                fade (layoutTarget().getLayoutTarget ());
            activeItem.show ();
            if ((layoutTarget() != null) && layoutTarget().isRendered ()) {
                markAsDirty ();
                // In some cases a component that has not yet rendered, rendering in this
                // execution thread can cause problems (i.e. if using async loading). It is
                // safer to take it out of the thread (which can be achieved using a timer). The
                // promise is used to allow carry-on processing knowing that the component has
                // rendered.
                return performLayout (!component.isRendered ());
            }
        }
        return Promise.create (ActivateOutcome.ACTIVATED);
    }

    /**
     * Applies a fade effect to the passed element.
     * 
     * @param el the element to apply fade to.
     */
    protected void fade(Element el) {
        if (el == null)
            return;
        el.classList.add (CardFitLayout.LocalCSS.instance ().fade ());
        TimerSupport.timer (() -> el.classList.remove (CardFitLayout.LocalCSS.instance ().fade ()), 500);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.StyleLayout#onLayout(com.effacy.gwt.dom.client.Elem)
     */
    @Override
    protected void onLayout(Element target) {
        if (getNumberItems () == 0)
            return;
        target.classList.add (LocalCSS.instance ().layout ());
        activeItem = (activeItem != null) ? activeItem : getItems ().get (0);
        super.onLayout (target);

        // Ensure that the target element has a valid position where we are
        // using absolute position for any reason.
        if ((config.fitWidth || config.fitHeight) && !(config.usePercentForWidth && config.usePercentForHeight)) {
            if (StringSupport.empty (((HTMLElement) target).style.position))
                CSS.POSITION.with (Position.RELATIVE).apply (target);
        }

        // Determine the size by the size imposed by style elements.
        sizeActiveItem ();
    }

    /**
     * Actually apply the size to the active item.
     */
    protected void sizeActiveItem() {
        if ((activeItem != null) && (activeItem.getRoot () != null)) {
            if (config.fitWidth || config.fitHeight) {
                JQueryElement el = JQuery.$ (activeItem.getRoot ());
                if (config.fitHeight) {
                    if (config.usePercentForHeight) {
                        CSS.apply (el, CSS.HEIGHT.with (Length.pct (100.0)));
                    } else {
                        CSS.POSITION.with (Position.ABSOLUTE).apply (el);
                        CSS.apply (el, CSS.TOP.with (Length.px (0)), CSS.BOTTOM.with (Length.px (0)));
                    }
                }
                if (config.fitWidth) {
                    if (config.usePercentForWidth) {
                        CSS.apply (el, CSS.WIDTH.with (Length.pct (100.0)));
                    } else {
                        CSS.POSITION.with (Position.ABSOLUTE).apply (el);
                        CSS.apply (el, CSS.LEFT.with (Length.px (0)), CSS.RIGHT.with (Length.px (0)));
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.Layout#onComponentActivate(com.effacy.jui.core.client.component.IComponent)
     */
    @Override
    protected void onComponentActivate(IComponent component) {
        activate (component);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.layout.Layout#renderComponent(com.effacy.jui.core.client.component.IComponent,
     *      int, elemental2.dom.Element, int)
     */
    @Override
    protected void renderComponent(IComponent component, int index, Element target, int size) {
        if (activeItem == component)
            super.renderComponent (component, index, target, size);
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
         * Align to the bottom.
         */
        public String fade();
    }

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/core/client/component/layout/CardFitLayout.css", 
        "com/effacy/jui/core/client/component/layout/CardFitLayout_Override.css"
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
