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
package com.effacy.jui.playground.ui.samples;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * A selection of capabilities that can be exported from JUI to be made
 * available from JS (using JsInterop).
 *
 * @author Jeremy Buckley
 */
@JsType
public class InteropExported {

    /**
     * A simple mechanism for implementing a callback function.
     */
    @JsFunction
    public interface Callback<V> {
        void result(V x);
    }

    /**
     * Method that returns a message.
     * 
     * @return the message.
     */
    public String message() {
        return "This is a message from the exported code";
    }

    /**
     * Simulates a query with a callback (potentially async).
     * <p>
     * The response simply reflects back the contents of the filter property of the
     * query object.
     * 
     * @param query
     *              the query to perform.
     * @param func
     *              the callback function.
     */
    public void query(Query query, Callback<Response> func) {
        // In a practical remoting case this will invoke an RPC (or Rest) call and the
        // response would invoke the callback. The callback could also incorporate error
        // handling.
        if (query.getFilter () == null)
            func.result (new Response ("No filter applied"));
        else
            func.result (new Response ("Results filtered on \"" + query.getFilter () + "\""));
    }

    /**
     * Query to pass through.
     * <p>
     * This is exported in a limited fashion so that only the constructor is
     * available to JS.
     */
    public static class Query {

        private String filter;

        @JsConstructor
        public Query(String filter) {
            this.filter = filter;
        }

        public String getFilter() {
            return filter;
        }
    }

    /**
     * Response to {@link Query) via {@link InteropExported#query(Query, Callback)}.
     * <p>
     * Exported in a limited fashion so only the {@link #getName}) method is
     * available to JS.
     */
    public static class Response {

        private String name;

        public Response(String name) {
            this.name = name;
        }

        @JsMethod
        public String getName() {
            return name;
        }
    }
}
