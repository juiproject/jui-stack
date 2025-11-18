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
package com.effacy.jui.ui.client.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.json.annotation.Transient;
import com.effacy.jui.ui.client.navigation.TabNavigator.ITabActivator;

public class TabCollection {

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
     * See {@link #getTabGroups()}.
     */
    private List<TabGroupConfig> groups = new ArrayList<> ();

    /**
     * The current tab group.
     */
    private TabGroupConfig currentGroup;

    /********************************************************************
     * Construction.
     ********************************************************************/

    /**
     * Clears the configuration of tabs.
     * 
     * @return this config.
     */
    void clear() {
        currentGroup = null;
        groups.clear ();
    }

    /**
     * Adds a tab group. All subsequent tabs are added to this group.
     * 
     * @param label
     *              the label
     * @param icon
     *              (optional) an icon CSS.
     * @return this configuration instance.
     */
    public ITabGroupConfig group(String label) {
        currentGroup = new TabGroupConfig (groups.size (), label);
        groups.add (currentGroup);
        return currentGroup;
    }

    /**
     * Adds a tab to the tab set (always to the last created tab group).
     * 
     * @param reference
     *                  the reference to the tab.
     * @param label
     *                  display label for the tab.
     * @param activator
     *                  the tab activator.
     * @return the tab (for further configuration).
     */
    public ITabConfig tab(String reference, String label, ITabActivator activator) {
        if (currentGroup == null) {
            // Create a silent group.
            currentGroup = new TabGroupConfig (groups.size (), null);
            groups.add (currentGroup);
        }
        TabConfig tab = new TabConfig (reference, label, activator);
        currentGroup.getTabs ().add (tab);
        return tab;
    }

    public ITabConfig tab(String reference, String label, IComponent component, LayoutData layoutData) {
        if (currentGroup == null) {
            // Create a silent group.
            currentGroup = new TabGroupConfig (groups.size (), null);
            groups.add (currentGroup);
        }
        TabConfig tab = new TabConfig (reference, label, component, layoutData);
        currentGroup.getTabs ().add (tab);
        return tab;
    }

    public ITabConfig tab(String reference, String label, Consumer<Consumer<IComponent>> component, LayoutData layoutData) {
        if (currentGroup == null) {
            // Create a silent group.
            currentGroup = new TabGroupConfig (groups.size (), null);
            groups.add (currentGroup);
        }
        TabConfig tab = new TabConfig (reference, label, component, layoutData);
        currentGroup.getTabs ().add (tab);
        return tab;
    }


    public ITabConfig tab(String label, Invoker handler) {
        if (currentGroup == null) {
            // Create a silent group.
            currentGroup = new TabGroupConfig (groups.size (), null);
            groups.add (currentGroup);
        }
        TabConfig tab = new TabConfig (label, handler);
        currentGroup.getTabs ().add (tab);
        return tab;
    }

    /**
     * Finds the configuration for a given tab reference.
     * 
     * @param ref
     *            the reference.
     * @return the associated config or {@code null}.
     */
    public TabConfig findTab(String ref) {
        if (ref == null)
            return null;
        for (TabGroupConfig group : groups) {
            for (TabConfig cfg : group.getTabs ()) {
                if (ref.equals (cfg.reference))
                    return cfg;
            }
        }
        return null;
    }

    /**
     * All the tab groups (in order).
     * 
     * @return the groups.
     */
    @Transient
    public List<TabGroupConfig> getTabGroups() {
        // Correctly set first.
        boolean first = true;
        for (TabGroupConfig grp : groups) {
            grp.first = false;
            if (first && !grp.silent) {
                grp.first = true;
                first = false;
            }
        }
        return groups;
    }

    /**
     * Determines if there are no declared tabs.
     * 
     * @return {@code true} if there are none.
     */
    public boolean isEmpty() {
        if (groups.isEmpty ())
            return true;
        for (TabGroupConfig group : groups) {
            if (!group.getTabs ().isEmpty ())
                return false;
        }
        return true;
    }

    /************************************************************************
     * Implementation classes.
     ************************************************************************/

    /**
     * Defines a grouping of tabs.
     */
    class TabGroupConfig implements ITabGroupConfig {

        /**
         * Index reference to the group (see constructor).
         */
        int idx;

        /**
         * Internal flag to mark the first non-silent group.
         */
        boolean first = false;

        /**
         * See {@link #collapsable(boolean)}.
         */
        boolean collapsable;

        /**
         * See {@link #expand()}.
         */
        boolean expand;

        /**
         * If the group is silient (i.e. not to display any headers).
         */
        boolean silent;

        /**
         * See {@link #icon(String)}.
         */
        String icon;

        /**
         * The label (see constructor).
         */
        String label;

        /**
         * See {@link #getTabs()}.
         */
        List<TabConfig> tabs = new ArrayList<> ();

        /**
         * Creates an explicit grouping with an (optional) icon and display label.
         * 
         * @param icon
         *              (optional) the icon CSS.
         * @param label
         *              the display label.
         */
        public TabGroupConfig(int idx, String label) {
            this.idx = idx;
            silent = (label == null);
            this.label = label;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.tabs.ITabSet.ITabGroupConfig#icon(java.lang.String)
         */
        public ITabGroupConfig icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.tabs.ITabSet.ITabGroupConfig#collapsable(boolean)
         */
        @Override
        public ITabGroupConfig collapsable(boolean collapsable) {
            this.collapsable = collapsable;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.tabs.ITabSet.ITabGroupConfig#expand()
         */
        @Override
        public ITabGroupConfig expand() {
            this.expand = true;
            return this;
        }

        /**
         * The tabs in the group.
         * 
         * @return the tabs.
         */
        public List<TabConfig> getTabs() {
            return tabs;
        }

    }

    /**
     * Configuration for a specific tab.
     */
    class TabConfig implements ITabConfig {

        String reference;

        String icon;

        String label;

        String indicator;

        int count;

        Invoker handler;

        Consumer<Consumer<IComponent>> componentdDeferred;

        IComponent component;

        LayoutData layoutData;
        
        ITabActivator activator;

        Consumer<INavigateCallback> inbound;

        INavigationHandlerProvider handlerProvider;

        /**
         * Construct a tab.
         * 
         * @param reference
         *                   the invocation reference.
         * @param icon
         *                   (optional) and icon.
         * @param label
         *                   the display label
         * @param (optional)
         *                   a count to display.
         */
        public TabConfig(String reference, String label, ITabActivator activator) {
            this.reference = reference;
            this.label = label;
            this.activator = activator;
        }

        public TabConfig(String reference, String label, IComponent component, LayoutData layoutData) {
            this.reference = reference;
            this.label = label;
            this.component = component;
            this.layoutData = layoutData;
        }

        public TabConfig(String reference, String label, Consumer<Consumer<IComponent>> componentdDeferred, LayoutData layoutData) {
            this.reference = reference;
            this.label = label;
            this.componentdDeferred = componentdDeferred;
            this.layoutData = layoutData;
        }

        /**
         * Construct an action-only tab.
         * <p>
         * This type of tab appears like a tab but acts like a button. When clicked some
         * action is performed other that instigating a change in navigation.
         * 
         * @param reference
         *                   the invocation reference.
         * @param icon
         *                   (optional) and icon.
         * @param label
         *                   the display label
         * @param (optional)
         *                   a count to display.
         */
        public TabConfig(String label, Invoker handler) {
            this.reference = "action-" + UID.createUID ();
            this.label = label;
            this.handler = handler;
        }

        /**
         * Assigns an initial count indicator value.
         * 
         * @param count
         *              the count to display (0 display nothing).
         * @return this tab configuration.
         */
        public TabConfig count(int count) {
            this.count = count;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.tabs.ITabSet.ITabConfig#icon(java.lang.String)
         */
        @Override
        public ITabConfig icon(String icon) {
            this.icon = icon;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.tabs.ITabSet.ITabConfig#indicator(java.lang.String)
         */
        @Override
        public ITabConfig indicator(String indicator) {
            this.indicator = indicator;
            return this;
        }            

        /**
         * This can be used to block navigate away requests (i.e. if there are unsaved
         * changes).
         * 
         * @param handler
         *                the handler.
         * @return this tab configuration.
         */
        public TabConfig navigationHandler(Consumer<INavigateCallback> handler) {
            this.inbound = handler;
            return this;
        }

        /**
         * Register a navigation handler for the tab. This is used when the element
         * associated to the tab will have its own navigation and needs to receive
         * navigation requests.
         * 
         * @param handler
         *                the handler.
         * @return this tab configuration.
         */
        public TabConfig handler(final INavigationHandler handler) {
            this.handlerProvider = new INavigationHandlerProvider () {

                @Override
                public INavigationHandler handler() {
                    return handler;
                }

            };
            return this;
        }

        /**
         * See {@link #handler(INavigationHandler)} but registers a provider.
         * 
         * @param handlerProvider
         *                        the handler provider.
         * @return this tab configuration.
         */
        public TabConfig handlerProvider(INavigationHandlerProvider handlerProvider) {
            this.handlerProvider = handlerProvider;
            return this;
        }

    }

}
