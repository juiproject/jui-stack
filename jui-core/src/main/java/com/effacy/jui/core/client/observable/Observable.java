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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.platform.util.client.Logger;

/**
 * Implementation of {@link IObservable} that makes use of the generic
 * {@link ListenerOracle} for event dispatch.
 * 
 * @author Jeremy Buckley
 */
public class Observable implements IObservable {

    /**
     * Listeners registered with the observable.
     */
    private List<IListener> listeners;

    /**
     * Observables to convey events to.
     */
    private List<ConveyObservable> observables;

    /**
     * If fire events should be debugged (written to console).
     */
    private String debug = null;

    /**
     * Iterate over each listener.
     * <p>
     * If you just want to know if listeners are present just pass {@code null} and
     * test the repsonse.
     * 
     * @param visitor
     *                to visit each listener.
     * @return {@code true} if there were listeners.
     */
    public boolean forEachListener(Consumer<IListener> visitor) {
        if ((visitor != null) && (listeners != null))
            listeners.forEach (visitor);
        return (listeners != null) && !listeners.isEmpty();
    }

    /**
     * Iterate over each observable.
     * <p>
     * If you just want to know if observables are present just pass {@code null} and
     * test the repsonse.
     * 
     * @param visitor
     *                to visit each observable.
     * @return {@code true} if there were observables.
     */
    public boolean forEachObservable(Consumer<ConveyObservable> visitor) {
        if ((visitor != null) && (observables != null))
            observables.forEach (visitor);
        return (observables != null) && !observables.isEmpty();
    }

    /**
     * Starts debugging the observable.
     */
    public void debugObservable(Class<?> owner) {
        this.debug = owner.toString ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#addListener(com.effacy.jui.core.client.observable.IListener)
     */
    @Override
    public <L extends IListener> L addListener(L listener) {
        if (listener == null)
            return null;
        if (listeners == null)
            listeners = new ArrayList<IListener> ();
        if (!listeners.contains (listener))
            listeners.add (listener);
        return listener;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#removeListener(com.effacy.jui.core.client.observable.IListener)
     */
    @Override
    public void removeListener(IListener... listeners) {
        if (this.listeners == null)
            return;
        for (IListener listener : listeners) {
            if (listener != null)
                this.listeners.remove (listener);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#removeAllListeners()
     */
    @Override
    public void removeAllListeners() {
        if (listeners != null)
            listeners.clear ();
        if (observables != null)
            observables.clear ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#convey(com.effacy.jui.core.client.observable.IObservable)
     */
    @Override
    @SafeVarargs
    @SuppressWarnings("unlikely-arg-type")
    public final void convey(IObservable observable, Class<? extends IListener>... listenerTypes) {
        if (observable == null)
            return;
        if (observables == null)
            observables = new ArrayList<ConveyObservable> ();
        int i = observables.indexOf (observable);
        if (i >= 0)
            observables.get (i).addListenerType (listenerTypes);
        else
            observables.add (new ConveyObservable (observable).addListenerType (listenerTypes));
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#removeObservable(com.effacy.jui.core.client.observable.IObservable[])
     */
    @Override
    @SuppressWarnings("unlikely-arg-type")
    public void removeObservable(IObservable... observables) {
        if (this.observables == null)
            return;
        for (IObservable observable : observables) {
            if (observable != null)
                this.observables.remove (observable);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.observable.IObservable#fireEvent(java.lang.Class)
     */
    @Override
    public <L extends IListener> L fireEvent(Class<L> listenerClass, IListener... listeners) {
        List<IListener> listenerCandidates = new ArrayList<IListener> ();
        if (this.listeners != null)
            listenerCandidates.addAll (this.listeners);
        for (IListener listener : listeners) {
            if (listener != null)
                listenerCandidates.add (listener);
        }
        if ((observables != null) && !observables.isEmpty ()) {
            for (ConveyObservable observable : observables) {
                L listener = observable.fireEvent (listenerClass);
                if (listener != null)
                    listenerCandidates.add (listener);
            }
        }
        if (debug != null)
            return ListenerOracle.instance ().find (listenerClass, listenerCandidates, debug);
        return ListenerOracle.instance ().find (listenerClass, listenerCandidates);
    }


    /**
     * Writes some debug information to the console.
     */
    protected void debugToConsole() {
        Logger.log ("Listeners: " + listeners.size ());
        for (IListener listener : listeners) {
            Logger.log ("   " + listener.getClass ().getName ());
        }
    }

    /**
     * Wraps an observable with an optional collection of listener classes that
     * can be conveyed to it.
     */
    private class ConveyObservable {

        /**
         * The observable.
         */
        private IObservable observable;

        /**
         * The (optional) list of filtering listener types.
         */
        private List<Class<? extends IListener>> listenerTypes;

        /**
         * Construct with an observable.
         * 
         * @param observable
         *            the observable.
         */
        public ConveyObservable(IObservable observable) {
            this.observable = observable;
            assert (observable != null) : "Must assign a non-null observable for converying to";
        }


        /**
         * Adds listeners to an observable.
         * 
         * @param listenerTypes
         *            the listener type to add.
         * @return This instance.
         */
        @SafeVarargs
        public final ConveyObservable addListenerType(Class<? extends IListener>... listenerTypes) {
            if (this.listenerTypes == null)
                this.listenerTypes = new ArrayList<Class<? extends IListener>> ();
            for (Class<? extends IListener> listenerType : listenerTypes) {
                if ((listenerType != null) && !this.listenerTypes.contains (listenerType))
                    this.listenerTypes.add (listenerType);
            }
            return this;
        }


        /**
         * Obtains a listener to convey events through.
         * 
         * @param listenerClass
         *            the listener class.
         * @return The listener (may be {@code null} if not permitted to
         *         convery).
         */
        @SafeVarargs
        public final <L extends IListener> L fireEvent(Class<L> listenerClass, L... listeners) {
            if ((listenerTypes == null) || listenerTypes.isEmpty () || listenerTypes.contains (listenerClass))
                return observable.fireEvent (listenerClass, listeners);
            return null;
        }


        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return observable.hashCode ();
        }


        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IObservable)
                return this.observable.equals (obj);
            return super.equals (obj);
        }

    }

}
