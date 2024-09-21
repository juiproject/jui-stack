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

import java.util.List;

/**
 * A method callback that is parameterized over a response type {@code R} but
 * delegates to a callback that is parameterized over {@link QueryResponse<R,Q>}
 * . This provides a means to include query in the response.
 * 
 * @author Jeremy Buckley
 */
public class QueryResponseRemoteMethodCallback<Q, R> extends RemoteMethodCallback<R> {

    /**
     * The delegate.
     */
    private IRemoteMethodCallback<QueryResponse<Q, R>> delegate;

    /**
     * The query.
     */
    private Q query;


    /**
     * Constructs a delegating callback.
     * 
     * @param query
     *            the query.
     * @param delegate
     *            the delegate.
     */
    public QueryResponseRemoteMethodCallback(Q query, IRemoteMethodCallback<QueryResponse<Q, R>> delegate) {
        this.delegate = delegate;
        this.query = query;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onSuccess(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onSuccess(R response, List<ErrorMessage> messages) {
        delegate.onSuccess (new QueryResponse<Q, R> (query, response), messages);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onError(java.lang.Object,
     *      java.util.List,
     *      com.effacy.jui.remote.response.dto.remote.response.RemoteResponseType)
     */
    @Override
    public void onError(R response, List<ErrorMessage> messages, RemoteResponseType status) {
        delegate.onError (new QueryResponse<Q, R> (query, response), messages, status);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onValidationError(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onValidationError(R response, List<ErrorMessage> messages) {
        delegate.onValidationError (new QueryResponse<Q, R> (query, response), messages);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onTransportError(java.lang.String)
     */
    @Override
    public void onTransportError(String message) {
        delegate.onTransportError (message);
    }
}
