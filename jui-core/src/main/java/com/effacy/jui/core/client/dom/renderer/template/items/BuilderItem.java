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

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.renderer.template.Condition;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.Provider;
import com.effacy.jui.core.client.dom.renderer.template.Condition.NotEmptyCondition;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node;

/**
 * A convenience class for implementing a builder framework. This allows a
 * pre-defined set of {@link BuilderItem}'s to be configured programmatically.
 * These items (once configured) will build a suitable set of nodes for use with
 * {@link DomTemplateBuilder}.
 * <p>
 * The intention is that one sub-classes this class for each component of the
 * overall structure. These component can include other components that are then
 * assembled with {@link Provider}'s and {@link Condition}'s against a data
 * type. When need the builder will insert children into a parent {@link Node}
 * template.
 */
public abstract class BuilderItem<V> implements ITemplateBuilder<V> {

    /**
     * See {@link #condition(Condition)}.
     */
    private Condition<V> condition;

    /**
     * Adds a condition under which the item will be rendered. This is passed
     * through to the generated node.
     * 
     * @param condition
     *                  the condition to assign.
     * @return this item.
     */
    @SuppressWarnings("unchecked")
    public <B extends BuilderItem<V>> B condition(Condition<V> condition) {
        this.condition = condition;
        return (B) this;
    }

    /**
     * Adds a condition under which the item will be rendered. This is passed
     * through to the generated node.
     * 
     * @param condition
     *                  the condition to assign.
     * @return this item.
     */
    @SuppressWarnings("unchecked")
    public <B extends BuilderItem<V>> B conditionNoEmpty(Provider<?, V> provider) {
        this.condition = new NotEmptyCondition<V> (provider);
        return (B) this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder#build(Container)
     */
    @Override
    public void build(Container<V> parent) {
        Node<V> node = buildImpl (parent);
        if (condition != null)
            node.condition (condition);
    }

    /**
     * Builds the node structure in the parent (should be inserted as the last node
     * in the parent) for this item.
     * 
     * @param parent
     *               the parent to insert into.
     * @return the root node of the stucture.
     */
    protected abstract Node<V> buildImpl(Container<V> parent);

    /**
     * Exposes the underlying condition.
     * 
     * @param data
     *             the data.
     * @return the outcome if the condition.
     */
    public boolean test(V data) {
        if (condition == null)
            return true;
        return condition.test (data);
    }

    /**
     * A convenience to work with with some object without having to maintain a
     * reference to the object.
     * 
     * @param <T>
     *                 the type of the object.
     * @param obj
     *                 the object.
     * @param consumer
     *                 the lambda to process the object.
     * @return the passed object.
     */
    protected <T> T with(T obj, Consumer<T> consumer) {
        consumer.accept (obj);
        return obj;
    }

}
