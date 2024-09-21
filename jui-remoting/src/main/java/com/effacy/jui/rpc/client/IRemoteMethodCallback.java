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

import com.effacy.jui.rpc.extdirect.client.ExtDirectRemoteMethodCallModifier;

/**
 * Callback handler for a remote method.
 * <p>
 * Note, one may modify the behaviour of a specific call though
 * {@link ExtDirectRemoteMethodCallModifier}. General behaviours are handled through
 * {@link ExtDirectRemoteMethodConfigurator} and related.
 * 
 * @author Jeremy Buckley
 */
public interface IRemoteMethodCallback<T> {

    /**
     * Called when the method call was successful.
     * 
     * @param context
     *            the context that was passed to the invocation.
     * @param response
     *            the response object.
     * @param messages
     *            any associated messages (can be {@code null}).
     */
    public void onSuccess(T response, List<ErrorMessage> messages);


    /**
     * Called when the method call was in error (but not a validation error).
     * 
     * @param response
     *            the response object.
     * @param messages
     *            any associated messages (can be {@code null}).
     * @param status
     *            the response status.
     */
    public void onError(T response, List<ErrorMessage> messages, RemoteResponseType status);


    /**
     * Called when the method call was in error (but not a validation error).
     * 
     * @param response
     *            the response object.
     * @param messages
     *            any associated messages (can be {@code null}).
     * @param status
     *            the response status.
     */
    public void onValidationError(T response, List<ErrorMessage> messages);


    /**
     * Called when there was a problem communicating with the remote service.
     */
    public void onTransportError(String message);


    /**
     * Called if the communication was cancelled (generally by the user).
     */
    public void onCancel();

}
