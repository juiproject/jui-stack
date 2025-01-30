package com.effacy.jui.core.client.dom.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import elemental2.dom.Element;

/**
 * To generate HTML content from a string with limited templating capabilities.
 * <p>
 * The content must have a single root tag and be well-formed. It should also be
 * used with care as content is generated directly via
 * {@link Element#innerHTML} which means that unsafe content can be injected if
 * it is not properly prepared.
 * <p>
 * See {@link HtmlBuilder} as the underlying {@link ElementBuilder} that this
 * class utilises.
 */
public class Html {

    /**
     * Generates HTML content.
     * 
     * @param html
     *             the html to generate.
     * @return the newly created element builder.
     */
    public static HtmlBuilder $(String html) {
        return new HtmlBuilder (html, null);
    }

    /**
     * Generates HTML content with property substitution. Properties are identified
     * by surrounding with <code>${</code> and <code>}</code>.
     * <p>
     * An example usage follows:
     * <tt>
     *   Html.$("""
     *     <a class='button'>
     *       <em class='${icon}'></em><span>select</span>
     *     </a>""",
     *     Map.of ("icon", FontAwesome.check())
     *   )
     * </tt>
     * 
     * @param html
     *                   the html to generate.
     * @param properties
     *                   the properties to use for replacement.
     * @return the newly created element builder.
     */
    public static HtmlBuilder $(String html, Map<String,String> properties) {
        return new HtmlBuilder (html, properties);
    }

    /**
     * See {@link #$(IDomInsertableContainer, String, Map)} where the properties are
     * prepared by way of a lambda-expression.
     * 
     * @param html
     *                   the html to generate.
     * @param properties
     *                   a builder for properties.
     * @return the newly created element builder.
     */
    public static HtmlBuilder $(String html, Consumer<Map<String,String>> properties) {
        if (properties != null) {
            Map<String,String> map = new HashMap<>();
            properties.accept(map);
            return $(html, map);
        }
        return $(html);
    }
    
    /**
     * See {@link #$(String)} but passing the parent node to insert into.
     */
    public static HtmlBuilder $(IDomInsertableContainer<?> parent, String text) {
        HtmlBuilder builder = $ (text);
        parent.insert (builder);
        return builder;
    }

    /**
     * See {@link #$(String, Map)} but passing the parent node to insert into.
     */
    public static HtmlBuilder $(IDomInsertableContainer<?> parent, String text, Map<String,String> properties) {
        HtmlBuilder builder = $ (text, properties);
        parent.insert (builder);
        return builder;
    }

    /**
     * See {@link #$(String, Consumer)} but passing the parent node to insert into.
     */
    public static HtmlBuilder $(IDomInsertableContainer<?> parent, String text, Consumer<Map<String,String>> properties) {
        HtmlBuilder builder = $ (text, properties);
        parent.insert (builder);
        return builder;
    }

}
