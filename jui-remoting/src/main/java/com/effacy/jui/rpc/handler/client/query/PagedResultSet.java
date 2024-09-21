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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.rpc.handler.client.Converter;
import com.effacy.jui.rpc.handler.client.IConverter;

/**
 * Represents a collection of results from a query. This includes range data and
 * total number of results. Generally used for applications where data is
 * presented in a paginated grid or table.
 * <p>
 * Although this is annotated with {@link JsonSerializable} the class is
 * abstract to ensure it is not instantiated directly. In order to be
 * deserializable at the client it must be sub-classes to a non-generic.
 * 
 * @author Jeremy Buckley
 * @param <T>
 *            The underlying base type.
 */
@JsonSerializable
public abstract class PagedResultSet<T> extends PageQuery<T> implements IPagedResultSet<T> {

    /**
     * The total number of results for the query without consideration for
     * pagination.
     */
    private int totalResults;

    /**
     * The results subject to pagination.
     */
    private List<T> results;

    /**
     * Default constructor.
     */
    protected PagedResultSet() {
        super ();
    }


    /**
     * Constructs the result set.
     * 
     * @param results
     *            the results as an iterable.
     * @param totalResults
     *            the total number of results (that would be returned without
     *            pagination) which may be negative and the total will be taken
     *            as the size of the results.
     * @param pagination
     *            the pagination data that the results have been subject to.
     */
    protected PagedResultSet(Iterable<T> results, int totalResults, IPageQuery pagination) {
        super (pagination);
        if (results instanceof List) {
            this.results = (List<T>) results;
            this.totalResults = Math.max (this.results.size (), totalResults);
        } else {
            this.results = new ArrayList<T> ();
            this.totalResults = 0;
            for (T result : results) {
                this.results.add (result);
                this.totalResults++;
            }
            if (totalResults > this.totalResults)
                this.totalResults = totalResults;
        }
    }


    /**
     * Constructs the result set.
     * 
     * @param results
     *            the results as an iterable over the source class.
     * @param converter
     *            the converter to convert from the source class to the return
     *            type class for this result set.
     * @param totalResults
     *            the total number of results (that would be returned without
     *            pagination) which may be negative and the total will be taken
     *            as the size of the results.
     * @param pagination
     *            the pagination data that the results have been subject to.
     */
    protected <S> PagedResultSet(Iterable<S> results, IConverter<S, T> converter, int totalResults, IPageQuery pagination) {
        this (Converter.convert (results, converter), totalResults, pagination);
    }


    /**
     * Gets the total number of results for the query without consideration for
     * pagination.
     * 
     * @return The total number of results.
     */
    public int getTotalResults() {
        return totalResults;
    }


    /**
     * Sets the results.
     * 
     * @param totalResults
     *            the results.
     */
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }


    /**
     * Gets the underlying results.
     * 
     * @return The results.
     */
    public List<T> getResults() {
        if (results == null)
            results = new ArrayList<T> ();
        return results;
    }


    /**
     * Sets the results.
     * 
     * @param results
     *            the results.
     */
    public void setResults(List<T> results) {
        this.results = results;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.shared.IResultSet#asList()
     */
    @Override
    public List<T> asList() {
        return getResults ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Collection#size()
     */
    @Override
    public int size() {
        return results.size ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        if (results == null)
            return Collections.EMPTY_LIST.iterator ();
        return results.iterator ();
    }

}
