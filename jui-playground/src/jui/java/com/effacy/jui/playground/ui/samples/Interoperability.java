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
package com.effacy.jui.playground.ui.samples;

import java.util.function.Supplier;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.builder.Custom;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.button.IButtonHandler;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;

import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

/**
 * A collection of examples to demonstrate how one can work interoperablity
 * between JUI and JavaScript.
 *
 * @author Jeremy Buckley
 */
public class Interoperability extends Panel {

    /**
     * Version of VueJS to include.
     */
    private static final String VUEJS_VERSION = "2.5.16";
    static {
        // Load VueJS, but this needs to be loaded before the next script (hence the
        // callback).
        ScriptInjector.fromUrl ("https://cdn.jsdelivr.net/npm/vue@" + VUEJS_VERSION + "/dist/vue.js").setWindow (Js.cast (DomGlobal.window)).setCallback (new Callback<Void, Exception> () {

            @Override
            public void onFailure(Exception reason) {
                NotificationDialog.error ("Failed to load VueJS", "The script for VueJS did not load, so the examples on this page will not work.", null);
            }

            @Override
            public void onSuccess(Void result) {
                // Load external script to interact with. This is found in the resources source
                // tree under "public" in the same package as the module file.
                ScriptInjector.fromUrl (GWT.getModuleBaseForStaticFiles () + "interoperability.js").setWindow (Js.cast (DomGlobal.window)).inject ();
            }
        }).inject ();

    }

    /**
     * Construct an instance of the components panel with the demonstration
     * components in a vertical layout (down the page).
     */
    public Interoperability() {
        super (PanelCreator.config ().scrollable ().layout (VertLayout.$ ().separator (Separator.LINE).spacing (Length.em (1)).build ()));

        add (ButtonCreator.config ().label ("Button 1").handler (IButtonHandler.convert (() -> new Interop ().button1 ())).build ());
        add (ButtonCreator.config ().label ("Button 2").handler (IButtonHandler.convert (() -> new Interop ().button2 ())).build ());
        add (ButtonCreator.config ().label ("Button 3").handler (IButtonHandler.convert (() -> new Interop ().button3 ())).build ());

        // The details of this example are at
        // https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-vue.
        add (ButtonCreator.config ().label ("Button 4").handler (IButtonHandler.convert (() -> {
            ModalDialogCreator.build ((Supplier<Component<Component.Config>>) () -> {
                return ComponentCreator.build (root -> {
                    root.id ("interop_id");
                    Ul.$ (root).$ (ul -> {
                        Li.$ (ul).attr ("v-for", "player in players").$ (li -> {
                            Custom.$ (li, "player-card")
                                .attr ("v-bind:player", "player")
                                .attr ("v-bind:key", "player.id");
                        });
                    });
                }, dom -> {
                    // Need a bit of delay to ensure that we are in the DOM.
                    TimerSupport.defer (() -> new Interop ().attach ("interop_id"));
                });
            }, cfg -> {
                cfg.title ("VueJS dialog");
                cfg.width (Length.px (500));
                cfg.action (a -> a.label ("Close").handler (h -> h.success ()));
                cfg.removeOnClose ();
            }).open ();
        })).build ());

    }

}
