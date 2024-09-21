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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ErrorViewBuilder implements ViewBuilder {

    protected int status;
    protected String statusMessage;

    public ErrorViewBuilder (String errorMessage) {
        this (HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }

    public ErrorViewBuilder (int status, String errorMessage) {
        this.status = status;
        this.statusMessage = errorMessage;
    }


    @Override
    public void build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus (status);
        response.setContentType("text/html");
        response.setHeader ("Cache-control", "no-cache");
        response.getWriter ().print ("""
<html>
  <head>
    <title>Unavailable (JUI Code Server)</title>
  </head>
  <body>
    <p>""" + statusMessage + """
    </p>
  </body>
</html>
        """);
        response.getWriter ().flush ();
        //compiler.log ("Sent error page: " + statusMessage);
    }
    
}
