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

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.css.client.CssResource.UseStyle;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLIElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.HTMLTextAreaElement;
import elemental2.dom.Node;
import jsinterop.base.Js;

/**
 * Support controller class for operating on elements and for providing
 * interoperability between J2CL and GWT.
 *
 * @author Jeremy Buckley
 */
public class DomSupport {

    /**
     * Creates a UUID.
     * 
     * @return the UUID.
     */
    public static native String uuid() /*-{
        return crypto.randomUUID();
    }-*/;

    /**
     * Convenience to iterate over child nodes of a node.
     * 
     * @param node
     *                the node to iterate over.
     * @param visitor
     *                the visitor to visit the children.
     */
    public static void forEach(Node node, Consumer<Node> visitor) {
        if (node == null)
            return;
        for (int i = 0, len = node.childNodes.length; i < len; i++) {
            visitor.accept(node.childNodes.getAt(i));
        }
    }
    
    public static Node find(Node node, Predicate<Node> test) {
        if (node == null)
            return null;
        for (int i = 0, len = node.childNodes.length; i < len; i++) {
            Node child = node.childNodes.getAt(i);
            if (test.test(child))
                return child;
        }
        return null;
    }


    /************************************************************************
     * DOM element creation.
     ************************************************************************/

    /**
     * Creates the specified element.
     * 
     * @param tag
     *               the tag type.
     * @param parent
     *               (optional) parent to append the element to (if multiple are
     *               present then the first one is taken).
     * @return the element.
     */
    public static HTMLElement createElement(String tag, Node... parent) {
        HTMLElement el = (HTMLElement) DomGlobal.document.createElement (tag);
        if ((parent.length > 0) && (parent[0] != null))
            parent[0].appendChild (el);
        return el;
    }

    public static HTMLElement createElement(Node parent, String tag, Consumer<HTMLElement> builder) {
        HTMLElement el = (HTMLElement) DomGlobal.document.createElement (tag);
        if (builder != null)
            builder.accept (el);
        if (parent != null)
            parent.appendChild (el);
        return el;
    }

    /**
     * Creates a DIV element.
     * 
     * @return the element.
     */
    public static HTMLDivElement createDiv(Node... parent) {
        return (HTMLDivElement) createElement ("div", parent);
    }

    /**
     * Creates a DIV element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLDivElement createDiv(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLDivElement) createElement (parent, "div", builder);
    }

    /**
     * Creates a SPAN element.
     * 
     * @return the element.
     */
    public static HTMLElement createSpan(Node... parent) {
        return createElement ("span", parent);
    }

    /**
     * Creates a SPAN element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLElement createSpan(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLElement) createElement (parent, "span", builder);
    }

    /**
     * Creates a EM element.
     * 
     * @return the element.
     */
    public static HTMLElement createEm(Node... parent) {
        return createElement ("em", parent);
    }

    /**
     * Creates a EM element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLElement createEm(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLElement) createElement (parent, "em", builder);
    }

    /**
     * Creates a TABLE element.
     * 
     * @return the element.
     */
    public static HTMLTableElement createTable(Node... parent) {
        return (HTMLTableElement) createElement ("table", parent);
    }

    /**
     * Creates a TABLE element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLTableElement createTable(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLTableElement) createElement (parent, "table", builder);
    }

    /**
     * Creates a THEADER element.
     * 
     * @return the element.
     */
    public static HTMLElement createTHeader(Node... parent) {
        return createElement ("theader", parent);
    }

    /**
     * Creates a THEADER element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLElement createTHeader(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLElement) createElement (parent, "theader", builder);
    }

    /**
     * Creates a TBODY element.
     * 
     * @return the element.
     */
    public static HTMLElement createTBody(Node... parent) {
        return createElement ("tbody", parent);
    }

    /**
     * Creates a TBODY element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLElement createTBody(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLElement) createElement (parent, "tbody", builder);
    }

    /**
     * Creates a TR element.
     * 
     * @return the element.
     */
    public static HTMLTableRowElement createTR(Node... parent) {
        return (HTMLTableRowElement) createElement ("tr", parent);
    }

    /**
     * Creates a TR element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLTableRowElement createTR(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLTableRowElement) createElement (parent, "tr", builder);
    }

    /**
     * Creates a TH element.
     * 
     * @return the element.
     */
    public static HTMLTableCellElement createTH(Node... parent) {
        return (HTMLTableCellElement) createElement ("th", parent);
    }

    /**
     * Creates a TH element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLTableCellElement createTH(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLTableCellElement) createElement (parent, "th", builder);
    }

    /**
     * Creates a TD element.
     * 
     * @return the element.
     */
    public static HTMLElement createTD(Node... parent) {
        return createElement ("td", parent);
    }

    /**
     * Creates a TD element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLElement createTD(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLElement) createElement (parent, "td", builder);
    }

    /**
     * Creates an LI element.
     * 
     * @return the element.
     */
    public static HTMLLIElement createLi(Node... parent) {
        return (HTMLLIElement) createElement ("li", parent);
    }

    /**
     * Creates a LI element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLLIElement createLi(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLLIElement) createElement (parent, "li", builder);
    }

    /**
     * Creates an A element.
     * 
     * @return the element.
     */
    public static HTMLAnchorElement createA(Node... parent) {
        return (HTMLAnchorElement) createElement ("a", parent);
    }

    /**
     * Creates an A element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLAnchorElement createA(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLAnchorElement) createElement (parent, "a", builder);
    }

    /**
     * Creates an INPUT element.
     * 
     * @return the element.
     */
    public static HTMLInputElement createInput(Node... parent) {
        return (HTMLInputElement) createElement ("input", parent);
    }

    /**
     * Creates an INPUT element.
     * 
     * @param parent
     *                an optional parent.
     * @param builder
     *                an optional builder for the tag.
     * @return the element.
     */
    public static HTMLInputElement createInput(Node parent, Consumer<HTMLElement> builder) {
        return (HTMLInputElement) createElement (parent, "input", builder);
    }

    public static void addRemoveClass(Element el, String style, boolean add) {
        if ((el == null) || (style == null))
            return;
        if (!add)
            el.classList.remove (style);
        else if (!el.classList.contains (style))
            el.classList.add (style);
    }

    /********************************************************************
     * DOM manipulations
     ********************************************************************/

    /**
     * Inserts the given child into the given parent and the specified index.
     * 
     * @param parent
     *               the parent to insert into.
     * @param addition
     *               the child to insert.
     * @param index
     *               the index (less than 0 or beyond the end will append).
     */
    // public static void insertChild(Element parent, Element child, int index) {
    //     DOM.insertChild (Js.cast (parent), Js.cast (child), index);
    // }
    public static native void insertChild(Element parent, Element addition, int index) /*-{
        var count = 0, child = parent.firstChild, before = null;
        while (child) {
            if (child.nodeType == 1) {
                if (count == index) {
                    before = child;
                    break;
                }
                ++count;
            }
            child = child.nextSibling;
        }

        parent.insertBefore(addition, before);
    }-*/;


    /**
     * Removes all the children of a node.
     * 
     * @param node
     *             the node.
     */
    public static <N extends Node> N removeAllChildren(N node) {
        if (node == null)
            return node;
        while (node.lastChild != null)
            node.removeChild (node.lastChild);
        return node;
    }

    /**
     * Removes all the CSS classes on the node.
     * 
     * @param node
     *             the node.
     */
    public static <N extends Element> N removeAllClasses(N node) {
        if (node == null)
            return node;
        for (String style : new ArrayList<> (node.classList.asList()))
            node.classList.remove (style);
        return node;
    }

    /********************************************************************
     * DOM actions
     ********************************************************************/

    /**
     * Applies a click action to the raw element (i.e. an anchor).
     * 
     * @param el
     *           the element to apply to.
     * @return the passed element.
     */
    public static native Element click(Element el) /*-{
        el.click ();
        return el;
    }-*/;

    /**
     * Disables the raw element.
     * 
     * @param el
     *           the element to disable.
     * @return the passed element.
     */
    public static native Element disable(Element el) /*-{
        el.disabled = true;
        return el;
    }-*/;

    /**
     * Enables the raw element.
     * 
     * @param el
     *           the element to enable.
     * @return the passed element.
     */
    public static native Element enable(Element el) /*-{
        el.disabled = false;
        return el;
    }-*/;

    /**
     * Assigns the inner text property to an element.
     * 
     * @param el
     *             the element to assign to.
     * @param text
     *             the text to assign.
     * @return the passed element.
     */
    public static native Element innerText(Element el, String text) /*-{
        el.innerText = text;
        return el;
    }-*/;

    public static native String innerText(Element el) /*-{
        return el.innerText;
    }-*/;


    /********************************************************************
     * DOM inspections and determinations
     ********************************************************************/

    /**
     * Traverse the tree whose root is the passed node.
     * 
     * @param node
     *                the node to traverse.
     * @param visitor
     *                the visitor to analyze the nodes.
     */
    public static void traverse(Node node, Consumer<Node> visitor) {
        if (node == null)
            return;
        
        // First we pass the node to the visitor.
        visitor.accept (node);
        
        // Then we traverse all the child nodes.
        if (node.hasChildNodes ())
            node.childNodes.asList ().forEach (n -> traverse (n, visitor));
    }

    /**
     * Given a possible child and possible parent determine if the child is indeed a
     * child of the parent (or is the parent),
     * 
     * @param child
     *               the potential child.
     * @param parent
     *               the potential parent.
     * @return {@code true} if the child is a child of the parent.
     */
    public static boolean isChildOf(Node child, Node parent) {
        if (parent == null)
            return false;
        while (child != null) {
            if ((child == parent) || (child.equals (parent)))
                return true;
            child = child.parentElement;
        }
        return false;
    }

    /**
     * Using a selector attempts to locate an element equal to or above this element
     * that matches the selector, but only going so far up the hierarchy.
     * 
     * @param el
     *                 the starting element to work up from.
     * @param selector
     *                  the selector to attempt to node match against.
     * @param maxHeight
     *                  the maximum height in the hierarchy to go.
     * @return either the first matching element or {@code null} if there is no
     *         match.
     */
    public static Element parent(Node el, String selector, int maxHeight) {
        Node context = null;
        if (maxHeight > 0) {
            context = el;
            while ((context != null) && (maxHeight > 0)) {
                maxHeight--;
                context = context.parentNode;
            }
        }
        if (context == null)
            return null;
        return parent (el, selector, context);
    }

    /**
     * Using a selector attempts to locate an element equal to or above this element
     * that matches the selector, but only going so far up the hierarchy.
     * 
     * @param el
     *                 the starting element to work up from.
     * @param selector
     *                 the selector to attempt to node match against.
     * @param scope
     *                 the bounding scope.
     * @return either the first matching element or {@code null} if there is no
     *         match.
     */
    public static Element parent(Node el, String selector, Node scope) {
        if (scope == null)
            return null;
        JQueryElement result = JQuery.$ (el).closest (selector, scope);
        return (result.length () == 0) ? null : result.get (0);
    }

    /********************************************************************
     * Event management
     ********************************************************************/

    /**
     * Integer parsing with default value.
     * 
     * @param value
     *                     the value to parse.
     * @param defaultValue
     *                     the default value.
     * @return the parsed integer (or default).
     */
    public static native int parseInt(String value, int defaultValue) /*-{
      return parseInt(value, 10) || defaultValue;
    }-*/;

    /************************************************************************
     * Casting.
     ************************************************************************/

    public static void forEach(JQueryElement el, Consumer<Element> child) {
        for (Element element : el.get ())
            child.accept (Js.cast (element));
    }

    public static JQueryElement asJQueryElement(Element el) {
        if (el == null)
            return null;
        return JQuery.$ (el);
    }

    public static JQueryElement[] asJQueryElement(Element[] el) {
        if (el == null)
            return null;
        JQueryElement[] results = new JQueryElement[el.length];
        for (int i = 0; i < results.length; i++) {
            if (el[i] == null)
                results[i] = null;
            else
                results[i] = JQuery.$ (el[i]);
        }
        return results;
    }

    public static JQueryElement asJQueryElementComposed(Element[] el) {
        if (el == null)
            return null;
        return JQuery.$ (el);
    }

    public static HTMLInputElement asHTMLInputElement(Element el) {
        if (el == null)
            return null;
        return (HTMLInputElement) Js.cast (el);
    }

    public static HTMLInputElement[] asHTMLInputElement(Element[] el) {
        if (el == null)
            return null;
        HTMLInputElement[] results = new HTMLInputElement[el.length];
        for (int i = 0; i < results.length; i++) {
            if (el[i] == null)
                results[i] = null;
            else
                results[i] = (HTMLInputElement) Js.cast (el[i]);
        }
        return results;
    }

    public static HTMLTextAreaElement asHTMLTextAreaElement(Element el) {
        if (el == null)
            return null;
        return (HTMLTextAreaElement) Js.cast (el);
    }

    public static HTMLTextAreaElement[] asHTMLTextAreaElement(Element[] el) {
        if (el == null)
            return null;
        HTMLTextAreaElement[] results = new HTMLTextAreaElement[el.length];
        for (int i = 0; i < results.length; i++) {
            if (el[i] == null)
                results[i] = null;
            else
                results[i] = (HTMLTextAreaElement) Js.cast (el[i]);
        }
        return results;
    }

    /************************************************************************
     * Masking.
     ************************************************************************/

    /**
     * Puts a mask over this element to disable user interaction. By default the
     * mask sits over top the element being masked and applies an opacity to "dim"
     * the element and its contents. The caller may add additional content to the
     * mask if necessary.
     * 
     * @return the mask element.
     */
    public static Element mask(Element el) {
        DomSupportCSS css = DomSupportCSS.instance ();

        // If this is already masked, re-mask by clearing the contents of the
        // mask and returning the existing mask.
        if (el.classList.contains (css.masked ())) {
            JQueryElement mask = JQuery.$ (el).children ("div." + css.mask ());
            if ((mask != null) && (mask.length () > 0)) {
                removeAllChildren(mask.get (0));
                return Js.cast (mask.get (0));
            }
        }

        // Mark the element as being masked.
        el.classList.add (css.masked ());
        if ((el instanceof HTMLElement) && "static".equals (((HTMLElement) el).style.position))
            el.classList.add (css.maskedRelative ());

        // Add the masking element.
        Element mask = createDiv ();
        mask.classList.add (css.mask ());
        el.appendChild (mask);

        // Return the element doing the masking.
        return mask;
    }

    /**
     * Removes a previously applied mask.
     */
    public static void unmask(Element el) {
        DomSupportCSS css = DomSupportCSS.instance ();
        if (el.classList.contains (css.masked ())) {
            JQuery.$ (el).children ("div." + css.mask ()).remove ();
            el.classList.remove (css.masked ());
            el.classList.remove (css.maskedRelative ());
        }
    }

    /********************************************************************
     * CSS
     ********************************************************************/

    @CssResource("com/effacy/jui/core/client/dom/DomSupport.css")
    public static abstract class DomSupportCSS implements CssDeclaration {

        /**
         * CSS style to apply to a mask of the element.
         * 
         * @return The style.
         */
        @UseStyle("egwt-mask")
        public abstract String mask();

        /**
         * CSS style to apply to the element when being masked.
         * 
         * @return The style.
         */
        @UseStyle("egwt-masked")
        public abstract String masked();

        /**
         * CSS style to apply to the element when being masked and the element is
         * statically positioned.
         * 
         * @return The style.
         */
        @UseStyle("egwt-masked-relative")
        public abstract String maskedRelative();

        private static DomSupportCSS STYLES;

        public static DomSupportCSS instance() {
            if (STYLES == null) {
                STYLES = (DomSupportCSS) GWT.create (DomSupportCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
