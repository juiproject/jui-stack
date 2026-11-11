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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;
import jsinterop.base.Js;

/**
 * A button with a label, an optional icon and a handler (with an asynchronous
 * completion callback).
 * <p>
 * A button can also carry a list of options (see
 * {@link Config#option(String, String, String, Invoker)}), shown in a menu
 * beneath it. Either the whole button opens the menu (a menu button) or, with
 * {@link Config#split()}, the button keeps its own action and a chevron beside
 * it opens the menu (a split button). Without options a button is drawn and
 * behaves as it always has.
 *
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
         * An option offered by the button (see
         * {@link Config#option(String, String, String, Invoker)} and
         * {@link Config#iconOption(String, String, String, Invoker)}).
         */
        public static class Option {

            private String label;

            private String text;

            private String code;

            private String icon;

            private Invoker handler;

            protected Option(String label, String text, String code, String icon, Invoker handler) {
                this.label = label;
                this.text = text;
                this.code = code;
                this.icon = icon;
                this.handler = handler;
            }

            /**
             * The name of the option.
             */
            public String getLabel() {
                return label;
            }

            /**
             * A line saying what the option is (may be {@code null}).
             */
            public String getText() {
                return text;
            }

            /**
             * A short tag shown before the label (may be {@code null}).
             */
            public String getCode() {
                return code;
            }

            /**
             * An icon shown before the label in place of a code (may be {@code null}).
             */
            public String getIcon() {
                return icon;
            }

            /**
             * Invoked when the option is chosen (after the menu has closed).
             */
            public Invoker getHandler() {
                return handler;
            }
        }

        /**
         * See {@link #option(String, String, String, Invoker)}.
         */
        private List<Option> options = new ArrayList<> ();

        /**
         * See {@link #split()}.
         */
        private boolean split;

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
         * Adds an option to the button. Options show in a menu beneath the button,
         * in the order they are added.
         * <p>
         * Without {@link #split()} the whole button opens the menu (with a chevron
         * after its label) and the button's own handler is not used. With
         * {@link #split()} the button keeps its handler and a chevron beside it opens
         * the menu.
         *
         * @param label
         *                the name of the option.
         * @param text
         *                a line saying what the option is (may be {@code null}).
         * @param code
         *                a short tag shown before the label, such as {@code REQ}
         *                (may be {@code null}).
         * @param handler
         *                invoked when the option is chosen (once the menu has
         *                closed).
         * @return this configuration instance.
         */
        public Button.Config option(String label, String text, String code, Invoker handler) {
            options.add (new Option (label, text, code, null, handler));
            return this;
        }

        /**
         * As {@link #option(String, String, String, Invoker)} but with an icon in
         * place of the code.
         *
         * @param label
         *                the name of the option.
         * @param text
         *                a line saying what the option is (may be {@code null}).
         * @param icon
         *                the icon CSS (see {@link FontAwesome}; may be
         *                {@code null}).
         * @param handler
         *                invoked when the option is chosen (once the menu has
         *                closed).
         * @return this configuration instance.
         */
        public Button.Config iconOption(String label, String text, String icon, Invoker handler) {
            options.add (new Option (label, text, null, icon, handler));
            return this;
        }

        /**
         * When the button has options (see
         * {@link #option(String, String, String, Invoker)}) this makes it a split
         * button: the button's own handler is its action and a chevron, divided from
         * it by a hairline, opens the options.
         *
         * @return this configuration instance.
         */
        public Button.Config split() {
            this.split = true;
            return this;
        }

        /**
         * The options added to the button (in the order added).
         */
        public List<Option> getOptions() {
            return options;
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
        if (isRendered () && (toggleEl != null))
            toggleEl.setAttribute ("aria-label", toggleLabel (label));
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
     * The chevron of a split button (that opens the options).
     */
    protected Element toggleEl;

    /**
     * Receives the menu of options while it is open.
     */
    protected Element holderEl;

    /**
     * Determines if the button has options.
     */
    protected boolean hasOptions() {
        return !config ().options.isEmpty ();
    }

    /**
     * Determines if the button has options and the whole button opens them (rather
     * than a chevron beside it, see {@link Config#split()}).
     */
    protected boolean opensOnAction() {
        return hasOptions () && !config ().split;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.RendererComponent#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config config) {
        boolean options = !config.options.isEmpty ();
        boolean menu = options && !config.split;
        return Wrap.$ (el).$ (root -> {
            if (config.width != null)
                root.css (CSS.WIDTH, config.width);
            Div.$ (root).$ (outer -> {
                outer.style (styles ().outer ());
                if (options)
                    outer.style (optionStyles ().parts ());
                A.$ (outer).$ (a -> {
                    // This is needed to ensure the anchor can receive focus.
                    a.setAttribute("href", "javascript:;");
                    a.testId (buildTestId ("action")).testRef ("action");
                    a.by ("action")
                        .on (e -> {
                            // A menu button opens its options in place of its action.
                            if (opensOnAction ())
                                toggleOptions (e);
                            else
                                _onClick ();
                        }, UIEventType.ONCLICK)
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
                    if (menu) {
                        a.attr ("aria-haspopup", "menu").attr ("aria-expanded", "false");
                        a.on (e -> onTriggerKeyDown (e), UIEventType.ONKEYDOWN);
                    }
                    if (options && config.split)
                        a.style (optionStyles ().part ());
                    I.$(a).style (FontAwesome.spinner (FontAwesome.Option.SPIN));
                    // The icon on the right reverses the order (so the chevron goes first).
                    if (menu && config.iconOnRight)
                        Em.$ (a).style (optionStyles ().chevron (), FontAwesome.chevronDown ());
                    if (!StringSupport.empty (config.icon))
                        Em.$ (a).style (config.icon);
                    Span.$ (a).by ("label").testRef ("label").text (config.label);
                    if (menu && !config.iconOnRight)
                        Em.$ (a).style (optionStyles ().chevron (), FontAwesome.chevronDown ());
                });
                if (options && config.split) {
                    A.$ (outer).$ (a -> {
                        a.setAttribute ("href", "javascript:;");
                        a.style (optionStyles ().part (), optionStyles ().toggle ());
                        a.testId (buildTestId ("toggle")).testRef ("toggle");
                        a.attr ("aria-haspopup", "menu").attr ("aria-expanded", "false");
                        a.ariaLabel (toggleLabel (config.label));
                        // Not a focus element of the component (that would move focus to the
                        // action), so it shows its own focus (see the options' styles).
                        a.by ("toggle")
                            .on (e -> toggleOptions (e), UIEventType.ONCLICK)
                            .on (e -> onTriggerKeyDown (e), UIEventType.ONKEYDOWN);
                        Em.$ (a).style (FontAwesome.chevronDown ());
                    });
                }
            });
            if (options)
                Div.$ (root).style (optionStyles ().holder ()).by ("holder");
        }).build (tree -> {
            actionEl = manageFocusEl (tree.first ("action"));
            labelEl = tree.first ("label");
            toggleEl = tree.first ("toggle");
            holderEl = tree.first ("holder");
        });
    }

    /**
     * The accessible label of the chevron of a split button.
     */
    protected String toggleLabel(String label) {
        if (StringSupport.empty (label))
            return "More options";
        return "More options for " + label;
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
        if (hasOptions ()) {
            getRoot ().classList.add (optionStyles ().opens ());
            if (config ().split)
                getRoot ().classList.add (optionStyles ().split ());
            updateInactive ();
        }
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
     * {@inheritDoc}
     * <p>
     * Closes the options (if open).
     *
     * @see com.effacy.jui.core.client.component.Component#onDisable()
     */
    @Override
    protected void onDisable() {
        super.onDisable ();
        closeOptions ();
        updateInactive ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#onEnable()
     */
    @Override
    protected void onEnable() {
        super.onEnable ();
        updateInactive ();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Closes the options (if open).
     *
     * @see com.effacy.jui.core.client.component.Component#onDetach()
     */
    @Override
    protected void onDetach() {
        closeOptions ();
        super.onDetach ();
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
        updateInactive ();
    }

    /**
     * For a button with options, marks the root as inactive while the button is
     * disabled or waiting (so the parts of a split button do not respond to the
     * pointer).
     */
    protected void updateInactive() {
        if (!isRendered () || !hasOptions ())
            return;
        if (isDisabled () || getRoot ().classList.contains (styles ().waiting ()))
            getRoot ().classList.add (optionStyles ().inactive ());
        else
            getRoot ().classList.remove (optionStyles ().inactive ());
    }

    /************************************************************************
     * Options
     ************************************************************************/

    /**
     * Gap (in pixels) between the button and its menu.
     */
    private static final int MENU_GAP = 4;

    /**
     * The open menu (or {@code null} when closed).
     */
    private Element menuEl;

    /**
     * The backdrop beneath the open menu (that closes it when clicked on).
     */
    private Element backdropEl;

    /**
     * The rows of the open menu (one per option).
     */
    private List<Element> optionEls = new ArrayList<> ();

    /**
     * The index of the active (highlighted) option, or -1 if none.
     */
    private int active = -1;

    /**
     * Listens to the keyboard (on the document in the capture phase) while the
     * menu is open.
     */
    private EventListener keyListener;

    /**
     * Repositions the menu on a scroll or resize while it is open.
     */
    private EventListener repositionListener;

    /**
     * Determines if the menu of options is open.
     *
     * @return {@code true} if it is.
     */
    public boolean isOptionsOpen() {
        return (menuEl != null);
    }

    /**
     * Toggles the options from a click on the trigger.
     *
     * @param e
     *          the click event.
     */
    protected void toggleOptions(UIEvent e) {
        if (isOptionsOpen ()) {
            closeOptions ();
            return;
        }
        // A click from the keyboard (Enter) has no click count, so it activates the
        // first option (as Down does).
        elemental2.dom.UIEvent evt = Js.uncheckedCast (e.getEvent ());
        openOptions ((evt != null) && (evt.detail == 0));
    }

    /**
     * Handles a key-down on the trigger while the menu is closed: Down opens the
     * menu with the first option active.
     *
     * @param e
     *          the key event.
     */
    protected void onTriggerKeyDown(UIEvent e) {
        if (isOptionsOpen () || !e.isDownKey ())
            return;
        e.stopEvent ();
        openOptions (true);
    }

    /**
     * Opens the menu of options (if the button has options, is enabled and is not
     * processing an action).
     *
     * @param activateFirst
     *                      {@code true} to make the first option active.
     */
    protected void openOptions(boolean activateFirst) {
        if (!isRendered () || (holderEl == null) || isOptionsOpen () || isDisabled () || processing)
            return;
        List<Config.Option> options = config ().options;
        Wrap.buildInto (holderEl, holder -> {
            Div.$ (holder).style (optionStyles ().backdrop ()).by ("backdrop").testRef ("backdrop").use (n -> {
                ((Element) n).addEventListener ("click", evt -> {
                    evt.stopPropagation ();
                    closeOptions ();
                });
            });
            Div.$ (holder).style (optionStyles ().menu ()).by ("menu").$ (menu -> {
                menu.attr ("role", "menu");
                menu.testId (buildTestId ("menu")).testRef ("menu");
                if (!StringSupport.empty (config ().label))
                    menu.ariaLabel (config ().label);
                for (int i = 0; i < options.size (); i++) {
                    Config.Option option = options.get (i);
                    int idx = i;
                    Div.$ (menu).style (optionStyles ().option ()).by ("option").$ (row -> {
                        row.attr ("role", "menuitem").attr ("tabindex", "-1");
                        row.testId (buildTestId ("option-" + (StringSupport.empty (option.code) ? option.label : option.code)));
                        row.use (n -> {
                            ((Element) n).addEventListener ("click", evt -> {
                                evt.stopPropagation ();
                                choose (idx);
                            });
                            ((Element) n).addEventListener ("mouseenter", evt -> activate (idx));
                        });
                        if (!StringSupport.empty (option.code))
                            Span.$ (row).style (optionStyles ().code ()).text (option.code);
                        else if (!StringSupport.empty (option.icon))
                            Em.$ (row).style (optionStyles ().icon (), option.icon);
                        Div.$ (row).style (optionStyles ().body ()).$ (body -> {
                            Span.$ (body).style (optionStyles ().label ()).text (option.label);
                            if (!StringSupport.empty (option.text))
                                Span.$ (body).style (optionStyles ().text ()).text (option.text);
                        });
                    });
                }
            });
        }, tree -> {
            backdropEl = tree.first ("backdrop");
            menuEl = tree.first ("menu");
            optionEls = tree.all ("option");
        });
        getRoot ().classList.add (optionStyles ().open ());
        expanded (true);
        active = -1;
        if (activateFirst)
            activate (0);
        positionOptions ();

        keyListener = evt -> onOptionsKeyDown (Js.uncheckedCast (evt));
        DomGlobal.document.addEventListener ("keydown", keyListener, true);
        repositionListener = evt -> positionOptions ();
        DomGlobal.window.addEventListener ("scroll", repositionListener, true);
        DomGlobal.window.addEventListener ("resize", repositionListener);
    }

    /**
     * Closes the menu of options (if open).
     */
    protected void closeOptions() {
        if (keyListener != null) {
            DomGlobal.document.removeEventListener ("keydown", keyListener, true);
            keyListener = null;
        }
        if (repositionListener != null) {
            DomGlobal.window.removeEventListener ("scroll", repositionListener, true);
            DomGlobal.window.removeEventListener ("resize", repositionListener);
            repositionListener = null;
        }
        if (!isOptionsOpen ())
            return;
        menuEl = null;
        backdropEl = null;
        optionEls = new ArrayList<> ();
        active = -1;
        if (holderEl != null)
            holderEl.innerHTML = "";
        getRoot ().classList.remove (optionStyles ().open ());
        expanded (false);
    }

    /**
     * Chooses an option: closes the menu, then runs the option's handler.
     *
     * @param idx
     *            the index of the option.
     */
    protected void choose(int idx) {
        if ((idx < 0) || (idx >= config ().options.size ()))
            return;
        Config.Option option = config ().options.get (idx);
        closeOptions ();
        if (option.handler != null)
            option.handler.invoke ();
    }

    /**
     * Makes an option the active (highlighted) one.
     *
     * @param idx
     *            the index of the option (-1 for none).
     */
    protected void activate(int idx) {
        for (int i = 0; i < optionEls.size (); i++) {
            if (i == idx)
                optionEls.get (i).classList.add (optionStyles ().active ());
            else
                optionEls.get (i).classList.remove (optionStyles ().active ());
        }
        active = ((idx >= 0) && (idx < optionEls.size ())) ? idx : -1;
    }

    /**
     * Handles a key-down (anywhere in the document) while the menu is open. Down
     * and Up move through the options, Enter chooses the active one and Escape or
     * Tab closes the menu.
     *
     * @param evt
     *            the key event.
     */
    protected void onOptionsKeyDown(KeyboardEvent evt) {
        int count = optionEls.size ();
        if ("ArrowDown".equals (evt.key)) {
            evt.preventDefault ();
            evt.stopPropagation ();
            activate ((active < 0) ? 0 : ((active + 1) % count));
            return;
        }
        if ("ArrowUp".equals (evt.key)) {
            evt.preventDefault ();
            evt.stopPropagation ();
            activate ((active <= 0) ? (count - 1) : (active - 1));
            return;
        }
        if ("Enter".equals (evt.key)) {
            if (active < 0)
                return;
            // Prevent the default so the focused trigger is not clicked (which would
            // open the menu again).
            evt.preventDefault ();
            evt.stopPropagation ();
            choose (active);
            return;
        }
        if ("Escape".equals (evt.key)) {
            evt.preventDefault ();
            evt.stopPropagation ();
            closeOptions ();
            return;
        }
        if ("Tab".equals (evt.key))
            closeOptions ();
    }

    /**
     * Reflects the state of the menu in the {@code aria-expanded} attribute of the
     * trigger.
     */
    private void expanded(boolean expanded) {
        Element trigger = config ().split ? toggleEl : actionEl;
        if (trigger != null)
            trigger.setAttribute ("aria-expanded", expanded ? "true" : "false");
    }

    /**
     * Positions the open menu beneath the button with its right edge on the
     * button's, against the window (so a container that clips its overflow does
     * not clip the menu), with the backdrop covering the window.
     */
    protected void positionOptions() {
        if (!isOptionsOpen ())
            return;
        DOMRect btn = getRoot ().getBoundingClientRect ();
        HTMLElement menu = Js.uncheckedCast (menuEl);
        HTMLElement backdrop = Js.uncheckedCast (backdropEl);

        backdrop.style.setProperty ("width", DomGlobal.window.innerWidth + "px");
        backdrop.style.setProperty ("height", DomGlobal.window.innerHeight + "px");
        place (backdrop, 0, 0);

        menu.style.setProperty ("min-width", btn.width + "px");
        double left = btn.right - menu.getBoundingClientRect ().width;
        place (menu, Math.max (0, left), btn.bottom + MENU_GAP);
    }

    /**
     * Places a fixed element at the given window coordinates. A transformed
     * ancestor places fixed elements against itself rather than the window, so the
     * position is corrected by where the element actually lands.
     */
    private void place(HTMLElement el, double left, double top) {
        el.style.setProperty ("left", left + "px");
        el.style.setProperty ("top", top + "px");
        DOMRect actual = el.getBoundingClientRect ();
        double dx = actual.left - left;
        double dy = actual.top - top;
        if ((Math.abs (dx) > 0.5) || (Math.abs (dy) > 0.5)) {
            el.style.setProperty ("left", (left - dx) + "px");
            el.style.setProperty ("top", (top - dy) + "px");
        }
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

    /**
     * Styles for the options (see {@link IOptionsCSS}).
     */
    protected IOptionsCSS optionStyles() {
        return OptionsCSS.instance ();
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
     * Styles for a button with options. These are a stylesheet of their own (so a
     * configuration's own {@link Config#styles(ILocalCSS)} keeps them) and sit on
     * the button's {@code --cpt-btn-*} tokens (so they take the variants).
     */
    public static interface IOptionsCSS extends CssDeclaration {

        /**
         * Marks the root of a button with options.
         */
        public String opens();

        /**
         * Marks the root of a split button.
         */
        public String split();

        /**
         * Marks the root while the menu is open.
         */
        public String open();

        /**
         * Marks the root while the button is disabled or waiting.
         */
        public String inactive();

        /**
         * The structural outer of a button with options.
         */
        public String parts();

        /**
         * Each of the two parts (action and chevron) of a split button.
         */
        public String part();

        /**
         * The chevron of a split button.
         */
        public String toggle();

        /**
         * The chevron after the label of a menu button.
         */
        public String chevron();

        /**
         * Receives the menu while it is open.
         */
        public String holder();

        /**
         * Covers the window beneath the menu (and closes it when clicked on).
         */
        public String backdrop();

        /**
         * The menu.
         */
        public String menu();

        /**
         * An option in the menu.
         */
        public String option();

        /**
         * The active (highlighted) option.
         */
        public String active();

        /**
         * The code of an option.
         */
        public String code();

        /**
         * The icon of an option.
         */
        public String icon();

        /**
         * Holds the label and line of an option.
         */
        public String body();

        /**
         * The label of an option.
         */
        public String label();

        /**
         * The line of an option.
         */
        public String text();
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

    /**
     * Options CSS (standard pattern).
     */
    @CssResource({
        "com/effacy/jui/ui/client/button/ButtonOptions.css",
        "com/effacy/jui/ui/client/button/ButtonOptions_Override.css"
    })
    public static abstract class OptionsCSS implements IOptionsCSS {

        private static OptionsCSS STYLES;

        public static IOptionsCSS instance() {
            if (STYLES == null) {
                STYLES = (OptionsCSS) GWT.create (OptionsCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
