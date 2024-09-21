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
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Represent a JQuery access model.
 *
 * @author Ben Dol
 */
@JsType(isNative = true)
public class JQuery {

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $();

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(String selector);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(String selector, JQueryElement context);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(JQueryElement element);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(JQueryElement[] element);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(JQueryElement element, Element context);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(JQueryElement element, JQueryElement context);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Element element);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Element[] element);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Element element, Element context);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Element element, JQueryElement context);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Node element);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Node[] element);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Node element, Element context);

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native JQueryElement $(Node element, JQueryElement context);

    @JsMethod(namespace = "$")
    public static native int inArray(Object value, Object[] array);

    @JsMethod(namespace = "$")
    public static native int inArray(Object value, Object[] array, double fromIndex);

    @JsMethod(namespace = "$")
    public static native boolean isArray(Object obj);

    @JsMethod(namespace = "$")
    public static native boolean isEmptyObject(Object obj);

    @JsMethod(namespace = "$")
    public static native boolean isFunction(Object obj);

    @JsMethod(namespace = "$")
    public static native boolean isNumeric(Object value);

    @JsMethod(namespace = "$")
    public static native boolean isPlainObject(Object object);

    @JsMethod(namespace = "$")
    public static native boolean isWindow(Object object);

    @JsMethod(namespace = "$")
    public static native boolean isXMLDoc(Element node);
}
