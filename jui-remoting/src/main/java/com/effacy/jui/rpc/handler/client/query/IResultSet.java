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

import java.util.List;

/**
 * Represents a collection of results from performing a query on a service.
 * Queries often include a specification for a range of the total number of
 * results to be returned (this implements paging on the client and as an
 * efficiency to ensure on the records needed will be returned). The result set
 * captures all the information needed to understand the results being returned
 * as well as the range of the complete result set that is available from the
 * query.
 * 
 * @author Jeremy Buckley
 */
public interface IResultSet<T> extends Iterable<T> {

    /**
     * Gets the total number of available records from the query.
     * 
     * @return The total number of records.
     */
    public int getTotalResults();


    /**
     * The size of the result set.
     * 
     * @return The number of items contained in the result set.
     */
    public int size();


    /**
     * Converts the results to a list.
     * 
     * @return The list of results.
     */
    public List<T> asList();

}
