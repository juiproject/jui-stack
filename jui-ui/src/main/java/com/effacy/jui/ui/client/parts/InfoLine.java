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
package com.effacy.jui.ui.client.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.ProviderBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem;
import com.effacy.jui.core.client.dom.renderer.template.items.BuilderItemLooper;
import com.effacy.jui.core.client.observable.IObservable;
import com.effacy.jui.core.client.util.UID;

/**
 * Represents a single line of informational items.
 *
 * @author Jeremy Buckley
 */
public class InfoLine<D> extends BuilderItem<D> {

    public class InfoItem<A> extends BuilderItem<A> {

        private Provider<String, A> content;

        private Provider<String, A> icon;

        private Consumer<IObservable> link;

        private String uid;

        public InfoItem<A> content(String content) {
            return content (ProviderBuilder.string (content));
        }

        public InfoItem<A> content(Provider<String, A> content) {
            this.content = content;
            return this;
        }

        public InfoItem<A> icon(String icon) {
            return icon (ProviderBuilder.string (icon));
        }

        public InfoItem<A> icon(Provider<String, A> icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Makes the content clickable and invokes the passed handler when clicked on.
         * 
         * @param link
         *             the link handler to invoke.
         * @return this information item.
         */
        public InfoItem<A> link(Consumer<IObservable> link) {
            this.link = link;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
         */
        @Override
        protected Node<A> buildImpl(Container<A> parent) {
            return parent.div (item -> {
                item.addClassName (styles.info_line_item ());
                if (icon != null) {
                    item.em (ico -> {
                        ico.condition (icon);
                        ico.addClassName (icon);
                    });
                }
                if (content != null) {
                    if (link != null) {
                        uid = UID.createUID ();
                        item.a (cnt -> {
                            cnt.condition (content);
                            cnt.text (content);
                            cnt.id ("action");
                            cnt.setAttribute ("item", uid);
                            cnt.on ((e, n) -> link.accept (e.getSource ()), UIEventType.ONCLICK);
                        });
                    } else {
                        item.span (cnt -> {
                            cnt.condition (content);
                            cnt.text (content);
                        });
                    }
                }
            });
        }

    }

    /**
     * Styles instance to use.
     */
    protected IInfoLineCSS styles;

    /**
     * Info items for display.
     */
    protected List<BuilderItem<D>> items = new ArrayList<> ();

    /**
     * Construct instance of the builder item.
     * 
     * @param styles
     *               the style to use.
     */
    public InfoLine(IInfoLineCSS styles) {
        this.styles = styles;
    }

    /**
     * Creates a single item for display.
     * 
     * @return the item.
     */
    public InfoItem<D> item() {
        InfoItem<D> item = new InfoItem<D> ();
        items.add (item);
        return item;
    }

    /**
     * Creates a single item for display.
     * 
     * @param configurer
     *                   (optional) to configure the item.
     * @return this info-line instance.
     */
    public InfoLine<D> item(Consumer<InfoItem<D>> configurer) {
        InfoItem<D> item = item ();
        if (configurer != null)
            configurer.accept (item);
        return this;
    }

    /**
     * Creates a collection of items from a source.
     * 
     * @param <B>
     *               the data type of the looped over items
     * @param looper
     *               obtains a list of children to itemise.
     * @return the item.
     */
    public <B> InfoItem<B> item(Provider<List<B>, D> looper) {
        InfoItem<B> item = new InfoItem<> ();
        BuilderItemLooper<B, D> loopedItem = new BuilderItemLooper<B, D> (item, looper);
        items.add (loopedItem);
        return item;
    }

    public <B> InfoLine<D> item(Provider<List<B>, D> looper, Consumer<InfoItem<B>> configurer) {
        InfoItem<B> item = item (looper);
        if (configurer != null)
            configurer.accept (item);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.template.items.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container)
     */
    @Override
    protected Node<D> buildImpl(Container<D> parent) {
        return parent.div (line -> {
            line.addClassName (styles.info_line ());
            items.forEach (item -> item.build (line));
        });
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    public interface IInfoLineCSS {

        public String info_line();

        public String info_line_item();

    }
}
