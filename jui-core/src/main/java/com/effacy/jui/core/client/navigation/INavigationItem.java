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

import com.effacy.jui.core.client.component.layout.ILayout;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.platform.util.client.Promise;

/**
 * Represents a single navigable item, its associated state and a mechanism to
 * activate it from a display perspective.
 *
 * @author Jeremy Buckley
 */
public interface INavigationItem extends INavigationHandlerProvider, INavigationReference {

    /**
     * Determines if the navigation item is enabled.
     * 
     * @return {@code true} if it is.
     */
    public boolean isEnabled();

    /**
     * Activate the navigation item (this is purely to activate the presentation of
     * the item so plays no further role in the cascade of navigation).
     * 
     * @param context
     *                the navigation context.
     * @return a promise that is invoked when the activation has been performed
     *         (this is compliant with the promise returned by {@link ILayout}.
     */
    public Promise<ActivateOutcome> activate(NavigationContext context);

    /**
     * Invoked when the item transitions from being active to being inactive (i.e.
     * has been successfully navigated away from).
     * <p>
     * This is only called once the newly active item has been activated.
     */
    public void deactivate();

    /**
     * Determines if activation should be on forward propagation not back. This can
     * be the case where an item has not yet been fully resolved (i.e. with it's own
     * children) and resolution will only occur after activation. In this case the
     * item can conditionally return {@code true} to ensure that the forward
     * propagation can continue.
     * 
     * @return {@code true} if to activate on the forward propagation.
     */
    default public boolean activateOnForwardPropagation() {
        return false;
    }

    /**
     * Process the residual navigation (this will only be called when this item is
     * not able to provide a navigation handler yet there is still some of the path
     * in place which could encode useful directives).
     * 
     * @param ctx
     *                 the navigation context.
     * @param residual
     *                 the resudual path.
     */
    default public void navigationResidual(NavigationContext ctx, List<String> residual) {
        // Nothing.
    }

}
