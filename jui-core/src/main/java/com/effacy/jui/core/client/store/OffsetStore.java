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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.util.Dispatcher;
import com.effacy.jui.platform.util.client.TimerSupport;

/**
 * Standard implementation of {@link IOffsetStore}.
 */
public abstract class OffsetStore<V,T> extends StoreSelection<V> implements IOffsetStore<V> {

    /**
     * The items in the store.
     */
    private List<V> items = new ArrayList<> ();

    /**
     * See {@link #getTotalAvailable()}.
     */
    private int totalAvailable;

    /**
     * The next page (based on the start page token).
     */
    private T nextPage = null;

    /**
     * The size of each retrieval batch.
     */
    private int batchSize = 10;

    /**
     * Indicates that a loading request in in progress (activated).
     */
    private boolean requestInProgress;

    /**
     * The next load request that is pending.
     */
    private LoadRequest request;

    /**
     * The current status.
     */
    private Status status = Status.UNLOADED;

    /**
     * See {@link #setDelayInInitialLoad(int)}.
     */
    private int delayOnInitialLoad = 0;

    /**
     * See {@link #onClear(Consumer)}.
     */
    private Dispatcher<IOffsetStore<V>> clearHandlers = new Dispatcher<IOffsetStore<V>> ();

    /**
     * See {@link #onChange(Consumer)}.
     */
    private Dispatcher<IOffsetStore<V>> changeHandlers = new Dispatcher<IOffsetStore<V>> ();

    /**
     * Assigns a delay to apply when initially loading the store. This can be used
     * to ensure that certain there is a minimum time delay between the before load
     * event and the after load event (to enable certain behaviours or experiences).
     * 
     * @param delayOnInitialLoad
     *                           the delay in ms.
     * @return this paginated store instance.
     */
    public OffsetStore<V,T> setDelayInInitialLoad(int delayOnInitialLoad) {
        this.delayOnInitialLoad = delayOnInitialLoad;
        return this;
    }

    /**
     * Adds a clear handler (invoked when {@link #clear()} is invoked).
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    public OffsetStore<V,T> onClear(Consumer<IOffsetStore<V>> handler) {
        clearHandlers.add (handler);
        return this;
    }

    /**
     * Adds a change handler (invoked when the store contents change, along with the
     * firing of the {@link IStoreChangedListener#onStoreChanged(IStore)} event).
     * 
     * @param handler
     *                the handler to add.
     * @return this store instance.
     */
    public OffsetStore<V,T> onChange(Consumer<IOffsetStore<V>> handler) {
        changeHandlers.add (handler);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#fullyLoaded()
     */
    @Override
    public boolean fullyLoaded() {
        if (status.is(Status.LOADED, Status.FILTERED))
            return (nextPage == null);
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#getStatus()
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#getTotalAvailable()
     */
    @Override
    public int getTotalAvailable() {
        return totalAvailable;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#size()
     */
    @Override
    public int size() {
        return items.size ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#get(int)
     */
    @Override
    public V get(int idx) {
        if ((idx < 0) || (idx >= items.size ()))
            return null;
        return items.get (idx);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(V value) {
        return items.indexOf (value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#asList()
     */
    @Override
    public List<V> asList() {
        return items;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<V> iterator() {
        return items.iterator ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IOffsetStore#load(int)
     */
    @Override
    public void load(int batchSize) {
        this.items.clear ();
        this.nextPage = null;
        this.batchSize = batchSize;

        // Perform an active load.
        _load (null, batchSize);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IOffsetStore#loadNext()
     */
    public void loadNext() {
        if (status == Status.UNLOADED)
            return;
        _load (nextPage, batchSize);
    }

    /**
     * Performs a load (no matter what).
     * 
     * @param page
     *                 the page to load.
     * @param pageSize
     *                 the page size.
     * @param full
     *                 {@code true} to force a full load (rather than testing for an
     *                 addition).
     */
    protected void _load(T page, int pageSize) {
        // Create the load request and activate if a request is not in progress.
        this.request = new LoadRequest (page, pageSize);
        if (!this.requestInProgress)
            this.request.activate ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IOffsetStore#clear()
     */
    @Override
    public void clear() {
        selectionAsStore ().clear ();
        this.nextPage = null;
        this.items.clear ();
        this.status = Status.UNLOADED;
        _onClear ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IOffsetStore#clearAndReload()
     */
    @Override
    public void clearAndReload() {
        clear ();
        if (batchSize <= 0)
            batchSize = 10;
        load (batchSize);
    }
    

    /**
     * Dispatches a clear action.
     */
    protected final void _onClear() {
        onClear ();
        clearHandlers.dispatch (this);
        _onChange ();
    }

    /**
     * Invoked by {@link #clear()} when a clear has been performed (but before the
     * clear event has fired).
     */
    protected void onClear() {
        // Nothing.
    }

    /**
     * Dispatches a clear action.
     */
    protected final void _onChange() {
        onChange ();
        changeHandlers.dispatch (this);
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
    }

    /**
     * Invoked when there is a change in the store contents.
     */
    protected void onChange() {
        // Nothing.
    }

    /**
     * Requests a load with response to be passed through to the callback. It will
     * be guaranteed that only one load request is processed at any given time (and
     * it is assumed that the request is being processed immediately).
     *
     * @param page
     *                 the page being requested (this is a token).
     * @param batchSize
     *                 the maximum number of items to load.
     * @param cb
     *                 the callback.
     */
    protected abstract void requestLoad(T page, int batchSize, ILoadRequestCallback<V,T> cb);

    /**
     * Captures a loading response.
     */
    public interface ILoadRequestCallback<V,T> {

        /**
         * The loading of items was successful.
         * 
         * @param items
         *                                 the items that were retrieved.
         * @param nextPage
         *                                 the next page start token.
         * @param totalItotalAvailabletems
         *                                 the total number of items available for
         *                                 paginating through.
         * @param filtered
         *                                 if filtering was applied.
         */
        public void onSuccess(List<V> items, T nextPage, int totalAvailable, boolean filtered);

        /**
         * If the loading failed this is the failure message.
         * 
         * @param message
         *                the message.
         */
        public void onFailure(String message);

    }

    /**
     * Packages up a load request that is processed by
     * {@link PaginatedStore#requestLoad(int, int, ILoadRequestCallback)}.
     */
    class LoadRequest implements ILoadRequestCallback<V,T> {

        /**
         * Indicates the the request is in progress.
         */
        private boolean inProgress = false;

        /**
         * The page to load.
         */
        private T page;

        /**
         * The batch size.
         */
        private int batchSize;

        /**
         * The pre-loading status.
         */
        private Status preLoadStatus;

        /**
         * Request a load for the given page and page size.
         * 
         * @param page
         *                 the page to load.
         * @param batchSize
         *                 the batch size.
         */
        LoadRequest(T page, int batchSize) {
            this.page = page;
            this.batchSize = batchSize;
        }

        /**
         * Activates the request (this can be re-entrant).
         */
        void activate() {
            if (inProgress)
                return;
            preLoadStatus = OffsetStore.this.status;
            inProgress = true;
            OffsetStore.this.requestInProgress = true;
            if (OffsetStore.this.request == this)
                OffsetStore.this.request = null;
            fireEvent (IStoreLoadingListener.class).onStoreBeforeLoad (OffsetStore.this);
            OffsetStore.this.status = Status.LOADING;
            if ((delayOnInitialLoad > 0) && (Status.UNLOADED == preLoadStatus))
                TimerSupport.timer (() -> requestLoad (page, batchSize, LoadRequest.this), delayOnInitialLoad);
            else
                requestLoad (page, batchSize, this);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.store.PaginatedStore.ILoadRequestCallback#onSuccess(java.util.List,
         *      int)
         */
        @Override
        public void onSuccess(List<V> items, T nextPage, int totalAvailable, boolean filtered) {
            OffsetStore.this.items.addAll (items);
            OffsetStore.this.nextPage = nextPage;
            OffsetStore.this.totalAvailable = totalAvailable;
            OffsetStore.this.status = filtered ? Status.FILTERED : Status.LOADED;

            // Complete the request process.
            complete ();

            // Fire a change event.
            _onChange ();
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.store.PaginatedStore.ILoadRequestCallback#onFailure(java.lang.String)
         */
        @Override
        public void onFailure(String message) {
            OffsetStore.this.status = preLoadStatus;
            complete ();
        }

        /**
         * Completes the processing of the request (including starting up any pending
         * request).
         */
        protected void complete() {
            // Notify the load.
            fireEvent (IStoreLoadingListener.class).onStoreAfterLoad (OffsetStore.this);

            // Clear progress status.
            OffsetStore.this.requestInProgress = false;

            // Check if there is another request to load.
            if (OffsetStore.this.request != null) {
                LoadRequest requestToRun = OffsetStore.this.request;
                OffsetStore.this.request = null;
                requestToRun.activate ();
            }
        }

    }
    
}
