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

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extends a {@link RemoteCallRequest} to add in context information.
 * 
 * @author Jeremy Buckley
 */
public class ExtendedRemoteCallRequest extends RemoteCallRequest {

    /**
     * The associated servlet request.
     */
    private HttpServletRequest request;


    /**
     * Construct with a call to copy and a request.
     * 
     * @param call
     *            the call to copy.
     * @param request
     *            the HTTP request.
     */
    public ExtendedRemoteCallRequest(RemoteCallRequest call, HttpServletRequest request) {
        super (call);
        setRequest (request);
    }


    /**
     * Gets the servlet request associated to this call.
     * 
     * @return The servlet request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }


    /**
     * Sets the servlet request associated to this call.
     * 
     * @param request
     *            the request.
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
}
