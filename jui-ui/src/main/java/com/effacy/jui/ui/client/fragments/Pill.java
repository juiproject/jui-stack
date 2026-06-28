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

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Fragment.IFragmentVariant;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.IFragmentCSS;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.google.gwt.core.client.GWT;

/**
 * Simple pill (badge) fragment — a small rounded label, optionally with a leading
 * icon, used to convey a status or tag.
 * <p>
 * All presentation flows from the local <code>--frag-pill-*</code> token layer, so a
 * design can retune every pill at once and individual {@link Variant variants} reach
 * in to override colour (and supply a relevant icon). The icon is optional: the plain
 * {@link Variant#NEUTRAL} variant carries none, the semantic variants supply one, and
 * {@link PillFragment#icon(String)} sets or clears it explicitly.
 */
public class Pill {

    public static PillFragment $(String label) {
        return new PillFragment (label);
    }

    public static PillFragment $(IDomInsertableContainer<?> parent, String label) {
        PillFragment frg = $ (label);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * The visual form a pill takes — a colour scheme and (optionally) a default icon.
     */
    public interface Variant extends IFragmentVariant<PillFragment> {

        /**
         * Neutral grey pill with no icon (the default).
         */
        public static final Variant NEUTRAL = fragment -> {
        };

        /**
         * Quiet outlined pill — a surface with a subtle border and muted text, no icon.
         * Suited to a tag or version marker.
         */
        public static final Variant OUTLINE = fragment -> fragment.css("""
            --frag-pill-bg: var(--jui-color-aux-white);
            --frag-pill-border: var(--jui-color-neutral30);
            --frag-pill-text: var(--jui-color-neutral60);
        """);

        /**
         * Informational pill (accent blue) with an info icon.
         */
        public static final Variant INFO = fragment -> fragment.css("""
            --frag-pill-bg: var(--jui-color-info10);
            --frag-pill-text: var(--jui-color-info70);
        """).icon(FontAwesome.circleInfo());

        /**
         * Success pill (green) with a tick icon.
         */
        public static final Variant SUCCESS = fragment -> fragment.css("""
            --frag-pill-bg: var(--jui-color-success10);
            --frag-pill-text: var(--jui-color-success70);
        """).icon(FontAwesome.circleCheck());

        /**
         * Warning pill (amber) with a warning icon.
         */
        public static final Variant WARNING = fragment -> fragment.css("""
            --frag-pill-bg: var(--jui-color-warning10);
            --frag-pill-text: var(--jui-color-warning70);
        """).icon(FontAwesome.triangleExclamation());

        /**
         * Danger pill (red) with an error icon.
         */
        public static final Variant DANGER = fragment -> fragment.css("""
            --frag-pill-bg: var(--jui-color-error10);
            --frag-pill-text: var(--jui-color-error70);
        """).icon(FontAwesome.circleExclamation());
    }

    /**
     * Fragment implementation.
     */
    public static class PillFragment extends BaseFragment<PillFragment> {

        /**
         * See constructor.
         */
        private String label;

        /**
         * See {@link #icon(String)}.
         */
        private String icon;

        /**
         * See {@link #testId(String)}.
         */
        private String testId;

        /**
         * See {@link #attr(String, String)}.
         */
        private Map<String,String> attributes;

        /**
         * Construct with the pill's label.
         *
         * @param label
         *              the label.
         */
        public PillFragment(String label) {
            this.label = label;
            variant(Variant.NEUTRAL);
        }

        /**
         * The pill variant.
         *
         * @param variant
         *                the variant to apply.
         * @return the fragment instance.
         */
        public PillFragment variant(Variant variant) {
            return variant((IFragmentVariant<PillFragment>) variant);
        }

        /**
         * The leading icon (a CSS class, e.g. from {@link FontAwesome}). Pass
         * {@code null} to clear an icon supplied by a variant.
         *
         * @param icon
         *             the icon CSS to apply.
         * @return the fragment instance.
         */
        public PillFragment icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Adds an attribute to the root element.
         *
         * @param name
         *              the name of the attribute.
         * @param value
         *              the value of the attribute (or {@code null} to remove).
         * @return the fragment instance.
         */
        public PillFragment attr(String name, String value) {
            if (attributes == null)
                attributes = new HashMap<>();
            if (value == null)
                attributes.remove(name);
            else
                attributes.put(name, value);
            return this;
        }

        /**
         * Assigns a test ID to the pill.
         *
         * @param testId
         *               the test ID.
         * @return the fragment instance.
         */
        public PillFragment testId(String testId) {
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
            ElementBuilder pill = Span.$ (parent);
            pill.style(styles().fragment());
            if (attributes != null)
                attributes.forEach((k,v) -> pill.attr(k, v));
            if (testId != null)
                pill.testId (testId);
            if (!StringSupport.empty(icon))
                Em.$ (pill).style (styles().icon(), icon);
            Span.$ (pill).text (label);
            return pill;
        }

    }

    public static interface ILocalCSS extends IFragmentCSS {

        String icon();
    }

    @CssResource({
        "com/effacy/jui/ui/client/fragments/Pill.css",
        "com/effacy/jui/ui/client/fragments/Pill_Override.css"
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
