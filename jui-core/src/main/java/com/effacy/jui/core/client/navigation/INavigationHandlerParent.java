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
package com.effacy.jui.core.client.navigation;

import java.util.List;
import java.util.function.BiConsumer;

import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;

/**
 * Represents a parent node in a navigation hierarcy.
 */
public interface INavigationHandlerParent {

    /**
     * Invoked when the handler being listened to has experienced a change in
     * navigation (which includes any child navigation).
     * <p>
     * This is activated on the back-propagation flow as the new tree activation is
     * resolved from leaf to root.
     * 
     * @param context
     *                the navigation context that the navigation was initiated with.
     * @param path
     *                the path within the handler being listened to that represents
     *                the updated path of navigation.
     */
    public void onNavigation(NavigationContext context, List<String> path);

    /**
     * Determines if the parent is active (in the sense that the parent hierarchy is
     * active in the navigation sequence).
     * 
     * @return {@code true} if it is.
     */
    default boolean isActive() {
        return true;
    }

    /**
     * Convenience to create a listener.
     * 
     * @param navigation
     *                   the handler for
     *                   {@link INavigationHandlerParent#onNavigation(NavigationContext, List)}.
     * @return the listener.
     */
    public static INavigationHandlerParent navigation(BiConsumer<NavigationContext, List<String>> navigation) {
        return new INavigationHandlerParent () {

            @Override
            public void onNavigation(NavigationContext context, List<String> path) {
                if (navigation != null)
                    navigation.accept (context, path);
            }

        };
    }

}
