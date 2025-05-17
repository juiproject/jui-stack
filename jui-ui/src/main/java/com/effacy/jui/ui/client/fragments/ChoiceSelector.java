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
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Color;
import com.effacy.jui.core.client.dom.css.Colors;
import com.effacy.jui.platform.util.client.StringSupport;

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
     * Defines a variant of the selector.
     */
    public interface Variant {

        public static final Variant STANDARD = Variant.create("variant-outlined", true);

        public static final Variant CONTROL = Variant.create("variant-control", false);

        public String style();

        public boolean dropshadow();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Variant create(String style, boolean dropshadow) {
            return new Variant() {
                public String style() { return style; }
                public boolean dropshadow() { return dropshadow; }
            };
        }
    }

    public static class ChoiceSelectorFragment extends AChoiceSelectorFragment<ChoiceSelectorFragment> {}

    public static class AChoiceSelectorFragment<T extends AChoiceSelectorFragment<T>> extends BaseFragment<T> {

        /**
         * See {@link #variant(Variant)}.
         */
        protected Variant variant = Variant.STANDARD;

        /**
         * See {@link #option(Option)}.
         */
        protected List<Option> options = new ArrayList<>();

        /**
         * Assigns a variant to the card.
         * 
         * @param variant
         *                the variant.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return (T) this;
        }

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

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiChoiceSelector", variant.style());
            if (variant.dropshadow())
                root.style("dropshadow");
            options.forEach(option -> {
                Div.$(root).$(op -> {
                    if (option.state().disabled()) {
                        op.style("disabled");
                    } else {
                        if (option.state().active())
                            op.style("active");
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
    
}
