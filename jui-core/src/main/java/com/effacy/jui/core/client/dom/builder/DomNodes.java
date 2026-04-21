/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Text;

/**
 * Delegates DOM node creation for builder infrastructure.
 * <p>
 * This currently forwards directly to {@link DomGlobal#document}. It exists so
 * that node creation can be centralised and later made swappable for testing.
 */
public final class DomNodes {

    /**
     * Optional factory used to create DOM nodes.
     */
    private static IDomNodeFactory factory;

    private DomNodes() {
        // Static utility.
    }

    /**
     * Assigns the DOM node factory.
     * <p>
     * When {@code null}, node creation falls back to {@link DomGlobal#document}.
     * 
     * @param factory
     *                the factory.
     */
    public static void factory(IDomNodeFactory factory) {
        DomNodes.factory = factory;
    }

    /**
     * Obtains the configured DOM node factory.
     * 
     * @return the factory, or {@code null} if none is configured.
     */
    public static IDomNodeFactory factory() {
        return factory;
    }

    /**
     * Creates a DOM element with the given tag name.
     * 
     * @param tag
     *            the tag name.
     * @return the created element.
     */
    public static Element createElement(String tag) {
        if (factory != null)
            return factory.createElement(tag);
        return DomGlobal.document.createElement(tag);
    }

    /**
     * Creates a DOM element within the given namespace.
     * 
     * @param namespace
     *                  the namespace URI.
     * @param tag
     *            the tag name.
     * @return the created element.
     */
    public static Element createElementNS(String namespace, String tag) {
        if (factory != null)
            return factory.createElementNS(namespace, tag);
        return DomGlobal.document.createElementNS(namespace, tag);
    }

    /**
     * Creates a DOM text node.
     * 
     * @param text
     *             the text content.
     * @return the created text node.
     */
    public static Text createTextNode(String text) {
        if (factory != null)
            return factory.createTextNode(text);
        return DomGlobal.document.createTextNode(text);
    }

    /**
     * Used to provide an alternative DOM node factory for builder infrastructure.
     */
    public interface IDomNodeFactory {

        /**
         * Creates a DOM element with the given tag name.
         * 
         * @param tag
         *            the tag name.
         * @return the created element.
         */
        Element createElement(String tag);

        /**
         * Creates a DOM element within the given namespace.
         * 
         * @param namespace
         *                  the namespace URI.
         * @param tag
         *            the tag name.
         * @return the created element.
         */
        Element createElementNS(String namespace, String tag);

        /**
         * Creates a DOM text node.
         * 
         * @param text
         *             the text content.
         * @return the created text node.
         */
        Text createTextNode(String text);
    }
}
