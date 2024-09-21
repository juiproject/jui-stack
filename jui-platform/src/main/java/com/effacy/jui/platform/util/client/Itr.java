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
package com.effacy.jui.platform.util.client;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Convenience to iterator over a list while tracking list position (including
 * begining and end).
 */
public class Itr {

    /**
     * The iteration context.
     */
    public interface IContext {

        /**
         * The current index into the collection.
         * 
         * @return the index (offest from 0).
         */
        public int index();

        /**
         * If this is the first element.
         * 
         * @return {@code true} if it is.
         */
        public boolean first();

        /**
         * If this is the last element.
         * 
         * @return {@code true} if it is.
         */
        public boolean last();
    }

    /**
     * Internal implementation of the iteration context.
     */
    static class Context implements IContext {
        private int size;
        private int idx;

        /**
         * Construct with initial size of collection.
         * 
         * @param size
         *             the collection size.
         */
        public Context(int size) {
            this.size = size;
        }

        /**
         * Update with a new index.
         * 
         * @param idx
         *            the new index.
         * @return this context object.
         */
        public Context update(int idx) {
            this.idx= idx;
            return this;
        }
        @Override
        public int index() {
            return idx;
        }
        @Override
        public boolean first() {
            return (idx == 0);
        }
        @Override
        public boolean last() {
            return (idx == (size - 1));
        }
    }
    
    /**
     * Iterates over the elements of the passed list.
     * 
     * @param <T>
     * @param list
     *                 the collection to iterator over.
     * @param iterator
     *                 the visitor to recive and process the items (passed the
     *                 current iteration context and the current item).
     */
    public static <T> void forEach(List<T> list, BiConsumer<IContext,T> iterator) {
        if ((list == null) || (iterator == null))
            return;
        Context context = new Context (list.size ());
        for (int i = 0; i < context.size; i++) {
            T value = list.get (i);
            iterator.accept (context.update (i), value);
        }
    }
}
