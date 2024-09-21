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
import java.util.function.Predicate;

import com.effacy.jui.core.client.observable.Observable;

public class FilteredStore<V> extends Observable implements IFilteredStore<V> {

    private IStore<V> store;

    private boolean filtered;

    private List<V> items = new ArrayList<V> ();

    public FilteredStore(IStore<V> store) {
        this.store = store;
        this.store.addListener (IStoreChangedListener.create (s -> {
            if (filtered)
                clearFilter ();
            FilteredStore.this.fireEvent (IStoreChangedListener.class).onStoreChanged (this);
        }));
    }

    public FilteredStore<V> onStoreChanged(Consumer<IStore<V>> listener) {
        this.store.addListener (IStoreChangedListener.create (listener));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<V> iterator() {
        return filtered ? items.iterator () : store.iterator ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#getStatus()
     */
    @Override
    public Status getStatus() {
        return filtered ? Status.FILTERED : store.getStatus ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#getTotalAvailable()
     */
    @Override
    public int getTotalAvailable() {
        return filtered ? items.size () : store.getTotalAvailable ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#size()
     */
    @Override
    public int size() {
        return filtered ? items.size () : store.size ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#get(int)
     */
    @Override
    public V get(int idx) {
        if (!filtered)
            return store.get (idx);
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
        if (value == null)
            return -1;
        if (!filtered)
            return store.indexOf (value);
        return items.indexOf (value);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#asList()
     */
    @Override
    public List<V> asList() {
        return filtered ? items : store.asList ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IFilteredStore#filter(java.util.function.Predicate)
     */
    @Override
    public void filter(Predicate<V> filter) {
        items.clear ();
        store.forEach (v -> {
            if (filter.test (v))
                items.add (v);
        });
        filtered = true;
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IFilteredStore#clearFilter()
     */
    @Override
    public void clearFilter() {
        filtered = false;
        items.clear ();
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
    }

}
