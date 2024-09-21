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
package com.effacy.jui.codeserver.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.google.gwt.dev.json.JsonObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonViewBuilder implements ViewBuilder {

    static final Pattern SAFE_CALLBACK = Pattern.compile ("([a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*");

    private Supplier<JsonObject> json;

    public JsonViewBuilder(JsonObject json) {
        this.json = () -> json;
    }

    public JsonViewBuilder(Supplier<JsonObject> json) {
        this.json = json;
    }

    @Override
    public void build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader ("Cache-control", "no-cache");
        PrintWriter out = response.getWriter ();

        String callbackExpression = request.getParameter ("_callback");
        if (callbackExpression == null) {
            // AJAX
            response.setContentType ("application/json");
            json.get ().write (out);
        } else {
            // JSONP
            response.setContentType("application/javascript");
            if (SAFE_CALLBACK.matcher(callbackExpression).matches()) {
                out.print ("/* API response */ " + callbackExpression + "(");
                json.get ().write (out);
                out.println(");");
            } else {
                // compiler.error ("invalid callback: " + callbackExpression);
                // Notice that we cannot execute the callback
                out.print ("alert('invalid callback parameter');\n");
                json.get ().write (out);
            }
        }
    }
        
    }
