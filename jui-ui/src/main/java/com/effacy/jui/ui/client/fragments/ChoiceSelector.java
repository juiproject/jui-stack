/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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
package com.effacy.jui.ui.client.fragments;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.IFragmentCSS;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Colors;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

/**
 * Displays a collection of options as selectable items in a row. Good for use
 * in filters.
 * <p>
 * This is not a dynamic component so is expected to be re-rendered when the
 * option changes (i.e. in a state component).
 */
public class ChoiceSelector {

    public static ChoiceSelectorFragment $() {
        return new ChoiceSelectorFragment ();
    }

    public static ChoiceSelectorFragment $(IDomInsertableContainer<?> parent) {
        ChoiceSelectorFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    public static record State(boolean active, boolean disabled) {
        public static State of(boolean active) {
            return new State(active, false);
        }
        public static State of(boolean active, boolean disabled) {
            return new State(active, disabled);
        }
    }

    /**
     * Represents a selectable option.
     *
     * @param label
     *                the display label.
     * @param icon
     *                an optional icon CSS class (i.e. {@link FontAwesome}).
     * @param colors
     *                an optional set of colors to use.
     * @param active
     *                {@code true} if it is activated (i.e. selected).
     * @param handler
     *                to be invoked when the option is selected.
     */
    public static record Option(String label, String icon, Colors colors, State state, Invoker handler) {
        public static Option of(String label, boolean active, Invoker handler) {
            return new Option(label, null, null, State.of(active), handler);
        }
        public static Option of(String label, State state, Invoker handler) {
            return new Option(label, null, null, state, handler);
        }
        public static Option of(String label, Colors color, boolean active, Invoker handler) {
            return new Option(label, null, color, State.of(active), handler);
        }
        public static Option of(String label, Colors color, State state, Invoker handler) {
            return new Option(label, null, color, state, handler);
        }
        public static Option of(String label, Color color, boolean active, Invoker handler) {
            return new Option(label, null, Colors.of(color), State.of(active), handler);
        }
        public static Option of(String label, Color color, State state, Invoker handler) {
            return new Option(label, null, Colors.of(color), state, handler);
        }
        public static Option of(String label, String icon, boolean active, Invoker handler) {
            return new Option(label, icon, null, State.of(active), handler);
        }
        public static Option of(String label, String icon, State state, Invoker handler) {
            return new Option(label, icon, null, state, handler);
        }
        public static Option of(String label, String icon, Colors color, boolean active, Invoker handler) {
            return new Option(label, icon, color, State.of(active), handler);
        }
        public static Option of(String label, String icon, Colors color, State state, Invoker handler) {
            return new Option(label, icon, color, state, handler);
        }
        public static Option of(String label, String icon, Color color, boolean active, Invoker handler) {
            return new Option(label, icon, Colors.of(color), State.of(active), handler);
        }
        public static Option of(String label, String icon, Color color, State state, Invoker handler) {
            return new Option(label, icon, Colors.of(color), state, handler);
        }
    }

    /**
     * Variants configure the selector purely by overriding the
     * {@code --juiChoiceSelector-*} CSS tokens exposed by
     * {@code ChoiceSelector.css}. Custom variants are one-liner lambdas — no
     * additional CSS required.
     */
    public interface Variant extends com.effacy.jui.core.client.dom.builder.Fragment.IFragmentVariant<ChoiceSelectorFragment> {

        /**
         * Standard relief-and-shadow presentation (the CSS defaults).
         */
        public static final Variant STANDARD = fragment -> {};

        /**
         * Flat "toggle group" presentation with dividers between options and no
         * relief / shadow. Suitable when sitting inside a control bar.
         */
        public static final Variant CONTROL = fragment -> fragment.css("""
            --juiChoiceSelector-relief: 0;
            --juiChoiceSelector-option-tb: 0.25em;
            --juiChoiceSelector-text-weight: 600;
            --juiChoiceSelector-option-radius: 0;
            --juiChoiceSelector-text: var(--jui-color-neutral50);
            --juiChoiceSelector-option-divider: 1px solid #e1e1e1;
            --juiChoiceSelector-shadow-selected: none;
        """);

        /**
         * Tight inline "segmented pill" presentation — soft rounded container,
         * no outer border, small options with subtle selected tint. Suitable
         * for embedding in toolbars or card headers where a full STANDARD
         * presentation would be too loud.
         */
        public static final Variant COMPACT = fragment -> fragment.css("""
            --juiChoiceSelector-relief: 3px;
            --juiChoiceSelector-relief-border: transparent;
            --juiChoiceSelector-radius: 8px;
            --juiChoiceSelector-option-radius: 5px;
            --juiChoiceSelector-option-tb: 0.2em;
            --juiChoiceSelector-option-lr: 0.7em;
            --juiChoiceSelector-text-weight: 500;
            --juiChoiceSelector-shadow-selected: none;
        """);

        /**
         * Classic segmented-control presentation — bordered rounded container,
         * options share edges (no relief, no individual radius, no dividers).
         * Selected option fills its slot with a muted tint; unselected options
         * are transparent. Matches the "inline-flex rounded-lg border" pattern
         * common in utility-CSS designs.
         */
        public static final Variant SEGMENTED = fragment -> fragment.css("""
            --juiChoiceSelector-relief: 0;
            --juiChoiceSelector-relief-border: var(--jui-color-neutral20);
            --juiChoiceSelector-radius: 8px;
            --juiChoiceSelector-option-radius: 0;
            --juiChoiceSelector-bg: var(--jui-color-aux-white);
            --juiChoiceSelector-bg-selected: var(--jui-color-neutral10);
            --juiChoiceSelector-bg-hover: var(--jui-color-neutral05);
            --juiChoiceSelector-text: var(--jui-color-neutral50);
            --juiChoiceSelector-text-selected: var(--jui-color-neutral70);
            --juiChoiceSelector-option-tb: 0.25em;
            --juiChoiceSelector-option-lr: 0.7em;
            --juiChoiceSelector-text-weight: 500;
            --juiChoiceSelector-shadow-selected: none;
        """);
    }

    public static class ChoiceSelectorFragment extends AChoiceSelectorFragment<ChoiceSelectorFragment> {}

    public static class AChoiceSelectorFragment<T extends AChoiceSelectorFragment<T>> extends BaseFragment<T> {

        /**
         * See {@link #option(Option)}.
         */
        protected List<Option> options = new ArrayList<>();

        /**
         * See {@link #disable(boolean)}.
         */
        protected boolean disabled;

        /**
         * Adds an option to the choice list.
         *
         * @param option
         *               the option to add.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T option(Option option) {
            if (option != null)
                this.options.add(option);
            return (T) this;
        }

        /**
         * Conditionally adds an option to the choice list.
         *
         * @param condition
         *                  {@code true} if to add.
         * @param option
         *                  the option to add.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T option(boolean condition, Option option) {
            if (condition && (option != null))
                this.options.add(option);
            return (T) this;
        }

        /**
         * Generates a disabled version of the selector. This is a convenience to avoid
         * having to set each option to disabled.
         *
         * @param disabled
         *                 {@code true} to disable.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T disable(boolean disabled) {
            this.disabled = disabled;
            return (T) this;
        }

        /**
         * Expose the CSS resource for extensions.
         */
        protected ILocalCSS styles() {
            return LocalCSS.instance();
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style (styles().fragment());
            options.forEach(option -> {
                Div.$(root).$(op -> {
                    if (option.state().disabled() || disabled) {
                        op.style(styles().disabled());
                    } else {
                        if (option.state().active())
                            op.style(styles().active());
                        if (option.handler() != null)
                            op.onclick(e -> option.handler().invoke());
                    }
                    if (!StringSupport.empty(option.icon()))
                        Em.$(op).style(option.icon());
                    if (option.colors() != null) {
                        if (option.colors().foreground() != null)
                            op.css(CSS.COLOR, option.colors().foreground());
                        if (option.colors().background() != null)
                            op.css(CSS.BACKGROUND_COLOR, option.colors().background());
                    }
                    Span.$(op).text(option.label());
                });
            });
        }

    }

    public static interface ILocalCSS extends IFragmentCSS {

        String active();

        String disabled();
    }

    @CssResource({
        "com/effacy/jui/ui/client/fragments/ChoiceSelector.css",
        "com/effacy/jui/ui/client/fragments/ChoiceSelector_Override.css"
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
