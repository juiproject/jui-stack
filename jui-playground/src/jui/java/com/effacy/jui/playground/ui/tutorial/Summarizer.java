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
package com.effacy.jui.playground.ui.tutorial;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.TemplateComponent;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Element;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem;

/**
 * A started component for one that builds a summary display consisting of a header
 * (title and sub-title) and an arbitrary number of sections represented as cards
 * that contain a title (and sub-title), pie chart and quantity.
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
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
         */
        @Override
        protected Node<D> buildImpl(Container<D> parent) {
            Element<D> node = parent.div ();
            // TODO: Implement this.
            return node;
        }

    }

}

