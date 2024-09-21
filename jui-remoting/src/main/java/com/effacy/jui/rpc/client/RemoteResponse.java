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

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.Transient;
import com.effacy.jui.rpc.extdirect.client.ExtDirectRemoteMethod;

/**
 * This remote response is the response type expected by.
 * <p>
 * The ExtDirect protocol actually does not require any specific response type
 * but unfortunately the protocol does not provide for any extensive or standard
 * application error handling. In order to overcome this limitation we have
 * imposed a return type (which is expected by the {@link ExtDirectRemoteMethod}
 * implementation).
 * <p>
 * This return type (this class) must wrap every method invocation response from
 * the server side. That is, the server must return an instance of this class
 * (or one of its sub-classes) and the actual response instance should be set by
 * {@link #setResponse(Object)} (or via one of the constructors). In the event
 * of an application exception then the response value need not be set, only the
 * error data.
 * <p>
 * In order for the response to be correctly reconstituted on the client it MUST
 * be serializable (that is, it should be annotated with
 * {@link JsonSerializable} or an equivalent annotation that the JSON serializer
 * recognizes). Note that this class need not be deserializable in this context
 * (so can remain generic) as {@link ExtDirectRemoteMethod} manipulates the raw
 * JSON directly and only deserializes the response to an instance.
 * 
 * @param <T>
 *            The type being returned.
 * @author Steve Baker
 * @author Jeremy Buckley
 */
public class RemoteResponse<T> {

    /**
     * The root case of the error (if any).
     */
    private RemoteResponseType outcome = RemoteResponseType.SUCCESS;

    /**
     * Collection of error messages keyed by field.
     */
    private List<ErrorMessage> messages;

    /**
     * The return data (if any).
     */
    private T response;

    /**
     * Default constructor. The outcome is set as
     * {@link RemoteResponseType#SUCCESS} bit no messages or data are
     * associated.
     */
    public RemoteResponse() {
        this.outcome = RemoteResponseType.SUCCESS;
    }


    /**
     * Construct with just an outcome (no messages or return data).
     * 
     * @param outcome
     *            the outcome of the response.
     */
    public RemoteResponse(RemoteResponseType outcome) {
        this.outcome = outcome;
    }


    /**
     * Construct with a collection of messages and the outcome
     * {@link RemoteResponseType#VALIDATION}.
     * 
     * @param outcome
     *            the outcome of the response.
     * @param messages
     *            the messages to pass.
     */
    public RemoteResponse(RemoteResponseType outcome, List<ErrorMessage> messages) {
        this.outcome = outcome;
        this.messages = messages;
    }


    /**
     * Default fail response.
     * 
     * @param messages
     *            the messages to fail against.
     * @param outcome
     *            the outcome of the operation.
     */
    public RemoteResponse(RemoteResponseType outcome, ErrorMessage... messages) {
        this.outcome = outcome;
        this.messages = new ArrayList<ErrorMessage> ();
        for (ErrorMessage message : messages)
            getMessages ().add (message);
    }


    /**
     * Construct with a collection of messages and the outcome
     * {@link RemoteResponseType#VALIDATION}.
     * 
     * @param outcome
     *            the outcome of the response.
     * @param messages
     *            the messages to pass.
     */
    public RemoteResponse(RemoteResponseType outcome, String message) {
        this.outcome = outcome;
        getMessages ().add (new ErrorMessage (message));
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
    public RemoteResponse(RemoteResponseType outcome, String field, String message) {
        this.outcome = outcome;
        getMessages ().add (new ErrorMessage (field, message));
    }


    /**
     * Construct with an outcome, messages and associated data.
     * 
     * @param outcome
     *            the outcome of the response.
     * @param messages
     *            messages to associate with the response.
     * @param response
     *            the response data to return with the response.
     */
    public RemoteResponse(RemoteResponseType outcome, List<ErrorMessage> messages, T response) {
        this.outcome = outcome;
        this.messages = messages;
        this.response = response;
    }


    /**
     * Construct with a collection of messages and the outcome
     * {@link RemoteResponseType#VALIDATION}.
     * 
     * @param messages
     *            the messages to pass.
     */
    public RemoteResponse(List<ErrorMessage> messages) {
        this (RemoteResponseType.ERROR_VALIDATION, messages);
    }


    /**
     * Construct with return data and the outcome
     * {@link RemoteResponseType#SUCCESS}.
     * 
     * @param response
     *            the response data for the response.
     */
    public RemoteResponse(T response) {
        this.outcome = RemoteResponseType.SUCCESS;
        this.response = response;
    }


    /**
     * Gets the data to associate with the response.
     * 
     * @return The return data.
     */
    public T getResponse() {
        return response;
    }


    /**
     * Set the response data to return with the response.
     * 
     * @param response
     *            the response data.
     */
    public void setResponse(T response) {
        this.response = response;
    }


    /**
     * Determines if was a success or not. This is only the case if the outcome
     * is {@link RemoteResponseType#SUCCESS}.
     * 
     * @return {@code true} if was a success.
     */
    @Transient
    public boolean isSuccess() {
        return RemoteResponseType.SUCCESS.equals (getOutcome ());
    }


    /**
     * Gets the outcome of the response. Note that if this is successful then
     * {@link RemoteResponseType#SUCCESS} will be returned and
     * {@link #isSuccess()} will return {@code true}.
     * 
     * @return
     */
    public RemoteResponseType getOutcome() {
        return outcome;
    }


    /**
     * Gets any messages associated to the response.
     * 
     * @return The messages.
     */
    public List<ErrorMessage> getMessages() {
        if (messages == null)
            messages = new ArrayList<ErrorMessage> ();
        return messages;
    }
}
