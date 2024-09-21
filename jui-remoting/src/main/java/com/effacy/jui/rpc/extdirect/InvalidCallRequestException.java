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
 * Thrown where there is a problem mapping a request to a method (this is not
 * intended for general error arising from within the processing method of a
 * request).
 * 
 * @author Jeremy Buckley
 */
public class InvalidCallRequestException extends Exception {

    /**
     * Unique serialization ID.
     */
    private static final long serialVersionUID = -419733640678845507L;

    /**
     * The problematic request.
     */
    private RemoteCallRequest request;


    /**
     * Construct with a request and a message.
     * 
     * @param request
     *            the request that could not be mapped.
     * @param message
     *            a messages expressing the exact problem.
     */
    public InvalidCallRequestException(RemoteCallRequest request, String message) {
        super (message);
        this.request = request;
    }


    /**
     * Gets the request that could not be mappped.
     * 
     * @return The request.
     */
    public RemoteCallRequest getRequest() {
        return request;
    }
}
