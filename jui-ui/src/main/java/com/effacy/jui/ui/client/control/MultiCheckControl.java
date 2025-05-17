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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.control.Control;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEvent.KeyCode;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

/**
 * A variant of the {@link CheckControl} that allows the user to select among a
 * number of options encoded as an enum (rather than a boolean).
 *
 * @author Jeremy Buckley
 */
public class MultiCheckControl<V> extends Control<V, MultiCheckControl.Config<V>> {

    /**
     * The default style to employ when one is not assign explicitly.
     */
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link MultiCheckControl}.
     */
    public static class Config<V> extends Control.Config<V, Config<V>> {

        /********************************************************************
         * Styles for the control.
         ********************************************************************/

        /**
         * Style for the control (defines presentation configuration including CSS).
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

            /**
             * Panel style.
             */
            public static final Style PANEL = create(PanelLocalCSS.instance ());

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = (DEFAULT_STYLE != null) ? DEFAULT_STYLE : Style.STANDARD;

        /**
         * The value options mapped to labels.
         */
        private List<Option> options = new ArrayList<> ();

        /**
         * See {@link #label(String)}.
         */
        private String label;

        /**
         * See {@link #labelBold(boolean)}.
         */
        private boolean labelBold;

        /**
         * See {@link #left(boolean)}.
         */
        private boolean left;

        /**
         * See {@link #expand(boolean)}.
         */
        private boolean expand;

        /**
         * See {@link #span(Length)}.
         */
        private Length span;

        /**
         * Captures a selectable option.
         */
        public class Option {

            /**
             * The value.
             */
            protected V value;

            /**
             * The display label.
             */
            protected String label;

            /**
             * The name of the option.
             */
            private String name;

            /**
             * UID for mapping.
             */
            private String uid = UID.createUID ();

            /**
             * Construct with a value and display label.
             * 
             * @param value
             *              the value.
             * @param label
             *              the display label.
             */
            public Option(V value, String label) {
                this.value = value;
                this.label = label;
            }

            /**
             * Unique name for the option (is included in the label).
             */
            public String name() {
                if (name == null) {
                    if (StringSupport.empty (Config.this.getName ()))
                        name = "multicheck_ctl_" + UID.createUID ();
                    else
                        name = Config.this.getName ();
                }
                return name;
            }

        }

        /**
         * Construct with the default style.
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
            super ();
            style(style);
        }

        /**
         * Assigns an alternative style.
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config<V> style(Style style) {
            if (style != null)
                this.style = style;
            return this;
        }

        /**
         * Adds an option.
         * 
         * @param value
         *              the value.
         * @param label
         *              the display label.
         * @return this configuration instance.
         */
        public Config<V> option(V value, String label) {
            options.add (new Option (value, label));
            return this;
        }

        /**
         * Adds a collection of options with a function for providing labels.
         * 
         * @param values
         *                      the value.
         * @param labelProvider
         *                      (optional) the label provider.
         * @return this configuration instance.
         */
        public Config<V> option(V[] values, Function<V, String> labelProvider) {
            for (V value : values) {
                if (value == null)
                    continue;
                String label = (labelProvider == null) ? value.toString () : labelProvider.apply (value);
                option (value, label);
            }
            return this;
        }

        /**
         * Provides a label for the check.
         * 
         * @param label
         *              the label for the check
         * @return this configuration instance.
         */
        public Config<V> label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Convenience for passing {@code true} through to {@link #labelBold(boolean)}.
         */
        public Config<V> labelBold() {
            return labelBold (true);
        }

        /**
         * Bold faces the label (so it is more promanent).
         * 
         * @param bold
         *             {@code true} to bold the label.
         * @return this configuration instance.
         */
        public Config<V> labelBold(boolean labelBold) {
            this.labelBold = labelBold;
            return this;
        }

        /**
         * Convenience for passing {@code true} through to {@link #expand(boolean)}.
         */
        public Config<V> left() {
            return left (true);
        }

        /**
         * Reverse the checkbox and label (i.e. label on the left rather than on the
         * right).
         * 
         * @param left
         *             {@code true} to place the label on the left.
         * @return this configuration instance.
         */
        public Config<V> left(boolean left) {
            this.left = left;
            return this;
        }

        /**
         * Convenience for passing {@code true} through to {@link #expand(boolean)}.
         */
        public Config<V> expand() {
            return expand (true);
        }

        /**
         * To allow for the gap between the checkbox and the label to expand so the
         * label and checkbox appear at the ends of the containment area for the
         * control.
         * <p>
         * When placing in a control group ensure that the containment area grows.
         * 
         * @param expand
         *               {@code true} to place the label on the left.
         * @return this configuration instance.
         */
        public Config<V> expand(boolean expand) {
            this.expand = expand;
            return this;
        }

        /**
         * Defines a span (width) of the selector control (which can be used to balance
         * the width of the items).
         * 
         * @param span
         *             the span of the selector control.
         * @return this configuration instance.
         */
        public Config<V> span(Length span) {
            this.span = span;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public MultiCheckControl<V> build(LayoutData... data) {
            return build (new MultiCheckControl<V> (this), data);
        }

    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public MultiCheckControl(MultiCheckControl.Config<V> config) {
        super (config);
    }

    private Predicate<V> preRenderTest;

    /**
     * Filters (shows / hides) options.
     * 
     * @param test
     *             the predicate test to which option is enabled.
     */
    public void filterOptions(Predicate<V> test) {
        if (test == null)
            return;
        if (isRendered()) {
            config().options.forEach(option -> {
                if (test.test(option.value))
                    JQuery.$(options.get(option.uid)).show();
                else
                    JQuery.$(options.get(option.uid)).hide();
            });
        } else {
            this.preRenderTest = test;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#prepareValueForAssignment(java.lang.Object)
     */
    @Override
    protected V prepareValueForAssignment(V value) {
        // This avoids the issue of the equivalence of null and the empty
        // string: convert null values to empty strings.
        return (value == null) ? config ().options.get (0).value : super.prepareValueForAssignment (value);
    }

    /************************************************************************
     * Rendering and styles.
     ************************************************************************/

    /**
     * The radio buttons representing the options.
     */
    protected List<HTMLInputElement> inputEls;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueFromSource()
     */
    @Override
    public V valueFromSource() {
        for (HTMLInputElement inputEl : inputEls) {
            if (inputEl.checked)
                return config ().options.get (Integer.parseInt (inputEl.getAttribute ("item"))).value;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#valueToSource(java.lang.Object)
     */
    @Override
    public void valueToSource(V value) {
        for (HTMLInputElement inputEl : inputEls) {
            Config<V>.Option option = config ().options.get (Integer.parseInt (inputEl.getAttribute ("item")));
            inputEl.parentElement.classList.remove (styles ().active ());
            inputEl.checked = false;
            if (option.value.equals (value)) {
                inputEl.parentElement.classList.add (styles ().active ());
                inputEl.checked = true;
            }
        }
    }

    private Map<String,Element> options = new HashMap<>();

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$ (root -> {
            Div.$(root).$(inner -> {
                inner.style (styles ().inner ());
                Div.$ (inner).$ (item -> {
                    item.style (styles ().item ());
                    if (config().left)
                        item.style (styles ().reverse ());
                    if (config().expand)
                        item.style (styles ().expand ());
                    if (config().labelBold)
                        item.style (styles ().bold ());
                    Div.$ (item).$ (grp -> {
                        grp.style (styles ().toggle ());
                        if (config().span != null)
                            grp.css (CSS.WIDTH, config().span);
                        Itr.forEach (config().options, (c, option) -> {
                            Label.$ (grp).$ (toggle -> {
                                toggle.use(n -> options.put(option.uid, (Element) n));
                                if (c.first ())
                                    toggle.style (styles ().first ());
                                if (c.last ())
                                    toggle.style (styles ().last ());
                                toggle.attr ("for", "check_ctl_" + option.uid);
                                Input.$ (toggle, "radio").id ("check_ctl_" + option.uid)
                                    .attr ("item", "" + c.index ())
                                    .attr ("value", option.uid)
                                    .attr ("name", option.name ())
                                    .on (e -> handleChange (e), UIEventType.ONCHANGE)
                                    .on (e -> handleKeyPress (e), UIEventType.ONKEYPRESS)
                                    .by ("radio")
                                    .testId (buildTestId ("input_" + c.index ()))
                                    .testRef ("input_" + c.index ());
                                Span.$ (toggle).$ (content -> {
                                    content.text (option.label);
                                });
                            });
                        });
                    });
                    if (!StringSupport.empty(config().label)) {
                        Span.$ (item).style (styles ().spacer ());
                        Span.$ (item).$ (label -> {
                            label.text (config().label);
                        });
                    }
                });
            });
        }).build (tree -> {
            inputEls = tree.all ("radio");
            inputEls.forEach (i -> manageFocusEl (i));
            if (preRenderTest != null)
                filterOptions(preRenderTest);
        });
    }

    /**
     * Handles a change on an input.
     * 
     * @param e
     *          the event.
     */
    protected void handleChange(UIEvent e) {
        HTMLInputElement el = Js.cast (e.getTarget ());
        if (el.checked)
            activate (Integer.parseInt (el.getAttribute ("item")));
        modified ();
        TimerSupport.defer (() -> e.getTarget ().focus ());
    }

    /**
     * Handle a key press on a check box to change its state.
     */
    protected void handleKeyPress(UIEvent e) {
        if (!KeyCode.ENTER.is (e.getKeyCode ()))
            return;
        HTMLInputElement el = Js.cast (e.getTarget ());
        if (el.checked)
            return;
        modified ();
        e.stopEvent ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.control.Control#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();
        activate (0);
    }

    protected void activate(int idx) {
        if ((idx < 0) || (idx >= inputEls.size ()))
            return;
        for (HTMLInputElement inputEl : inputEls)
            inputEl.parentElement.classList.remove (styles ().active ());
        if (inputEls.size () > 0) {
            inputEls.get (idx).checked = true;
            inputEls.get (idx).parentElement.classList.add (styles ().active ());
        }
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

        public String reverse();

        public String expand();

        public String bold();

        public String spacer();

        public String toggle();

        public String active();

        public String first();

        public String last();
    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/MultiCheckControl.css",
        "com/effacy/jui/ui/client/control/MultiCheckControl_Override.css"
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
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/control/MultiCheckControl.css",
        "com/effacy/jui/ui/client/control/MultiCheckControl_Override.css",
        "com/effacy/jui/ui/client/control/MultiCheckControl_Panel.css"
    })
    public static abstract class PanelLocalCSS implements ILocalCSS {

        private static PanelLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (PanelLocalCSS) GWT.create (PanelLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
