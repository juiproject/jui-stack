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

import java.util.function.Consumer;
import java.util.function.Function;

public interface IStateVariable<V extends IStateVariable<V>> {

    /**
     * Returns when adding a listener. Allows the listener to be removed.
     */
    public interface IStateListenerHandler<T> {

        /**
         * Determines if the state has emitted a change since the last time this method
         * was called.
         * 
         * @return {@code true} if a change has occurred.
         */
        public boolean changedSinceLastCall();

        /**
         * Removes the listener.
         */
        public void remove();

        /**
         * The state that the listener is bound to.
         * 
         * @return the state.
         */
        public T state();

        /**
         * Block change events from notifying the wrapped listener.
         */
        public void block();

        /**
         * Reverse any {@link #block()} state.
         * 
         * @param notifyIfChanged
         *                        {@code true} if the state has changed since last
         *                        blocked then notify the listener.
         */
        public void unblock(boolean notifyIfChanged);
    }

    /**
     * Adds the passed listener to the state variable. This will receive
     * notifications when the state changes.
     * 
     * @param listener
     *                 the listener to add.
     * @return the handler.
     */
    public IStateListenerHandler<V> listen(Consumer<V> listener);

    /**
     * Adds a listener bound to a handler to determine last change events.
     * 
     * @return the handler.
     */
    default public IStateListenerHandler<V> listen() {
        return listen (null);
    }

    /**
     * Modifies the state variable and notify the listeners.
     * 
     * @param modifier
     *                 to modify.
     */
    default public void modify(Consumer<V> modifier) {
        if (modifier != null) {
            modify (v -> {
                modifier.accept (v);
                return true;
            });
        }
    }

    /**
     * Modifies the state variable and notify the listeners.
     * 
     * @param modifier
     *                 to modify.
     */
    default public void modifyIf(boolean condition, Consumer<V> modifier) {
        if (condition && (modifier != null)) {
            modify (v -> {
                modifier.accept (v);
                return true;
            });
        }
    }

    /**
     * Conditionally modifies the state and notifies the listener.
     * <p>
     * The modifer needs to return {@code true} when it has actually modified the
     * data. This allows for conditional modification to occur efficiently.
     * 
     * @param modifier
     *                 to modify.
     */
    public void modify(Function<V,Boolean> modifier);
}
