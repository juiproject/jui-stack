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

/**
 * This is a type of store that queries at a given offset where the offset is a
 * token of some form.
 */
public interface IOffsetStore<V> extends IStore<V> {

    /**
     * Loads the first batch of items from the start.
     * 
     * @param batchSize
     *                 the number of items to laal.
     */
    public void load(int batchSize);

    /**
     * Loads the next batch of items.
     */
    public void loadNext();

    /**
     * Clears the contents of the store (and any selection where the store also
     * implements {@link IStoreSelection}) and reverts to the unloaded state. The
     * page size will be reset to the default page size (see
     * {@link #getInitialPageSize()}).
     */
    public void clear();

    /**
     * Clears (see {@link #clear()}) then reloads using previous batch size.
     */
    public void clearAndReload();
}
