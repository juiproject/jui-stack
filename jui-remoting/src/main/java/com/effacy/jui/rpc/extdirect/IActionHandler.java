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

import com.effacy.jui.rpc.extdirect.metadata.IActionMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata;

/**
 * Represents an action handler that embodies a collection of methods.
 * 
 * @author Jeremy Buckley
 */
public interface IActionHandler extends IActionMetadata {

    /**
     * Attempts to lookup the metadata for a given request.
     * 
     * @param request
     *            the request to associate the metadata to.
     * @return The method data.
     * @throws InvalidCallRequestException
     *             If the request could not be mapped to a handler.
     */
    public IMethodMetadata lookupMethodMetadata(ExtendedRemoteCallRequest request) throws InvalidCallRequestException;


    /**
     * Process a request. It is expected that if there is an error, that will
     * generate an exception.
     * 
     * @param request
     *            the request.
     * @return The response.
     * @throws Throwable
     *             On error (this will roll back any active transaction and
     *             result in a call to
     *             {@link #processError(ExtendedRemoteCallRequest, Throwable)}).
     * @throws InvalidCallRequestException
     *             If the request could not be mapped to a handler.
     */
    public RemoteCallResponse process(ExtendedRemoteCallRequest request) throws Throwable, InvalidCallRequestException;


    /**
     * Process an error arising from processing the given call request.
     * 
     * @param request
     *            the request that generated the error.
     * @param error
     *            the error generated by the request.
     * @return The response to the error.
     * @throws InvalidCallRequestException
     *             If the error could not be handled.
     */
    public RemoteCallResponse processError(ExtendedRemoteCallRequest request, Throwable error) throws InvalidCallRequestException;


    /**
     * Determines if the error passed is subject to retry (often this will be if
     * the error is something that is recoverable, such as a loss of database
     * connection).
     * 
     * @param request
     *            the request that generated the error.
     * @param error
     *            the error to test for.
     * @return {@code true} if the error is subject to retry.
     */
    public boolean retryError(ExtendedRemoteCallRequest request, Throwable error);


    /**
     * Determines if all the requests are read only requests.
     * 
     * @param request
     *            the request to process.
     * @return {@code true} if they are read only.
     */
    public boolean isReadOnly(ExtendedRemoteCallRequest request);
}
