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
package com.effacy.jui.rpc.handler.client.query;

public abstract class OffsetQuery<V,T> extends Query<V> implements IOffsetQuery<T> {

    /**
     * The page of the pagination.
     */
    private T page;

    /**
     * The batch size.
     */
    private int batchSize = -1;

    /**
     * Construct an unlimited pagination.
     */
    public OffsetQuery() {
        // Nothing.
    }

    /**
     * Copy constructor.
     * 
     * @param copy
     *             the query to copy.
     */
    public OffsetQuery(IOffsetQuery<T> copy) {
        this.page = copy.getPage ();
        this.batchSize = copy.getBatchSize ();
    }

    /**
     * Construct a pagination with the specified page and page size.
     * 
     * @param page
     *                 the page in the pagination.
     * @param batchSize
     *                 the number of items per page.
     */
    public OffsetQuery(T page, int batchSize) {
        setPage (page);
        setBatchSize (batchSize);
    }

    /**
     * Gets the page (zero indexed).
     * 
     * @return The page (if unlimited then this will always be 0).
     */
    public T getPage() {
        return page;
    }

    /**
     * Sets the page.
     * 
     * @param page
     *             the page (if less than zero then will be treated as zero).
     */
    public void setPage(T page) {
        this.page = page;
    }

    /**
     * Gets the page size.
     * 
     * @return the page size (if unlimited this will be {@link Integer#MAX_VALUE}).
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Sets the page size (number of items per page).
     * 
     * @param batchSize
     *                 the page size (if less than or equal to zero then will be
     *                 treated as an unlimited pagination).
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Determines if the query is performing any filtering (meaning that it is
     * attempting to reduce the full set of data by way of restriction separate from
     * paging).
     * 
     * @return {@code true} if it is.
     */
    public boolean filtering() {
        return false;
    }
}
