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
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;

/**
 * A collection of builder items. This will be built one after the other into
 * the parent.
 * <p>
 * Preference is to use {@link BuilderItemList} unless there is a specific need
 * to have a definitive builder item that is collected over.
 */
public class BuilderItems<A, I extends BuilderItem<A>> extends BuilderItem<A> implements Iterable<I> {

    /**
     * The items held in the collection.
     */
    private List<I> items = new ArrayList<I> ();

    /**
     * See {@link #startIndex(int)}.
     */
    private int startIndex;

    /**
     * Construct an instance of a list of items.
     */
    public BuilderItems() {
        this (false);
    }

    /**
     * Construct.
     * 
     * @param emptyCondition
     *                       {@code true} to default a condition that tests each of
     *                       the items for their conditions.
     */
    public BuilderItems(boolean emptyCondition) {
        if (emptyCondition) {
            condition (new Condition<A> () {

                @Override
                public boolean test(A data) {
                    if (items.isEmpty ())
                        return false;
                    for (I item : items) {
                        if (item.test (data))
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Assigns a start index for iteration. This allows one to skip the first items
     * (for example to select out the first item for special treatment).
     * 
     * @param startIndex
     *                   the starting index.
     * @return this instance.
     */
    public BuilderItems<A, I> startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Retrieves the item at the given index.
     * 
     * @param idx
     *            the index.
     * @return the item (or {@code null}).
     */
    public I get(int idx) {
        if ((idx < 0) || (idx >= items.size ()))
            return null;
        return items.get (idx);
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
    public <J extends I> J add(J item) {
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
    public <J extends I> J add(J item, Condition<A> condition) {
        return add (item).condition (condition);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.BuilderItem#buildImpl(com.effacy.jui.core.client.dom.renderer.DomTemplateDataRenderer.Container)
     */
    @Override
    public Node<A> buildImpl(Container<A> parent) {
        for (I item : items)
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
    public Iterator<I> iterator() {
        if (startIndex > 0)
            return items.subList (startIndex, items.size ()).iterator ();
        return items.iterator ();
    }

}
