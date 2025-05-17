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
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;

/**
 * This is a type of {@link PaginatedStore} that is based by a list of records
 * (rather than a remote source of records).
 * <p>
 * This can be used for data mocking (i.e. paginating through the set) but also
 * for filtering a given collection of records.
 *
 * @author Jeremy Buckley
 */
public class ListPaginatedStore<R> extends PaginatedStore<R> implements IFilteredStore<R>, ISearchStore<R> {

    /**
     * The totality of all records being represented.
     */
    private List<R> records = new ArrayList<> ();

    /**
     * Search keywords.
     */
    private String keywords;

    /**
     * The filter to apply.
     */
    private BiPredicate<R,String> filter;

    /**
     * Construct an instance of the store.
     */
    protected ListPaginatedStore() {
        this.keywords = null;
        populate (records);
    }

    /**
     * Construct with a keyword filter. The filter takes a record and keywords (only
     * if assigned) and returns {@code true} if filtered in.
     * 
     * @param filter
     *               the keyword filter mechanism.
     */
    public ListPaginatedStore(BiPredicate<R,String> filter) {
        super();
        this.filter = (v,k) -> StringSupport.empty(k) || filter.test(v, k);
        this.keywords = "";
        populate(records);
    }

    /**
     * Construct with a non-keyword filter. The filter takes a record and returns
     * {@code true} if filtered in.
     * 
     * @param filter
     *               the filter mechanism.
     */
    public ListPaginatedStore(Predicate<R> filter) {
        super();
        this.filter = (v,k) -> filter.test(v);
        this.keywords = null;
        populate(records);
    }

    /**
     * Refresh the list with the given items.
     * 
     * @param source
     *               the items.
     */
    public void refresh(List<R> source) {
        this.records.clear();
        this.records.addAll(source);
        reload();
    }

    /**
     * A alternative to loading the records is to overrides this method and populate
     * the passed list.
     * 
     * @param records
     *                the records list to populate.
     */
    protected void populate(List<R> records) {
        // Nothing.
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
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.PaginatedStore#requestLoad(int, int,
     *      com.effacy.jui.core.client.store.PaginatedStore.ILoadRequestCallback)
     */
    @Override
    protected void requestLoad(int page, int pageSize, ILoadRequestCallback<R> cb) {
        // Filter the records as needed.
        List<R> filtered = new ArrayList<>(records);
        if (filter != null)
            filtered.removeIf(v -> !filter.test(v,keywords));

        // Extract out from the filtered list the relevant page (or partial page).
        List<R> subset = new ArrayList<>();
        int fromIndex = page * pageSize;
        if (fromIndex < filtered.size()) {
            int toIndex = Math.min(fromIndex + pageSize, filtered.size());
            subset.addAll(filtered.subList(fromIndex, toIndex));
        }
        Logger.log (getClass ().getSimpleName () + " filtered=" + (filtered != null) + " page=" + page + " pageSize=" + pageSize + " results=" + records.size ());
        cb.onSuccess(subset, filtered.size(), false);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.store.PaginatedStore#clear()
     */
    @Override
    public void clear() {
        if (this.keywords != null)
            this.keywords = "";
        else
            this.filter = null;
        super.clear ();
    }

    @Override
    public void clearFilter() {
        if (this.keywords != null)
            this.keywords = "";
        else
            this.filter = null;
        reload (10);
    }

    /**
     * Perform a keyword filter with the given keywords.
     * <p>
     * This requires that a keyword filter has been provided.
     * 
     * @param keywords
     *                 the keywords to filter on.
     */
    @Override
    public void filter(String keywords) {
        if (this.keywords != null)
            this.keywords = (keywords == null) ? "" : keywords;
        reload(10);
    }

    /**
     * Replaces the filter last used with a new filter. Note that this will negate
     * any keyword filter that has been set and so {@link #filter(String)} will no
     * longer work.
     */
    @Override
    public void filter(Predicate<R> filter) {
        this.keywords = null;
        this.filter = (v,k) -> filter.test(v);
        reload (10);
    }
}

