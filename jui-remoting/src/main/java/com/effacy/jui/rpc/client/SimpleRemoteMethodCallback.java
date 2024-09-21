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
 * Convenience abstract implementation of {@link IRemoteMethodCallback} that
 * provides delegates the transport and validation error through to the
 * {@link #onError(Object, List, RemoteResponseType)} method. If a transport
 * error is encountered, all fields are {@code null}.
 * 
 * @author Steve Baker
 * @author Jeremy Buckley
 */
public abstract class SimpleRemoteMethodCallback<T> implements IRemoteMethodCallback<T> {

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onError(java.lang.Object,
     *      java.util.List,
     *      com.effacy.jui.remote.response.dto.remote.response.RemoteResponseType)
     */
    @Override
    public abstract void onError(T response, List<ErrorMessage> messages, RemoteResponseType status);


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onSuccess(java.lang.Object,
     *      java.util.List)
     */
    @Override
    public abstract void onSuccess(T response, List<ErrorMessage> messages);


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethodCallback#onTransportError(java.lang.Object)
     */
    @Override
    public void onTransportError(String message) {
        onError (null, null, null);
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
        onError (response, messages, RemoteResponseType.ERROR_VALIDATION);
    }

}
