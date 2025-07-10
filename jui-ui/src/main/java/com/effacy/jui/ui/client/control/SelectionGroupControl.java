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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEvent.KeyCode;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Carrier;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

public class SelectionGroupControl<V> extends Control<List<V>, SelectionGroupControl.Config<V>> {

    /**
     * The default style to employ when one is not assign explicitly.
    */
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link SelectionGroupControl}.
     */
    public static class Config<V> extends Control.Config<List<V>, Config<V>> {

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
             * Standard style (horizontal).
             */
            public static final Style STANDARD = create (StandardLocalCSS.instance ());

            /**
             * Standard style (vertical).
             */
            public static final Style STANDARD_VERTICAL = create (StandardVerticalLocalCSS.instance ());

            /**
             * Survey style (vertical).
             */
            public static final Style SURVEY = create (SurveyLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * See {@link #option(Object, String, String)}.
         */
        private List<Option> options = new ArrayList<> ();

        /**
         * See {@link #radio(boolean)}.
         */
        private boolean radio;

        /**
         * Encapsulates an option (display data and vaue).
         */
        public class Option {

            /**
             * A unique ID.
             */
            private String uid;

            /**
             * The value.
             */
            private V value;

            /**
             * An optional icon.
             */
            private String icon;

            /**
             * A display label.
             */
            private String label;

            /**
             * An optional description.
             */
            private String description;

            /**
             * If the option is nested.
             */
            private boolean nested;

            /**
             * Construct an option.
             * 
             * @param icon
             *                    (optional) the CSS class for an icon.
             * @param value
             *                    the value.
             * @param label
             *                    the display label.
             * @param description
             *                    (optional) a description.
             * @param nested
             *                    if this option is nested under the previous (un-nested)
             *                    option.
             */
            public Option(V value, String icon, String label, String description, boolean nested) {
                this.value = value;
                this.icon = icon;
                this.label = label;
                this.description = description;
                this.nested = nested;
                this.uid = UID.createUID ();
            }

            /**
             * The name from the configuration.
             * 
             * @return the name.
             */
            public String getName() {
                if (Config.this.radio)
                    return Config.this.name;
                return Config.this.name + "-" + Config.this.options.indexOf (this);
            }

            /**
             * The test ID.
             * 
             * @return the test ID.
             */
            public String getTestId() {
                return Config.this.testId + "-" + Config.this.options.indexOf (this);
            }

            /**
             * Determines if the input should be a radio button.
             * 
             * @return {@code true} if is a radio.
             */
            public boolean isRadio() {
                return Config.this.radio;
            }
        }

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
        public Config<V> style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Specifies an option that can be selected.
         * 
         * @param value
         *                    the underlying value.
         * @param label
         *                    the display label.
         * @param description
         *                    (optional) supporting description.
         * @return this configuration.
         */
        public Config<V> option(V value, String label, String description) {
            return option(value, label, description, false);
        }

        /**
         * Specifies an option that can be selected.
         * <p>
         * Note that the option can be nested in which case it will appear below the
         * last previous un-nested option. The option is also disabled when the parent
         * is unchecked.
         * 
         * @param value
         *                    the underlying value.
         * @param label
         *                    the display label.
         * @param description
         *                    (optional) supporting description.
         * @param nested
         *                    if the option should be nested (only one level is
         *                    permitted).
         * @return this configuration.
         */
        public Config<V> option(V value, String label, String description, boolean nested) {
            options.add (new Option (value, null, label, description, nested));
            return this;
        }

        /**
         * Specifies an option that can be selected.
         * 
         * @param icon
         *                    (optional) the CSS class for an icon.
         * @param value
         *                    the underlying value.
         * @param label
         *                    the display label.
         * @param description
         *                    (optional) supporting description.
         * @return this configuration.
         */
        public Config<V> option(V value, String icon, String label, String description) {
            return option (value, icon, label, description, false);
        }

        /**
         * Specifies an option that can be selected.
         * <p>
         * Note that the option can be nested in which case it will appear below the
         * last previous un-nested option. The option is also disabled when the parent
         * is unchecked.
         * 
         * @param icon
         *                    (optional) the CSS class for an icon.
         * @param value
         *                    the underlying value.
         * @param label
         *                    the display label.
         * @param description
         *                    (optional) supporting description.
         * @param nested
         *                    if the option should be nested (only one level is
         *                    permitted).
         * @return this configuration.
         */
        public Config<V> option(V value, String icon, String label, String description, boolean nested) {
            options.add (new Option (value, icon, label, description, nested));
            return this;
        }

        /**
         * Convenience to pass {@code true} to {@link #radio(boolean)}.
         * 
         * @return this configuration instance.
         */
        public Config<V> radio() {
            return radio (true);
        }

        /**
         * The options should be radio buttons not check boxes.
         * 
         * @param radio
         *              {@code true} if should be radio buttons.
         * @return this configuration instance.
         */
        public Config<V> radio(boolean radio) {
            this.radio = radio;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public SelectionGroupControl<V> build(LayoutData... data) {
            return build (new SelectionGroupControl<V> (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public SelectionGroupControl(SelectionGroupControl.Config<V> config) {
        super (config);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(java.lang.Object)
     */
    @Override
    protected List<V> prepareValueForAssignment(List<V> value) {
        // This avoids the issue of the equivalence of null and the empty
        // string: convert null values to empty strings.
        return (value == null) ? new ArrayList<> () : super.prepareValueForAssignment (value);
    }

    /************************************************************************
     * Rendering and styles.
     ************************************************************************/

    /**
     * The input element.
     */
    protected List<HTMLInputElement> inputEls;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public List<V> valueFromSource() {
        List<V> results = new ArrayList<> ();
        for (HTMLInputElement inputEl : inputEls) {
            if (inputEl.checked) {
                V value = find (inputEl.getAttribute ("value"));
                if (value != null)
                    results.add (value);
            }
        }
        return results;
    }

    /**
     * Find the matching value for the UID.
     * 
     * @param uid
     *            the UID.
     * @return the associated value.
     */
    protected V find(String uid) {
        for (Config<V>.Option option : config ().options) {
            if (option == null)
                continue;
            if (option.uid.equals (uid))
                return option.value;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(List<V> value) {
        Set<String> uids = new HashSet<> ();
        for (Config<V>.Option option : config ().options) {
            if (option == null)
                continue;
            if (option.value == null)
                continue;
            if (value.contains (option.value))
                uids.add (option.uid);
        }
        for (HTMLInputElement inputEl : inputEls) {
            inputEl.parentElement.parentElement.classList.remove (styles().selected());
            String uid = inputEl.getAttribute ("value");
            if (uids.contains (uid))
                inputEl.checked = true;
            else
                inputEl.checked = false;
        }
        updateSelected();
    }

    /**
     * Loops over the input elements and updates the selected style on the enclosing
     * item.
     */
    protected void updateSelected() {
        for (HTMLInputElement inputEl : inputEls) {
            String depends = (String) JQuery.$(inputEl).attr("depends");
            LOOP: for (HTMLInputElement dependEl : inputEls) {
                if (dependEl.id.equals(depends)) {
                    if (dependEl.checked) {
                        inputEl.disabled = false;
                        inputEl.parentElement.parentElement.classList.remove (styles().disabled());
                    } else {
                        inputEl.disabled = true;
                        inputEl.parentElement.parentElement.classList.add (styles().disabled());
                    }
                    break LOOP;
                }
            }
            if (inputEl.checked)
                inputEl.parentElement.parentElement.classList.add (styles().selected());
            else
                inputEl.parentElement.parentElement.classList.remove (styles().selected());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config<V> data) {
        return Wrap.$ (el).$ (root -> {
            if (config().radio)
                root.style(styles().radio());
            Div.$ (root).style (styles ().inner ()).$ (inner -> {
                Carrier<String> currentOption = Carrier.of();
                data.options.forEach (option -> {
                    if (!option.nested)
                        currentOption.set(option.uid);
                    Div.$ (inner).style (styles ().item ()).style(option.nested, styles().indented()).$ (
                        Label.$ ().attr ("for", "selection_group_" + option.uid).$ (
                            Input.$ (option.isRadio () ? "radio" : "checkbox")
                                .by ("input")
                                .id ("selection_group_" + option.uid)
                                .attr ("testid", option.getTestId ())
                                .attr ("name", option.getName ())
                                .attr ("value", "" + option.uid)
                                .attr ("depends", (option.nested) && !currentOption.isNull() ? "selection_group_" + currentOption.get() : "")
                                .on (e -> handleKeyPress (e), UIEventType.ONKEYPRESS)
                                .on (e -> handleChange (e), UIEventType.ONCHANGE),
                            Div.$ ().style (styles ().label ()).$ (
                                Div.$ ().$ (
                                    Em.$ ().style (option.icon).iff (!StringSupport.empty (option.icon)),
                                    Span.$ ().text (option.label)
                                ),
                                Div.$ ().style (styles ().description ())
                                    .text (option.description)
                                    .iff (!StringSupport.empty (option.description))
                            )
                        )
                    );
                });
            });
        }).build (tree -> {
            inputEls = tree.all ("input");
            inputEls.forEach (i -> manageFocusEl (i));
        });
    }

    /**
     * Handle a key press on a check box to change its state.
     */
    protected void handleKeyPress(UIEvent e) {
        if (!KeyCode.ENTER.is (e.getKeyCode ()))
            return;
        HTMLInputElement el = Js.cast (e.getTarget ());
        el.checked = !el.checked;
        updateSelected ();
        modified ();
        e.stopEvent ();
    }

    /**
     * Handle a change on a check box.
     */
    protected void handleChange(UIEvent e) {
        updateSelected ();
        modified ();
        TimerSupport.defer (() -> e.getTarget ().focus ());
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * Styles (made available to selection).
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IControlCSS {

        /**
         * Inner wrap around the control (for the border).
         */
        public String inner();

        public String item();

        public String label();

        public String description();

        public String selected();

        public String indented();

        public String radio();
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/SelectionGroupControl.css",
        "com/effacy/jui/ui/client/control/SelectionGroupControl_Override.css"
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

    /**
     * Component CSS (vertical).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/SelectionGroupControl.css",
        "com/effacy/jui/ui/client/control/SelectionGroupControl_Vertical.css",
        "com/effacy/jui/ui/client/control/SelectionGroupControl_Vertical_Override.css"
    })
    public static abstract class StandardVerticalLocalCSS implements ILocalCSS {

        private static StandardVerticalLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardVerticalLocalCSS) GWT.create (StandardVerticalLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (survey style).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/SelectionGroupControl.css",
        "com/effacy/jui/ui/client/control/SelectionGroupControl_Survey.css",
        "com/effacy/jui/ui/client/control/SelectionGroupControl_Survey_Override.css"
    })
    public static abstract class SurveyLocalCSS implements ILocalCSS {

        private static SurveyLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (SurveyLocalCSS) GWT.create (SurveyLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
