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
package com.effacy.jui.core.client.dom.renderer;

import java.util.function.Function;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;

import elemental2.dom.Element;

/**
 * Convenience to create a renderer from a {@link DomBuilder}.
 */
public class DomBuilderDataRenderer<D> implements IDataRenderer<D> {

    /**
     * A function that maps data to a node.
     */
    private Function<D, NodeContext> generator;

    protected DomBuilderDataRenderer() {
        // Nothing.
    }

    public DomBuilderDataRenderer(Function<D, NodeContext> generator) {
        this.generator = generator;
    }

    @Override
    public IUIEventHandler render(Element el, D data) {
        NodeContext ctx = generator.apply (data);
        if (ctx != null) {
            el.append (ctx.node ());
            return ctx;
        }
        return DomBuilder.el (el, root -> generate (root, data)).build ();
    }

    /**
     * Given data this will generate an associated node structure.
     * <p>
     * The default delegates to any generator passed during construction.
     * 
     * @param data
     *             the data to use.
     * @return the node context built from the data.
     */
    protected NodeContext generate(D data) {
        if (generator == null)
            return null;
        return generator.apply (data);
    }

    /**
     * This is an abridged version that is passed a {@link DomBuilder} element and
     * data. Events can be declared but nodes not extracted.
     * 
     * @param el   the element to build into.
     * @param data the data to build against.
     */
    protected void generate(ElementBuilder el, D data) {
        // Nothing.
    }
    
}
