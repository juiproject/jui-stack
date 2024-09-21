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
 * Remote response that always generates an error.
 * 
 * @author Jeremy Buckley
 */
public class ErrorRemoteResponse extends RemoteResponse<Void> {

    /**
     * Fail response with the {@link RemoteResponseType#VALIDATION}.
     * 
     * @param messages
     *            the messages to fail against.
     */
    public ErrorRemoteResponse(List<ErrorMessage> messages) {
        super (RemoteResponseType.ERROR, messages);
    }


    /**
     * Fail response with the {@link RemoteResponseType#VALIDATION}.
     * 
     * @param messages
     *            the messages to fail against.
     */
    public ErrorRemoteResponse(ErrorMessage... messages) {
        super (RemoteResponseType.ERROR, messages);
    }


    /**
     * Fail response with provided response type outcome.
     * 
     * @param outcome
     *            the outcome of the operation.
     */
    public ErrorRemoteResponse(RemoteResponseType outcome) {
        super (outcome);
    }


    /**
     * Fail response with provided response type outcome.
     * 
     * @param outcome
     *            the outcome of the operation.
     * @param messages
     *            the messages to fail against.
     */
    public ErrorRemoteResponse(RemoteResponseType outcome, List<ErrorMessage> messages) {
        super (outcome, messages);
    }


    /**
     * Fail response with provided response type outcome.
     * 
     * @param outcome
     *            the outcome of the operation.
     * @param messages
     *            the messages to fail against.
     */
    public ErrorRemoteResponse(RemoteResponseType outcome, ErrorMessage... messages) {
        super (outcome, messages);
    }
}
