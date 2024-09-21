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

import elemental2.dom.Element;
import elemental2.dom.Node;
import elemental2.dom.Text;

public class NodeSupport {
    /**
     * Determines if the passed node is a text node.
     * 
     * @param node
     *             the node to test.
     * @return {@code true} if it is.
     */
    public static boolean isText(Node node) {
        return (node.nodeType == 3);
    }

    /**
     * This up-cast avoid problems that may arise due to casting the original node.
     * 
     * @param node
     *             the node to cast.
     * @return the cast node.
     */
    public static native Text asText(Object node) /*-{
        return node;
    }-*/;

    /**
     * This up-cast avoid problems that may arise due to casting the original node.
     * 
     * @param node
     *             the node to cast.
     * @return the cast node.
     */
    public static native Node asNode(Object node) /*-{
        return node;
    }-*/;

    /**
     * This up-cast avoid problems that may arise due to casting the original node.
     * 
     * @param node
     *             the node to cast.
     * @return the cast node.
     */
    public static native Element asElement(Object node) /*-{
        return node;
    }-*/;
}
