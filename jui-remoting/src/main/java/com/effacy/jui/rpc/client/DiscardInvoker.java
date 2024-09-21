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

import org.gwtproject.core.client.Scheduler.ScheduledCommand;
import org.gwtproject.timer.client.Timer;


/**
 * There are cases whereby a remote method could be invoked multiple times in
 * fast succession, with invocation occurring prior to responses being received
 * from prior invocations. In this case a reasonable model is to discard the
 * results from prior invocations and only rely on the response from the most
 * recent one. An example of this is a type-ahead query that allows a user to
 * type characters one at a time but where each keypress effectively discards
 * the results of the prior keypress. Rather than slow the mechanism down or
 * "flash" results to the user, the system stays quite until the user stops
 * typing, then displays matching results.
 * <p>
 * This class implements this behaviour by allowing multiple invocations while
 * responding only to the last invocation. In addition, if multiple invocations
 * are fast enough, then prior requests will not be processed.
 * 
 * @author Jeremy Buckley
 * @param <Q>
 *            type that holds data to be passed to the remote method.
 * @param <V>
 *            the response type of the remote method.
 */
public abstract class DiscardInvoker<Q, V> extends RemoteMethodCallback<V> {

    /**
     * The most recent request that is to be, or is being processed.
     */
    private LoadRequest requestToProcess = null;


    /**
     * Executes a load request.
     * 
     * @param query
     *            the query to perform.
     * @param delay
     *            the delay to apply to executing the query.
     */
    public void load(Q query, int delay) {
        load (query, delay, this);
    }


    /**
     * Executes a load request.
     * 
     * @param query
     *            the query to perform.
     * @param delay
     *            the delay to apply to executing the query.
     * @param cb
     *            the callback to use (rather than this).
     */
    public void load(Q query, int delay, IRemoteMethodCallback<V> cb) {
        LoadRequest loadRequest = new LoadRequest (query, delay, cb);
        if ((requestToProcess == null) || requestToProcess.cancel ()) {
            requestToProcess = loadRequest;
            requestToProcess.invoke ();
        } else {
            requestToProcess = loadRequest;
        }
    }


    /**
     * Implements the actual invocation.
     * 
     * @param query
     *            the query data to pass.
     * @param cb
     *            the call back.
     */
    protected abstract void invoke(Q query, IRemoteMethodCallback<V> cb);


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.RemoteMethodCallback#onSuccess(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onSuccess(V response, List<ErrorMessage> messages) {
        // Nothing.
    }


    /**
     * Called prior to a load being invoked. Could be called multiple times.
     */
    protected void onBeforeLoad() {
        // Nothing.
    }


    /**
     * Called immediately after a load. If this method returns {@code false}
     * then one of the {@link IRemoteMethodCallback} methods on this instances
     * will be invoked (this is the default). This allows one to alter behaviour
     * of the callback using information passed in the query that would not
     * otherwise be available (for example, preventing a dialog appearing when
     * there is an error).
     * 
     * @param query
     *            the query that initiated the response.
     * @param response
     *            the response (if the query was not successful this may be
     *            {@code null}.
     * @param success
     *            {@code true} if the load was successful.
     * @return {@code true} if this method has fully handled the response.
     */
    protected boolean onAfterLoad(Q query, V response, boolean success) {
        return false;
    }

    /**
     * This encapsulates a request to load records for the loader.
     */
    class LoadRequest implements ScheduledCommand, IRemoteMethodCallback<V> {

        /**
         * The query to perform.
         */
        private Q query;

        /**
         * Any delay to apply prior to loading.
         */
        private int delay = 0;

        /**
         * Flag indicating that the request has started.
         */
        private boolean started = false;

        /**
         * Flag indicating that the request has cancelled.
         */
        private boolean cancelled = false;

        /**
         * Task for implementing delays.
         */
        private Timer task = null;

        /**
         * The callback to invoke.
         */
        private IRemoteMethodCallback<V> cb;


        /**
         * Constructs a request.
         * 
         * @param delay
         *            any delay to apply prior to initiating a load.
         */
        public LoadRequest(Q query, int delay, IRemoteMethodCallback<V> cb) {
            this.query = query;
            this.delay = delay;
            this.cb = cb;
        }


        /**
         * Invokes the load. If the request has cancelled or is in play the this
         * will return. Prior initiating it will fire (through the store) a
         * before load event. It will then call {@link #execute()} directly if
         * there is no delay or will call it via the delay task.
         */
        public void invoke() {
            if (cancelled || (task != null))
                return;
            DiscardInvoker.this.onBeforeLoad ();
            if (delay <= 0) {
                execute ();
            } else {
                task = new Timer () {

                    @Override
                    public void run() {
                        try {
                            execute ();
                        } finally {
                            // Nothing.
                        }
                    }
                };
                task.schedule (delay);
            }
        }


        /**
         * {@inheritDoc}
         * 
         * @see org.gwtproject.core.client.Scheduler.ScheduledCommand#execute()
         */
        public void execute() {
            started = true;
            task = null;
            DiscardInvoker.this.invoke (query, this);
        }


        /**
         * Cancels the request (if the request is delayed). Any assigned
         * listener will receive a {@link LoadStoreEvent.Cancelled} event.
         * 
         * @return {@code true} if the cancel was successful.
         */
        public boolean cancel() {
            if (started)
                return false;
            if (cancelled)
                return true;
            cancelled = true;
            if (task != null) {
                task.cancel ();
                task = null;
            }
            return true;
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onSuccess(java.lang.Object,
         *      java.util.List)
         */
        @Override
        public void onSuccess(V response, List<ErrorMessage> messages) {
            if ((requestToProcess != null) && !requestToProcess.equals (this)) {
                requestToProcess.invoke ();
            } else {
                requestToProcess = null;
                if (!DiscardInvoker.this.onAfterLoad (query, response, true))
                    cb.onSuccess (response, messages);
            }
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onError(java.lang.Object,
         *      java.util.List,
         *      com.effacy.jui.remote.response.RemoteResponseType)
         */
        @Override
        public void onError(V response, List<ErrorMessage> messages, RemoteResponseType status) {
            if ((requestToProcess != null) && !requestToProcess.equals (this)) {
                requestToProcess.invoke ();
            } else {
                requestToProcess = null;
                if (!DiscardInvoker.this.onAfterLoad (query, response, false))
                    cb.onError (response, messages, status);
            }
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onValidationError(java.lang.Object,
         *      java.util.List)
         */
        @Override
        public void onValidationError(V response, List<ErrorMessage> messages) {
            if ((requestToProcess != null) && !requestToProcess.equals (this)) {
                requestToProcess.invoke ();
            } else {
                requestToProcess = null;
                if (!DiscardInvoker.this.onAfterLoad (query, response, false))
                    cb.onValidationError (response, messages);
            }
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onTransportError(java.lang.String)
         */
        @Override
        public void onTransportError(String message) {
            if ((requestToProcess != null) && !requestToProcess.equals (this)) {
                requestToProcess.invoke ();
            } else {
                requestToProcess = null;
                if (!DiscardInvoker.this.onAfterLoad (query, null, false))
                    cb.onTransportError (message);
            }
        }


        /**
         * {@inheritDoc}
         * 
         * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onCancel()
         */
        @Override
        public void onCancel() {
            if ((requestToProcess != null) && !requestToProcess.equals (this)) {
                requestToProcess.invoke ();
            } else {
                requestToProcess = null;
                if (!DiscardInvoker.this.onAfterLoad (query, null, false))
                    cb.onCancel ();
            }
        }

    }
}
