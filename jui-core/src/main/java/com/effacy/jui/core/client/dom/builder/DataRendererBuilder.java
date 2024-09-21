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
package com.effacy.jui.core.client.dom.builder;

import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;

import elemental2.dom.Element;
import elemental2.dom.Node;

public class DataRendererBuilder<D> extends NodeBuilder<DataRendererBuilder<D>> {

    /**
     * The renderer to invoke.
     */
    private IDataRenderer<D> renderer;

    /**
     * The data for the renderer.
     */
    private D data;

    /**
     * Construct with the text contents.
     * 
     * @param renderer
     *                 the renderer to invoke.
     * @param data
     *                 data for the renderer.
     */
    public DataRendererBuilder(IDataRenderer<D> renderer, D data) {
        this.renderer = renderer;
        this.data = data;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.DomBuilder.NodeBuilder#_nodeImpl(Node, BuildContext)
     */
    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        if (renderer == null)
            return null;
        if (!(parent instanceof Element))
            return null;
        IUIEventHandler handler = renderer.render ((Element) parent, data);
        if (handler != null)
            ctx.register (handler);
        return null;
    }

}
