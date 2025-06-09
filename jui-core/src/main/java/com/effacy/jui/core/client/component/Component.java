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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.ICompletionCallback;
import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.IComponent.IParent;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.IContainerRegion;
import com.effacy.jui.core.client.component.layout.ILayout;
import com.effacy.jui.core.client.component.layout.ILayoutFactory;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.component.layout.MinimalLayout;
import com.effacy.jui.core.client.control.ControlContext;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IModifiedListener;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.EventLifecycle;
import com.effacy.jui.core.client.dom.EventLifecycle.IEventListener;
import com.effacy.jui.core.client.dom.EventLifecycle.IEventRegistration;
import com.effacy.jui.core.client.dom.IDomSelectable;
import com.effacy.jui.core.client.dom.IDomSelector;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Fragment.IFragmentAdornment;
import com.effacy.jui.core.client.dom.builder.NodeBuilder;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.NodeContext;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.IRenderer;
import com.effacy.jui.core.client.dom.renderer.template.ITemplateBuilder;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container;
import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.core.client.observable.IObservable;
import com.effacy.jui.core.client.observable.Observable;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.TimerSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.Node;
import jsinterop.base.Js;

/**
 * Abstract support class for building generic components.
 * <p>
 * <h3>Focus and blur</h3>
 * <p>
 * Focus and blur can come about either programmatically (see {@link #focus()}
 * and {@link #blur()}) or via UI events (see {@link #onFocusUI()} and
 * {@link #onBlurUI()}). For the latter this is invoked (at the very least) when
 * the component receives focus and blur UI events that are not handled
 * elsewhere (and if they are handled elsewhere then that elsewhere should
 * invoke {@link #onFocusUI()} or {@link #onBlurUI()} as necessary).
 * <p>
 * Regardless of how the change in focus state is received nothing will be done
 * if the component is already in the target state. If there is a change then
 * the component will first be marked as being the in the desired state then a
 * call will be made to {@link #renderFocus()} or {@link #renderBlur()} followed
 * by {@link #onFocus()} or {@link #onBlur()} (as applicable).
 * <p>
 * A note of caution is where a component has multiple input controls (rather,
 * DOM element or the like that generate their own focus and blur events). In
 * this case just switching between controls could result in blur-focus pairs
 * from the component which may not be desirable. Implementations should
 * consider deferring blur events allow them to be cancelled should a focus
 * event come along shortly after.
 * 
 * @author Jeremy Buckley
 */
public class Component<C extends Component.Config> implements IEventListener, IComponent, IBindable, IParent, IConfigurable<C> {

    /**
     * Common configuration for all components. This encapsulate the common data and
     * behaviour that all components do, or are expected to, support.
     */
    public static class Config {

        /**
         * See {@link #testId(String)}.
         */
        private String testId;

        /**
         * See {@link #adorn(IFragmentAdornment...)}.
         */
        private Consumer<Element> adorn;

        /*
         * See {@link #styles(String...)}
         */
        private List<String> styles;

        /**
         * Assigns a test ID to this component.
         * 
         * @param testId
         *               the test ID.
         * @return the test ID.
         */
        @SuppressWarnings("unchecked")
        public <C extends Config> C testId(String testId) {
            this.testId = testId;
            return (C) this;
        }

        /**
         * Obtains the test ID.
         * 
         * @return the test ID.
         */
        public String getTestId() {
            if (this.testId == null)
                return this.testId = "";
            return this.testId;
        }

        /**
         * Convenience to test if there is a test ID.
         * 
         * @return {@code true} if there is.
         */
        public boolean hasTestId() {
            return !StringSupport.empty (getTestId ());
        }

        /**
         * Enables access to the root element to apply any adornments (styles, sizes,
         * etc) that fall outside of the configuration. This is called only once during
         * renderering (on post render).
         * <p>
         * Use of this is generally discouraged with preference to limit configuration
         * to that available through configuration. However, there are always exception
         * cases.
         * 
         * @param adorn
         *              used to apply the adornment.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public <C extends Config> C adorn(Consumer<Element> adorn) {
            if (this.adorn == null) {
                this.adorn = adorn;
            } else if (adorn != null) {
                Consumer<Element> chain = this.adorn;
                this.adorn = el -> {
                    chain.accept (el);
                    adorn.accept (el);
                };
            }
            return (C) this;
        }

        /**
         * This is a convenience to apply CSS under adornment. See
         * {@link ElementBuilder#css(String)} for details.
         * 
         * @param css
         *            the CSS string to apply.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public <C extends Config> C css(String css) {
            if (!StringSupport.empty(css)) {
                adorn (el -> {
                    JQueryElement jel = JQuery.$ (el);
                    for (String part : css.split(";")) {
                        int idx = part.indexOf (':');
                        if (idx > 0)
                            jel.css (part.substring (0, idx).trim (), part.substring (idx + 1).trim ());
                    }
                });
            }
            return (C) this;
        }

        /**
         * Adds the passed styles to the root element of the component.
         * <p>
         * This is additive.
         * 
         * @param styles
         *               the styles to add.
         * @return this configuration instance.
         */
        @SuppressWarnings("unchecked")
        public <C extends Config> C styles(String... styles) {
            if (this.styles == null)
                this.styles = new ArrayList<>();
            for (String style : styles) {
                if (style != null)
                    this.styles.add (style);
            }
            return (C) this;
        }

        /**
         * Convenience to build a component directly from this configuration.
         * 
         * @return the component (default is to return {@code null}).
         */
        public <P extends IComponent> P build(LayoutData... data) {
            return build (null, data);
        }

        /**
         * Used by {@link #build(LayoutData...)} (see internal implementation for an
         * example) to assign layout data should it be present.
         */
        protected <P extends IComponent> P build(P cpt, LayoutData... data) {
            if (cpt == null)
                return null;
            if ((data != null) && (data.length > 0) && (data[0] != null))
                cpt.setLayoutData (data[0]);
            return cpt;
        }
    }

    /**
     * UUID for components.
     */
    private static Long UUID = 0L;

    /**
     * Component UUID.
     */
    protected long uuid;

    /**
     * Manager for controls.
     */
    private ControlContext controls;

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
        RENDER(1<<1),

        /**
         * Log lifecycle events.
         */
        LIFECYCLE(1<<2),

        /**
         * Place names of components in the DOM.
         */
        NAME(1<<3),

        /**
         * Apply visual outlines of components.
         */
        OUTLINE(1<<4);

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
            return ((Component.DEBUG & flag) > 0);
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
     * Metadata
     ************************************************************************/

    /**
     * See {@link #putMetaAttribute(String, String)} and
     * {@link #getMetaAttribute(String)}.
     */
    private Map<String,String> metadata;

    @Override
    public void putMetaAttribute(String name, String value) {
        if (metadata == null)
            metadata = new HashMap<>();
        metadata.put (name, value);
    }

    @Override
    public String getMetaAttribute(String name) {
        if (metadata == null)
            return null;
        return metadata.get (name);
    }

    /************************************************************************
     * Rendering state.
     ************************************************************************/

    /**
     * Rendering pending.
     */
    private static final int RENDER_PENDING = 0;

    /**
     * Rendering pending.
     */
    private static final int RENDER_STARTED = 1;

    /**
     * Rendering has darwn the component.
     */
    private static final int RENDER_DRAWN = 2;

    /**
     * Rendering has configured the component.
     */
    private static final int RENDER_CONFIGURED = 3;

    /**
     * If the component has been rendered.
     */
    private int rendered = RENDER_PENDING;

    /************************************************************************
     * Observables
     ************************************************************************/

    /**
     * Event bus for events that are emitted by this component.
     */
    private Observable outboundBus = new Observable ();

    /**
     * Event bus for events that are intended to be propagated through to children
     * of the component.
     */
    private Observable inboundBus;

    /************************************************************************
     * General state.
     ************************************************************************/

    /**
     * If the component is hidden or not.
     */
    private boolean hidden;

    /**
     * If the component is disabled or not.
     */
    private boolean disabled;

    /**
     * If the component is currently masked.
     */
    protected boolean masked;

    /**
     * The component root element.
     */
    private Element rootEl;

    /**
     * Unique component ID.
     */
    private String id;

    /**
     * Disables right clicking.
     */
    protected boolean disableRightClick;

    /**
     * If window resizing should be monitored by the component.
     */
    private boolean monitorWindowResize;

    /**
     * Resize handler for the monitoring of window resizing.
     */
    private IEventRegistration windowResizeHandler;

    /**
     * If window scrolling should be monitored.
     */
    private boolean monitorWindowScroll;

    /**
     * Scroll handler for the monitoring of window scrolling.
     */
    private IEventRegistration windowScrollHandler;

    /**
     * To be invoked on render.
     */
    private List<BiConsumer<IComponent, Element>> onRenderHandlers;

    /**
     * Width to apply to component on render. The default is {@link Length#NONE} (no
     * width).
     */
    protected Length preRenderWidth = null;

    /**
     * Height to apply to component on render. The default is {@link Length#NONE}
     * (no height).
     */
    protected Length preRenderHeight = null;

    /**
     * Hide when set pre-render.
     */
    protected boolean preRenderHide = false;

    // /**
    //  * The assigned width of the component.
    //  */
    // private Length assignedWidth = null;

    // /**
    //  * The assigned height of the component.
    //  */
    // private Length assignedHeight = null;

    /**
     * The last recorded height of the component.
     */
    protected int lastActualWidth = -1;

    /**
     * The last recorded width of the component.
     */
    protected int lastActualHeight = -1;

    /**
     * To prevent the reconfigure method being re-entrant.
     */
    private boolean reconfiguring = false;

    /**
     * The deferred reconfigure.
     */
    private DeferredReconfigure deferredReconfigure = null;

    /**
     * Configuration for the component.
     */
    private C config;

    /**
     * Default layout factory.
     */
    protected ILayoutFactory regionDefaultLayout = CardFitLayout.FACTORY;

    /**
     * Flag to determine if a layout needs to be performed when components are added
     * or removed from the container.
     */
    protected boolean layoutOnChange = true;

    /**
     * Listens to events from the component.
     */
    private IResizeListener componentListener;

    /**
     * List of all UI handlers for the component.
     */
    protected List<UIEventHandlerWrapper> uiHandlers;

    /**
     * Stack for storing enable state.
     */
    private Stack<Boolean> enableStateStack;

    /**
     * To dispatch events when the component is disabled.
     */
    protected boolean dispatchWhenDisabled = false;

    /**
     * The name of the cache key to the renderer. This is nominally derived but can
     * be set outright with {@link #buildContainerCacheKey(String, boolean)}.
     */
    protected String cacheKey;

    /**
     * The renderer which is passed during construction or is build by overriding
     * {@link #buildContainer(Container)} (or by passing a builder during
     * construction). This will be caching so that multiple instances of the
     * component will not incur multiple renderer templates.
     */
    protected IDataRenderer<C> renderer;

    /**
     * Resolved test ID.
     */
    private String testId;

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * Construct an instance without a renderer.
     */
    protected Component() {
        super ();
        uuid = UUID++;
    }

    /**
     * Construct with configuration.
     * 
     * @param config
     *               the configuration.
     */
    protected Component(C config) {
        super ();
        this.config = config;
        uuid = UUID++;
    }

    /**
     * Construct around a renderer. If the renderer implements
     * {@link IUIEventHandler} it will be registered as a handler.
     * 
     * @param config
     *                 the component configuration.
     * @param renderer
     *                 the renderer to use.
     */
    public Component(C config, IDataRenderer<C> renderer) {
        this (config);
        this.renderer = renderer;
        if (renderer instanceof IUIEventHandler)
            registerEventHandler ((IUIEventHandler) renderer);
    }

    /************************************************************************
     * Component events.
     ************************************************************************/

    /**
     * Represents a component event.
     */
    public static class ComponentEvent {

        /**
         * Some standard event types.
         */
        public enum Standard {
            /**
             * Parent has been shown.
             */
            SHOW,

            /**
             * Parent has been hidden.
             */
            HIDE;
        }

        /**
         * See {@link #type()}.
         */
        private Object type;

        /**
         * See {@link #source()}.
         */
        private IComponent source;

        
        private int depth;

        /**
         * Construct with tyep and the source.
         * 
         * @param type
         *               the type.
         * @param source
         *               the source.
         */
        public ComponentEvent(Object type, IComponent source) {
            this.type = type;
            this.source = source;
            this.depth = 0;
        }

        /**
         * The compent that issued the event.
         * 
         * @return the component.
         */
        public IComponent source() {
            return source;
        }
        

        /**
         * Determines if this event is of the passed type.
         * 
         * @param type
         *             the type to check against.
         * @return {@code true} if they are the same.
         */
        public boolean is(Object type) {
            if (this.type == type)
                return true;
            if (this.type == null)
                return false;
            return this.type.equals (type);
        }

        /**
         * The type of event.
         * 
         * @return the type.
         */
        public Object type() {
            return type;
        }

        /**
         * The depth of the event.
         * 
         * @return the depth (1 means directly below).
         */
        public int depth() {
            return depth;
        }
    }

    /**
     * Fires a component event.
     * 
     * @param type
     *             the type of event.
     */
    protected void fireComponentEvent(Object type) {
        _fireComponentEvent (new ComponentEvent (type, this));
    }

    /**
     * Called by {@link #fireComponentEvent(Object)}.
     * 
     * @param event
     *              the event.
     */
    protected void _fireComponentEvent(ComponentEvent event) {
        if (event == null)
            return;
        int depth = event.depth;
        forEach (cpt -> {
            event.depth = depth + 1;
            if (cpt instanceof Component) {
                try {
                    if (((Component<?>) cpt).onComponentEvent (event))
                        ((Component<?>) cpt)._fireComponentEvent (event);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e, this);
                }
            }
            event.depth = depth;
        });
    }

    /**
     * Receives component events.
     * 
     * @param event
     *              the event.
     * @return {@code true} if to cascade to child components (default is
     *         {@code false}).
     */
    protected boolean onComponentEvent(ComponentEvent event) {
        return false;
    }

    /************************************************************************
     * General properties.
     ************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#getUUID()
     */
    @Override
    public long getUUID() {
        return uuid;
    }

    
    /**
     * The test ID is formed by composing the test ID of the parent with the
     * test ID part of this component separated by a period.
     * <p>
     * The test ID part of this component is derived initially from the test
     * ID set against the configuration (see {@link Config#testId(String)}). If
     * this is not set (i.e. is {@code null} or empty) then the lower-case
     * version of the class name is used.
     * 
     * @see com.effacy.jui.core.client.component.IComponent.IParent#getTestId()
     */
    @Override
    public String getTestId() {
        if (this.testId == null) {
            String localTestId = ((this.parent != null) && (this.parent.getTestId () != null)) ? this.parent.getTestId () : "";
            String cptTestId = ((config () != null) && config().hasTestId ()) ? config().getTestId () : getClass().getSimpleName().toLowerCase();
            if (!localTestId.isEmpty())
                localTestId += ".";
            localTestId += cptTestId;
            if (this.parent == null)
                return localTestId;
            this.testId = localTestId;
        }
        return this.testId;
    }


    /**
     * The ID of the component type to be placed in the {@code test-cpt} field (the
     * default is the simple name of the class).
     * 
     * @return the component ID.
     */
    protected String getComponentId() {
        String id = getClass ().getName ().toLowerCase ().replace ("$", "_");
        int i = id.lastIndexOf ('.');
        if (i > 0)
            id = id.substring(i + 1);
        return id;
    }

    /**
     * Builds a test ID based on the ID of the component extended by the
     * given extension. The extension will be separated from the component
     * test ID by a dash (of the component has one).
     * 
     * @param ext the extension.
     * @return the extended text ID.
     */
    protected String buildTestId(String ext) {
        if (StringSupport.empty (ext))
            return getTestId ();
        return StringSupport.empty (getTestId()) ? ext : getTestId () + "-" + ext;
    }

    /**
     * Start debugging events.
     */
    protected void debugEvents() {
        outboundBus.debugObservable (this.getClass ());
    }

    /**
     * Wraps an {@link IUIEventHandler} with the ability to have it replaced when
     * re-assigned (determined by a key).
     */
    class UIEventHandlerWrapper implements IUIEventHandler {

        /**
         * Key to match against for the purpose of replacement.
         */
        private String replacementKey;

        /**
         * The handler being wrapped.
         */
        private IUIEventHandler handler;

        /**
         * Construct instance of the wrapper.
         * 
         * @param handler
         *                       the handler being wrapped.
         * @param replacementKey
         *                       (optional) the replacement key (if {@code null} then no
         *                       replacement is performed).
         */
        public UIEventHandlerWrapper(IUIEventHandler handler, String replacementKey) {
            this.handler = handler;
            this.replacementKey = replacementKey;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
         */
        @Override
        public boolean handleEvent(UIEvent event) {
            return handler.handleEvent (event);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        @SuppressWarnings("rawtypes")
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (obj instanceof Component.UIEventHandlerWrapper)
                return handler.equals (((Component.UIEventHandlerWrapper) obj).handler);
            if (obj instanceof IUIEventHandler)
                return handler.equals (((IUIEventHandler) obj));
            return super.equals (obj);
        }

        /**
         * Disposes of the handler if it implements {@link IDisposable}.
         */
        void dispose() {
            if (handler instanceof IDisposable)
                ((IDisposable) handler).dispose ();
        }

        /**
         * Matches this wrapper with the passed replacement key.
         * 
         * @param replacementKey
         *                       the matching key.
         * @return {@code true} if the keys are defined and match.
         */
        boolean match(String replacementKey) {
            if (replacementKey == null)
                return false;
            if (this.replacementKey == null)
                return false;
            return this.replacementKey.equals (replacementKey);
        }

        /**
         * Performs a DOM selection post render. If the handler implements
         * {@link ISelectableUIEventHandler} it will be selected against the root
         * element. If it implements {@link IDomSelectable} it will be selected against
         * the passed selector (if present).
         * <p>
         * This allows the handler to configure itself post render (i.e. to wire up
         * event handlers an the like).
         * 
         * @param selector
         *                 the selector from the rendering process.
         */
        void select(IDomSelector selector) {
            if (handler instanceof ISelectableUIEventHandler)
                ((ISelectableUIEventHandler) handler).select (rootEl);
            if ((selector != null) && (handler instanceof IDomSelectable))
                ((IDomSelectable) handler).select (selector);
        }

    }

    /**
     * See {@link #registerEventHandler(IUIEventHandler,String,boolean) but with no
     * priority.
     */
    public <H extends IUIEventHandler> H registerEventHandler(H handler, String replacementKey) {
        return registerEventHandler (handler, replacementKey, false);
    }

    /**
     * Registers a UI handler with an optional key that will result in any existing
     * handler that matches the key (i.e. having previously been registered with
     * that key) to be removed of first.
     * <p>
     * If the handler implements {@link ISelectableUIEventHandler} then post render
     * it will be selected against the root element. If it implements
     * {@link IDomSelectable} and the rendering returns a {@link IDomSelector} then
     * it will be selected against that selector. Both of these allow the handler to
     * sink events (or extract nodes) as required and decouples it from the
     * rendering mechanism.
     * <p>
     * If the handler implements {@link IDisposable} then it will be disposed of
     * when removed or when the component itself is disposed.
     * <p>
     * Note that should any of these handlers handle a UI event then the event will
     * be stopped (see {@link UIEvent#stopEvent()}).
     * 
     * @param handler
     *                       the handler to register.
     * @param replacementKey
     *                       associated the handler with a key so that any existing
     *                       handler with the same key will be replaced.
     * @param priority
     *                       {@code true} if the handler shoud be placed at the head
     *                       of the queue of handlers (meaning that it is processed
     *                       first).
     * @return the passed handler.
     */
    public <H extends IUIEventHandler> H registerEventHandler(H handler, String replacementKey, boolean priority) {
        if (handler == null)
            return handler;
        if (uiHandlers == null)
            uiHandlers = new ArrayList<> ();
        if (!uiHandlers.contains (handler)) {
            if (replacementKey != null) {
                uiHandlers.removeIf (h -> {
                    if (h.match (replacementKey)) {
                        h.dispose ();
                        return true;
                    }
                    return false;
                });
            }
            if (priority)
                uiHandlers.add (0, new UIEventHandlerWrapper (handler, replacementKey));
            else
                uiHandlers.add (new UIEventHandlerWrapper (handler, replacementKey));
        }
        return handler;
    }

    /**
     * See {@link #registerEventHandler(IUIEventHandler, String)} but without a
     * replacement key.
     */
    public <H extends IUIEventHandler> H registerEventHandler(H handler) {
        return registerEventHandler (handler, null);
    }

    /**
     * Removes a UI handler.
     * <p>
     * If the handler implements {@link IDisposable} then it will be disposed of.
     * 
     * @param handler
     *                the handler to remove.
     * @return the removed handler.
     */
    public <H extends IUIEventHandler> H removeEventHandler(H handler) {
        if ((handler == null) || (uiHandlers == null))
            return handler;
        uiHandlers.removeIf (h -> {
            if (h.equals (handler)) {
                h.dispose ();
                return true;
            }
            return false;
        });
        return handler;
    }

    /**
     * Sets the configuration.
     * 
     * @param config
     *               the configuration.
     */
    protected void setConfig(C config) {
        this.config = config;
    }

    /**
     * Gets the configuration for the component (this will have been set during
     * construction, otherwise the sub-class should override this method to provide
     * a configuration).
     * 
     * @return The assigned configuration (may be {@code null}).
     */
    @Override
    public C config() {
        return config;
    }

    /**
     * Sets the state of window resize monitoring. If monitoring then
     * {@link #onWindowResize(int, int)} will be called each time the window changes
     * in size.
     * 
     * @param monitor
     *                {@code true} if changes in the window size should be monitored
     *                for.
     */
    public void setMonitorWindowResize(boolean monitor) {
        this.monitorWindowResize = monitor;
        if (monitor) {
            if (isAttached () && (windowResizeHandler == null)) {
                windowResizeHandler = EventLifecycle.registerWindowResizeEvent (e -> deferOnWindowResize ());
                deferOnWindowResize ();
            }
        } else if (windowResizeHandler != null) {
            windowResizeHandler.remove ();
            windowResizeHandler = null;
        }
    }

    public void setMonitorWindowScroll(boolean monitor) {
        this.monitorWindowScroll = monitor;
        if (monitor) {
            if (isAttached () && (windowScrollHandler == null)) {
                windowScrollHandler = EventLifecycle.registerDocumentScrollEvent (e -> deferOnWindowScroll ());
                deferOnWindowScroll ();
            }
        } else if (windowScrollHandler != null) {
            windowScrollHandler.remove ();
            windowScrollHandler = null;
        }
    }

    /**
     * Determines if the component is listening to window resizes.
     * 
     * @return {@code true} if it is.
     */
    protected boolean isMonitorWindowResize() {
        return this.monitorWindowResize;
    }

    /**
     * Performs a deferred call to {@link #onWindowResize(int, int)}.
     */
    private void deferOnWindowResize() {
        TimerSupport.defer (() -> onWindowResize (DomGlobal.document.documentElement.clientWidth, DomGlobal.document.documentElement.clientHeight));
    }

    /**
     * Determines if the component is listening to window scrolling.
     * 
     * @return {@code true} if it is.
     */
    protected boolean isMonitorWindowScroll() {
        return this.monitorWindowScroll;
    }

    /**
     * Performs a deferred call to {@link #onWindowResize(int, int)}.
     */
    private void deferOnWindowScroll() {
        TimerSupport.defer (() -> onWindowScroll (DomGlobal.document.documentElement.clientWidth, DomGlobal.document.documentElement.clientHeight, (int) DomGlobal.document.documentElement.scrollLeft, (int) DomGlobal.document.documentElement.scrollTop));
    }

    /**
     * Gets the ID of the component (which matches the ID of the root element of the
     * component).
     * 
     * @return The component ID.
     */
    public String getId() {
        if (id == null)
            id = UID.createUID ();
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#setId(java.lang.String)
     */
    public void setId(String id) {
        this.id = id;
        if (rootEl != null)
            rootEl.id = id;
    }

    /**
     * Gets the root element of the component.
     * 
     * @return the root element.
     */
    public Element getRoot() {
        return rootEl;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends IComponent> T configureOnRender(BiConsumer<IComponent, Element> configurer){
        if (configurer != null) {
            if (onRenderHandlers == null)
                onRenderHandlers = new ArrayList<>();
            onRenderHandlers.add (configurer);
            if (isRendered ())
                configurer.accept (this, rootEl);
        }
        return (T) this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#isHidden()
     */
    @Override
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Determines if the component is showing which is a combination of having been
     * rendered and not hidden.
     * 
     * @return {@code true} if it is showing.
     */
    public boolean isShowing() {
        return !hidden && (rendered > 0);
    }

    /**
     * Determines if the component is disabled.
     * 
     * @return {@code true} if it is disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Determined if the component is enabled (i.e. is not disabled).
     * 
     * @return {@code true} if it is enabled.
     */
    public boolean isEnabled() {
        return !isDisabled ();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is more subtle as it uses both the standard and
     * <code>:visible</code> selector.
     */
    public boolean isVisible() {
        if (getRoot () == null)
            return false;
        return JQuery.$ (getRoot ()).is (":visible");
    }

    /**
     * Determines if the component has been rendered (simply meaning that it has a
     * root element).
     * 
     * @return {@code true} if it has.
     */
    public boolean isRendered() {
        return (rootEl != null);
    }

    /**
     * Determines if the component has been rendered and configured (meaning its
     * children have been rendered).
     * 
     * @return {@code true} if it has.
     */
    public boolean isRenderedAndConfigured() {
        return (rendered > RENDER_DRAWN) && (rootEl != null);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#mask()
     */
    @Override
    public void mask() {
        masked = true;
        if (rootEl != null)
            DomSupport.mask (rootEl);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#unmask()
     */
    @Override
    public void unmask() {
        masked = false;
        if (rootEl != null)
            DomSupport.unmask (rootEl);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#setSize(com.effacy.jui.core.client.component.Length,
     *      com.effacy.jui.core.client.component.Length)
     */
    // @Override
    // public void setSize(Length width, Length height) {
    //     // Store the values if not rendered.
    //     if (rootEl == null) {
    //         if (width != null)
    //             this.preRenderWidth = width;
    //         if (height != null)
    //             this.preRenderHeight = height;
    //         return;
    //     }

    //     // Check that this is OK.
    //     if (onSetSize (width, height))
    //         return;

    //     // Check if the sizes have changed since last time.
    //     boolean widthChanged = (width != null) && !width.equals (this.assignedWidth);
    //     boolean heightChanged = (height != null) && !height.equals (this.assignedHeight);
    //     if (!widthChanged && !heightChanged)
    //         return;

    //     // Apply the new Lengths.
    //     if (widthChanged) {
    //         assignWidth (width);
    //         this.assignedWidth = width;
    //     }
    //     if (heightChanged) {
    //         assignHeight (height);
    //         this.assignedHeight = height;
    //     }

    //     // Notify the change.
    //     reconfigure ();
    // }

    /**
     * Called when the component is having its size set.
     * 
     * @param width
     *               the width being set.
     * @param height
     *               the height being set.
     * @return {@code true} if the setting of the size should be blocked.
     */
    protected boolean onSetSize(Length width, Length height) {
        return false;
    }

    /**
     * Assigns the passed width Length to the component. The default behaviour is to
     * apply the size to the root element.
     * 
     * @param width
     *              the width.
     */
    protected void assignWidth(Length width) {
        if (rootEl != null)
            CSS.WIDTH.apply ((elemental2.dom.Element) Js.cast (rootEl), width);
    }

    /**
     * Assigns the passed height Length to the component. The default behaviour is
     * to apply the size to the root element.
     * 
     * @param height
     *               the height.
     */
    protected void assignHeight(Length height) {
        if (rootEl != null)
            CSS.HEIGHT.apply ((elemental2.dom.Element) Js.cast (rootEl), height);
    }

    /**
     * See {@link #setLayoutData(Object)}.
     */
    private Object layoutData;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#setLayoutData(java.lang.Object)
     */
    @Override
    public void setLayoutData(Object layoutData) {
        this.layoutData = layoutData;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#getLayoutData()
     */
    @Override
    public Object getLayoutData() {
        return layoutData;
    }

    /**
     * Called when dragging is enabled or disabled (a consequence of having
     * listeners added and being drag enabled).
     * <p>
     * By default this adds events to the components elements that are used to track
     * dragging.
     * 
     * @param active
     *               if the drag is active.
     */
    protected void onDragActive(boolean active) {
        if (active && (rootEl != null)) {
            UIEventType.ONMOUSEDOWN.attach (rootEl);
            UIEventType.ONMOUSEUP.attach (rootEl);
            UIEventType.ONMOUSEMOVE.attach (rootEl);
        }
    }

    /**
     * Assigns a renderer.
     * 
     * @param renderer the renderer.
     */
    protected void renderer(IDataRenderer<C> renderer) {
        this.renderer = renderer;
    }

    /**
     * Assigns a renderer.
     * 
     * @param renderer the renderer.
     */
    protected void renderer(IRenderer renderer) {
        renderer (IDataRenderer.convert (renderer));
    }

    /**
     * Any component styles to apply. Components may override this to provide styles
     * (otherwise it returns {@code null}).
     * <p>
     * Used by {@link #render(Element, int)} to assign the component root style.
     * 
     * @return the styles to apply.
     */
    protected IComponentCSS styles() {
        return null;
    }

    /**
     * Renders the element. Typically, this method does not need to be called
     * directly. A component will rendered by its parent if it is a container, or
     * rendered when attached if added to a gwt panel.
     * 
     * @param target
     *               the element this component should be rendered into
     * @param index
     *               the index within the container <b>before</b> which this
     *               component will be inserted (defaults to appending to the end of
     *               the container if value is -1)
     */
    public void render(Element target, int index) {
        // Check that the component has not already been rendered.
        if (rendered > RENDER_PENDING) {
            // If we are here then we are moving parents.
            DomSupport.insertChild (target, rootEl, index);
            return;
        }

        // Perform and pre-rendering processing.
        onBeforeRender ();

        // Indicate the rendering has started.
        rendered = RENDER_STARTED;

        // If we are here then then should be no root element. For safety we double
        // check this and if present then just proceed as if rendered.
        if (rootEl == null) {
            if (index < 0)
                index = target.childElementCount;
            rootEl = createRootElement ();
            DomSupport.insertChild (target, rootEl, index);
            if (DebugMode.RENDER.set ())
                Logger.trace ("[cpt]", "{rendering} [" + toString() + "]");
            _render ();
            if (config != null) {
                Config cfg = (Config) config;
                if (cfg.adorn != null)
                    cfg.adorn.accept (rootEl);
                if ((cfg.styles != null) && !cfg.styles.isEmpty())
                    cfg.styles.forEach (style -> rootEl.classList.add (style));
            }
        } else {
            onPostRender ();
        }
    }

    /**
     * Components that are re-used across rendering. The appear here simple to avoid
     * disposing of them.
     */
    private Collection<IComponent> reusedComponents;

    /**
     * Marks a component as being re-used across re-renderings.
     * 
     * @param component
     *                  the component to mark.
     * @return the passed component.
     */
    public <CPT extends IComponent> CPT reuse(CPT component) {
        if (component != null) {
            if (reusedComponents == null)
                reusedComponents = new HashSet<> ();
            reusedComponents.add (component);
        }
        return component;
    }

    /**
     * Performs a re-rendering of the component. This will dispose of all components
     * and attachments (so these need to be re-done).
     * <p>
     * If the component has not already been rendered then nothing is done.
     */
    public void rerender() {
        if (getRoot () == null)
            return;

        if (DebugMode.RENDER.set ())
            Logger.trace ("[cpt]" ,"{rerendering} [" + toString() + "]");

        // Clear out all child components.
        disposeChildren (reusedComponents);

        // Clear out the DOM contents of the root element.
        DomSupport.removeAllChildren (getRoot ());

        // Perform the render.
        _render ();
    }

    /**
     * See {@link #rerender()} by replaces the existing configuration with this new
     * configuration.
     * 
     * @param config
     *               the replacement configuration.
     */
    public void rerender(C config) {
        this.config = config;
        rerender();
    }

    /**
     * Called by {@link #render(Element, int)}.
     */
    private void _render() {
        try {
            // Mark rendering as drawn.
            IDomSelector selector = onRender (rootEl);
            if (styles () != null)
                rootEl.classList.add (styles ().component ());
            if (onRenderHandlers != null) {
                onRenderHandlers.forEach(handler -> {
                    handler.accept (this, rootEl);
                });
            }

            // For any of the registered event handlers determine if any are selectable post
            // render.
            if (uiHandlers != null)
                uiHandlers.forEach (h -> h.select (selector));
        } catch (Throwable e) {
            Logger.reportUncaughtException (e, this);
        }
        rendered = RENDER_DRAWN;
        onPostRender ();
    }

    /**
     * Post-render processing.
     */
    protected void onPostRender() {
        // Retrieve or assign the ID.
        if (id == null)
            id = getRoot ().id;
        else
            getRoot ().id = id;

        // Set any width and height.
        // if ((preRenderWidth != null) || (preRenderHeight != null)) {
        //     reconfiguring = true;
        //     setSize (preRenderWidth, preRenderHeight);
        //     reconfiguring = false;
        // }

        // Set hide state.
        if (preRenderHide) {
            preRenderHide = false;
            hide ();
        }

        // Process after render.
        onAfterRender ();

        // Puts a marker on the component if debugging.
        if ((DebugMode.NAME.set () || DebugMode.OUTLINE.set ()) && (getRoot () != null)) {
            if (DebugMode.NAME.set ()) 
                JQuery.$ (getRoot ()).attr ("component", toString());
            if (DebugMode.OUTLINE.set ())
                JQuery.$ (getRoot ()).css ("border", "1px dashed #eee");
        }

        // Sets the hidden and disabled states.
        if (disabled)
            implDisable ();

        if (masked)
            mask ();

        // Fire event.
        fireEvent (IRenderListener.class).onRender (this, false);

        // Ensure that a resize event is invoked if need be.
        reconfigure ();

        // Process the after render and reconfigure event.
        fireEvent (IRenderListener.class).onRender (this, false);
        rendered = RENDER_CONFIGURED;
        onAfterRenderAndReconfigure ();

        // Last we assign the test ID.
        if (Debug.isTestMode()) {
            getRoot().setAttribute("test-id", getTestId ());
            getRoot().setAttribute("test-cpt", getComponentId ());
        }
    }

    /**
     * Creates the root element of the component into which the component will be
     * rendered. In general there is no need to override this and simply implement
     * rendering through {@link #onRender(Element)}.
     * 
     * @return the root element.
     */
    protected Element createRootElement() {
        return DomSupport.createDiv ();
    }

    /**
     * Called by {@link #render(Element, int)} to actually render the content of the
     * component into the given element (the root element).
     * <p>
     * If not overridden then an attempt will be made to use the assigned renderer.
     * If that is not present then an attempt to build a renderer will be made (by
     * calling {@link #buildRenderer()}).
     * 
     * @param el
     *           the root element to render into.
     * @return if a renderer generates a selectable this is that selectable (to mine
     *         elements from).
     */
    protected IDomSelector onRender(Element el) {
        // First we attempt to build a renderer is there is none assigned.
        if (renderer == null) {
            renderer = buildRenderer ();
            if (renderer == null)
                throw new RuntimeException ("No renderer");
            if (renderer instanceof IUIEventHandler)
                registerEventHandler ((IUIEventHandler) renderer);
        }

        // Render and register any returned event handler.
        IUIEventHandler handler = renderer.render (el, config ());
        if (handler != null) {
            if (handler instanceof NodeContext) {
                // This is a special case where the passed handler comes from the DomBuilder
                // which may have components lodged against it. We need to extract these and
                // adopt them. We do this here as the DomBuilder could be used in numerous
                // contexts (i.e. from a directly supplied renderer through to one of the
                // buildNode methods).
                adopt ((NodeContext) handler);
            }
            registerEventHandler (handler, "onRender");
            if (handler instanceof IDomSelector)
                return ((IDomSelector) handler);
        }

        // No selector is available.
        return null;
    }

    /**
     * Creates the renderer.
     * <p>
     * One may override this to return a renderer of choice, otherwise a renderer
     * will be constructed that will either utilise {@link #buildNode(Config)} or
     * will go down the template path with {@link #buildContainer(Container)}).
     * 
     * @return the renderer.
     */
    protected IDataRenderer<C> buildRenderer() {
        return new IDataRenderer<C> () {

            private IDataRenderer<C> renderer;

            @Override
            public IUIEventHandler render(Element el, C data) {
                if (renderer == null) {
                    INodeProvider node = null;
                    try {
                        node = buildNode (el, data);
                    } catch (Throwable e) {
                        // This is really for debugging purposes. When an exception is ecountered we
                        // re-run it here to allow a breakpoint to be set.
                        node = buildNode (el, data);
                    }
                    if (node != null) {
                        // Check if we need to append the node (this will be the case if the returned
                        // node is not the root node).
                        Node nodeEl = node.node ();
                        if (nodeEl != el)
                            el.appendChild (node.node ());
                        if (node instanceof IUIEventHandler)
                            return (IUIEventHandler) node;
                        return null;
                    }
                    if (cacheKey == null)
                        cacheKey = "__" + Component.this.getClass ().getCanonicalName ();
                    renderer = ITemplateBuilder.renderer (cacheKey, root -> Component.this.buildContainer (root));
                }
                return renderer.render (el, data);
            }
        };
    }

    /**
     * Builds into the passed node (see {@link Wrap#buildInto(Element, Consumer)})
     * and registers a UI handler to handle any declared events as well as removing
     * any components previously rendered into the passed element (as it is deemed
     * to have been cleared of all contents).
     * <p>
     * If this is called against the same element a second time then any previously
     * recorded event handler will be removed and replaced.
     * <p>
     * Note that the event handlers will be added ahead of any others that are
     * registered (which generally reflects the priorty afforded to handlers
     * declared at lower levels of the DOM).
     * <p>
     * See also {@link #registerEventHandler(IUIEventHandler, String)}.
     * 
     * @param el
     *                the element to render into (the contents will be removed).
     * @param builder
     *                to build out the element.
     */
    protected void buildInto(Element el, Consumer<ElementBuilder> builder) {
        buildInto (el, builder, null);
    }

    /**
     * Builds into the passed node (see {@link Wrap#buildInto(Element, Consumer)})
     * and registers a UI handler to handle any declared events.
     * <p>
     * If this is called against the same element a second time then any previously
     * recorded event handler will be removed and replaced.
     * <p>
     * See also {@link #registerEventHandler(IUIEventHandler, String)}.
     * 
     * @param el
     *                the element to render into (the contents will be removed).
     * @param builder
     *                to build out the element.
     * @param extractor
     *                  to extract elements from the newly built DOM.
     */
    protected void buildInto(Element el, Consumer<ElementBuilder> builder, Consumer<NodeContext> extractor) {
        if (el == null)
            return;
            
        // We need to generate a unique reference for the element, we do this by
        // assigning an attribute with a unique ID and re-using that for subsequent
        // calls.
        String uid = el.getAttribute("__jui_nuid");
        if (uid == null) {
            uid = UID.createUID ();
            el.setAttribute("__jui_nuid", uid);
        }
        buildInto (uid, el, builder, extractor);
    }

    /**
     * See {@link #buildInto(Element, Consumer)} but rather than deriving a
     * replacement key to register event handlers with it uses the passed key.
     * <p>
     * See also {@link #registerEventHandler(IUIEventHandler, String)}.
     * 
     * @param replacementKey
     *                       the key to use to replace any previously registered
     *                       event handlers.
     * @param el
     *                       the element to render into (the contents will be
     *                       removed).
     * @param builder
     *                       to build out the element.
     */
    protected void buildInto(String replacementKey, Element el, Consumer<ElementBuilder> builder) {
        buildInto (replacementKey, el, builder, null);
    }

    /**
     * See {@link #buildInto(Element, Consumer)} but rather than deriving a
     * replacement key to register event handlers with it uses the passed key.
     * <p>
     * See also {@link #registerEventHandler(IUIEventHandler, String)}.
     * 
     * @param replacementKey
     *                       the key to use to replace any previously registered
     *                       event handlers.
     * @param el
     *                       the element to render into (the contents will be
     *                       removed).
     * @param builder
     *                       to build out the element.
     * @param extractor
     *                  to extract elements from the newly built DOM.
     */
    protected void buildInto(String replacementKey, Element el, Consumer<ElementBuilder> builder, Consumer<NodeContext> extractor) {
        if (DebugMode.RENDER.set())
            Logger.trace ("[cpt]", "{render-into} [" + toString() + "] key=" + replacementKey);
        disposeChildren (el);
        INodeProvider provider = (extractor == null) ? Wrap.buildInto (el, builder) : Wrap.buildInto (el, builder, extractor);
        if (provider instanceof IUIEventHandler)
            registerEventHandler ((IUIEventHandler) provider, replacementKey, true);
        if (provider instanceof NodeContext)
            adopt ((NodeContext) provider);
    }

    /**
     * Used by the default renderer returned by {@link #buildRenderer()}. This
     * provides a simple mechanism for rendering the component DOM and any
     * associated event handling. The suggested mechanism for this approach is to
     * build a node provider using the methods available on {@link DomBuilder}.
     * <p>
     * The returned node provider can implement {@ink IUIEventHandler} and if it
     * does it will be registered to handle events.
     * <p>
     * This differs from {@link #buildNode(Config)} in that the root element is
     * passed so that it can incorporated into the node provider (i.e. see
     * {@link DomBuilder#el(Element)}). This can be useful when more than one child
     * needs to be added to the root or where you want to modify the root attributes
     * (including adding classes).
     * 
     * @param el
     *             the root element that the node should wrap.
     * @param data
     *             the data to render.
     * @return the rendered root node (as a provider) already inserted into the root
     *         element.
     */
    protected INodeProvider buildNode(Element el, C data) {
        INodeProvider node = buildNode (data);
        if (node == null)
            node = buildNode (el);
        return node;
    }

    /**
     * See {@link #buildNode(Config)} but the returned node will be inserted into
     * the root element.
     * 
     * @param data
     *             the data to render.
     * @return the rendered root node (as a provider) to be inserted into the root
     *         node.
     */
    protected INodeProvider buildNode(C data) {
        return null;
    }
    
    /**
     * See {@link #buildNode(Config)} but the returned node will be inserted into
     * the root element.
     * 
     * @param el
     *             the root element that the node should wrap.
     * @return the rendered root node (as a provider) to be inserted into the root
     *         node.
     */
    protected INodeProvider buildNode(Element el) {
        return null;
    }

    /**
     * Changes the key of the renderer (where the renderer was created by specifying
     * a cache key in the first instance). This has to be called prior to the first
     * render and has no effect if a renderer was assigned.
     * <p>
     * This can be used we a fixed key is provided but due to some configuration
     * change (such as styles) it needs to be updated.
     * 
     * @param cacheKey
     *                 the cache key name.
     * @param append
     *                 if to append to the initial cache name.
     */
    protected void buildContainerCacheKey(String cacheKey, boolean append) {
        if (append && (this.cacheKey == null)) {
            // We add a prefix to provide better separation in cases where the class name
            // may be used for other builders.
            this.cacheKey = "__" + getClass ().getCanonicalName ();
        }
        this.cacheKey = !append ? cacheKey : this.cacheKey + "$" + cacheKey;
    }

    /**
     * Builds out a renderer template. This will be used if
     * {@link #buildNode(Config)} returns {@code null} (which it does by default).
     * <p>
     * All templated renderers will be cached and the default cache key is the
     * canonical class name. A key can assigned directly through the constructor or
     * by making a call to {@link #buildContainerCacheKey(String, boolean)} prior to
     * the first render.
     * 
     * @param root
     *             the root container to build into.
     */
    protected void buildContainer(Container<C> root) {
        // Nothing.
    }

    /**
     * Invoked immediately prior to rendering.
     */
    protected void onBeforeRender() {
        // Nothing.
    }

    /**
     * See {@link #handleAfterRender(Invoker)}.
     */
    private Invoker afterRenderHandler;

    /**
     * To handler the after-render event.
     * 
     * @param afterRenderHandler
     *                           the handler.
     */
    protected void handleAfterRender(Invoker afterRenderHandler) {
        this.afterRenderHandler = afterRenderHandler;
    }

    /**
     * Called after rendering the component (the component has been renderer, focus
     * events sunk and initial size set).
     */
    protected void onAfterRender() {
        if (blurFocusManager.isHasFocus()) {
            TimerSupport.defer (() -> {
                renderFocus ();
            });
        }
        if (afterRenderHandler != null) {
            try {
                afterRenderHandler.invoke();
            } catch (Throwable e) {
                Logger.reportUncaughtException(e, this);
            }
        }
    }

    /**
     * Called after the rendering and reconfiguration of the component (this is at
     * the very end of {@link #render(Elem, int)} and after a call to
     * {@link #onAfterRender()} has been made).
     */
    protected void onAfterRenderAndReconfigure() {
        // Nothing.
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#setHeight(com.effacy.jui.core.client.dom.css.Length)
     */
    // @Override
    // @SuppressWarnings("unchecked")
    // public <T extends IComponent> T setHeight(Length height) {
    //     setSize (null, height);
    //     return (T) this;
    // }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#setWidth(com.effacy.jui.core.client.component.Length)
     */
    // @Override
    // @SuppressWarnings("unchecked")
    // public <T extends IComponent> T setWidth(Length width) {
    //     setSize (width, null);
    //     return (T) this;
    // }

    /************************************************************************
     * Focus and blur
     ************************************************************************/

    /**
     * For managing blur and focus (which can be tricky).
     */
    protected BlurAndFocusBehaviour blurFocusManager = new BlurAndFocusBehaviour () {

        @Override
        protected void renderFocus() {
            super.renderFocus ();
            if (isRendered ())
                applyFocusStyle ();
        }

        @Override
        protected void renderBlur() {
            super.renderBlur ();
            if (isRendered ())
                removeFocusStyle ();
        }

        @Override
        protected void onBlur() {
            onBlurUI ();
        }

        @Override
        protected void onFocus() {
            onFocusUI ();
        }

    };

    /**
     * Used to apply the focus style.
     * <p>
     * The default is to check if {@link #styles()} return a non-{@code null} value
     * then apply {@link IComponentCSS#focus()} to the root element.
     */
    protected void applyFocusStyle() {
        if (styles () != null)
            getRoot ().classList.add (styles ().focus ());
    }

    /**
     * Used to remove the focus style.
     * <p>
     * The default is to as per {@link #applyFocusStyle()} but to remove the style
     * not add it.
     */
    protected void removeFocusStyle() {
        if (styles () != null)
            getRoot ().classList.remove (styles ().focus ());
    }

    /**
     * Adds an element for focus events.
     * <p>
     * This will serve both to receive focus actions and to respond to focus events.
     * 
     * @param el
     *           the focus element.
     * @return the passed element.
     */
    protected Element manageFocusEl(Element el) {
        blurFocusManager.manageFocusEl (el);
        return el;
    }

    /**
     * Determines if the component has focus.
     * 
     * @return {@code true} if it does.
     */
    public boolean isInFocus() {
        return blurFocusManager.isHasFocus();
    }

    /**
     * Called when the UI generates a focus event (i.e. direct from the DOM).
     * <p>
     * The default behaviour is to invoke {@link #focus()} (which is non-reentrant).
     * <p>
     * Note that this should only be called when such a UI focus event is detected
     * so is implementation specific.
     */
    protected void onFocusUI() {
        onFocus ();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the control already has focus then nothing is done (this is important to
     * prevent runaway conditions in response to focus event handling). Otherwise
     * the control will first be marked as being in focus then a call will be made
     * to {@link #renderFocus()} to change the UI state (any UI focus events that
     * invoke {@link #onFocusUI()} will be ignored as the control will have been
     * marked as gaining focus). Finally a call to {@link #onFocus()} is made.
     * 
     * @see com.effacy.jui.core.client.component.IComponent#focus()
     */
    @Override
    public void focus() {
        if (blurFocusManager.isHasFocus ())
            return;
        renderFocus ();
        onFocus ();
    }

    /**
     * Invoked from {@link #focus()} to apply blur to the UI.
     */
    protected void renderFocus() {
        blurFocusManager.focus ();
    }

    /**
     * Invoked whenever the component changes its focus state to being in focus.
     * This can be done programmatically (see {@link #focus()}) or by receipt of a
     * UI focus event (see {@link #onFocusUI()}).
     * <p>
     * The default behaviour is to invoke
     * {@link IComponentListener#onFocusUI(IComponent)}.
     */
    protected void onFocus() {
        fireEvent (IFocusBlurListener.class).onFocus (this);
    }

    /**
     * Determines if the component does not have focus.
     * 
     * @return {@code true} if it does not.
     */
    public boolean isInBlur() {
        return !isInFocus();
    }

    /**
     * Called when the UI generates a blur event (i.e. direct from the DOM).
     * <p>
     * The default behaviour is to invoked {@link #blur()}.
     * <p>
     * Note that this should only be called when such a UI blur event is detected so
     * is implementation specific.
     */
    protected void onBlurUI() {
        onBlur ();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the control does not have focus then nothing is done (this is important to
     * prevent runaway conditions in response to blur event handling). Otherwise the
     * control will first be marked as having lost focus then a call will be made to
     * {@link #renderBlur()} to change the UI state (any UI focus events that invoke
     * {@link #onBlurUI()} will be ignored as the control will have been marked as
     * gaining focus). Finally a call to {@link #onBlur()} is made.
     * 
     * @see com.effacy.jui.core.client.component.IComponent#blur()
     */
    @Override
    public void blur() {
        if (!blurFocusManager.isHasFocus ())
            return;
        renderBlur ();
        onBlur ();
    }

    /**
     * Invoked from {@link #blur()} to apply blur to the UI.
     */
    protected void renderBlur() {
        // Re-entrance here is OK as the behaviour expects this as a possibility.
        blurFocusManager.blur ();
    }

    /**
     * Invoked whenever the component changes its focus state to having lost focus.
     * This can be done programmatically (see {@link #blur()}) or by receipt of a UI
     * focus event (see {@link #onBlurUI()}).
     * <p>
     * The default behaviour is to invoke
     * {@link IComponentListener#onBlurUI(IComponent)}.
     */
    protected void onBlur() {
        fireEvent (IFocusBlurListener.class).onBlur (this);
    }

    /************************************************************************
     * Display states (show and hide).
     ************************************************************************/

    /**
     * Hides the component.
     */
    public void hide() {
        // If not rendered then pre-render.
        if (rendered == RENDER_PENDING) {
            preRenderHide = true;
            return;
        }

        // Rendered so apply rendering.
        if (!hidden) {
            hidden = true;
            JQuery.$ (getRoot ()).hide ();
            onHide ();
        }
    }

    /**
     * Called when the component is hidden.
     */
    protected void onHide() {
        fireEvent (IResizeListener.class).onResize (this);
        fireEvent (IShowHideListener.class).onHide (this);
    }

    /**
     * Shows the component.
     */
    public void show() {
        // If not rendered then just change the internal state.
        if (!isRendered ()) {
            hidden = false;
            return;
        }

        // Hide if already showing (which requires it to have been rendered).
        if (!isHidden ())
            hide ();

        // Start show process.
        onBeforeShow ();
        hidden = false;
        JQuery.$ (getRoot ()).show ();
        onShow ();
    }

    /**
     * Called immediately prior to showing (by
     * {@link #show(IComponentShower, ICompletionCallback)}). Does nothing by
     * default.
     */
    protected void onBeforeShow() {
        // Nothing.
    }

    /**
     * Called when the component is shown (that is, when the shower has completed
     * showing the component).
     */
    protected void onShow() {
        fireEvent (IResizeListener.class).onResize (this);
        fireEvent (IShowHideListener.class).onShow (this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#pushEnableState()
     */
    @Override
    public void pushEnableState() {
        if (enableStateStack == null)
            enableStateStack = new Stack<Boolean> ();
        enableStateStack.push (isEnabled ());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#popEnableState()
     */
    @Override
    public void popEnableState() {
        if ((enableStateStack != null) && !enableStateStack.isEmpty ()) {
            if (enableStateStack.pop ())
                enable ();
            else
                disable ();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#disable()
     */
    public void disable() {
        if (disabled)
            return;

        disabled = true;

        if (rootEl != null)
            implDisable ();

        onDisable ();
        fireEvent (IEnableDisableListener.class).onDisable (this);
    }

    /**
     * Implements disabling. Should only be called once rendered.
     */
    protected void implDisable() {
        if (styles () != null)
            getRoot ().classList.add (styles ().disabled ());
    }

    /**
     * This is invoked by {@link #disable()} whenever the component has been
     * disabled (and is not already disabled).
     */
    protected void onDisable() {
        // Nothing.
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#enable()
     */
    @Override
    public void enable() {
        if (!disabled)
            return;

        disabled = false;

        if (rootEl != null)
            implEnable ();

        onEnable ();
        fireEvent (IEnableDisableListener.class).onEnable (this);
    }

    /**
     * Implements enabling. Should only be called once rendered.
     */
    protected void implEnable() {
        if (styles () != null)
            getRoot ().classList.remove (styles ().disabled ());
    }

    /**
     * This is invoked by {@link #enable()} whenever the component has been enabled
     * (and is not already enabled).
     */
    protected void onEnable() {
        // Nothing.
    }

    /**
     * {@inheritDoc}
     *
     * @see IEventListener#processBrowserEvent(Event)
     */
    @Override
    public void processBrowserEvent(Event event) {
        // Pass events through to regions.
        if (regions != null) {
            for (RegionPoint region : regions) {
                if (region.onBrowserEvent (event))
                    return;
            }
        }

        // Don't accept if the component is disabled.
        if (!dispatchWhenDisabled && isDisabled ())
            return;

        // In safari process focus events correctly.
        // if (BrowserInfo.isWebKit && (UIEventType.ONCLICK.matches (event))) {
        //     //if ("input".equalsIgnoreCase (getRoot ().tagName) || (DomSupport.getPropertyAsString (Js.cast (event.target), "__eventBits") == null))
        //     if ("input".equalsIgnoreCase (getRoot ().tagName) || (((Element) event.target).getAttribute ("__eventBits") == null))
        //         focus ();
        // }

        // Wrap the event.
        UIEvent ce = new UIEvent (this, event);
        if (ce.isStopped ())
            return;

        // Process the event more generally.
        onUIEvent (ce);
    }

    /**
     * Called so the component can process an event directed to the component.
     * <p>
     * The default behaviour is to first invoke {@link #handleEvent(UIEvent)} and if
     * that returns {@code false} them to let the renderer handle the event. If the
     * event is not handled and no renderer is registered then it will check for
     * blur and focus events invoked {@link #onBlurUI()} or {@link #onFocusUI()}
     * respectively.
     * 
     * @param event
     *              the event (the source will be this component).
     */
    protected void onUIEvent(UIEvent event) {
        try {
            // Let any registered handler handle the event.
            if (uiHandlers != null) {
                for (IUIEventHandler handler : uiHandlers) {
                    if (handler.handleEvent (event)) {
                        // We stop the event unless it is a key press. The reason for that is stopping a
                        // key press will stop subsequent events.
                        if (!event.isEvent (UIEventType.ONKEYPRESS, UIEventType.ONKEYDOWN, UIEventType.ONKEYUP, UIEventType.ONPASTE, UIEventType.DRAGSTART, UIEventType.DRAGOVER, UIEventType.DRAGEND, UIEventType.DRAGENTER, UIEventType.DRAGLEAVE, UIEventType.DROP))
                            event.stopEvent ();
                        return;
                    }
                }
            }

            // Continue with the event handling chain.
            if (handleEvent (event)) {
                if (!event.isEvent (UIEventType.ONKEYPRESS))
                    event.stopEvent ();
            } else if (blurFocusManager.handleEvent (event)) {
                if (!event.isEvent (UIEventType.ONKEYPRESS))
                    event.stopEvent ();
            } else if (alternativeHandleEvent (event)) {
                if (!event.isEvent (UIEventType.ONKEYPRESS))
                    event.stopEvent ();
            } else {
                // Capture any focus events in a trivial manner (for the case where
                // these are not consumed by the event handlers).
                if (event.isEvent (UIEventType.ONBLUR))
                    onBlurUI ();
                else if (event.isEvent (UIEventType.ONFOCUS))
                    onFocusUI ();
            }
        } catch (ClassCastException e) {
            Logger.error ("Uncaught exception processing UI event: ", e);
        }
    }

    /**
     * Try to handle a UI event within the component.
     * 
     * @param event
     *              the event to handle.
     * @return {@code true} if the event was handled (default is {@code false}).
     */
    protected boolean handleEvent(UIEvent event) {
        return false;
    }

    /**
     * This is an alternative to the handle event.
     * 
     * @param event
     *              the event to handle.
     * @return {@code true} if handled.
     */
    protected boolean alternativeHandleEvent(UIEvent event) {
        return false;
    }

    /**
     * Process a resize of the window.
     * 
     * @param width
     *               the width of the window.
     * @param height
     *               the height of the window.
     */
    protected void onWindowResize(int width, int height) {
        // if (rootEl != null)
        // rootEl.sync ();
    }

    /**
     * Process a scroll of the window.
     * 
     * @param width
     *                   the width of the window.
     * @param height
     *                   the height of the window.
     * @param scrollLeft
     *                   the scroll left position.
     * @param scrollTop
     *                   the scroll top position.
     */
    protected void onWindowScroll(int width, int height, int scrollLeft, int scrollTop) {
        // Nothing.
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#activate()
     */
    @Override
    public void activate() {
        fireEvent (IActivateListener.class).onActivate (this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will first invoke a synchronisation of the root element (if rendered)
     * and the firing of a synchronisation event. Then it will call
     * {@link #onReconfigure()} to allow any immediate changes to be applied. After
     * this any resulting change in size will result in a resize event being fired
     * and a call to {@link #onResize()} being made. Finally
     * {@link #onDeferredReconfigure()} will be called once the browser event loop
     * has returned (along with another check for a change in size).
     * <p>
     * The method performs re-entrance checking to ensure that it does not execute
     * if it is already in the middle of executing.
     * 
     * @see com.effacy.jui.core.client.component.IComponent#reconfigure()
     */
    public final void reconfigure() {
        // Ensure that we are not being re-entrant.
        if (reconfiguring)
            return;
        reconfiguring = true;

        // Cancel any prior deferred reconfigure as we will be creating a new
        // one.
        if (deferredReconfigure != null) {
            deferredReconfigure.cancel ();
            deferredReconfigure = null;
        }

        // Actually run the reconfigure.
        try {
            if (rootEl != null) {
                // if (rootEl.sync ())
                // onReposition ();
            }
            onReconfigure ();
            assertResize ();
        } finally {
            reconfiguring = false;
        }

        // Deferred reconfigure.
        deferredReconfigure = new DeferredReconfigure ();
        TimerSupport.defer (deferredReconfigure);
    }

    /**
     * Invoked when the root element is repositioned (due to sync'ing of the
     * element).
     */
    protected void onReposition() {
        // Nothing.
    }

    /**
     * Called when there has been a request to reconfigure the component (this is
     * invoked only from {@link #reconfigure()}).
     * <p>
     * Sub-classes should override this in order to perform additional layout and
     * size adjustments to the component as a result of changes to the component. If
     * these adjustments need a stable DOM model to work against, then sub-classes
     * should override {@link #onDeferredReconfigure()} instead.
     * 
     * @see #onDeferredReconfigure()
     * @see #reconfigure()
     */
    protected void onReconfigure() {
        fireEvent (IReconfigureListener.class).onReconfigure (this);

        // Process regions.
        if (regions != null) {
            boolean resized = isResized ();
            for (RegionPoint region : regions)
                region.layout (resized);
        }
    }

    /**
     * Called when there has been a request to reconfigure the component (this is
     * invoked only from {@link #reconfigure()}). This differs from
     * {@link #onReconfigure()} in that it is called only after the browser event
     * loop has completed ensuring that the DOM is in a stable state.
     * 
     * @see #onReconfigure()
     * @see #reconfigure()
     */
    protected void onDeferredReconfigure() {
        if (regions != null) {
            for (RegionPoint region : regions)
                resizeRegion (region, region.getLocationReference ());
        }
        if (attachments != null) {
            for (AttachmentPoint attachment : attachments)
                resizeAttachment (attachment);
        }
    }

    /**
     * All the attachment points for the component.
     */
    private List<AttachmentPoint> attachments;

    /**
     * All the region points for the component.
     */
    protected List<RegionPoint> regions;

    /**
     * Resizes containment region. This must work out the height and width of the
     * attachment as it should be applied to the child component.
     * <p>
     * The default behaviour is to call {@link #resizeRegionLayout(Elem, String)} if
     * the region has been rendered.
     * 
     * @param region
     *               the region to resize.
     * @param name
     *               the name of the region.
     */
    protected void resizeRegion(IContainerRegion region, String name) {
        if (region.getLayoutTarget () != null)
            resizeRegionLayout (Js.cast (region.getLayoutTarget ()), name);
    }

    /**
     * Called by {@link #resizeRegion(IContainerRegion, String)} if the region has
     * been rendered.
     * <p>
     * Default behaviour is to do nothing to the region.
     * 
     * @param layoutTargetEl
     *                       the layout target for the region.
     * @param name
     *                       the name of the region.
     */
    protected void resizeRegionLayout(Element layoutTargetEl, String name) {
        // region.getLayoutTarget ().setAutoWidth ();
        // region.getLayoutTarget ().setAutoHeight ();
    }

    /**
     * Resizes an attachment point. This must work out the height and width of the
     * attachment as it should be applied to the child component.
     * <p>
     * The default behaviour is to do nothing (no resizing).
     * 
     * @param attachment
     *                   the attachment to resize.
     */
    protected void resizeAttachment(AttachmentPoint attachment) {
        // attachment.setSize (Length.AUTO, Length.AUTO);
    }

    /**
     * Finds an attachment by name (creating it if necessary).
     * 
     * @param location
     *                 the location name of the attachment.
     * @return The matching attachment.
     */
    protected RegionPoint findRegionPoint(String location) {
        if (regions == null)
            regions = new ArrayList<RegionPoint> ();
        for (RegionPoint region : regions)
            // Note that the equals will test for a string value.
            if (region.equals (location))
                return region;
        RegionPoint region = new RegionPoint (location);
        regions.add (region);
        return region;
    }

    /**
     * Finds an attachment by name (creating it if necessary).
     * 
     * @param location
     *                 the location name of the attachment.
     * @return The matching attachment.
     */
    protected AttachmentPoint findAttachmentPoint(String location) {
        if (attachments == null)
            attachments = new ArrayList<AttachmentPoint> ();
        for (AttachmentPoint attachment : attachments)
            // Note that the equals will test for a string value.
            if (attachment.equals (location))
                return attachment;
        AttachmentPoint attachment = new AttachmentPoint (location);
        attachments.add (attachment);
        return attachment;
    }

    /**
     * Registers an attachment point.
     * 
     * @param name
     *             the name of the attachment point.
     * @param el
     *             the element corresponding to the attachment point.
     * @return The created attachment point.
     */
    protected AttachmentPoint registerAttachmentPoint(String name, Element el) {
        AttachmentPoint ap = findAttachmentPoint (name);
        ap.setElement (el);
        return ap;
    }

    /**
     * De-registers an attachment point by name.
     * 
     * @param name
     *             the name of the attachment point.
     * @return {@code true} if the attachment point existed and was de-registered.
     */
    protected boolean deregisterAttachmentPoint(String name) {
        for (AttachmentPoint attachment : attachments) {
            if (attachment.equals (name))
                return deregisterAttachmentPoint (attachment);
        }
        return false;
    }

    /**
     * De-registers an attachment point. This will detach any attached component and
     * removes the attachment point from the list of attachment points.
     * 
     * @param attachment
     *                   the attachment point to de-register.
     * @return {@code true} if the attachment point existed and was de-registered.
     */
    protected boolean deregisterAttachmentPoint(AttachmentPoint attachment) {
        if (attachments.contains (attachment)) {
            attachment.detach ();
            attachment.component = null;
            attachment.locationEl = null;
            attachments.remove (attachment);
            return true;
        }
        return false;
    }

    protected boolean deregisterAttachmentPoint(IComponent component) {
        if (component == null)
            return false;
        for (AttachmentPoint attachment : attachments) {
            if ((attachment.component != null) && attachment.component.equals (component)) {
                return deregisterAttachmentPoint (attachment);
            }
        }
        return false;
    }

    /**
     * Registers a component against the component. This will create an attachment
     * point for the component with a random ID.
     * 
     * @param component
     *                  the component to register.
     * @param attachEl
     *                  the element to attach the component to.
     * @return The component that was added.
     */
    public <CPT extends IComponent> CPT registerComponent(CPT component, Element attachEl) {
        AttachmentPoint ap = registerAttachmentPoint (UID.createUID (), attachEl);
        ap.setComponent (component);
        onComponentRegistered (component, ap);
        return component;
    }

    /**
     * Called when a child component has been registered with the component.
     * 
     * @param component
     *                  the component.
     * @param ap
     *                  the attachment point to which the component has been
     *                  registered.
     */
    protected void onComponentRegistered(IComponent component, AttachmentPoint ap) {
        fireEvent (IAddRemoveListener.class).onAdd (this, component);
    }

    /**
     * Registers a control also registering it as a component against the control
     * element from the control handler. Also registered the control with the
     * handler (for management).
     * 
     * @param control
     *                the control to register.
     * @param handler
     *                the handler for the control.
     * @return the passed control.
     */
    public <V, CTRL extends IControl<V>> CTRL registerControl(CTRL control, Element attachEl) {
        if (control == null)
            return null;
        registerComponent (control, attachEl);
        onControlRegistered (control);
        return control;
    }

    /**
     * Registers a control (that has been added by some other means) such that it
     * will invoke {@link #onControlRegistered(IControl)}.
     * 
     * @param control
     *                the control to register.
     * @return the passed control.
     */
    public <V, CTRL extends IControl<V>> CTRL registerControl(CTRL control) {
        if (control == null)
            return null;
        onControlRegistered (control);
        return control;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#contains(com.effacy.jui.core.client.component.IComponent)
     */
    @Override
    public boolean contains(IComponent component) {
        if (component == this)
            return true;
        if (attachments != null) {
            for (AttachmentPoint ap : attachments) {
                if (ap.component == component)
                    return true;
            }
        }
        if (regions != null) {
            for (RegionPoint rp : regions) {
                if (rp.contains (component))
                    return true;
            }
        }
        if ((managedComponents != null) && managedComponents.contains (component))
            return true;
        return false;
    }

    /**
     * Obtains (and creates if necessary) the control manager.
     * 
     * @return the control manager.
     */
    public ControlContext controls() {
        if (controls == null) {
            controls = new ControlContext () {

                @Override
                protected void onModified(IControl<?> control) {
                    Component.this.onControlModified (control);
                    Component.this.fireEvent (IModifiedListener.class).onModified (control);
                }

            };
        }
        return controls;
    }

    /**
     * Called when a control has been registered with the component.
     * 
     * @param control
     *                the control.
     * @return a suitable control handler.
     */
    protected <V, CTRL extends IControl<V>> CTRL onControlRegistered(CTRL control) {
        if (controls ().contains (control))
            return control;
        fireEvent (IAddRemoveListener.class).onAdd (this, control);
        controls ().register (control);
        return control;
    }

    /**
     * Determines if the components current size has changed from the last time
     * {@link #onResize()} was invoked (via {@link #assertResize()}). Only a call to
     * {@link #assertResize()} will reset the internal size state allowing this
     * method to return {@code false}.
     * 
     * @return {@code true} if the component has a different size.
     */
    protected boolean isResized() {
        if (rootEl == null)
            return false;
        if (lastActualWidth != rootEl.clientWidth)
            return true;
        if (lastActualHeight != rootEl.clientHeight)
            return true;
        return false;
    }

    /**
     * Asserts any change in size.
     */
    protected void assertResize() {
        if (isResized () && (rootEl != null)) {
            lastActualWidth = rootEl.clientWidth;
            lastActualHeight = rootEl.clientHeight;
            onResize ();
        }
    }

    /**
     * Called when the component has actually changed in size.
     */
    protected void onResize() {
        fireEvent (IResizeListener.class).onResize (this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#addListener(IListener).
     */
    @Override
    public <L extends IListener> L addListener(L listener) {
        return outboundBus.addListener (listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.gwt.common.client.event.IObservable#removeListener(com.effacy.gwt.common.client.event.IListener)
     */
    @Override
    public void removeListener(IListener... listeners) {
        outboundBus.removeListener (listeners);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.gwt.common.client.event.IObservable#removeAllListeners()
     */
    @Override
    public void removeAllListeners() {
        outboundBus.removeAllListeners ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#contains(elemental2.dom.Element)
     */
    @Override
    public boolean contains(Element el) {
        if (rootEl == null)
            return false;
        return DomSupport.isChildOf (el, getRoot ());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.component.IComponent#fireEvent(java.lang.Class)
     */
    @Override
    public <L extends IListener> L fireEvent(Class<L> listenerClass, IListener... listeners) {
        return outboundBus.fireEvent (listenerClass, listeners);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.gwt.common.client.event.IObservable#addObservable(com. effacy
     *      .gwt.common.client.event.IObservable, java.lang.Class<? extends
     *      com.effacy.gwt.common.client.event.IListener>[])
     */
    @Override
    @SafeVarargs
    public final void convey(IObservable observable, Class<? extends IListener>... listenerTypes) {
        outboundBus.convey (observable, listenerTypes);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.gwt.common.client.event.IObservable#removeObservable(com.effacy.gwt.common.client.event.IObservable[])
     */
    @Override
    public void removeObservable(IObservable... observables) {
        outboundBus.removeObservable (observables);
    }

    /**
     * Gets the observable for propagating events through to children. The component
     * as an observable is intended to propagate events external to the component.
     * For example, a child component may convey events through to the parent
     * component which then emits that event to be conveyed on up to its parent
     * (where event conveying is concerned). However, sometimes a parent may want to
     * convey events through to its children. Sometimes this event originated from a
     * child and so one does not want to enter into a state of multiple event firing
     * (once out and once in) and infinite looping of events. As such, one may
     * employ the in-bound bus to handle this inward direction.
     * 
     * @return The in-bound observable.
     */
    public IObservable getInbound() {
        if (inboundBus == null)
            inboundBus = new Observable ();
        return inboundBus;
    }

    /**
     * Called when a child component of this component (when the component manages
     * other components) resizes. By default this calls {@link #reconfigure()} so
     * most response behaviour can be implemented by overriding one of
     * {@link #onReconfigure()} or {#link {@link #onDeferredReconfigure()} (that is,
     * only override this method if the sub-class needs to know that a child has
     * been resized).
     */
    protected void onChildResize() {
        reconfigure ();
    }

    /**
     * Attaches a component to the given location.
     * <p>
     * This is just a convenience to combine the location of the attachment point
     * and assignment of its component. This does not assign an element so that is
     * expected to be done separately.
     * 
     * @param component
     *                  the component to attach.
     * @param location
     *                  the location to attach to.
     * @see {@link #findAttachmentPoint(String)}
     */
    protected <CPT extends IComponent> CPT attach(String location, CPT component) {
        return findAttachmentPoint (location).setComponent (component);
    }

    /**
     * Invoked when a region layout has been executed.
     * 
     * @param region
     *               the region the layout is being executed on.
     */
    protected void onRegionLayoutExecuted(RegionPoint region) {
        // Nothing.
    }

    /**
     * Invoked when a layout on a region has completed.
     * 
     * @param region
     *                    the region the layout was applied to.
     * @param firstLayout
     *                    if this was the first layout for the region.
     */
    protected void onRegionLayoutComplete(RegionPoint region, boolean firstLayout) {
        // Nothing.
    }

    /**
     * Invoked prior to adding a component to a region.
     * 
     * @param region
     *                  the region the component is being added to.
     * @param component
     *                  the component being added.
     */
    protected void onRegionBeforeRemove(RegionPoint region, IComponent component) {
        // Nothing.
    }

    /**
     * Invoked just before a component is to be removed. The default implementation
     * invokes {@link #sync(boolean)} if the component has auto height or width.
     * 
     * @param item
     *             the component that is to be removed.
     */
    protected void onBeforeRemove(IComponent item, RegionPoint region) {
        // Nothing.
    }

    /**
     * Called when a component has been removed.
     * 
     * @param item
     *             the component that has been removed.
     */
    protected void onRemove(IComponent item, RegionPoint region) {
        // Nothing.
    }

    /**
     * Called after an item has been inserted. The default implementation executes
     * {@link #sync(boolean)} if the container has auto width or height.
     * 
     * @param item
     *              the component being inserted.
     * @param index
     *              the index at which the component has been inserted.
     */
    protected void onInsert(IComponent item, int index, RegionPoint region) {
        // Nothing.
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#forEach(java.util.function.Consumer)
     */
    @Override
    public void forEach(Consumer<IComponent> visitor) {
        if (this.regions != null)
            this.regions.forEach (region -> region.forEach (visitor));
        if (this.attachments != null) {
            this.attachments.forEach (ap -> {
                if (ap.component != null)
                    visitor.accept (ap.component);
            });
        }
        if (this.managedComponents != null) {
            new ArrayList<> (managedComponents).forEach (c -> visitor.accept (c));
        }
    }

    /**
     * Invoked when one of the registered controls is modified.
     * 
     * @param control
     *                the control.
     */
    protected void onControlModified(IControl<?> control) {
        // Nothing.
    }

    /**
     * Convenience to add a {@link IModifiedListener} listener.
     * 
     * @param handler
     *                to handle when a control is modified.
     */
    public void handleControlModified(Consumer<IControl<?>> handler) {
        if (handler != null)
            addListener(IModifiedListener.create(handler));
    }

    /************************************************************************
     * Tools to work with DomBuilder.
     ************************************************************************/

    /**
     * Given a component this returns a node consumer that will attach the component
     * to the node under a unqiue attachment point.
     * <p>
     * This is intended to be used with {@link DomBuilder} (see
     * {@link NodeBuilder#exec(Consumer)}).
     * 
     * @param cpt
     *            the component to attach.
     * @return the attachment consumer.
     */
    public Consumer<Node> attach(IComponent cpt) {
        return n -> {
            String uniqueReference = "_AP_UNIQUE_" + UID.createUID ();
            AttachmentPoint ap = findAttachmentPoint (uniqueReference);
            ap.setElement ((Element) n);
            ap.setComponent (cpt);
        };
    }

    /**
     * Convenience to apply the {@link #attach(IComponent)} to the passed node.
     */
    public <D extends IComponent> D attach(ElementBuilder node, D cpt) {
        node.apply (attach (cpt));
        return cpt;
    }

    /**
     * See {@link #region(String, ILayout, IComponent...)} but constructs a unique
     * name to use and with the {@link MinimalLayout}.
     */
    public Consumer<Node> region(IComponent... cpts) {
        return region ("_RG_UNIQUE_" + UID.createUID (), MinimalLayout.config ().build (), cpts);
    }

    /**
     * Convenience to apply {@link #region(IComponent...)} to the passed
     * node.
     */
    public void region(ElementBuilder node, IComponent... cpts) {
        node.apply (region (cpts));
    }

    /**
     * See {@link #region(String, ILayout, IComponent...)} but constructs a unique
     * name to use.
     */
    public Consumer<Node> region(ILayout layout, IComponent... cpts) {
        return region ("_RG_UNIQUE_" + UID.createUID (), layout, cpts);
    }

    /**
     * Convenience to apply {@link #region(ILayout, IComponent...)} to the passed
     * node.
     */
    public void region(ElementBuilder node, ILayout layout, IComponent... cpts) {
        node.apply (region (layout, cpts));
    }

    /**
     * Given a component this returns a node consumer that will attach the
     * components to the node under a region point with the given reference and
     * layout.
     * <p>
     * This is intended to be used with {@link DomBuilder} (see
     * {@link NodeBuilder#exec(Consumer)}).
     * 
     * @param name
     *               the reference (see {@link #findRegionPoint(String)}).
     * @param layout
     *               the layout to apply.
     * @param cpts
     *               the components to attach.
     * @return the attachment consumer.
     */
    public Consumer<Node> region(String name, ILayout layout, IComponent... cpts) {
        return region (name, layout, rp -> {
            for (IComponent cpt : cpts) {
                if (cpt == null)
                    continue;
                rp.add (cpt);
            }
        });
    }

    /**
     * Convenience to apply {@link #region(String, ILayout, IComponent...)} to the passed
     * node.
     */
    public void region(ElementBuilder node, String name, ILayout layout, IComponent... cpts) {
        node.apply (region (name, layout, cpts));
    }

    /**
     * Given a component this returns a node consumer that will attach the
     * components to the node under a region point with the given reference and
     * layout.
     * <p>
     * This is intended to be used with {@link DomBuilder} (see
     * {@link NodeBuilder#exec(Consumer)}).
     * 
     * @param name
     *               the reference (see {@link #findRegionPoint(String)}).
     * @param layout
     *               the layout to apply.
     * @param cpts
     *               to add components to the region point.
     * @return the attachment consumer.
     */
    public Consumer<Node> region(String name, ILayout layout, Consumer<RegionPoint> cpts) {
        return n -> {
            RegionPoint region = findRegionPoint (name);
            if (layout != null)
                region.setLayout (layout);
            region.setElement ((Element) n);
            if (cpts != null)
                cpts.accept (region);
        };
    }

    /**
     * Convenience to apply {@link #region(String, ILayout, Consumer<RegionPoint>)} to the passed
     * node.
     */
    public void region(ElementBuilder node, String name, ILayout layout, Consumer<RegionPoint> cpts) {
        node.apply (region (name, layout, cpts));
    }

    /**
     * Invoked whenever the component is added to a parent (via a region or
     * attachment point).
     * <p>
     * The default is to configure any testable configuration.
     * 
     * @param parent
     *               the parent.
     */
    protected void onAddedTo(IComponent parent) {
        // Nothing.
    }

    /**
     * Implements a deferred reconfigure.
     */
    class DeferredReconfigure implements Runnable {

        /**
         * If the reconfigure should be cancelled.
         */
        private boolean cancelled = false;

        /**
         * Cancel the reconfigure.
         */
        public void cancel() {
            this.cancelled = true;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            if (!cancelled) {
                onDeferredReconfigure ();
                assertResize ();
            }
        }

    }

    public interface IListenerVisitor {

        public void process(IResizeListener listener);
    }

    /**
     * Internal class that represents an attachment point.
     */
    protected class AttachmentPoint implements IParent {

        /**
         * The element that is the attachment element.
         */
        private Element locationEl;

        /**
         * Name of the attachment.
         */
        private String locationReference;

        /**
         * The component assigned to the attachment.
         */
        private IComponent component;

        // /**
        //  * The width of the attachment (if assigned).
        //  */
        // private Length width;

        // /**
        //  * The height of the attachment (if assigned).
        //  */
        // private Length height;

        /**
         * Construct with a location reference.
         * 
         * @param locationReference
         *                          the reference.
         */
        public AttachmentPoint(String locationReference) {
            this.locationReference = locationReference;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.component.IComponent.IParent#getTestId()
         */
        @Override
        public String getTestId() {
            return Component.this.getTestId();
        }

        public String getLocation() {
            return locationReference;
        }

        // public void setSize(Length width, Length height) {
        //     this.width = width;
        //     this.height = height;
        //     if (component != null)
        //         component.setSize (width, height);
        // }

        /**
         * Renders the attachment point.
         */
        private void render() {
            if ((locationEl != null) && (component != null)) {
                component.render (locationEl, 0);
                // component.setSize (width, height);
                if (Component.this.isAttached ())
                    attach ();
            }
        }

        /**
         * Sets the component for the attachment point.
         * <p>
         * This will also attempt to render the attachment point (if an element has been
         * assigned).
         * 
         * @param component
         *                  the component to set.
         * @return The passed component.
         */
        public <CPT extends IComponent> CPT setComponent(CPT component) {
            if (this.component != null)
                this.component.parent (null);
            this.component = component;
            if (this.component != null) {
                this.component.parent (this);
                if (componentListener == null)
                    componentListener = IResizeListener.create (cpt -> onChildResize ());
                this.component.addListener (componentListener);
                if (this.component instanceof Component)
                    ((Component<?>) this.component).onAddedTo (Component.this);
            }
            render ();
            return component;
        }

        /**
         * Assigns the element. At this point the attachment point is rendered.
         * 
         * @param locationEl
         *                   the element to associated with the attachment point.
         */
        public void setElement(Element locationEl) {
            this.locationEl = locationEl;
            render ();
        }

        /**
         * Performs a comparison based on location reference.
         */
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (this == obj)
                return true;
            if (obj instanceof String)
                return obj.equals (locationReference);
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.IComponent.IParent#orhpan(com.effacy.jui.core.client.component.IComponent)
         */
        @Override
        public void orhpan(IComponent child) {
            if ((child != null) && (this.component == child)) {
                if (componentListener != null)
                    this.component.removeListener (componentListener);
                this.component = null;
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.IComponent.IParent#isAttached()
         */
        @Override
        public boolean isAttached() {
            return Component.this.isAttached ();
        }

        /**
         * Attaches the associated component.
         */
        public void attach() {
            if ((component != null) && (component instanceof Component))
                ((Component<?>) component).attach ();
        }

        /**
         * Detaches the associated component.
         */
        public void detach() {
            if ((component != null) && (component.getRoot () != null)) {
                component.getRoot ().remove ();
                if (component instanceof Component)
                    ((Component<?>) component).detach ();
            }
        }

    }

    /**
     * Implementation of {@link IContainerRegion}. This is what actually contains
     * the various child components.
     */
    public class RegionPoint extends Observable implements IContainerRegion, IParent {

        /**
         * The element the container represents.
         */
        private Element el;

        /**
         * The layout to use for the container.
         */
        private ILayout layout;

        /**
         * The components that are contained in this container.
         */
        private List<IComponent> items = new ArrayList<> ();

        /**
         * Convenient location reference.
         */
        private String locationReference;

        private IResizeListener componentListener = new IResizeListener () {

            @Override
            public void onResize(IComponent component) {
                if (getItems ().contains (component))
                    onChildResize (component);
            }

        };

        public RegionPoint(String locationReference) {
            this.locationReference = locationReference;
            convey (Component.this);
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<IComponent> iterator() {
            return items.iterator ();
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.component.IComponent.IParent#getTestId()
         */
        @Override
        public String getTestId() {
            return Component.this.getTestId();
        }

        /**
         * Gets the location reference.
         * 
         * @return The location reference.
         */
        public String getLocationReference() {
            return locationReference;
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String)
                return matches ((String) obj);

            // TODO: Verify this is correct.
            return false;
        }

        /**
         * Determines if there is a match on location reference.
         * 
         * @param locationReference
         *                          the location reference to test for a match.
         * @return {@code true} if there is a match.
         */
        public boolean matches(String locationReference) {
            if (locationReference == null)
                return this.locationReference == null;
            return (locationReference.equals (this.locationReference));
        }

        /**
         * Processes a potential browser event on the region. Allows for the layout to
         * respond to events.
         * 
         * @param event
         *              the candidate event.
         * @return {@code true} if the event occurred within the region.
         */
        public boolean onBrowserEvent(Event event) {
            if ((event == null) || (event.target == null))
                return false;
            try {
                Element target = Js.cast (event.target);
                if ((el != null) && (target != null) && DomSupport.isChildOf (target, el)) {
                    try {
                        layout.onBrowserEvent (event);
                    } catch (Throwable e) {
                        Logger.error ("Uncaught exception on event passed to layout:", e);
                    }
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * Sets the layout target element.
         * 
         * @param target
         *               the layout target.
         */
        public void setElement(Element target) {
            this.el = target;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.IContainerRegion#setLayout(com.effacy.jui.core.client.container.layout.StyleLayout)
         */
        @Override
        public void setLayout(ILayout layout) {
            if (this.layout != null)
                this.layout.setLayoutTarget (null);
            this.layout = layout;
            if (layout != null)
                layout.setLayoutTarget (this);
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.IContainerRegion#getLayout()
         */
        @Override
        public ILayout getLayout() {
            // If there is no layout then we set to assign the default.
            if (layout == null)
                setLayout (regionDefaultLayout.create ());
            return layout;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.IContainerRegion#layout(boolean)
         */
        @Override
        public boolean layout(boolean force) {
            if ((el == null) || !getLayout ().layout (force))
                return false;
            return true;
        }

        /**
         * Determines if the region point contains the passed component (or one of its
         * components contains it).
         * 
         * @param component
         *                  the component to test for.
         * @return {@code true} if it is contained.
         */
        public boolean contains(IComponent component) {
            for (IComponent item : getItems ()) {
                if (item == null)
                    continue;
                if (item == component)
                    return true;
                if (item instanceof Component) {
                    if (((Component<?>) item).contains (component))
                        return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.layout.ILayoutTarget#getItems()
         */
        @Override
        public List<IComponent> getItems() {
            return items;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.layout.ILayoutTarget#getItemAt(int)
         */
        @Override
        public IComponent getItemAt(int index) {
            return ((index >= 0) && (index < items.size ())) ? items.get (index) : null;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.layout.ILayoutTarget#isRendered()
         */
        @Override
        public boolean isRendered() {
            return (rootEl != null);
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.layout.ILayoutTarget#getLayoutTarget()
         */
        @Override
        public elemental2.dom.Element getLayoutTarget() {
            return Js.cast (el);
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.layout.ILayoutTarget#onLayoutExecuted()
         */
        @Override
        public void onLayoutExecuted() {
            attach ();
            Component.this.onRegionLayoutExecuted (this);
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.layout.ILayoutTarget#onLayoutComplete(boolean)
         */
        @Override
        public void onLayoutComplete(boolean firstLayout) {
            Component.this.onRegionLayoutComplete (this, firstLayout);
        }

        /**
         * Called when a child component (or layout extension of the child component)
         * has resized.
         * <p>
         * By default this forces a reconfigure of the container.
         * 
         * @param child
         *              the child component that has been resized.
         */
        protected void onChildResize(IComponent child) {
            // fireEvent (new ComponentEvent.Resize (AbstractContainer.this));
            reconfigure ();
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.IContainerRegion#add(com.effacy.jui.core.client.component.IComponent)
         */
        @Override
        public <D extends IComponent> D add(D component) {
            return add (component, null);
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.IContainerRegion#add(com.effacy.jui.core.client.component.IComponent,
         *      com.effacy.jui.core.client.container.layout.LayoutData)
         */
        @Override
        public <D extends IComponent> D add(D component, LayoutData layoutData) {
            insert (component, -1, layoutData);
            return component;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.layout.IContainerRegion#indexOf(com.effacy.jui.core.client.component.IComponent)
         */
        @Override
        public int indexOf(IComponent component) {
            return items.indexOf (component);
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.container.IContainerRegion#insert(com.effacy.jui.core.client.component.IComponent,
         *      int, com.effacy.jui.core.client.container.layout.LayoutData)
         */
        @Override
        public boolean insert(IComponent component, int index, LayoutData layoutData) {
            if (component == null) {
                Logger.error ("Attempt to insert a NULL component!");
                return false;
            }

            // Ensure that layout data is set on the component.
            if (layoutData != null)
                component.setLayoutData (layoutData);

            // Modify the index so that it is in range.
            if (items.isEmpty ()) {
                index = 0;
            } else {
                index = index % (items.size () + 1);
                if (index < 0)
                    index = items.size () + 1 + index;
            }

            // Correct the index if the component is already a child.
            int idx = indexOf (component);
            if (idx != -1) {
                if (idx < index)
                    index--;
            }

            // Remove the component from its current parent.
            component.parent (this);

            // Ensure that the component is removed from the DOM.
            if (component.getRoot () != null)
                component.getRoot ().remove ();

            // Add the item and alert.
            items.add (index, component);
            onInsert (component, index, this);
            if (component instanceof Component)
                ((Component<?>) component).onAddedTo (Component.this);

            component.addListener (componentListener);
            // fireEvent (IContainerListener.class).onAdd (null, component,
            // index);
            fireEvent (IAddRemoveListener.class).onAdd (Component.this, component);

            // Mark as needing a layout and invoke.
            if ((rootEl != null) && layoutOnChange) {
                if (layout == null)
                    setLayout (regionDefaultLayout.create ());
                layout.markAsDirty ();
                reconfigure ();
            }
            return true;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.component.layout.IContainerRegion#disposeAll()
         */
        @Override
        public void disposeAll() {
            if (items.isEmpty ())
                return;

            // We need to drop into a new list as the items will be removed from the list on
            // disposal.
            blockReconfigure = true;
            for (IComponent component : new ArrayList<> (items)) {
                try {
                    component.dispose ();
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            }
            blockReconfigure = false;

            // Indicate that a layout is needed and perform the layout if
            // rendered.
            if ((rootEl != null) && layoutOnChange) {
                if (layout == null)
                    setLayout (regionDefaultLayout.create ());
                layout.markAsDirty ();
                reconfigure ();
            }
        }

        /**
         * Blocks a reconfigure. Used when performing a bulk disposal of children.
         */
        private boolean blockReconfigure = false;

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.IComponent.IParent#orhpan(com.effacy.jui.core.client.component.IComponent)
         */
        @Override
        public void orhpan(IComponent child) {
            if ((child != null) && items.contains (child)) {
                onBeforeRemove (child, this);
                items.remove (child);
                if (componentListener != null)
                    child.removeListener (componentListener);
                fireEvent (IAddRemoveListener.class).onRemove (Component.this, child);
                onRemove (child, this);

                // Perform a re-layout on the orphaned child.
                if (!blockReconfigure && (rootEl != null) && layoutOnChange) {
                    if (layout == null)
                        setLayout (regionDefaultLayout.create ());
                    layout.markAsDirty ();
                    reconfigure ();
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.IComponent.IParent#isAttached()
         */
        @Override
        public boolean isAttached() {
            return Component.this.isAttached ();
        }

        /**
         * Attaches the items in the region.
         */
        public void attach() {
            for (IComponent item : getItems ()) {
                if (item.getRoot () != null) {
                    if (item instanceof Component)
                        ((Component<?>) item).attach ();
                }
            }
        }

        /**
         * Detaches the items in the region.
         */
        public void detach() {
            for (IComponent item : getItems ()) {
                if (item.getRoot () != null) {
                    if (item instanceof Component)
                        ((Component<?>) item).detach ();
                }
            }
        }

    }

    /*******************************************************************************
     * UI lifecycle (widget related)
     *******************************************************************************/

    /**
     * The parent of the component (where the component has a parent).
     */
    private IParent parent;

    /**
     * Components that are managed by this component but indirectly.
     */
    private Set<IComponent> managedComponents;

    /**
     * Adopts all the components from the passed context.
     * 
     * @param ctx
     *            the context to adopt from.
     */
    protected void adopt(NodeContext ctx) {
        // If there is no replacement key then we simply adopt.
        ctx.forEachLodgement (object -> {
            if (object instanceof IComponent)
                adopt (((IComponent) object));
        });
        return;
    }

    /**
     * Adopts the passed component treating this as it's parent.
     * <p>
     * Normally this will be done automatically with the attachment points and
     * region points. If not using one of those mechanisms then this should be used.
     * 
     * @param cpt
     *            the component to adopt.
     */
    protected void adopt(IComponent cpt) {
        if (cpt == null)
            return;
        if (contains (cpt))
            return;
        if (managedComponents == null)
            managedComponents = new HashSet<> ();
        if (managedComponents.contains (cpt))
            return;
        managedComponents.add (cpt);
        // If it is a control then we need to register it as a control.
        if (cpt instanceof IControl)
            registerControl ((IControl<?>) cpt);
        cpt.parent (this);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#parent(com.effacy.jui.core.client.component.IComponent.IParent)
     */
    @Override
    public void parent(IParent parent) {
        orphan ();
        if (parent != null) {
            this.parent = parent;
            if (parent.isAttached ())
                attach ();
            if (Debug.isTestMode() && isRendered()) {
                getRoot().setAttribute("test-id", getTestId ());
                getRoot().setAttribute("test-cpt", getComponentId ());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent#getParent()
     */
    @Override
    public IParent getParent() {
        return parent;
    }

    /**
     * Orphan this component from its parent and detach.
     * 
     * @see com.effacy.jui.core.client.component.IComponent#orphan()
     */
    @Override
    public void orphan() {
        if (parent != null) {
            parent.orhpan (this);
            parent = null;

            // Detach if attached (which will cascade through the children).
            if (isAttached ())
                detach ();

            // Remove listeners.
            removeAllListeners ();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent.IParent#orhpan(com.effacy.jui.core.client.component.IComponent)
     */
    @Override
    public void orhpan(IComponent child) {
        if (child == null)
            return;
        if (regions != null)
            regions.forEach (rp -> rp.orhpan (child));
        if (attachments != null)
            attachments.forEach (ap -> ap.orhpan (child));
        if ((managedComponents != null) && managedComponents.contains (child))
            managedComponents.remove (child);
        if ((controls != null) && (child instanceof IControl))
            controls.remove ((IControl<?>) child);
    }

    /**
     * See {@link #isAttached()}.
     */
    private boolean attached = false;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.IComponent.IParent#isAttached()
     */
    @Override
    public boolean isAttached() {
        return attached;
    }

    /**
     * Attaches this component to the event mechanism.
     * <p>
     * This will not run if the component is already attached. Initially this will
     * associate the root element of the component with the UI event dispatcher (see
     * {@link EventLifecycle#setEventListener(Element, IEventListener)}).
     */
    public final void attach() {
        if (!isRendered () || isAttached ())
            return;

        // Associate the event listener.
        this.attached = true;
        EventLifecycle.register (getRoot (), this);

        // Ensure the children are attached.
        try {
            if (attachments != null)
                attachments.forEach (ap -> ap.attach ());
            if (regions != null)
                regions.forEach (rp -> rp.attach ());
            if (managedComponents != null) {
                managedComponents.forEach (cpt -> {
                    if (cpt instanceof Component)
                        ((Component<?>) cpt).attach ();
                });
            }
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }

        // Ensure the resize listener is created and lodged.
        if (monitorWindowResize)
            setMonitorWindowResize (true);

        // Ensure the scroll listener is created and lodged.
        if (monitorWindowScroll)
            setMonitorWindowScroll (true);

        // Reconfigure.
        // reconfigure ();

        onAttach ();
    }

    /**
     * Called from {@link #attach()} as a hook.
     * <p>
     * When called the component will be full attached to the event handling
     * mechanism.
     */
    protected void onAttach() {
        // Nothing.
    }

    /**
     * Detaches the component.
     */
    public final void detach() {
        if (!isAttached ())
            return;
        try {
            if (attachments != null)
                attachments.forEach (ap -> ap.detach ());
            if (regions != null)
                regions.forEach (rp -> rp.detach ());
            if (managedComponents != null) {
                managedComponents.forEach (cpt -> {
                    if (cpt instanceof Component)
                        ((Component<?>) cpt).detach ();
                });
            }
        } catch (Throwable e) {
            Logger.reportUncaughtException (e);
        }

        // Remove event listener.
        if (getRoot () != null)
            EventLifecycle.deregister (getRoot ());

        // Clean up handlers.
        if (windowResizeHandler != null) {
            windowResizeHandler.remove ();
            windowResizeHandler = null;
        }
        if (windowScrollHandler != null) {
            windowScrollHandler.remove ();
            windowScrollHandler = null;
        }

        // Mark as no longer attached.
        attached = false;

        // Invoke the hook.
        onDetach ();
    }

    /**
     * Invoked by {@link #detach()} as a hook.
     * <p>
     * When called the component will be fully detached from the event handling
     * mechanism. At this point the component can be safely disposed of.
     */
    protected void onDetach() {
        // Nothing.
    }

    /**
     * This fully disposes of the component (includeing removal from the DOM).
     *
     * @see com.effacy.jui.core.client.IDisposable#dispose()
     */
    @Override
    public void dispose() {
        // Invoke the event (needs to be done here as orphan clears listeners).
        try {
            fireEvent (IDisposeListener.class).onDispose (this);
        } catch (Throwable e) {
            Logger.error ("Exception while notifying of disposal (" + this + ")", e);
        }

        // Orphan from any parent (and detach).
        orphan();

        // Dispose of all the children.
        disposeChildren ((List<IComponent>) null);

        // Clear out all the handlers.
        if (uiHandlers != null) {
            uiHandlers.forEach (handler -> handler.dispose ());
            uiHandlers.clear ();
        }

        // Dispose of the element.
        if  (getRoot () != null)
            getRoot ().remove ();

        // Invoke the hook.
        try {
            onDispose ();
        } catch (Throwable e) {
            Logger.error ("Exception while executing onDispose() (" + this + ")", e);
        }

        if (DebugMode.LIFECYCLE.set ())
            Logger.trace ("[cpt]", "{dispose} [" + toString() + "]");
    }

    /**
     * Diposes of all the children (child components) that reside under the given
     * element (that is, rendered under).
     * 
     * @param el
     *           the element to dispose the children under.
     */
    protected void disposeChildren(Element el) {
        if (managedComponents != null) {
            new ArrayList<> (managedComponents).forEach (cpt -> {
                if ((cpt.getRoot () != null) &&  (el.equals (cpt.getRoot ()) || (el.contains (cpt.getRoot ())))) {
                    if (DebugMode.RENDER.set())
                        Logger.trace ("[cpt]", "{dispose-child} [" + toString() + "] child=" + cpt.toString ());
                    cpt.dispose ();
                }
            });
        }
    }

    /**
     * Disposes of all the children (child components).
     * <p>
     * This includes removal of the children as managed components.
     * 
     * @param exclude
     *                (optional) collection of components to exclude from disposal.
     */
    protected void disposeChildren(Collection<IComponent> exclude) {
        // Dispose of all the children.
        forEach (c -> {
            if ((exclude == null) || !exclude.contains (c))
                c.dispose ();
        });
        if (attachments != null)
            attachments.clear ();
        if (regions != null)
            regions.clear ();
        if (managedComponents != null)
            managedComponents.clear ();
        if (controls != null)
            controls.dispose ();
    }

    /**
     * Invoked from {@link #dispose()} when the component has been disposed of.
     */
    protected void onDispose() {
        // Nothing.
    }

    /*******************************************************************************
     * Debug.
     *******************************************************************************/

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String className = getClass ().getName ();
        int i = className.lastIndexOf('.');
        if (i >= 0)
            className = className.substring (i + 1);
        if (config == null)
            return className + "::" + uuid;
        return className + "::" + uuid + "::" + config.getTestId();
    }    

    /*******************************************************************************
     * Binding a component to the DOM (for root-level components).
     *******************************************************************************/

    @Override
    public Element bind(String elementId, boolean clear) {
        if (parent != null)
            throw new RuntimeException ("Cannot bind a parented component");
        RootBinding binding = BINDINGS.get (elementId);
        if (binding == null) {
            Element el = DomGlobal.document.getElementById (elementId);
            if (el == null)
                throw new RuntimeException ("Unable to find bind taget with ID \"" + elementId + "\"");
            if (clear)
                DomSupport.removeAllChildren (el);
            binding = new RootBinding (el);
            BINDINGS.put (elementId, binding);
        }
        binding.add (this);
        return binding.el;
    }

    /**
     * Collection of root-level DOM bindings.
     */
    protected static Map<String, RootBinding> BINDINGS = new HashMap<> ();
    static {
        // The following cannot be run in test mode.
        if (!Debug.isUnitTestMode ()) {
            EventLifecycle.registerWindowCloseEvent ((e-> {
                BINDINGS.values ().forEach (b -> b.dispose ());
                BINDINGS.clear ();
            }));
        }
    }

    /**
     * Represents a binding to the DOM to which components can be added.
     * <p>
     * See {@link Component#bind(String, boolean)} (where it is employed).
     */
    static class RootBinding implements IParent {

        /**
         * Element the binding is bound to (being the root DOM element for the portion
         * of the application residing under this binding).
         */
        private Element el;

        /**
         * Child components of the binding.
         */
        private List<IComponent> children = new ArrayList<> ();

        /**
         * Construct with element to bind to.
         * 
         * @param el
         *           the element.
         */
        RootBinding(Element el) {
            this.el = el;
        }

        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.core.client.component.IComponent.IParent#getTestId()
         */
        @Override
        public String getTestId() {
            return "";
        }

        /**
         * Disposes of this root binding.
         */
        void dispose() {
            // Dispose of the contents.
            children.forEach (c -> {
                try {
                    if (c instanceof Component)
                        ((Component<?>) c).detach ();
                } catch (Throwable e) {
                    // Not to worry.
                }
            });
            children.clear ();
        }

        /**
         * Adds a component into the root binding for rendering or display.
         * 
         * @param <C>
         *            component type.
         * @param cpt
         *            the component to add.
         * @return the passed component.
         */
        public <C extends IComponent> C add(C cpt) {
            if ((cpt != null) && !children.contains (cpt)) {
                children.add (cpt);
                if (cpt.getRoot () != null)
                    el.appendChild (cpt.getRoot ());
                else
                    cpt.render (el, 0);
                cpt.parent (this);
            }
            return cpt;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.IComponent.IParent#orhpan(com.effacy.jui.core.client.component.IComponent)
         */
        @Override
        public void orhpan(IComponent child) {
            if ((child != null) && children.contains (child)) {
                children.remove (child);
                if (child.getRoot () != null)
                    el.removeChild (child.getRoot ());
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.component.IComponent.IParent#isAttached()
         */
        @Override
        public boolean isAttached() {
            // Always deemed attached.
            return true;
        }

    }

}
