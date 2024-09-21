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
package com.effacy.jui.core.client.dom.css;

import java.util.HashSet;
import java.util.Set;

import com.effacy.jui.platform.util.client.Resources;
import com.effacy.jui.platform.util.client.TimerSupport;

import elemental2.dom.Document;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;

/**
 * Used to inject CSS from a URL.
 *
 * @author Jeremy Buckley
 */
public class CSSInjector {

    /**
     * Record of URL's that have been injected already, so we don't inject more than
     * once.
     */
    private static Set<String> INJECTED = new HashSet<>();

    /**
     * Injects from the <code>public</code> directory off the module base.
     * <p>
     * It is safe to call this multiple times with the same path (only the first
     * will be injected).
     * 
     * @param path
     *             the path relative to the <code>public</code> directory (no
     *             leading slash).
     */
    public static void injectFromModuleBase(String path) {
        injectFromUrl (Resources.staticModuleBase (path));
    }

    /**
     * Injects from the passed URL.
     * <p>
     * It is safe to call this multiple times with the same URL (only the first
     * will be injected).
     * 
     * @param url
     *            the URL.
     */
    public static void injectFromUrl(String url) {
        if (url == null)
            return;
        if (INJECTED.contains (url))
            return;
        INJECTED.add (url);
        TimerSupport.defer (() -> {
            Document document = DomGlobal.document;
            Element link = document.createElement ("link");
            link.setAttribute ("rel", "stylesheet");
            link.setAttribute ("type", "text/css");
            link.setAttribute ("href", url);
            document.getElementsByTagName ("head").getAt (0).appendChild (link);
        });
    }
}
