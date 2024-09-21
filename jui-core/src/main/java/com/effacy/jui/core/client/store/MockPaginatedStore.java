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
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.effacy.jui.core.client.observable.Observable;
import com.effacy.jui.platform.util.client.TimerSupport;

/**
 * Used to mock a paginated store for testing purposes. It mimics the timing of
 * remote calls.
 *
 * @author Jeremy Buckley
 */
public class MockPaginatedStore<V> extends Observable implements IPaginatedStore<V>, ISearchStore<V> {

    /**
     * Convenience to build a (mock) paginated store that is build using the given
     * builder and applies the given matcher for search.
     * 
     * @param builder
     *                to builder out the store contents.
     * @param matcher
     *                to match for a keyword search.
     * @return the mock store.
     */
    public static <V> MockPaginatedStore<V> build(Consumer<ListStore<V>> builder, BiFunction<V,String,Boolean> matcher) {
        return new MockPaginatedStore<V> (new SearchStore<V> (new ListStore<V> (builder), matcher));
    }

    private ISearchStore<V> store;

    private int page;

    private int pageSize;

    private List<V> items = new ArrayList<> ();

    private Status status = Status.UNLOADED;

    private int delay = 1000;

    public MockPaginatedStore(ISearchStore<V> store) {
        this.store = store;
        this.pageSize = 10;
        this.page = -1;
    }

    /**
     * Assigns a delay (the default is 1000).
     * 
     * @param delay
     *              the delay in ms.
     * @return this instance.
     */
    public MockPaginatedStore<V> delay(int delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public void filter(String keywords) {
        store.filter (keywords);
        reload ();
    }

    @Override
    public void clearFilter() {
        store.clearFilter ();
        reload ();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public int getTotalAvailable() {
        return store.getTotalAvailable ();
    }

    @Override
    public int size() {
        return items.size ();
    }

    @Override
    public V get(int idx) {
        return items.get (idx);
    }

    @Override
    public int indexOf(V value) {
        return items.indexOf (value);
    }

    @Override
    public List<V> asList() {
        return items;
    }

    @Override
    public Iterator<V> iterator() {
        return items.iterator ();
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void reload() {
        load (0, 10);
    }

    @Override
    public void load(int page, int pageSize) {
        this.page = Math.max (0, page);
        this.pageSize = Math.max (1, pageSize);

        fireEvent (IStoreLoadingListener.class).onStoreBeforeLoad (store);
        status = Status.LOADING;
        TimerSupport.timer (() -> {
            MockPaginatedStore.this.items.clear ();
            int start = MockPaginatedStore.this.page * MockPaginatedStore.this.pageSize;
            int end = start + MockPaginatedStore.this.pageSize;
            if (start < store.size ()) {
                if (end >= store.size ())
                    end = store.size ();
                for (int i = start; i < end; i++)
                    MockPaginatedStore.this.items.add (store.get (i));
            }
            fireEvent (IStoreLoadingListener.class).onStoreAfterLoad (store);
            status = Status.LOADED;
            fireEvent (IStoreChangedListener.class).onStoreChanged (MockPaginatedStore.this);
        }, delay);
    }

    @Override
    public void clear() {
        this.items.clear ();
        if (this.status != Status.UNLOADED) {
            this.pageSize = 10;
            this.page = -1;
            this.status = Status.UNLOADED;
            fireEvent (IStoreChangedListener.class).onStoreChanged (MockPaginatedStore.this);
        }
    }

}
