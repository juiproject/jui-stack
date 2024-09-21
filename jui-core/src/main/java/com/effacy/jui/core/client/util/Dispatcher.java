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
package com.effacy.jui.core.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.platform.util.client.Logger;

/**
 * Simple mechanism to declare handlers.
 *
 * @author Jeremy Buckley
 */
public class Dispatcher<V> {

    /**
     * Convenience to captire an event with a source and a value.
     */
    public static class Event<S,V> {

        /**
         * See {@link #source()}.
         */
        private S source;

        /**
         * See {@link #value()}.
         */
        private V value;

        /**
         * Construct with source and value.
         * 
         * @param source
         *               the source of the event.
         * @param value
         *               the value of the event.
         */
        public Event(S source, V value) {
            this.source = source;
            this.value = value;
        }

        /**
         * The source of the event.
         * 
         * @return the source.
         */
        public S source() {
            return source;
        }

        /**
         * The event value.
         * 
         * @return the value.
         */
        public V value() {
            return value;
        }
    }
    
    /**
     * See {@link #dispatch(Object, boolean)} but unforced.
     */
    public static <V> void dispatch(Dispatcher<V> dispatcher, V value) {
        Dispatcher.dispatch (dispatcher, value, false);
    }

    /**
     * Safely dispatches (i.e. deals with the possiblity that the dispatcher is
     * {@code null}).
     * 
     * @param dispatcher
     *                   the dispatcher (could be {@code null}).
     * @param value
     *                   the value to dispatch.
     * @param force
     *                   if to force a dispatch (even if the value appears not to
     *                   have changed).
     */
    public static <V> void dispatch(Dispatcher<V> dispatcher, V value, boolean force) {
        if (dispatcher != null)
            dispatcher.dispatch (value, force);
    }

    /**
     * Adds the handler to the dispatcher and creates the dispatcher if it is
     * {@code null}.
     * 
     * @param dispatcher
     *                   the dispatcher to add to (can be {@code null}).
     * @param handler
     *                   the handler to add.
     * @return the passed (or created) dispatcher.
     */
    public static <V> Dispatcher<V> add(Dispatcher<V> dispatcher, Consumer<V> handler) {
        if (handler == null)
            return dispatcher;
        if (dispatcher == null)
            dispatcher = new Dispatcher<> ();
        dispatcher.add (handler);
        return dispatcher;
    }

    /**
     * Registration handler.
     */
    public interface IRegistrationHandler {

        /**
         * Removes the handler.
         */
        public void remove();
    }

    /**
     * Collection of handlers to dispatch to.
     */
    private List<Consumer<V>> handlers;

    /**
     * The last dispatched value.
     */
    private V last;

    /**
     * Obtains the last dispatched value.
     * 
     * @return the value.
     */
    public V get() {
        return last;
    }

    /**
     * Adds a handler to the dispatcher.
     * 
     * @param handler
     *                the handler to add.
     * @return this dispatcher instance.
     */
    public Dispatcher<V> add(Consumer<V> handler) {
        if (handler == null)
            return this;
        if (handlers == null)
            handlers = new ArrayList<> ();
        handlers.add (handler);
        return this;
    }

    /**
     * See {@link #add(Consumer)} but returns an object that allows for the handler
     * to be removed.
     * 
     * @param handler
     *                the handler to add.
     * @return registration handler.
     */
    public IRegistrationHandler addWithHandler(Consumer<V> handler) {
        add (handler);
        return new IRegistrationHandler () {

            @Override
            public void remove() {
                if (handler != null)
                    handlers.remove (handler);
            }

        };
    }

    /**
     * Dispatch the value across the handlers, but only if the value has changed.
     * 
     * @param value
     *              the value to dispatch.
     */
    public void dispatch(V value) {
        dispatch(value, false);
    }

    /**
     * Dispatch the value across the handlers.
     * 
     * @param value
     *              the value to dispatch.
     * @param force
     *              {@code true} to force a dispatch even if the value has not
     *              changed.
     */
    public void dispatch(V value, boolean force) {
        if (handlers == null)
            return;
        if (!force && (last == value))
            return;
        last = value;
        handlers.forEach (h -> {
            try {
                h.accept (value);
            } catch (Throwable e) {
                Logger.reportUncaughtException (e);
            }
        });
    }

}
