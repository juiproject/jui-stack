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
 * Often there is a problem where a remote call may become obsolete and the
 * response no longer needed and a subsequent call takes presedence. For example
 * a list of links, each when clicked on displays some data in an area. Clicking
 * on two links rapidly normally results in a flicker of the data area where the
 * first item is displayed followed by the second. In a worst case scenario,
 * since the calls are asynchronous, it is possible for the response to the
 * first link to come after the response to the second, resulting in the wrong
 * data being displayed.
 * <p>
 * This class enables calls to be tracked, so only the data from the last
 * invoked call will be passed through to the callback.
 * <p>
 * Sub-classes must implement the
 * {@link #onExecute(Object, IRemoteMethodCallback)} method that actually
 * performs the remote call. An assumption is made that the query data needed to
 * be passed to the call can be encapsulated into a single data type. If no data
 * is required, the type this as {@code Void}.
 * 
 * @author Jeremy Buckley
 */
public abstract class TrackedRemoteMethodInvoker<Q, R> extends RemoteMethodCallback<QueryResponse<Q, R>> {

    /**
     * The tracking reference.
     */
    private long trackingReference = 0L;


    /**
     * Invokes the query.
     * 
     * @param query
     *            the query.
     * @param cb
     *            the callback.
     */
    public void invoke(Q query) {
        onExecute (query, new LocalRemoteMethodCallback (++trackingReference, query, this));
    }


    /**
     * Invokes the query with a callback that includes passing the query.
     * 
     * @param query
     *            the query.
     * @param cb
     *            the callback.
     */
    public void invoke(Q query, IRemoteMethodCallback<QueryResponse<Q, R>> cb) {
        onExecute (query, new LocalRemoteMethodCallback (++trackingReference, query, cb));
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.RemoteMethodCallback#onSuccess(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onSuccess(QueryResponse<Q, R> response, List<ErrorMessage> messages) {
        // Nothing.

    }


    /**
     * This is called to actually execute a remote call.
     * 
     * @param query
     *            the query to perform.
     * @param cb
     *            the callback to call against.
     */
    protected abstract void onExecute(Q query, IRemoteMethodCallback<R> cb);

    /**
     * Local callback that will determine if the underlying callback should be
     * invoked.
     */
    public class LocalRemoteMethodCallback implements IRemoteMethodCallback<R> {

        /**
         * The current tracking reference.
         */
        private Long reference;

        /**
         * The query.
         */
        private Q query;

        /**
         * The callback to invoke.
         */
        private IRemoteMethodCallback<QueryResponse<Q, R>> callback;


        /**
         * Construct with a tracking reference and a callback.
         * 
         * @param reference
         *            the tracking reference.
         * @param callback
         *            the callback to invoke.
         */
        public LocalRemoteMethodCallback(Long reference, Q query, IRemoteMethodCallback<QueryResponse<Q, R>> callback) {
            this.reference = reference;
            this.query = query;
            this.callback = callback;
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onSuccess(java.lang.Object,
         *      java.util.List)
         */
        @Override
        public void onSuccess(R response, List<ErrorMessage> messages) {
            if (trackingReference == reference)
                callback.onSuccess (new QueryResponse<Q, R> (query, response), messages);
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
            if (trackingReference == reference)
                callback.onError (new QueryResponse<Q, R> (query, response), messages, status);
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onValidationError(java.lang.Object,
         *      java.util.List)
         */
        @Override
        public void onValidationError(R response, List<ErrorMessage> messages) {
            if (trackingReference == reference)
                callback.onValidationError (new QueryResponse<Q, R> (query, response), messages);
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onTransportError(java.lang.String)
         */
        @Override
        public void onTransportError(String message) {
            if (trackingReference == reference)
                callback.onTransportError (message);
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onCancel()
         */
        @Override
        public void onCancel() {
            if (trackingReference == reference)
                callback.onCancel ();
        }

    }
}
