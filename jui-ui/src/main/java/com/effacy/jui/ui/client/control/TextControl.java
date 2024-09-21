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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;

public class TextControl extends Control<String, TextControl.Config> {

    /************************************************************************
     * Configuration and construction
     ************************************************************************/

     /**
      * The default style to employ when one is not assign explicitly.
      */
     public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link TextControl}.
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
             *                     the CSS styles.
             * @param selectorIcon
             *                     the CSS class to use for the selector icon.
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
            public static final Style STANDARD = Style.create (StandardLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #max(int)}.
         */
        private int max = 0;

        /**
         * See {@link #password(boolean)}.
         */
        private boolean password = false;

        /**
         * See {@link #placeholder(String)}.
         */
        private String placeholder;

        /**
         * See {@link #iconLeft(String)}.
         */
        private String iconLeft;

        /**
         * See {@link #iconRight(String)}.
         */
        private String iconRight;

        /**
         * See {@link #clearAction(boolean)}.
         */
        private boolean clearAction;

        /**
         * See {@link #keyPressHandler(Function)}.
         */
        private BiFunction<String, String, Boolean> keyPressHandler;

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
         * See {@link #password(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config password() {
            return password (true);
        }

        /**
         * Marks the text input as occluded (such as when entering a password).
         * 
         * @param password
         *                 {@code true} if should be occluded.
         * @return this configuration instance.
         */
        public Config password(boolean password) {
            this.password = password;
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
         * Places an icon on the left side of the field (inside the border).
         * 
         * @param iconLeft
         *                 icon CSS (i.e. {@link FontAwesome}).
         * @return this configuration instance.
         */
        public Config iconLeft(String iconLeft) {
            this.iconLeft = iconLeft;
            return this;
        }

        /**
         * Places an icon on the right side of the field (inside the border).
         * 
         * @param iconRight
         *                  icon CSS (i.e. {@link FontAwesome}).
         * @return this configuration instance.
         */
        public Config iconRight(String iconRight) {
            this.iconRight = iconRight;
            return this;
        }

        /**
         * See {@link #password(boolean)}. Convenience to pass {@code true}.
         * 
         * @return this configuration instance.
         */
        public Config clearAction() {
            return clearAction (true);
        }

        /**
         * Whether or not a clear action should be displayed when the control has
         * contents.
         * 
         * @param clearAction
         *                    {@code true} if a clear action should be included.
         * @return this configuration instance.
         */
        public Config clearAction(boolean clearAction) {
            this.clearAction = clearAction;
            return this;
        }

        /**
         * Registers a key press handler. This (if present) will receive the various key
         * presses and return whether to accept that key press or not. It is a way of
         * filtering out key presses (or simply to respond to specific keys, like the
         * enter key).
         * <p>
         * The function is passed the keycode and the key from the key event. If
         * accepted it must return {@code true}.
         * 
         * @param keyPressHandler
         *                        the handler.
         * @return this configuration instance.
         */
        public Config keyPressHandler(BiFunction<String, String, Boolean> keyPressHandler) {
            this.keyPressHandler = keyPressHandler;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TextControl build(LayoutData... data) {
            return build (new TextControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TextControl(TextControl.Config config) {
        super (config);
    }

    /************************************************************************
     * Behaviour
     ************************************************************************/

    /**
     * The input element.
     */
    protected HTMLInputElement inputEl;

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
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public String valueFromSource() {
        String value = StringSupport.safe (inputEl.value);
        if (config ().clearAction) {
            if (StringSupport.empty (value))
                getRoot ().classList.remove (styles ().clear ());
            else
                getRoot ().classList.add (styles ().clear ());
        }
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(String value) {
        if (config ().clearAction) {
            if (StringSupport.empty (value))
                getRoot ().classList.remove (styles ().clear ());
            else
                getRoot ().classList.add (styles ().clear ());
        }
        inputEl.value = StringSupport.safe (value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$ (el).$ (
            Div.$ ().style (styles ().inner ()).$ (
                Em.$ ()
                    .style (styles ().read_only (), FontAwesome.lock ()),
                Em.$ ().iff (!StringSupport.empty (data.iconLeft))
                    .style (styles ().left (), data.iconLeft),
                Input.$ (data.password ? "password" : "text").$ (input -> {
                    input.ref ("input");
                    input.on (e -> {
                        if (!filterKeyPress (e.getKeyCode (), e.getKey(), inputEl.value))
                            e.stopEvent ();
                    }, UIEventType.ONKEYPRESS);
                    input.on (e -> modified (), UIEventType.ONKEYUP, UIEventType.ONPASTE);
                    if (StringSupport.empty (data.getName ()))
                        input.attr ("name", "" + getUUID ());
                    else
                        input.attr ("name", data.getName ());
                    if (data.max > 0)
                        input.attr ("maxlength", "" + data.max);
                    if (!StringSupport.empty (data.placeholder))
                        input.attr ("placeholder", new SafeHtmlBuilder ().appendEscaped (data.placeholder));
                    input.testId (buildTestId ("input")).testRef ("input");
                }),
                Em.$ ().iff (data.clearAction)
                    .style (styles ().clear (), FontAwesome.times ())
                    .testId (buildTestId ("clear")).testRef ("clear")
                    .on (e -> {
                        inputEl.value = "";
                        modified ();
                    }, UIEventType.ONCLICK),
                Em.$ ().iff (!StringSupport.empty (data.iconRight))
                    .style (styles ().right (), data.iconRight)
            )
        ).build (tree -> {
            // Register the input as the focus element (we only have one).
            inputEl = (HTMLInputElement) manageFocusEl (tree.first ("input"));
        });
    }

    /**
     * Processes a key press and determines if the change should go ahead.
     * 
     * @param keyCode
     *                the key code from the press.
     * @param key
     *                the key.
     * @param value
     *                the current value on the element.
     * @return {@code true} if the key passes.
     */
    protected boolean filterKeyPress(String keyCode, String key, String value) {
        if (config ().keyPressHandler != null) {
            Boolean response = config ().keyPressHandler.apply (keyCode, key);
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
         * Left locality.
         */
        public String left();

        /**
         * Right locality.
         */
        public String right();

        /**
         * Clear action.
         */
        public String clear();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/TextControl.css",
        "com/effacy/jui/ui/client/control/TextControl_Override.css"
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
