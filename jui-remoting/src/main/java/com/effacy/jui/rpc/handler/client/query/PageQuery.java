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

import com.effacy.jui.json.annotation.Transient;

public class PageQuery<T> extends Query<T> implements IPageQuery {

    /**
     * The page of the pagination.
     */
    private int page = 0;

    /**
     * The page size.
     */
    private int pageSize = -1;

    /**
     * Construct an unlimited pagination.
     */
    public PageQuery() {
        // Nothing.
    }

    /**
     * Copy constructor.
     * 
     * @param copy
     *             the query to copy.
     */
    public PageQuery(IPageQuery copy) {
        this.page = copy.getPage ();
        this.pageSize = copy.getPageSize ();
    }

    /**
     * Construct a pagination with the specified page and page size.
     * 
     * @param page
     *                 the page in the pagination.
     * @param pageSize
     *                 the number of items per page.
     */
    public PageQuery(int page, int pageSize) {
        setPage (page);
        setPageSize (pageSize);
    }

    /**
     * Gets the page (zero indexed).
     * 
     * @return The page (if unlimited then this will always be 0).
     */
    public int getPage() {
        return isUnlimited () ? 0 : page;
    }

    /**
     * Gets the page as a whole number (this is indexed from 1 rather than 0).
     * 
     * @return The page as a whole number.
     */
    @Transient
    public int getWholePage() {
        return getPage () + 1;
    }

    /**
     * Sets the page.
     * 
     * @param page
     *             the page (if less than zero then will be treated as zero).
     */
    public void setPage(int page) {
        this.page = (page < 0) ? 0 : page;
    }

    /**
     * Gets the page size.
     * 
     * @return The page size (if unlimited this will be {@link Integer#MAX_VALUE}).
     */
    public int getPageSize() {
        return isUnlimited () ? Integer.MAX_VALUE : pageSize;
    }

    /**
     * Sets the page size (number of items per page).
     * 
     * @param pageSize
     *                 the page size (if less than or equal to zero then will be
     *                 treated as an unlimited pagination).
     */
    public void setPageSize(int pageSize) {
        this.pageSize = (pageSize <= 0) ? -1 : pageSize;
    }

    /**
     * Determines if the paging is unlimited.
     * 
     * @return {@code true} if paging is unlimited.
     */
    @Transient
    public boolean isUnlimited() {
        return (pageSize <= 0);
    }

    /**
     * Gets the start index for pagination. This is 0 if the paging is unlimited,
     * otherwise it is the start page times the page size.
     * 
     * @return The (zero indexed) starting index.
     */
    public int startIndex() {
        return isUnlimited () ? 0 : page * pageSize;
    }

    /**
     * Gets the (inclusive) end index for pagination. This is
     * {@link Integer#MAX_VALUE} if paging is unlimited, otherwise it is the start
     * index plus the page size less one.
     * 
     * @return The (zero index) inclusive end index.
     */
    public int endIndex() {
        return isUnlimited () ? Integer.MAX_VALUE : (((page + 1) * pageSize) - 1);
    }

    /**
     * Determines if the pagination is on the first page (page zero).
     * 
     * @return {@code true} if it is.
     */
    public boolean firstPage() {
        return (getPage () == 0);
    }

    /**
     * Gets the total number of items having been paged through based on the current
     * page and the page size. For example this will be 50 if the current page is 5
     * (the 6th physical page as page indexing starts from 0) and the page size is
     * 10.
     * 
     * @return The total number of prior items/
     */
    public long pagedSize() {
        return getPage () * getPageSize ();
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
