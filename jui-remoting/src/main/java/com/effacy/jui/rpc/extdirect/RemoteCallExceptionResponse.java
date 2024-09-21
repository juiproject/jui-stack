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
package com.effacy.jui.rpc.extdirect;

/**
 * This is an extension of the response that caters for exceptions.
 * 
 * @author Jeremy Buckley
 */
public class RemoteCallExceptionResponse extends RemoteCallResponse {

    /**
     * The message associated with the error.
     */
    private String message;

    /**
     * The location of the problem.
     */
    private String where;


    /**
     * Construct with a call to base on and a cause.
     * 
     * @param call
     *            the call.
     * @param cause
     *            the cause of the problem.
     */
    public RemoteCallExceptionResponse(RemoteCall call, Throwable cause) {
        super (call);
        setType (RemoteCallType.exception);
        setMessage (cause.getMessage ());
        setWhere (cause.toString ());
    }


    /**
     * Construct with a call to base on and message and where.
     * 
     * @param call
     *            the call.
     * @param message
     *            the message.
     * @param where
     *            where the problem occurred.
     */
    public RemoteCallExceptionResponse(RemoteCall call, String message, String where) {
        super (call);
        setType (RemoteCallType.exception);
        setMessage (message);
        setWhere (where);
    }


    /**
     * Construct with a call to base on and message but no where.
     * 
     * @param call
     *            the call.
     * @param message
     *            the message.
     */
    public RemoteCallExceptionResponse(RemoteCall call, String message) {
        super (call);
        setType (RemoteCallType.exception);
        setMessage (message);
        setWhere ("");
    }


    /**
     * Gets the message.
     * 
     * @return The message.
     */
    public String getMessage() {
        return message;
    }


    /**
     * Sets the message.
     * 
     * @param message
     *            the message.
     */
    public void setMessage(String message) {
        this.message = message;
    }


    /**
     * Gets where the problem occurred.
     * 
     * @return Where the problem occurred.
     */
    public String getWhere() {
        return where;
    }


    /**
     * Sets where the problem occurred.
     * 
     * @param where
     *            where the problem occurred.
     */
    public void setWhere(String where) {
        this.where = where;
    }

}
