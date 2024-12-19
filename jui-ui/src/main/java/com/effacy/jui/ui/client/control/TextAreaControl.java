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
package com.effacy.jui.ui.client.control;

import java.util.function.Function;

import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.IFocusBlurListener;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Textarea;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTextAreaElement;

/**
 * A control akin to an HTML <code>TEXTAREA</code> for the entering of a body of
 * (unformatted) text.
 * 
 * @author Jeremy Buckley
 */
public class TextAreaControl extends Control<String, TextAreaControl.Config> {

    /************************************************************************
     * Configuration and construction
     ************************************************************************/

     /**
      * The default style to employ when one is not assign explicitly.
      */
     public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link TextAreaControl}.
     */
    public static class Config extends Control.Config<String, Config> {

        /********************************************************************
         * Styles for the tab set.
         ********************************************************************/

        /**
         * Style for the tab set (defines presentation configuration including CSS).
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
            public static Style create(final ILocalCSS styles) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                };
            }

            /**
             * Standard style.
             */
            public static final Style STANDARD = create(StandardLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #height(Length)}.
         */
        protected Length height;

        /**
         * See {@link #counter(boolean)}.
         */
        private boolean counter;

        /**
         * See {@link #max(int)}.
         */
        private int max = 0;

        /**
         * See {@link #rows(int)}.
         */
        private int rows = 0;

        /**
         * See {@link #cols(int)}.
         */
        private int cols = 0;

        /**
         * See {@link #resizable(boolean)}.
         */
        private boolean resizable = false;

        /**
         * See {@link #nowrap(boolean)}.
         */
        private boolean nowrap = false;

        /**
         * See {@link #placeholder(String)}.
         */
        private String placeholder;

        /**
         * See {@link #expandOnFocus(Length)}.
         */
        private Length expandOnFocus;

        /**
         * See {@link #expandOnFocus(Length,boolean)}.
         */
        private boolean expandOnFocusRetain;

        /**
         * See {@link #keyPressHandler(Function)}.
         */
        private Function<String, Boolean> keyPressHandler;

        /**
         * Construct with a default style.
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a style.
         * 
         * @param style
         *              the style.
         */
        public Config(Style style) {
            style (style);
        }

        /**
         * Assigns a different style.
         * 
         * @param style
         *              the style.
         * @return this configuration.
         */
        public Config style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Assigns a height to the control text area.
         * 
         * @param height
         *            the height to apply.
         * @return this configuration instance.
         */
        public Config height(Length height) {
            this.height = height;
            return this;
        }

        /**
         * Convenience to call {@link #counter(boolean)} with {@code true}.
         */
        public Config counter() {
            return counter(true);
        }

        /**
         * Determines if a character count indicator should be displayed.
         * <p>
         * If it is and there is a {@link #max(int)} then the maximum will also be
         * displayed.
         * 
         * @param counter
         *                {@code true} if to display a character counter.
         * @return this configuration instance.
         */
        public Config counter(boolean counter) {
            this.counter = counter;
            return this;
        }

        /**
         * Assigns the maximum number of characters the field can permit.
         * 
         * @param max
         *            the maximum (if 0 or less then no restriction is applied).
         * @return this configuration instance.
         */
        public Config max(int max) {
            this.max = max;
            return this;
        }

        /**
         * The number of rows to enforce.
         * 
         * @param rows
         *             the number.
         * @return this configuration instance.
         */
        public Config rows(int rows) {
            this.rows = rows;
            return this;
        }

        /**
         * The number of columns to enforce.
         * 
         * @param cols
         *             the number.
         * @return this configuration instance.
         */
        public Config cols(int cols) {
            this.cols = cols;
            return this;
        }

        /**
         * Convenience to call {@link #resizable(boolean)} with {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config resizable() {
            return resizable (true);
        }

        /**
         * Determines if the text area for entry can be resized by the user.
         * 
         * @param resizable
         *                  {@code true} if so (default is {@code false}).
         * @return this configuration instance.
         */
        public Config resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        /**
         * Convenience to call {@link #nowrap(boolean)} with {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config nowrap() {
            return nowrap (true);
        }

        /**
         * Determines if the text area for entry should not wrap lines.
         * 
         * @param nowrap
         *                  {@code true} if not to wrap (default is {@code false}).
         * @return this configuration instance.
         */
        public Config nowrap(boolean nowrap) {
            this.nowrap = nowrap;
            return this;
        }

        /**
         * See {@link #expandOnFocus(Length, boolean)} but with no height retention.
         */
        public Config expandOnFocus(Length expandOnFocus) {
            this.expandOnFocus = expandOnFocus;
            return this;
        }

        /**
         * Assign a height to expand to when focus is gained (and when lost it reverts
         * to the prior height).
         * <p>
         * Optionally retains the height if not empty.
         * 
         * @param expandOnFocus
         *                         the height to expand to when focus is gained.
         * @param retainIfNotEmpty
         *                         {@code true} to retain the height if not empty.
         * @return this configuration instance.
         */
        public Config expandOnFocus(Length expandOnFocus, boolean retainIfNotEmpty) {
            this.expandOnFocus = expandOnFocus;
            this.expandOnFocusRetain = retainIfNotEmpty;
            return this;
        }

        /**
         * Registers a key press handler. This (if present) will receive the various key
         * presses and return whether to accept that key press or not. It is a way of
         * filtering out key presses (or simply to respond to specific keys, like the
         * enter key).
         * 
         * @param keyPressHandler
         *                        the handler.
         * @return this configuration instance.
         */
        public Config keyPressHandler(Function<String, Boolean> keyPressHandler) {
            this.keyPressHandler = keyPressHandler;
            return this;
        }

        /**
         * Assigns placeholder text to display when the field is empty.
         * 
         * @param placeholder
         *                    placeholder content to display.
         * @return this configuration instance.
         */
        public Config placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TextAreaControl build(LayoutData... data) {
            return build (new TextAreaControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TextAreaControl(TextAreaControl.Config config) {
        super (config);

        if (config.expandOnFocus != null) {
            addListener(IFocusBlurListener.create(focus -> {
                height(config.expandOnFocus);
            }, blr -> {
                // This may be null which removes the height.
                if (!config.expandOnFocusRetain || StringSupport.empty(value()))
                    height(config.height);
            }));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(java.lang.Object)
     */
    @Override
    protected String prepareValueForAssignment(String value) {
        // This avoids the issue of the equivalence of null and the empty
        // string: convert null values to empty strings.
        return StringSupport.safe (super.prepareValueForAssignment (value));
    }

    /**
     * The input element.
     */
    protected HTMLTextAreaElement inputEl;

    /**
     * For the character counter.
     */
    protected HTMLElement counterEl;

    /**
     * This is used to assign a new height at run time. Often used to increase the
     * size of the text area when activated.
     * <p>
     * If called prior to rendering then will have the effect of setting a height on
     * the configuration.
     * 
     * @param height
     *               the new height (if {@code null} then the natural height is
     *               imposed).
     * @return this control.
     */
    public TextAreaControl height(Length height) {
        if (!isRendered()) {
            config().height = height;
        } else {
            if (height == null)
                height = Length.px (0);
            CSS.MIN_HEIGHT.apply(inputEl, height);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public String valueFromSource() {
        String value = StringSupport.safe (inputEl.value);
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(String value) {
        inputEl.value = StringSupport.safe (value);
        if ((config().expandOnFocus != null) && config().expandOnFocusRetain) {
            if (StringSupport.empty(value))
                height(config().height);
            else
                height(config().expandOnFocus);
        }
        _updateCounter();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$(el).$(root -> {
            Div.$(root).$(inner -> {
                inner.style (styles ().inner ());
                Textarea.$ (inner).$ (ta -> {
                    ta.by ("input");
                    ta.on (e -> {
                        if (!filterKeyPress (e.getKeyCode (), inputEl.value))
                            e.stopEvent ();
                    }, UIEventType.ONKEYPRESS);
                    ta.on (e -> modified (), UIEventType.ONKEYUP, UIEventType.ONPASTE);
                    ta.attr ("name", StringSupport.empty (data.getName ()) ? "" + getUUID () : data.getName ());
                    if (data.rows > 0)
                        ta.attr ("rows", "" + data.rows);
                    if (data.cols > 0)
                        ta.attr ("cols", "" + data.cols);
                    if (data.max > 0)
                        ta.attr ("maxlength", "" + data.max);
                    if (!StringSupport.empty (data.placeholder))
                        ta.attr ("placeholder", new SafeHtmlBuilder ().appendEscaped (data.placeholder).toSafeHtml ().asString ());
                    if (data.height != null)
                        ta.css (CSS.HEIGHT, data.height);
                    if (data.nowrap)
                        ta.attr ("wrap", "off");
                    ta.testId (buildTestId ("input")).testRef ("input");
                });
            });
            if (config().counter) {
                Div.$(root).style(styles().counter()).by("counter").text ((config().max <= 0) ? "0 characters" : "0 / " + config().max);
            }
        }).build (tree -> {
            inputEl = (HTMLTextAreaElement) manageFocusEl (tree.first ("input"));
            counterEl = (HTMLElement) manageFocusEl (tree.first ("counter"));
        });
    }

    @Override
    protected void onModified() {
        super.onModified();
        _updateCounter();
    }

    /**
     * Updates the counter indicator.
     */
    protected void _updateCounter() {
        if (counterEl != null) {
            Wrap.buildInto(counterEl, el -> {
                int length = (inputEl.value == null) ? 0 : inputEl.value.length();
                if (config().max <= 0) {
                    if (length == 1)
                        el.text("1 character");
                    else
                        el.text(length + " characters");
                } else {
                    if (length < config().max) {
                        counterEl.classList.remove(styles().limit());
                        el.text(length + " / " + config().max);
                    } else {
                        counterEl.classList.add(styles().limit());
                        el.text("limit reached " + length + " / " + config().max);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        if (config ().resizable)
            getRoot ().classList.add (styles ().resizable ());
    }

    /**
     * Processes a key press and determines if the change should go ahead.
     * 
     * @param keyCode
     *                the key code from the press.
     * @param value
     *                the current value on the element.
     * @return {@code true} if the key passes.
     */
    protected boolean filterKeyPress(String keyCode, String value) {
        if (config ().keyPressHandler != null) {
            Boolean response = config ().keyPressHandler.apply (keyCode);
            if ((response == null) || response)
                return true;
            return false;
        }
        return true;
    }

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    public static interface ILocalCSS extends IControlCSS {

        /**
         * Inner wrap around the control (for the border).
         */
        public String inner();

        /**
         * Allows for the text area to resize.
         */
        public String resizable();

        /**
         * Formats the character counter.
         */
        public String counter();

        /**
         * When the character counter has reached its limit.
         */
        public String limit();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/TextAreaControl.css",
        "com/effacy/jui/ui/client/control/TextAreaControl_Override.css"
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
