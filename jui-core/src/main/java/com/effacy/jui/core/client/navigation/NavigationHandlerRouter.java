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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.util.TriConsumer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.Promise;

/**
 * Support class for implementing a {@link INavigationHandler} inside of a class
 * that activates various child components but needs delicate control over the
 * path (i.e. the path may not directly reference specific children or the
 * children require more complicated activation criteria).
 *
 * @author Jeremy Buckley
 */
public class NavigationHandlerRouter implements INavigationHandler {

    /**
     * This can be useful for creating child specifiers that are registered with the
     * router and where the specifier contains a component that is ultimately
     * rendered.
     * <p>
     * This allows for the component to implement {@link INavigationAware},
     * {@link INavigationHandler} or {@link INavigationHandlerProvider} which is
     * then appropriately utilised.
     */
    public static abstract class RegistrationItem implements INavigationAware, INavigationHandlerProvider, INavigationResidualAware {

        protected IComponent component;

        protected RegistrationItem(IComponent component) {
            this.component = component;
        }

        @Override
        public void onNavigateDeactivated() {
            if (component() instanceof INavigationAware)
                ((INavigationAware) component()).onNavigateDeactivated();
        }

        @Override
        public void onNavigateFrom(INavigateCallback cb) {
            if (component() instanceof INavigationAware)
                ((INavigationAware) component()).onNavigateFrom(cb);
            else
                cb.proceed ();
        }

        @Override
        public void onNavigateTo(NavigationContext context) {
            if (component() instanceof INavigationAware)
                ((INavigationAware) component()).onNavigateTo(context);
        }

        @Override
        public void onNavigateToPrepare(String segment) {
            if (component() instanceof INavigationAware)
                ((INavigationAware) component()).onNavigateToPrepare(segment);
        }

        @Override
        public void navigationResidual(NavigationContext ctx, List<String> residual) {
            if (component() instanceof INavigationResidualAware)
                ((INavigationResidualAware) component()).navigationResidual (ctx, residual);
        }

        @Override
        public INavigationHandler handler() {
            if (component() instanceof INavigationHandler)
                return (INavigationHandler) component();
            if (component() instanceof INavigationHandlerProvider)
                return ((INavigationHandlerProvider) component()).handler();
            return null;
        }

        /**
         * Returns the wrapped component.
         * 
         * @return the component.
         */
        public IComponent component() {
            return component;
        }
    }


    /**
     * Listeners (see {@link #assignParent(INavigationHandlerParent)}).
     */
    private List<INavigationHandlerParent> listeners = new ArrayList<> ();

    /**
     * The various handlers in place mapped to by their associated object (often the same but is what is activate).
     */
    private Map<Object,INavigationHandler> handlers = new HashMap<> ();

    /**
     * The currently active child.
     */
    private Object activeChild;

    /**
     * Internal listener to add to registered handlers.
     */
    private INavigationHandlerParent handlerListener = new INavigationHandlerParent () {

        @Override
        public void onNavigation(NavigationContext context, List<String> path) {
            NavigationHandlerRouter.this.onNavigationBackward (context, path, outcome -> {
                if (outcome != null) {
                    listeners.forEach (l -> {
                        try {
                            l.onNavigation (context, outcome);
                        } catch (Throwable e) {
                            onUncaughtException (e);
                        }
                    });
                }
            });
        }
    };

    /**
     * Invoked when one of the child handler issues an
     * {@link INavigationHandlerParent#onNavigation(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext, List)}
     * event.
     * <p>
     * The default behaviour is to propagate through. However, one can modify the
     * path as needed where a more complex path arrangement is in place (typically
     * when a router needs to draw on multiple path components for a state
     * determination).
     * 
     * @param context
     *                   the associated navigation context.
     * @param path
     *                   the path being feed back up.
     * @param propagator
     *                   to invoked with any revised path (propagated to this
     *                   handlers listeners).
     */
    protected void onNavigationBackward(NavigationContext context, List<String> path, Consumer<List<String>> propagator) {
        propagator.accept (NavigationSupport.copy (path));
    }

    /**
     * Invoked to process forward navigation.
     * <p>
     * When called the path prefix should be used to determine the state of the
     * component employing the router. This prefix should be removed and the balance
     * propagated foward via a call to the propagator with a suitably modified
     * context (i.e. marked as changed if the state of the component has changed
     * based on the passed path).
     * <p>
     * Passed to the propagator should be the navigation context (marked as changed
     * as necessary and as described above), the path with the prefix used to
     * determine the component state removed and the child object that represents
     * what child has been activated. This child will be passed to
     * {@link #onChildActivated(Object)} which us responsible for actually changing
     * the state of the component to bring that child into focus (often this child
     * is just a component itself and is simply shown).
     * <p>
     * On the last point the child needs to be registed (by calling one of the
     * {@link #register(Object)} methods) which will associated the child with an
     * optional handler (if the child has children) that will be propagated to.
     * 
     * @param context
     *                   the context describing the nature of the navigation event.
     * @param path
     *                   the path being asserted (descendent from the parent so the
     *                   first element should map to a registered child).
     * @param propagator
     *                   the propagator to continue forward-propagation.
     */
    protected void onNavigationForward(NavigationContext context, List<String> path, TriConsumer<NavigationContext, List<String>, Object> propagator) {
        propagator.accept (context, path, null);
    }

    /**
     * To handle an uncaught exception (for registering, logging, etc).
     * <p>
     * The default is to delegate to any registered handler (see
     * {@link Logger#reportUncaughtException(Throwable)}.
     * 
     * @param e
     *          the exception.
     */
    protected void onUncaughtException(Throwable e) {
        Logger.reportUncaughtException (e);
    }

    /**
     * Registers a child. If the child is a
     * {@link INavigationHandler} or {@link INavigationHandlerProvider} (from which
     * a handler can be extracted) then the handler will be retained and
     * appropriately wired in.
     * <p>
     * This will wire up any listeners.
     * 
     * @param <H>
     * @param handler
     *                the child to register.
     * @return the passed handler.
     */
    public <H> H register(H child) {
        return register (child, null);
    }

    /**
     * Registers a child with its associated handler (if the child has children).
     * <p>
     * The handler will be wired unto the router.
     * 
     * @param <H>
     * @param child
     *                the child to register (this is what is activated).
     * @param handler
     *                (optional) the handler to register against the child (which is
     *                responsible for the childs children and will be propagated
     *                to).
     * @return the passed child.
     */
    public <H> H register(H child, INavigationHandler handler) {
        if (child == null)
            return child;
        if (handler == null) {
            if (child instanceof INavigationHandler)
                handler = (INavigationHandler) child;
            else if (child instanceof INavigationHandlerProvider)
                handler = ((INavigationHandlerProvider) child).handler ();
        }
        if (handler != null) {
            handlers.put (child, handler);
            handler.assignParent (handlerListener);
        }
        return child;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#navigate(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext,
     *      java.util.List)
     */
    @Override
    public void navigate(NavigationContext context, List<String> path) {
        if (context == null)
            context = new NavigationContext();
        onNavigationForward (context, path, (c, p, h) -> {
            // First we resolve any handler to delegate to. If there is none then we
            // commence back-propagation.
            activateChild (c, h).onFulfillment (v -> {
                if ((v != ActivateOutcome.NOT_PRESENT) && (activeChild != null)) {
                    INavigationHandler handler = handlers.get (activeChild);
                    if (handler != null) {
                        handler.navigate (c, p);
                    } else {
                        // This indicates the the node is terminal. We check awareness and invoke. Note
                        // that we do this even if there is no residual (since the residual could be
                        // embodied in the navigation context).
                        // if (activeChild instanceof INavigationAware)
                        //     ((INavigationAware) activeChild).onNavigateTo (context);
                        //if ((activeChild instanceof INavigationResidualAware) && (p != null) && !p.isEmpty ())
                        if ((activeChild instanceof INavigationResidualAware))
                            ((INavigationResidualAware) activeChild).navigationResidual (c, (p == null) ? new ArrayList<>() : p);
                        handlerListener.onNavigation (c, path);
                    }
                } else if (c.isChanged () || c.isBackPropagateIfNotChanged ())
                    handlerListener.onNavigation (c, path);
            });
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#deactivate()
     */
    @Override
    public void deactivate() {
        if (activeChild != null) {
            if (activeChild instanceof INavigationAware)
                ((INavigationAware) activeChild).onNavigateDeactivated ();
            INavigationHandler handler = handlers.get (activeChild);
            if (handler != null)
                handler.deactivate ();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#activeItem()
     */
    @Override
    public INavigationItem activeItem() {
        if (activeChild instanceof INavigationItem)
            return ((INavigationItem) activeChild);
        return null;
    }

    /**
     * Obtains the active child (which may or may not be a {@link INavigationItem},
     * if it is it is also returned by {@link #activeItem()}).
     * 
     * @return the active item.
     */
    public Object activeChild() {
        return activeChild;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#renavigate(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext)
     */
    @Override
    public void renavigate(NavigationContext context) {
        if (activeChild != null) {
            INavigationHandler handler = handlers.get (activeChild);
            if (handler != null)
                handler.renavigate (context);
        }
    }

    /**
     * Activate the given child (represented by the passed child data).
     * <p>
     * If the child is {@code null} then {@link ActivateOutcome#NOT_PRESENT} is
     * returned. If there is no change in child then
     * {@link ActivateOutcome#ALREADY_ACTIVATED} is returned. Otherwise the context
     * is marked as changed, the activate child is swapped out and the child
     * activated with a call to {@link #onChildActivated(Object)}.
     * 
     * @param context
     *                 the navigation context being operated under.
     * @param child
     *                 the child that is to be activated.
     */
    public Promise<ActivateOutcome> activateChild(NavigationContext context, Object child) {
        if (child == null)
            return Promise.create (ActivateOutcome.NOT_PRESENT);
        if (this.activeChild == child)
            return Promise.create (ActivateOutcome.ALREADY_ACTIVATED);
        context.changed ();
        if (this.activeChild instanceof INavigationAware)
            ((INavigationAware) this.activeChild).onNavigateDeactivated ();
        this.activeChild = child;
        if (child instanceof INavigationAware)
            ((INavigationAware) child).onNavigateTo (context);
        return onChildActivated (context, child);
    }

    /**
     * Called by {@link #activateChild(NavigationContext, Object)} to signal a
     * change in child.
     * 
     * @param context
     *                the navigation context (for reference).
     * @param child
     *                the child being activated.
     */
    protected Promise<ActivateOutcome> onChildActivated(NavigationContext context, Object child) {
        return onChildActivated (child);
    }

    /**
     * Called by {@link #activateChild(NavigationContext, Object)} to signal a
     * change in child (actially via
     * {@link #onChildActivated(NavigationContext, Object)}).
     * 
     * @param child
     *              the child being activated.
     */
    protected Promise<ActivateOutcome> onChildActivated(Object child) {
        return Promise.create (ActivateOutcome.NOT_PRESENT);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#assignParent(com.effacy.jui.core.client.navigation.INavigationHandlerParent)
     */
    @Override
    public void assignParent(INavigationHandlerParent listener) {
        if (listener == null)
            return;
        if (!this.listeners.contains (listener))
            this.listeners.add (listener);
    }

}
