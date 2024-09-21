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

/**
 * A navigation handler is something that is able to absorb navigation requests
 * and respond to those requests in accordance with the navigation handler
 * model. That means that whenever the navigation handler actions a navigation
 * request (potentially outside of the standard navigation methods provided on
 * this interface) it will generate a navigation event to any associated
 * listener. If it accepts other navigation handlers as navigable content then
 * it must select the relevant handler from the navigate list and pass an
 * abridged list through to that handler. It must also add a listener to that
 * handler and respond to navigation events but activating the handler as
 * appropriate.
 *
 * @author Jeremy Buckley
 */
public interface INavigationHandler {

    /**
     * A context to perform navigation within. This can be used to change certain
     * behaviours or invoke certain outcomes as a result of the navigation attempt.
     */
    public static class NavigationContext {

        /**
         * Possible sources of a navigation event.
         */
        public enum Source {
            /**
             * During initialisation of the owning component (i.e. on render). This will
             * typically imply the establishment of an initial navigation state.
             */
            INITIAL,

            /**
             * Externally imposed.
             */
            EXTERNAL,

            /**
             * The default when navigation events occur (i.e. when a tab is clicked).
             */
            INTERNAL;
        }

        /**
         * Enumerates possible scopes that the navigation is operating under.
         */
        public enum Scope {

            /**
             * No specific consideration.
             */
            NONE,

            /**
             * This is primed in some sense, for example, the user has selected this
             * navigation as a default location.
             */
            PRIMED;
        }

        /**
         * See {@link #getSource()}.
         */
        protected Source source;

        /**
         * See {@link #isRefresh()}.
         */
        private boolean refresh;

        /**
         * See {@link #isChanged()}.
         */
        protected boolean changed = false;

        /**
         * See {@link #backPropagateIfNotChanged(boolean)}.
         */
        protected boolean backPropagateIfNotChanged = false;

        /**
         * See {@link scope(Scope)}.
         */
        protected Scope scope = Scope.NONE;

        /**
         * The current depth of navigation (for debugging).
         */
        protected int depth = 0;

        /**
         * Meta data attached to the context.
         */
        protected Map<String,Object> metadata;

        /**
         * Construct with initial configuration. The source will be
         * {@link Source#INTERNAL}.
         */
        public NavigationContext() {
            this (null, false);
        }

        /**
         * Construct with default context settings.
         * 
         * @param source
         *               the source of the navigation.
         */
        public NavigationContext(Source source) {
            this (source, false);
        }

        /**
         * Construct with initial configuration. The source will be
         * {@link Source#INTERNAL}.
         * 
         * @param refresh
         *                {@code true} if a refresh of the items in the path should be
         *                notified (see {@link #isRefresh()} for more details).
         */
        public NavigationContext(boolean refresh) {
            this (null, refresh);
        }

        /**
         * Construct with initial configuration.
         * 
         * @param source
         *                the source of the navigation.
         * @param refresh
         *                {@code true} if a refresh of the items in the path should be
         *                notified (see {@link #isRefresh()} for more details).
         */
        public NavigationContext(Source source, boolean refresh) {
            this.source = (source == null) ? Source.INTERNAL : source;
            this.refresh = refresh;
        }

        /**
         * Assigns metadata to the context.
         * 
         * @param name
         *              the name of the property.
         * @param value
         *              the value of the property.
         * @return this context.
         */
        public NavigationContext metadata(String name, Object value) {
            if (metadata == null)
                metadata = new HashMap<>();
            metadata.put(name, value);
            return this;
        }

        /**
         * Obtains the named metadata attribute.
         * 
         * @param name
         *             the name of the attribute.
         * @return the value of the attribute.
         */
        public Object getMetadata(String name) {
            return (metadata == null) ? null : metadata.get(name);
        }

        /**
         * Obtains the named metadata attribute.
         * 
         * @param name
         *                     the name of the attribute.
         * @param defaultValue
         *                     the default value to use if the attribute is not set.
         * @return the value of the attribute.
         */
        @SuppressWarnings("unchecked")
        public <T> T getMetadata(String name, T defaultValue) {
            if (metadata == null)
                return defaultValue;
            T value = (T) metadata.get(name);
            return (value == null) ? defaultValue : value;
        }

        /**
         * Determines if the metadata attribute is present.
         * 
         * @param name
         *             the name of the attribute.
         * @return {@code true} if the attribute is set.
         */
        public boolean hasMetadata(String name) {
            return (metadata != null) && (metadata.get(name) != null); 
        }

        /**
         * The source of the navigation event. See {@link Source}.
         * <p>
         * This is not used by the navigation mechanism itself but rather to inform
         * individual components on how the navigation came about and for them to act
         * accordingly (in general it can be safely ignored but sometimes it is
         * important to know where a navigation event has originated).
         * 
         * @return the source.
         */
        public Source getSource() {
            return source;
        }

        /**
         * Assigns a scope (this is not used directly but can be used to inform external
         * behaviours, such as error messages).
         * 
         * @param scope
         *              {@code true} the scope.
         * @return this context instance.
         */
        public NavigationContext scope(Scope scope) {
            if (scope != null)
                this.scope = scope;
            return this;
        }

        /**
         * Getter for {@link #scope(Scope)}.
         */
        public Scope getScope() {
            return scope;
        }

        /**
         * If this is set then on back-propagation all items that implement
         * {@link INavigationAware} will have their
         * {@link INavigationAware#onNavigateTo(NavigationContext)} method called even
         * if they were previously active.
         * <p>
         * This is subordinate to {@link #isBackPropagateIfNotChanged()} (meaning that
         * if there was no change in the navigation back-propagation normally does not
         * occur and no refreshing is performed).
         * 
         * @return {@code true} if to refresh.
         */
        public boolean isRefresh() {
            return refresh;
        }

        /**
         * Marks the changed status which is used to inform the navigation handler than
         * navigation has changed somewhere upstream. This will invoke a
         * back-propagation in all cases.
         * 
         * @return this instance.
         */
        public NavigationContext changed() {
            this.changed = true;
            return this;
        }

        /**
         * Indicates that the navigation has now changed from what it was. This variable
         * is updated as the navigation is traversed so as soon as a new branch has been
         * detected it will be set to {@code true}.
         * <p>
         * Internally this is used to block or allow tests to
         * {@link INavigationAware#onNavigateFrom(INavigationAware.INavigateCallback)}
         * In this instance it is possible to navigate to a branch that is new but
         * within the branch a default navigation is being imposed. In this instance it
         * maybe the case that a navigation away will occur as one navigates to the
         * default path. However that should not trigger an awareness check as it wasn't
         * previously on the active branch (the test would presumably have been done
         * earlier). So such tests only need to be done at the point of branching away
         * and after that should not be done. This is able to effect that condition.
         * <p>
         * When the context is created it is set to {@code false} so it is up to the
         * navigation handler to change.
         * 
         * @return {@code true} if the current navigation is on a new branch differing
         *         from the current navigation path.
         */
        public boolean isChanged() {
            return changed;
        }

        /**
         * Normally a back propagation occurs only if the activation state (path) has
         * changed. Setting this to {@code true} will back propagate even in this case.
         * 
         * @param backPropagateIfNotChanged
         *                                  {@code true} to back propagate even if the
         *                                  path has not changed.
         * @return this context instance.
         */
        public NavigationContext backPropagateIfNotChanged(boolean backPropagateIfNotChanged) {
            this.backPropagateIfNotChanged = backPropagateIfNotChanged;
            return this;
        }

        /**
         * Getter for {@link #backPropagateIfNotChanged(boolean)}.
         */
        public boolean isBackPropagateIfNotChanged() {
            return backPropagateIfNotChanged;
        }
    }

    /**
     * Requests a change in navigation path to the given path sequence.
     * <p>
     * The first element in the sequence is the item handled by this handler. If the
     * item also provides a {@link INavigationHandler} then then navigation event
     * will be propagated through to the handler (the path will have its first
     * element removed if not empty, if empty the default path is assumed).
     * 
     * @param content
     *                the navigation context.
     * @param path
     *                the path sequence of navigation items.
     * @return {@code true} if the navigation was successful.
     */
    public void navigate(NavigationContext context, List<String> path);

    /**
     * See {@link #navigate(List)}.
     * 
     * @param content
     *                the navigation context.
     * @param path
     *                the path sequence to navigate to.
     */
    default public void navigate(NavigationContext context, String... path) {
        List<String> items = new ArrayList<String> ();
        for (String item : path)
            items.add (item);
        navigate (context, items);
    }

    /**
     * Applies the passed context to the current navigation path.
     * <p>
     * If a current navigation item is no longer enabled then, from that point on,
     * the default navigation is applied.
     * 
     * @param refresh
     *                same as for navigate.
     */
    public void renavigate(NavigationContext context);

    /**
     * Used to notify the handler that it's item has been deactivated
     */
    public void deactivate();

    /**
     * Obtains the currently active item.
     * 
     * @return the item.
     */
    public INavigationItem activeItem();

    /**
     * Assigns the parent to the handler to receive navigation events from the
     * handler when it changes its navigation.
     * 
     * @param parent
     *               the parent handler (for back-propagation).
     */
    public void assignParent(INavigationHandlerParent parent);

}
