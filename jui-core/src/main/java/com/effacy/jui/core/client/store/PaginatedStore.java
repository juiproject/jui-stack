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
import java.util.function.Predicate;

import com.effacy.jui.platform.util.client.Carrier;
import com.effacy.jui.platform.util.client.TimerSupport;

/**
 * PaginatedStore
 *
 * @author Jeremy Buckley
 */
public abstract class PaginatedStore<V> extends StoreSelection<V> implements IPaginatedStore<V>, IStoreReplacable<V> {

    /**
     * The items in the store.
     */
    private List<V> items = new ArrayList<> ();

    /**
     * See {@link #getTotalAvailable()}.
     */
    private int totalAvailable;

    /**
     * See {@link #getPage()}.
     */
    private int page = -1;

    /**
     * See {@link #getPageSize()}.
     */
    private int pageSize = 10;

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
     * Any message associated with the current status.
     */
    private String statusMessage;

    /**
     * See {@link #setDelayInInitialLoad(int)}.
     */
    private int delayOnInitialLoad = 0;
    
    /************************************************************************
     * Event dispatchers.
     ************************************************************************/

    /**
     * Dispatches a clear action.
     */
    protected final void _onClear() {
        onClear ();
        // Dispatcher.dispatch (clearHandlers, this, true);
        fireEvent (IStoreClearListener.class).onStoreCleared (this);
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
     * Dispatches a change action.
     */
    protected final void _onChange() {
        onChange ();
        // Dispatcher.dispatch (changeHandlers, this, true);
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
    }

    /**
     * Invoked when there is a change in the store contents.
     */
    protected void onChange() {
        // Nothing.
    }

    /**
     * Dispatches a before-load action.
     */
    protected final void _onBeforeLoad() {
        onBeforeLoad ();
        //Dispatcher.dispatch (beforeLoadHandlers, this, true);
        fireEvent (IStoreBeforeLoadListener.class).onStoreBeforeLoad (this);
    }

    /**
     * Invoked just prior to the store loading.
     */
    protected void onBeforeLoad() {
        // Nothing.
    }

    /**
     * Dispatches a before-load action.
     */
    protected final void _onAfterLoad() {
        onAfterLoad ();
        //Dispatcher.dispatch (afterLoadHandlers, this, true);
        fireEvent (IStoreAfterLoadListener.class).onStoreAfterLoad (this);
    }

    /**
     * Invoked just prior to the store loading.
     */
    protected void onAfterLoad() {
        // Nothing.
    }

    /************************************************************************
     * Properties
     ************************************************************************/

    /**
     * Assigns a delay to apply when initially loading the store. This can be used
     * to ensure that certain there is a minimum time delay between the before load
     * event and the after load event (to enable certain behaviours or experiences).
     * 
     * @param delayOnInitialLoad
     *                           the delay in ms.
     * @return this paginated store instance.
     */
    public PaginatedStore<V> setDelayInInitialLoad(int delayOnInitialLoad) {
        this.delayOnInitialLoad = delayOnInitialLoad;
        return this;
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
     * @see com.effacy.jui.core.client.store.IStore#getStatusMessage()
     */
    @Override
    public String getStatusMessage() {
        return statusMessage;
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
     * @see com.effacy.jui.core.client.store.IPaginatedStore#getPage()
     */
    @Override
    public int getPage() {
        return page;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IPaginatedStore#getPageSize()
     */
    @Override
    public int getPageSize() {
        return pageSize;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IPaginatedStore#reload()
     */
    @Override
    public void reload() {
        // Clear the current contents of the store.
        items.clear ();
        // Mark as loading (if not first time) and notify listeners.
        if (status != Status.UNLOADED)
            status = Status.LOADING;
        statusMessage = null;
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
        if (status == Status.UNLOADED)
            load (0, pageSize);
        else
            _load (page, pageSize, true);
    }

    /**
     * Reloads with the given page size (the first page will be loaded).
     * 
     * @param pageSize
     *                 the page size (will be updated to internally).
     */
    public void reload(int pageSize) {
        this.pageSize = pageSize;
        this.page = 0;
        reload ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IPaginatedStore#load(int, int)
     */
    @Override
    public void load(int page, int pageSize) {
        // Check that a change is actually occurring.
        if ((this.status != Status.UNLOADED) && (page == this.page) && (pageSize == this.pageSize))
            return;

        // Check for a contraction (which will just remove items from the list without
        // loading).
        if ((this.status != Status.UNLOADED) && (page == 0) && (this.pageSize == 0) && (pageSize < this.pageSize)) {
            this.pageSize = pageSize;
            this.items = this.items.subList (0, pageSize);
            _onChange ();
            return;
        }

        // Perform an active load.
        _load (page, pageSize, false);
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
    protected void _load(int page, int pageSize, boolean full) {
        // Determine if we already have everything loaded.
        if (!full && (totalAvailable > 0) && (size() >= totalAvailable))
            return;

        // Determine if this is an addition.
        boolean addition = !full && (this.status != Status.UNLOADED) && (page == 0) && (this.page == 0) && (pageSize > this.pageSize);
        if (addition) {
            // For now we keep this even (otherwise force a full reload).
            int diff = pageSize - this.pageSize;
            if (this.items.size () % diff == 0) {
                page = this.items.size () / diff;
                pageSize = diff;
            } else
                addition = false;
        }

        // Create the load request and activate if a request is not in progress.
        this.request = new LoadRequest (page, pageSize, addition);
        if (!this.requestInProgress)
            this.request.activate ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IPaginatedStore#clear()
     */
    @Override
    public void clear() {
        selectionAsStore ().clear ();
        this.page = -1;
        this.pageSize = getInitialPageSize ();
        this.items.clear ();
        this.statusMessage = null;
        Status prior = this.status;
        this.status = Status.UNLOADED;
        if (prior != this.status)
            fireEvent(IStoreStatusListener.class).onStoreStatucChanged(this, this.status, prior);
        _onClear ();
    }

    /**
     * Remove all elements that match the given predicate. This will not perform a
     * reload.
     * 
     * @param test
     *             the test to use.
     */
    public void remove(Predicate<V> test) {
        if (items.removeIf(v -> {
            if (!test.test(v))
                return false;
            totalAvailable--;
            return true;
        })) {
            if (items.isEmpty())
                reload();
            else
                _onChange ();
        }
    }

    @Override
    public boolean replace(List<V> updates, IReplacementComparator<V> comparator) {
        if ((updates == null) || updates.isEmpty ())
            return false;
        Carrier<Boolean> updated = Carrier.of (false);
        items.replaceAll (v -> {
            if (v == null)
                return v;
            for (V update : updates) {
                if (update == null)
                    continue;
                if ((comparator == null) ? update.equals (v) : comparator.replace (v, update)) {
                    updated.set (true);
                    return update;
                }
            }
            return v;
        });
        if (updated.get ())
            _onChange ();
        return updated.get ();
    }


    /**
     * Requests a load with response to be passed through to the callback. It will
     * be guaranteed that only one load request is processed at any given time (and
     * it is assumed that the request is being processed immediately).
     *
     * @param page
     *                 the page being requested (from 0, the offset indexed from 0
     *                 is the page multiplied by the page size).
     * @param pageSize
     *                 the page size (maximum number of items to return).
     * @param cb
     *                 the callback.
     */
    protected abstract void requestLoad(int page, int pageSize, ILoadRequestCallback<V> cb);

    /**
     * Captures a loading response.
     */
    public interface ILoadRequestCallback<V> {

        /**
         * The loading of items was successful.
         * 
         * @param items
         *                                 the items that were retrieved.
         * @param totalItotalAvailabletems
         *                                 the total number of items available for
         *                                 paginating through.
         * @param filtered
         *                                 if filtering was applied.
         */
        public void onSuccess(List<V> items, int totalAvailable, boolean filtered);

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
    class LoadRequest implements ILoadRequestCallback<V> {

        /**
         * Indicates the the request is in progress.
         */
        private boolean inProgress = false;

        /**
         * The page to load.
         */
        private int page;

        /**
         * The page size.
         */
        private int pageSize;

        /**
         * This is a incremental load not a partialy laod.
         */
        private boolean addition;

        /**
         * The pre-loading status.
         */
        private Status preLoadStatus;

        /**
         * Request a load for the given page and page size.
         * 
         * @param page
         *                 the page to load.
         * @param pageSize
         *                 the size of the page.
         * @param addition
         *                 {@code true} if this is an incremental load (i.e. adding to
         *                 the items in which case the page is not updated and the page
         *                 size is added to).
         */
        LoadRequest(int page, int pageSize, boolean addition) {
            this.page = page;
            this.pageSize = pageSize;
            this.addition = addition;
        }

        /**
         * Activates the request (this can be re-entrant).
         */
        void activate() {
            if (inProgress)
                return;
            preLoadStatus = PaginatedStore.this.status;
            inProgress = true;
            PaginatedStore.this.requestInProgress = true;
            if (PaginatedStore.this.request == this)
                PaginatedStore.this.request = null;
            _onBeforeLoad ();
            PaginatedStore.this.statusMessage = null;
            Status prior = PaginatedStore.this.status;
            PaginatedStore.this.status = Status.LOADING;
            if (prior != PaginatedStore.this.status)
                fireEvent(IStoreStatusListener.class).onStoreStatucChanged(PaginatedStore.this, PaginatedStore.this.status, prior);
            if ((delayOnInitialLoad > 0) && (Status.UNLOADED == preLoadStatus))
                TimerSupport.timer (() -> requestLoad (page, pageSize, LoadRequest.this), delayOnInitialLoad);
            else
                requestLoad (page, pageSize, this);
        }

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.core.client.store.PaginatedStore.ILoadRequestCallback#onSuccess(java.util.List,
         *      int)
         */
        @Override
        public void onSuccess(List<V> items, int totalAvailable, boolean filtered) {
            if (addition) {
                PaginatedStore.this.items.addAll (items);
                PaginatedStore.this.page = 0;
                PaginatedStore.this.pageSize += pageSize;
                if (PaginatedStore.this.pageSize > totalAvailable)
                    PaginatedStore.this.pageSize = totalAvailable;
            } else {
                PaginatedStore.this.items.clear ();
                PaginatedStore.this.items.addAll (items);
                PaginatedStore.this.page = page;
                PaginatedStore.this.pageSize = pageSize;
            }
            PaginatedStore.this.totalAvailable = totalAvailable;

            Status prior = PaginatedStore.this.status;
            PaginatedStore.this.status = filtered ? Status.FILTERED : Status.LOADED;
            if (prior != PaginatedStore.this.status)
                fireEvent(IStoreStatusListener.class).onStoreStatucChanged(PaginatedStore.this, PaginatedStore.this.status, prior);

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
            PaginatedStore.this.statusMessage = message;
            Status prior = PaginatedStore.this.status;
            PaginatedStore.this.status = Status.ERROR;
            if (prior != PaginatedStore.this.status)
                fireEvent(IStoreStatusListener.class).onStoreStatucChanged(PaginatedStore.this, PaginatedStore.this.status, prior);
            complete ();
        }

        /**
         * Completes the processing of the request (including starting up any pending
         * request).
         */
        protected void complete() {
            // Notify the load.
            _onAfterLoad ();

            // Clear progress status.
            PaginatedStore.this.requestInProgress = false;

            // Check if there is another request to load.
            if (PaginatedStore.this.request != null) {
                LoadRequest requestToRun = PaginatedStore.this.request;
                PaginatedStore.this.request = null;
                requestToRun.activate ();
            }
        }

    }

}
