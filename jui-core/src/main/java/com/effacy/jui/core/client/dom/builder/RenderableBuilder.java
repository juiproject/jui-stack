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

import com.effacy.jui.core.client.dom.renderer.IRenderable;

import elemental2.dom.Element;
import elemental2.dom.Node;

public class RenderableBuilder extends NodeBuilder<RenderableBuilder> {

    private IRenderable renderable;
    
    public RenderableBuilder (IRenderable renderable) {
        this.renderable = renderable;
    }

    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        if ((renderable != null) && (parent instanceof Element)) {
            renderable.render((Element) parent, parent.childNodes.length);
            ctx.lodge (renderable);
        }
        return null;
    }
    
}
