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
package com.effacy.jui.platform.css.client;

import java.util.ArrayList;
import java.util.List;

import org.gwtproject.core.client.Scheduler;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Used to inject CSS into the page.
 * <p>
 * This will wait until the completion of the current browser event loop then
 * combine all CSS being injected into a single SCRIPT elements added to the
 * document head (so a head must be declared).
 */
public class CssInjector {

    /**
     * The CSS being injected.
     */
    private static List<String> INJECTION = new ArrayList<>();

    /**
     * Injects the passed CSS into the page (this is deferred to the end of the
     * current browser event loop).
     * 
     * @param cssText
     *                the CSS to inject.
     */
    public static void inject(String cssText) {
        boolean schedule = INJECTION.isEmpty ();
        if (cssText != null)
            INJECTION.add (cssText);
        if (schedule) {
            Scheduler.get().scheduleFinally (() -> {
                if (INJECTION.isEmpty())
                    return;
                if (DomGlobal.document.head == null) {
                    DomGlobal.window.alert("A head element is required for JUI script injection");
                } else {
                    Element el = DomGlobal.document.createElement("style");
                    el.setAttribute ("language", "text/css");
                    String contents = "";
                    for (String css : INJECTION)
                        contents += css;
                    INJECTION.clear ();
                    innerText(el, contents);
                    DomGlobal.document.head.append (el);
                }
            });
        }
    }

    /**
     * Called by {@link #inject(String)} to set the body content as text.
     * 
     * @param el
     *             the element to assign the content to.
     * @param text
     *             the content to assign as text.
     */
    private static native void innerText(Element el, String text) /*-{
        el.innerText = text;
    }-*/;
}
