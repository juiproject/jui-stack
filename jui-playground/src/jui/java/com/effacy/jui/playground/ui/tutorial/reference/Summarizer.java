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
package com.effacy.jui.playground.ui.tutorial.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.TemplateComponent;
import com.effacy.jui.core.client.dom.renderer.template.ConditionBuilder;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Element;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItemLooper;

/**
 * A general component for building a summary display consisting of a header
 * (title and sub-title) and an arbitrary number of sections represented as cards
 * that contain a title (and sub-title), pie chart and quantity.
 * <p>
 * In general this type of component probably is not so generic as to justify
 * this treatment (i.e. could be built using a {@link DomBuilder} rather than a
 * template) but does illustrate how templates can be employed.
 *
 * @author Jeremy Buckley
 */
public class Summarizer<D> extends TemplateComponent<D, Component.Config> {

    /**
     * The builder to configure the items.
     */
    private Consumer<SummarizerItem> builder;

    /**
     * Construct with a configuration builder.
     * 
     * @param builder
     *                the builder.
     */
    public Summarizer(Consumer<SummarizerItem> builder) {
        super (new Component.Config ());
        this.builder = builder;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.TemplateComponent#template()
     */
    @Override
    protected ITemplateBuilder<D> template() {
        SummarizerItem item = new SummarizerItem ();
        builder.accept (item);
        return item;
    }

    /*******************************************************************************
     * Builder items for the summarizer.
     *******************************************************************************/

    public class SummarizerItem extends BuilderItem<D> {

        /**
         * See {@link #title(Provider)}.
         */
        private Provider<String, D> title;

        /**
         * See {@link #subtitle(Provider)}.
         */
        private Provider<String, D> subtitle;

        /**
         * See {@link #section(Provider, Consumer)}.
         */
        private List<BuilderItem<D>> items = new ArrayList<> ();

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
         */
        @Override
        protected Node<D> buildImpl(Container<D> parent) {
            Element<D> node = parent.div ();
            node.addClassName ("summarizer");
            if (title != null) {
                node.div (header -> {
                    header.addClassName ("summarizer_header");
                    header.h3 ().text (title);
                    if (subtitle != null)
                        header.p ().text (subtitle);
                });
            }
            items.forEach (item -> item.build (node));
            return node;
        }

        public SummarizerItem title(Provider<String, D> title) {
            this.title = title;
            return this;
        }

        public SummarizerItem subtitle(Provider<String, D> subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public SummarizerItem section(Consumer<SectionItem<D>> config) {
            SectionItem<D> section = new SectionItem<D> ();
            config.accept (section);
            items.add (section);
            return this;
        }

        public <A> SummarizerItem section(Provider<List<A>, D> looper, Consumer<SectionItem<A>> config) {
            SectionItem<A> section = new SectionItem<A> ();
            config.accept (section);
            items.add (new BuilderItemLooper<A, D> (section, looper));
            return this;
        }

        /**
         * Represents a section (card) that contains a title and sub-title to the left,
         * a pie chart in the middle for percentage and a quantity on the right for
         * absolute value.
         * <p>
         * The pie chart is pure CSS as per
         * {@link https://www.freecodecamp.org/news/css-only-pie-chart/}. This takes the
         * <code>--p</code> style variable to define the percentage.
         */
        public class SectionItem<A> extends BuilderItem<A> {

            private Provider<String, A> title;

            private Provider<String, A> subtitle;

            private Provider<Integer, A> percent;

            private Provider<Integer, A> quantity;

            public SectionItem<A> title(Provider<String, A> title) {
                this.title = title;
                return this;
            }

            public SectionItem<A> subtitle(Provider<String, A> subtitle) {
                this.subtitle = subtitle;
                return this;
            }

            public SectionItem<A> percent(Provider<Integer, A> percent) {
                this.percent = percent;
                return this;
            }

            public SectionItem<A> quantity(Provider<Integer, A> quantity) {
                this.quantity = quantity;
                return this;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
             */
            @Override
            protected Node<A> buildImpl(Container<A> parent) {
                Element<A> node = parent.div ();
                node.addClassName ("summarizer_section");
                node.div (div -> {
                    div.addClassName ("summarizer_heading");
                    div.h4 ().text (title);
                    div.p ().condition (ConditionBuilder.createCondition (subtitle)).text (subtitle);
                });
                node.div (div -> {
                    div.addClassName ("summarizer_chart");
                    div.condition (ConditionBuilder.createCondition (percent));
                    div.div (pie -> {
                        pie.addClassName ("summarizer_pie");
                        pie.span ().text (d -> Integer.toString (percent.get (d)));
                        pie.setAttribute ("style", d -> "--p:" + Integer.toString (percent.get (d)));
                    });
                });
                node.div (div -> {
                    div.addClassName ("summarizer_number");
                    div.condition (ConditionBuilder.createCondition (quantity));
                    div.text (d -> Integer.toString (quantity.get (d)));
                });
                return node;
            }

        }

    }

}
