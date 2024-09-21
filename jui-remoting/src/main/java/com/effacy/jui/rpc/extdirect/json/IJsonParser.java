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
package com.effacy.jui.rpc.extdirect.json;

import java.util.List;

import com.effacy.jui.rpc.extdirect.RemoteCallRequest;
import com.effacy.jui.rpc.extdirect.RemoteCallResponse;

/**
 * Conversion mechanism for handling JSON.
 * 
 * @author Jeremy Buckley
 */
public interface IJsonParser {

    /**
     * Converts a single response to a JSON array.
     * 
     * @param response
     *            the response to convert.
     * @return The JSON string representing the objects.
     */
    public String remoteCallResponseToJson(RemoteCallResponse response) throws JsonParserException;


    /**
     * Given a JSON string, extracts a list of {@link RemoteCallRequest}'s from
     * that string. It is expected that the data in each remote call is
     * correctly typed in accordance with the type meta-data specification for
     * that action and associated method.
     * <p>
     * Note that the method may fail to correctly parse the string, in which
     * case an exception is thrown. If the method is not able to parse data, but
     * is able to parse a request, then it should still create the request but
     * fail it by calling the requests {@link RemoteCallRequest#fail(String)}
     * method.
     * 
     * @param jsonString
     *            the JSON string to convert.
     * @return the converted string.
     * @throws JsonParserException
     *             if there is a parse problem.
     */
    public List<RemoteCallRequest> jsonToRemoteCallRequests(String jsonString) throws JsonParserException;

    /**
     * Convenience to parse a general object (which is expected to be in the scope
     * of serialisable classes).
     * 
     * @param <T>        the type.
     * @param jsonString
     *                   the JSON string to convert.
     * @param klass
     *                   the class type.
     * @return the converted string.
     * @throws JsonParserException
     *                             if there is a parse problem.
     */
    public <T> T jsonToType(String jsonString, Class<T> klass) throws JsonParserException;

}
