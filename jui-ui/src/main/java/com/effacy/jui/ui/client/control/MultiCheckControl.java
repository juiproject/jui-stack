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

import org.objectweb.asm.Handle;

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
     * The default style to employ when one is not assigned explicitly.
     *
     * @deprecated retained as a back-compat hook for code that toggled the
     * default presentation by reassigning this field. New code should call
     * {@code config.variant(Variant.PANEL)} (etc.) on a per-control basis.
     */
    @Deprecated
    public static Config.Style DEFAULT_STYLE = Config.Style.STANDARD;

    /**
     * Configuration for building a {@link MultiCheckControl}.
     */
    public static class Config<V> extends Control.Config<V, Config<V>> {

        /********************************************************************
         * Variants and (deprecated) Styles.
         *
         * Variants are token-driven config-time hooks that any caller can
         * apply to switch the visual treatment without swapping
         * stylesheets. The base CSS does the structural work; variants
         * override custom-property tokens via {@code config.css(...)}.
         *
         * Style is the legacy interface-with-styles() pattern; it is now
         * a Variant under the hood so existing call sites that pass
         * {@code Style.STANDARD} / {@code Style.PANEL} continue to work.
         ********************************************************************/

        /**
         * A configurable variant of the control. Applied via
         * {@link Config#variant(Variant)} at config-time, typically just
         * setting custom-property tokens via {@link Config#css(String)}.
         */
        @FunctionalInterface
        public interface Variant {

            /** Apply this variant's configuration. */
            void configure(Config<?> cfg);

            /**
             * Standard horizontal radio-group presentation — pill-shaped
             * cells in a single rounded surround. The default; applies
             * no overrides on top of the base CSS.
             */
            public static final Variant STANDARD = config -> {
                // Defaults from MultiCheckControl.css; nothing extra.
            };

            /**
             * Panel variant — separate boxed cells with hairline borders.
             * Implemented entirely as token overrides on the standard
             * stylesheet (no separate CSS file).
             */
            public static final Variant PANEL = config -> config.css("""
                --jui-multicheckctl-border-radius: 5px;
                --jui-multicheckctl-border-width: 1px;
                --jui-multicheckctl-border-color: #ddd;
                --jui-multicheckctl-padding: 0;
                --jui-multicheckctl-item-border-radius: 3px;
                --jui-multicheckctl-item-border-radius-inner: 0;
                --jui-multicheckctl-size: 1em;
                --jui-multicheckctl-bg: var(--jui-ctl-bg);
                --jui-multicheckctl-item-text: #888;
                --jui-multicheckctl-item-padding-tb: 6px;
                --jui-multicheckctl-item-hover-bg: #eee;
            """);

            /**
             * Segmented control — iOS-style. The whole group sits in a
             * rounded surround with a thin border; the selected pill
             * lifts forward with a lighter surface + subtle shadow ring.
             * Unselected items are muted-text on the surround background.
             */
            public static final Variant SEGMENTED = config -> config.css("""
                --jui-multicheckctl-border-radius: 8px;
                --jui-multicheckctl-border-width: 1px;
                --jui-multicheckctl-border-color: var(--jui-role-border-default);
                --jui-multicheckctl-bg: var(--jui-role-surface-muted);
                --jui-multicheckctl-padding: 1px;
                --jui-multicheckctl-size: 0.875em;
                --jui-multicheckctl-item-border-radius: 5px;
                --jui-multicheckctl-item-border-radius-inner: 5px;
                --jui-multicheckctl-item-padding-tb: 8px;
                --jui-multicheckctl-item-padding-lr: 14px;
                --jui-multicheckctl-item-text: var(--jui-role-text-muted);
                --jui-multicheckctl-item-weight: 500;
                --jui-multicheckctl-item-hover-bg: transparent;
                --jui-multicheckctl-item-selected-bg: var(--jui-role-surface-canvas);
                --jui-multicheckctl-item-selected-text: var(--jui-role-text-default);
                --jui-multicheckctl-item-selected-weight: 600;
                --jui-multicheckctl-item-selected-shadow:
                    0 1px 2px rgba(0,0,0,0.06),
                    0 0 0 1px rgba(0,0,0,0.06);
            """);
        }

        /**
         * Style for the control.
         *
         * @deprecated use {@link Variant} directly. Style now extends Variant
         * so existing call sites using {@code Style.STANDARD} / {@code Style.PANEL}
         * continue to compile and behave identically.
         */
        @Deprecated
        public interface Style extends Variant {

            /**
             * The CSS styles for this style.
             *
             * @deprecated styles are now owned by {@link Config#styles(ILocalCSS)};
             * a Variant should set them via that setter from inside its
             * {@link Variant#configure} implementation if it needs an
             * alternate stylesheet.
             */
            @Deprecated
            public ILocalCSS styles();

            /**
             * Convenience to wrap a custom {@link ILocalCSS} as a Style.
             *
             * @deprecated define a {@link Variant} and call
             * {@code cfg.styles(...)} from its configure method.
             */
            @Deprecated
            public static Style create(final ILocalCSS styles) {
                return new Style () {
                    @Override public ILocalCSS styles() { return styles; }
                    @Override public void configure(Config<?> cfg) { cfg.styles (styles); }
                };
            }

            /** @deprecated use {@link Variant#STANDARD}. */
            @Deprecated
            public static final Style STANDARD = new Style () {
                @Override public ILocalCSS styles() { return StandardLocalCSS.instance (); }
                @Override public void configure(Config<?> cfg) { Variant.STANDARD.configure (cfg); }
            };

            /** @deprecated use {@link Variant#PANEL}. */
            @Deprecated
            public static final Style PANEL = new Style () {
                @Override public ILocalCSS styles() { return StandardLocalCSS.instance (); }
                @Override public void configure(Config<?> cfg) { Variant.PANEL.configure (cfg); }
            };
        }

        /**
         * The CSS styles in effect. Defaults to {@link StandardLocalCSS} —
         * Variants tweak custom-property tokens on top of this rather than
         * swapping the stylesheet, so the obfuscated class names stay
         * stable across variants. Custom callers can still substitute a
         * fully different ILocalCSS via {@link #styles(ILocalCSS)}.
         */
        private ILocalCSS styles = StandardLocalCSS.instance ();

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
         * See {@link #nowrap(boolean)}.
         */
        private boolean nowrap;

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
         * Construct with the default style. Honours
         * {@link MultiCheckControl#DEFAULT_STYLE} if a non-standard one
         * has been globally set.
         */
        public Config() {
            super ();
            if (DEFAULT_STYLE != null)
                DEFAULT_STYLE.configure (this);
        }

        /**
         * Construct with a specific (legacy) style.
         *
         * @param style the style.
         * @deprecated use {@link #variant(Variant)} or set the styles
         * directly via {@link #styles(ILocalCSS)}.
         */
        @Deprecated
        public Config(Style style) {
            super ();
            if (style != null)
                style (style);
            else if (DEFAULT_STYLE != null)
                DEFAULT_STYLE.configure (this);
        }

        /**
         * Apply a {@link Variant} to this configuration.
         *
         * @param variant the variant (no-op if {@code null}).
         * @return this configuration instance.
         */
        public Config<V> variant(Variant variant) {
            if (variant != null)
                variant.configure (this);
            return this;
        }

        /**
         * Substitute the {@link ILocalCSS} stylesheet used by the control.
         * Most callers should use {@link #variant(Variant)} instead — the
         * built-in variants tune the standard stylesheet via tokens.
         *
         * @param styles the stylesheet (no-op if {@code null}).
         * @return this configuration instance.
         */
        public Config<V> styles(ILocalCSS styles) {
            if (styles != null)
                this.styles = styles;
            return this;
        }

        /**
         * The active stylesheet. Used by the control's
         * {@link MultiCheckControl#styles()} accessor.
         */
        public ILocalCSS getStyles() {
            return styles;
        }

        /**
         * Assigns an alternative style.
         *
         * @param style the style.
         * @return this configuration instance.
         * @deprecated use {@link #variant(Variant)}.
         */
        @Deprecated
        public Config<V> style(Style style) {
            if (style != null)
                style.configure (this);
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
         * Convenience for passing {@code true} through to {@link #nowrap(boolean)}.
         */
        public Config<V> nowrap() {
            return nowrap (true);
        }

        /**
         * Prevents the option labels from wrapping. Useful when the control
         * sits inside a constrained-width column and the labels would
         * otherwise break across two lines.
         *
         * @param nowrap {@code true} to apply {@code white-space: nowrap}
         *               to the option labels.
         * @return this configuration instance.
         */
        public Config<V> nowrap(boolean nowrap) {
            this.nowrap = nowrap;
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
                    if (config().nowrap)
                        item.style (styles ().nowrap ());
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
        return config ().getStyles ();
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

        public String nowrap();

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
}
