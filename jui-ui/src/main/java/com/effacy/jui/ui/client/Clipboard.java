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

import com.effacy.jui.platform.util.client.ScriptInjector;

import elemental2.dom.Element;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Provides copy-to-clipboard functionality. Makes use of https://clipboardjs.com.
 */
public class Clipboard {

    /**
     * Initialised the clipboard. Must be done in the application entry point to
     * give sufficient time for the JS to load and initialise.
     */
    public static void init() {
        // ScriptInjector.injectFromUrl ("https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.11/clipboard.min.js");
        ScriptInjector.injectFromModuleBase ("clipboard.js");
    }

    /**
     * Enables copy-to-clipboard functionality to the passed element using the
     * second element as the target for contents. An optional activate (CSS) class
     * can be added to the element when activated (and is removed a short time
     * later).
     * 
     * @param el
     *           the element that, when clicked on, activates copy.
     * @param t
     *           the target element that contains the content to copy.
     * @return a handler.
     */
    public static native ClipboardJS enableCopyToClipboard(Element el, Element t, String activateClass) /*-{
        var clipboard = new $wnd.ClipboardJS (el, {
            target: function() {
                return t;
            }
        });
        clipboard.on ('success', function(e) {
            if (activateClass != null) {
            el.classList.add(activateClass);
                $wnd.setTimeout(function() {
                    el.classList.remove(activateClass);
                }, 1500);
            }
            e.clearSelection();
        });
        clipboard.on('error', function(e) {
            $wnd.alert("Sorry, could not copy to clipboard (possibly the browser is blocking this).")
        });
        return clipboard;
    }-*/;

    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    public static class ClipboardJS {

        /**
         * Destroy the copy-to-clipboard handler.
         */
        public native void destroy();        
    }
}
