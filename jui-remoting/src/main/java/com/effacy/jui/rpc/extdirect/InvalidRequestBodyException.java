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

import java.io.IOException;

/**
 * Thrown when the body of a request is not a remote call (or an array of remote
 * calls) at all, so no call can be answered. This is the client's error and is
 * answered with a 400 (Bad Request).
 * <p>
 * Problems with a call's arguments are not reported this way: the call is
 * rejected (see {@link RemoteCallRequest#reject(com.effacy.jui.rpc.client.ErrorMessage)})
 * and answered on its own.
 */
public class InvalidRequestBodyException extends IOException {

    /**
     * Unique serialization ID.
     */
    private static final long serialVersionUID = 3125766502861309014L;

    /**
     * Construct with a message and a cause.
     * 
     * @param message
     *                the message.
     * @param cause
     *                the cause.
     */
    public InvalidRequestBodyException(String message, Throwable cause) {
        super (message, cause);
    }
}
