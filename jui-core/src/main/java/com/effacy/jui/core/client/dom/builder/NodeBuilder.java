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
package com.effacy.jui.core.client.dom.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.IDomSelector;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.platform.util.client.Logger;

import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.Node;

public abstract class NodeBuilder<T extends NodeBuilder<T>> implements IDomInsertable {

    /**
     * The return type from building a node.
     */
    public static class NodeContext implements INodeProvider, IUIEventHandler, IDomSelector, IDisposable {

        /**
         * The generated node.
         */
        private Node node;

        /**
         * The build context that has been populated.
         */
        private BuildContext ctx;

        /**
         * Construct with the node and context.
         * 
         * @param node
         *             the node.
         * @param ctx
         *             the context.
         */
        protected NodeContext(Node node, BuildContext ctx) {
            this.node = node;
            this.ctx = ctx;
        }

        /**
         * Obtains the node the heads the node context complex.
         * 
         * @return the node.
         */
        public Node node() {
            return node;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
         */
        @Override
        public boolean handleEvent(UIEvent event) {
            return ctx.handleEvent (event);
        }

        /**
         * Determines if this has UI event handler(s) registered.
         * 
         * @return {@code true} if it does.
         */
        public boolean doesHandleEvent() {
            return ctx.doesHandleEvent ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelector#first(java.lang.String)
         */
        @Override
        public <E extends Node> E first(String reference) {
            return ctx.first (reference);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelector#all(java.lang.String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <E extends Node> List<E> all(String reference) {
            return (List<E>) ctx.all (reference);
        }


        /**
         * Processes each lodgement.
         * 
         * @param processor
         *                  to process a lodgement.
         */
        public void forEachLodgement(Consumer<Object> processor) {
            if (ctx != null)
                ctx.forEachLodgement (processor);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.IDisposable#dispose()
         */
        @Override
        public void dispose() {
            if (ctx != null)
                ctx.dispose ();
            ctx = null;
            node = null;
        }

    }

    /**
     * Encapsulates an event handler bound to the node.
     */
    static class EventBinding implements IDisposable {

        /**
         * The events that will trigger the handler.
         */
        protected List<UIEventType> events = new ArrayList<> ();

        /**
         * The event handler.
         */
        protected BiConsumer<UIEvent, Node> handler;

        /**
         * Relative ordering.
         */
        protected int order = -1;

        /**
         * Copy constructor.
         * 
         * @param copy
         *             the registration to copy.
         */
        protected EventBinding(EventBinding copy) {
            this.events = copy.events;
            this.handler = copy.handler;
            this.order = copy.order;
        }

        /**
         * Construct with data about the event handler.
         * 
         * @param handler
         *                the handler.
         * @param order
         *                the ordering (-1 always at the end, otherwise as per
         *                specified). processed).
         * @param events
         *                the events to match.
         */
        public EventBinding(BiConsumer<UIEvent, Node> handler, int order, UIEventType... events) {
            this.handler = handler;
            this.order = Math.max (order, -1);
            for (UIEventType event : events) {
                if (event != null)
                    this.events.add (event);
            }
        }

        /**
         * Sorts the list of registrations based on their specified ordering.
         * 
         * @param registrations
         *                      the registrations to sort.
         */
        public static void sort(List<? extends EventBinding> registrations) {
            Collections.sort (registrations, (o1, o2) -> {
                if (o1.order == o2.order)
                    return 0;
                if (o1.order < 0)
                    return 1;
                if (o2.order < 0)
                    return -1;
                if (o1.order < o2.order)
                    return -1;
                return 1;
            });
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.IDisposable#dispose()
         */
        @Override
        public void dispose() {
            events.clear ();
            handler = null;
        }
    }

    /**
     * The build context.
     */
    public static class BuildContext implements IUIEventHandler, IDomSelector, IDisposable {

        /**
         * Binding or an event registration (by sub-class) to a node.
         */
        class EventHandler extends EventBinding {

            /**
             * The node being bound to.
             */
            private Node node;

            /**
             * Construct with the registration (which contains the handler and event data)
             * and the node.
             * 
             * @param registration
             *                     the registration.
             * @param node
             *                     the node being bound to.
             */
            public EventHandler(EventBinding registration, Node node) {
                super (registration);
                this.node = node;
                if (node instanceof Element)
                    registration.events.forEach (e -> e.attach ((Element) node));
            }

            /**
             * Determines if the passed event matches this handler. That means the event
             * type must be one of those that appear in the registration data and the target
             * node must be the node or a child of the node.
             * 
             * @param e
             *          the event to match against.
             * @return {@code true} if there is a match.
             */
            public boolean match(UIEvent e) {
                if ((e == null) || (node == null) || events.isEmpty ())
                    return false;
                if (!events.contains (e.getType ()))
                    return false;
                try {
                    Element targetEl = e.getTarget ();
                    if (targetEl.equals (node) || DomSupport.isChildOf (targetEl, node))
                        return true;
                    return false;
                } catch (ClassCastException ex) {
                    Event ev = e.getEvent();
                    String evDescription = (ev == null) ? "null event" : ev.toString();
                    Logger.error ("DomBuilder ClassCastException on match (" + e.getEventLabel () + "::" + evDescription + ")");
                    return false;
                }
            }

            /**
             * Attempts to handle the passed event.
             * <p>
             * A call to {@link #match(UIEvent)} will be made to determine if the event
             * should trigger the handler then the handler will be executed.
             * 
             * @param e
             *          the event to handle.
             * @return {@code true} if the event was handled (so does not need to
             *         propagate).
             */
            public boolean handle(UIEvent e) {
                if (!match (e))
                    return false;
                if (handler != null)
                    handler.accept (e, node);
                return true;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.core.client.dom.DomBuilder.NodeBuilder.EventBinding#dispose()
             */
            @Override
            public void dispose() {
                super.dispose ();
                node = null;
            }
        }

        /**
         * Collection of node references.
         */
        private Map<String, List<Node>> references;

        /**
         * Collection of event handlers.
         */
        private List<EventHandler> handlers;

        /**
         * Collection of additional UI handlers.
         */
        private List<IUIEventHandler> uiHandlers;

        /**
         * Objects that have been lodged for potential processing.
         */
        private List<Object> lodgements;

        /**
         * Lodges an object for protential processing by the called.
         * 
         * @param object
         *               the object to lodge.
         */
        void lodge(Object object) {
            if (object != null) {
                if (lodgements == null)
                    lodgements = new ArrayList<> ();
                lodgements.add (object);
            }
        }

        /**
         * Processes each lodgement.
         * 
         * @param processor
         *                  to process a lodgement.
         */
        public void forEachLodgement(Consumer<Object> processor) {
            if (lodgements != null)
                lodgements.forEach (processor);
        }

        /**
         * Registers a node under a given name.
         * 
         * @param reference
         *                  the reference name.
         * @param node
         *                  the node to reference.
         */
        void register(String reference, Node node) {
            if ((reference == null) || (node == null))
                return;
            if (references == null)
                references = new HashMap<> ();
            List<Node> nodes = references.get (reference);
            if (nodes == null) {
                nodes = new ArrayList<> ();
                references.put (reference, nodes);
            }
            nodes.add (node);
        }

        /**
         * Registers an event and handler against a node.
         * 
         * @param registration
         *                     the event and handler registration.
         * @param node
         *                     the node to associate the registration with.
         */
        void register(EventBinding registration, Node node) {
            if (handlers == null)
                handlers = new ArrayList<> ();
            handlers.add (new EventHandler (registration, node));
            EventBinding.sort (handlers);
        }

        /**
         * Registers an additional event handler.
         */
        void register(IUIEventHandler handler) {
            if (handler == null)
                return;
            if (uiHandlers == null)
                uiHandlers = new ArrayList<> ();
            uiHandlers.add (handler);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelector#first(java.lang.String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <E extends Node> E first(String reference) {
            if ((reference == null) || (references == null))
                return null;
            List<Node> nodes = references.get (reference);
            if ((nodes == null) || nodes.isEmpty ())
                return null;
            return (E) nodes.get (0);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelector#all(java.lang.String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <E extends Node> List<E> all(String reference) {
            if ((reference == null) || (references == null))
                return null;
            List<E> nodes = (List<E>) references.get (reference);
            if (nodes == null)
                return new ArrayList<> ();
            return nodes;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
         */
        @Override
        public boolean handleEvent(UIEvent event) {
            if ((handlers != null) && !handlers.isEmpty ()) {
                for (EventHandler handler : handlers) {
                    if (handler.handle (event))
                        return true;
                }
            }
            if ((uiHandlers != null) && !uiHandlers.isEmpty ()) {
                for (IUIEventHandler handler : uiHandlers) {
                    if (handler.handleEvent (event))
                        return true;
                }
            }
            return false;
        }

        /**
         * Determines if this handles UI events (so that is worth while registering it
         * as a UI event handler).
         * 
         * @return {@code true} if it does.
         */
        public boolean doesHandleEvent() {
            return ((handlers != null) && !handlers.isEmpty()) || ((uiHandlers != null) && !uiHandlers.isEmpty());
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.IDisposable#dispose()
         */
        @Override
        public void dispose() {
            if (handlers != null) {
                handlers.forEach (e -> e.dispose ());
                handlers.clear ();
                handlers = null;
            }
            if (references != null)
                references.clear ();
            references = null;
            if (lodgements != null)
                lodgements.clear ();
            lodgements = null;
        }
    }

    /**
     * Reference to record the generated node in the context.
     */
    protected String reference;

    /**
     * Events bound to this element.
     */
    protected List<EventBinding> events;

    /**
     * Executions bound to the node.
     */
    protected List<Consumer<Node>> executions;

    /**
     * See {@link #iff(boolean)}.
     */
    protected boolean condition = true;

    /**
     * A test condition for the node. The node will only be build if this condition
     * is {@code true}.
     * <p>
     * In general this is not widely used as one can achieve this using external
     * logic and construction using lambda-expressions with the various helper
     * classes. Where it does come in useful is introducing simple conditionals when
     * employing the simplified build model (not using lamda-expressions but
     * adding directly node builders). For example:
     * <tt>
     * P.$ (parent).$ (
     *   Em.$ ().styles (FontAwesome.user ()).iff (isPerson),
     *   Em.$ ().styles (FontAwesome.users ()).iff (!isPerson && isGroup),
     *   Text.$ (name);
     * )
     * </tt>
     * as opposed to:
     * <tt>
     * P.$ (parent).$ (para -> {
     *   if (isPerson)
     *     Em.$ (para).styles (FontAwesome.user ());
     *   else if (!isPerson && isGroup))
     *     Em.$ (para).styles (FontAwesome.users ());
     *   Text.$ (para, name);
     * });
     * </tt>
     * 
     * @param condition
     *                  condition for building the node.
     * @return this node instance.
     */
    @SuppressWarnings("unchecked")
    public T iff(boolean condition) {
        this.condition = condition;
        return (T) this;
    }

    /**
     * Convenience to act on oneself returning self (for chaining actions).
     * 
     * @param actions
     *                the actions to apply (as a consumer) to this instance.
     * @return this node instance.
     */
    @SuppressWarnings("unchecked")
    public T self(Consumer<T> actions) {
        if (actions != null)
            actions.accept((T) this);
        return (T) this;
    }

    /**
     * Register a reference to the DOM element that can be recovered from the
     * {@link NodeContext}.
     * 
     * @param reference
     *                  the reference to record the node against.
     * @return this builder.
     */
    @SuppressWarnings("unchecked")
    public T by(String reference) {
        this.reference = reference;
        return (T) this;
    }

    /**
     * Alternative name for {@link #by(String)}.
     */
    public T ref(String reference) {
        return by (reference);
    }

    /**
     * After the build, apply the passed execution code against this node this
     * builder built.
     * 
     * @param execution
     *                  the execution code to apply.
     * @return this node builder.
     */
    @SuppressWarnings("unchecked")
    public T apply(Consumer<Node> execution) {
        if (execution == null)
            return (T) this;
        if (this.executions == null)
            this.executions = new ArrayList<> ();
        this.executions.add (execution);
        return (T) this;
    }

    /**
     * Alternative name for {@link #apply(Consumer)}.
     */
    public T use(Consumer<Node> execution) {
        return apply (execution);
    }

    /**
     * Alternative name for {@link #apply(Consumer)}.
     */
    public T with(Consumer<Node> execution) {
        return apply (execution);
    }

    /**
     * Convenience to register an on-click handler (see
     * {@link #on(Consumer, UIEventType...)} pasing {@link UIEventType#ONCLICK}).
     * 
     * @param handler
     *                the handler.
     * @return this builder instance.
     */
    public T onclick(Consumer<UIEvent> handler) {
        return on (handler, UIEventType.ONCLICK);
    }

    /**
     * Convenience to register an on-click handler (see
     * {@link #on(BiConsumer, UIEventType...)} pasing {@link UIEventType#ONCLICK}).
     * 
     * @param handler
     *                the handler.
     * @return this builder instance.
     */
    public T onclick(BiConsumer<UIEvent, Node> handler) {
        return on (handler, UIEventType.ONCLICK);
    }

    /**
     * Convenience to register an on-change handler (see
     * {@link #on(Consumer, UIEventType...)} pasing {@link UIEventType#ONCHANGE}).
     * 
     * @param handler
     *                the handler.
     * @return this builder instance.
     */
    public T onchange(Consumer<UIEvent> handler) {
        return on (handler, UIEventType.ONCHANGE);
    }

    /**
     * Convenience to register an on-change handler (see
     * {@link #on(BiConsumer, UIEventType...)} pasing {@link UIEventType#ONCHANGE}).
     * 
     * @param handler
     *                the handler.
     * @return this builder instance.
     */
    public T onchange(BiConsumer<UIEvent, Node> handler) {
        return on (handler, UIEventType.ONCHANGE);
    }

    /**
     * This is a special case to simply sink UI events on the element without
     * specifying a handler. Handling is done by the component.
     * 
     * @param events
     *               the events to sink.
     * @return this builder instance.
     */
    public T on(UIEventType... events) {
        return on ((Consumer<UIEvent>) null, -1, events);
    }

    /**
     * Register an event handler.
     * 
     * @param handler
     *                the handler (takes an event).
     * @param events
     *                the events to associated with.
     * @return
     */
    public T on(Consumer<UIEvent> handler, UIEventType... events) {
        return on ((e, n) -> handler.accept (e), events);
    }

    /**
     * Register an event handler.
     * 
     * @param handler
     *                the handler (takes an event).
     * @param order
     *                a relative ordering to enable conflict resolution (anything
     *                less than 0 will automatically occur after any with a
     *                specified order).
     * @param events
     *                the events to associated with.
     * @return
     */
    public T on(Consumer<UIEvent> handler, int order, UIEventType... events) {
        return on ((e, n) -> {
            if (handler != null)
                handler.accept (e);
        }, order, events);
    }

    /**
     * Register an event handler.
     * 
     * @param handler
     *                the handler (takes an event and the node that the listener is
     *                associated with).
     * @param events
     *                the events to associated with.
     * @return
     */
    public T on(BiConsumer<UIEvent, Node> handler, UIEventType... events) {
        return on (handler, -1, events);
    }

    /**
     * Register an event handler.
     * 
     * @param handler
     *                the handler (takes an event and the node that the listener is
     *                associated with).
     * @param order
     *                a relative ordering to enable conflict resolution (anything
     *                less than 0 will automatically occur after any with a
     *                specified order).
     * @param events
     *                the events to associated with.
     * @return
     */
    @SuppressWarnings("unchecked")
    public T on(BiConsumer<UIEvent, Node> handler, int order, UIEventType... events) {
        if (this.events == null)
            this.events = new ArrayList<> ();
        this.events.add (new EventBinding (handler, order, events));
        return (T) this;
    }

    /**
     * See {@link IDomInsertable#insertInto(ContainerBuilder)}.
     */
    @Override
    public void insertInto(ContainerBuilder<?> parent) {
        parent.insertNode (this);
    }

    /**
     * Builds a responsive node which includes any registered event handlers.
     * 
     * @return the responsive node.
     */
    public NodeContext build() {
        return build (null);
    }

    /**
     * Builds a responsive node which includes any registered event handlers.
     * 
     * @return the responsive node.
     */
    public NodeContext build(Consumer<NodeContext> extractor) {
        BuildContext ctx = new BuildContext ();
        NodeContext nodeCtx = new NodeContext (_node (null, ctx), ctx);
        if (extractor != null) {
            try {
                extractor.accept (nodeCtx);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e);
            }
        }
        return nodeCtx;
    }

    /**
     * Performs a build and returns only the node.
     * <p>
     * For referencing nodes and capturing UI events call {@link #build()}.
     * 
     * @return the node.
     */
    public Node node() {
        return build ().node ();
    }

    /**
     * Builds the structure and returns the root node.
     * 
     * @param parent
     *               the parent node (if present)
     * @param ctx
     *               the context to register against as needed.
     * @return the root node of the structure.
     */
    public final Node _node(Node parent, BuildContext ctx) {
        if (!condition)
            return null;
        Node node = _nodeImpl (parent, ctx);
        if (node == null)
            return null;
        if (reference != null)
            ctx.register (reference, node);
        if (events != null) {
            for (EventBinding registration : events)
                ctx.register (registration, node);
        }
        if (this.executions != null)
            this.executions.forEach (h -> h.accept (node));
        return node;
    }

    /**
     * Called by {@link #_node(BuildContext)} to perform the actual node building
     * for the node and returns the root of the associated structure.
     * <p>
     * This is intended to be implemented by sub-classes.
     * 
     * @param ctx
     *            the context to register against as needed.
     * @return the root node of the structure.
     */
    protected abstract Node _nodeImpl(Node parent, BuildContext ctx);

}
