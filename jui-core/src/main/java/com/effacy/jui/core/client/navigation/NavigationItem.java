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

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;

/**
 * Support class for implementing {@link INavigationItem}'s. Also provides
 * support for an underlying component though does not implement any mechanism
 * to activate the component. It will test the component for handlers and
 * navigation awareness.
 * 
 * @author Jeremy Buckley
 */
public abstract class NavigationItem implements INavigationItem, INavigationAware {

    /**
     * See constructor.
     */
    private String reference;

    /**
     * See constructor.
     */
    private IComponent component;

    /**
     * Construct with a reference.
     * 
     * @param reference
     *            the reference.
     */
    protected NavigationItem(String reference) {
        this.reference = reference;
    }


    /**
     * Construct with a reference and a component.
     * 
     * @param reference
     *            the reference.
     * @param component
     *            the component.
     */
    protected NavigationItem(String reference, IComponent component) {
        this.reference = reference;
        this.component = component;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationItem#getReference()
     */
    @Override
    public String getReference() {
        return reference;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }


    /**
     * The component associated with the item.
     * 
     * @return the component.
     */
    public IComponent getComponent() {
        return component;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationAware#onNavigateFrom(com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback)
     */
    @Override
    public void onNavigateFrom(INavigateCallback cb) {
        if ((component != null) && (component instanceof INavigationAware))
            ((INavigationAware) component).onNavigateFrom (cb);
        else
            INavigationAware.super.onNavigateFrom (cb);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationAware#onNavigateTo(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext)
     */
    @Override
    public void onNavigateTo(NavigationContext context) {
        if ((component != null) && (component instanceof INavigationAware))
            ((INavigationAware) component).onNavigateTo (context);
        else
            INavigationAware.super.onNavigateTo (context);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationAware#onNavigateToPrepare(java.lang.String)
     */
    @Override
    public void onNavigateToPrepare(String pathComponent) {
        if ((component != null) && (component instanceof INavigationAware))
            ((INavigationAware) component).onNavigateToPrepare (pathComponent);
        else
            INavigationAware.super.onNavigateToPrepare (pathComponent);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandlerProvider#handler()
     */
    @Override
    public INavigationHandler handler() {
        if ((component != null) && (component instanceof INavigationHandlerProvider))
            return ((INavigationHandlerProvider) component).handler ();
        return INavigationItem.super.handler ();
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationItem#activateOnForwardPropagation()
     */
    @Override
    public boolean activateOnForwardPropagation() {
        if (component != null)
            return !component.isRendered ();
        return INavigationItem.super.activateOnForwardPropagation ();
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationItem#navigationResidual(java.util.List)
     */
    @Override
    public void navigationResidual(NavigationContext ctx, List<String> residual) {
        if ((component != null) && (component instanceof INavigationResidualAware))
            ((INavigationResidualAware) component).navigationResidual (ctx, residual);
    }

    @Override
    public String toString() {
        return this.reference + "," + component;
    }

}
