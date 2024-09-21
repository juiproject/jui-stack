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

import com.effacy.jui.platform.util.client.With;

import elemental2.dom.Element;

/**
 * Convenience mechanism for building a DOM structure in a more programmatic
 * friendly manner.
 *
 * @author Jeremy Buckley
 */
public class DomBuilder {

    /**
     * Wraps a node builder around an existing element. This allows the element to
     * be added into including assignment of class styles and attributes.
     * 
     * @param node
     *                   the element node to wrap.
     * @param configurer
     *                   to configure the wrapped node.
     * @return the associated builder.
     */
    @SafeVarargs
    public static ExistingElementBuilder el(Element node, Consumer<ExistingElementBuilder>... configurer) {
        return With.$ (new ExistingElementBuilder (node), configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder custom(String tag, Consumer<ElementBuilder>... configurer) {
        return With.$ (new ElementBuilder (tag), configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder a(Consumer<ElementBuilder>... configurer) {
        return custom ("a", configurer);
    }

    /**
     * Inserts a standard anchor DOM element with the given HREF attribute.
     * 
     * @param href
     *             the href (i.e. url).
     * @return the element.
     */
    public static ElementBuilder a(String href) {
        return a (href, null);
    }

    /**
     * Inserts a standard anchor DOM element with the given HREF attribute and
     * target attribute.
     * 
     * @param href
     *               the href (i.e. url).
     * @param target
     *               the target attribute.
     * @return the element.
     */
    public static ElementBuilder a(String href, String target) {
        ElementBuilder el = new ElementBuilder ("a");
        el.setAttribute ("href", href);
        if (target != null)
            el.setAttribute ("target", target);
        return el;
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder article(Consumer<ElementBuilder>... configurer) {
        return custom ("article", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder aside(Consumer<ElementBuilder>... configurer) {
        return custom ("aside", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder blockquote(Consumer<ElementBuilder>... configurer) {
        return custom ("blockquote", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder br(Consumer<ElementBuilder>... configurer) {
        return custom ("br", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder button(Consumer<ElementBuilder>... configurer) {
        return custom ("button", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder caption(Consumer<ElementBuilder>... configurer) {
        return custom ("caption", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder cite(Consumer<ElementBuilder>... configurer) {
        return custom ("cite", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder code(Consumer<ElementBuilder>... configurer) {
        return custom ("code", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder col(Consumer<ElementBuilder>... configurer) {
        return custom ("col", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder colgroup(Consumer<ElementBuilder>... configurer) {
        return custom ("colgroup", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder dd(Consumer<ElementBuilder>... configurer) {
        return custom ("dd", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder details(Consumer<ElementBuilder>... configurer) {
        return custom ("details", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder div(Consumer<ElementBuilder>... configurer) {
        return custom ("div", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder dl(Consumer<ElementBuilder>... configurer) {
        return custom ("dl", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder dt(Consumer<ElementBuilder>... configurer) {
        return custom ("dt", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder em(Consumer<ElementBuilder>... configurer) {
        return custom ("em", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder footer(Consumer<ElementBuilder>... configurer) {
        return custom ("footer", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder h1(Consumer<ElementBuilder>... configurer) {
        return custom ("h1", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder h2(Consumer<ElementBuilder>... configurer) {
        return custom ("h2", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder h3(Consumer<ElementBuilder>... configurer) {
        return custom ("h3", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder h4(Consumer<ElementBuilder>... configurer) {
        return custom ("h4", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder h5(Consumer<ElementBuilder>... configurer) {
        return custom ("h5", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s)(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder h6(Consumer<ElementBuilder>... configurer) {
        return custom ("h6", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder header(Consumer<ElementBuilder>... configurer) {
        return custom ("header", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @param nodes
     *                   (optional) nodes to append.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder hr(Consumer<ElementBuilder>... configurer) {
        return custom ("hr", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder i(Consumer<ElementBuilder>... configurer) {
        return custom ("i", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder img(Consumer<ElementBuilder>... configurer) {
        return custom ("img", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param src
     *            the source URL for the image.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder img(String src, Consumer<ElementBuilder>... configurer) {
        ElementBuilder img = custom ("img", configurer);
        if (src != null)
            img.setAttribute ("src", src);
        return img;
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param type
     *             the input type.
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder input(String type, Consumer<ElementBuilder>... configurer) {
        ElementBuilder el = new ElementBuilder ("input");
        el.setAttribute ("type", type);
        return With.$ (el, configurer);
    }

    /**
     * Inserts a standard label DOM element containing the passed text (a
     * convenience method that avoids having to separately insert a text node into
     * the label node).
     * 
     * @return the element.
     */
    public static ElementBuilder label(String text) {
        ElementBuilder el = new ElementBuilder ("label");
        el.text (text);
        return el;
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder label(Consumer<ElementBuilder>... configurer) {
        return custom ("label", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder li(Consumer<ElementBuilder>... configurer) {
        return custom ("li", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder mark(Consumer<ElementBuilder>... configurer) {
        return custom ("mark", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder nav(Consumer<ElementBuilder>... configurer) {
        return custom ("nav", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder ol(Consumer<ElementBuilder>... configurer) {
        return custom ("ol", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder p(Consumer<ElementBuilder>... configurer) {
        return custom ("p", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder span(Consumer<ElementBuilder>... configurer) {
        return custom ("span", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder strong(Consumer<ElementBuilder>... configurer) {
        return custom ("strong", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder sub(Consumer<ElementBuilder>... configurer) {
        return custom ("sub", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder summary(Consumer<ElementBuilder>... configurer) {
        return custom ("summary", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder sup(Consumer<ElementBuilder>... configurer) {
        return custom ("sup", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder table(Consumer<ElementBuilder>... configurer) {
        return custom ("table", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder tbody(Consumer<ElementBuilder>... configurer) {
        return custom ("tbody", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder td(Consumer<ElementBuilder>... configurer) {
        return custom ("td", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder textarea(Consumer<ElementBuilder>... configurer) {
        return custom ("textarea", configurer);
    }

    /**
     * Inserts a standard DOM textarea element.
     * 
     * @param rows
     *             the number of rows (will only be applied if greater than 0).
     * @param cols
     *             the number of cols (will only be applied if greater than 0).
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder textarea(int rows, int cols, Consumer<ElementBuilder>... configurer) {
        ElementBuilder el = new ElementBuilder ("textarea");
        if (rows > 0)
            el.setAttribute ("rows", "" + rows);
        if (cols > 0)
            el.setAttribute ("cols", "" + cols);
        return With.$ (el, configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder tfoot(Consumer<ElementBuilder>... configurer) {
        return custom ("tfoot", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder th(Consumer<ElementBuilder>... configurer) {
        return custom ("th", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder thead(Consumer<ElementBuilder>... configurer) {
        return custom ("thead", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder tr(Consumer<ElementBuilder>... configurer) {
        return custom ("tr", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder u(Consumer<ElementBuilder>... configurer) {
        return custom ("u", configurer);
    }

    /**
     * Inserts a standard DOM element.
     * 
     * @param configurer
     *                   configurer(s) for the newly created element.
     * @return the element.
     */
    @SafeVarargs
    public static ElementBuilder ul(Consumer<ElementBuilder>... configurer) {
        return custom ("ul", configurer);
    }
    
}
