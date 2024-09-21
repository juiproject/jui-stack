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

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.platform.util.client.StringSupport;

public class CardHeader {

    public static CardHeaderFragment $() {
        return new CardHeaderFragment ();
    }

    public static CardHeaderFragment $(IDomInsertableContainer<?> parent) {
        CardHeaderFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    public static class CardHeaderFragment extends ACardHeaderFragment<CardHeaderFragment> {}

    public static class ACardHeaderFragment<T extends ACardHeaderFragment<T>> extends BaseFragment<T> {

        protected Invoker onclick;

        protected String icon;

        protected String iconCss;

        protected String title;

        protected String titleCss;

        protected Consumer<ElementBuilder> titleBuilder;

        protected String subtitle;
        
        protected Consumer<ElementBuilder> subtitleBuilder;

        protected String subtitleCss;
        

        /**
         * Assigns a click handler for the card.
         * 
         * @param onclick
         *                the click handler.
         * @return this fragment.
         */
        @SuppressWarnings("unchecked")
        public T onclick(Invoker onclick) {
            this.onclick = onclick;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T onclick(Invoker onclick, boolean condition) {
            if (condition)
                this.onclick = onclick;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T icon(String icon) {
            this.icon = icon;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T icon(Supplier<String> icon) {
            this.icon = icon.get ();
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T iconCss(String iconCss) {
            this.iconCss = iconCss;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T iconCss(Supplier<String> iconCss) {
            this.iconCss = iconCss.get ();
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T title(String title) {
            this.title = title;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T title(Supplier<String> title) {
            this.title = title.get ();
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T titleCss(String titleCss) {
            this.titleCss = titleCss;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T titleCss(Supplier<String> titleCss) {
            this.titleCss = titleCss.get ();
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T titleBuilder(Consumer<ElementBuilder> titleBuilder) {
            this.titleBuilder = titleBuilder;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T subtitle(String subtitle) {
            this.subtitle = subtitle;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T subtitle(Supplier<String> subtitle) {
            this.subtitle = subtitle.get ();
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T subtitleBuilder(Consumer<ElementBuilder> subtitleBuilder) {
            this.subtitleBuilder = subtitleBuilder;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T subtitleCss(String subtitleCss) {
            this.subtitleCss = subtitleCss;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T subtitleCss(Supplier<String> subtitleCss) {
            this.subtitleCss = subtitleCss.get ();
            return (T) this;
        }

        @Override
        protected void buildInto(ElementBuilder root) {
            if ((title == null) && (titleBuilder == null))
                title = "MISSING TITLE";
            root.style ("juiCardHeader");
            if (!StringSupport.empty (icon))
                Em.$ (root).style (icon).css (iconCss);
            Div.$ (root).style ("juiCardHeader_inner").$ (inner -> {
                if (onclick != null) {
                    A.$ (inner).style ("juiCardHeader_title").css (titleCss).$ (t -> {
                        if (titleBuilder != null)
                            titleBuilder.accept(t);
                        else
                            Text.$ (t, title);
                    }).onclick (e -> onclick.invoke ());
                } else {
                    Div.$ (inner).style ("juiCardHeader_title").css (titleCss).$ (t -> {
                        if (titleBuilder != null)
                            titleBuilder.accept(t);
                        else
                            Text.$ (t, title);
                    });
                }
                if (subtitleBuilder != null)
                    Div.$(inner).style ("juiCardHeader_subtitle").css (subtitleCss).$ (st -> subtitleBuilder.accept(st));
                else if (!StringSupport.empty (subtitle))
                    Div.$(inner).style ("juiCardHeader_subtitle").css (subtitleCss).$ (Text.$ (subtitle));
            });
            super.buildInto(root);
        }
    }
    
}

