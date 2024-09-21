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

import com.effacy.jui.rpc.handler.client.IConverter;

public abstract class OffsetResultSet<V,T> extends ResultSet<V> implements IOffsetResultSet<V,T> {
    
    private T nextPage;

    /**
     * Constructs the result set.
     * 
     * @param results
     *                     the results as an iterable (if {@code null} then is
     *                     treated as empty).
     * @param totalResults
     *                     the total number of results (that would be returned
     *                     without pagination) which may be negative and the total
     *                     will be taken as the size of the results.
     * @param pagination
     *                     the pagination data that the results have been subject
     *                     to.
     */
    protected OffsetResultSet(Iterable<V> results, int totalResults, T nextPage) {
        super(results, totalResults);
        this.nextPage = nextPage;
    }

    /**
     * Constructs the result set.
     * 
     * @param results
     *                     the results as an iterable over the source class.
     * @param converter
     *                     the converter to convert from the source class toå the
     *                     return type class for this result set.
     * @param totalResults
     *                     the total number of results (that would be returned
     *                     without pagination) which may be negative and the total
     *                     will be taken as the size of the results.
     * @param pagination
     *                     the pagination data that the results have been subject
     *                     to.
     */
    public <S> OffsetResultSet(Iterable<S> results, IConverter<S, V> converter, int totalResults, T nextPage) {
        super (results, converter, totalResults);
        this.nextPage = nextPage;
    }

    @Override
    public T getNextPage() {
        return nextPage;
    }

    public void setNextPage(T nextPage) {
        this.nextPage = nextPage;
    }
}
