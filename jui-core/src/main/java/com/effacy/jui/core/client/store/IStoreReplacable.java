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

import java.util.ArrayList;
import java.util.List;

/**
 * A store that can have items replaced.
 */
public interface IStoreReplacable<V> extends IStore<V> {

    /**
     * Comparator for comparing items to determine replacement.
     */
    @FunctionalInterface
    public interface IReplacementComparator<V> {

        /**
         * Determines if the existing item should be replaced by the updating item.
         * 
         * @param existing
         *                 the existing item (in the store).
         * @param updating
         *                 an updating item (resolved outside of the store)
         * @return {@code true} if the existing item should be replaced by the updating
         *         item.
         */
        public boolean replace(V existing, V updating);
    }

    /**
     * Updates the store's contents with the given revisions. If a replacement is
     * made a change event is expected to be fired.
     * 
     * @param updates
     *                   the revisions to make.
     * @param comparator
     *                   that compares two items to determine if the latter should
     *                   replace the former.
     * @return {@code true} if at least one replacement is made.
     */
    public boolean replace(List<V> updates, IReplacementComparator<V> comparator);

    /**
     * Same as for {@link #replace(List, IReplacementComparator)} but takes a single
     * item.
     */
    public default boolean replace(V update, IReplacementComparator<V> comparator) {
        if (update == null)
            return false;
        List<V> updates = new ArrayList<>();
        updates.add (update);
        return replace (updates, comparator);
    }
}
