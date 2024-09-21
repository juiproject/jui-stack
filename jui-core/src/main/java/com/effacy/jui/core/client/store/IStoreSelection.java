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
import java.util.function.Consumer;

import com.effacy.jui.core.client.observable.IObservable;

/**
 * Represents behavuour that can be applied to a store for the selection of
 * elements from the store.
 *
 * @author Jeremy Buckley
 */
public interface IStoreSelection<V> extends IObservable {

    /**
     * Checks if a given value selected.
     * 
     * @param value
     *              the value to check.
     * @return {@code true} if it is selected.
     */
    default public boolean isSelected(V value) {
        return selectionAsStore ().contains (value);
    }

    /**
     * Selects the given value.
     * 
     * @param value
     *              the value to add to the selection set.
     */
    @SuppressWarnings("unchecked")
    default public void select(V value) {
        selectionAsStore ().add (value);
    };

    /**
     * Remove from selection the given value.
     * 
     * @param value
     *              the value to remove.
     */
    default public void unselect(V value) {
        selectionAsStore ().remove (value);
    };

    /**
     * Clear everything that has been selected.
     */
    default public void clearSelection() {
        selectionAsStore ().clear ();
    }

    /**
     * Get the selection as a list.
     * 
     * @return the selection.
     */
    default public List<V> selection() {
        return selectionAsStore ().asList ();
    }

    /**
     * Obtains the selection as a store.
     * 
     * @return the associated store.
     */
    public IStoreMutable<V> selectionAsStore();

    /**
     * Adds a selection change handler.
     * 
     * @param handler
     *                the handler.
     */
    public void handleOnSelectionChanged(Consumer<IStoreSelection<V>> handler) ;
}
