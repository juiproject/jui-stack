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
package com.effacy.jui.core.client;

import com.effacy.jui.core.client.util.Tribool;
import com.effacy.jui.platform.util.client.Logger;
import com.google.gwt.core.client.GWT;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Node;
import elemental2.dom.NodeList;
import elemental2.dom.Range;

public class Debug {
    
    /**
     * Logs a simple debug message.
     * 
     * @param message the message.
     */
    public static void log(String message) {
        Logger.log (message);
    }

    /**
     * Logs a debug message with exception.
     * 
     * @param message the message.
     * @param e       the exception.
     */
    public static void log(String message, Throwable e) {
        Logger.error (message, e);
    }

    /**
     * Determines if one is operating in a test mode (i.e. running a standard Java
     * unit test).
     * 
     * @return {@code true} if so.
     */
    public static boolean isUnitTestMode() {
        return !GWT.isClient();
    }

    /**
     * See {@link #isTestMode()}
     */
    private static Tribool TEST_MODE = Tribool.UNDETERMINED;

    /**
     * Determines if one is operating in test mode which means that the code is
     * being tested. This is determined by checking for the existence of a
     * {@code meta} tag {@code <meta name="jui:test" content="true" />}.
     * 
     * @return {@code true} if so.
     */
    public static boolean isTestMode () {
        if (TEST_MODE.isUndetermined()) {
            // Attempt to determine from the meta-tags.
            Element tag = DomGlobal.document.querySelector("meta[name='jui:test']");
            if ((tag != null) && ("true".equalsIgnoreCase(tag.getAttribute("content")) || "true".equalsIgnoreCase(tag.getAttribute("value"))))
                TEST_MODE = Tribool.TRUE;
            else
                TEST_MODE = Tribool.FALSE; 
        }
        return TEST_MODE.isTrue();
    }

    /**
     * Convenience to print a range object.
     * 
     * @param r
     *          the range.
     * @return the string representation.
     */
    public static String print(Range r) {
        if (r == null)
            return "{null}";
        String str = "{";
        if (r.startContainer == null) {
            str += "null";
        } else {
            str += "(";
            str += print (r.startContainer);
            str += ",";
            str += r.startOffset;
            str += ")";
        }
        str += ",";
        if (r.endContainer == null) {
            str += "null";
        } else {
            str += "(";
            str += print (r.endContainer);
            str += ",";
            str += r.endOffset;
            str += ")";
        }
        str += "}";
        return str;
    }

    public static String print(Node n) {
        String str = "[";
        if (n.nodeType == 3) {
            str += "Text";
            if (n.textContent != null) {
                str += "<";
                if (n.textContent.length() < 10)
                    str += n.textContent;
                else
                    str += n.textContent.substring(0, 10) + "...";
                str += ">";
            }
        } else
            str += n.nodeName;
        int idx = 0;
        while ((idx = indexOf (n)) != -1) {
            str += ",";
            str += idx;
            n = n.parentNode;
        }
        str += "]";
        return str;
    }

    protected static int indexOf(Node child) {
        if (child == null)
            return -1;
        if (child.parentElement == null)
            return -1;
        NodeList<Node> nodes = child.parentElement.childNodes;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes.getAt(i) == child)
                return i;
        }
        return -1;
    }
}
