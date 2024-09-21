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
package com.effacy.jui.core.client.component;

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.core.client.dom.renderer.IRenderer;

/**
 * Simple component with no configuration and utility methods for building from
 * the constructor.
 */
public class SimpleComponent extends Component<Component.Config> {
    
    /**
     * See {@link #renderer(Consumer, Consumer)} but without an <code>onbuild</code> parameter.
     */
    protected void renderer(Consumer<ElementBuilder> builder) {
        renderer (builder, null);
    }

    /**
     * Short cut to using a {@link DomBuilder}. The passed builder will be invoked
     * with a wrapper around the components root element.
     * 
     * @param builder
     *                to build out the component root element.
     * @param onbuild
     *                (optional) will be invoked post-build to allow for element
     *                extraction.
     */
    protected void renderer(Consumer<ElementBuilder> builder, Consumer<NodeContext> onbuild) {
        renderer (IRenderer.build (el -> DomBuilder.el (el, root -> {
            builder.accept (root);
        }).build (onbuild)));
    }
}
