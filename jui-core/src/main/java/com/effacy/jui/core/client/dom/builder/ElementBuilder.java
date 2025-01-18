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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.css.CSS.CSSProperty;
import com.effacy.jui.core.client.dom.css.CSS.ICSSProperty;
import com.effacy.jui.platform.util.client.StringSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Node;

public class ElementBuilder extends ContainerBuilder<ElementBuilder> {

    /**
     * Construct with a tag name.
     * 
     * @param tag
     *            the name of the element tag.
     */
    public ElementBuilder(String tag) {
        this.tag = tag;
    }

    /**
     * Adds a class name (via provider) to render into the <code>class</code>
     * attribute.
     * 
     * @param classNames
     *                   the class name(s) to assign.
     * @return this element instance.
     */
    public ElementBuilder addClassName(String... classNames) {
        return addClassName (true, classNames);
    }

    /**
     * Adds a class name to the element.
     * 
     * @param condition
     *                   blocking condition (the class won't be applied if this is
     *                   {@code false}).
     * @param classNames
     *                   the class name(s) to assign.
     * @return this element instance.
     */
    public ElementBuilder addClassName(boolean condition, String... classNames) {
        return style (condition, classNames);
    }

    /**
     * Adds a class name (via provider) to render into the <code>class</code>
     * attribute.
     * 
     * @param classNames
     *                   the class name(s) to assign.
     * @return this element instance.
     */
    public ElementBuilder style(String... classNames) {
        return style (true, classNames);
    }

    /**
     * Adds a class name (via provider) to render into the <code>class</code>
     * attribute.
     * 
     * @param classNames
     *                   the class name(s) to assign.
     * @return this element instance.
     */
    public ElementBuilder style(List<String> classNames) {
        return style (true, classNames);
    }

    /**
     * Adds a class name to the element.
     * 
     * @param condition
     *                   blocking condition (the class won't be applied if this is
     *                   {@code false}).
     * @param classNames
     *                   the class name(s) to assign.
     * @return this element instance.
     */
    public ElementBuilder style(boolean condition, String... classNames) {
        if (!condition)
            return this;
        if (classNames != null) {
            if (klasses == null)
                klasses = new ArrayList<String> ();
            for (String className : classNames) {
                if (className == null)
                    continue;
                for (String classNamePart : className.split (" ")) {
                    classNamePart = classNamePart.trim ();
                    if (classNamePart.isEmpty ())
                        continue;
                    klasses.add (classNamePart);
                }
            }
        }
        return this;
    }

    /**
     * Adds a class name to the element.
     * 
     * @param condition
     *                   blocking condition (the class won't be applied if this is
     *                   {@code false}).
     * @param classNames
     *                   the class name(s) to assign.
     * @return this element instance.
     */
    public ElementBuilder style(boolean condition, List<String> classNames) {
        if (!condition)
            return this;
        if (classNames != null) {
            if (klasses == null)
                klasses = new ArrayList<String> ();
            for (String className : classNames) {
                if (className == null)
                    continue;
                for (String classNamePart : className.split (" ")) {
                    classNamePart = classNamePart.trim ();
                    if (classNamePart.isEmpty ())
                        continue;
                    klasses.add (classNamePart);
                }
            }
        }
        return this;
    }

    /**
     * Assigns a value to the <code>item</code> attribute.
     * 
     * @param item
     *             the value of the item.
     * @return this element instance.
     */
    public ElementBuilder item(String item) {
        return setAttribute ("item", item);
    }

    /**
     * Assigns a value to the <code>title</code> attribute.
     * 
     * @param title
     *             the value of the title.
     * @return this element instance.
     */
    public ElementBuilder title(String title) {
        return setAttribute ("title", title);
    }

    /**
     * Assigns a value to the <code>id</code> attribute (via a provider).
     * 
     * @param id
     *            the value of the id.
     * @return this element instance.
     */
    public ElementBuilder id(String id) {
        return setAttribute ("id", id);
    }

    /**
     * Assigns <code>true</code> to the <code>draggable</code> attribute.
     * 
     * @return this element instance.
     */
    public ElementBuilder draggable() {
        return setAttribute ("draggable", "true");
    }

    /**
     * Assigns a value to an attribute (via a provider).
     * 
     * @param attr
     *              the name of the attribute being set.
     * @param value
     *              the value of the attribute.
     * @return this element instance.
     */
    public ElementBuilder attr(String attr, Object value) {
        return setAttribute(attr, value);
    }

    /**
     * Assigns a value to an attribute (via a provider).
     * <p>
     * The passed value will be appropriately handled if it is a {@link String},
     * {@link SafeHtmlBuilder} or {@link SafeHtml}; otherwise it will be converted
     * to a string via a call to {@link Object#toString()}.
     * 
     * @param attr
     *              the name of the attribute being set.
     * @param value
     *              the value of the attribute.
     * @return this element instance.
     */
    public ElementBuilder setAttribute(String attr, Object value) {
        if (StringSupport.empty (attr))
            return this;
        attr = attr.toLowerCase ();
        if (value == null) {
            if (attributes == null)
                return this;
            attributes.remove (attr);
            return this;
        }
        if (attributes == null)
            attributes = new HashMap<String, String> ();
        if (value instanceof String)
            attributes.put (attr, (String) value);
        else if (value instanceof SafeHtmlBuilder)
            attributes.put (attr, ((SafeHtmlBuilder) value).toSafeHtml ().asString());
        else if (value instanceof SafeHtml)
            attributes.put (attr, ((SafeHtml) value).asString());
        else
            attributes.put (attr, value.toString ());
        return this;
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder htmlFor(Object value) {
        return setAttribute ("htmlFor", value);
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder ariaLabel(Object value) {
        return setAttribute ("aria-label", value);
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder key(Object value) {
        return setAttribute ("key", value);
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder name(Object value) {
        return setAttribute ("name", value);
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder value(Object value) {
        return setAttribute ("value", value);
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder readOnly(boolean value) {
        return setAttribute ("readOnly", value ? "true" : "false");
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder disabled(boolean value) {
        return setAttribute ("disabled", value ? "true" : "false");
    }
    
    /**
     * Convenience attribute setter.
     * 
     * @param value
     *              the value for the attribute.
     * @return this element instance.
     */
    public ElementBuilder checked(boolean value) {
        return setAttribute ("checked", value ? "true" : "false");
    }

    /**
     * Assigns the {@code test-id} attribute using the supplied value. However it
     * will only do this if running in test mode ({@see Debug#isTestMode()}).
     * 
     * @param value
     *              the test ID to assign.
     * @return the element instance.
     */
    public ElementBuilder testId(String value) {
        if (StringSupport.empty (value))
            return this;
        if (Debug.isTestMode())
            setAttribute ("test-id", value);
        return this;
    }

    /**
     * Assigns the {@code test-ref} attribute using the supplied value. However it
     * will only do this if running in test mode ({@see Debug#isTestMode()}).
     * 
     * @param value
     *              the test ID to assign.
     * @return the element instance.
     */
    public ElementBuilder testRef(String value) {
        if (StringSupport.empty (value))
            return this;
        if (Debug.isTestMode())
            setAttribute ("test-ref", value);
        return this;
    }

    /**
     * Assigns a CSS style.
     * <p>
     * This takes a CSS declaration as it would appear in a CSS file and splits it
     * into property and value.
     * <p>
     * Note that if the property is a CSS variable (i.e. starts with --) then it
     * will be assigned to the style via <code>setProperty</code> (rather than
     * <code>set</code>).
     * 
     * @param line
     *             combined property and value separated by a colon.
     * @return this element instance.
     */
    public ElementBuilder css(String line) {
        if (line == null)
            return this;
        for (String part : line.split(";")) {
            int idx = part.indexOf (':');
            if (idx > 0)
                css (part.substring (0, idx).trim (), part.substring (idx + 1).trim ());
        }
        return this;
    }

    /**
     * Conditionally assigns a CSS style.
     * <p>
     * See {@link #css(String))} for details.
     * 
     * @param line
     *                  combined property and value separated by a colon.
     * @param condition
     *                  test condition whether to apply the CSS.
     * @return this element instance.
     */
    public ElementBuilder css(String line, boolean condition) {
        if (!condition)
            return this;
        return css(line);
    }

    /**
     * Assigns a CSS style.
     * 
     * @param property
     *                 the CSS property (if the condition check fails the no
     *                 assignment is performed).
     * @param value
     *                 the value provider for the property.
     * @return this element instance.
     */
    public ElementBuilder css(String property, String value) {
        if (StringSupport.empty (property))
            return this;
        if (value == null) {
            if (css == null)
                return this;
            css.remove (property);
            return this;
        }
        if (css == null)
            css = new HashMap<String, String> ();
        css.put (property, value);
        return this;
    }

    /**
     * Assigns a CSS style.
     * 
     * @param property
     *                 the CSS property (if the condition check fails the no
     *                 assignment is performed).
     * @param value
     *                 the value provider for the property.
     * @return this element instance.
     */
    public <V extends ICSSProperty> ElementBuilder css(CSSProperty<V> property, V value) {
        if (property == null)
            return this;
        if (value == null) {
            if (css == null)
                return this;
            css.remove (property.text ());
            return this;
        }
        if (css == null)
            css = new HashMap<String, String> ();
        css.put (property.text (), value.value ());
        return this;
    }

    /**
     * The name of the element tag.
     */
    private String tag;

    /**
     * Collection of CSS class name providers.
     */
    private List<String> klasses;

    /**
     * Collection of attribute-value pair providers.
     */
    private Map<String, String> attributes;

    /**
     * Collection of css property-value pair providers.
     */
    private Map<String, String> css;

    /**
     * Events to sink.
     */
    private List<UIEventType> events;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.builder.NodeBuilder#build()
     */
    @Override
    protected Node _nodeImpl(Node parent, BuildContext ctx) {
        // There is a special case here where the tag is null. In that case we assume
        // that we are actually wrapping the parent as our own.
        Element element = (tag == null) ? (Element) parent : DomGlobal.document.createElement (tag);
        __nodeImpl (element);
        super._nodeImpl (element, ctx);
        return element;
    }

    /**
     * Invoked by {@link #_nodeImpl(BuildContext)} to adorn the element.
     */
    protected void __nodeImpl(Element element) {
        if (klasses != null) {
            for (String klass : this.klasses)
                element.classList.add (klass);
        }
        if (attributes != null) {
            for (Map.Entry<String, String> attribute : attributes.entrySet ()) {
                if (attribute.getKey () == null)
                    continue;
                if ("checked".equalsIgnoreCase (attribute.getKey())) {
                    // The checked attribute is a property which requires setting differently.
                    if ("true".equalsIgnoreCase (attribute.getValue ()))
                        ((HTMLInputElement) element).checked = true;
                } else if ("disabled".equalsIgnoreCase (attribute.getKey())) {
                    // The disabled attribute is a property which requires setting differently.
                    if ("true".equalsIgnoreCase (attribute.getValue ()))
                        ((HTMLInputElement) element).disabled = true;
                } else
                    element.setAttribute (attribute.getKey (), attribute.getValue ());
            }
        }
        if (css != null) {
            if (css != null) {
                for (String property : css.keySet ()) {
                    String value = css.get (property);
                    if (value == null)
                        continue;
                    if (StringSupport.empty (value))
                        continue;
                    if (property.startsWith("--"))
                        ((HTMLElement) element).style.setProperty (property, value);
                    else
                        ((HTMLElement) element).style.set (property, value);
                }
            }
        }
        if (events != null) {
            for (UIEventType event : events)
                event.attach (element);
        }
    }

}
