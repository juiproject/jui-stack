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
import java.util.List;

import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;

public class NavigationHandler<I extends INavigationItem> implements INavigationHandler, IDisposable {

    /************************************************************************
     * Debugging
     ************************************************************************/

    /**
     * Various debug modes.
     */
    public enum DebugMode {
        /**
         * Log the back-propagation path construction.
         */
        BACK_PROP(1<<1),

        /**
         * Log the forward-propagation path construction.
         */
        FORWARD_PROP(1<<2),

        /**
         * Log navigation triggers.
         */
        TRIGGER(1<<3),

        /**
         * Log navigation aware invocations.
         */
        AWARE(1<<4),

        /**
         * Log activations and de-activations.
         */
        ACTIVATE(1<<5),
        
        /**
         * Log entry (initiation).
         */
        ENTRY(1<<6),

        DETAIL(1<<7);

        /**
         * Bit flag for the specific debug mode.
         */
        private int flag;

        /**
         * Construct with initial data.
         */
        private DebugMode(int flag) {
            this.flag = flag;
        }

        /**
         * Determines if the flag is set.
         * 
         * @return {@code true} if it is.
         */
        public boolean set() {
            return ((NavigationHandler.DEBUG & flag) > 0);
        }

        /**
         * Determines if any of the passed modes are set.
         * 
         * @param modes
         *              the modes to check.
         * @return {@code true} if any are set.
         */
        public static boolean any(DebugMode... modes) {
            for (DebugMode mode :modes) {
                if (mode.set ())
                    return true;
            }
            return false;
        }
    }

    /**
     * Flag to toggle debug mode.
     */
    private static int DEBUG = 0;

    /**
     * Assigns the passed modes for debugging.
     * 
     * @param modes
     *              the modes.
     */
    public static void debug(DebugMode...modes) {
        DEBUG = 0;
        for (DebugMode mode : modes) {
            if (mode == null)
                continue;
            DEBUG |= mode.flag;
        }
    }

    /************************************************************************
     * Members
     ************************************************************************/

    /**
     * The items listed by priority (for selection as default).
     */
    private List<I> items = new ArrayList<I> ();

    /**
     * The item that is currently active.
     */
    private I activeItem;

    /**
     * The parent listener.
     */
    private INavigationHandlerParent parent;
    

    /*******************************************************************************
     * Implementation of {@link INavigationHandler}
     *******************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#navigate(boolean,
     *      java.util.List)
     */
    @Override
    public void navigate(NavigationContext context, List<String> path) {
        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP, DebugMode.ENTRY))
            Logger.trace ("[NAVIGATE] START (requested)", NavigationSupport.build (path) + " [" + print (context) + "]");

        // Default navigation if there is no path.
        if ((path == null) || path.isEmpty () || (path.get (0) == null)) {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP, DebugMode.TRIGGER))
                Logger.trace ("[NAVIGATE] {requested->to_default} (use_default)", "");
            navigateToDefault (context);
            return;
        }

        // Default navigation if there is no mapped item or the mapped item is
        // not enabled.
        I item = lookup (path.get (0));
        if ((item == null) || !item.isEnabled ()) {
            // TODO: Future consideration is to allow the navigation flow to fail in this
            // instance if configured to do so (i.e through the context).
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP, DebugMode.TRIGGER))
                Logger.trace ("[NAVIGATE] {requested->to_default} (item_not_found_or_disabled)", "[" + path.get (0) + "]");
            navigateToDefault (context);
            return;
        }

        // Navigate through to the item and truncate the path.
        if (item instanceof INavigationAware)
            ((INavigationAware) item).onNavigateToPrepare (path.get (0));

        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP, DebugMode.TRIGGER))
            Logger.trace ("[NAVIGATE] {requested->activate} (item_found)", "[item={"  + print (item) + "}]");
        activate (context, item, childPath (path));
    }

    /**
     * Generate a human-readable debug string for the passed context.
     */
    protected String print(NavigationContext context) {
        if (context == null)
            return "null";
        String str = "";
        if (context.scope == null)
            str += "sc=null";
        else
            str += "sc=" + context.scope.name();
        str += ",ch=" + context.changed;
        str += ",bich=" + context.backPropagateIfNotChanged;
        if (context.source == null)
            str += ",src=null";
        else
            str += ",src=" + context.source.name();
        return str;
    }

    protected String print(I item) {
        if (item == null)
            return "null";
        return item.getReference() + "," + item;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#renavigate(boolean)
     */
    @Override
    public void renavigate(NavigationContext context) {
        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP, DebugMode.ENTRY))
            Logger.trace ("[NAVIGATE] START (renavigate)",  "[" + print (context) + "]");
        if ((activeItem == null) || !activeItem.isEnabled ()) {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                Logger.trace ("[NAVIGATE] {renavigate->to_default} [no_active_item]",  "");
            navigateToDefault (context);
        } else {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                Logger.trace ("[NAVIGATE] {renavigate->delegate} [active_item]",  "[" + activeItem + "]");
            activeItem.activate (context);
            activeItem.handler (handler -> handler.renavigate (context));
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#deactivate()
     */
    @Override
    public void deactivate() {
        activateItem (null);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandler#assignParent(com.effacy.jui.core.client.navigation.INavigationHandlerParent)
     */
    @Override
    public void assignParent(INavigationHandlerParent listener) {
        this.parent = listener;
    }

    /*******************************************************************************
     * Item management
     *******************************************************************************/

    /**
     * Registers a navigation item with a name.
     * 
     * @param name
     *             the name of the item.
     * @param item
     *             the item.
     * @return the passed item.
     */
    public <T extends I> T register(final T item) {
        if ((item == null) || (item.getReference () == null))
            return item;
        items.add (item);
        INavigationHandler handler = item.handler ();
        if (handler != null) {
            handler.assignParent (new INavigationHandlerParent () {

                @Override
                public void onNavigation(NavigationContext context, List<String> path) {
                    NavigationHandler.this.backPropagate (context, item, path);
                }
            });
        }
        onRegister (item);
        return item;
    }

    /**
     * Invoked by {@link #register(INavigationItem)} when an item is registered.
     * 
     * @param item
     *             the item that was registered.
     */
    protected void onRegister(I item) {
        // Nothing.
    }

    /**
     * The reference to the currently active item. This may be {@code null} if no
     * navigation sequence has been initiated.
     * 
     * @return the currently active item.
     */
    public I getActiveItem() {
        return activeItem;
    }

    /**
     * Looks up a navigation item by its reference.
     * 
     * @param reference
     *                  the reference.
     * @return the matching navigation item (or {@code null} if not found).
     */
    public I lookup(String reference) {
        if (reference == null)
            return null;
        for (I item : items) {
            if (item.matchReference (reference))
                return item;
        }
        return null;
    }

    /**
     * Looks up a reference to a navigation item. Note that this simply tests if the
     * item is present in the registered items then returns the reference from the
     * item.
     * 
     * @param item
     *             the item.
     * @return the matching reference (or {@code null} if not found).
     */
    public String lookup(I item) {
        if (item == null)
            return null;
        if (!items.contains (item))
            return null;
        return item.getReference ();
    }

    /**
     * Obtains an ordered list of the items.
     * 
     * @return the items.
     */
    public List<I> getItems() {
        return new ArrayList<I> (items);
    }

    /**
     * Obtains the item at the given index.
     * 
     * @param idx
     *            the index.
     * @return the associated item or {@code null}.
     */
    public I getItem(int idx) {
        if ((idx < 0) || (idx >= items.size ()))
            return null;
        return items.get (idx);
    }

    /**
     * Obtains the index of the given item.
     * 
     * @param item
     *             the item.
     * @return the associated index (<code>-1</code> if the item has not been
     *         registered or is {@code null}).
     */
    public int getIndexOf(I item) {
        if (item == null)
            return -1;
        int idx = 0;
        for (I test : items) {
            if ((item == test) || item.equals (test))
                return idx;
            idx++;
        }
        return -1;
    }

    /**
     * This clears the currently active item which will force a re-assertion on the
     * next navigation. Note that this will negate any permission checking so should
     * only be used in cases where such things as the navigation is being rebuilt.
     */
    public void clearActive() {
        this.activeItem = null;
    }

    /**
     * Clears all the registered items and the currently active item.
     */
    public void clearAll() {
        clearActive ();
        items.clear ();
    }

    /*******************************************************************************
     * Supporting methods
     *******************************************************************************/

    /**
     * Navigate to the default item which is just the first enabled item in the
     * items-by-priority list.
     * 
     * @return {@code true} if there was an item found to navigate to.
     */
    protected void navigateToDefault(NavigationContext context) {
        for (I item : items) {
            if (item.isEnabled ()) {
                if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                    Logger.trace ("[NAVIGATE] {to_default->activate} (found)", "[item={" + print (item) + "}]");
                activate (context, item, null);
                return;
            }
        }
        // No default so just fall through as complete and commence back
        // propagation. The back propagation ensures that we will at least
        // activate any of the parent items where those items activate on the
        // back path (rather than die here).
        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
            Logger.trace ("[NAVIGATE] {to_default->back} (no_items)", "");
        backPropagate (context, null, null);
    }

    /**
     * Navigates to the given item with the child path.
     * 
     * @param refresh
     *                see {@link #navigate(boolean, List)}.
     * @param item
     *                the item to navigate to.
     * @param path
     *                the child path (below the item).
     */
    protected void activate(final NavigationContext context, final I item, final List<String> path) {
        // Check if the item being navigated to is different from the current
        // item (if not then we are not navigating away from it).
        I activeItem = getActiveItem ();
        boolean same = (activeItem != null) && (item != null) && ((item == activeItem) || item.equals (activeItem));
        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP)) {
            if (same)
                Logger.trace ("[NAVIGATE] {activate}", "[same=" + ((item == null) ? "null" : item.getReference ()) + "]");
            else
                Logger.trace ("[NAVIGATE] {activate}", "[to=" + ((item == null) ? "null" : item.getReference ()) + ",from=" + ((activeItem == null) ? "null" : activeItem.getReference ()) + "]");
        }
        if (!context.changed && !same) {
            if (DebugMode.AWARE.set ())
                Logger.trace ("[NAVIGATE] {activate::aware}", "[start]");
            // Here we obtain the terminal item from the active item and invoke it's
            // onNavigateFrom if it is aware.
            INavigationItem terminalItem = activeItem;
            while ((terminalItem != null) && (terminalItem.handler () != null))
                terminalItem = terminalItem.handler ().activeItem ();
            if (terminalItem instanceof INavigationAware) {
                ((INavigationAware) terminalItem).onNavigateFrom (new INavigateCallback () {

                    @Override
                    public void proceed() {
                        if (DebugMode.AWARE.set ())
                            Logger.trace ("[NAVIGATE] {activate::aware}", "[proceed]");

                        // Carry on as normal.
                        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                            Logger.trace ("[NAVIGATE] {activate->(callback)propagate}", "");
                        navigatePropagate (context, item, path, false);
                    }

                    @Override
                    public void cancel() {
                        // Nothing needs to be done here as we just cancel the
                        // navigation flow and no back-flow will occur (this no
                        // navigation).
                        if (DebugMode.AWARE.set ())
                            Logger.trace ("[NAVIGATE] {activate::aware}", "[cancel]");
                    }

                });
            } else {
                if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                    Logger.trace ("[NAVIGATE] {activate->propagate}", "");
                navigatePropagate (context, item, path, same);
            }
        } else {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                Logger.trace ("[NAVIGATE] {activate->propagate}", "");
            navigatePropagate (context, item, path, same);
        }
    }

    /**
     * Internal. Called by
     * {@link #navigate(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext, String...)}
     * to implement a navigation post determination of any callback outcome.
     */
    protected void navigatePropagate(NavigationContext context, I item, List<String> childPath, boolean same) {
        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
            Logger.trace ("[NAVIGATE] {propagate}", "[item=" + ((item == null) ? "[null]" : item.getReference ()) + ",path=" + NavigationSupport.build (childPath) + ",same=" + same + ",item_activate_on_forward=" + ((item == null) ? "null" : item.activateOnForwardPropagation ()) + "]");

        // Update the context changed flag (indicating that we have now deviated from
        // the main path and there is no need to invoke onNavigateFrom for any active
        // item since those active items will be artefacts rather than truely active).
        context.changed = context.changed || !same;

        // Check that the item has been rendered. If not activate it forceably (but only
        // if activating on forward propagation).
        if ((item != null) && item.activateOnForwardPropagation ()) {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
                Logger.trace ("[NAVIGATE] {propagate->activate}", "[item={" + print (item) + "}]");
            // Using a promise allows for deferred activation.
            item.activate (context).onFulfillment (v -> {
                activateItem (item);
                forwardPropagate (context, item, childPath);
            });
            return;
        }

        if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP))
            Logger.trace ("[NAVIGATE] {propagate->forward}", "[child= " + NavigationSupport.build (childPath) + "]");
        forwardPropagate (context, item, childPath);
    }

    /**
     * Invoked by
     * {@link #navigatePropagate(INavigationHandler.NavigationContext, INavigationItem, List, boolean)}
     * to commence the forward propagation.
     */
    protected void forwardPropagate(NavigationContext context, I item, List<String> childPath) {
        INavigationHandler handler = item.handler ();
        if (handler != null) {
            // Here we keep propagating forward.
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP)) {
                if (childPath == null)
                    Logger.trace ("[NAVIGATE] {forward->descend}", "/ [child_path_size=0,item={" + item + "}]");
                else
                    Logger.trace ("[NAVIGATE] {forward->descend}", NavigationSupport.build (childPath) + " [child_path_size=" + childPath.size() + ",item={" + print (item) + "}]");
            }
            handler.navigate (context, childPath);
        } else {
            // Here we have reached the end of the navigation line and we start
            // propagating back. There may be some residual path components so
            // we look to see if the item is interested in handling them (this
            // is similar to navigation but they could be directives that can be
            // safely disposed of).
            if ((childPath != null) && !childPath.isEmpty ())
                item.navigationResidual (context, childPath);
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.FORWARD_PROP)) {
                String prefix = "";
                if (!context.isChanged () && !context.isBackPropagateIfNotChanged ())
                    prefix = "[no_back] ";
                if (childPath == null)
                    Logger.trace ("[NAVIGATE] {forward->back} {reached_end}", prefix + "/" + NavigationSupport.build (childPath) + " [null," + context.isChanged() + "," + context.isBackPropagateIfNotChanged() + "][" + item.toString() + "]");
                else
                    Logger.trace ("[NAVIGATE] {forward->back} {reached_end}", prefix + "/" + NavigationSupport.build (childPath) + " [" + childPath.size() + "," + context.isChanged() + "," + context.isBackPropagateIfNotChanged() + "][" + item.toString() + "]");
            }
            if (context.isChanged () || context.isBackPropagateIfNotChanged ())
                backPropagate (context, item, null);
        }
    }

    /**
     * Invoked when the handler is back-propagating a navigation event.
     * <p>
     * This will activate the specified item the propagate the navigation event to
     * the registered navigation listener (generally a parent handler).
     * 
     * @param context
     *                  the context the navigation is being performed within.
     * @param item
     *                  the item that is being activated.
     * @param childpath
     *                  the path of children below that item being activated that
     *                  are currently activated (will be empty or {@code null} of
     *                  the item being activated is a leaf item).
     */
    protected void backPropagate(NavigationContext context, I item, List<String> childpath) {
        // If there is no item we just propagate up.
        if (item == null) {
            if (parent != null) {
                if (childpath != null)
                    childpath.clear ();
                if (DebugMode.any (DebugMode.DETAIL, DebugMode.BACK_PROP)) {
                    if (childpath == null)
                        Logger.trace ("[NAVIGATE] {back->call_parent} (no_item)", "");
                    else
                        Logger.trace ("[NAVIGATE] {back->call_parent} (no_item)", NavigationSupport.build (childpath) + " [" + childpath.size() + "]");
                }
                parent.onNavigation (context, childpath);
            }
            return;
        }

        // Activate the item if it not already activated.
        if (item != activeItem) {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.ACTIVATE))
                Logger.trace ("[NAVIGATE] {back::activate}", "[" + item + "]");
            // Note that we do not need to worry about the promise as we are
            // back-propagating.
            item.activate (context);
            if (item instanceof INavigationAware)
                ((INavigationAware) item).onNavigateTo (context);
            activateItem (item);
        } else if (context.isRefresh () && (item instanceof INavigationAware)) {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.ACTIVATE))
                Logger.trace ("[NAVIGATE] {back::refresh}", "[" + item + "]");
            ((INavigationAware) item).onNavigateTo (context);
        } else if (DebugMode.any (DebugMode.DETAIL, DebugMode.ACTIVATE))
            Logger.trace ("[NAVIGATE] {back::already_activate}", "");

        // Push up the chain.
        if (parent == null) {
            if (DebugMode.any (DebugMode.DETAIL, DebugMode.BACK_PROP))
                Logger.trace ("[NAVIGATE] {back->done} (no_parent)", "[" + item + "]");
            return;
        }
        if (childpath == null)
            childpath = new ArrayList<> ();
        String reference = item.getReference ();
        if (!StringSupport.empty (reference))
            childpath.add (0, item.getReference ());
        else
            childpath.clear ();

        if (DebugMode.any (DebugMode.DETAIL, DebugMode.BACK_PROP))
            Logger.trace ("[NAVIGATE] {back->parent}", NavigationSupport.build (childpath) + " [" + childpath.size() + "][" + item.toString() + "]");
        parent.onNavigation (context, childpath);
    }

    /**
     * Makes the passed item the active item.
     * <p>
     * This will deactivate the currently active item and replace it with the passed
     * item.
     * 
     * @param item
     *             the item to make active (can be {@code null} to deactive the
     *             current item).
     */
    protected void activateItem(I item) {
        if (activeItem == item)
            return;
        if (activeItem != null) {
            activeItem.deactivate ();
            if (activeItem instanceof INavigationAware) {
                ((INavigationAware) activeItem).onNavigateDeactivated ();
            }
            INavigationHandler handler = activeItem.handler ();
            if (handler != null)
                handler.deactivate ();
        }
        activeItem = item;
    }

    
    /**
     * {@inheritDoc}
     * 
     * See {@link INavigationHandler#activeItem()}.
     */
    @Override
    public INavigationItem activeItem() {
        return activeItem;
    }

    /**
     * Removes the first item in the path and returns the resultant. Note that if
     * there is no path, the path is empty or the resultant path is empty then
     * {@code null} will be returned.
     * 
     * @param path
     *             the path to extract the child path from.
     * @return the child path.
     */
    protected List<String> childPath(List<String> path) {
        if (path == null)
            return null;
        if (path.size () <= 1)
            return null;
        path = new ArrayList<String> (path);
        path.remove (0);
        return path;
    }

    /**
     * See {@link IDisposable#dispose()}.
     */
    @Override
    public void dispose() {
        this.parent = null;
        this.items.clear ();
    }

    
}
