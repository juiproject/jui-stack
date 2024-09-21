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
package com.effacy.jui.core.client.state;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.platform.util.client.Logger;

/**
 * Encapsulates a variable with additional state reflecting that the variable is
 * sourced remotely.
 * <p>
 * On creation the initial state is loading (so {@link #isLoading()} returns
 * {@code true}).
 */
public class StateVariable<V extends StateVariable<V>> implements IStateVariable<V> {

    /**
     * Debug mode for a state variable
     */
    protected boolean debug = false;

    /**
     * Listeners to receive changes in state.
     */
    private List<StateListenerHandler<V>> listeners = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public V debug() {
        this.debug = true;
        return (V) this;
    }

    /**
     * Notifies a modification that has occurred (and ensures correct handling of
     * any additional state).
     * <p>
     * This assumes that the modification has occurred internally (hence the method
     * is protected) and a modification only needs to be notified. Rather than
     * calling {@link #emit()} this ensures that any addtional scrutiny is performed
     * (by a sub-class) associated with a modification.
     */
    protected void modify() {
        modify (v -> {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void modify(Function<V,Boolean> modifier) {
        if (modifier != null) {
            Boolean result = modifier.apply ((V) this);
            if ((result == null) || result) {
                onModify ();
                if (debug)
                    Logger.log ("{state:modify} [" + toString () + "]");
                emit ();
            }
        }
    }

    /**
     * Called by {@link #modify(Consumer)} post modification but prior to emission.
     */
    protected void onModify() {
        // Nothing.
    }

    @Override
    public IStateListenerHandler<V> listen(Consumer<V> listener) {
        StateListenerHandler<V> handler = new StateListenerHandler<> (listener);
        this.listeners.add (handler);
        return handler;
    }

    /**
     * Invokes each listener as to a change in state.
     */
    protected void emit() {
        if (debug)
            Logger.log ("{state:emit} [" + toString () + "]");

        this.listeners.forEach (l -> {
            // We need to check for a null listener which can occur if a listener is removed
            // while processing the dispatch.
            if (l != null)
                l.onChange ();
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String className = getClass ().getName ();
        int i = className.lastIndexOf('.');
        if (i >= 0)
            className = className.substring (i + 1);
        return className;
    }

    /**
     * Used to passively monitor when changes occur.
     */
    class StateListenerHandler<T> implements IStateListenerHandler<T> {

        /**
         * See {@link #changedSinceLastCall()}.
         */
        private boolean updated = false;

        /**
         * See {@link #unblock(boolean)}.
         */
        private boolean updatedSinceBlocked = false;

        /**
         * See {@link #block()} and {@link #unblock()}.
         */
        private boolean block;

        /**
         * Listener to delegate to.
         */
        private Consumer<T> listener;

        /**
         * Construct with an optional listener to invoke on change.
         * 
         * @param listener
         *                 (optional) lister to invoke no change (and when not blocked).
         */
        StateListenerHandler(Consumer<T> listener) {
            this.listener = listener;
        }

        /**
         * Invoked when there is a change.
         */
        void onChange() {
            updated = true;
            updatedSinceBlocked = true;
            if (!block && (listener != null)) {
                if (debug)
                    Logger.log ("{state:listener-dispatch} [" + StateVariable.this.toString() + "]");
                try {
                    listener.accept (state ());
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e, StateVariable.this);
                }
            } else if (debug)
                Logger.log ("{state:listener-dispatch} BLOCKED [" + StateVariable.this.toString() + "]");
        }
        
        @Override
        public void remove() {
            listeners.remove (this);
        }

        @Override
        public boolean changedSinceLastCall() {
            if (updated) {
                updated = false;
                return true;
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        public T state() {
            return (T) StateVariable.this;
        }

        @Override
        public void block() {
            if  (this.block)
                return;
            this.block = true;
            this.updatedSinceBlocked = false;
            if (debug)
                Logger.log ("{state:listener-block} [" + StateVariable.this.toString() + "]");
        }

        @Override
        public void unblock(boolean notifyIfChanged) {
            if (!this.block)
                return;
            this.block = false;
            if (notifyIfChanged && updatedSinceBlocked && (listener != null)) {
                if (debug)
                    Logger.log ("{state:listener-unblock-dispatch} [" + StateVariable.this.toString() + "]");
                try {
                    listener.accept (state ());
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e, StateVariable.this);
                }
            } else 
            if (debug)
                Logger.log ("{state:listener-unblock} [" + StateVariable.this.toString() + "]");
            this.updatedSinceBlocked = false;
        }
        
    }
}
