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
 * This is an extension of the response that caters for denials.
 * 
 * @author Jeremy Buckley
 */
public class RemoteCallDeniedResponse extends RemoteCallResponse {

    /**
     * The message associated with the error.
     */
    private String message;

    /**
     * Construct with a call to base on and a cause.
     * 
     * @param call
     *            the call.
     * @param cause
     *            the cause of the problem.
     */
    public RemoteCallDeniedResponse(RemoteCall call, String message) {
        super (call);
        setType (RemoteCallType.denied);
        setMessage (message);
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

}
