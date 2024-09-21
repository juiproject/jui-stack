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

/**
 * Abstraction of a remote execution.
 * 
 * @author Jeremy Buckley
 */
public interface IRequestExecution<T> {

    /**
     * If the request should be processed immediately rather than being batch.
     * 
     * @return {@code true} if it, and possibly any other batched requests,
     *         should be processed immediately.
     */
    public boolean immediate();


    /**
     * Resends the request.
     */
    public void resend();


    /**
     * Cancels the request.
     */
    public void cancel();


    /**
     * Gets the TID for the request.
     * 
     * @return The transition ID.
     */
    public long getTid();


    /**
     * Gets the request body for the transaction.
     * 
     * @return The request body.
     */
    public String getRequest();


    /**
     * Gets the callback method.
     * 
     * @return The callback.
     */
    public IRemoteMethodCallback<T> getCallback();
}
