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
package com.effacy.jui.ui.client.tabs;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;

/**
 * Abstraction of a set of tabs for navigation purposes.
 *
 * @author Jeremy Buckley
 */
public interface ITabSet extends INavigationHandler, IComponent {

    /**
     * Configuration for a tab group.
     */
    public interface ITabGroupConfig {

        /**
         * The icon to display along with the display label.
         * 
         * @param icon
         *             the icon.
         * @return this tab configuration.
         */
        public ITabGroupConfig icon(String icon);

        /**
         * Convenience to invoke {@link #collapsable(boolean)} passing {@code true}.
         */
        default public ITabGroupConfig collapsable() {
            return collapsable (true);
        }

        /**
         * The icon to display along with the display label.
         * 
         * @param collapsable
         *                    {@code true} if the group should be collapsable (default
         *                    is not to).
         * @return this tab configuration.
         */
        public ITabGroupConfig collapsable(boolean collapsable);

        /**
         * If this should be expanded above.
         * 
         * @return this tab configuration.
         */
        public ITabGroupConfig expand();
    }

    /**
     * Configuration for a tab.
     */
    public interface ITabConfig {

        /**
         * The icon to display along with the display label.
         * 
         * @param icon
         *             the icon.
         * @return this tab configuration.
         */
        public ITabConfig icon(String icon);

        /**
         * Additional indicator to display.
         * 
         * @param indicator
         *              the indicator text.
         * @return this tab configuration.
         */
        public ITabConfig indicator(String indicator);

        /**
         * This can be used to block navigate away requests (i.e. if there are unsaved
         * changes).
         * 
         * @param handler
         *                the handler.
         * @return this tab configuration.
         */
        public ITabConfig navigationHandler(Consumer<INavigateCallback> handler);

        /**
         * Register a navigation handler for the tab. This is used when the element
         * associated to the tab will have its own navigation and needs to receive
         * navigation requests.
         * 
         * @param handler
         *                the handler.
         * @return this tab configuration.
         */
        public ITabConfig handler(final INavigationHandler handler);

        /**
         * See {@link #handler(INavigationHandler)} but registers a provider.
         * 
         * @param handlerProvider
         *                        the handler provider.
         * @return this tab configuration.
         */
        public ITabConfig handlerProvider(INavigationHandlerProvider handlerProvider);
    }

    /**
     * Configuration for a tab set.
     */
    public interface ITabSetConfiguration {

        public interface ITabActivator {

            public void activate(NavigationContext context, Consumer<ActivateOutcome> outcome);

            public void deactivate();

            public static ITabActivator create (BiConsumer<NavigationContext, Consumer<ActivateOutcome>> activator, Invoker deactivator) {
                return new ITabActivator() {

                    @Override
                    public void activate(NavigationContext context, Consumer<ActivateOutcome> outcome) {
                        if (activator != null)
                            activator.accept (context, outcome);
                    }

                    @Override
                    public void deactivate() {
                        if (deactivator != null)
                            deactivator.invoke ();
                    }

                };
            }
        }

        /**
         * Adds a tab group. All subsequent tabs are added to this group.
         * 
         * @param label
         *              the label
         * @return this configuration instance.
         */
        public ITabGroupConfig group(String label);

        /**
         * Adds a tab to the tab set (always to the last created tab group).
         * 
         * @param reference
         *                  the reference to the tab.
         * @param label
         *                  display label for the tab.
         * @param activator
         *                  activator for the tab.
         * @return the tab (for further configuration).
         */
        public ITabConfig tab(String reference, String label, ITabActivator activator);

        /**
         * Adds a (pseudo-)tab to the tab set (always to the last created tab group).
         * 
         * @param label
         *                  display label for the tab.
         * @param activator
         *                  activator for the tab.
         * @return the tab (for further configuration).
         */
        public ITabConfig tab(String label, Invoker activator);
    }

    /**
     * Enables the passed tabs (by reference).
     * 
     * @param refs
     *             the references to the tabs to enable.
     */
    public void enable(String... refs);

    /**
     * Disables the passed tabs (by reference).
     * 
     * @param refs
     *             the references to the tabs to disable.
     */
    public void disable(String... refs);

    /**
     * Determines if the passed reference is currently active.
     * 
     * @param ref
     *            the reference to test.
     * @return {@code true} if it is currently active.
     */
    public boolean isActive(String ref);

    /**
     * Tab set configuration (to create groups and tabs against).
     * 
     * @return the configuration.
     */
    public ITabSetConfiguration config();

}
