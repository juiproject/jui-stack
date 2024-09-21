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
package com.effacy.jui.core.client.dom;

import elemental2.dom.Node;

/**
 * Something that provides a node.
 *
 * @author Jeremy Buckley
 */
@FunctionalInterface
public interface INodeProvider {

    /**
     * The node being represented by the provider.
     * 
     * @return the node.
     */
    public Node node();

    /**
     * Convenience to wrap a node in a {@link INodeProvider} that returns the node.
     * 
     * @param node
     *             the node to wrap.
     * @return the node provider wrapping the passed node.
     */
    public static INodeProvider wrap(Node node) {
        return new INodeProvider () {

            @Override
            public Node node() {
                return node;
            }

        };
    }

}
