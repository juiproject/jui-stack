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
package com.effacy.jui.core.client.observable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

public class ListenerOracle {

    /**
     * The oracle instance.
     */
    private static ListenerOracle ORACLE = (ListenerOracle) GWT.create (ListenerOracle.class);

    /**
     * The various registered factories.
     */
    private Map<Class<? extends IListener>, IListenerFactory<?>> factories = new HashMap<Class<? extends IListener>, IListenerFactory<?>> ();


    /**
     * Registers a listener factory and base listener class with the oracle.
     * 
     * @param factory
     *            the factory to register.
     * @param klass
     *            the class to register the factory under.
     */
    protected <L extends IListener> void register(IListenerFactory<L> factory, Class<L> klass) {
        factories.put (klass, factory);
    }


    /**
     * Given a listener class and a collection of listeners, this will return a
     * instance of the listener class that will fire events to other listeners
     * in the passed listener collection.
     * 
     * @param klass
     *            the listener class to locate.
     * @param listeners
     *            the listeners that will be sent invocations to.
     * @return The listener implementation that will dispatch the invocations.
     */
    public <L extends IListener> L find(Class<L> klass, Collection<IListener> listeners) {
        return find (klass, listeners, null);
    }


    /**
     * Given a listener class and a collection of listeners, this will return a
     * instance of the listener class that will fire events to other listeners
     * in the passed listener collection.
     * 
     * @param klass
     *            the listener class to locate.
     * @param listeners
     *            the listeners that will be sent invocations to.
     * @param debugString
     *            a debug string to display to the console (for tracking
     *            invocations).
     * @return The listener implementation that will dispatch the invocations.
     */
    @SuppressWarnings("unchecked")
    public <L extends IListener> L find(Class<L> klass, Collection<IListener> listeners, String debugString) {
        IListenerFactory<L> factory = (IListenerFactory<L>) factories.get (klass);
        if (factory == null)
            return null;
        return factory.createDispatcher (listeners, debugString);
    }

    /**
     * Interface for creating dispatcher listeners.
     */
    public interface IListenerFactory<L extends IListener> {

        /**
         * Returns a listener that when invoked will dispatch the invocation to
         * all matching listeners in the passed list.
         * 
         * @param listeners
         *            the listeners to dispatch to.
         * @param debugString
         *            an optional string to log to the console with debug
         *            messages.
         * @return The dispatcher.
         */
        public L createDispatcher(Collection<IListener> listeners, String debugString);
    }


    /**
     * Obtains an instance of the oracle.
     * 
     * @return The instance.
     */
    public static ListenerOracle instance() {
        return ORACLE;
    }
}
