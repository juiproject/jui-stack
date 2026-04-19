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
package com.effacy.jui.ui.client.button;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * @author Jeremy Buckley
 */
public class Button extends Component<Button.Config> implements IButton {

    /**
     * Component configuration.
     */
    public static class Config extends Component.Config {

        /**
         * Variant for the component.
         */
        @FunctionalInterface
        public interface Variant {

            /**
             * Applies variant configuration.
             */
            void configure(Config cfg);

            /**
             * Standard visual style (box-like with solid color body).
             */
            public static final Variant STANDARD = config -> {
                config.styles (StandardCSS.instance ());
            };

            /**
             * Outlined visual style (box-like with inverted body color and text).
             */
            public static final Variant OUTLINED = config -> {
                config.css("""
                    --cpt-btn-bg: var(--jui-comp-button-outline-surface);
                    --cpt-btn-bg-hover: var(--jui-comp-button-outline-surface-hover);
                    --cpt-btn-text: var(--jui-comp-button-outline-text);
                    --cpt-btn-text-hover: var(--jui-comp-button-outline-text);
                    --cpt-btn-border: var(--jui-comp-button-outline-border);
                    --cpt-btn-disabled-bg: transparent;
                """);
            };

            /**
             * Link visual style.
             */
            public static final Variant LINK = config -> {
                config.css("""
                    --cpt-btn-bg: transparent;
                    --cpt-btn-bg-hover: transparent;
                    --cpt-btn-text: var(--jui-comp-button-link-text);
                    --cpt-btn-text-hover: var(--jui-comp-button-link-text-hover);
                    --cpt-btn-border: transparent;
                    --cpt-btn-border-width: 0;
                    --cpt-btn-padding-block: 0;
                    --cpt-btn-text-lineheight: 1.2;
                    --cpt-btn-margin: 0;
                    --cpt-btn-waiting-bg: transparent;
                    --cpt-btn-disabled-bg: transparent;
                    --cpt-btn-disabled-border: transparent;
                    --cpt-btn-disabled-text: var(--jui-state-disabled);
                    --cpt-btn-hover-text-decoration: underline;
                """);
            };

            /**
             * Success color overlay.
             */
            public static final Variant SUCCESS = config -> {
                config.css("""
                    --cpt-btn-bg: var(--jui-comp-button-success-surface);
                    --cpt-btn-bg-hover: var(--jui-comp-button-success-surface-hover);
                    --cpt-btn-border: var(--jui-comp-button-success-border);
                """);
            };

            /**
             * Warning color overlay.
             */
            public static final Variant WARNING = config -> {
                config.css("""
                    --cpt-btn-bg: var(--jui-comp-button-warning-surface);
                    --cpt-btn-bg-hover: var(--jui-comp-button-warning-surface-hover);
                    --cpt-btn-border: var(--jui-comp-button-warning-border);
                """);
            };

            /**
             * Danger color overlay.
             */
            public static final Variant DANGER = config -> {
                config.css("""
                    --cpt-btn-bg: var(--jui-comp-button-danger-surface);
                    --cpt-btn-bg-hover: var(--jui-comp-button-danger-surface-hover);
                    --cpt-btn-border: var(--jui-comp-button-danger-border);
                """);
            };
        }

        /**
         * Backward compatibility layer around {@link Variant}.
         */
        @Deprecated
        public enum Style implements Variant {
            @Deprecated NORMAL,
            @Deprecated NORMAL_SUCCESS,
            @Deprecated NORMAL_WARNING,
            @Deprecated NORMAL_DANGER,
            @Deprecated OUTLINED,
            @Deprecated LINK;

            @Deprecated
            @Override
            public void configure(Config cfg) {
                switch (this) {
                    case NORMAL:
                        Variant.STANDARD.configure (cfg);
                        break;
                    case NORMAL_SUCCESS:
                        Variant.STANDARD.configure (cfg);
                        Variant.SUCCESS.configure (cfg);
                        break;
                    case NORMAL_WARNING:
                        Variant.STANDARD.configure (cfg);
                        Variant.WARNING.configure (cfg);
                        break;
                    case NORMAL_DANGER:
                        Variant.STANDARD.configure (cfg);
                        Variant.DANGER.configure (cfg);
                        break;
                    case OUTLINED:
                        Variant.OUTLINED.configure (cfg);
                        break;
                    case LINK:
                        Variant.LINK.configure (cfg);
                        break;
                }
            }
        }

        /**
         * Click behaviour of the button.
         */
        public enum Behaviour {
            /**
             * Normal with no state change.
             */
            NORMAL,

            /**
             * Displays a spinner which clicked.
             */
            WAIT,

            /**
             * Falls to a disable state.
             */
            DISABLE;
        }

        /**
         * See {@link #handler(IButtonHandler)}.
         */
        private IButtonHandler handler;

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #label(String)}.
         */
        private String label;

        /**
         * See {@link #icon(String)}.
         */
        private String icon;

        /**
         * See {@link #iconOnRight(boolean)}.
         */
        private boolean iconOnRight;

        /**
         * See {@link #styles(ILocalCSS)}.
         */
        private ILocalCSS styles = StandardCSS.instance ();

        /**
         * See {@link #behaviour(Behaviour)}.
         */
        private Behaviour behaviour = Behaviour.WAIT;

        /**
         * Construct with the default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a specific variant.
         * 
         * @param variant
         *                the variant.
         */
        public Config(Variant variant) {
            super ();
            variant (variant);
        }

        /**
         * Construct with a specific style.
         * 
         * @param style
         *              the style.
         */
        @Deprecated
        public Config(Style style) {
            super ();
            style (style);
        }

        /**
         * Assigns a presentation variant.
         * 
         * @param variant
         *                the variant (default is {@link Variant#STANDARD}).
         * @return this configuration instance.
         */
        public Button.Config variant(Variant variant) {
            if (variant != null)
                variant.configure (this);
            return this;
        }

        /**
         * Assigns a set of presentation variants.
         * 
         * @param variants
         *                 the variants to apply in order.
         * @return this configuration instance.
         */
        public Button.Config variant(Variant... variants) {
            if (variants != null) {
                for (Variant variant : variants)
                    variant (variant);
            }
            return this;
        }

        /**
         * Assigns a presentation style.
         * 
         * @param style
         *              the style (default is {@link Style#NORMAL}).
         * @return this configuration instance.
         */
        @Deprecated
        public Button.Config style(Style style) {
            if (style != null)
                style.configure (this);
            return this;
        }

        /**
         * Registers a handler when click on.
         * 
         * @param handler
         *                the handler.
         * @return this configuration instance.
         */
        public Button.Config handler(IButtonHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Registers a handler when clicked on.
         * <p>
         * In this case the button state returns to its activate state once the handler
         * returns (as opposed to {@link #handler(IButtonHandler)} where a passed
         * callback needs to be invoked, which is useful for dialogs).
         * 
         * @param handler
         *                the handler.
         * @return this configuration instance.
         */
        public Button.Config handler(Invoker handler) {
            return handler (cb -> {
                handler.invoke ();
                cb.complete ();
            });
        }

        /**
         * Assigns the display label that is presented against the button.
         * 
         * @param label
         *              the label.
         * @return this configuration instance.
         */
        public Button.Config label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Assigns awidth for the button.
         * 
         * @param width
         *              the width.
         * @return this configuration instance.
         */
        public Button.Config width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Assigns an (optional) icon the is presented alongside the label.
         * 
         * @param icon
         *             the icon CSS (see {@link FontAwesome} for example).
         * @return this configuration instance.
         */
        public Button.Config icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * See {@link #iconOnRight(boolean)}. Short cut for
         * <code>iconOnRight(true)</code>.
         * 
         * @return this configuration instance.
         */
        public Button.Config iconOnRight() {
            return iconOnRight (true);
        }

        /**
         * See {@link #iconOnRight(boolean)}. Short cut for
         * <code>iconOnRight(false)</code>.
         * 
         * @return this configuration instance.
         */
        public Button.Config iconOnLeft() {
            return iconOnRight (false);
        }

        /**
         * The icon is displayed on the left side of the button by default; this can be
         * changed to display on the right.
         * 
         * @param iconOnRight
         *                    {@code true} to display on the right.
         * @return this configuration instance.
         */
        public Button.Config iconOnRight(boolean iconOnRight) {
            this.iconOnRight = iconOnRight;
            return this;
        }

        /**
         * Assigns the styles to use for the button.
         * 
         * @param styles
         *               the styles.
         * @return this configuration instance.
         */
        public Button.Config styles(ILocalCSS styles) {
            if (styles != null)
                this.styles = styles;
            return this;
        }

        /**
         * Getter for {@link #styles(ILocalCSS)}.
         */
        public ILocalCSS getStyles() {
            if (styles == null)
                styles = StandardCSS.instance ();
            return styles;
        }

        /**
         * Assigns a click behaviour.
         * 
         * @param behaviour
         *                  the behaviour (default is {@link Behaviour#NORMAL}).
         * @return this configuration instance.
         */
        public Button.Config behaviour(Behaviour behaviour) {
            if (behaviour != null)
                this.behaviour = behaviour;
            return this;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Config#testId(String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Button.Config testId(String testId) {
            return (Button.Config) super.testId(testId);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public Button build(LayoutData... data) {
            return (Button) build (new Button (this), data);
        }
    }

    /**
     * Flag indicating the button action is processing (awaiting return).
     */
    private boolean processing = false;

    /**
     * Used to flag when the button has been activated by a mouse click.
     * <p>
     * This is used to hide the focus highlight to behave more like the standard
     * button (i.e. so when clicked on it still gains focus but does not look like
     * it gains focus).
     */
    private boolean activatedByClick = false;

    /**
     * Construct instance.
     * 
     * @param config
     *               component configuration.
     */
    public Button(Button.Config config) {
        super (config);
    }

    /**
     * Determines if the button is in a processing state. This is awaiting
     * completion and will not be able to be invoked.
     * 
     * @return {@code true} if it is.
     */
    public boolean isProcessing() {
        return processing;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.button.IButton#click()
     */
    @Override
    public void click() {
        if (isRendered () && !processing && (actionEl != null))
            DomSupport.click (actionEl);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.button.IButton#updateLabel(java.lang.String)
     */
    @Override
    public void updateLabel(String label) {
        config ().label = label;
        if (isRendered () && (labelEl != null))
            DomSupport.innerText (labelEl, label);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.ui.client.button.IButton#setHandler(com.effacy.jui.ui.client.button.IButtonHandler)
     */
    @Override
    public void setHandler(IButtonHandler handler) {
        config ().handler = handler;
    }

    /**
     * Invoked to initiate a click action. This will call
     * {@link #onClick(IButtonActionCallback)} but will manage processing state.
     */
    protected void _onClick() {
        if (!isDisabled () && !processing) {
            processing = true;
            blur ();
            if (config ().behaviour == Button.Config.Behaviour.WAIT)
                waiting (true);
            else if (config().behaviour == Button.Config.Behaviour.DISABLE)
                disable ();
            onClick (() -> {
                processing = false;
                waiting (false);
                if (config().behaviour == Button.Config.Behaviour.DISABLE)
                    enable ();
                if (!isDisabled ())
                    focus ();
            });
        }
    }

    /**
     * Invoked when the button is clicked (programatically or via the UI). The
     * default behaviour is to pass through to any handler registered against the
     * button (directly or via configuration).
     * 
     * @param cb
     *           completion callback (needs to be invoked).
     */
    protected void onClick(IButtonActionCallback cb) {
        if (config ().handler != null)
            config ().handler.handleAction (cb); 
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IResetable#reset()
     */
    @Override
    public void reset() {
        blur ();
        if (processing) {
            processing = false;
            waiting (false);
        }
    }

    /************************************************************************
     * Rendering
     ************************************************************************/

    /**
     * The element containing the label.
     */
    protected Element labelEl;

    /**
     * The element containing the label. 
     */
    protected Element actionEl;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config config) {
        return Wrap.$ (el).$ (root -> {
            if (config.width != null)
                root.css (CSS.WIDTH, config.width);
            Div.$ (root).$ (outer -> {
                outer.style (styles ().outer ());
                A.$ (outer).$ (a -> {
                    // This is needed to ensure the anchor can receive focus.
                    a.setAttribute("href", "javascript:;");
                    a.testId (buildTestId ("action")).testRef ("action");
                    a.by ("action")
                        .on (e -> _onClick (), UIEventType.ONCLICK)
                        .on (e -> {
                            // On a key-press we clear any activated by click to ensure the focus
                            // visualisation appears.
                            activatedByClick = false;
                        }, UIEventType.ONKEYPRESS)
                        .on (e -> {
                            // On a mouse-down (i.e. a mouse click) we mark this as being by a click and
                            // this will prevent the focus visualisation from being applied (though the
                            // component will still have focus).
                            activatedByClick = true;
                        }, UIEventType.ONMOUSEDOWN);
                    I.$(a).style (FontAwesome.spinner (FontAwesome.Option.SPIN));
                    if (!StringSupport.empty (config.icon))
                        Em.$ (a).style (config.icon);
                    Span.$ (a).by ("label").testRef ("label").text (config.label);
                });
            });
        }).build (tree -> {
            actionEl = manageFocusEl (tree.first ("action"));
            labelEl = tree.first ("label");
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        if (config ().iconOnRight)
            getRoot ().classList.add (styles ().icon_right ());
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#applyFocusStyle()
     */
    @Override
    protected void applyFocusStyle() {
        // Here we use a flag to determine if the focus style should be applied. This
        // prevents the button from visually having focus when clicked on (this being
        // how standard buttons operate).
        if (!activatedByClick)
            super.applyFocusStyle ();
        activatedByClick = false;
    }

    /**
     * Mark in the waiting state.
     * 
     * @param waiting
     *                {@code true} if waiting.
     */
    public void waiting(boolean waiting) {
        if (waiting)
            getRoot ().classList.add (styles ().waiting ());
        else
            getRoot ().classList.remove (styles ().waiting ());
    }

    /********************************************************************
     * CSS
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    @Override
    protected ILocalCSS styles() {
        return config ().getStyles ();
    }

    public static interface ILocalCSS extends IComponentCSS {

        /**
         * To orient the icon to the right.
         */
        public String icon_right();

        /**
         * Structural outer (for border control).
         */
        public String outer();

        /**
         * To mark the button as being in the waiting state.
         */
        public String waiting();

    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({  
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/button/Button.css",
        "com/effacy/jui/ui/client/button/Button_Override.css"
    })
    public static abstract class StandardCSS implements ILocalCSS {

        private static StandardCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardCSS) GWT.create (StandardCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
