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
package com.effacy.jui.core.client.store;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.effacy.jui.core.client.observable.IObservable;

public interface IStore<V> extends Iterable<V>, IObservable {

    /**
     * Store status values.
     */
    public enum Status {
        
        /**
         * The store has not loaded anything so no information is available (i.e. total
         * available, etc).
         */
        UNLOADED,

        /**
         * The store is in error.
         */
        ERROR,

        /**
         * Data is loading.
         */
        LOADING,

        /**
         * The store has loaded and no filtering is in progress.
         */
        LOADED,

        /**
         * The store has loaded and filtering of the results is in progress.
         */
        FILTERED;

        /**
         * Determines if this is any of the passed states.
         * 
         * @param status
         *               the status value to test for.
         * @return {@code true} if this is one of the passed values.
         */
        public boolean is(Status... status) {
            for (Status s : status) {
                if (this == s)
                    return true;
            }
            return false;
        }
    }

    /**
     * The status of the store.
     * 
     * @return the status.
     */
    public Status getStatus();

    /**
     * Any message that is associated with the {@link #getStatus()}.
     * 
     * @return the message.
     */
    default String getStatusMessage() {
        return null;
    }

    /**
     * The total number of items available to be loaded into the store.
     * <p>
     * If this is negative then the underlying service it not able to provide this
     * information.
     * 
     * @return the total.
     */
    public int getTotalAvailable();

    /**
     * The total number of items loaded in the store.
     * 
     * @return the total.
     */
    public int size();

    /**
     * If the store is fully loaded.
     * 
     * @return {@code true} if so.
     */
    default boolean fullyLoaded() {
        return (size () >= getTotalAvailable ());
    }

    /**
     * Determines if the store is empty (contains no items).
     * 
     * @return {@code true} if it is empty.
     */
    default public boolean empty() {
        return (size () == 0);
    }

    /**
     * Obtains the element (needs to have been loaded into the store) at the given
     * index.
     * 
     * @param idx
     *            the index.
     * @return the value (or {@code null}).
     */
    public V get(int idx);

    /**
     * Attempts to find the index of the given item. This index is such that
     * {@link #get(int)} will return the item.
     * 
     * @param value
     *              the item to find in the store.
     * @return the index of the item or {@code -1} if not found.
     */
    public int indexOf(V value);

    /**
     * Determines if the passed value is contained in the store.
     * 
     * @param value
     *              the value to check.
     * @return {@code true} if it is contained in the store.
     */
    default boolean contains(V value) {
        return (indexOf (value) >= 0);
    }

    /**
     * The contents of the store as a list.
     * 
     * @return the list.
     */
    public List<V> asList();

    /**
     * Finds the first instance of the record that matches the passed predicate.
     * 
     * @param test
     *             the predicate to use as the matching criteria.
     * @return the result (first match if there is one).
     */
    default public Optional<V> find(Predicate<V> test) {
        if (test != null) {
            for (var item : asList()) {
                if ((item != null) && test.test(item))
                    return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * Convenience to cast a store.
     * 
     * @param <T>
     * @return the cast store.
     */
    @SuppressWarnings("unchecked")
    default public <T extends IStore<V>> T cast() {
        return (T) this;
    }

}
