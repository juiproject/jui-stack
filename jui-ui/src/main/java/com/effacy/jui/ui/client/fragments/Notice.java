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

import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Decimal;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;

/**
 * Displays a notice consisting of an icon, message and optional child content.
 * The notice is offset (colourwise) so as to be distinct.
 */
public class Notice {

    public static NoticeFragment $() {
        return new NoticeFragment ();
    }

    public static NoticeFragment $(IDomInsertableContainer<?> parent) {
        NoticeFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    /**
     * The visual style of the notice.
     */
    public enum Style {
        STANDARD, BORDER, INLINE;
    }

    /**
     * Variations in intent for the notice (affects the colour and default icon).
     */
    public enum Variant {
        STANDARD, DANGER, WARNING, SUCCESS;
    }

    /**
     * Icon to use for {@link Variant#STANDARD}.
     */
    public static String ICON_STANDARD = FontAwesome.flag ();

    /**
     * Icon to use for {@link Variant#DANGER}.
     */
    public static String ICON_DANGER = FontAwesome.bug ();
    
    /**
     * Icon to use for {@link Variant#WARNING}.
     */
    public static String ICON_WARNING = FontAwesome.warning ();
    
    /**
     * Icon to use for {@link Variant#SUCCESS}.
     */
    public static String ICON_SUCCESS = FontAwesome.circleCheck ();

    /**
     * Icon to use for {@link Variant#STANDARD}.
     */
    public static class NoticeFragment extends ANoticeFragment<NoticeFragment> {}

    /**
     * Implementation class for extension.
     */
    public static class ANoticeFragment<T extends ANoticeFragment<T>> extends BaseFragmentWithChildren<T> {

        /**
         * See {@link #style(Style)}.
         */
        protected Style style = Style.STANDARD;

        /**
         * See {@link #variant(Variant)}.
         */
        protected Variant variant = Variant.STANDARD;

        /**
         * See {@link #icon(String)}.
         */
        protected String icon;

        /**
         * See {@link #icon(boolean)}.
         */
        protected boolean noIcon = false;

        /**
         * See {@link #size(Length)}.
         */
        protected Length size;

        /**
         * See {@link #width(Length)}.
         */
        protected Length width;

        /**
         * See {@link #message(String)}.
         */
        protected String message;

        /**
         * See {@link #contentAligned(boolean)}.
         */
        protected boolean contentAligned;

        /**
         * See {@link #weight(int)}.
         */
        protected int weight;

        /**
         * Assigns a style to the notice.
         * 
         * @param style
         *                the style.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T style(Style style) {
            if (style != null)
                this.style = style;
            return (T) this;
        }

        /**
         * Assigns a variant to the notice.
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
         * Overrides the standard icon.
         * 
         * @param icon
         *             the icon CSS to apply (see, for example, {@link FontAwesome}).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T icon(String icon) {
            this.icon = icon;
            return (T) this;
        }
        
        /**
         * Determines if the icon should be suppressed.
         * 
         * @param noIcon
         *               {@code true} if to suppress the icon.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T icon(boolean icon) {
            this.noIcon = !icon;
            return (T) this;
        }

        /**
         * Sizes the overall notice (i.e. applies a font size).
         * 
         * @param size
         *             the size (generally apply as an em).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T size(Length size) {
            this.size = size;
            return (T) this;
        }

        /**
         * See {@link #contentAligned(boolean)}. Convenience that passed {@code true}.
         */
        public T contentAligned() {
            return contentAligned (true);
        }

        /**
         * Determines if any child content should appear along with the message aligned
         * with the icon.
         * 
         * @param contentAligned
         *                           {@code true} if with message.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T contentAligned(boolean contentAligned) {
            this.contentAligned = contentAligned;
            return (T) this;
        }

        /**
         * Assigns a width to the notice.
         * 
         * @param width
         *                the width (the default is no width).
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T width(Length width) {
            this.width = width;
            return (T) this;
        }

        /**
         * Assigns a message to display.
         * 
         * @param message
         *                the message.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T message(String message) {
            this.message = message;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T weight(int weight) {
            this.weight = weight;
            return (T) this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            root.style ("juiNotice", "variant-" + variant.name ().toLowerCase (), "style-" + style.name ().toLowerCase());
            if (size != null)
                root.css (CSS.FONT_SIZE, size);
            if (width != null)
                root.css (CSS.WIDTH, width);
            Div.$ (root).$ (main -> {
                main.style ("main");
                if (!noIcon) {
                    if (!StringSupport.empty (icon))
                        Em.$ (main).style (icon);
                    else if (Variant.STANDARD == variant)
                        Em.$ (main).style (ICON_STANDARD);
                    else if (Variant.WARNING == variant)
                        Em.$ (main).style (ICON_WARNING);
                    else if (Variant.DANGER == variant)
                        Em.$ (main).style (ICON_DANGER);
                    else if (Variant.SUCCESS == variant)
                        Em.$ (main).style (ICON_SUCCESS);
                }
                Div.$ (main).$ (content -> {
                    if (!StringSupport.empty (message)) {
                        P.$ (content).style ("message").$ (msg -> {
                            if (weight > 0) {
                                if (weight < 10)
                                    weight *= 100;
                                msg.css (CSS.FONT_WEIGHT, Decimal.of (weight));
                            }
                            Text.$ (msg, message);
                        });
                    }
                    if (contentAligned)
                        super.buildInto (content);
                });
            });
            if (!contentAligned)
                super.buildInto (root);
        }
    }
}
