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

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.store.IPaginatedStore;
import com.effacy.jui.core.client.store.IStore;

public interface ISearchMenu<V> extends IComponent {

    /**
     * Configuration to the selector menu.
     */
    public interface ISearchMenuConfig<V> {

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
         * For {@link IPaginatedStore} stores this clears the store on each opening of
         * the selector (forcing a reload).
         * 
         * @return {@code true} if to clear on open.
         */
        public boolean isStoreClear();
        
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
        * Determines of the menu should be masked during a load of data from the store.
        * 
        * @return {@code true} if it should.
        */
       default boolean isUseMaskOnLoad() {
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
     * Searches given the passd value.
     * 
     * @param value
     *              the value.
     */
    public void search(String value);

    /**
     * Navigate the cursor up.
     */
    public void up();

    /**
     * Navigation the cursor down.
     */
    public void down();

    /**
     * Select whatever is currently highlighted.
     */
    public void select();

    /**
     * Resets (starts a blank search).
     */
    public void reset();

}
