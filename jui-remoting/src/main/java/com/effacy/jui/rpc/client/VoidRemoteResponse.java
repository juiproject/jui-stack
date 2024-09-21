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
 * Remote response that represents a void.
 * 
 * @author Jeremy Buckley
 */
public class VoidRemoteResponse extends RemoteResponse<Void> {

    /**
     * Default success response.
     */
    public VoidRemoteResponse() {
        super ();
    }


    /**
     * Specify an outcome.
     * 
     * @param outcome
     *            the outcome of the operation.
     */
    public VoidRemoteResponse(RemoteResponseType outcome) {
        super (outcome);
    }


    /**
     * Default fail response.
     * 
     * @param messages
     *            the messages to fail against.
     * @param outcome
     *            the outcome of the operation.
     */
    public VoidRemoteResponse(RemoteResponseType outcome, List<ErrorMessage> messages) {
        super (outcome, messages);
    }


    /**
     * Fail response against the default field.
     * 
     * @param message
     *            the message to record.
     * @param outcome
     *            the outcome of the operation.
     */
    public VoidRemoteResponse(RemoteResponseType outcome, String message) {
        super (outcome, message);
    }


    /**
     * Fail response.
     * 
     * @param outcome
     *            the outcome of the operation.
     * @param field
     *            the field name to record a message against.
     * @param message
     *            the message to record.
     */
    public VoidRemoteResponse(RemoteResponseType outcome, String field, String message) {
        super (outcome, field, message);
    }
}
