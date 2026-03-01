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
package com.effacy.jui.playground.ui;

import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.user.history.client.History;

import com.effacy.jui.core.client.dom.renderer.CachedDataRenderer;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext.Source;
import com.effacy.jui.core.client.navigation.INavigationHandlerParent;
import com.effacy.jui.core.client.navigation.NavigationSupport;
import com.effacy.jui.platform.core.client.ApplicationEntryPoint;
import com.effacy.jui.platform.util.client.ListSupport;

import elemental2.dom.DomGlobal;

/**
 * The entry point to the application. This expects to find an element with id
 * <code>pageBody</code> and will bind to that node. It will
 * then create a main application instance (see {@link PlaygroundUI}) and add
 * that to the root panel.
 *
 * @author Jeremy Buckley
 */
public class PlaygroundApp implements ApplicationEntryPoint {

    /**
     * Application instance.
     */
    private static PlaygroundUI APP;

    @Override
    public void onApplicationLoad() {
        // Debugging.
        CachedDataRenderer.DEBUG = false;

        // Configuration.
        //DiagramBlock.BASE_URL = "/uml/";

        // Setup the main application component.
        APP = new PlaygroundUI ();
        APP.bind ("pageBody");

        // This allows for updating the navigation path on the browser. This will also
        // update the navigation history.
        APP.assignParent (INavigationHandlerParent.navigation ((ctx, path) -> {
            DomGlobal.window.open ("#/" + ListSupport.contract (path, "/"), "_self", null);
        }));

        // Listen to history changes. Note that the update above will invoke such a
        // change but since we don't back-propagate non-changes then we can safely
        // re-navigate.
        History.addValueChangeHandler (new ValueChangeHandler<String> () {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (event != null)
                    APP.navigate (ListSupport.split (event.getValue (), '/'));
            }

        });
    }

    
    public static void navigation(String path) {
        APP.navigate (new NavigationContext (Source.INTERNAL), NavigationSupport.split (path));
    }
}
