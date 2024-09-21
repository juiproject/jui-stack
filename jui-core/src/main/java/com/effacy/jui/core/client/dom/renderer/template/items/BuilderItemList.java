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
package com.effacy.jui.core.client.dom.renderer.template.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.effacy.jui.core.client.dom.renderer.template.Condition;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;

/**
 * A collection of builder items. This will be built one after the other into
 * the parent.
 */
public class BuilderItemList<A> extends BuilderItem<A> implements Iterable<BuilderItem<?>> {

    /**
     * The items held in the collection.
     */
    private List<BuilderItem<?>> items = new ArrayList<BuilderItem<?>> ();

    /**
     * Construct an instance of a list of items.
     */
    public BuilderItemList() {
        this (false);
    }

    /**
     * Construct.
     * 
     * @param emptyCondition
     *                       {@code true} to default a condition that tests each of
     *                       the items for their conditions.
     */
    public BuilderItemList(boolean emptyCondition) {
        if (emptyCondition) {
            condition (new Condition<A> () {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public boolean test(A data) {
                    if (items.isEmpty ())
                        return false;
                    for (BuilderItem item : items) {
                        if (item.test (data))
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * The number of items.
     * 
     * @return the number.
     */
    public int size() {
        return items.size ();
    }

    /**
     * Adds an item to the collection.
     * 
     * @param item
     *             the item to add.
     * @return the added item.
     */
    public <J extends BuilderItem<A>> J add(J item) {
        items.add (item);
        return item;
    }

    /**
     * Convenience method to add an item to the collection with a condition.
     * 
     * @param item
     *                  the item to add.
     * @param condition
     *                  the condition underwhich the item will be rendered.
     * @return the added item.
     */
    public <J extends BuilderItem<A>> J add(J item, Condition<A> condition) {
        return add (item).condition (condition);
    }

    /**
     * Convenience to add a builder item that loops over some sub-set of the base
     * data type.
     * 
     * @param item
     *               the item over the sub-set type.
     * @param looper
     *               the looper that yields a list of content.
     * @return the passed item.
     */
    public <B, J extends BuilderItem<B>> J loop(J item, Provider<List<B>, A> looper) {
        items.add (new BuilderItemLooper<B, A> (item, looper));
        return item;
    }

    /**
     * Convenience to add a builder item that loops over some sub-set of the base
     * data type.
     * 
     * @param item
     *                  the item over the sub-set type.
     * @param looper
     *                  the looper that yields a list of content.
     * @param condition
     *                  the condition underwhich the item will be rendered.
     * @return the passed item.
     */
    @SuppressWarnings("unchecked")
    public <B, J extends BuilderItem<B>> J loop(BuilderItem<B> item, Provider<List<B>, A> looper, Condition<A> condition) {
        items.add (new BuilderItemLooper<B, A> (item, looper).condition (condition));
        return (J) item;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.DomTemplateDataRenderer.Container)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Node<A> buildImpl(Container<A> parent) {
        for (BuilderItem item : items)
            item.build (parent);
        return parent;
    }

    /**
     * If there are not items in the collection.
     * 
     * @return {@code true} if there are none.
     */
    public boolean isEmpty() {
        return items.isEmpty ();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<BuilderItem<?>> iterator() {
        return items.iterator ();
    }

}
