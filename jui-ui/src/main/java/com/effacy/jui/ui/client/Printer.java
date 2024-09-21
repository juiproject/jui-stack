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
package com.effacy.jui.ui.client;

import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.core.client.dom.css.Length;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;

/**
 * Used to print the contents of an element.
 * <p>
 * It works by creating a special container attached to the document body that
 * does not display when not printing. When printing (via this method) the
 * container does display and the rest of the DOM at the same level is hidden.
 * As such, only the printer is printed.
 * <p>
 * The container is populated with the inner HTML of the element being printer
 * and the CSS styles attached to that element are copied across. That should
 * (in the most part) preserve the formatting.
 * <p>
 * A common problem is the splitting of text across page breaks (for example, a
 * list item). To ameliorate this you can use the CSS
 * <code>break-inside: avoid;</code> that will keep the content (as best as
 * possible) together without splitting.
 * <p>
 * A final note is that the CSS that comes with this does use the
 * <code>@media print</code> directive to conditionally apply formatting. Some
 * formatting is applied that is global in nature (i.e. to the <code>html</code>
 * element and using the <code>@page</code> directive). This has been kept to
 * a minimum but could interfere with other printing scenarios that fall outside
 * of this. For reference see that <code>jui_printer.css</code> in the module
 * base (this can be overridden if necessary using the <code>super</code>
 * mechanism).
 */
public class Printer {
    
    /**
     * ID for the top-level modals DIV. This is used to associated a root panel
     * specifically for modals.
     */
    private static String CONTAINER_NAME = "jui-printer-container";
    static {
        /**
         * For reference the following will remove the page margin while reproducing it
         * on the <code>html</code> element. This has the effect of hiding the page
         * number and file data that normally appears in the page header and footer.
         * <tt>
         * @page {
         *   margin: 0;
         * }
         * html {
         *   padding: 1.6cm;
         *   box-sizing: border-box;
         * }
         * </tt>
         */
        CSSInjector.injectFromModuleBase("jui_printer.css");
    }
    private static Element TOP_EL;
    static {
        TOP_EL = DomSupport.createElement ("div");
        TOP_EL.id = CONTAINER_NAME;
        TOP_EL.classList.add ("jui_printer");
        DomGlobal.document.body.appendChild (TOP_EL);
    }

    /**
     * Prints the given element.
     * <p>
     * See {@link #print(Element, Consumer, String...)}.
     * 
     * @param el
     *                 the element to print.
     * @param styles
     *                 any additional styles to apply (as needed).
     */
    public static void print(Element el, String... styles) {
        print (el, (Consumer<Element>) null, styles);
    }

    /**
     * Prints the given element.
     * <p>
     * See {@link #print(Element, Consumer, String...)}.
     * 
     * @param el
     *               the element to print.
     * @param width
     *               a fixed with to constrain to.
     * @param styles
     *               any additional styles to apply (as needed).
     */
    public static void print(Element el, Length width, String... styles) {
        print (el, container -> CSS.WIDTH.apply (container, width), styles);
    }

    /**
     * Prints the given element.
     * 
     * @param el
     *                 the element to print.
     * @param modifier
     *                 (optional) to modify the root element (i.e. to place sizing,
     *                 etc).
     * @param styles
     *                 any additional styles to apply (as needed).
     */
    public static void print(Element el, Consumer<Element> modifier, String... styles) {
        DomSupport.removeAllChildren (TOP_EL);
        if (el == null)
            return;
        HTMLDivElement printEl = DomSupport.createDiv ();
        for (String klass : el.classList.asList ())
            printEl.classList.add (klass);
        printEl.innerHTML = el.innerHTML;
        if (modifier != null)
            modifier.accept (printEl);
        TOP_EL.appendChild (printEl);
        DomGlobal.document.body.classList.add ("jui_printer");
        DomGlobal.window.print ();
        DomGlobal.document.body.classList.remove ("jui_printer");
        DomSupport.removeAllChildren (TOP_EL);
    }

    /**
     * A style to use that does not print. You can either use this or directly use
     * <code>@media print {...}</code> in your CSS (which is preferable).
     * 
     * @return the style.
     */
    public static String noprint() {
        return "jui_printer_noprint";
    }

}
