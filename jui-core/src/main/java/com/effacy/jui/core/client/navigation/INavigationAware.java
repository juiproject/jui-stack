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

import com.effacy.jui.core.client.IActivateAware;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.platform.util.client.Logger;

/**
 * Implemented by content that is aware of being navigated (both to and away).
 * Allows for the component to receive tab events that will invoke restrictions
 * to navigating away.
 *
 * @author Jeremy Buckley
 */
public interface INavigationAware {

    /**
     * Callback for managing navigation requests.
     */
    public interface INavigateCallback {

        /**
         * To proceed with the navigation request.
         */
        public void proceed();

        /**
         * To cancel (not proceed) with the navigation request.
         */
        public void cancel();
    }

    /**
     * Invoked when being deactiaved.
     * <p>
     * This is invoked whenever a sibling sub-tree is activated that this is not a
     * member of. It starts with the direct sibling then descends down through the
     * deactivated branch. This ensures that all items that were active and no
     * longer are receive this notification.
     * <p>
     * The default behaviour (which should continue to be respected) iterates over
     * each child component and if it implements {@link INavigationAwareChild} then
     * invoke {@link INavigationAwareChild#onNavigateDeactivated()} on the child.
     */
    default public void onNavigateDeactivated() {
        if (NavigationHandler.DebugMode.ACTIVATE.set ())
            Logger.log ("{nav-activation:deactivate} [" + toString() + "]");
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateDeactivated ();
            });
        }
    }

    /**
     * Invoked when being navigated away from. Allows one to inspect state, request
     * user input and then allow the change.
     * 
     * @param cb
     *           the callback to chain the navigation request.
     */
    default public void onNavigateFrom(INavigateCallback cb) {
        onNavigateFrom ();
        cb.proceed ();
    }

    /**
     * Invoked by the default implementation of
     * {@link #onNavigateFrom(INavigateCallback)} to simply signal a navigation
     * away.
     */
    default public void onNavigateFrom() {
        // Nothing.
    }

    /**
     * Invoked when navigated to.
     * <p>
     * Note that this is always called after the activation of the underlying
     * component (this is necessary to allow child components that make up
     * navigation to complete configuration and rendering prior to descending),
     * which means the component will be displaying when this is called. If you are
     * considering refreshing content then this may cause a "flicker" (since any
     * previous state will be initially shown prior to commencement of the refresh).
     * A better approach is to use the features of {@link IActivateAware} (assuming
     * a supporting layout, such as {@link CardFitLayout}, is used). If you choose
     * this approach then note that navigation to a currently activated component
     * (even when a parent has been navigated into) will invoke a re-activation not
     * an activation).
     * <p>
     * The default behaviour (which should continue to be respected) iterates over
     * each child component and if it implements {@link INavigationAwareChild} then
     * invoke {@link INavigationAwareChild#onNavigateActivated()} on the child.
     * 
     * @param context
     *                the navigation context.
     */
    default public void onNavigateTo(NavigationContext context) {
        if (NavigationHandler.DebugMode.ACTIVATE.set ())
            Logger.log ("{nav-activation:activate} [" + toString() + "]");
        onNavigateTo();
    }

    /**
     * See {@link #onNavigateTo(NavigationContext)} except passed no context (for
     * convenience).
     */
    default public void onNavigateTo() {
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateActivated ();
            });
        }
    }

    /**
     * An item is generally activated on the back propagation (i.e. first the
     * navigator traverses down through the path resolve items then propagates back
     * up activating the items). It is on the back propagation that
     * {@link #onNavigateTo(NavigationContext)} is called. However there are cases
     * where a path component is dynamic but on the back propagation that path is
     * reconstructed from the item's references. This means any dynamic segments are
     * resolved back to their base. To ensure that the correct dynamic segment is
     * preserved we can prepare the item on the forward direction. This is when this
     * method is called and is passed the segment that activated it. The item can
     * then respond accordingly.
     * 
     * @param segment
     *                the path segment activating the item.
     */
    default public void onNavigateToPrepare(String segment) {
        // Nothing.
    }
}
