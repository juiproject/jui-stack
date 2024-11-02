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
package com.effacy.jui.core.client.component;

import java.util.function.Consumer;

import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationAwareChild;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.navigation.NavigationHandler;
import com.effacy.jui.core.client.state.IStateVariable;
import com.effacy.jui.core.client.state.IStateVariable.IStateListenerHandler;
import com.effacy.jui.core.client.state.StateVariable;
import com.effacy.jui.platform.util.client.Logger;

/**
 * A type of {@link SimpleComponent} (a component without specific
 * configuration) that maintains an internal state variable that will re-render
 * the component when it changes.
 * <p>
 * The state is base on {@link StateVariable} so that it can keep track of
 * loading state, error or unexpected state and the expected variable. The
 * rendering mechanism can interrogate the state to determine what should be
 * displayed.
 * <p>
 * If the sub-class implements {@link INavigationAware} or
 * {@link INavigationAwareChild} then changes in navigation will be used to
 * block calls to {@link #onStateChanged()} when the component is not active in
 * the navigation state (the initial state will be blocked until the component
 * is navigated to or through).
 */
public class StateComponent<V extends IStateVariable<V>> extends SimpleComponent {

    /**
     * The internal state variable.
     */
    protected V state;

    /**
     * Reference to the state listener for removal when the component is disposed
     * of.
     */
    private IStateListenerHandler<V> stateListener;

    /**
     * Various behaviours when dealing with changes in navigation.
     */
    public enum NavigationBehaviour {
        NONE, BLOCK, BLOCK_AND_NOTIFY;
    }

    /**
     * See {@link #respectNavigation(NavigationBehaviour)}.
     */
    private NavigationBehaviour navigationBehaviour;

    /**
     * Construct using the passed state.
     */
    protected StateComponent(V state) {
        this (state, null);
    }

    /**
     * Construct using the passed state.
     */
    protected StateComponent(V state, NavigationBehaviour navigationBehaviour) {
        if (state == null)
            throw new IllegalArgumentException ("Missing state");
        this.state = state;
        if (state != null)
            this.stateListener = this.state.listen (v -> onStateChanged ());
        if (navigationBehaviour == null)
            this.navigationBehaviour = ((this instanceof INavigationAware) || (this instanceof INavigationAwareChild)) ? NavigationBehaviour.BLOCK_AND_NOTIFY : NavigationBehaviour.NONE;
        else
            this.navigationBehaviour = NavigationBehaviour.NONE;
        
        // If we have have a defined behaviour and we are a navigation aware (which
        // implies that we are directly in the navigation flow) then we block state
        // changes from re-renderering. For children these tend to be rendered out when
        // the parent is rendered, so we don't block these.
        if ((this.navigationBehaviour != NavigationBehaviour.NONE) && (this instanceof INavigationAware))
            this.stateListener.block ();
    }

    /**
     * Invoked when the internal state variable changes.
     * <p>
     * The default behaviour is to re-render the component (by calling
     * {@link #rerender ()}).
     */
    protected void onStateChanged() {
        rerender ();
    }

    /**
     * The state listener (can be blocked, unblocked, etc).
     * 
     * @return the listener (may be {@code null}).
     */
    public IStateListenerHandler<V> stateListener() {
        return stateListener;
    }

    /**
     * Obtains the internal state.
     * 
     * @return the state.
     */
    protected V state() {
        return state;
    }

    /**
     * Modifies the state with the given modifier.
     * 
     * @param modifier
     *                 to modify the value.
     */
    public void modify(Consumer<V> modifier) {
        state.modify (modifier);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Component#onDispose().
     */
    @Override
    protected void onDispose() {
        if (this.stateListener != null) {
            this.stateListener.remove ();
            this.stateListener = null;
        }
        super.onDispose();
    }

    /************************************************************************
     * Navigation related.
     ************************************************************************/

    /**
     * Used if implements {@link INavigationAware} or {@link INavigationAwareChild}.
     * 
     * @see INavigationAware#onNavigateDeactivated()
     */
    public void onNavigateDeactivated() {
        if (NavigationHandler.DebugMode.ACTIVATE.set ()) {
            if (this instanceof INavigationAware)
                Logger.log ("{nav-activation:deactivate} [" + toString() + "]");
            else
                Logger.log ("{nav-activation-child:deactivate} [" + toString() + "]");
        }
        if (navigationBehaviour != NavigationBehaviour.NONE)
            stateListener.block ();
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateDeactivated ();
            });
        }
    }

    /**
     * Used if implements {@link INavigationAware}.
     * 
     * @see INavigationAware#onNavigateTo(NavigationContext)
     */
    public void onNavigateTo(NavigationContext context) {
        if (NavigationHandler.DebugMode.ACTIVATE.set ())
            Logger.log ("{nav-activation:aactivate} [" + toString() + "]");
        if (navigationBehaviour == NavigationBehaviour.BLOCK_AND_NOTIFY)
            stateListener.unblock (true);
        else if (navigationBehaviour == NavigationBehaviour.BLOCK)
            stateListener.unblock (false);
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateDeactivated ();
            });
        }
        onNavigateTo();
    }

    /**
     * Used if implements {@link INavigationAware}.
     */
    public void onNavigateTo() {
        // Nothing.
    }

    /**
     * Used if implements {@link INavigationAwareChild}.
     * 
     * @see INavigationAwareChild#onNavigateActivated()
     */
    public void onNavigateActivated() {
        if (NavigationHandler.DebugMode.ACTIVATE.set ())
            Logger.log ("{nav-activation-child:activate} [" + toString() + "]");
        if (navigationBehaviour == NavigationBehaviour.BLOCK_AND_NOTIFY)
            stateListener.unblock (true);
        else if (navigationBehaviour == NavigationBehaviour.BLOCK)
            stateListener.unblock (false);
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (child -> {
                if (child instanceof INavigationAwareChild)
                    ((INavigationAwareChild) child).onNavigateActivated ();
            });
        }
    }
}
