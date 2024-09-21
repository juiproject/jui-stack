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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.thirdparty.guava.common.io.Resources;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HtmlViewBuilder implements ViewBuilder {

    private String variableName;
    private JsonObject json;
    private URL template;

    /**
     * Construct with configuration data.
     * 
     * @param variableName
     *                     the name of a variable to assign to the {@code window}
     *                     object.
     * @param json
     *                     the JSON to assign to {@code window.variableName}.
     * @param resource
     *                     the template.
     */
    public HtmlViewBuilder(String variableName, JsonObject json, URL template) {
        this.variableName = variableName;
        this.json = json;
        this.template = template;
    }

    @Override
    public void build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus (HttpServletResponse.SC_OK);
        response.setContentType ("text/html");
        ServletOutputStream outBytes = response.getOutputStream ();
        Writer out = new OutputStreamWriter (outBytes, "UTF-8");

        out.append ("<!DOCTYPE html>\n");
        out.append ("<script>\n");
        out.append ("window." + variableName + " = ");
        json.write (out);
        out.append (";\n");
        out.append ("</script>\n");
        out.flush ();

        Resources.copy (template, outBytes);
    }
    
}
