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

import java.util.ArrayList;
import java.util.List;

import elemental2.dom.DomGlobal;


/**
 * Convenience abstract implementation of {@link IRemoteMethodCallback} that
 * provides default implementations of the error handlers.
 * 
 * @author Steve Baker
 * @author Jeremy Buckley
 */
public abstract class RemoteMethodCallback<T> implements IRemoteMethodCallback<T> {

    /**
     * The optional error call back.
     */
    protected IRemoteMethodCallback<T> callback;

    /**
     * Set this if debugging information should be included.
     */
    public static boolean DEBUG = false;

    /**
     * Default constructor. Sub-classes should override
     * {@link #onError(Object, List, RemoteResponseType)} so as to correctly
     * handle error cases.
     */
    protected RemoteMethodCallback() {
        // Nothing.
    }


    /**
     * Construct with a call-back. The call-back will be invoked if an error
     * occurs (see
     * {@link IRemoteMethodCallback#onError(Object, List, RemoteResponseType)}
     * and others) but no response will every be passed through (as the
     * parameter type may vary).
     * 
     * @param callback
     *            the (optional) call back.
     */
    protected RemoteMethodCallback(IRemoteMethodCallback<T> callback) {
        this.callback = callback;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onError(java.lang.Object,
     *      java.util.List,
     *      com.effacy.jui.remote.response.dto.remote.response.RemoteResponseType)
     */
    @Override
    public void onError(T response, List<ErrorMessage> messages, RemoteResponseType status) {
        if (callback != null) {
            callback.onError (null, messages, status);
            onComplete (false);
        } else if (alert (messages, status))
            onComplete (false);
    }


    /**
     * Generates a error alert.
     * 
     * @param messages
     *            the messages returned.
     * @param status
     *            the status.
     * @return {@code true} if {@link #onComplete(boolean)} should be called.
     */
    protected boolean alert(List<ErrorMessage> messages, RemoteResponseType status) {
        if (DEBUG)
            DomGlobal.window.alert ("Sorry, an error occurred.  Try again later. " + getClass ().getName ());
        else
            DomGlobal.window.alert ("Sorry, an error occurred.  Try again later. ");
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onSuccess(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onSuccess(T response, List<ErrorMessage> messages) {
        onComplete (true);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onTransportError(java.lang.Object)
     */
    @Override
    public void onTransportError(String message) {
        if (callback != null) {
            callback.onTransportError (message);
        } else {
            List<ErrorMessage> messages = new ArrayList<ErrorMessage> ();
            messages.add (new ErrorMessage (null, message));
            this.onError (null, messages, RemoteResponseType.ERROR);
        }
        onComplete (false);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onValidationError(java.lang.Object,
     *      com.google.gwt.core.client.JavaScriptObject,
     *      com.simplar.js.core.client.remote.response.FieldMessages)
     */
    @Override
    public void onValidationError(T response, List<ErrorMessage> messages) {
        if (callback != null)
            callback.onValidationError (null, messages);
        else
            this.onError (response, messages, RemoteResponseType.ERROR_VALIDATION);
        onComplete (false);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onCancel()
     */
    @Override
    public void onCancel() {
        if (callback != null)
            callback.onCancel ();
    }


    /**
     * Convenient completion callback.
     * 
     * @param success
     *            {@code true} if successful.
     */
    protected void onComplete(boolean success) {
        // Nothing.
    }

}
