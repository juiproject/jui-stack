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
package com.effacy.jui.core.client.dom.renderer.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlBuilder;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.IDomSelectable;
import com.effacy.jui.core.client.dom.IDomSelector;
import com.effacy.jui.core.client.dom.IUIEventHandler;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;
import com.effacy.jui.core.client.dom.renderer.template.Provider.CastingProvider;
import com.effacy.jui.core.client.dom.renderer.template.Provider.ConstantProvider;
import com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node.EventBinding;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.platform.util.client.With;

/**
 * Allows for the construction of a DOM-oriented template that builds nodes
 * responsive to passed data (conditionally and content).
 * <p>
 * One generally commences with a {@link Container} over a given data type then
 * constructs within that container a nesting of various {@link Node} types with
 * relevant data-based controls. The result is that the container can render DOM
 * in response to a specific instance of the configuration data.
 * <p>
 * Once a template has been build it can be converted to a {@link IDataRenderer}
 * by calling {@link Node#renderer()}. Internally, when rendering, a
 * {@link BuildContext} is created and that is used to enact the DOM
 * construction (this is build using {@link SafeHtml} rather than direct DOM
 * construct as is the case with {@link DomBuilder}, this affords some
 * advantages as well as disadvantages but both are mostly substitutable). This
 * context is returned as a {@link IDomSelector} to allows for extraction of
 * marked nodes and as a {@link IUIEventHandler} for event management. The first
 * perspective operates the same as {@link DomBuilder} but the second is more
 * subtle. With a {@link DomRenderer} the build in which events are declared are
 * localised to the calling instance so event handlers can (via closure) access
 * the calling component (or context as applicable). For templates the build
 * generally occurs once and the result cached for re-user (see
 * {@link ITemplateBuilder#renderer(String, ITemplateBuilder)} for an example).
 * In this case the handlers will be detached from the rendering component. With
 * this in mind one needs to rely on the {@link UIEvent#getSource()} to gain
 * access to the correct context in which to handle events.
 * <p>
 * For ad-hoc or highly contextualised building one should use
 * {@link DomBuilder} but in cases where the construction of a structure is time
 * consuming (i.e. from a template) this this is more appropriate.
 * 
 * @author Jeremy Buckley
 */
public final class TemplateBuilder {

    public static class BuildContext implements IUIEventHandler, IDomSelector, IDomSelectable {

        /**
         * Binding or an event registration (by sub-class) to a node.
         */
        public class EventHandler extends EventBinding {

            /**
             * The node being bound to.
             */
            private elemental2.dom.Node node;

            /**
             * Construct with the registration (which contains the handler and event data)
             * and the node.
             * 
             * @param registration
             *                     the registration.
             * @param node
             *                     the node being bound to.
             */
            public EventHandler(EventBinding registration, elemental2.dom.Node node) {
                super (registration);
                this.node = node;
                if (node instanceof elemental2.dom.Element)
                    registration.events.forEach (e -> e.attach ((elemental2.dom.Element) node));
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
                    elemental2.dom.Element targetEl = e.getTarget ();
                    if (targetEl.equals (node) || DomSupport.isChildOf (targetEl, node))
                        return true;
                    return false;
                } catch (ClassCastException ex) {
                    Logger.error ("TemplateBuilder ClassCastException on match (" + e.getEventLabel () + " " + e.getTarget () + ")");
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
        }

        /**
         * When an HTML builder is used this is the attribute to store a reference
         * against.
         */
        public static final String LOOKUP_ATTR = "_ref";

        /**
         * When an HTML builder is used this is the attribute to store a reference
         * against.
         */
        public static final String BINDING_ATTR = "_bind";

        /**
         * When building from HTML.
         */
        private SafeHtmlBuilder builder;

        /**
         * The node being built into.
         */
        private elemental2.dom.Element parent;

        /**
         * Collection of node references.
         */
        private Map<String, List<elemental2.dom.Node>> references;

        /**
         * Collection of event handlers.
         */
        private List<EventHandler> handlers;

        /**
         * Collection of bindings.
         */
        private Map<String, List<EventBinding>> bindings;

        /**
         * Auxillary handlers that have been declared.
         */
        private List<IUIEventHandler> uiHandlers;

        /**
         * Construct with a parent node to build into.
         * 
         * @param parent
         *                   the parent node.
         * @param similarKey
         *                   key for testing for similarity.
         */
        public BuildContext(elemental2.dom.Element parent) {
            this.parent = parent;
            this.builder = new SafeHtmlBuilder ();
        }

        /**
         * Obtains the safe HTML builder.
         * <p>
         * This is a bit historical in that the original template builder built into a
         * string and the applied to the root node as its inner HTML. At the time that
         * was a very efficient way of building DOM (especially on IE). It is not clear
         * whether this is still the best way to go.
         * 
         * @return the builder.
         */
        public SafeHtmlBuilder builder() {
            return builder;
        }

        /**
         * Finishes the building process and ensure the parent node is populated.
         * 
         * @return this build context.
         */
        public BuildContext finish() {
            parent.innerHTML = builder.toSafeHtml ().asString ();
            builder = null;

            // Resolve any references.
            if ((references != null) || (bindings != null)) {
                // Traverse the DOM and pickup the references.
                DomSupport.traverse (parent, n -> {
                    if (n.nodeType == 1) {
                        if (references != null) {
                            String ref = ((elemental2.dom.Element) n).getAttribute (LOOKUP_ATTR);
                            if ((ref != null) && references.containsKey (ref))
                                references.get (ref).add (n);
                        }
                        if (bindings != null) {
                            String ref = ((elemental2.dom.Element) n).getAttribute (BINDING_ATTR);
                            if ((ref != null) && bindings.containsKey (ref)) {
                                for (EventBinding binding : bindings.get (ref)) {
                                    if (handlers == null)
                                        handlers = new ArrayList<> ();
                                    handlers.add (new EventHandler (binding, n));
                                }
                                EventBinding.sort (handlers);
                            }
                        }
                    }
                });
            }
            if (bindings != null) {
                bindings.clear ();
                bindings = null;
            }

            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelectable#select(com.effacy.jui.core.client.dom.IDomSelector)
         */
        @Override
        public void select(IDomSelector selector) {
            if (uiHandlers != null) {
                uiHandlers.forEach (h -> {
                    if (h instanceof IDomSelectable)
                        ((IDomSelectable) h).select (selector);
                });
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
         */
        @Override
        public boolean handleEvent(UIEvent event) {
            if (uiHandlers != null) {
                for (IUIEventHandler handler : uiHandlers) {
                    if (handler.handleEvent (event))
                        return true;
                }
            }
            if ((handlers == null) || handlers.isEmpty ())
                return false;
            for (EventHandler handler : handlers) {
                if (handler.handle (event))
                    return true;
            }
            return false;
        }

        /**
         * Registers a UI event handler that will be delegated to by the context.
         * 
         * @param handler
         *                the handler.
         */
        public void register(IUIEventHandler handler) {
            if (handler == null)
                return;
            if (uiHandlers == null)
                uiHandlers = new ArrayList<> ();
            if (!uiHandlers.contains (handler))
                uiHandlers.add (handler);
        }

        /**
         * Registers a node.
         * 
         * @param reference
         *                  the reference for the node.
         * @param node
         *                  the node (if {@code null} the a lookup is done on finish).
         */
        public void register(String reference) {
            if (reference == null)
                return;
            if (references == null)
                references = new HashMap<> ();
            List<elemental2.dom.Node> nodes = references.get (reference);
            if (nodes == null) {
                nodes = new ArrayList<> ();
                references.put (reference, nodes);
            }
        }

        /**
         * Registers a reference against some event bindings.
         * 
         * @param reference
         *                  the reference.
         * @param bindings
         *                  the bindings to associate with it.
         */
        public void registerBindings(String reference, List<EventBinding> bindings) {
            if (reference == null)
                return;
            if ((bindings == null) || bindings.isEmpty ())
                return;
            if (this.bindings == null)
                this.bindings = new HashMap<> ();
            this.bindings.put (reference, bindings);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelector#first(java.lang.String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public <E extends elemental2.dom.Node> E first(String reference) {
            if ((reference == null) || (references == null))
                return null;
            List<elemental2.dom.Node> nodes = references.get (reference);
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
        public <E extends elemental2.dom.Node> List<E> all(String reference) {
            if ((reference == null) || (references == null))
                return null;
            List<E> nodes = (List<E>) references.get (reference);
            if (nodes == null)
                return new ArrayList<> ();
            return nodes;
        }

    }

    /**
     * A context when looping a node over a data structure. Here the node is
     * repeatedly rendered but for each datum in the loop. The loop context carries
     * additional information about the loop that can be used as additional
     * configuration.
     *
     * @author Jeremy Buckley
     */
    public static class LoopContext {

        /**
         * See {@link #getIndex()}.
         */
        private int index;

        /**
         * See {@link #getLength()}.
         */
        private int length;

        /**
         * Construct a loop context with the current loop data.
         * 
         * @param index
         *               the index in the loop (from <code>0</code>).
         * @param length
         *               the total number of items in the loop.
         */
        public LoopContext(int index, int length) {
            this.index = index;
            this.length = length;
        }

        /**
         * The loop index (starting from 0).
         * 
         * @return the index.
         */
        public int getIndex() {
            return index;
        }

        /**
         * The total number of items being looped over.
         * 
         * @return the length.
         */
        public int getLength() {
            return length;
        }

        /**
         * Determines if this is the first item in the list.
         * 
         * @return {@code true} if it is.
         */
        public boolean isFirst() {
            return (index == 0);
        }

        /**
         * Determines if this is the last item in the list.
         * 
         * @return {@code true} if it is.
         */
        public boolean isLast() {
            return (index == (length - 1));
        }

        /**
         * Determines if this is and even item (note that this is based on the first
         * item being <code>1</code> rather that its index, which starts at
         * <code>0</code>).
         * 
         * @return {@code true} if it is.
         */
        public boolean isEven() {
            return (index + 1) % 2 == 0;
        }
    }

    /**
     * Implements a condition for looping.
     */
    public interface LoopCondition {

        public boolean testLoop(LoopContext loop);

        public static LoopCondition first() {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.isFirst ();
                }
            };
        }

        public static LoopCondition notFirst() {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return !loop.isFirst ();
                }
            };
        }

        public static LoopCondition last() {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.isLast ();
                }
            };
        }

        public static LoopCondition notLast() {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return !loop.isLast ();
                }
            };
        }

        public static LoopCondition even() {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.isEven ();
                }
            };
        }

        public static LoopCondition odd() {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return !loop.isEven ();
                }
            };
        }

        /**
         * Where the index is greater than or equal to the given index.
         * 
         * @param index
         *              the test index.
         * @return the condition.
         */
        public static LoopCondition ge(final int index) {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.index >= index;
                }
            };
        }

        /**
         * Where the index is greater than to the given index.
         * 
         * @param index
         *              the test index.
         * @return the condition.
         */
        public static LoopCondition gt(final int index) {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.index > index;
                }
            };
        }

        /**
         * Where the index is less than or equal to the given index.
         * 
         * @param index
         *              the test index.
         * @return the condition.
         */
        public static LoopCondition le(final int index) {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.index <= index;
                }
            };
        }

        /**
         * Where the index is less than to the given index.
         * 
         * @param index
         *              the test index.
         * @return the condition.
         */
        public static LoopCondition lt(final int index) {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.index < index;
                }
            };
        }

        /**
         * Where the index is equal to the given index.
         * 
         * @param index
         *              the test index.
         * @return the condition.
         */
        public static LoopCondition eq(final int index) {
            return new LoopCondition () {

                @Override
                public boolean testLoop(LoopContext loop) {
                    if (loop == null)
                        return false;
                    return loop.index == index;
                }
            };
        }
    }

    /**
     * Creates a container to build into.
     * 
     * @param <A>
     *            the configuration data type.
     * @return the container.
     */
    public static <A> Container<A> container() {
        return new Container<A> ();
    }

    /**
     * The base class for implementing a DOM-like generation structure.
     */
    public static abstract class Node<A> {

        /**
         * Encapsulates an event handler bound to the node.
         */
        public static class EventBinding {

            /**
             * The events that will trigger the handler.
             */
            protected List<UIEventType> events = new ArrayList<> ();

            /**
             * The event handler.
             */
            protected BiConsumer<UIEvent, elemental2.dom.Node> handler;

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
             *                specified).
             * @param events
             *                the events to match.
             */
            public EventBinding(BiConsumer<UIEvent, elemental2.dom.Node> handler, int order, UIEventType... events) {
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

        }

        /**
         * This can be used to mark a node for ease of debugging.
         */
        protected String debug;

        /**
         * Condition to determine if the node should be rendered.
         */
        protected Condition<A> condition;

        /**
         * Condition related to any operating loop.
         */
        protected LoopCondition loopCondition;

        /**
         * To be invoked on build of the node (a type of hook).
         */
        protected Consumer<BuildContext> buildHandler;

        /**
         * Registers a build handler that is invoked when the node is being built.
         * <p>
         * Only one can be assigned at any time.
         * 
         * @param buildHandler
         *                     the handler.
         * @return this node.
         */
        public Node<A> onBuild(Consumer<BuildContext> buildHandler) {
            this.buildHandler = buildHandler;
            return this;
        }

        /**
         * Marks the node with some debug information. This can be used while tracing.
         * 
         * @param marker
         *               the marker.
         * @return this instance.
         */
        public Node<A> debug(String marker) {
            this.debug = marker;
            return this;
        }

        /**
         * Assigns a condition to the node. The condition must evaluate to {@code true}
         * for the node to render into the DOM.
         * 
         * @param condition
         *                  the condition.
         * @return this node.
         */
        public Node<A> condition(Condition<A> condition) {
            this.condition = condition;
            return (Node<A>) this;
        }

        /**
         * Assigns a loop condition to the node. The condition must evaluate to
         * {@code true} for the node to render into the DOM.
         * 
         * @param condition
         *                  the condition.
         * @return this node.
         */
        public Node<A> loopCondition(LoopCondition loopCondition) {
            this.loopCondition = loopCondition;
            return (Node<A>) this;
        }

        /**
         * Render the node into the HTML builder.
         * 
         * @param builder
         *                the builder for HTML.
         * @param data
         *                data that may be used to guide rendering.
         */
        public final void render(BuildContext context, A data, LoopContext loop) {
            if ((condition != null) && !condition.test (data))
                return;
            if ((loop != null) && (loopCondition != null) && !loopCondition.testLoop (loop))
                return;
            renderImplControl (context, data, loop);
            if (buildHandler != null)
                buildHandler.accept (context);
        }

        /**
         * Allows a sub-class to control actual rending.
         * <p>
         * This is called by {@link #render(SafeHtmlBuilder, Object, LoopContext)} after
         * any node based condition is evaluated. The default is to call
         * {@link #renderImpl(SafeHtmlBuilder, Object, LoopContext)}. This allows for an
         * intermediary call allowing for an additional control point.
         * 
         * @param builder
         *                the builder to write HTML to.
         * @param data
         *                the passed data to used a configuration.
         * @param loop
         *                and looping context (passed when looping over data).
         */
        protected void renderImplControl(BuildContext context, A data, LoopContext loop) {
            renderImpl (context, data, loop);
        }

        /**
         * Render the node into the HTML builder.
         * 
         * @param context
         *                the builder context for HTML.
         * @param data
         *                data that may be used to guide rendering.
         * @param loop
         *                and looping context (passed when looping over data).
         */
        protected abstract void renderImpl(BuildContext context, A data, LoopContext loop);

        /**
         * Converts the node to a renderer.
         * 
         * @return the renderer.
         */
        public IDataRenderer<A> renderer() {
            return new IDataRenderer<A> () {

                @Override
                public IUIEventHandler render(elemental2.dom.Element el, A data) {
                    BuildContext context = new BuildContext (el);
                    Node.this.render (context, data, null);
                    return context.finish ();
                }

            };
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            if (debug != null)
                return super.toString ();
            return "[" + debug + "]";
        }

    }

    /**
     * A node that renders content (aka a DOM text node).
     */
    public static class Text<A> extends Node<A> {

        /**
         * The content to render (see the constructor).
         */
        private Provider<String, A> content;

        /**
         * See {@link #html()}.
         */
        private Condition<A> html;

        /**
         * See {@link #linize()}.
         */
        private Condition<A> linize;

        /**
         * Construct a text node with a provider that generates the content to render.
         * 
         * @param store
         *                the template context.
         * @param content
         *                the content provider.
         */
        public Text(Provider<String, A> content) {
            this.content = (Provider<String, A>) content;
        }

        /**
         * Marks the content to be converted to lines of text where each line is
         * separated by a <code>BR</code> element. See
         * {@link SafeHtmlBuilder#appendEscapedLines(String)}.
         * 
         * @return this text node.
         */
        public Text<A> linize() {
            return linize (ConditionBuilder.trueCondition ());
        }

        /**
         * See {@link #linize()} but uses a condition to trigger that treatment.
         * 
         * @param condition
         *                  the condition.
         * @return this text node.
         */
        public Text<A> linize(Condition<A> condition) {
            this.linize = condition;
            return this;
        }

        /**
         * Marks the content to be rendered as HTML (so is not escaped). See
         * {@link SafeHtmlBuilder#appendHtmlConstant(String)}. Care needs to be taken
         * here to ensure that no user-generated content is rendered unchecked. If so
         * then this could provide a point to inject HTML code that poses a security
         * risk (i.e. injecting javascript).
         * 
         * @return this text node.
         */
        public Text<A> html() {
            return html (ConditionBuilder.trueCondition ());
        }

        /**
         * See {@link #html()} but uses a condition to trigger that treatment.
         * 
         * @param condition
         *                  the condition.
         * @return this text node.
         */
        public Text<A> html(Condition<A> condition) {
            this.html = condition;
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node#renderImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.BuildContext,
         *      java.lang.Object,
         *      com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext)
         */
        @Override
        public void renderImpl(BuildContext context, A data, LoopContext loop) {
            if (content != null) {
                if ((html != null) && html.test (data))
                    context.builder ().appendHtmlConstant (StringSupport.safe (content.get (data, loop)));
                else if ((linize != null) && linize.test (data))
                    context.builder ().appendEscapedLines (StringSupport.safe (content.get (data, loop)));
                else
                    context.builder ().appendEscaped (StringSupport.safe (content.get (data, loop)));
            }
        }

    }

    /**
     * A type of node that contains other nodes (and provides methods to create
     * nodes corresponding to DOM elements).
     * <p>
     * The default is that it generates no content itself and defers to its children
     * to create content (through sub-classes may change this behaviour). It also
     * provides the ability to be looped. Looping requires a provider that extracts
     * a list of items to loop over.
     */
    public static class Container<A> extends Node<A> {

        /**
         * Carries loop meta-data around a datum.
         */
        public static class LoopData<V> {

            /**
             * See {@link #data()}.
             */
            private V data;

            /**
             * See {@link #index()}.
             */
            private int index;

            /**
             * See {@link #size()}.
             */
            private int size;

            /**
             * Construct with initial data.
             * 
             * @param data
             *              see {@link #data()}.
             * @param index
             *              see {@link #index()}.
             * @param size
             *              see {@link #size()}.
             */
            public LoopData(V data, int index, int size) {
                this.data = data;
                this.index = index;
                this.size = size;
            }

            /**
             * The datum being adorned.
             * 
             * @return the datum.
             */
            public V data() {
                return data;
            }

            /**
             * The loop index (starting from 0).
             * 
             * @return the index.
             */
            public int index() {
                return index;
            }

            /**
             * The total number of elements being looped over.
             * 
             * @return the total.
             */
            public int size() {
                return size;
            }

            /**
             * Indicates whether this is the first item in the loop.
             * 
             * @return {@code true} if it is.
             */
            public boolean first() {
                return (index == 0);
            }

            /**
             * Indicates whether this is the last item in the loop.
             * 
             * @return {@code true} if it is.
             */
            public boolean last() {
                return (index == (size - 1));
            }

            /**
             * Indicates whether this is an odd item (determined by {@link #index()}).
             * 
             * @return {@code true} if it is.
             */
            public boolean odd() {
                return (index % 2 == 1);
            }

            /**
             * Indicates whether this is an even item (determined by {@link #index()}).
             * 
             * @return {@code true} if it is.
             */
            public boolean even() {
                return !odd ();
            }
        }

        /**
         * See {@link #loop(Provider)} and {@link #loopWithData(Provider)}.
         */
        private Provider<List<Object>, A> looper;

        /**
         * See {@link #loopWithData(Provider)}. Means that loop meta data should be
         * added to (aka encapsulted with) the data being looped.
         */
        private boolean looperWithData = false;

        /**
         * Collection of children to be rendered (sequentially).
         */
        private List<Node<?>> children;

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node#renderImplControl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.BuildContext,
         *      java.lang.Object,
         *      com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext)
         */
        protected void renderImplControl(BuildContext context, A data, LoopContext loop) {
            // We take advantage of the control point to implement looping. We
            // loop over the items in the loop and render via renderImpl.
            if (looper != null) {
                int idx = 0;
                List<Object> items = looper.get (data, loop);
                if (items != null) {
                    for (Object item : items) {
                        if (looperWithData)
                            renderImpl (context, new LoopData<Object> (item, idx, items.size ()), new LoopContext (idx, items.size ()));
                        else
                            renderImpl (context, item, new LoopContext (idx, items.size ()));
                        idx++;
                    }
                }
            } else
                renderImpl (context, data, loop);
        }

        /**
         * Warnings("unchecked"){@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node#renderImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.BuildContext,
         *      java.lang.Object,
         *      com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext)
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected void renderImpl(BuildContext context, Object data, LoopContext loop) {
            // Any sub-class will adorn this method according. Here we just loop
            // over each child and let each render itself.
            if (children != null) {
                for (Node node : children) {
                    if (node == null)
                        continue;
                    node.render (context, data, loop);
                }
            }
        }

        /**
         * Makes this node loop over each item in the list as provided by the passed
         * provider.
         * 
         * @param looper
         *               a provider that extracts a list of items from the data to loop
         *               over.
         */
        @SuppressWarnings("unchecked")
        public <B> Container<B> loop(Provider<List<B>, A> looper) {
            this.looper = (Provider<List<Object>, A>) (Object) looper;
            return (Container<B>) this;
        }

        /**
         * Makes this node loop over each item in the list as provided by the passed
         * provider.
         * 
         * @param looper
         *               a provider that extracts a list of items from the data to loop
         *               over.
         */
        @SuppressWarnings("unchecked")
        public <B> Container<LoopData<B>> loopWithData(Provider<List<B>, A> looper) {
            this.looper = (Provider<List<Object>, A>) (Object) looper;
            this.looperWithData = true;
            return (Container<LoopData<B>>) this;
        }

        /**
         * Creates a child container. This can be used for grouping elements together
         * for control purposes (i.e. looping or conditionals).
         * 
         * @return the container.
         */
        public Container<A> container() {
            return insert (new Container<A> ());
        }

        /**
         * Inserts a container that includes a data conversion from the type of this
         * container to a different type (as specified by the converter).
         * 
         * @param <B>
         * @param converter
         *                  the data conversion.
         * @return the inserted container of the different data type.
         */
        public <B> Container<B> container(final Function<A, B> converter) {
            // Adds a container that includes a data conversion.
            final Container<B> container = new Container<B> ();
            insert (new Node<A> () {

                @Override
                protected void renderImpl(BuildContext context, A data, LoopContext loop) {
                    container.render (context, converter.apply (data), loop);
                }

            });
            return container;
        }

        /**
         * Inserts a child node (at the end if the child list).
         * 
         * @param <N>
         *              the type of node.
         * @param child
         *              the child to add.
         * @return the added child.
         */
        public <N extends Node<A>> N insert(N child) {
            if (children == null)
                children = new ArrayList<Node<?>> ();
            children.add (child);
            return child;
        }

        /**
         * Inserts a {@link Text} node with constant contents.
         * 
         * @param contents
         *                 the contents.
         * @return the inserted node.
         */
        public Text<A> text(String contents) {
            return text (new ConstantProvider<String, A> (contents));
        }

        /**
         * Inserts a {@link Text} node with html contents.
         * 
         * @param contents
         *                 the contents.
         * @return the inserted node.
         */
        public Text<A> html(String contents) {
            return text (contents).html ();
        }

        /**
         * Inserts a {@link Text} node with html contents.
         * 
         * @param contents
         *                 the contents.
         * @return the inserted node.
         */
        public Text<A> html(Provider<String, A> contents) {
            return text (contents).html ();
        }

        /**
         * Inserts a {@link Text} node with provided contents.
         * 
         * @param contents
         *                 the contents provider.
         * @return the inserted node.
         */
        public Text<A> text(Provider<String, A> contents) {
            return insert (new Text<A> (contents));
        }

        /**
         * Inserts a {@link Text} node with provided contents (as safe html).
         * 
         * @param contents
         *                 the contents provider.
         * @return the inserted node.
         */
        public Text<A> safeHtml(final Provider<SafeHtml, A> contents) {
            Text<A> text = new Text<A> (CastingProvider.<SafeHtml, String, A>create (contents, v -> (v == null) ? "" : v.asString ()));
            text.html ();
            return insert (text);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> h1() {
            return insert (new Element<A> ("h1"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> h1(Consumer<Element<A>> configurer) {
            return With.$ (h1 (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> h2() {
            return insert (new Element<A> ("h2"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> h2(Consumer<Element<A>> configurer) {
            return With.$ (h2 (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> h3() {
            return insert (new Element<A> ("h3"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> h3(Consumer<Element<A>> configurer) {
            return With.$ (h3 (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> h4() {
            return insert (new Element<A> ("h4"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> h4(Consumer<Element<A>> configurer) {
            return With.$ (h4 (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> h5() {
            return insert (new Element<A> ("h5"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> h5(Consumer<Element<A>> configurer) {
            return With.$ (h5 (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> h6() {
            return insert (new Element<A> ("h6"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> h6(Consumer<Element<A>> configurer) {
            return With.$ (h6 (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> div() {
            return insert (new Element<A> ("div"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> div(Consumer<Element<A>> configurer) {
            return With.$ (div (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> label() {
            return insert (new Element<A> ("label"));
        }

        /**
         * Inserts a standard label DOM element containing the passed text (a
         * convenience method that avoids having to separately insert a text node into
         * the label node).
         * 
         * @return the element.
         */
        public Element<A> label(String text) {
            Element<A> el = insert (new Element<A> ("label"));
            el.text (text);
            return el;
        }

        /**
         * Inserts a standard label DOM element containing the passed text (a
         * convenience method that avoids having to separately insert a text node into
         * the label node).
         * 
         * @return the element.
         */
        public Element<A> label(Provider<String, A> text) {
            Element<A> el = insert (new Element<A> ("label"));
            el.text (text);
            return el;
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> label(Consumer<Element<A>> configurer) {
            return With.$ (label (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> span() {
            return insert (new Element<A> ("span"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> span(Consumer<Element<A>> configurer) {
            return With.$ (span (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> p() {
            return insert (new Element<A> ("p"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> p(Consumer<Element<A>> configurer) {
            return With.$ (p (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> u() {
            return insert (new Element<A> ("u"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> u(Consumer<Element<A>> configurer) {
            return With.$ (u (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> i() {
            return insert (new Element<A> ("i"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> em() {
            return insert (new Element<A> ("em"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> em(Consumer<Element<A>> configurer) {
            return With.$ (em (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> img() {
            return insert (new Element<A> ("img"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param src
         *            the source URL for the image.
         * @return the element.
         */
        public Element<A> img(Provider<String, A> src) {
            Element<A> img = insert (new Element<A> ("img"));
            if (src != null)
                img.setAttribute ("src", src);
            return img;
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> a() {
            Element<A> el = insert (new Element<A> ("a"));
            el.setAttribute ("href", "javascript:;");
            return el;
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> a(Consumer<Element<A>> configurer) {
            return With.$ (a (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> strong() {
            return insert (new Element<A> ("strong"));
        }

        /**
         * Inserts a standard anchor DOM element with the given HREF attribute.
         * 
         * @param href
         *             the href (i.e. url).
         * @return the element.
         */
        public Element<A> a(String href) {
            return a (href, null);
        }

        /**
         * Inserts a standard anchor DOM element with the given HREF attribute and
         * target attribute.
         * 
         * @param href
         *               the href (i.e. url).
         * @param target
         *               the target attribute.
         * @return the element.
         */
        public Element<A> a(String href, String target) {
            Element<A> el = insert (new Element<A> ("a"));
            el.setAttribute ("href", href);
            if (target != null)
                el.setAttribute ("target", target);
            return el;
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> sub() {
            return insert (new Element<A> ("sub"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> sup() {
            return insert (new Element<A> ("sup"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> ul() {
            return insert (new Element<A> ("ul"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> ul(Consumer<Element<A>> configurer) {
            return With.$ (ul (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> li() {
            return insert (new Element<A> ("li"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> li(Consumer<Element<A>> configurer) {
            return With.$ (li (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> table() {
            return insert (new Element<A> ("table"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> table(Consumer<Element<A>> configurer) {
            return With.$ (table (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> thead() {
            return insert (new Element<A> ("thead"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> thead(Consumer<Element<A>> configurer) {
            return With.$ (thead (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> tbody() {
            return insert (new Element<A> ("tbody"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> tbody(Consumer<Element<A>> configurer) {
            return With.$ (tbody (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> tfoot() {
            return insert (new Element<A> ("tfoot"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> tfoot(Consumer<Element<A>> configurer) {
            return With.$ (tfoot (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> tr() {
            return insert (new Element<A> ("tr"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> tr(Consumer<Element<A>> configurer) {
            return With.$ (tr (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> th() {
            return insert (new Element<A> ("th"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> th(Consumer<Element<A>> configurer) {
            return With.$ (th (), configurer);
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> td() {
            return insert (new Element<A> ("td"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> td(Consumer<Element<A>> configurer) {
            return With.$ (td (), configurer);
        }

        /**
         * Inserts a standard DOM input element.
         * 
         * @param type
         *             the input type.
         * @return the element.
         */
        public Element<A> input(String type) {
            Element<A> el = insert (new Element<A> ("input"));
            el.setAttribute ("type", type);
            return el;
        }

        /**
         * Inserts a standard DOM input element.
         * 
         * @param type
         *             the input type.
         * @return the element.
         */
        public Element<A> input(Provider<String, A> type) {
            Element<A> el = insert (new Element<A> ("input"));
            el.setAttribute ("type", type);
            return el;
        }

        /**
         * Inserts a standard DOM textarea element.
         * 
         * @return the element.
         */
        public Element<A> textarea() {
            Element<A> el = insert (new Element<A> ("textarea"));
            return el;
        }

        /**
         * Inserts a standard DOM textarea element.
         * 
         * @param rows
         *             the number of rows (will only be applied if greater than 0).
         * @param cols
         *             the number of cols (will only be applied if greater than 0).
         * @return the element.
         */
        public Element<A> textarea(int rows, int cols) {
            Element<A> el = insert (new Element<A> ("textarea"));
            if (rows > 0)
                el.setAttribute ("rows", "" + rows);
            if (cols > 0)
                el.setAttribute ("cols", "" + cols);
            return el;
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @return the element.
         */
        public Element<A> button() {
            return insert (new Element<A> ("button"));
        }

        /**
         * Inserts a standard DOM element.
         * 
         * @param configurer
         *                   a configurer for the newly created element.
         * @return the element.
         */
        public Element<A> button(Consumer<Element<A>> configurer) {
            return With.$ (button (), configurer);
        }
    }

    /**
     * Represents a standard DOM element.
     */
    public static class Element<A> extends Container<A> {

        /**
         * The name of the element tag.
         */
        private String tag;

        /**
         * Collection of CSS class name providers.
         */
        private List<Provider<String, A>> klasses;

        /**
         * Collection of attribute-value pair providers.
         */
        private Map<String, Provider<String, A>> attributes;

        /**
         * Collection of css property-value pair providers.
         */
        private Map<String, Provider<String, A>> css;

        /**
         * Used to generate a reference for the node to return back post-render.
         */
        protected String reference;

        /**
         * Events bound to this element.
         */
        private List<EventBinding> events;

        /**
         * Construct with the tag name.
         * 
         * @param context
         *                the template context.
         * @param tag
         *                the tag name.
         */
        public Element(String tag) {
            this.tag = tag;
        }

        /**
         * This is a special case to simply sink UI events on the element without
         * specifying a handler. Handling is done by the component.
         * 
         * @param events
         *               the events to sink.
         * @return this builder instance.
         */
        public Element<A> on(UIEventType... events) {
            return on ((Consumer<UIEvent>) null, -1, events);
        }

        /**
         * Register an event handler.
         * 
         * @param handler
         *                the handler (takes an event).
         * @param events
         *                the events to associated with.
         * @return this builder instance.
         */
        public Element<A> on(Consumer<UIEvent> handler, UIEventType... events) {
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
         * @return this builder instance.
         */
        public Element<A> on(Consumer<UIEvent> handler, int order, UIEventType... events) {
            return on ((e, n) -> handler.accept (e), order, events);
        }

        /**
         * Register an event handler.
         * 
         * @param handler
         *                the handler (takes an event and the node that the listener is
         *                associated with).
         * @param events
         *                the events to associated with.
         * @return this builder instance.
         */
        public Element<A> on(BiConsumer<UIEvent, elemental2.dom.Node> handler, UIEventType... events) {
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
         * @return this builder instance.
         */
        public Element<A> on(BiConsumer<UIEvent, elemental2.dom.Node> handler, int order, UIEventType... events) {
            if (this.events == null)
                this.events = new ArrayList<> ();
            this.events.add (new EventBinding (handler, order, events));
            return this;
        }

        /**
         * Generate a lookup reference for the node so that it can be returned post
         * render (see {@link IDomSelector}).
         * 
         * @param reference
         *                  the reference to apply.
         * @return this node instance.
         */
        public Element<A> by(String reference) {
            this.reference = reference;
            return this;
        }

        /**
         * Casts return type to {@link Element}.
         */
        public <B> Element<B> loop(Provider<List<B>, A> looper) {
            return (Element<B>) super.loop (looper);
        }

        /**
         * Casts return type to {@link Element}.
         * 
         * @param looper
         *                   the looper.
         * @param configurer
         *                   to configure the resultant element.
         */
        public <B> Element<B> loop(Provider<List<B>, A> looper, Consumer<Element<B>> configurer) {
            Element<B> el = (Element<B>) super.loop (looper);
            if (configurer != null)
                configurer.accept (el);
            return el;
        }

        /**
         * Casts return type to {@link Element}.
         */
        @SuppressWarnings("unchecked")
        public <B> Element<LoopData<B>> loopWithData(Provider<List<B>, A> looper) {
            return (Element<LoopData<B>>) super.loop (looper);
        }

        /**
         * Casts return type to {@link Element}.
         * 
         * @param looper
         *                   the looper.
         * @param configurer
         *                   to configure the resultant element.
         */
        public <B> Element<LoopData<B>> loopWithData(Provider<List<B>, A> looper, Consumer<Element<LoopData<B>>> configurer) {
            Element<LoopData<B>> result = (Element<LoopData<B>>) super.loopWithData (looper);
            if (configurer != null)
                configurer.accept (result);
            return result;
        }

        /**
         * Adds one or more class names to render into the <code>class</code> attribute.
         * 
         * @param <E>
         *                   the element type (allows for sub-classing).
         * @param classNames
         *                   the class names to render.
         * @return this element instance.
         */
        @SuppressWarnings("unchecked")
        public <E extends Element<A>> E addClassName(String... classNames) {
            for (String className : classNames) {
                if (StringSupport.empty (className))
                    continue;
                if (klasses == null)
                    klasses = new ArrayList<Provider<String, A>> ();
                klasses.add (new ConstantProvider<String, A> (className));
            }
            return (E) this;
        }

        /**
         * Adds a class name to render into the <code>class</code> attribute.
         * 
         * @param <E>
         *                  the element type (allows for sub-classing).
         * @param className
         *                  the class name to render.
         * @param condition
         *                  a condition to apply.
         * @return this element instance.
         */
        @SuppressWarnings("unchecked")
        public <E extends Element<A>> E addClassName(String className, Condition<A> condition) {
            if (StringSupport.empty (className))
                return (E) this;
            if (klasses == null)
                klasses = new ArrayList<Provider<String, A>> ();
            klasses.add (new ConstantProvider<String, A> (className).condition (condition));
            return (E) this;
        }

        /**
         * Adds a class name (via provider) to render into the <code>class</code>
         * attribute.
         * 
         * @param <E>
         *              the element type (allows for sub-classing).
         * @param klass
         *              the class name to render (via provider).
         * @return this element instance.
         */
        public <E extends Element<A>> E addClassName(Provider<String, A> klass) {
            return addClassName (klass, null);
        }

        /**
         * Adds a class name (via provider) to render into the <code>class</code>
         * attribute.
         * 
         * @param <E>
         *                   the element type (allows for sub-classing).
         * @param klass
         *                   the class name to render (via provider).
         * @param (optional)
         *                   configurer a function to configure further the klass
         *                   provider (i.e. adding conditions).
         * @return this element instance.
         */
        @SuppressWarnings("unchecked")
        public <E extends Element<A>, P extends Provider<String, A>> E addClassName(P klass, Consumer<P> configurer) {
            if (klass != null) {
                if (klasses == null)
                    klasses = new ArrayList<Provider<String, A>> ();
                klasses.add (klass);
                if (configurer != null)
                    configurer.accept (klass);
            }
            return (E) this;
        }

        /**
         * Assigns a value to the <code>item</code> attribute.
         * 
         * @param <E>
         *             the element type (allows for sub-classing).
         * @param item
         *             the value of the item.
         * @return this element instance.
         */
        public <E extends Element<A>> E item(String item) {
            return setAttribute ("item", item);
        }

        /**
         * Assigns a value to the <code>item</code> attribute (via a provider).
         * 
         * @param <E>
         *             the element type (allows for sub-classing).
         * @param item
         *             the value of the item (as a provider).
         * @return this element instance.
         */
        public <E extends Element<A>> E item(Provider<String, A> id) {
            return setAttribute ("item", id);
        }

        /**
         * Assigns a value to the <code>id</code> attribute.
         * 
         * @param <E>
         *            the element type (allows for sub-classing).
         * @param id
         *            the value of the id.
         * @return this element instance.
         */
        public <E extends Element<A>> E id(String id) {
            return setAttribute ("id", id);
        }

        /**
         * Assigns a value to the <code>id</code> attribute (via a provider).
         * 
         * @param <E>
         *            the element type (allows for sub-classing).
         * @param id
         *            the value of the id (as a provider).
         * @return this element instance.
         */
        public <E extends Element<A>> E id(Provider<String, A> id) {
            return setAttribute ("id", id);
        }

        /**
         * Assigns <code>true</code> to the <code>draggable</code> attribute.
         * 
         * @param <E>
         *            the element type (allows for sub-classing).
         * @return this element instance.
         */
        public <E extends Element<A>> E draggable() {
            return setAttribute ("draggable", "true");
        }

        /**
         * Assigns <code>true</code> to the <code>draggable</code> attribute.
         * 
         * @param condition
         *                  a test condition to apply.
         * @param <E>
         *                  the element type (allows for sub-classing).
         * @return this element instance.
         */
        public <E extends Element<A>> E draggable(Condition<A> condition) {
            ConstantProvider<String, A> cp = new ConstantProvider<String, A> ("true");
            cp.condition (condition);
            return setAttribute ("draggable", cp);
        }

        /**
         * Assigns a value to an attribute.
         * 
         * @param <E>
         *              the element type (allows for sub-classing).
         * @param attr
         *              the name of the attribute being set.
         * @param value
         *              the value of the attribute.
         * @return this element instance.
         */
        @SuppressWarnings("unchecked")
        public <E extends Element<A>> E setAttribute(String attr, String value) {
            if (value != null)
                return (E) setAttribute (attr, new ConstantProvider<String, A> (value));
            return (E) setAttribute (attr, (Provider<String, A>) null);
        }

        /**
         * Assigns a value to an attribute (via a provider).
         * 
         * @param <E>
         *              the element type (allows for sub-classing).
         * @param attr
         *              the name of the attribute being set.
         * @param value
         *              the value of the attribute (as a provider).
         * @return this element instance.
         */
        @SuppressWarnings("unchecked")
        public <E extends Element<A>> E setAttribute(String attr, Provider<String, A> value) {
            if (StringSupport.empty (attr))
                return (E) this;
            attr = attr.toLowerCase ();
            if (value == null) {
                if (attributes == null)
                    return (E) this;
                attributes.remove (attr);
                return (E) this;
            }
            if (attributes == null)
                attributes = new HashMap<String, Provider<String, A>> ();
            attributes.put (attr, (Provider<String, A>) value);
            return (E) this;
        }

        /**
         * See {@link #css(String, Provider)} but with a literal value.
         */
        public <E extends Element<A>> E css(String property, String value) {
            return css (property, (value == null) ? null : ProviderBuilder.string (value));
        }

        /**
         * Assigns a CSS style.
         * 
         * @param <E>
         *                 the element type (allows for sub-classing).
         * @param property
         *                 the CSS property (if the condition check fails the no
         *                 assignment is performed).
         * @param value
         *                 the value provider for the property.
         * @return this element instance.
         */
        @SuppressWarnings("unchecked")
        public <E extends Element<A>> E css(String property, Provider<String, A> value) {
            if (StringSupport.empty (property))
                return (E) this;
            if (value == null) {
                if (css == null)
                    return (E) this;
                css.remove (property);
                return (E) this;
            }
            if (css == null)
                css = new HashMap<String, Provider<String, A>> ();
            css.put (property, (Provider<String, A>) value);
            return (E) this;
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Node#condition(com.effacy.jui.core.client.dom.renderer.template.Condition)
         */
        @Override
        public Element<A> condition(Condition<A> condition) {
            // Cast up for convenience.
            return (Element<A>) super.condition (condition);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.Container#renderImpl(com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.BuildContext,
         *      java.lang.Object,
         *      com.effacy.jui.core.client.dom.renderer.template.TemplateBuilder.LoopContext)
         */
        @Override
        @SuppressWarnings("unchecked")
        public void renderImpl(BuildContext context, Object data, LoopContext loop) {
            SafeHtmlBuilder builder = context.builder ();
            builder.appendHtmlConstant ("<");
            builder.appendEscaped (tag);
            if ((this.klasses != null) && !klasses.isEmpty ()) {
                String classList = null;
                for (Provider<String, A> klass : klasses) {
                    if (!klass.test ((A) data))
                        continue;
                    if (!klass.testLoop (loop))
                        continue;
                    if (classList == null)
                        classList = klass.get ((A) data, loop);
                    else
                        classList += " " + klass.get ((A) data, loop);
                }
                if (classList != null) {
                    builder.appendHtmlConstant (" class=\"");
                    builder.appendEscaped (classList);
                    builder.appendHtmlConstant ("\"");
                }
            }
            if (attributes != null) {
                for (String attr : attributes.keySet ()) {
                    Provider<String, A> provider = attributes.get (attr);
                    if (provider == null)
                        continue;
                    if (!provider.test ((A) data))
                        continue;
                    if (!provider.testLoop (loop))
                        continue;
                    builder.appendHtmlConstant (" ");
                    builder.appendEscaped (attr);
                    builder.appendHtmlConstant ("=\"");
                    builder.appendEscaped (provider.get ((A) data, loop));
                    builder.appendHtmlConstant ("\"");
                }
            }
            if (css != null) {
                boolean started = false;
                for (String property : css.keySet ()) {
                    Provider<String, A> provider = css.get (property);
                    if (provider == null)
                        continue;
                    if (!provider.test ((A) data))
                        continue;
                    if (!provider.testLoop (loop))
                        continue;
                    String value = provider.get ((A) data, loop);
                    if (StringSupport.empty (value))
                        continue;
                    if (!started) {
                        builder.appendHtmlConstant (" style=\"");
                        started = true;
                    }
                    builder.appendEscaped (property);
                    builder.appendHtmlConstant (":");
                    builder.appendEscaped (value);
                    builder.appendHtmlConstant ("; ");
                }
                if (started)
                    builder.appendHtmlConstant ("\"");
            }
            if ((events != null) && !events.isEmpty ()) {
                String bindingRef = UID.createUID ();
                builder.appendHtmlConstant (" ");
                builder.appendEscaped (BuildContext.BINDING_ATTR);
                builder.appendHtmlConstant ("=\"");
                builder.appendEscaped (bindingRef);
                builder.appendHtmlConstant ("\"");
                context.registerBindings (bindingRef, events);
            }
            if (!StringSupport.empty (reference)) {
                builder.appendHtmlConstant (" ");
                builder.appendEscaped (BuildContext.LOOKUP_ATTR);
                builder.appendHtmlConstant ("=\"");
                builder.appendEscaped (reference);
                builder.appendHtmlConstant ("\"");
                context.register (reference);
            }
            builder.appendHtmlConstant (">");
            super.renderImpl (context, data, loop);
            builder.appendHtmlConstant ("</");
            builder.appendEscaped (tag);
            builder.appendHtmlConstant (">");
        }
    }
}
