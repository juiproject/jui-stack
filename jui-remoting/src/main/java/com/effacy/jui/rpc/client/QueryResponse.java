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
package com.effacy.jui.rpc.client;

/**
 * A useful way of bundling a query and a response to become a new response.
 * 
 * @author Jeremy Buckley
 */
public class QueryResponse<Q, R> {

    /**
     * The query.
     */
    private Q query;

    /**
     * The response.
     */
    private R response;


    /**
     * Default constructor.
     * 
     * @param query
     *            the query.
     * @param response
     *            the response.
     */
    public QueryResponse(Q query, R response) {
        this.query = query;
        this.response = response;
    }


    /**
     * Gets the query.
     * 
     * @return The query.
     */
    public Q getQuery() {
        return query;
    }


    /**
     * Gets the response.
     * 
     * @return The response.
     */
    public R getResponse() {
        return response;
    }
}
