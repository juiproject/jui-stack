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
package com.effacy.jui.core.client.component.layout;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.IActivateListener;
import com.effacy.jui.core.client.component.IAddRemoveListener;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IResizeListener;
import com.effacy.jui.core.client.component.IShowHideListener;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.observable.Observable;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;

import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.Node;
import elemental2.dom.NodeList;
import jsinterop.base.Js;

/**
 * Abstract base class for layouts. These are used to lay out the children of a
 * {@link IContainer} within that containers layout target (see
 * {@link IContainer#getLayoutTarget()}).
 * <p>
 * A layout is usually assigned to a container (a container without a layout
 * will not render its children).
 * <p>
 * The layout process first involves ensuring that the container has been
 * rendered and that the layout is actually dirty (a layout is set to be dirty
 * by calling {@link #markAsDirty()} and the cases where this occurs is
 * determined by the container, such as a change of size). The
 * {@link #onLayout(Element)} method is called which is responsible for
 * rendering an positioning the children, then
 * {@link IContainer#onLayoutComplete(boolean)} is called. After this the
 * children are processed, meaning that {@link Component#recalculate()} is
 * called on components. Finally {@link #onAfterLayout(ILayoutTarget)} is
 * invoked which allows for any fine adjustments to be made knowing that the
 * children of the container have been rendered.
 * 
 * @author Jeremy Buckley
 */
public class Layout extends Observable implements ILayout {
    
    /************************************************************************
     * Debugging
     ************************************************************************/

    /**
     * Various debug modes.
     */
    public enum DebugMode {
        /**
         * Log rendering related activities.
         */
        FLOW(1<<1);

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
            return ((Layout.DEBUG & flag) > 0);
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

    /**
     * The container that this layout instance is associated with. The layout is
     * responsible for laying out the children of this container (and this container
     * only).
     */
    private ILayoutTarget layoutTarget;

    /**
     * Height of the layout target.
     */
    private int layoutTargetHeight = -1;

    /**
     * Optional extra style to apply to child components subject to this layout.
     */
    protected String layoutTargetStyle;

    /**
     * See {@link #isRunning()}.
     */
    private boolean running;

    /**
     * Flag to indicate if the layout is a first layout run.
     */
    private boolean firstLayout = true;

    /**
     * Flag to indicate that the layout is dirty and needs to be cleaned up (i.e.
     * that a layout needs to be performed).
     */
    private boolean dirty = true;

    /**
     * Listens to changes in the child components.
     */
    private LocalComponentListener componentListener = new LocalComponentListener ();

    /**
     * Listens to changes in the container.
     */
    private LocalContainerListener containerListener = new LocalContainerListener ();

    /**
     * The renderer to use.
     */
    protected ILayoutRenderer renderer = null;

    /**
     * The various component layouts.
     */
    protected List<IComponentLayout> layouts = new ArrayList<IComponentLayout> ();

    /**
     * Default constructor.
     */
    protected Layout() {
        this (null);
    }

    /**
     * Construct with a given renderer.
     * 
     * @param renderer
     *                 the renderer.
     */
    public Layout(ILayoutRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Obtains the layout target (as assigned by
     * {@link #setLayoutTarget(ILayoutTarget)}).
     * 
     * @return the target.
     */
    protected ILayoutTarget layoutTarget() {
        return layoutTarget;
    }

    /**
     * Sets the height of the layout target.
     * 
     * @param layoutTargetHeight
     *                           the height in pixels.
     */
    public void setLayoutTargetHeight(int layoutTargetHeight) {
        this.layoutTargetHeight = layoutTargetHeight;
        if ((layoutTargetHeight > 0) && isRendered ())
            CSS.HEIGHT.apply (getLayoutTarget (), Length.px (layoutTargetHeight));
    }

    /**
     * Assigns an optional CSS style name to be applied to the layout target (that
     * which is returned by {@link #getLayoutTarget()}).
     * <p>
     * In general it is preferable not to use this and let the layout apply its own
     * styles as needed.
     * 
     * @param layoutTargetStyle
     *                          the CSS class style name.
     */
    public void setLayoutTargetStyle(String layoutTargetStyle) {
        this.layoutTargetStyle = layoutTargetStyle;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.ILayout#isRunning()
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.ILayout#markAsDirty()
     */
    @Override
    public void markAsDirty() {
        this.dirty = true;
    }

    /**
     * The outcome promise.
     */
    private Promise<ActivateOutcome> promise;

    /**
     * Performs a layout while deferring (which allows for any components that have
     * not been rendered to be rendered).
     * 
     * @param defer
     *               {@code true} if to defer.
     * @param forced
     *               if the layout should be forced.
     */
    protected Promise<ActivateOutcome> performLayout(boolean defer) {
        if (running) {
            // A null promise should not happen as the promise is created before the setting
            // of the running state and the running state is cleared before the promise is
            // fulfilled. However if there is no promise then we set one.
            Logger.warn ("Activating layout while running: " + Layout.this);
            if (promise == null)
                promise = Promise.create ();
            return promise;
        }
        if (DebugMode.FLOW.set ())
            Logger.trace ("[Layout]", "performLayout(" + defer + ") running = true " + Layout.this);
        running = true;
        if (defer) {
            promise = Promise.create ();
            if (DebugMode.FLOW.set ())
                Logger.trace ("[Layout]", "scheduled(" + defer + ") " + Layout.this);
            //TimerSupport.timer (() -> _performLayout(), 1);
            TimerSupport.defer (() -> _performLayout());
            return promise;
        }
        _performLayout();
        return Promise.create (ActivateOutcome.ACTIVATED);
    }


    /**
     * Called by {@link #performLayout(boolean)}.
     */
    private void _performLayout() {
        try {
            if (DebugMode.FLOW.set ())
                Logger.trace("[Layout]", " _performLayout() " + Layout.this);
            if (layoutTarget != null)
                _layout (false);
        } finally {
            if (promise != null)
                promise.fulfill (ActivateOutcome.ACTIVATED);
            promise = null;
        }
    }

    /**
     * Executes the layout on its associated layout target (i.e. container). The
     * layout will be flagged as running ({@link #isRunning()} will return
     * {@code true}) and will call {@link #onLayout(Elem)} passing the element
     * returned by {@link ILayoutTarget#getLayoutTarget()} (so long as the layout
     * target has been rendered). It is this method that is responsible for
     * rendering and positioning the children the layout or resize all the children.
     * Once complete the children will have their layout methods invoked if they are
     * containers.
     * <p>
     * In general, this is called directly and should not be overridden. Consider
     * overriding {@link #onLayout(Elem)}, {@link #renderAll()},
     * {@link #getLayoutTarget()} or
     * {@link #renderComponent(Component, int, Elem, int)} instead.
     * 
     * @param force
     *              {@code true} if the layout should be forced (otherwise the
     *              layout will only be performed if the layout has been marked as
     *              dirty).
     * @return {@code true} if the layout was performed (may not be performed if
     *         there is no container set, the container is not rendered or the
     *         layout is already running on the container).
     */
    public boolean layout(boolean force) {
        if (isRunning ())
            return false;
        return _layout (force);
    }

    /**
     * Called internally to process a layout.
     */
    protected boolean _layout(boolean force) {
        if (force)
            this.markAsDirty ();

        // Ensure that the layout is in a state to execute.
        if (!this.dirty || !isRendered ())
            return false;

        // Start the layout.
        if (DebugMode.FLOW.set ())
            Logger.trace ("[Layout]", "_lauout(" + force + ") running = true " + this.toString());
        running = true;

        // Apply any optional style.
        if (layoutTargetStyle != null)
            getLayoutTarget ().classList.add (layoutTargetStyle);

        // Perform the actual layout.
        onLayout (layoutTarget.getLayoutTarget ());
        layoutTarget.onLayoutExecuted ();

        // Layout each of the children.
        for (IComponent c : layoutTarget.getItems ())
            c.reconfigure ();

        // Stop the layout.
        onAfterLayout (layoutTarget);
        layoutTarget.onLayoutComplete (firstLayout);
        if (DebugMode.FLOW.set ())
            Logger.trace ("[Layout]", "_layout(" + force + ") running = false " + this.toString());
        running = false;
        firstLayout = false;
        dirty = false;

        return true;
    }

    /**
     * Determines if the layout target has been rendered and is ready for a layout.
     * 
     * @return {@code true} if it has.
     */
    protected boolean isRendered() {
        return ((layoutTarget != null) && layoutTarget.isRendered ());
    }

    /**
     * Gets the layout target for the container. This is the element that should be
     * used to render the children of the container to. In general this will be the
     * same as {@link ILayoutTarget#getLayoutTarget()} but a layout may override
     * this (for example, if it wants to decorate the layout).
     * 
     * @return The layout target (will be {@code null} if the container is
     *         {@code null} or has not been rendered).
     */
    protected Element getLayoutTarget() {
        if (!isRendered ())
            return null;
        return layoutTarget.getLayoutTarget ();
    }

    /**
     * Gets the items that are subject to layout.
     * 
     * @return The items in order.
     */
    protected List<IComponent> getItems() {
        return layoutTarget.getItems ();
    }

    /**
     * Gets the number of items that are subject to layout.
     * 
     * @return The number of items.
     */
    protected int getNumberItems() {
        return layoutTarget.getItems ().size ();
    }

    /**
     * This is called by {@link #layout(boolean)} after it ensures that the layout
     * target has been rendered and that the layout is not already running.
     * 
     * @param target
     *               the target element from the layout target (in general this will
     *               be the same as that returned by {@link #getLayoutTarget()} but
     *               the latter may return a different location).
     */
    protected void onLayout(Element target) {
        renderAll ();
    }

    /**
     * Called after the layout of the children (and calling layout for each child)
     * has been done.
     * 
     * @param container
     *                  the container to which the layout is being applied.
     */
    protected void onAfterLayout(ILayoutTarget container) {
        // Nothing.
    }

    /**
     * Render all the components in a container to the target element. This will
     * traverse the list of children of the container and render any of the children
     * that have not yet been rendered or have been rendered but their current
     * attachment is not valid (determined by
     * {@link #isValidParent(Element, Element)}) by calling
     * {@link #renderComponent(Component, int, Elem, int)}. After the components
     * have been rendered {@link #afterRender()} is called}.
     * <p>
     * For simple layouts its sufficient to override
     * {@link #renderComponent(Component, int, Elem, int)}. For more complicated
     * layout one can either override this method completely or re-shuffle the
     * rendered components by overriding {@link #afterRender()}.
     * 
     * @param container
     *                  the container to render.
     * @param target
     *                  the target element to render to.
     */
    protected void renderAll() {
        int idx = 0;
        int size = getNumberItems ();
        Element target = getLayoutTarget ();
        if (layoutTargetHeight > 0)
            CSS.HEIGHT.apply (target, Length.px (layoutTargetHeight));
        for (IComponent cpt : getItems ()) {
            if ((cpt.getRoot () == null) || !isValidParent (cpt.getRoot (), target)) {
                try {
                    renderComponent (cpt, idx, target, size);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            }
            idx++;
        }
        afterRender (target);
    }

    /**
     * Called after the components have been rendered.
     * 
     * @param target
     *               the element that the children were rendered into.
     */
    protected void afterRender(Element target) {
        // Nothing.
    }

    /**
     * Used to determine if a component needs to be relocated into a container.
     * Passed is the (root) element of the child component and the container element
     * (that will contain the child). The default behaviour is to check is the child
     * element is a descendant of the parent.
     * 
     * @param child
     *               the child element to check.
     * @param parent
     *               the parent element (layout target).
     * @return If the child is correctly located.
     */
    protected boolean isValidParent(Element child, Element parent) {
        return (parent != null) && DomSupport.isChildOf (child, parent);
    }

    /**
     * Renders or moves a component to the given index in the target (if the
     * component has been rendered then one may safely assume that the component is
     * not located in the region being processed as this check will have already
     * been performed).
     * 
     * @param component
     *                  the component to render.
     * @param index
     *                  the index of the component as the child of the container.
     * @param target
     *                  the target element to render the component into.
     * @param size
     *                  the total number of items.
     */
    protected void renderComponent(IComponent component, int index, Element target, int size) {
        Object layoutData = component.getLayoutData ();
        IComponentLayout componentLayout;
        if (renderer != null)
            componentLayout = renderer.renderComponent (component, index, target, size);
        else
            componentLayout = renderComponentLayout (component, index, target, size);
        layouts.add (componentLayout);

        // Apply any extra styles.
        if (layoutTargetStyle != null)
            componentLayout.getLayoutEl ().classList.add (layoutTargetStyle);
        if (layoutData instanceof LayoutData) {
            if (!StringSupport.empty (((LayoutData) layoutData).getExtraStyle ()))
                componentLayout.getLayoutEl ().classList.add (((LayoutData) layoutData).getExtraStyle ());
        }
    }

    /**
     * This is effectively a default implementation of {@link ILayoutRenderer} and
     * is only called if not renderer has been provided.
     * <p>
     * It will render the component directly into the target element at the given
     * index (by calling {@link IComponent#render(Element, int)}) if the component
     * has not already been rendered. If it has been rendered then it will move the
     * component to the given index in the target.
     * 
     * @param component
     *                  the component to render.
     * @param index
     *                  the index of the component as the child of the container.
     * @param target
     *                  the target element to render the component into.
     * @param size
     *                  the total number of items.
     * @return The inserted element.
     */
    protected IComponentLayout renderComponentLayout(IComponent component, int index, Element target, int size) {
        return DefaultLayoutRenderer.DEFAULT.renderComponent (component, index, target, size);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.ILayout#setLayoutTarget(com.effacy.jui.core.client.container.layout.ILayoutTarget)
     */
    @Override
    public void setLayoutTarget(ILayoutTarget layoutTarget) {
        if (this.layoutTarget == layoutTarget)
            return;

        // Detach the currently assigned container.
        if (this.layoutTarget != null) {
            this.layoutTarget.removeListener (containerListener);
            if (this.layoutTarget.isRendered () && (this.layoutTargetStyle != null) && (this.layoutTarget.getLayoutTarget () != null))
                this.layoutTarget.getLayoutTarget ().classList.add (this.layoutTargetStyle);
            for (IComponent c : this.layoutTarget.getItems ())
                onComponentRemove (c);
        }

        // Attach the new container.
        this.layoutTarget = layoutTarget;
        if (layoutTarget != null) {
            layoutTarget.addListener (containerListener);
            for (IComponent c : this.layoutTarget.getItems ())
                onComponentAdd (c);
        }

        // Notify that the layout target has been set.
        onLayoutTargetSet (layoutTarget);
    }

    /**
     * Called whenever the layout target has been set.
     * 
     * @param layoutTarget
     *                     the new layout target.
     */
    protected void onLayoutTargetSet(ILayoutTarget layoutTarget) {
        // Nothing.
    }

    /**
     * Gets the layout data for a component.
     * 
     * @param c
     *          the component.
     * @return The layout data.
     */
    @SuppressWarnings("unchecked")
    protected <L extends LayoutData> L getLayoutData(IComponent c) {
        return (L) c.getLayoutData ();
    }

    /**
     * Invoked when a component has been hidden.
     * 
     * @param component
     *                  the component that has been hidden.
     */
    protected void onComponentHide(IComponent component) {
        markAsDirty ();
    }

    /**
     * Invoked when a component has been shown.
     * 
     * @param component
     *                  the component that has been shown.
     */
    protected void onComponentShow(IComponent component) {
        markAsDirty ();
    }

    /**
     * Invoked when a component has requested activation. By default this does
     * nothing.
     * 
     * @param component
     *                  the component that has been activated.
     */
    protected void onComponentActivate(IComponent component) {
        // Nothing.
    }

    /**
     * Adds a component to the layout.
     * 
     * @param component
     *                  the component.
     */
    protected void onComponentAdd(IComponent component) {
        if ((layoutTargetStyle != null) && (component.getRoot () != null))
            component.getRoot ().classList.add (layoutTargetStyle);
        component.addListener (componentListener);
        markAsDirty ();
    }

    /**
     * Invoked when a component has been removed from a container.
     * 
     * @param component
     *                  the component that has been removed.
     */
    protected void onComponentRemove(IComponent component) {
        if ((layoutTargetStyle != null) && (component.getRoot () != null))
            component.getRoot ().classList.remove (layoutTargetStyle);
        component.removeListener (componentListener);
        Element layoutTarget = getLayoutTarget ();
        if (layoutTarget != null)
            removeChild (component, layoutTarget);
        markAsDirty ();
    }

    /**
     * Invoked when the container resizes.
     */
    protected void onContainerResize() {
        markAsDirty ();
    }

    /**
     * Executes a removal of the component from the container. Typically this will
     * be called from {@link #onComponentRemove(Component)} when a component is
     * actually removed. Sub-classes may override as this simply removes the child
     * that contains the component from the layout.
     * 
     * @param component
     *                  the component to be removed.
     * @param target
     *                  the target element to remove the child from.
     */
    protected void removeChild(IComponent component, Element target) {
        if (target != null) {
            NodeList<Node> children = target.childNodes;
            Element componentEl = component.getRoot ();
            LOOP1: for (int i = 0, len = children.getLength (); i < len; i++) {
                Element child = Js.cast (children.getAt (i));
                if (DomSupport.isChildOf (componentEl, child)) {
                    target.removeChild (child);
                    LOOP2: for (IComponentLayout componentLayout : layouts) {
                        if (DomSupport.isChildOf (componentLayout.getLayoutEl (), child)) {
                            layouts.remove (componentLayout);
                            break LOOP2;
                        }
                    }
                    break LOOP1;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.layout.ILayout#onBrowserEvent(elemental2.dom.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {
        if (UIEventType.ONMOUSEOVER.matches (event))
            return;
        if (UIEventType.ONMOUSEOUT.matches (event))
            return;
        Element target = Js.cast (event.target);
        for (IComponentLayout componentLayout : layouts) {
            if (DomSupport.isChildOf (target, componentLayout.getLayoutEl ())) {
                componentLayout.onBrowserEvent (event);
                return;
            }
        }
    }

    /**
     * Container listener that conveys container events to the layout.
     */
    class LocalContainerListener implements IAddRemoveListener, IResizeListener {

        @Override
        public void onAdd(IComponent source, IComponent added) {
            Layout.this.onComponentAdd (added);
        }

        @Override
        public void onRemove(IComponent source, IComponent removed) {
            onComponentRemove (removed);
        }

        @Override
        public void onResize(IComponent source) {
            onContainerResize ();
        }

    }

    /**
     * Component listener that conveys component events to the layout.
     */
    class LocalComponentListener implements IShowHideListener, IActivateListener {

        @Override
        public void onShow(IComponent source) {
            onComponentShow (source);
        }

        @Override
        public void onHide(IComponent source) {
            onComponentHide (source);
        }

        @Override
        public void onActivate(IComponent source) {
            onComponentActivate (source);
        }

    }

}
