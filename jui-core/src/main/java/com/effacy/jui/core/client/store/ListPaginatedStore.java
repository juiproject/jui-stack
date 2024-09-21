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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.effacy.jui.platform.util.client.Logger;

/**
 * This is a type of {@link PaginatedStore} that is based by a list of records
 * (rather than a remote source of records).
 * <p>
 * This can be used for data mocking (i.e. paginating through the set) but also
 * for filtering a given collection of records.
 *
 * @author Jeremy Buckley
 */
public abstract class ListPaginatedStore<R> extends PaginatedStore<R> implements IFilteredStore<R> {

    /**
     * The totality of all records being represented.
     */
    private List<R> records;

    /**
     * The filtered set of records.
     */
    private List<R> filtered;

    /**
     * Construct an instance of the store.
     */
    public ListPaginatedStore() {
        records = new ArrayList<> ();
        populate (records);
    }

    /**
     * Obtains the underlying set of records.
     * 
     * @return the records.
     */
    protected List<R> records() {
        return records;
    }

    /**
     * Populate the passed list with the body of records the store represents.
     * 
     * @param records
     *                the records list to populate.
     */
    protected abstract void populate(List<R> records);

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.PaginatedStore#requestLoad(int, int,
     *      com.effacy.jui.core.client.store.PaginatedStore.ILoadRequestCallback)
     */
    @Override
    protected void requestLoad(int page, int pageSize, ILoadRequestCallback<R> cb) {
        List<R> results;
        List<R> source = (filtered != null) ? filtered : records;
        int beginIdx = page * pageSize;
        int endIdx = (page + 1) * pageSize;
        if (beginIdx < source.size ()) {
            if (endIdx >= source.size ())
                endIdx = source.size ();
            results = source.subList (beginIdx, endIdx);
        } else
            results = new ArrayList<> ();
        Logger.log (getClass ().getSimpleName () + " filtered=" + (filtered != null) + " page=" + page + " pageSize=" + pageSize + " results=" + source.size ());
        cb.onSuccess (results, source.size (), (filtered != null));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.PaginatedStore#clear()
     */
    @Override
    public void clear() {
        filtered = null;
        super.clear ();
    }

    @Override
    public void clearFilter() {
        filtered = null;
        reload (10);
    }

    @Override
    public void filter(Predicate<R> filter) {
        if (filter == null)
            filtered = null;
        else
            filtered = records.stream ().filter (filter).collect (Collectors.toList ());
        reload (10);
    }
}

