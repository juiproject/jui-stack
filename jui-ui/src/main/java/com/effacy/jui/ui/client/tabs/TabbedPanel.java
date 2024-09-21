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

import java.util.List;
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
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Header;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;
import com.effacy.jui.core.client.navigation.INavigationHandlerWithProvider;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.modal.Modal.IModalController;
import com.effacy.jui.ui.client.tabs.ITabSet.ITabConfig;
import com.effacy.jui.ui.client.tabs.ITabSet.ITabGroupConfig;
import com.effacy.jui.ui.client.tabs.ITabSet.ITabSetConfiguration.ITabActivator;
import com.google.gwt.core.client.GWT;

import elemental2.dom.Element;

/**
 * A simple tabbed panel that can orient either vertically or horizontally.
 * <p>
 * When running in test mode the {@code test-state} will contain the reference
 * of the currently active tab. This is the same behaviour as {@link TabSet}.
 *
 * @author Jeremy Buckley
 */
public class TabbedPanel extends Component<TabbedPanel.Config> implements INavigationHandlerWithProvider, INavigationAware, IClosable, IOpenAware {

    /**
     * Configuration for the panel.
     */
    public static class Config extends Component.Config {

        /**
         * See {@link #style(TabSet.Config.Style)}.
         */
        private TabSet.Config.Style style;

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
        protected BiConsumer<TabbedPanel,Boolean> collapseHandler;

        /**
         * See {@link #minHeight(Length)}.
         */
        protected Length minHeight;

        /**
         * Empty constructor (with defaults).
         */
        public Config() {
            // Nothing.
        }

        /**
         * Construct with a given tab style.
         * 
         * @param style
         *              the tabs style.
         */
        public Config(TabSet.Config.Style style) {
            style (style);
        }

        /**
         * Getter for
         * {@link #style(com.effacy.jui.ui.client.tabs.TabSet.Config.Style)}.
         */
        public TabSet.Config.Style getStyle() {
            if (style == null)
                style = TabSet.Config.Style.HORIZONTAL;
            return style;
        }

        /**
         * Assigns a tab style.
         * 
         * @param style
         *              the style.
         * @return this configuration instance.
         */
        public Config style(TabSet.Config.Style style) {
            this.style = style;
            return this;
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
        public Config collapseHandler(BiConsumer<TabbedPanel,Boolean> collapseHandler) {
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
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.Component.Config#build(com.effacy.jui.core.client.component.layout.LayoutData[])
         */
        @Override
        @SuppressWarnings("unchecked")
        public TabbedPanel build(LayoutData... data) {
            return (TabbedPanel) super.build (new TabbedPanel (this), data);
        }

    }

    /**
     * Region containing the components being managed.
     */
    private static final String REGION_BODY = "body";

    /**
     * Displays the tabs and manages navigation.
     */
    private ITabSet tabs;

    /**
     * Construct with defaults.
     */
    public TabbedPanel() {
        this (new Config ());
    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    public TabbedPanel(Config config) {
        this (config, new TabSet (new TabSet.Config (config.getStyle ())));
    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     * @param tabSet
     *               a custom tab set to use.
     */
    public TabbedPanel(Config config, ITabSet tabSet) {
        super (config);
        tabs = tabSet;
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
        return tabs.config ().group (label);
    }

    /**
     * Determines if the passed reference is currently active.
     * 
     * @param ref
     *            the reference to check.
     * @return {@code true} if it is active.
     */
    public boolean isActive(String ref) {
        return tabs.isActive(ref);
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
        ITabConfig tab = tabs.config ().tab (label, handler);
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
    public <C extends IComponent> ITabConfig tab(String reference, String label, C cpt, LayoutData layoutData) {
        // Create the tab and activate the item when selected. If the item is
        // navigation aware then pass through.
        ITabActivator activator = ITabActivator.create ((ctx, cb) -> {
            ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).activate (cpt).onFulfillment (cb);
            if (cpt instanceof INavigationAware)
                ((INavigationAware) cpt).onNavigateTo (ctx);
            if (Debug.isTestMode ())
                getRoot().setAttribute("test-state", reference);
        }, () -> {
            if (cpt instanceof INavigationAware)
                ((INavigationAware) cpt).onNavigateDeactivated ();
        });
        ITabConfig tab = tabs.config ().tab (reference, label, activator);

        // Set the test ID to be the tab reference.
        if (cpt instanceof Component) {
            Component.Config config = ((Component<?>) cpt).config ();
            if ((config != null) && StringSupport.empty (config.getTestId()))
                config.testId (reference);
        }

        // Bind and return.
        bind (tab, cpt, layoutData);
        return tab;
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
    public <C extends IComponent> ITabConfig tabBuilder(String reference, String label, Supplier<C> componentBuilder) {
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
        ComponentHandler handler = new ComponentHandler (deferredCpt);
        ITabActivator activator = ITabActivator.create ((ctx, cb) -> {
            if (Debug.isTestMode())
                getRoot().setAttribute ("test-state", reference);
            handler.activate (ctx).onFulfillment (outcome -> {
                if (cb != null)
                    cb.accept (outcome);
            });
        }, () -> {
            handler.deactivate ();
        });
        ITabConfig tab = tabs.config ().tab (reference, label, activator);
        handler.assign (tab);
        return tab;
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
        cpt.convey (TabbedPanel.this, IModalController.class);

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
     * Update the count on the given tab.
     * 
     * @param ref
     *              the tab reference.
     * @param count
     *              the count.
     */
    public void updateTabCount(String ref, int count) {
        ((TabSet) tabs).updateTabCount (ref, count);
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
        ((TabSet) tabs).updateTabIcon (ref, icon);
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
        ((TabSet) tabs).updateTabLabel (ref, label);
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
        ((TabSet) tabs).moveTabToAfter (ref, position);
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
     * Collapses the tabset to nothing.
     * <p>
     * Not all styles support collapsing.
     * 
     * @param collapse
     *                 {@code true} to collapse, otherwise restores.
     */
    public void collapse(boolean collapse) {
        if (tabs instanceof TabSet) {
            if (collapse && collapsed ())
                return;
            if (!collapse && !collapsed())
                return;
            ((TabSet)tabs).collapse (collapse);
            if (config().collapseHandler != null)
                config().collapseHandler.accept(this, !collapse);
        }
    }

    /**
     * Determines if the tabset is collapsed.
     * 
     * @return {@code true} if it is.
     */
    public boolean collapsed() {
        if (tabs instanceof TabSet)
            return ((TabSet)tabs).collapsed ();
        return false;
    }

    /************************************************************************
     * Children
     ************************************************************************/

    /**
     * See {@link TabSet#enable(String...)}.
     */
    public void enable(String... refs) {
        tabs.enable (refs);
    }

    /**
     * See {@link TabSet#disable(String...)}.
     */
    public void disable(String... refs) {
        tabs.disable (refs);
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
     * Navigate using a default context.
     * 
     * @param path
     *             the navigation path.
     */
    public void navigate(String... path) {
        this.navigate (new NavigationContext (), path);
    }

    /**
     * Navigate using a default context.
     * 
     * @param path
     *             the navigation path.
     */
    public void navigate(List<String> path) {
        this.navigate (new NavigationContext (), path);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.navigation.INavigationHandlerProvider#handler()
     */
    @Override
    public INavigationHandler handler() {
        return tabs;
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
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
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
                Header.$ (wrap).$ (header -> {
                    header.style (styles ().header (), orientation);
                    header.use (attach (tabs));
                    header.by ("header");
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
     * @see com.effacy.jui.core.client.component.Component#styles()
     */
    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance ();
    }

    public static interface ILocalCSS extends IComponentCSS {

        public String wrap();

        public String vertical();

        public String horizontal();

        public String header();

        public String body();
    }

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/tabs/TabbedPanel.css",
        "com/effacy/jui/ui/client/tabs/TabbedPanel_Override.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
