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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.IClosable;
import com.effacy.jui.core.client.IOpenAware;
import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.CardFitLayout.Config.Effect;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H6;
import com.effacy.jui.core.client.dom.builder.Header;
import com.effacy.jui.core.client.dom.builder.I;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Strong;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationAwareItem;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;
import com.effacy.jui.core.client.navigation.INavigationHandlerWithProvider;
import com.effacy.jui.core.client.navigation.NavigationHandler;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.With;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.Modal.IModalController;
import com.effacy.jui.ui.client.navigation.TabCollection.ITabConfig;
import com.effacy.jui.ui.client.navigation.TabCollection.ITabGroupConfig;
import com.effacy.jui.ui.client.navigation.TabCollection.TabConfig;
import com.effacy.jui.ui.client.navigation.TabCollection.TabGroupConfig;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

public class TabNavigator extends Component<TabNavigator.Config> implements INavigationHandlerWithProvider, INavigationAware, IClosable, IOpenAware {

    public interface ITabActivator {

        public void activate(NavigationContext context, Consumer<ActivateOutcome> outcome);

        public void deactivate();

        public static ITabActivator create(BiConsumer<NavigationContext, Consumer<ActivateOutcome>> activator, Invoker deactivator) {
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
     * Configuration for the panel.
     */
    public static class Config extends Component.Config {

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

        /**
         * See {@link #padding(Insets)}.
         */
        private Insets padding;

        /**
         * See {@link #propagateOpen(boolean)}.
         */
        private boolean propagateOpen;

        /**
         * See {@link #effect(Effect)}.
         */
        private Effect effect;

        /**
         * See {@link #color(String)}.
         */
        protected String color;

        /**
         * See {@link #collapseHandler(BiConsumer)}.
         */
        protected BiConsumer<TabNavigator,Boolean> collapseHandler;

        /**
         * See {@link #minHeight(Length)}.
         */
        protected Length minHeight;

        protected TabCollection tabs = new TabCollection();

        /**
         * Empty constructor (with defaults).
         */
        public Config() {
            // Nothing.
        }

        /**
         * Assigns a tab style.
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config style(Config.Style style) {
            this.style = style;
            return this;
        }

        /**
         * Getter for {@link #style(Style)}.
         */
        public Config.Style getStyle() {
            if (style == null)
                style = Config.Style.HORIZONTAL;
            return style;
        }

        /**
         * Getter for {@link #padding(Padding)}.
         */
        public Insets getPadding() {
            return padding;
        }

        /**
         * Assigns padding.
         * 
         * @param padding
         *                the padding.
         * @return this configuration instance.
         */
        public Config padding(Insets padding) {
            this.padding = padding;
            return this;
        }

        /**
         * Propagates open (see {@link IOpenAware#onOpen()}). This allows the tabbed
         * panel to not propagate this and rely on other mechanisms.
         * 
         * @param propagateOpen
         *                      {@code true} to propagate.
         * @return this configuration instance.
         */
        public Config propagateOpen(boolean propagateOpen) {
            this.propagateOpen = propagateOpen;
            return this;
        }

        /**
         * See {@link #propagateOpen(boolean)}. Convenience to pass <code>true</code>.
         * 
         * @return this configuration instance.
         */
        public Config propagateOpen() {
            return propagateOpen (true);
        }

        /**
         * Determines if a transiation effect should be employed.
         * 
         * @param effect
         *               the transition effect to apply.
         * @return this configuration instance.
         */
        public Config effect(Effect effect) {
            this.effect = effect;
            return this;
        }

        /**
         * Applies a background color for the contents area.
         * 
         * @param color
         *              the color to apply.
         * @return this configuration instance.
         */
        public Config color(String color) {
            this.color = color;
            return this;
        }

        /**
         * Assign an handler to be invoked when the menu is collapsed or opened.
         * <p>
         * The handler is invoked with the panel instance and a boolean that is
         * {@code true} on open and {@code false} on close.
         * 
         * @param collapseHandler
         *                        the handler to register.
         * @return this configuration instance.
         */
        public Config collapseHandler(BiConsumer<TabNavigator,Boolean> collapseHandler) {
            this.collapseHandler = collapseHandler;
            return this;
        }

        /**
         * Assigns a minimum height.
         * 
         * @param minHeight
         *                  the minimum height.
         * @return this configuration instance.
         */
        public Config minHeight(Length minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        /**
         * Starts a group.
         * 
         * @param label
         *              the label for the group.
         * @return the group (for further configuration).
         */
        public ITabGroupConfig group(String label) {
            return tabs.group(label);
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
            return tabs.tab(reference, label, activator);
        }
    
        public ITabConfig tab(String label, Invoker handler) {
            return tabs.tab(label, handler);
        }

        public ITabConfig tab(String reference, String label, IComponent component) {
            return tabs.tab (reference, label, component, null);
        }

        public ITabConfig tab(String reference, String label, IComponent component, LayoutData layoutData) {
            return tabs.tab (reference, label, component, layoutData);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TabNavigator build(LayoutData... data) {
            return (TabNavigator) super.build (new TabNavigator (this), data);
        }

    }

    /**
     * Region containing the components being managed.
     */
    private static final String REGION_BODY = "body";

    /**
     * Construct with defaults.
     */
    public TabNavigator() {
        this (new Config ());
    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TabNavigator(Config config) {
        super (config);
    }

    /**
     * Declares a grouping of tabs. All tabs added after this will be included in
     * this group.
     * 
     * @param label
     *              the group label.
     * @param icon
     *              (optional) the icon for the group.
     */
    public ITabGroupConfig group(String label) {
        return config().tabs.group (label);
    }

    /**
     * See {@link #add(String, String, String, IComponent, LayoutData)} but without
     * layout data or icon.
     */
    public <C extends IComponent> ITabConfig tab(String reference, String label, C cpt) {
        return tab (reference, label, cpt, null);
    }

    /**
     * See {@link #add(String, String, String, IComponent, LayoutData)} but without
     * layout data or icon.
     */
    public <C extends IComponent> ITabConfig tab(String label, Invoker handler) {
        ITabConfig tab = config().tabs.tab (label, handler);
        return tab;
    }

    /**
     * Adds the passed component to the panel.
     * <p>
     * This must be done before rendering (otherwise the tabs will not update).
     * 
     * @param reference
     *                   the navigation reference to activate the tab.
     * @param label
     *                   the display icon for the tab.
     * @param cpt
     *                   the component to add.
     * @param layoutData
     *                   (optional) layout data to apply.
     * @return the passed component.
     */
    public ITabConfig tab(String reference, String label, IComponent cpt, LayoutData layoutData) {
        return config().tab (reference, label, cpt, layoutData);
    }

    /**
     * Adds the passed component to the panel.
     * <p>
     * This must be done before rendering (otherwise the tabs will not update).
     * 
     * @param reference
     *                         the navigation reference to activate the tab.
     * @param label
     *                         the display label for the tab.
     * @param componentBuilder
     *                         the a builder for the component (that supplies a
     *                         component).
     * @return the passed component.
     */
    public <C extends IComponent> ITabConfig tabSupplied(String reference, String label, Supplier<C> componentBuilder) {
        return tab (reference, label, componentBuilder.get ());
    }

    /**
     * Adds the passed component to the panel. This is done in a manner that defers
     * the retrieval of the component via a means that can allow asynchronous
     * component generation.
     * <p>
     * This must be done before rendering (otherwise the tabs will not update).
     * 
     * @param reference
     *                  the navigation reference to activate the tab.
     * @param label
     *                  the display icon for the tab.
     * @param label
     *                  the display label for the tab.
     * @param cpt
     *                  deferred component evaluator.
     */
    public ITabConfig tabDeferred(String reference, String label, Consumer<Consumer<IComponent>> deferredCpt) {
        return config().tabs.tab (reference, label, deferredCpt, null);
    }

    /**
     * Used to handle the deferred resolution and activation of a component.
     */
    class ComponentHandler {

        /**
         * The associated tab.
         */
        private ITabConfig tab;

        /**
         * The resolved component.
         */
        private IComponent cpt;

        /**
         * The deferred component resolver.
         */
        private Consumer<Consumer<IComponent>> deferredCpt;

        /**
         * Prevents re-entrance when activating when resolving the component.
         */
        private Promise<ActivateOutcome> building = null;

        /**
         * Construct with a deferred component creator.
         * 
         * @param deferredCpt
         *                    the component creator.
         */
        public ComponentHandler(Consumer<Consumer<IComponent>> deferredCpt) {
            this.deferredCpt = deferredCpt;
        }

        /**
         * Activates the component. This will handle the async building of the
         * component.
         * 
         * @param ctx
         *            the navigation context.
         * @return a promise that is invoked when the item is activated (and the value
         *         is if it was activated).
         */
        public Promise<ActivateOutcome> activate(NavigationContext ctx) {
            if (cpt == null) {
                if (building != null)
                    return building;
                building = Promise.create ();
                deferredCpt.accept (createdCpt -> {
                    this.cpt = createdCpt;
                    bind (tab, createdCpt, null);
                    activate (ctx).onFulfillment (v -> {
                        building.fulfill (v);
                        building = null;
                    });
                });
                return building;
            }

            // Activate the component.
            return ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).activate (cpt).onFulfillment (activated -> {
                // If wasn't already activated then alert the change in navigation.
                if ((ActivateOutcome.ACTIVATED == activated) && (cpt instanceof INavigationAware))
                    ((INavigationAware) cpt).onNavigateTo (ctx);
            });
        }

        public void deactivate() {
            if (cpt instanceof INavigationAware)
                ((INavigationAware) cpt).onNavigateDeactivated ();
        }

        /**
         * Assigns the related tab.
         * 
         * @param tab
         *            the tab.
         */
        public void assign(ITabConfig tab) {
            this.tab = tab;
        }

    }

    /**
     * Binds a tab and a component for navigation events and register the component
     * against the panel.
     * 
     * @param tab
     *                   the tab.
     * @param cpt
     *                   the component
     * @param layoutData
     *                   (optional 0..1) layout data to apply.
     */
    @SuppressWarnings("unchecked")
    private void bind(ITabConfig tab, IComponent cpt, LayoutData layoutData) {
        if (cpt == null)
            return;

        // We convey modal control events up to the panel.
        cpt.convey (TabNavigator.this, IModalController.class);

        // If the component is navigation aware then register against the tab handler
        if (cpt instanceof INavigationAware) {
            tab.navigationHandler (c -> {
                ((INavigationAware) cpt).onNavigateFrom (c);
            });
        }

        // If the component has its own navigation the extract that.
        if (cpt instanceof INavigationHandlerProvider)
            tab.handlerProvider ((INavigationHandlerProvider) cpt);
        else if (cpt instanceof INavigationHandler)
            tab.handler ((INavigationHandler) cpt);

        // Add the component to the body.
        findRegionPoint (REGION_BODY).add (cpt, layoutData);
    }

    /**
     * Invoked when a link has been clicked. Can be called directly to mimic a tab
     * click.
     * 
     * @param ref
     *            the reference to the tab.
     */
    public void onTabClicked(String ref) {
        TabConfig tab = config ().tabs.findTab (ref);
        if (tab.handler != null)
            tab.handler.invoke ();
        else
            handler.navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false), ref);
    }

    /**
     * Determines if the passed reference is currently active.
     * 
     * @param ref
     *            the reference to check.
     * @return {@code true} if it is active.
     */
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
     * Update the count on the given tab.
     * 
     * @param ref
     *              the tab reference.
     * @param count
     *              the count.
     */
    public void updateTabCount(String ref, int count) {
        if (!isRendered ()) {
            TabConfig tab = config ().tabs.findTab (ref);
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
            TabConfig tab = config ().tabs.findTab (ref);
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
            TabConfig tab = config ().tabs.findTab (ref);
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
        TabConfig tab = config ().tabs.findTab (ref);
        if (tab == null)
            return;
        LOOP: for (TabGroupConfig grp : config ().tabs.getTabGroups ()) {
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
     * Obtains the currently active item.
     * 
     * @return the active item.
     */
    public IComponent getActiveItem() {
        return ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).getActiveItem ();
    }

    /**
     * See {@link #collapse(boolean)}, when called prior to rendering.
     */
    private boolean collpased;

    /**
     * Collapses the tabset to nothing.
     * <p>
     * Not all styles support collapsing.
     * 
     * @param collapse
     *                 {@code true} to collapse, otherwise restores.
     */
    public void collapse(boolean collapse) {
        this.collpased = collapse;

        if (isRendered()) {
            if (collapse)
                getRoot().classList.add (styles ().collapse ());
            else
                getRoot().classList.remove (styles ().collapse ());
        }

        if (config().collapseHandler != null)
            config().collapseHandler.accept(this, !collapse);
    }

    /**
     * Determines if the tabset is collapsed.
     * 
     * @return {@code true} if it is.
     */
    public boolean collapsed() {
        return collpased;
    }

    /************************************************************************
     * Children
     ************************************************************************/

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
        for (TabGroupConfig group : config ().tabs.getTabGroups ()) {
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
     *             the references of the tabs to enable.
     */
    public void enable(String... refs) {
        for (String ref : refs)
            _enable (ref);
    }

    /**
     * Disables the passed tabs (by reference).
     * 
     * @param refs
     *             the references of the tabs to disable.
     */
    public void disable(String... refs) {
        for (String ref : refs)
            _disable (ref);
    }

    /**
     * Utility method to enable or disable a tab based on a flag.
     * 
     * @param enable
     *               {@code true} to enable, otherwise disables.
     * @param refs
     *               the reference of the tabs to change the state of.
     */
    public void enable (boolean enable, String... refs) {
        if (enable)
            enable  (refs);
        else
            disable (refs);
    }

    /**
     * The children of the panel.
     * 
     * @return the children.
     */
    public Iterable<IComponent> getChildren() {
        return findRegionPoint (REGION_BODY);
    }

    /************************************************************************
     * Navigation
     ************************************************************************/

    /**
     * Underlying navigation handler.
     */
    private NavigationHandler<INavigationAwareItem> handler = new NavigationHandler<INavigationAwareItem> ();

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandlerProvider#handler()
     */
    @Override
    public INavigationHandler handler() {
        return handler;
    }

    /**
     * Navigate using a default context.
     * 
     * @param path
     *             the navigation path.
     */
    public void navigate(String... path) {
        navigate (new NavigationContext (), path);
    }

    /**
     * Navigate using a default context.
     * 
     * @param path
     *             the navigation path.
     */
    public void navigate(List<String> path) {
        navigate (new NavigationContext (), path);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationAware#onNavigateFrom(com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback)
     */
    @Override
    public void onNavigateFrom(INavigateCallback cb) {
        // Pass through to the component.
        IComponent cpt = ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).getActiveItem ();
        if ((cpt != null) && (cpt instanceof INavigationAware))
            ((INavigationAware) cpt).onNavigateFrom (cb);
        else
            INavigationAware.super.onNavigateFrom (cb);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IClosable#close()
     */
    @Override
    public void close() {
        // Fires a close event.
        fireEvent (IModalController.class).close ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IOpenAware#onOpen()
     */
    @Override
    public void onOpen() {
        if (config ().propagateOpen) {
            forEach (cpt -> {
                if (cpt instanceof IOpenAware)
                    ((IOpenAware) cpt).onOpen ();
            });
        }
    }

    /************************************************************************
     * Rendering
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
    class Tab {

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

    private Element tabHolderEl;

    /**
     * Refreshes the tabs from configuration.
     */
    protected void _refresh() {
        tabs.clear();
        buildInto(tabHolderEl, root -> {
            for (TabGroupConfig tabGroup : config().tabs.getTabGroups ()) {
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
        }, dom -> {
            groupsEl = dom.all ("group");
            tabsEl = dom.all ("tab");
        });

        // Create a map of the tab references to the tab items so they may
        // be controlled.
        for (Element el : tabsEl) {
            String ref = el.getAttribute ("item");
            if (!StringSupport.empty (ref))
                tabs.put (ref, new Tab (el));
        }

        // Any counts.
        for (TabGroupConfig group : config ().tabs.getTabGroups ()) {
            for (TabConfig tab : group.getTabs ()) {
                if (tab.count > 0)
                    _updateTabCount (tab.reference, tab.count);
            }
        }
    }
   
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return Wrap.$ (el).$ (root -> {
            // Apply root-level styles.
            if (!StringSupport.empty (config ().color))
                root.css ("backgroundColor", config ().color);
            String orientation = config ().getStyle ().vertical () ? styles ().vertical () : styles ().horizontal ();
            root.style (styles ().component (), orientation);

            // Build out DOM structure.
            Div.$ (root).$ (wrap -> {
                wrap.style (styles ().wrap (), orientation);
                Header.$ (wrap).$ (tabs -> {
                    tabs.style (styles ().tabs (), orientation);
                    tabs.use (n -> tabHolderEl = (Element) n);
                });
                Div.$ (wrap).$ (body -> {
                    if (data.minHeight != null)
                        body.css (CSS.MIN_HEIGHT, data.minHeight);
                    body.style (styles ().body (), orientation);
                    body.use (region (REGION_BODY, new CardFitLayout.Config ().effect (config ().effect).build ()));
                    if (config ().getPadding () != null) {
                        // Applies internal padding which is realised as a margin (since the
                        // behaviour of the layout positions absolutely).
                        body.css (CSS.MARGIN, config ().getPadding ());
                    }
                });
            });
        }).build ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.AbstractBaseComponent#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender ();

        // Assign activators.
        config().tabs.getTabGroups().forEach(group -> {
            group.getTabs().forEach(tab -> {
                if (tab.activator != null)
                    return;
                if (tab.component != null) {
                    ITabActivator activator = ITabActivator.create ((ctx, cb) -> {
                        ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).activate (tab.component).onFulfillment (cb);
                        if (tab.component instanceof INavigationAware)
                            ((INavigationAware) tab.component).onNavigateTo (ctx);
                        if (Debug.isTestMode ())
                            getRoot().setAttribute("test-state", tab.reference);
                    }, () -> {
                        if (tab.component instanceof INavigationAware)
                            ((INavigationAware) tab.component).onNavigateDeactivated ();
                    });
                    tab.activator = activator;
                    if (tab.component instanceof Component) {
                        Component.Config config = ((Component<?>) tab.component).config ();
                        if ((config != null) && StringSupport.empty (config.getTestId()))
                            config.testId (tab.reference);
                    }
                    bind (tab, tab.component, tab.layoutData);
                } else if (tab.componentdDeferred != null) {
                    ComponentHandler handler = new ComponentHandler (tab.componentdDeferred);
                    ITabActivator activator = ITabActivator.create ((ctx, cb) -> {
                        if (Debug.isTestMode())
                            getRoot().setAttribute ("test-state", tab.reference);
                        handler.activate (ctx).onFulfillment (outcome -> {
                            if (cb != null)
                                cb.accept (outcome);
                        });
                    }, () -> {
                        handler.deactivate ();
                    });
                    tab.activator = activator;
                    handler.assign (tab);
                }
            });
        });

        // Build out the tabs.
        _refresh();
 
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
        for (TabGroupConfig group : config ().tabs.getTabGroups ()) {
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
        if (this.collpased)
            collapse (true);
    }

    /**
     * Registers the tabs with the handler.
     */
    protected void _registerTabs() {
        handler.clearAll ();
        _forEach ((ref, tab) -> {
            final TabConfig cfg = config ().tabs.findTab (ref);
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
                            TabNavigator.this.activate (ref);
                            return promise;
                        }
                        TabNavigator.this.activate (ref);
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

    /************************************************************************
     * Styles
     ************************************************************************/

    @Override
    protected ILocalCSS styles() {
        return config().style.styles();
    }

    public static interface ILocalCSS extends IComponentCSS {

        /**
         * Base CSS.
         */
        public static final String CSS = "com/effacy/jui/ui/client/navigation/TabNavigator.css";

        /**
         * Base CSS (for override).
         */
        public static final String CSS_OVERRIDE = "com/effacy/jui/ui/client/navigation/TabNavigator_Override.css";

        public String wrap();

        public String vertical();

        public String horizontal();

        public String body();

        /**
         * WAS HEADER.
         */
        public String tabs();

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
        "com/effacy/jui/ui/client/navigation/TabNavigator_Horizontal.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_Horizontal_Override.css"
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
        "com/effacy/jui/ui/client/navigation/TabNavigator_HorizontalUnderline.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_HorizontalUnderline_Override.css"
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
        "com/effacy/jui/ui/client/navigation/TabNavigator_HorizontalBar.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_HorizontalBar_Override.css"
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
        "com/effacy/jui/ui/client/navigation/TabNavigator_Vertical.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_Vertical_Override.css" })
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
        "com/effacy/jui/ui/client/navigation/TabNavigator_Vertical.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_VerticalAlt.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_VerticalAlt_Override.css"
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
        "com/effacy/jui/ui/client/navigation/TabNavigator_VerticalCompact.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_VerticalCompact_Override.css"
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
        "com/effacy/jui/ui/client/navigation/TabNavigator_Vertical.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_Vertical_Override.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_VerticalIcon.css",
        "com/effacy/jui/ui/client/navigation/TabNavigator_VerticalIcon_Override.css"
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

