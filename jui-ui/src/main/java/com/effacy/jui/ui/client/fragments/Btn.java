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
import com.effacy.jui.core.client.dom.builder.IFragmentCSS;
import com.effacy.jui.core.client.dom.builder.Fragment.IFragmentVariant;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.IButtonHandler.IButtonActionCallback;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

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
    public interface Variant extends IFragmentVariant<BtnFragment> {

        /**
         * Standard button presentation.
         */
        public static final Variant STANDARD = fragment -> {
        };

        /**
         * Expanded padding.
         */
        public static final Variant EXPANDED = fragment -> {
            fragment.css("--frag-btn-padding-lr: 1em;");
        };

        /**
         * Rounded corners.
         */
        public static final Variant ROUNDED = fragment -> {
            fragment.css("--frag-btn-radius: 16px;");
        };

        /**
         * Compact padding.
         */
        public static final Variant COMPACT = fragment -> {
            fragment.css("--frag-btn-padding-lr: 0;");
        };

        /**
         * Standard button presentation that is rounded.
         */
        public static final Variant STANDARD_ROUNDED = fragment -> {
            fragment.variant(STANDARD).variant(ROUNDED);
        };

        /**
         * Same as {@link #STANDARD} but expands the padding.
         */
        public static final Variant STANDARD_EXPANDED = fragment -> {
            fragment.variant(STANDARD).variant(EXPANDED);
        };
        
        /**
         * Same as {@link #STANDARD_EXPANDED} but is rounded.
         */
        public static final Variant STANDARD_EXPANDED_ROUNDED = fragment -> {
            fragment.variant(STANDARD_EXPANDED).variant(ROUNDED);
        };
        
        /**
         * Draws with an outline.
         */
        public static final Variant OUTLINED = fragment -> {
            fragment.css("""
                --frag-btn-text: var(--frag-btn-border);
                --frag-btn-text-hover: var(--jui-color-aux-white);
                --frag-btn-bg: var(--jui-color-aux-white);
                --frag-btn-hover-bg: var(--frag-btn-border);
            """);
        };

        /**
         * Same as {@link #OUTLINED} but is rounded.
         */
        public static final Variant OUTLINED_ROUNDED = fragment -> fragment.variant(OUTLINED).variant(ROUNDED);
        
        /**
         * Text only (link-like).
         */
        public static final Variant TEXT = fragment -> {
            fragment.css("""
                --frag-btn-text: var(--frag-btn-base);
                --frag-btn-text-hover: var(--frag-btn-base);
                --frag-btn-border-width: 0;
                --frag-btn-bg: transparent;
                --frag-btn-bg-hover: transparent;
                --frag-btn-hover-text-decoration: underline;
            """);
        };
        
        /**
         * Same as {@link #TEXT} but is compact (no padding).
         */
        public static final Variant TEXT_COMPACT = fragment -> fragment.variant(TEXT).variant(COMPACT);
    }

    /**
     * Various colour schemes that are phrased in the language of use.
     */
    public interface Nature extends IFragmentVariant<BtnFragment> {

        public static final Nature NORMAL = fragment -> {
        };

        public static final Nature WARNING = fragment -> fragment.css("""
            --frag-btn-base: var(--jui-btn-warning-bg);
            --frag-btn-bg-hover: var(--jui-btn-warning-bg-hover);
        """);
        
        public static final Nature DANGER = fragment -> fragment.css("""
            --frag-btn-base: var(--jui-btn-danger-bg);
            --frag-btn-bg-hover: var(--jui-btn-danger-bg-hover);
            --frag-btn-text-weight: 600;
        """);
        
        public static final Nature SUCCESS = fragment -> fragment.css("""
            --frag-btn-base: var(--jui-btn-success-bg);
            --frag-btn-bg-hover: var(--jui-btn-success-bg-hover);
        """);
        
        public static final Nature GREY = fragment -> fragment.css("""
            --frag-btn-border: var(--jui-color-neutral30);
            --frag-btn-base: var(--jui-color-aux-white);
            --frag-btn-bg-hover: var(--jui-color-neutral05);
            --frag-btn-text: var(--jui-color-neutral60);
            --frag-btn-text-hover: var(--jui-color-neutral60);
        """);
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
            variant(Variant.STANDARD);
            nature(Nature.NORMAL);
        }

        /**
         * The button variant.
         * 
         * @param variant
         *              the variant to apply.
         * @return the fragment instance.
         */
        public BtnFragment variant(Variant variant) {
            return variant((IFragmentVariant<BtnFragment>) variant);
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
                nature.configure(this);
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

        protected ILocalCSS styles() {
            return LocalCSS.instance();
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            if (label == null)
                return null;
            ElementBuilder btn = com.effacy.jui.core.client.dom.builder.Button.$ (parent);
            btn.style(styles().fragment());
            if (attributes != null)
                attributes.forEach((k,v) -> btn.attr(k, v));
            if (!StringSupport.empty(icon))
                Em.$ (btn).style (icon);
            if (testId != null)
                btn.testId (testId);
            btn.$(
                Span.$().style(styles().runningpart()).$(
                    Em.$().style(FontAwesome.spinner(FontAwesome.Option.SPIN))
                ),
                Span.$().style(styles().label()).text (label)
            );
            if (size != null)
                btn.css (CSS.FONT_SIZE, size);
            if (width != null) {
                btn.css (CSS.WIDTH, width);
                btn.style(styles().left());
            }
            if (onclick != null) {
                btn.onclick ((e, n) -> {
                    ((HTMLButtonElement)n).disabled = true;
                    ((Element)n).classList.add(styles().running());
                    onclick.accept(() -> {
                        ((Element)n).classList.remove(styles().running());
                        ((HTMLButtonElement)n).disabled = false;
                    });
                });
            }
            return btn;
        }

    }

    public static interface ILocalCSS extends IFragmentCSS {

        String left();

        String running();

        String runningpart();

        String label();
    }

    @CssResource({
        "com/effacy/jui/ui/client/fragments/Btn.css",
        "com/effacy/jui/ui/client/fragments/Btn_Override.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {

        private static ILocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (ILocalCSS) GWT.create(LocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
