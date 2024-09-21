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

import com.effacy.jui.core.client.observable.Observable;
import com.effacy.jui.platform.util.client.Carrier;

/**
 * Variant of an {@link IStore} that is backed by a list (so items can be added
 * and removed directly).
 * <p>
 * If you want a filtered version then wrap in a {@link FilteredStore}.
 *
 * @author Jeremy Buckley
 */
public class ListStore<V> extends Observable implements IStoreMutable<V>, IStoreReplacable<V> {

    /**
     * Creates a store populated with the passed enum values.
     * 
     * @param <E>
     *               the value type.
     * @param values
     *               the values.
     * @return the pre-populated store.
     */
    @SafeVarargs
    public static <E> ListStore<E> create(E... values) {
        ListStore<E> store = new ListStore<E> ();
        store.add (values);
        return store;
    }

    /**
     * Creates a store populated with the passed enum values.
     * 
     * @param <E>
     *               the value type.
     * @param builder
     *               to build out the store contents.
     * @return the pre-populated store.
     */
    public static <E> ListStore<E> create(Consumer<ListStore<E>> builder) {
        ListStore<E> store = new ListStore<E> ();
        builder.accept (store);
        return store;
    }

    /**
     * The items held in the store.
     */
    private List<V> items = new ArrayList<V> ();

    /**
     * Construct an empty store.
     */
    public ListStore() {
        // Nothing.
    }

    /**
     * Construct a store populated with the passed items.
     * 
     * @param items
     *              the items to populate the store with.
     */
    public ListStore(List<V> items) {
        if (items != null) 
            this.items.addAll (items);
    }

    /**
     * Construct a store populated with the passed builder.
     * 
     * @param builder
     *              to build out the store.
     */
    public ListStore(Consumer<ListStore<V>> builder) {
        if (builder != null)
            builder.accept (this);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStoreMutable#add(java.lang.Object[])
     */
    @Override
    @SafeVarargs
    final public ListStore<V> add(V... values) {
        if ((values != null) && (values.length > 0)) {
            for (V value : values)
                items.add (value);
            fireEvent (IStoreChangedListener.class).onStoreChanged (this);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#getStatus()
     */
    @Override
    public Status getStatus() {
        // An array store is always full loaded.
        return Status.LOADED;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStore#getTotalAvailable()
     */
    @Override
    public int getTotalAvailable() {
        return items.size ();
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
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<V> iterator() {
        return items.iterator ();
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
        if (value == null)
            return -1;
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
     * Determines if the list contains the given item.
     * 
     * @param item
     *             the item to test for.
     * @return {@code true} if the store contains the item.
     */
    public boolean contains(V item) {
        if (item == null)
            return false;
        return items.contains (item);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStoreMutable#remove(java.lang.Object)
     */
    @Override
    public ListStore<V> remove(V item) {
        if (item == null)
            return this;
        if (!items.contains (item))
            return this;
        items.remove (item);
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.IStoreMutable#clear()
     */
    @Override
    public ListStore<V> clear() {
        if (items.isEmpty ())
            return this;
        items.clear ();
        fireEvent (IStoreChangedListener.class).onStoreChanged (this);
        return this;
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
            fireEvent (IStoreChangedListener.class).onStoreChanged (this);
        return updated.get ();
    }
}
