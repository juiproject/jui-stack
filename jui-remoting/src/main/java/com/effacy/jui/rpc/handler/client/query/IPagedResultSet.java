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

/**
 * This extends {@link IResultSet} to add information about pagination applies
 * to the result set.
 * <p>
 * In most cases {@link IResultSet} will be sufficient and the information
 * returned (number of results and total results) along with the pagination data
 * passed in the first place will be enough to precisely determine the locality
 * of the results returned.
 * 
 * @author Jeremy Buckley
 */
public interface IPagedResultSet<T> extends IResultSet<T> {

    /**
     * Gets the page (zero indexed).
     * 
     * @return The page (if unlimited then this will always be 0).
     */
    public int getPage();


    /**
     * Gets the page size.
     * 
     * @return The page size (if unlimited this will be
     *         {@link Integer#MAX_VALUE}).
     */
    public int getPageSize();


    /**
     * Determines if the paging is unlimited.
     * 
     * @return {@code true} if paging is unlimited.
     */
    public boolean isUnlimited();

}
