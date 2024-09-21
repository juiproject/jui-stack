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
public abstract class ResultSet<T> extends Result implements IResultSet<T> {

    /**
     * Describes an outcome of a result set query.
     */
    public enum Outcome {
        /**
         * Successful outcome.
         */
        SUCCESS,

        /**
         * Error processing query.
         */
        ERROR,

        /**
         * No results due to a query constraint (i.e. require a certain number of
         * characters in a keyword query).
         */
        THRESHOLD;
    }

    /**
     * See {@link #getOutcome()}.
     */
    private Outcome outcome;

    /**
     * See {@link #getMessage()}.
     */
    private String message;

    /**
     * See {@link #getTotalResults()}.
     */
    private int totalResults;

    /**
     * See {@link #getResults()}.
     */
    private List<T> results;

    /**
     * Default constructor.
     */
    protected ResultSet() {
        super ();
    }

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
    protected ResultSet(Iterable<T> results, int totalResults) {
        if (results instanceof List) {
            this.results = (List<T>) results;
            this.totalResults = Math.max (this.results.size (), totalResults);
        } else {
            this.results = new ArrayList<T> ();
            this.totalResults = 0;
            if (results != null) {
                for (T result : results) {
                    this.results.add (result);
                    this.totalResults++;
                }
            }
            if (totalResults > this.totalResults)
                this.totalResults = totalResults;
        }
    }

    /**
     * Constructs the result set.
     * 
     * @param results
     *                     the results as an iterable over the source class.
     * @param converter
     *                     the converter to convert from the source class to the
     *                     return type class for this result set.
     * @param totalResults
     *                     the total number of results (that would be returned
     *                     without pagination) which may be negative and the total
     *                     will be taken as the size of the results.
     * @param pagination
     *                     the pagination data that the results have been subject
     *                     to.
     */
    public <S> ResultSet(Iterable<S> results, IConverter<S, T> converter, int totalResults) {
        this (Converter.convert (results, converter), totalResults);
    }

    /**
     * The outcome of the result.
     * 
     * @return the outcome.
     */
    public Outcome getOutcome() {
        if (outcome == null)
            outcome = Outcome.SUCCESS;
        return outcome;
    }

    /**
     * Setter for {@link #getOutcome()}.
     */
    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    /**
     * Any supporting message for the outcome (generally only applicable for non
     * {@link Outcome#SUCCESS} outcomes).
     * 
     * @return the supporting message (if relevant or available).
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for {@link #getMessage()}.
     */
    public void setMessage(String message) {
        this.message = message;
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
     * Setter for {@link #getTotalResults()}.
     */
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * Obtains the underlying results for the page in question.
     * 
     * @return the results.
     */
    public List<T> getResults() {
        if (results == null)
            results = new ArrayList<T> ();
        return results;
    }

    /**
     * Setter for {@link #getResults()}.
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
        if (results == null)
            return 0;
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
