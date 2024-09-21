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

import java.util.function.Consumer;

import com.effacy.jui.core.client.util.TriConsumer;

/**
 * A version of {@link IStore} that represents a view into a larger body of
 * data. This view is parameterised by the notion of a page which has a size and
 * an index (where the underlying data is partitioned into pages).
 * <p>
 * This is a fairly standard model that is used for data retrieval and thus
 * reflects that model.
 *
 * @author Jeremy Buckley
 */
public interface IPaginatedStore<V> extends IStore<V> {

    /**
     * The current page of results being loaded (indexed from 0).
     * 
     * @return the page.
     */
    public int getPage();

    /**
     * The maximum number of results per page.
     * 
     * @return the page size.
     */
    public int getPageSize();

    /**
     * Loads the first batch of items from the start.
     * 
     * @param pageSize
     *                 the page size.
     */
    default public void load(int pageSize) {
        load (0, pageSize);
    }

    /**
     * Commences a loading of the given page for the given page size.
     * 
     * @param page
     *                 the page.
     * @param pageSize
     *                 the page size.
     */
    public void load(int page, int pageSize);

    /**
     * Loads the next batch of items.
     */
    default void loadNext() {
        loadNext (true);
    }

    /**
     * Loads the next page.
     * 
     * @param expand
     *               {@code true} if loading the next means expading the current
     *               page size by the {@link #getInitialPageSize()}).
     */
    default void loadNext(boolean expand) {
        if (expand)
            load (0, getPageSize () + getInitialPageSize ());
        else
            load (getPage () + 1, getPageSize ());
    }

    /**
     * Forces a reload of the contents of the store.
     * <p>
     * If this is the first load then will automatically load the first page of
     * results.
     */
    public void reload();

    /**
     * Clears the contents of the store (and any selection where the store also
     * implements {@link IStoreSelection}) and reverts to the unloaded state. The
     * page size will be reset to the default page size (see
     * {@link #getInitialPageSize()}).
     */
    public void clear();

    /**
     * Convenience to {@link #clear()} then {@link #reload()} which essentially just
     * resets the store to is initially loaded state.
     */
    default void clearAndReload() {
        clear ();
        reload ();
    }

    /**
     * Obtains the initial (default) page size.
     * 
     * @return the page size.
     */
    default int getInitialPageSize() {
        return 10;
    }

    /**
     * Adds a clear handler (invoked when {@link #clear()} is invoked).
     * <p>
     * Is just a convenience to add an {@link IStoreClearListener}.
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    default public IPaginatedStore<V> handleOnClear(Consumer<IStore<V>> handler) {
        if (handler != null)
            addListener(IStoreClearListener.create(handler));
        return this;
    }

    /**
     * Adds a change handler (invoked when the store contents change).
     * <p>
     * Is just a convenience to add an {@link IStoreChangedListener}.
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    default public IPaginatedStore<V> handleOnChange(Consumer<IStore<V>> handler) {
        if (handler != null)
            addListener(IStoreChangedListener.create(handler));
        return this;
    }

    /**
     * Adds a change handler (invoked just before the store commences a load).
     * <p>
     * Is just a convenience to add an {@link IStoreBeforeLoadListener}.
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    default public IPaginatedStore<V> handleBeforeLoad(Consumer<IStore<V>> handler) {
        if (handler != null)
            addListener(IStoreBeforeLoadListener.create(handler));
        return this;
    }

    /**
     * Adds a change handler (invoked just before the store commences a load).
     * <p>
     * Is just a convenience to add an {@link IStoreAfterLoadListener}.
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    default public IPaginatedStore<V> handleAfterLoad(Consumer<IStore<V>> handler) {
        if (handler != null)
            addListener(IStoreAfterLoadListener.create(handler));
        return this;
    }

    /**
     * Adds a change of status handler.
     * <p>
     * Is just a convenience to add an {@link IStoreStatusListener}.
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    default public IPaginatedStore<V> handleStatusChanged(TriConsumer<IStore<V>, Status, Status> handler) {
        if (handler != null)
            addListener(IStoreStatusListener.create(handler));
        return this;
    }

}
