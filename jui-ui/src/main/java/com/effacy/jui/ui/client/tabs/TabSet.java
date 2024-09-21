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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H6;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Strong;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback;
import com.effacy.jui.core.client.navigation.INavigationAwareItem;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;
import com.effacy.jui.core.client.navigation.INavigationHandlerWithProvider;
import com.effacy.jui.core.client.navigation.NavigationHandler;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.json.annotation.Transient;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.With;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.tabs.TabSet.Config.TabConfig;
import com.effacy.jui.ui.client.tabs.TabSet.Config.TabGroupConfig;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;


/**
 * Displays a tab navigation (in some specified layout) that can be used to
 * control the display of other components (external to this). Generally this
 * will be included in a larger panel where the panel has a containment region
 * that child components are added to, it is these component that are controlled
 * by the tabset.
 * <p>
 * This implements {@link INavigationHandler} so it may partake in a navigation
 * hierarchy. To achieve this the component that includes the tabset must
 * implement {@link INavigationHandlerProvider} and must return the tabset when
 * {@link INavigationHandlerProvider#handler()} is called.
 * <p>
 * In this instance the items are the tabs. When an item is activated the tab
 * will be activated but there need to be a mechanism to have that activation
 * activate an actual component.
 * <p>
 * There are three standard styles (see
 * {@link TabSet.Config.TabSetStyle#HORIZONTAL},
 * {@link TabSet.Config.TabSetStyle#VERTICAL} and
 * {@link TabSet.Config.TabSetStyle#VERTICAL_COMPACT}) that are assignable
 * through configuration. You can create your own styles by implementing custom
 * CSS (for example see {@link TabSet.HorizontalLocalCSS}) and crafting an
 * instance of {@link TabSet.Config.Style}). You can override the styles of
 * these defaults as well. To do this use the standard CSS override mechanism.
 * <p>
 * When running in test mode the {@code test-state} will contain the reference
 * of the currently active tab.
 * 
 * @author Jeremy Buckley
 */
public class TabSet extends Component<TabSet.Config> implements ITabSet, INavigationHandlerWithProvider {

    /**
     * Configuration for the component.
     */
    public static class Config extends Component.Config implements ITabSet.ITabSetConfiguration {

        /********************************************************************
         * Styles for the tab set.
         ********************************************************************/  

        /**
         * Style for the tab set (defines presentation configuration including CSS).
         */
        public interface Style {

            /**
             * Horizontal tabs.
             */
            public static final Style HORIZONTAL = create (HorizontalLocalCSS.instance (), false, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * Horizontal tabs (underline version).
             */
            public static final Style HORIZONTAL_UNDERLINE = create (HorizontalUnderlineLocalCSS.instance (), false, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * Horizontal tabs (bar version). 
             */
            public static final Style HORIZONTAL_BAR = create (HorizontalBarLocalCSS.instance (), false, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * Vertical tabs (icon only).
             */
            public static final Style VERTICAL_ICON = create (VerticalIconLocalCSS.instance (), true, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * Vertical tabs.
             */
            public static final Style VERTICAL = create (VerticalLocalCSS.instance (), true, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * Vertical tabs (alternative).
             */
            public static final Style VERTICAL_ALT = create (VerticalAltLocalCSS.instance (), true, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * Vertical tabs that are slim (only displays the icon) and slide out on hover
             * to display the label.
             */
            public static final Style VERTICAL_COMPACT = create (VerticalCompactLocalCSS.instance (), true, FontAwesome.minus (), FontAwesome.plus ());

            /**
             * The CSS styles.
             */
            public ILocalCSS styles();

            /**
             * Determines if the styles are based on a vertical arrangement.
             * 
             * @return {@code true} if they are.
             */
            public boolean vertical();

            /**
             * The icon to show for an open group (one that is collapsable).
             * 
             * @return the icon.
             */
            public String groupOpenIcon();

            /**
             * The icon to show for a closed group (one that is collapsable).
             * 
             * @return the icon.
             */
            public String groupClosedIcon();

            /**
             * Convenience to create a style.
             * 
             * @return the style.
             */
            public static Style create(ILocalCSS styles, boolean vertical, String groupOpenIcon, String groupClosedIcon) {
                return new Style () {

                    @Override
                    public ILocalCSS styles() {
                        return styles;
                    }

                    @Override
                    public boolean vertical() {
                        return vertical;
                    }

                    @Override
                    public String groupOpenIcon() {
                        return groupOpenIcon;
                    }

                    @Override
                    public String groupClosedIcon() {
                        return groupClosedIcon;
                    }

                };
            }

        }

        /**
         * The styles to apply to the tab set.
         */
        private Style style = Style.HORIZONTAL;

        /********************************************************************
         * Tab groups and tabs.
         ********************************************************************/

        /**
         * Defines a grouping of tabs.
         */
        public class TabGroupConfig implements ITabGroupConfig {

            /**
             * Index reference to the group (see constructor).
             */
            private int idx;

            /**
             * Internal flag to mark the first non-silent group.
             */
            private boolean first = false;

            /**
             * See {@link #collapsable(boolean)}.
             */
            private boolean collapsable;

            /**
             * See {@link #expand()}.
             */
            private boolean expand;

            /**
             * If the group is silient (i.e. not to display any headers).
             */
            private boolean silent;

            /**
             * See {@link #icon(String)}.
             */
            private String icon;

            /**
             * The label (see constructor).
             */
            private String label;

            /**
             * See {@link #getTabs()}.
             */
            private List<TabConfig> tabs = new ArrayList<> ();

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
        public class TabConfig implements ITabConfig {

            private String reference;

            private String icon;

            private String label;

            private String indicator;

            private int count;

            private Invoker handler;
            
            private ITabActivator activator;

            private Consumer<INavigateCallback> inbound;

            private INavigationHandlerProvider handlerProvider;

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
         * Default constructor (the default style is {@link #HORIZONTAL}).
         */
        public Config() {
            super ();
        }

        /**
         * Construct with a specific set of styles (that defines the presentation).
         * <p>
         * See also {@link #VERTICAL}, {@link #VERTICAL_COMPACT} and
         * {@link #HORIZONTAL}.
         * 
         * @param style
         *              the style to apply.
         */
        public Config(Style style) {
            if (style != null)
                this.style = style;
        }

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

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.ui.client.tabs.ITabSet.ITabSetConfiguration#tab(java.lang.String,
         *      com.effacy.jui.ui.client.tabs.ITabSet.ITabClickHandler)
         */
        @Override
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

        /**
         * The display style.
         * 
         * @return the style.
         */
        public Style getStyle() {
            return style;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.ComponentConfig#create(com.effacy.jui.core.client.container.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TabSet build(LayoutData... data) {
            return build (new TabSet (this), data);
        }
    }

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * Construct a notice with the given configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TabSet(Config config) {
        super (config);
    }

    /**
     * Invoked when a link has been clicked. Can be called directly to mimic a tab
     * click.
     * 
     * @param ref
     *            the reference to the tab.
     */
    public void onTabClicked(String ref) {
        TabConfig tab = config ().findTab (ref);
        if (tab.handler != null)
            tab.handler.invoke ();
        else
            handler.navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false), ref);
    }

    /************************************************************************
     * Navigation.
     ************************************************************************/

    /**
     * Update the count on the given tab.
     * 
     * @param ref
     *              the tab reference.
     * @param count
     *              the count.
     */
    public void updateTabCount(String ref, int count) {
        if (!isRendered ()) {
            TabConfig tab = config ().findTab (ref);
            if (tab != null)
                tab.count = count;
        } else
            _updateTabCount (ref, count);
    }

    /**
     * Update the icon on the given tab.
     * 
     * @param ref
     *             the tab reference.
     * @param icon
     *             the icon CSS.
     */
    public void updateTabIcon(String ref, String icon) {
        if (!isRendered ()) {
            TabConfig tab = config ().findTab (ref);
            if (tab != null)
                tab.icon = icon;
        } else
            _updateTabIcon (ref, icon);
    }

    /**
     * Update the label on the given tab.
     * 
     * @param ref
     *              the tab reference.
     * @param label
     *              the label.
     */
    public void updateTabLabel(String ref, String label) {
        if (!isRendered ()) {
            TabConfig tab = config ().findTab (ref);
            if (tab != null)
                tab.label = label;
        } else
            _updateTabLabel (ref, label);
    }

    /**
     * Moves the specified tab to after the target tab.
     * 
     * @param ref
     *                 the reference of the tab to move.
     * @param position
     *                 the position to move to (within its group).
     */
    public void moveTabToAfter(String ref, int position) {
        TabConfig tab = config ().findTab (ref);
        if (tab == null)
            return;
        LOOP: for (TabGroupConfig grp : config ().getTabGroups ()) {
            if (grp.getTabs ().contains (tab)) {
                grp.getTabs ().remove (tab);
                if (position < 0)
                    position = 0;
                if (position >= grp.getTabs ().size ())
                    grp.getTabs ().add (tab);
                else
                    grp.getTabs ().add (position, tab);
                break LOOP;
            }
        }
        if (isRendered ())
            _refresh ();
    }

    /**
     * Activates the specified tab.
     * 
     * @param ref
     *            the reference of the tab to activate.
     */
    public void activate(String ref) {
        _activate (ref);
    }

    /**
     * Activates the first tab (that is not disabled).
     */
    public void activateFirst() {
        for (TabGroupConfig group : config ().getTabGroups ()) {
            for (TabConfig tab : group.getTabs ()) {
                if (_activate (tab.reference))
                    return;
            }
        }
    }

    /**
     * Enables the passed tabs (by reference).
     * 
     * @param refs
     *             the references to the tabs to enable.
     */
    public void enable(String... refs) {
        for (String ref : refs)
            _enable (ref);
    }

    /**
     * Disables the passed tabs (by reference).
     * 
     * @param refs
     *             the references to the tabs to disable.
     */
    public void disable(String... refs) {
        for (String ref : refs)
            _disable (ref);
    }

    /**
     * Updates the tabs from the configuration.
     */
    public void refresh(Consumer<Config> builder) {
        if (builder == null)
            return;
        config ().clear ();
        builder.accept (config ());
        _refresh ();
        _registerTabs ();
    }

    @Override
    public boolean isActive(String ref) {
        if (ref == null)
            return false;
        if (!isRendered ())
            return ref.equals (activatePreRender);
        Tab tab = tabs.get (ref);
        if (tab == null)
            return false;
        return tab.isActive ();
    }

    /**
     * Underlying navigation handler.
     */
    private NavigationHandler<INavigationAwareItem> handler = new NavigationHandler<INavigationAwareItem> ();

    
    @Override
    public INavigationHandler handler() {
        return handler;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#render(elemental2.dom.Element,
     *      int)
     */
    @Override
    public void render(Element target, int index) {
        super.render (target, index);
    }


    /**
     * See {@link #collapse(boolean)}, when called prior to rendering.
     */
    private boolean preRenderCollapse;

    /**
     * Collapses the tabset to nothing.
     * <p>
     * Not all styles support collapsing.
     * 
     * @param collapse
     *                 {@code true} to collapse, otherwise restores.
     */
    public void collapse(boolean collapse) {
        this.preRenderCollapse = collapse;

        if (isRendered()) {
            if (collapse)
                getRoot().classList.add (styles ().collapse ());
            else
                getRoot().classList.remove (styles ().collapse ());
        }
    }

    /**
     * Determines if the tabset is collapsed.
     * 
     * @return {@code true} if it is.
     */
    public boolean collapsed() {
        return preRenderCollapse;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.AbstractBaseComponent#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();
 
        if (tabsEl == null)
            return;

        // Create a map of the tab references to the tab items so they may
        // be controlled.
        for (Element el : tabsEl) {
            String ref = el.getAttribute ("item");
            if (!StringSupport.empty (ref))
                tabs.put (ref, new Tab (el));
        }

        // Any pre-render states.
        if (activatePreRender != null)
            _activate (activatePreRender);
        for (String tab : disablePreRender)
            this._disable (tab);

        // Any counts.
        for (TabGroupConfig group : config ().getTabGroups ()) {
            for (TabConfig tab : group.getTabs ()) {
                tabs.get (tab.reference).group = group.idx;
                if (tab.count > 0)
                    _updateTabCount (tab.reference, tab.count);
            }
        }

        // Assert the groups.
        _refreshGroups ();

        // Register items for each of the tabs.
        _registerTabs ();

        // Invoke a re-navigation to assert state.
        renavigate (new NavigationContext ());

        // Collpase state.
        if (this.preRenderCollapse)
            collapse (true);
    }

    /**
     * Registers the tabs with the handler.
     */
    protected void _registerTabs() {
        handler.clearAll ();
        _forEach ((ref, tab) -> {
            final TabConfig cfg = config ().findTab (ref);
            if (cfg.handler == null) {
                handler.register ((INavigationAwareItem) new INavigationAwareItem () {

                    @Override
                    public String getReference() {
                        return ref;
                    }

                    @Override
                    public boolean isEnabled() {
                        return _isTabEnabled (ref);
                    }

                    @Override
                    public Promise<ActivateOutcome> activate(NavigationContext context) {
                        if (cfg.activator != null) {
                            Promise<ActivateOutcome> promise = Promise.create ();
                            cfg.activator.activate (context, outcome -> promise.fulfill (outcome));
                            TabSet.this.activate (ref);
                            return promise;
                        }
                        TabSet.this.activate (ref);
                        return Promise.create (ActivateOutcome.ACTIVATED);
                    }

                    @Override
                    public void deactivate() {
                        if (cfg.activator != null)
                            cfg.activator.deactivate ();
                    }

                    @Override
                    public boolean activateOnForwardPropagation() {
                        return true;
                    }

                    @Override
                    public void onNavigateFrom(INavigateCallback cb) {
                        // If there is a navigation handler we pass that navigation
                        // request through.
                        if (cfg.inbound != null)
                            cfg.inbound.accept (cb);
                        else
                            INavigationAwareItem.super.onNavigateFrom (cb);
                    }

                    @Override
                    public void onNavigateTo(NavigationContext context) {
                        // Don't worry about this right now.
                    }

                    @Override
                    public INavigationHandler handler() {
                        if (cfg.handlerProvider != null)
                            return cfg.handlerProvider.handler ();
                        return INavigationAwareItem.super.handler ();
                    }

                    /**
                     * {@inheritDoc}
                     *
                     * @see java.lang.Object#toString()
                     */
                    @Override
                    public String toString() {
                        return "TabSetItem";
                    }

                });
            }
        });
    }

    /************************************************************************
     * Presentation
     ************************************************************************/

    /**
     * The various tab elements.
     */
    public List<Element> tabsEl;

    /**
     * The various group headers.
     */
    public List<Element> groupsEl;

    /**
     * Map of tab references to their item.
     */
    public Map<String, Tab> tabs = new HashMap<> ();

    /**
     * Internal state data for a tab.
     */
    public class Tab {

        /**
         * The tab element.
         */
        public Element el;

        /**
         * If the tab is enabled.
         */
        public boolean enabled;

        /**
         * If the tab is activated.
         */
        public boolean active;

        /**
         * The group that the tab belongs to.
         */
        public int group;

        /**
         * Construct around an element (initial state is assumed).
         * 
         * @param el
         *           the tab element.
         */
        public Tab(Element el) {
            this.el = el;
            this.enabled = true;
            this.active = false;
        }

        /**
         * Updates the count on the tab.
         * 
         * @param count
         *              the count.
         */
        public void updateCount(int count) {
            if (count <= 0) {
                el.classList.remove (styles ().count ());
            } else {
                el.classList.add (styles ().count ());
                JQuery.$ (el).find ("i").text ("" + count);
            }
        }

        /**
         * Updates the count on the tab.
         * 
         * @param count
         *              the count.
         */
        public void updateIcon(String icon) {
            JQueryElement em = JQuery.$ (el).find ("em");
            em.removeClass();
            if (!StringSupport.empty (icon))
                em.addClass (icon);
        }

        /**
         * Updates the count on the tab.
         * 
         * @param count
         *              the count.
         */
        public void updateLabel(String label) {
            JQuery.$ (el).find ("span").text (label);
        }

        /**
         * Enable the tab.
         */
        public void enable() {
            el.classList.remove (styles ().disabled ());
            enabled = true;
        }

        /**
         * Disable the tab.
         */
        public void disable() {
            el.classList.add (styles ().disabled ());
            enabled = false;
        }

        /**
         * Activates the tab.
         */
        public void activate() {
            el.classList.add (styles ().active ());
            active = true;
        }

        /**
         * De-activates the tab.
         */
        public void deactivate() {
            el.classList.remove (styles ().active ());
            active = false;
        }

        /**
         * Determines if this tab is currently active.
         * 
         * @return {@code true} if it is.
         */
        public boolean isActive() {
            return el.classList.contains (styles ().active ());
        }
    }

    /**
     * Pre-render activate.
     */
    private String activatePreRender = null;

    /**
     * Tabs to disable post-render.
     */
    private List<String> disablePreRender = new ArrayList<> ();

    /**
     * Refreshes the tabs from configuration.
     */
    protected void _refresh() {
        DomSupport.removeAllChildren (getRoot ());
        onRender (getRoot ());

        // Create a map of the tab references to the tab items so they may
        // be controlled.
        for (Element el : tabsEl) {
            String ref = el.getAttribute ("item");
            if (!StringSupport.empty (ref))
                tabs.put (ref, new Tab (el));
        }

        // Any counts.
        for (TabGroupConfig group : config ().getTabGroups ()) {
            for (TabConfig tab : group.getTabs ()) {
                if (tab.count > 0)
                    _updateTabCount (tab.reference, tab.count);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config config) {
        tabs.clear ();
        return DomBuilder.el (el, root -> {
            for (TabGroupConfig tabGroup : config.getTabGroups ()) {
                if (tabGroup.expand)
                    Div.$ (root).style(styles ().expander ());
                Div.$ (root).$ (group -> {
                    group.style (styles ().group ());
                    if (tabGroup.first)
                        group.style (styles ().first ());
                    if (tabGroup.silent) {
                        group.style (styles ().silent ());
                    } else {
                        H6.$ (group).$ (header -> {
                            header.by ("group");
                            header.attr ("idx", "" + tabGroup.idx);
                            header.style (styles ().header ());
                            if (tabGroup.icon != null)
                                Em.$ (header).style (tabGroup.icon);
                            if (tabGroup.label != null)
                                Span.$ (header).$ ().text (tabGroup.label);
                        });
                    }
                    Ul.$ (group).$ (ul -> {
                        for (TabConfig tab : tabGroup.getTabs ()) {
                            Li.$ (ul).$ (li -> {
                                li.by ("tab");
                                li.on (e -> onTabClicked (e.getTarget ("li", 3).getAttribute ("item")), UIEventType.ONCLICK);
                                li.attr ("item", tab.reference);
                                Div.$ (li).$ (div -> {
                                    if (tab.icon != null)
                                        Em.$ (div).style (tab.icon);
                                    Span.$ (div).text (tab.label);
                                    I.$ (div);
                                });
                                li.testId(buildTestId("tab_" + tab.reference));
                                if (!StringSupport.empty (tab.indicator))
                                    Strong.$ (li).text (tab.indicator);
                            });
                        }
                    });
                });
            }
        }).build (tree -> {
            groupsEl = tree.all ("group");
            tabsEl = tree.all ("tab");
        });
    }

    /**
     * Update the count on the given tab.
     * 
     * @param ref
     *              the tab reference.
     * @param count
     *              the count.
     */
    public void _updateTabCount(String ref, int count) {
        if ((tabs == null) || (ref == null))
            return;
        With.$ (tabs.get (ref), tab -> {
            tab.updateCount (count);
        });
    }

    /**
     * Update the icon on the given tab.
     * 
     * @param ref
     *             the tab reference.
     * @param icon
     *             the icon.
     */
    public void _updateTabIcon(String ref, String icon) {
        if ((tabs == null) || (ref == null))
            return;
        With.$ (tabs.get (ref), tab -> {
            tab.updateIcon (icon);
        });
    }

    /**
     * Update the label on the given tab.
     * 
     * @param ref
     *              the tab reference.
     * @param label
     *              the label.
     */
    public void _updateTabLabel(String ref, String label) {
        if ((tabs == null) || (ref == null))
            return;
        With.$ (tabs.get (ref), tab -> {
            tab.updateLabel (label);
        });
    }

    /**
     * Disables the specified tab.
     * 
     * @param ref
     *            the tab reference.
     */
    public void _disable(String ref) {
        if (!isRendered ()) {
            if (!disablePreRender.contains (ref))
                disablePreRender.add (ref);
            return;
        }
        if ((tabs == null) || (ref == null))
            return;
        With.$ (tabs.get (ref), tab -> {
            tab.disable ();
        });
        _refreshGroups ();
    }

    /**
     * Enables the specified tab.
     * 
     * @param ref
     *            the tab reference.
     */
    public void _enable(String ref) {
        if (!isRendered ()) {
            disablePreRender.remove (ref);
            return;
        }
        if ((tabs == null) || (ref == null))
            return;
        With.$ (tabs.get (ref), tab -> {
            tab.enable ();
        });
        _refreshGroups ();
    }

    /**
     * Refreshes the display state of the groups. This will show a group if any of
     * its tabs is enabled otherwise it will hide the group.
     */
    protected void _refreshGroups() {
        for (Element groupEl : groupsEl) {
            int idx = Integer.parseInt (groupEl.getAttribute ("idx"));
            boolean show = false;
            INNER: for (Tab tab : tabs.values ()) {
                if (tab.group != idx)
                    continue;
                if (tab.enabled) {
                    show = true;
                    break INNER;
                }
            }
            if (show)
                JQuery.$ (groupEl).show ();
            else
                JQuery.$ (groupEl).hide ();
        }
    }

    /**
     * Disables the specified tab.
     * 
     * @param ref
     *            the tab reference.
     * @return {@code true} if was activated.
     */
    public boolean _activate(String ref) {
        if (ref == null)
            return false;
        if (!isRendered ()) {
            activatePreRender = ref;
            return true;
        }
        if (tabs.get (ref) == null)
            return false;
        if (!tabs.get (ref).enabled)
            return false;
        tabs.forEach ((key, tab) -> {
            if (ref.equals (key))
                tab.activate ();
            else
                tab.deactivate ();
        });

        // If running in test mode update the state.
        if (Debug.isTestMode())
            getRoot().setAttribute("test-state", ref);

        return true;
    }

    /**
     * Determines if the given tab (by reference) is enabled.
     * 
     * @param ref
     *            the tab reference.
     * @return {@code true} if it is enabled.
     */
    public boolean _isTabEnabled(String ref) {
        if (ref == null)
            return false;
        Tab tab = tabs.get (ref);
        if (tab == null)
            return false;
        return tab.enabled;
    }

    /**
     * Iterate over each tab.
     * 
     * @param consumer
     *                 the consumer for the tab.
     */
    public void _forEach(BiConsumer<String, Tab> consumer) {
        tabs.forEach ((key, tab) -> consumer.accept (key, tab));
    }

    /********************************************************************
     * CSS with standard styles.
     ********************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    protected ILocalCSS styles() {
        return config ().style.styles ();
    }

    public static interface ILocalCSS extends IComponentCSS {

        /**
         * Base CSS.
         */
        public static final String CSS = "com/effacy/jui/ui/client/tabs/TabSet.css";

        /**
         * Base CSS (for override).
         */
        public static final String CSS_OVERRIDE = "com/effacy/jui/ui/client/tabs/TabSet_Override.css";

        /**
         * Header block.
         */
        public String header();

        /**
         * Tab grouping.
         */
        public String group();

        /**
         * Tab expander.
         */
        public String expander();

        /**
         * Active item.
         */
        public String active();

        /**
         * Shows the count on a tab.
         */
        public String count();

        /**
         * Marker for the first of something.
         */
        public String first();

        /**
         * Used when a group is silent.
         */
        public String silent();

        /**
         * Use to collapse the tab set.
         */
        public String collapse();

    }

    /**
     * Component CSS (horizontal).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_Horizontal.css",
        "com/effacy/jui/ui/client/tabs/TabSet_Horizontal_Override.css"
    })
    public static abstract class HorizontalLocalCSS implements ILocalCSS {

        private static HorizontalLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (HorizontalLocalCSS) GWT.create (HorizontalLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (horizontal underline).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_HorizontalUnderline.css",
        "com/effacy/jui/ui/client/tabs/TabSet_HorizontalUnderline_Override.css"
    })
    public static abstract class HorizontalUnderlineLocalCSS implements ILocalCSS {

        private static HorizontalUnderlineLocalCSS STYLES; 

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (HorizontalUnderlineLocalCSS) GWT.create (HorizontalUnderlineLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (horizontal bar).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_HorizontalBar.css",
        "com/effacy/jui/ui/client/tabs/TabSet_HorizontalBar_Override.css"
    })
    public static abstract class HorizontalBarLocalCSS implements ILocalCSS {

        private static HorizontalBarLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (HorizontalBarLocalCSS) GWT.create (HorizontalBarLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (vertical).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical.css",
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical_Override.css" })
    public static abstract class VerticalLocalCSS implements ILocalCSS {

        private static VerticalLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (VerticalLocalCSS) GWT.create (VerticalLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (vertical).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical.css",
        "com/effacy/jui/ui/client/tabs/TabSet_VerticalAlt.css",
        "com/effacy/jui/ui/client/tabs/TabSet_VerticalAlt_Override.css"
    })
    public static abstract class VerticalAltLocalCSS implements ILocalCSS {

        private static VerticalAltLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (VerticalAltLocalCSS) GWT.create (VerticalAltLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (vertical compact).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_VerticalCompact.css",
        "com/effacy/jui/ui/client/tabs/TabSet_VerticalCompact_Override.css"
    })
    public static abstract class VerticalCompactLocalCSS implements ILocalCSS {

        private static VerticalCompactLocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (VerticalCompactLocalCSS) GWT.create (VerticalCompactLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

    /**
     * Component CSS (vertical).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        ILocalCSS.CSS,
        ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical.css",
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical_Override.css",
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical_Icon.css",
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical_Icon_Override.css"
    })
    public static abstract class VerticalIconLocalCSS implements ILocalCSS {

        private static VerticalIconLocalCSS STYLES; 

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (VerticalIconLocalCSS) GWT.create (VerticalIconLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
