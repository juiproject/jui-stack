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
package com.effacy.jui.ui.client.control;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.IStore;

/**
 * A component for rendering and interacting with a selection control that lists
 * items that can be selected.
 */
public interface ISelectorMenu<V> extends IComponent {

    /**
     * Configuration to the selector menu.
     */
    public interface ISelectorMenuConfig<V> {

        /**
         * Supplies a comparator for comparing values. This is used for item selection.
         * 
         * @return the comparator (a {@code null} return defers to the default equality
         *         check).
         */
        default public BiFunction<V,V,Boolean> getComparator() {
            return null;
        }

        /**
         * The underlying store.
         * @return the store.
         */
        public IStore<V> getStore();

        /**
         * For {@link IPaginatedStore} stores this is batch size to load pages of data
         * in (increments the page size by this amount).
         * 
         * @return the batch size.
         */
        public int getStoreBatchSize();

        /**
         * Allows the selection to be searched ({@code null}).
         * 
         * @return {@code true} if seach should be enabled.
         */
        public boolean isAllowSearch();

        /**
         * For {@link IPaginatedStore} stores this clears the store on each opening of
         * the selector (forcing a reload).
         * 
         * @return {@code true} if to clear on open.
         */
        public boolean isStoreClear();

        /**
         * Allows the selection of an empty result (i.e. to clear the selection).
         * 
         * @return {@code true} if to allow empty values.
         */
        //public boolean isAllowEmpty();

        /**
         * Registers an <i>add</i> handler that has the selector display an add action.
         * <p>
         * The handler will be invoked with a callback. If the add action creates
         * something, then that should be returned via the callback.
         * 
         * @return the handler.
         */
        public Consumer<Consumer<V>> getAddHandler();

        /**
         * Provides for an alternative label to the default for when the add action is
         * configured (see {@link #addHandler(Consumer)}).
         * 
         * @return the add label.
         */
        public String getAddLabel();

        /**
         * This should be set when there is a risk of the control selector invoking
         * overflow (i.e. when used in a modal, which could result in obstruction of the
         * selector or induce scrolling of the modal contents).
         * <p>
         * If set this selector will be positioned using fixed semantics. However this
         * will result the selector being positioned fixed relative to the window so
         * will not move if the background scrolls.
         * 
         * @return {@code true} if should be safe.
         */
        public boolean isOverflowSafe();

        /**
         * If the items are selectable (checkboxes).
         * 
         * @return {@code true} if they are.
         */
        default boolean isSelectable() {
            return false;
        }

        /**
         * The buffering threshold for update count.
         * 
         * @return the threshold (default is 6 updates).
         */
        default int getSearchBufferCountThreshold() {
            return 6;
        }

        /**
         * The buffering threshold for time from first change.
         * 
         * @return the threshold in ms (default is 300).
         */
        default int getSearchBufferTimeThreshold() {
            return 300;
        }
    }

    /**
     * Refreshes (and reloads) the data.
     * 
     * @param value
     *              the currently selected value to pre-select (if possible).
     */
    public void reset(List<V> value);
}
