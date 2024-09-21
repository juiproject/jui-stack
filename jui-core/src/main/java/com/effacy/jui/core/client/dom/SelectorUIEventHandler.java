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
package com.effacy.jui.core.client.dom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.effacy.jui.core.client.IDisposable;
import com.effacy.jui.core.client.dom.renderer.IDataRenderer;

import elemental2.dom.Element;

/**
 * Used to build up a composite UI event handler that includes support for
 * runtime binding.
 * <p>
 * This can be registered with the component OR can be returned by a renderer
 * (see {@link IDataRenderer}). The latter is particularly useful when dealing
 * with cached renderers and shared behaviours (where there is a coupling
 * between the cached condiguration and event handling).
 * 
 * @author Jeremy Buckley
 */
public class SelectorUIEventHandler implements IUIEventHandler, IDomSelectable, IDisposable {

    /**
     * Collection of sub-ordinate event handler wrapped up in this handler (for
     * delegation).
     */
    private List<IUIEventHandler> handlers = new ArrayList<> ();

    /**
     * Captures information to bind.
     */
    class Binding implements IUIEventHandler, IDomSelectable, IDisposable {
        /**
         * The nodes that have been selected.
         */
        private Set<Element> nodes = new HashSet<> ();

        /**
         * The selector to use when selecting elements.
         */
        private String reference;

        /**
         * The handler to process the events.
         */
        private Consumer<UIEvent> handler;

        /**
         * The event types being processed.
         */
        private Set<UIEventType> types = new HashSet<> ();

        Binding(String reference, Consumer<UIEvent> handler, UIEventType... events) {
            this.reference = reference;
            this.handler = handler;
            for (UIEventType event : events) {
                if (event != null)
                    types.add (event);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IDomSelectable#select(com.effacy.jui.core.client.dom.IDomSelector)
         */
        @Override
        public void select(IDomSelector selector) {
            nodes.clear ();
            nodes.addAll (selector.all (reference));
            nodes.forEach (n -> types.forEach (t -> t.attach (n)));
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.IDisposable#dispose()
         */
        @Override
        public void dispose() {
            nodes.clear ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
         */
        @Override
        public boolean handleEvent(UIEvent event) {
            if ((handler == null) || !types.contains (event.getType ()))
                return false;
            for (elemental2.dom.Node node : nodes) {
                if (DomSupport.isChildOf (event.getTarget (), node)) {
                    handler.accept (event);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Construct.
     */
    public SelectorUIEventHandler() {
        // Nothing.
    }

    /**
     * Construct with initial data.
     * 
     * @param selector
     *                 selector to select against (convenience to call
     *                 {@link #select(IDomSelector)}); this is an object to allow
     *                 for the interface to be an optional behaviour.
     * @param builder
     *                 to build out the bindings prior to selection.
     */
    public SelectorUIEventHandler(Object selector, Consumer<SelectorUIEventHandler> builder) {
        if (builder != null)
            builder.accept (this);
        if ((selector != null) && (selector instanceof IDomSelector))
            select ((IDomSelector) selector);
    }

    /**
     * Wraps a data renderer so that this becomes the returned
     * {@link IUIEventHandler}.
     * 
     * @param <D>
     * @param delegate
     *                 the delegate renderer to wrap.
     * @return the wrapped delegate.
     */
    public <D> IDataRenderer<D> wrap(IDataRenderer<D> delegate) {
        return new IDataRenderer<D> () {

            @Override
            public IUIEventHandler render(Element el, D data) {
                SelectorUIEventHandler.this.select ((IDomSelector) delegate.render (el, data));
                return SelectorUIEventHandler.this;
            }

        };
    }

    /**
     * Bind to the given event.
     * 
     * @param reference
     *                  the reference.
     * @param handler
     *                  the handler.
     * @param events
     *                  the events.
     * @return this handler.
     */
    public SelectorUIEventHandler bind(String reference, Consumer<UIEvent> handler, UIEventType... events) {
        handlers.add (new Binding (reference, handler, events));
        return this;
    }

    /**
     * Adds a UI handler to be delegated to.
     * 
     * @param handler
     *                the handler to add.
     * @return this instance.
     */
    public SelectorUIEventHandler add(IUIEventHandler handler) {
        if (handler != null)
            handlers.add (handler);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.IDomSelectable#select(com.effacy.jui.core.client.dom.IDomSelector)
     */
    @Override
    public void select(IDomSelector selector) {
        handlers.forEach (b -> {
            if (b instanceof IDomSelectable)
                ((IDomSelectable) b).select (selector);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.dom.IUIEventHandler#handleEvent(com.effacy.jui.core.client.dom.UIEvent)
     */
    @Override
    public boolean handleEvent(UIEvent event) {
        for (IUIEventHandler handler : handlers) {
            if (handler.handleEvent (event))
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IDisposable#dispose()
     */
    @Override
    public void dispose() {
        handlers.forEach (b -> {
            if (b instanceof IDisposable)
                ((IDisposable) b).dispose ();
        });
    }

}
