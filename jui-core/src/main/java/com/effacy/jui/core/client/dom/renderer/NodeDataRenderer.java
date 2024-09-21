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

import elemental2.dom.Element;
import elemental2.dom.Node;

/**
 * A data renderer built on a node generator (something that directly converts
 * the data to a node).
 *
 * @author Jeremy Buckley
 */
public class NodeDataRenderer<D> implements IDataRenderer<D> {

    /**
     * A function that maps data to a node.
     */
    private Function<D, Node> generator;

    /**
     * Constructor to use when overriding {@link #generate(Object)}.
     */
    protected NodeDataRenderer() {
        super ();
    }

    /**
     * Construct with a generator that returns a built node structure configured
     * from the passed data.
     * 
     * @param generator
     *                  the generator.
     */
    public NodeDataRenderer(Function<D, Node> generator) {
        this.generator = generator;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.renderer.IDataRenderer#render(elemental2.dom.Element,
     *      java.lang.Object)
     */
    @Override
    public IUIEventHandler render(Element el, D data) {
        Node node = generate (data);
        if (node != null)
            el.appendChild (node);
        return null;
    }

    /**
     * Given data this will generate an associated node structure.
     * <p>
     * The default delegates to any generator passed during construction.
     * 
     * @param data
     *             the data to use.
     * @return the node built from the data.
     */
    protected Node generate(D data) {
        return generator.apply (data);
    }

}
