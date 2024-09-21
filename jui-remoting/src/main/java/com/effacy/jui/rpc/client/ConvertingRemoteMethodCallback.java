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
 * Converts the base response type of a callback to that expected by another
 * type of callback. The default is to use a {@code null} conversion, but the
 * behaviour can be changed by sub-classing and implementing
 * {@link #convert(Object)}.
 * 
 * @author Jeremy Buckley
 */
public class ConvertingRemoteMethodCallback<T, B> implements IRemoteMethodCallback<T> {

    /**
     * The method callback being delegated to.
     */
    private IRemoteMethodCallback<B> delegate;


    /**
     * Constructs a delegating callback.
     * 
     * @param delegate
     *            the delegate.
     */
    public ConvertingRemoteMethodCallback(IRemoteMethodCallback<B> delegate) {
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onSuccess(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onSuccess(T response, List<ErrorMessage> messages) {
        delegate.onSuccess (convert (response), messages);
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
        delegate.onError (convert (response), messages, status);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onValidationError(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public void onValidationError(T response, List<ErrorMessage> messages) {
        delegate.onValidationError (convert (response), messages);
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


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onCancel()
     */
    @Override
    public void onCancel() {
        delegate.onCancel ();
    }


    /**
     * Converts the actual response to the response expected by the delegate.
     * The default behaviour is to return {@code null}.
     * 
     * @param response
     *            the response received.
     * @return The response expected by the delegate.
     */
    protected B convert(T response) {
        return null;
    }

}
