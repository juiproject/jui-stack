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

public class NavigationHandlerDelegate implements INavigationHandler {

    /**
     * The handler to delegate to.
     */
    private INavigationHandler delegate;

    /**
     * The listener;
     */
    private INavigationHandlerParent listener;

    /**
     * Assigns a delegate.
     * 
     * @param delegate
     *            the delegate to assign.
     */
    public void assignDelegate(INavigationHandler delegate) {
        this.delegate = delegate;
        if (listener != null)
            delegate.assignParent (listener);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#navigate(boolean,
     *      java.util.List)
     */
    @Override
    public void navigate(NavigationContext context, List<String> path) {
        if (delegate != null)
            delegate.navigate (context, path);
        else if (listener != null)
            listener.onNavigation (context, null);

    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#renavigate(boolean)
     */
    @Override
    public void renavigate(NavigationContext context) {
        if (delegate != null)
            delegate.renavigate (context);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#activeItem()
     */
    @Override
    public INavigationItem activeItem() {
        if (delegate == null)
            return null;
        return delegate.activeItem ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#deactivate()
     */
    @Override
    public void deactivate() {
        if (delegate != null)
            delegate.deactivate ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#assignParent(com.effacy.jui.core.client.navigation.INavigationHandlerParent)
     */
    @Override
    public void assignParent(INavigationHandlerParent listener) {
        this.listener = listener;
        if (delegate != null)
            delegate.assignParent (listener);

    }

}
