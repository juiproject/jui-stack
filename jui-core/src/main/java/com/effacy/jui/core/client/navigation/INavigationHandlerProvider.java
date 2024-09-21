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

import java.util.function.Consumer;

/**
 * Implemented by anything that is able to participate in the navigation
 * structure. It is responsible for delivering a suitable navigation handler
 * from which the navigation flow can continue through.
 * <p>
 * For example a component can be made to contribute to navigation by
 * implementing this interface and overriding {@link #handler()}. This should
 * return a suitable navigation handler such as the default
 * {@link NavigationHandler} (which can be set as an instance member of the
 * component). The component can register navigable components directly against
 * the navigation handler and implement how they are enabled. It can also invoke
 * navigation events directly against the handler.
 *
 * @author Jeremy Buckley
 */
public interface INavigationHandlerProvider {

    /**
     * If the item is associated with a handler then return that. Note that if
     * the item implements {@link INavigationHandler} then the item will be
     * returned by default.
     * <p>
     * If a handler is associated then it will be listened to for navigation
     * events. When an event is received the containing handler (the one that
     * contains this item) will assume that something under the item has been
     * navigated to and will perform a navigation action to bring this item into
     * view. That way navigation is chained up from the bottom when it comes to
     * rendering.
     * 
     * @return the handler (or {@code null}).
     */
    default INavigationHandler handler() {
        if (this instanceof INavigationHandler)
            return (INavigationHandler) this;
        return null;
    }


    /**
     * See {@link #handler()}. This checks if a handler is available and if so
     * runs the passed action on it.
     * <p>
     * The default implementation calls {@link #handler()} to obtain the
     * undelying handler.
     * 
     * @param c
     *            the action to run.
     * @return {@code true} if a handler was returned.
     */
    default boolean handler(Consumer<INavigationHandler> c) {
        INavigationHandler handler = handler ();
        if (handler == null)
            return false;
        c.accept (handler);
        return true;
    }
}
