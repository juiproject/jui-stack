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
package com.effacy.jui.ui.client.fragments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;
import elemental2.dom.HTMLButtonElement;

/**
 * Simple button fragment.
 * <p>
 * This doesn't have the sophistication of the {@link Button} component but does
 * have a range of styling and the ability to act on a click.
 */
public class Btn {

    public static BtnFragment $(String label) {
        return new BtnFragment (label);
    }

    public static BtnFragment $(IDomInsertableContainer<?> parent, String label) {
        BtnFragment frg = $ (label);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * The visual form that the button takes.
     */
    public interface Variant {

        /**
         * Standard button presentation.
         */
        public static final Variant STANDARD = Variant.create("variant-standard");

        /**
         * Standard button presentation that is rounded.
         */
        public static final Variant STANDARD_ROUNDED = Variant.create("variant-standard variant-rounded");
        
        /**
         * Same as {@link #STANDARD} but expands the padding.
         */
        public static final Variant STANDARD_EXPANDED = Variant.create("variant-standard variant-expanded");
        
        /**
         * Same as {@link #STANDARD_EXPANDED} but is rounded.
         */
        public static final Variant STANDARD_EXPANDED_ROUNDED = Variant.create("variant-standard variant-expanded variant-rounded");
        
        /**
         * Draws with an outline.
         */
        public static final Variant OUTLINED = Variant.create("variant-outlined");

        /**
         * Same as {@link #OUTLINED} but is rounded.
         */
        public static final Variant OUTLINED_ROUNDED = Variant.create("variant-outlined variant-rounded");
        
        /**
         * Text only (link-like).
         */
        public static final Variant TEXT = Variant.create("variant-text");
        
        /**
         * Same as {@link #TEXT} but is compact (no padding).
         */
        public static final Variant TEXT_COMPACT = Variant.create("variant-text variant-compact");

        /**
         * A CSS class to apply in addition.
         */
        public String style();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Variant create(String style) {
            return new Variant() {
                public String style() { return style; }
            };
        }
    }

    /**
     * Various colour schemes that are phrased in the language of use.
     */
    public interface Nature {

        public static final Nature NORMAL = Nature.create("nature-normal");
        public static final Nature WARNING = Nature.create("nature-warning");
        public static final Nature DANGER = Nature.create("nature-danger");
        public static final Nature SUCCESS = Nature.create("nature-success");
        public static final Nature GREY = Nature.create("nature-grey");

        /**
         * A CSS class to apply in addition.
         */
        public String style();

        /**
         * Convenience to create an instance of a variant.
         */
        public static Nature create(String style) {
            return new Nature() {
                public String style() { return style; }
            };
        }
    }

    /**
     * Fragment implementation.
     */
    public static class BtnFragment extends BaseFragment<BtnFragment> {

        /**
         * See constructor.
         */
        private String label;

        /**
         * See {@link #variant(Variant)}.
         */
        private Variant variant = Variant.STANDARD;

        /**
         * See {@link #nature(Nature)}.
         */
        private Nature nature = Nature.NORMAL;

        /**
         * See {@link #icon(String)}.
         */
        private String icon;

        /**
         * See {@link #size(Length)}.
         */
        private Length size;

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #onclick(Consumer<IButtonActionCallback>)}.
         */
        private Consumer<IButtonActionCallback> onclick;

        /**
         * See {@link #testId(String)}.
         */
        private String testId;

        /**
         * See {@link #attr(String, String)}.
         */
        private Map<String,String> attributes;

        /**
         * Construct with the label for the button.
         * 
         * @param label
         *             the label.
         */
        public BtnFragment(String label) {
            this.label = label;
        }

        /**
         * The button variant.
         * 
         * @param variant
         *              the variant to apply.
         * @return the fragment instance.
         */
        public BtnFragment variant(Variant variant) {
            if (variant != null)
                this.variant = variant;
            return this;
        }

        /**
         * The button nature.
         * 
         * @param nature
         *              the nature to apply.
         * @return the fragment instance.
         */
        public BtnFragment nature(Nature nature) {
            if (nature != null)
                this.nature = nature;
            return this;
        }

        /**
         * Declares an icon to display.
         * 
         * @param icon
         *              the icon CSS to apply.
         * @return the fragment instance.
         */
        public BtnFragment icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * The font size.
         * 
         * @param size
         *              the size to apply.
         * @return the fragment instance.
         */
        public BtnFragment size(Length size) {
            this.size = size;
            return this;
        }

        /**
         * The width.
         * 
         * @param width
         *              the width to apply.
         * @return the fragment instance.
         */
        public BtnFragment width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Adds an attribute to add to the root element.
         * 
         * @param name
         *              the name of the attribute.
         * @param value
         *              the value of the attribute.
         * @return the fragment instance.
         */
        public BtnFragment attr(String name, String value) {
            if (attributes == null)
                attributes = new HashMap<>();
            if (value == null)
                attributes.remove(name);
            else
                attributes.put(name, value);
            return this;
        }

        /**
         * Adds an on-click handler to the icon.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public BtnFragment onclick(Invoker onclick) {
            this.onclick = (cb -> {
                onclick.invoke();
                cb.complete();
            });
            return this;
        }

        /**
         * Adds an on-click handler to the icon.
         * 
         * @param onclick
         *                the handler.
         * @return this icon instance.
         */
        public BtnFragment onclick(Consumer<IButtonActionCallback> onclick) {
            this.onclick = onclick;
            return this;
        }

        /**
         * Assigns a test ID to the action.
         * 
         * @param testId
         *                test ID.
         * @return this icon instance.
         */
        public BtnFragment testId(String testId) {
            this.testId = testId;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (label == null)
                return null;
            ElementBuilder btn = com.effacy.jui.core.client.dom.builder.Button.$ (parent);
            if (attributes != null)
                attributes.forEach((k,v) -> btn.attr(k, v));
            if (!StringSupport.empty(icon))
                Em.$ (btn).style (icon);
            if (testId != null)
                btn.testId (testId);
            btn.$(
                Span.$().style("running").$(
                    Em.$().style(FontAwesome.spinner(FontAwesome.Option.SPIN))
                ),
                Span.$().style("label").text (label)
            );
            btn.style ("juiButton", variant.style(), nature.style());
            if (size != null)
                btn.css (CSS.FONT_SIZE, size);
            if (width != null) {
                btn.css (CSS.WIDTH, width);
                btn.style("left");
            }
            if (onclick != null) {
                btn.onclick ((e, n) -> {
                    ((HTMLButtonElement)n).disabled = true;
                    ((Element)n).classList.add("running");
                    onclick.accept(() -> {
                        ((Element)n).classList.remove("running");
                        ((HTMLButtonElement)n).disabled = false;
                    });
                });
            }
            return btn;
        }

    }
}

