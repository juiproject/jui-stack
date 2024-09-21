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

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;

import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * Helper class for wrapping an existing element and rendering into that
 * element.
 */
public class Wrap {
    
    /**
     * Creates a wrapper builder around an existing DOM element.
     * <p>
     * See {@link DomBuilder#el(Element, java.util.function.Consumer...)}.
     * 
     * @param el
     *           the element to wrap.
     * @return the wrapped element (in a builder).
     */
    public static ExistingElementBuilder $(Element el) {
        return DomBuilder.el (el);
    }

    /**
     * Convenience to build into a node directly. Can be used for updates.
     * 
     * @param el
     *                the node to build into.
     * @param builder
     *                to build the content.
     * @return the generated node provider.
     */
    public static INodeProvider buildInto(Element el, Consumer<ElementBuilder> builder) {
        return Wrap.$ (el).clear ().$ (root -> {
            if (builder != null)
                builder.accept (root);
        }).build ();
    }

    /**
     * Convenience to build into a node directly. Can be used for updates.
     * 
     * @param el
     *                  the node to build into.
     * @param builder
     *                  to build the content.
     * @param extractor
     *                  to extract elements from the newly built DOM.
     * @return the generated node provider.
     */
    public static INodeProvider buildInto(Element el, Consumer<ElementBuilder> builder, Consumer<NodeContext> extractor) {
        return Wrap.$ (el).clear ().$ (root -> {
            if (builder != null)
                builder.accept (root);
        }).build (extractor);
    }

    /**
     * Convenience to build into a node directly. Can be used for updates.
     * 
     * @param el
     *                 the node to build into.
     * @param selector
     *                 the selector to identify the element to build into.
     * @param builder
     *                 to build the content.
     * @return the generated node provider.
     */
    public static INodeProvider findAndBuildInto(Element el, String selector, Consumer<ElementBuilder> builder) {
        JQueryElement elm = JQuery.$ (el).find (selector);
        if (elm.length () == 0)
            return null;
        return Wrap.$ (Js.cast (elm.get(0))).clear ().$ (root -> {
            if (builder != null)
                builder.accept (root);
        }).build ();
    }

    /**
     * Convenience to build into a node directly. Can be used for updates.
     * 
     * @param el
     *                 the node to build into.
     * @param selector
     *                 the selector to identify the element to build into.
     * @param builder
     *                 to build the content.
     * @param extractor
     *                  to extract elements from the newly built DOM.
     * @return the generated node provider.
     */
    public static INodeProvider findAndBuildInto(Element el, String selector, Consumer<ElementBuilder> builder, Consumer<NodeContext> extractor) {
        JQueryElement elm = JQuery.$ (el).find (selector);
        if (elm.length () == 0)
            return null;
        return Wrap.$ (Js.cast (elm.get(0))).clear ().$ (root -> {
            if (builder != null)
                builder.accept (root);
        }).build (extractor);
    }
}
