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

/**
 * A variation of {@link INavigationHandler} that exposes an underlying handler
 * via its implementation of {@link INavigationHandlerProvider}.
 * <p>
 * Generally this is used for implementations of {@link INavigationHandler} that
 * delegate through to the underlying handler resolved via
 * {@link INavigationHandlerProvider}.
 */
public interface INavigationHandlerWithProvider extends INavigationHandler, INavigationHandlerProvider {

    /**
     * Delegates to
     * {@link #navigationHandler()#navigate(INavigationHandler.NavigationContext,
     * List)}.
     */
    @Override
    default public void navigate(NavigationContext context, List<String> path) {
        if (handler() != null)
            handler().navigate (context, path);
    }

    /**
     * Delegates to
     * {@link #navigationHandler()#renavigate(INavigationHandler.NavigationContext)}.
     */
    default public void renavigate(NavigationContext context) {
        if (handler() != null)
            handler().renavigate (context);
    }

    /**
     * Delegates to {@link #navigationHandler()#deactivate()}.
     */
    default public void deactivate() {
        if (handler() != null)
            handler().deactivate ();
    }

    /**
     * Delegates to {@link #navigationHandler()#activeItem()}.
     */
    default public INavigationItem activeItem() {
        if (handler() == null)
            return null;
        return handler().activeItem ();
    }

    /**
     * Delegates to
     * {@link #navigationHandler()#assignParent(INavigationHandlerParent)}.
     */
    default public void assignParent(INavigationHandlerParent parent) {
        if (handler() != null)
            handler().assignParent (parent);
    }
}
