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

import elemental2.dom.Node;

public abstract class DeferredContainerBuilder<T extends DeferredContainerBuilder<T>> extends ContainerBuilder<T> {

    /**
     * If has been built.
     */
    private boolean built = false;

    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        if (!built) {
            built = true;
            // This builds the fragment into this container.
            build (parent, ctx);
        }
        // The container itself does not create a node so the children are built
        // directly into the parent node.
        super._nodeImpl (parent, ctx);
        return null;
    }
    
    protected abstract void build(Node parent, BuildContext ctx);
}
