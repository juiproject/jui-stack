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
package com.effacy.jui.core.client.dom.jquery;

import elemental2.dom.Element;
import elemental2.dom.Node;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(name = "jQuery", isNative = true)
public class JQueryElement extends Node {

    /**
     * A string containing the jQuery version number.
     */
    @JsProperty(name="jquery")
    public native int jquery();

    /**
     * The number of elements in the jQuery object.
     */
    @JsProperty(name="length")
    public native int length();

    /**
     * Use as the raw 0 index element.
     */
    @JsOverlay
    public final Element asElement() {
        return get(0);
    }

    /**
     * Create a new jQuery object with elements added to the set of matched elements.
     * @param selectorOrHtml A string representing a selector expression to find
     *                       additional elements to add to the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement add(String selectorOrHtml);

    /**
     * Create a new jQuery object with elements added to the set of matched elements.
     * @param element One or more elements to add to the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement add(Element element);

    /**
     * Create a new jQuery object with elements added to the set of matched elements.
     * @param selection An existing jQuery object to add to the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement add(JQueryElement selection);

    /**
     * Create a new jQuery object with elements added to the set of matched elements.
     * @param selector A string representing a selector expression to find additional
     *                 elements to add to the set of matched elements.
     * @param context The point in the document at which the selector should begin matching;
     *                similar to the context argument of the $(selector, context) method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement add(String selector, Element context);

    /**
     * Add the previous set of elements on the stack to the current set, optionally
     * filtered by a selector.
     * @param selector A string containing a selector expression to match the current
     *                 set of elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement addBack(String... selector);

    /**
     * Adds the specified class(es) to each element in the set of matched elements.
     * @param className One or more space-separated classes to be added to the class
     *                  attribute of each matched element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement addClass(String className);

    /**
     * Insert content, specified by the parameter, after each element in the
     * set of matched elements.
     * @param content HTML string to insert after each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement after(String content);

    /**
     * Insert content, specified by the parameter, after each element in the
     * set of matched elements.
     * @param content DOM elements to insert after each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement after(Element... content);

    /**
     * Insert content, specified by the parameter, after each element in the
     * set of matched elements.
     * @param content jQuery object to insert after each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement after(JQueryElement content);

    /**
     * Add the previous set of elements on the stack to the current set.
     * Note: This function has been deprecated and is now an alias for .addBack(), which
     * should be used with jQuery 1.8 and later.
     * @deprecated use {@link #addBack(String...)}
     */
    public native JQueryElement andSelf();

    /**
     * Perform a custom animation of a set of CSS properties.
     * @param properties An object of CSS properties and values that the animation will move toward.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement animate(Object properties, double duration);

    /**
     * Perform a custom animation of a set of CSS properties.
     * @param properties An object of CSS properties and values that the animation will move toward.
     * @param duration A string or number determining how long the animation will run.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement animate(Object properties, double duration, String easing);

    /**
     * Perform a custom animation of a set of CSS properties.
     * @param properties An object of CSS properties and values that the animation will move toward.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement animate(Object properties, AnimateOptions options);

    /**
     * Insert content, specified by the parameter, to the end of each element in the set of matched elements.
     * @param htmlString HTML string to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement append(String htmlString);

    /**
     * Insert content, specified by the parameter, to the end of each element in the set of matched elements.
     * @param element jQuery objects to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement append(JQueryElement element);

    /**
     * Insert content, specified by the parameter, to the end of each element in the set of matched elements.
     * @param element DOM elements to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement append(Element... element);

    /**
     * Insert every element in the set of matched elements to the end of the target.
     * @param htmlString HTML string the matched set of elements will be inserted at the end of the
     *                element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement appendTo(String htmlString);

    /**
     * Insert every element in the set of matched elements to the end of the target.
     * @param element jQuery objects the matched set of elements will be inserted at the end of the
     *                element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement appendTo(JQueryElement element);

    /**
     * Insert every element in the set of matched elements to the end of the target.
     * @param element DOM elements the matched set of elements will be inserted at the end of the
     *                element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement appendTo(Element... element);

    /**
     * Insert content, specified by the parameter, to the beginning of each element in the set of matched elements.
     * @param htmlString HTML string to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prepend(String htmlString);

    /**
     * Insert content, specified by the parameter, to the beginning of each element in the set of matched elements.
     * @param element jQuery objects to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prepend(JQueryElement element);

    /**
     * Insert content, specified by the parameter, to the beginning of each element in the set of matched elements.
     * @param element DOM elements to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prepend(Element... element);

    /**
     * Insert every element in the set of matched elements to the beginning of the target.
     * @param htmlString HTML string to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prependTo(String htmlString);

    /**
     * Insert every element in the set of matched elements to the beginning of the target.
     * @param element jQuery objects to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prependTo(JQueryElement element);

    /**
     * Insert every element in the set of matched elements to the beginning of the target.
     * @param element DOM elements to insert at the end of each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prependTo(Element... element);

    /**
     * Get the value of an attribute for the first element in the set of matched elements.
     * @param attr The name of the attribute to get.
     */
    public native Object attr(String attr);

    /**
     * Set one or more attributes for the set of matched elements.
     * @param attr The name of the attribute to set.
     * @param value A value to set for the attribute.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement attr(String attr, Object value);

    /**
     * Insert content, specified by the parameter, before each element in the set of matched elements.
     * @param htmlString HTML string to insert before each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement before(String htmlString);

    /**
     * Insert content, specified by the parameter, before each element in the set of matched elements.
     * @param element jQuery object to insert before each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement before(JQueryElement element);

    /**
     * Insert content, specified by the parameter, before each element in the set of matched elements.
     * @param element DOM elements to insert before each element in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement before(Element... element);

    /**
     * Get the children of each element in the set of matched elements, optionally filtered by a selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement children(String selector);


    /**
     * Create a deep copy of the set of matched elements.
     * @return  self {@link JQueryElement}
     */
    public native JQueryElement clone();

    /**
     * Create a deep copy of the set of matched elements.
     * @param withDataAndEvents A Boolean indicating whether event handlers should be copied along with
     *                          the elements. As of jQuery 1.4, element data will be copied as well.
     * @return  self {@link JQueryElement}
     */
    public native JQueryElement clone(boolean withDataAndEvents);

    /**
     * Create a deep copy of the set of matched elements.
     * @param withDataAndEvents A Boolean indicating whether event handlers should be copied along with
     *                          the elements. As of jQuery 1.4, element data will be copied as well.
     * @param deepWithDataAndEvents A Boolean indicating whether event handlers and data for all children
     *                              of the cloned element should be copied. By default its value matches
     *                              the first argument's value (which defaults to false).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement clone(boolean withDataAndEvents, boolean deepWithDataAndEvents);

    /**
     * For each element in the set, get the first element that matches the selector by testing the
     * element itself and traversing up through its ancestors in the DOM tree.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement closest(String selector);

    /**
     * For each element in the set, get the first element that matches the selector by testing the
     * element itself and traversing up through its ancestors in the DOM tree.
     * @param selector A string containing a selector expression to match elements against.
     * @param context A DOM element within which a matching element may be found. If no context is passed
     *                in then the context of the jQuery set will be used instead.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement closest(String selector, Node context);

    /**
     * For each element in the set, get the first element that matches the selector by testing the
     * element itself and traversing up through its ancestors in the DOM tree.
     * @param selection A jQuery object to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement closest(JQueryElement selection);

    /**
     * For each element in the set, get the first element that matches the selector by testing the
     * element itself and traversing up through its ancestors in the DOM tree.
     * @param element An element to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement closest(Node element);

    /**
     * Get the children of each element in the set of matched elements, including text and comment nodes.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement contents();

    /**
     * Get the computed style properties for the first element in the set of matched elements.
     * @param propertyName A CSS property.
     */
    public native String css(String... propertyName);

    /**
     * Set one or more CSS properties for the set of matched elements.
     * @param propertyName A CSS property.
     * @param value A value to set for the property.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement css(String propertyName, String value);

    /**
     * Set one or more CSS properties for the set of matched elements.
     * @param properties An object of property-value pairs to set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement css(Object properties);

    /**
     * Get the computed style properties for the first element in the set of matched elements.
     * @param propertyName A CSS property.
     * @return {@link String} property value.
     */
    public native String css(String propertyName);

    /**
     * Store arbitrary data associated with the matched elements.
     * @param key A string naming the piece of data to set.
     * @param value The new data value; this can be any Javascript type except undefined.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement data(String key, String value);

    /**
     * Store arbitrary data associated with the matched elements.
     * @param obj An object of key-value pairs of data to update.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement data(Object obj);

    /**
     * Return the value at the named data store for the first element in the jQuery collection,
     * as set by data(name, value) or by an HTML5 data-* attribute.
     * @param key Name of the data stored.
     * @return data object value.
     */
    public native Object data(String key);

    /**
     * Return the value at the named data store for the first element in the jQuery collection,
     * as set by data(name, value) or by an HTML5 data-* attribute.
     * @return data object value.
     */
    public native Object data();

    /**
     * Remove the set of matched elements from the DOM.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement detach();

    /**
     * Remove the set of matched elements from the DOM.
     * @param selector A selector expression that filters the set of matched elements to be removed.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement detach(String selector);

    /**
     * Remove all child nodes of the set of matched elements from the DOM.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement empty();

    /**
     * End the most recent filtering operation in the current chain and return the set of
     * matched elements to its previous state.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement end();

    /**
     * Reduce the set of matched elements to the one at the specified index.
     * @param index An integer indicating the 0-based position of the element. or,
     *              An integer indicating the position of the element, counting backwards
     *              from the last element in the set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement eq(int index);

    /**
     * Display the matched elements by fading them to opaque.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeIn();

    /**
     * Display the matched elements by fading them to opaque.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeIn(double duration);

    /**
     * Display the matched elements by fading them to opaque.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeIn(String easing);

    /**
     * Display the matched elements by fading them to opaque.
     * @param duration A string or number determining how long the animation will run.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeIn(double duration, String easing);

    /**
     * Display the matched elements by fading them to opaque.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeIn(AnimateOptions options);

    /**
     * Hide the matched elements by fading them to transparent.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeOut();

    /**
     * Hide the matched elements by fading them to transparent.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeOut(double duration);

    /**
     * Hide the matched elements by fading them to transparent.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeOut(String easing);

    /**
     * Hide the matched elements by fading them to transparent.
     * @param duration A string or number determining how long the animation will run.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeOut(double duration, String easing);

    /**
     * Hide the matched elements by fading them to transparent.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeOut(AnimateOptions options);

    /**
     * Adjust the opacity of the matched elements.
     * @param duration A string or number determining how long the animation will run.
     * @param opacity A number between 0 and 1 denoting the target opacity.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeTo(double duration, double opacity);

    /**
     * Adjust the opacity of the matched elements.
     * @param duration A string or number determining how long the animation will run.
     * @param opacity A number between 0 and 1 denoting the target opacity.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeTo(double duration, double opacity, String easing);

    /**
     * Display or hide the matched elements by animating their opacity.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeToggle(double duration);

    /**
     * Display or hide the matched elements by animating their opacity.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeToggle(String easing);

    /**
     * Display or hide the matched elements by animating their opacity.
     * @param duration A string or number determining how long the animation will run.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeToggle(double duration, String easing);

    /**
     * Display or hide the matched elements by animating their opacity.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement fadeToggle(AnimateOptions options);

    /**
     * Reduce the set of matched elements to those that match the selector or pass the function's test.
     * @param selector A string containing a selector expression to match the current set of elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement filter(String selector);

    /**
     * Reduce the set of matched elements to those that match the selector or pass the function's test.
     * @param elements One or more DOM elements to match the current set of elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement filter(Element... elements);

    /**
     * Reduce the set of matched elements to those that match the selector or pass the function's test.
     * @param selection An existing jQuery object to match the current set of elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement filter(JQueryElement selection);

    /**
     * Get the descendants of each element in the current set of matched elements, filtered
     * by a selector, jQuery object, or element.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement find(String selector);

    /**
     * Get the descendants of each element in the current set of matched elements, filtered
     * by a selector, jQuery object, or element.
     * @param elements DOM elements to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement find(Element... elements);

    /**
     * Get the descendants of each element in the current set of matched elements, filtered
     * by a selector, jQuery object, or element.
     * @param selection jQuery object to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement find(JQueryElement selection);

    /**
     * Stop the currently-running animation, remove all queued animations, and complete
     * all animations for the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement finish();

    /**
     * Reduce the set of matched elements to the first in the set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement first();

    /**
     * Retrieve one of the elements matched by the jQuery object.
     * @param index A zero-based integer indicating which element to retrieve.
     * @return Retrieved element
     */
    public native Element get(int index);

    /**
     * Retrieve the elements matched by the jQuery object.
     * @return All of the matched DOM nodes are returned by this call, contained
     * in a standard array:
     */
    public native Element[] get();

    /**
     * Reduce the set of matched elements to those that have a descendant that matches
     * the selector or DOM element.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement has(String selector);

    /**
     * Reduce the set of matched elements to those that have a descendant that matches
     * the selector or DOM element.
     * @param contained A DOM element to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement has(Element contained);

    /**
     * Determine whether any of the matched elements are assigned the given class.
     * @param className The class name to search for.
     * @return true if the class exists
     */
    public native boolean hasClass(String className);

    /**
     * Get the current computed height for the first element in the set of matched elements.
     */
    public native double height();

    /**
     * Set the CSS height of every matched element.
     * @param height An integer representing the number of pixels.
     * @return self {@link JQueryElement}
     */
    public native double height(int height);

    /**
     * Set the CSS height of every matched element.
     * @param height An integer with an optional unit of measure appended (as a string).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement height(String height);

    /**
     * Hide the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement hide();

    /**
     * Hide the matched elements.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement hide(double duration);

    /**
     * Hide the matched elements.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement hide(AnimateOptions options);

    /**
     * Get the HTML contents of the first element in the set of matched elements.
     */
    public native String html();

    /**
     * Set the HTML contents of each element in the set of matched elements.
     * @param htmlString A string of HTML to set as the content of each matched element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement html(String htmlString);

    /**
     * Search for a given element from among the matched elements.
     */
    public native int index();

    /**
     * Search for a given element from among the matched elements.
     * @param selector A selector representing a jQuery collection in which to look for an element.
     */
    public native int index(String selector);

    /**
     * Search for a given element from among the matched elements.
     * @param element The DOM element within the jQuery object to look for.
     */
    public native int index(Element element);

    /**
     * Search for a given element from among the matched elements.
     * @param element First element within the jQuery object to look for.
     */
    public native int index(JQueryElement element);

    /**
     * Get the current computed height for the first element in the set of matched elements,
     * including padding but not border.
     */
    public native double innerHeight();

    /**
     * Set the CSS inner height of each element in the set of matched elements.
     * @param value A number along with an optional unit of measure appended (as a string).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement innerHeight(String value);

    /**
     * Set the CSS inner height of each element in the set of matched elements.
     * @param value A number representing the number of pixels.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement innerHeight(double value);

    /**
     * Get the current computed width for the first element in the set of matched elements,
     * including padding but not border.
     */
    public native double innerWidth();

    /**
     * Set the CSS inner width of each element in the set of matched elements.
     * @param value A number along with an optional unit of measure appended (as a string).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement innerWidth(String value);

    /**
     * Set the CSS inner width of each element in the set of matched elements.
     * @param value A number representing the number of pixels.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement innerWidth(double value);

    /**
     * Insert every element in the set of matched elements after the target.
     * @param selector A selector or HTML string; matched set of elements will be inserted after the
     *                 element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement insertAfter(String selector);

    /**
     * Insert every element in the set of matched elements after the target.
     * @param element An element; matched set of elements will be inserted after the
     *                 element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement insertAfter(Element... element);

    /**
     * Insert every element in the set of matched elements after the target.
     * @param element jQuery object; matched set of elements will be inserted after the
     *                 element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement insertAfter(JQueryElement element);

    /**
     * Insert every element in the set of matched elements before the target.
     * @param element jQuery object; matched set of elements will be inserted after the
     *                 element(s) specified by this parameter.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement insertBefore(JQueryElement element);

    /**
     * Check the current matched set of elements against a selector, element, or jQuery object
     * and return true if at least one of these elements matches the given arguments.
     * @param selector A string containing a selector expression to match elements against.
     * @return return true if at least one of these elements matches the given arguments
     */
    public native boolean is(String selector);

    /**
     * Check the current matched set of elements against a selector, element, or jQuery object
     * and return true if at least one of these elements matches the given arguments.
     * @param selection An existing jQuery object to match the current set of elements against.
     * @return return true if at least one of these elements matches the given arguments
     */
    public native boolean is(JQueryElement selection);

    /**
     * Check the current matched set of elements against a selector, element, or jQuery object
     * and return true if at least one of these elements matches the given arguments.
     * @param elements One or more elements to match the current set of elements against.
     * @return return true if at least one of these elements matches the given arguments
     */
    public native boolean is(Element... elements);

    /**
     * Bind an event handler to the "keydown" JavaScript event, or trigger that event on an element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement keydown();

    /**
     * Bind an event handler to the "keypress" JavaScript event, or trigger that event on an element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement keypress();

    /**
     * Bind an event handler to the "keyup" JavaScript event, or trigger that event on an element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement keyup();

    /**
     * Reduce the set of matched elements to the final one in the set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement last();

    /**
     * Load data from the server and place the returned HTML into the matched element.
     * @param url A string containing the URL to which the request is sent.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement load(String url);

    /**
     * Load data from the server and place the returned HTML into the matched element.
     * @param url A string containing the URL to which the request is sent.
     * @param data A plain object or string that is sent to the server with the request.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement load(String url, String data);

    /**
     * Get the immediately following sibling of each element in the set of matched elements. If a selector
     * is provided, it retrieves the next sibling only if it matches that selector.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement next();

    /**
     * Get the immediately following sibling of each element in the set of matched elements. If a selector
     * is provided, it retrieves the next sibling only if it matches that selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement next(String selector);

    /**
     * Get all following siblings of each element in the set of matched elements, optionally
     * filtered by a selector.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextAll();

    /**
     * Get all following siblings of each element in the set of matched elements, optionally
     * filtered by a selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextAll(String selector);

    /**
     * Get all following siblings of each element up to but not including the element matched by
     * the selector, DOM node, or jQuery object passed.
     * @param selector A string containing a selector expression to indicate where to stop
     *                 matching following sibling elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextUntil(String selector, String filter);

    /**
     * Get all following siblings of each element up to but not including the element matched by
     * the selector, DOM node, or jQuery object passed.
     * @param element A DOM node indicating where to stop matching following sibling elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextUntil(Element element);

    /**
     * Get all following siblings of each element up to but not including the element matched by
     * the selector, DOM node, or jQuery object passed.
     * @param element A DOM node indicating where to stop matching following sibling elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextUntil(Element element, String filter);

    /**
     * Get all following siblings of each element up to but not including the element matched by
     * the selector, DOM node, or jQuery object passed.
     * @param element A jQuery object indicating where to stop matching following sibling elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextUntil(JQueryElement element);

    /**
     * Get all following siblings of each element up to but not including the element matched by
     * the selector, DOM node, or jQuery object passed.
     * @param element A jQuery object indicating where to stop matching following sibling elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement nextUntil(JQueryElement element, String filter);

    /**
     * Remove elements from the set of matched elements.
     * @param selector A string containing a selector expression, a DOM element, or an array of
     *                 elements to match against the set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement not(String selector);

    /**
     * Remove elements from the set of matched elements.
     * @param elements An existing jQuery object to match the current set of elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement not(Element... elements);

    /**
     * Remove elements from the set of matched elements.
     * @param selection An existing jQuery object to match the current set of elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement not(JQueryElement selection);

    /**
     * Get the current coordinates of the first element in the set of matched elements,
     * relative to the document.
     * @return returns an object containing the properties top and left.
     */
    public native Offset offset();

    /**
     * Set the current coordinates of every element in the set of matched elements,
     * relative to the document.
     * @param coordinates An object containing the properties top and left, which are numbers indicating
     *                    the new top and left coordinates for the elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement offset(Offset coordinates);

    /**
     * Get the closest ancestor element that is positioned.
     * @return Closest jQuery parent element.
     */
    public native JQueryElement offsetParent();

    /**
     * Get the current computed height for the first element in the set of matched elements, including
     * padding, border, and optionally margin. Returns a number (without "px") representation of the value
     * or null if called on an empty set of elements.
     * <pre><img src="https://api.jquery.com/resources/0042_04_03.png"/></pre>
     */
    public native int outerHeight();

    /**
     * Get the current computed height for the first element in the set of matched elements, including
     * padding, border, and optionally margin. Returns a number (without "px") representation of the value
     * or null if called on an empty set of elements.
     * <pre><img src="https://api.jquery.com/resources/0042_04_03.png"/></pre>
     * @param includeMargin A Boolean indicating whether to include the element's margin in the calculation.
     */
    public native int outerHeight(boolean includeMargin);

    /**
     * Set the CSS outer Height of each element in the set of matched elements.
     * @param value A number representing the number of pixels.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement outerHeight(int value);

    /**
     * Set the CSS outer Height of each element in the set of matched elements.
     * @param value A number along with an optional unit of measure appended (as a string).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement outerHeight(String value);

    /**
     * Get the current computed width for the first element in the set of matched elements, including
     * padding, border, and optionally margin. Returns a number (without "px") representation of the value
     * or null if called on an empty set of elements.
     * <pre><img src="https://api.jquery.com/resources/0042_04_03.png"/></pre>
     */
    public native int outerWidth();

    /**
     * Get the current computed width for the first element in the set of matched elements, including
     * padding, border, and optionally margin. Returns a number (without "px") representation of the value
     * or null if called on an empty set of elements.
     * <pre><img src="https://api.jquery.com/resources/0042_04_03.png"/></pre>
     * @param includeMargin A Boolean indicating whether to include the element's margin in the calculation.
     */
    public native int outerWidth(boolean includeMargin);

    /**
     * Set the CSS outer Width of each element in the set of matched elements.
     * @param value A number representing the number of pixels.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement outerWidth(int value);

    /**
     * Set the CSS outer Width of each element in the set of matched elements.
     * @param value A number along with an optional unit of measure appended (as a string).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement outerWidth(String value);

    /**
     * Get the parent of each element in the current set of matched elements, optionally
     * filtered by a selector.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parent();

    /**
     * Get the parent of each element in the current set of matched elements, optionally
     * filtered by a selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parent(String selector);

    /**
     * Get the ancestors of each element in the current set of matched elements, optionally
     * filtered by a selector.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parents();

    /**
     * Get the ancestors of each element in the current set of matched elements, optionally
     * filtered by a selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parents(String selector);

    /**
     * Get the ancestors of each element in the current set of matched elements, up to but not including
     * the element matched by the selector, DOM node, or jQuery object.
     * @param selector A string containing a selector expression to indicate where to
     *                 stop matching ancestor elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parentsUntil(String selector);

    /**
     * Get the ancestors of each element in the current set of matched elements, up to but not including
     * the element matched by the selector, DOM node, or jQuery object.
     * @param selector A string containing a selector expression to indicate where to
     *                 stop matching ancestor elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parentsUntil(String selector, String filter);

    /**
     * Get the ancestors of each element in the current set of matched elements, up to but not including
     * the element matched by the selector, DOM node, or jQuery object.
     * @param element A DOM node indicating where to stop matching ancestor elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parentsUntil(Element element);

    /**
     * Get the ancestors of each element in the current set of matched elements, up to but not including
     * the element matched by the selector, DOM node, or jQuery object.
     * @param element A DOM node indicating where to stop matching ancestor elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parentsUntil(Element element, String filter);

    /**
     * Get the ancestors of each element in the current set of matched elements, up to but not including
     * the element matched by the selector, DOM node, or jQuery object.
     * @param element A jQuery object indicating where to stop matching ancestor elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parentsUntil(JQueryElement element);

    /**
     * Get the ancestors of each element in the current set of matched elements, up to but not including
     * the element matched by the selector, DOM node, or jQuery object.
     * @param element A jQuery object indicating where to stop matching ancestor elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement parentsUntil(JQueryElement element, String filter);

    /**
     * Get the current coordinates of the first element in the set of matched elements,
     * relative to the offset parent.
     * @return Position value as top and left.
     */
    public native Offset position();

    /**
     * Get the immediately preceding sibling of each element in the set of matched elements,
     * optionally filtered by a selector.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prev();

    /**
     * Get the immediately preceding sibling of each element in the set of matched elements,
     * optionally filtered by a selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prev(String selector);

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevAll();

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevAll(String selector);

    /**
     * GGet all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param selector A string containing a selector expression to indicate where to
     *                 stop matching ancestor elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevUntil(String selector);

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param selector A string containing a selector expression to indicate where to
     *                 stop matching ancestor elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevUntil(String selector, String filter);

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param element A DOM node indicating where to stop matching ancestor elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevUntil(Element element);

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param element A DOM node indicating where to stop matching ancestor elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevUntil(Element element, String filter);

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param element A jQuery object indicating where to stop matching ancestor elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevUntil(JQueryElement element);

    /**
     * Get all preceding siblings of each element up to but not including the element matched
     * by the selector, DOM node, or jQuery object.
     * @param element A jQuery object indicating where to stop matching ancestor elements.
     * @param filter A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prevUntil(JQueryElement element, String filter);

    /**
     * Get the value of a property for the first element in the set of matched elements.
     * @param propertyName The name of the property to get.
     */
    public native Object prop(String propertyName);

    /**
     * Set one or more properties for the set of matched elements.
     * @param propertyName The name of the property to set.
     * @param value A value to set for the property.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prop(String propertyName, Object value);

    /**
     * Set one or more properties for the set of matched elements.
     * @param properties An object of property-value pairs to set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement prop(Object properties);

    /**
     * Remove the set of matched elements from the DOM.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement remove();

    /**
     * Remove the set of matched elements from the DOM.
     * @param selector A selector expression that filters the set of matched elements to be removed.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement remove(String selector);

    /**
     * Remove an attribute from each element in the set of matched elements.
     * @param attributeName An attribute to remove; as of version 1.7, it can be a space-separated
     *                      list of attributes.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeAttr(String attributeName);

    /**
     * Remove a single class, multiple classes, or all classes from each element
     * in the set of matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeClass();

    /**
     * Remove a single class, multiple classes, or all classes from each element
     * in the set of matched elements.
     * @param className One or more space-separated classes to be removed from the class
     *                  attribute of each matched element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeClass(String className);

    /**
     * Remove a previously-stored piece of data.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeData();

    /**
     * Remove a previously-stored piece of data.
     * @param name A string naming the piece of data to delete.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeData(String name);

    /**
     * Remove a previously-stored piece of data.
     * @param list An array or space-separated string naming the pieces of data to delete.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeData(String[] list);

    /**
     * Remove a property for the set of matched elements.
     * @param propertyName The name of the property to remove.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement removeProp(String propertyName);

    /**
     * Replace each target element with the set of matched elements.
     * @param target A selector string of elements indicating which element(s) to replace.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement replaceAll(String target);

    /**
     * Replace each target element with the set of matched elements.
     * @param target A jQuery object of elements indicating which element(s) to replace.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement replaceAll(JQueryElement target);

    /**
     * Replace each target element with the set of matched elements.
     * @param target DOM element(s) indicating which element(s) to replace.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement replaceAll(Element... target);

    /**
     * Replace each element in the set of matched elements with the provided new content and
     * return the set of elements that was removed.
     * @param newContent The content to insert an HTML string.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement replaceWith(String newContent);

    /**
     * Replace each element in the set of matched elements with the provided new content and
     * return the set of elements that was removed.
     * @param newContent The content to insert, a jQuery object.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement replaceWith(JQueryElement newContent);

    /**
     * Replace each element in the set of matched elements with the provided new content and
     * return the set of elements that was removed.
     * @param newContent The content to insert, a DOM element(s).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement replaceWith(Element... newContent);

    /**
     * Get the current horizontal position of the scroll bar for the first element in the
     * set of matched elements.
     * @return The horizontal scroll position is the same as the number of pixels that are
     * hidden from view to the left of the scrollable area. If the scroll bar is at the very
     * left, or if the element is not scrollable, this number will be 0.
     */
    public native int scrollLeft();

    /**
     * Set the current horizontal position of the scroll bar for each of the set of matched elements.
     * @param value An integer indicating the new position to set the scroll bar to.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement scrollLeft(int value);

    /**
     * Get the current vertical position of the scroll bar for the first element in the set of
     * matched elements or set the vertical position of the scroll bar for every matched element.
     * @return The vertical scroll position is the same as the number of pixels that are hidden
     * from view above the scrollable area. If the scroll bar is at the very top, or if the element
     * is not scrollable, this number will be 0.
     */
    public native int scrollTop();

    /**
     * Set the current vertical position of the scroll bar for each of the set of matched elements.
     * @param value An integer indicating the new position to set the scroll bar to.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement scrollTop(int value);

    /**
     * Encode a set of form elements as a string for submission.
     * @return he .serialize() method creates a text string in standard URL-encoded notation.
     * It can act on a jQuery object that has selected individual form controls.
     */
    public native String serialize();

    /**
     * Encode a set of form elements as an array of names and values.
     * @return creates a JavaScript array of objects, ready to be encoded as a JSON string.
     * It operates on a jQuery collection of forms and/or form controls.
     */
    public native Object[] serializeArray();

    /**
     * Display the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement show();

    /**
     * Display the matched elements.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement show(double duration);

    /**
     * Display the matched elements.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement show(AnimateOptions options);

    /**
     * Get the siblings of each element in the set of matched elements, optionally
     * filtered by a selector.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement siblings();

    /**
     * Get the siblings of each element in the set of matched elements, optionally
     * filtered by a selector.
     * @param selector A string containing a selector expression to match elements against.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement siblings(String selector);

    /**
     * Return the number of elements in the jQuery object.
     * @deprecated Use the .length property instead.
     */
    public native int size();

    /**
     * Reduce the set of matched elements to a subset specified by a range of indices.
     * @param start An integer indicating the 0-based position at which the elements begin to be
     *              selected. If negative, it indicates an offset from the end of the set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slice(int start);

    /**
     * Reduce the set of matched elements to a subset specified by a range of indices.
     * @param start An integer indicating the 0-based position at which the elements begin to be
     *              selected. If negative, it indicates an offset from the end of the set.
     * @param end An integer indicating the 0-based position at which the elements stop being selected.
     *            If negative, it indicates an offset from the end of the set. If omitted, the range
     *            continues until the end of the set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slice(int start, int end);

    /**
     * Display the matched elements with a sliding motion.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideDown();

    /**
     * Display the matched elements with a sliding motion.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideDown(double duration);

    /**
     * Display the matched elements with a sliding motion.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideDown(AnimateOptions options);

    /**
     * Display or hide the matched elements with a sliding motion.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideToggle();

    /**
     * Display or hide the matched elements with a sliding motion.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideToggle(double duration);

    /**
     * Display or hide the matched elements with a sliding motion.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideToggle(AnimateOptions options);

    /**
     * Hide the matched elements with a sliding motion.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideUp();

    /**
     * Hide the matched elements with a sliding motion.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideUp(double duration);

    /**
     * Hide the matched elements with a sliding motion.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement slideUp(AnimateOptions options);

    /**
     * Stop the currently-running animation on the matched elements.
     * @param clearQueue A Boolean indicating whether to remove queued animation as well.
     *                   Defaults to false.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement stop(boolean clearQueue);

    /**
     * Stop the currently-running animation on the matched elements.
     * @param queue The name of the queue in which to stop animations.
     * @param clearQueue A Boolean indicating whether to remove queued animation as well.
     *                   Defaults to false.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement stop(String queue, boolean clearQueue);

    /**
     * Stop the currently-running animation on the matched elements.
     * @param clearQueue A Boolean indicating whether to remove queued animation as well.
     *                   Defaults to false.
     * @param jumpToEnd A Boolean indicating whether to complete the current animation
     *                  immediately. Defaults to false.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement stop(boolean clearQueue, boolean jumpToEnd);

    /**
     * Stop the currently-running animation on the matched elements.
     * @param queue The name of the queue in which to stop animations.
     * @param clearQueue A Boolean indicating whether to remove queued animation as well.
     *                   Defaults to false.
     * @param jumpToEnd A Boolean indicating whether to complete the current animation
     *                  immediately. Defaults to false.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement stop(String queue, boolean clearQueue, boolean jumpToEnd);

    /**
     * Get the combined text contents of each element in the set of matched elements,
     * including their descendants.
     * @return a string containing the combined text of all matched elements.
     */
    public native String text();

    /**
     * Set the content of each element in the set of matched elements to the specified text.
     * @param text The text to set as the content of each matched element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement text(String text);

    /**
     * Set the content of each element in the set of matched elements to the specified text.
     * @param value The text to set as the content of each matched element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement text(int value);

    /**
     * Set the content of each element in the set of matched elements to the specified text.
     * @param value The text to set as the content of each matched element.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement text(boolean value);

    /**
     * Retrieve all the elements contained in the jQuery set, as an array.
     * @return all of the elements in the jQuery set.
     */
    public native Element[] toArray();

    /**
     * Display or hide the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggle();

    /**
     * Display or hide the matched elements.
     * @param duration A string or number determining how long the animation will run.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggle(double duration);

    /**
     * Display or hide the matched elements.
     * @param duration A string or number determining how long the animation will run.
     * @param easing A string indicating which easing function to use for the transition.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggle(double duration, String easing);

    /**
     * Display or hide the matched elements.
     * @param options A map of additional options to pass to the method.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggle(AnimateOptions options);

    /**
     * Display or hide the matched elements.
     * @param display Use true to show the element or false to hide it.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggle(boolean display);

    /**
     * Add or remove one or more classes from each element in the set of matched elements,
     * depending on either the class's presence or the value of the state argument.
     * @param className One or more class names (separated by spaces) to be toggled for
     *                  each element in the matched set.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggleClass(String className);

    /**
     * Add or remove one or more classes from each element in the set of matched elements,
     * depending on either the class's presence or the value of the state argument.
     * @param state A Boolean (not just truthy/falsy) value to determine whether the class
     *              should be added or removed.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggleClass(boolean state);

    /**
     * Add or remove one or more classes from each element in the set of matched elements,
     * depending on either the class's presence or the value of the state argument.
     * @param className One or more class names (separated by spaces) to be toggled for
     *                  each element in the matched set.
     * @param state A Boolean (not just truthy/falsy) value to determine whether the class
     *              should be added or removed.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement toggleClass(String className, boolean state);

    /**
     * Wrap an HTML structure around each element in the set of matched elements.
     * @param wrappingElement A selector or HTML string specifying the structure to wrap around the
     *                        matched elements. A selector matching more than one element, the first
     *                        element will be used.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrap(String wrappingElement);

    /**
     * Wrap an HTML structure around each element in the set of matched elements.
     * @param wrappingElement An element specifying the structure to wrap around the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrap(Element wrappingElement);

    /**
     * Wrap an HTML structure around each element in the set of matched elements.
     * @param wrappingElement A jQuery object specifying the structure to wrap around the matched elements.
     *                        When you pass a jQuery collection containing more than one element, or a
     *                        selector matching more than one element, the first element will be used.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrap(JQueryElement wrappingElement);

    /**
     * Wrap an HTML structure around all elements in the set of matched elements.
     * @param wrappingElement A selector or HTML string specifying the structure to wrap around the
     *                        matched elements. A selector matching more than one element, the first
     *                        element will be used.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrapAll(String wrappingElement);

    /**
     * Wrap an HTML structure around all elements in the set of matched elements.
     * @param wrappingElement An element specifying the structure to wrap around the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrapAll(Element wrappingElement);

    /**
     * Wrap an HTML structure around all elements in the set of matched elements.
     * @param wrappingElement A jQuery object specifying the structure to wrap around the matched elements.
     *                        When you pass a jQuery collection containing more than one element, or a
     *                        selector matching more than one element, the first element will be used.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrapAll(JQueryElement wrappingElement);

    /**
     * Wrap an HTML structure around the content of each element in the set of matched elements.
     * @param wrappingElement A selector or HTML string specifying the structure to wrap around the
     *                        matched elements. A selector matching more than one element, the first
     *                        element will be used.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrapInner(String wrappingElement);

    /**
     * Wrap an HTML structure around the content of each element in the set of matched elements.
     * @param wrappingElement An element specifying the structure to wrap around the matched elements.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrapInner(Element wrappingElement);

    /**
     * Wrap an HTML structure around the content of each element in the set of matched elements.
     * @param wrappingElement A jQuery object specifying the structure to wrap around the matched elements.
     *                        When you pass a jQuery collection containing more than one element, or a
     *                        selector matching more than one element, the first element will be used.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement wrapInner(JQueryElement wrappingElement);

    /**
     * Remove the parents of the set of matched elements from the DOM,
     * leaving the matched elements in their place.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement unwrap();

    /**
     * Get the current value of the first element in the set of matched elements.
     */
    public native Object val();

    /**
     * Set the value of each element in the set of matched elements.
     * @param value A string of text corresponding to the value of each matched element
     *              to set as selected/checked.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement val(String value);

    /**
     * Set the value of each element in the set of matched elements.
     * @param value a number corresponding to the value of each matched element to set
     *              as selected/checked.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement val(int value);

    /**
     * Set the value of each element in the set of matched elements.
     * @param value An array of strings corresponding to the value of each matched element
     *              to set as selected/checked.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement val(String[] value);

    /**
     * Get the current computed width for the first element in the set of matched elements.
     * <pre><img src="https://api.jquery.com/resources/0042_04_04.png"/></pre>
     */
    public native int width();

    /**
     * Set the CSS width of each element in the set of matched elements.
     * @param width An integer representing the number of pixels.
     * @return self {@link JQueryElement}
     */
    public native JQueryElement width(int width);

    /**
     * Set the CSS width of each element in the set of matched elements.
     * @param width An integer along with an optional unit of measure appended (as a string).
     * @return self {@link JQueryElement}
     */
    public native JQueryElement width(String width);

}
