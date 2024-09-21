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

import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;

import elemental2.dom.Element;

/**
 * Convenience to create a renderer from a node provider.
 */
public class NodeProviderDataRenderer<D> implements IDataRenderer<D> {

    /**
     * A function that maps data to a node provider.
     */
    private Function<D, INodeProvider> generator;

    /**
     * Constructor to use when overriding {@link #generate(Object)}.
     */
    protected NodeProviderDataRenderer() {
        super ();
    }

    /**
     * Construct with a generator that returns a built node structure configured
     * from the passed data.
     * 
     * @param generator
     *                  the generator.
     */
    public NodeProviderDataRenderer(Function<D, INodeProvider> generator) {
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
        INodeProvider provider = generate (data);
        if (provider != null)
            el.appendChild (provider.node ());
        return (provider instanceof IUIEventHandler) ? ((IUIEventHandler) provider) : null;
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
    protected INodeProvider generate(D data) {
        return generator.apply (data);
    }

}
