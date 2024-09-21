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
package com.effacy.jui.platform.util.client;

import java.util.Optional;
import java.util.function.Consumer;

import com.effacy.jui.platform.core.client.ApplicationEntryPointLifecycle;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.base.Js;

public class ScriptInjector {

    public static void injectFromUrl(String url) {
        injectFromUrl (url, null);
    }

    public static void injectFromUrl(String url, Consumer<Optional<Exception>> cb) {
        ApplicationEntryPointLifecycle.Callback callback = ApplicationEntryPointLifecycle.delay();
        com.google.gwt.core.client.ScriptInjector.fromUrl (url)
            .setWindow (Js.cast (DomGlobal.window))
            .setCallback(new com.google.gwt.core.client.Callback<Void,Exception>() {

                @Override
                public void onFailure(Exception reason) {
                    callback.complete();
                    if (cb != null)
                        cb.accept (Optional.of (reason));
                }

                @Override
                public void onSuccess(Void result) {
                    callback.complete();
                    if (cb != null)
                        cb.accept (Optional.empty ());
                }

            })
            .inject ();
    }

    public static void injectFromString(String content) {
        injectFromString (null, content);
    }

    public static void injectFromString(String type, String content) {
        Element script = DomGlobal.document.createElement ("script");
        if (type != null)
            script.setAttribute("type", type);
        script.textContent = content;
        DomGlobal.document.head.append (script);
    }

    public static void injectFromModuleBase(String resource) {
        injectFromUrl (Resources.staticModuleBase (resource));
    }

    public static void injectFromModuleBase(String resource, Consumer<Optional<Exception>> cb) {
        injectFromUrl (Resources.staticModuleBase (resource), cb);
    }
}
