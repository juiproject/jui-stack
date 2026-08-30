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

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;

public class NumberControl extends Control<Double, NumberControl.Config> {

    /************************************************************************
     * Configuration and construction
     ************************************************************************/

     /**
      * The default style to employ when one is not assign explicitly.
      */
     public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link NumberControl}.
     */
    public static class Config extends Control.Config<Double, Config> {

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
         * See {@link #decimalPlaces(int)}.
         */
        private int decimalPlaces = 0;

        /**
         * See {@link #step(Double)}.
         */
        private Double step;

        /**
         * See {@link #stepHide(boolean)}.
         */
        private boolean stepHide;

        /**
         * See {@link #min(Double)}.
         */
        private Double min;

        /**
         * See {@link #max(Double)}.
         */
        private Double max;

        /**
         * See {@link #allowEmpty(boolean)}.
         */
        private boolean allowEmpty;

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
         * Specifies the number of decimal places.
         * <p>
         * The default is (whole numbers). Any negative number is treated as 0 (whole
         * numbers) and otherwise cannot exceed 6.
         * 
         * @param decimalPlaces
         *                      the number.
         * @return this configuration.
         */
        public Config decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = Math.min(6,Math.max(0, decimalPlaces));
            return this;
        }

        /**
         * Assigns a custom step size.
         * <p>
         * The step size is, by default, the smallest resolution relative the number of
         * decimal places.
         * 
         * @param step
         *             the step size.
         * @return this configuration.
         */
        public Config step(Double step) {
            this.step = step;
            return this;
        }

        /**
         * Convenience to call {@link #stepHide(boolean)} passing {@code true}.
         * 
         * @return this configuration.
         */
        public Config stepHide() {
            return stepHide (true);
        }

        /**
         * Hides the stepper.
         * 
         * @param stepHide
         *                 {@code true} to hide the stepper.
         * @return this configuration.
         */
        public Config stepHide(boolean stepHide) {
            this.stepHide = stepHide;
            return this;
        }

        /**
         * The minimum value permissible.
         * 
         * @param min
         *            the minium.
         * @return this configuration.
         */
        public Config min(Double min) {
            this.min = min;
            return this;
        }

        /**
         * The maximum value permissible.
         * 
         * @param max
         *            the maximum.
         * @return this configuration.
         */
        public Config max(Double max) {
            this.max = max;
            return this;
        }

        /**
         * Convenience to call {@link #allowEmpty(boolean)} passing {@code true}.
         *
         * @return this configuration.
         */
        public Config allowEmpty() {
            return allowEmpty (true);
        }

        /**
         * Permits the control to be empty, which it conveys as a {@code null} value.
         * <p>
         * By default there is no such thing as an empty number control: an unassigned
         * value presents as zero (or the minimum, if one is configured, since the
         * limits are enforced on assignment) and reads back as a number the user never
         * entered. That suits a quantity that always has a value, but not an optional
         * one — bind a nullable field to the default behaviour and merely opening the
         * form and saving will write a value that was never intended.
         * <p>
         * When enabled the empty input round-trips as {@code null}: it is not coerced
         * on assignment, not filled in on blur, and not clamped to the minimum. Use
         * {@code required()} on the enclosing form cell where a value must be given —
         * that is the mechanism for demanding one, rather than the absence of a way to
         * express its lack.
         *
         * @param allowEmpty
         *                   {@code true} to treat an empty input as {@code null}.
         * @return this configuration.
         */
        public Config allowEmpty(boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public NumberControl build(LayoutData... data) {
            return build (new NumberControl (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public NumberControl(NumberControl.Config config) {
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
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(Double)
     */
    @Override
    protected Double prepareValueForAssignment(Double value) {
        // An empty-permitting control passes null through rather than enforcing the
        // limits against it; clamping "no value" to the minimum is how an unset field
        // silently acquires one.
        if ((value == null) && config ().allowEmpty)
            return null;
        // Need to enforce any limits (min and max).
        return normalise ((value == null) ? 0.0 : value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public Double valueFromSource() {
        if (empty ())
            return null;
        return normalise(inputEl.value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(Double value) {
        if (value == null) {
            if (config ().allowEmpty) {
                inputEl.value = "";
                return;
            }
            value = normalise(0.0);
        }
        inputEl.value = StringSupport.safe (value.toString());
    }

    @Override
    protected void onBlur() {
        renormalise ();
    }

    /**
     * Whether the input is currently empty <em>and</em> that is a value the control
     * is permitted to hold.
     */
    protected boolean empty() {
        return (config ().allowEmpty && StringSupport.empty (inputEl.value));
    }

    /**
     * Rewrites the input with its value brought within the configured limits,
     * leaving a permitted empty alone.
     */
    protected void renormalise() {
        if (empty ())
            return;
        inputEl.value = normalise(inputEl.value).toString();
    }

    /**
     * See {@link #normalise(Double)} but parses the value frome the passed string
     * (if not valid then treats as 0).
     * 
     * @param value
     *              the value to parse.
     * @return the normalised value.
     */
    protected Double normalise(String value) {
        if (StringSupport.empty(value))
            return normalise(0.0);
        try {
            return normalise(Double.parseDouble(value));
        } catch (Throwable e) {
            return normalise(0.0);
        }
    }

    /**
     * Normalises the passed value to be within range. A {@code null} value is
     * normalised to 0 and modified to be in bounds.
     * 
     * @param value
     *              the value to normalise.
     * @return the normalised value.
     */
    protected Double normalise(Double value) {
        if (value == null)
            value = 0.0;
        if ((config().min != null) && (value < config().min))
            value = config().min;
        if ((config().max != null) && (value > config().max))
            value = config().max;
        return value;
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
                Input.$ ("number").$ (input -> {
                    input.ref ("input");
                    if (data.decimalPlaces <= 0) {
                        input.on (e -> {
                            if (e.getKey().charAt(0) == '.')
                                e.stopEvent ();
                        }, UIEventType.ONKEYPRESS);
                    }
                    input.on (e -> modified (), UIEventType.ONINPUT);
                    if (StringSupport.empty (data.getName ()))
                        input.attr ("name", "" + getUUID ());
                    else
                        input.attr ("name", data.getName ());
                    if (data.step != null) {
                        input.attr ("step", data.step.toString());
                    } else if (data.decimalPlaces <= 0) {
                        input.attr ("step", "1");
                    } else {
                        String step = "0.";
                        for (int i = 1; i < data.decimalPlaces; i++)
                            step += "0";
                        input.attr ("step", step + "1");
                    }
                    input.testId (buildTestId ("input")).testRef ("input");
                }),
                Span.$ ().iff (!data.stepHide).$ (
                    // Steppers are not able to take focus. This is due to the
                    // fact that the up and down arrow keys can effect the
                    // stepping via the keyboard (or other means) so we just
                    // let the input take focus.
                    Em.$().style (FontAwesome.minus())
                        .onclick(e -> {
                            inputEl.stepDown();
                            renormalise ();
                            TimerSupport.defer(() -> {
                                modified();
                            });
                        }),
                    I.$ (),
                    Em.$().style (FontAwesome.plus())
                        .onclick(e -> {
                            inputEl.stepUp();
                            renormalise ();
                            TimerSupport.defer(() -> {
                                modified();
                            });
                        })
                )
            )
        ).build (tree -> {
            // Register the input as the focus element (we only have one).
            inputEl = (HTMLInputElement) manageFocusEl (tree.first ("input"));
        });
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

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/Control.css",
        "com/effacy/jui/ui/client/control/NumberControl.css",
        "com/effacy/jui/ui/client/control/NumberControl_Override.css"
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
