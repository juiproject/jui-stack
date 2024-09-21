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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.commons.text.StringEscapeUtils;

import com.effacy.jui.codeserver.gwt.ReverseSourceMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Used to return JAVA source file contents (provided as a stream) nicely
 * formatted in HTML.
 * <p>
 * A {@link ReverseSourceMap} may be used to highlight lines in the source that
 * are mapped via source maps indicating which part of the source code has been
 * compiled for use.
 */
public class HtmlSourceViewBuilder implements ViewBuilder {
        
    private ReverseSourceMap sourceMap;
    private String sourcePath;
    private InputStream pageBytes;

    public HtmlSourceViewBuilder(String sourcePath, ReverseSourceMap sourceMap, InputStream pageBytes) {
        this.sourceMap = sourceMap;
        this.sourcePath = sourcePath;
        this.pageBytes = pageBytes;
    }
    
    @Override
    public void build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File sourceFile = new File (sourcePath);
        response.setContentType ("text/html");
        PrintWriter pw = response.getWriter();
        pw.write ("""
<html>
    <head>
        <title>""" + StringEscapeUtils.escapeHtml4 (sourceFile.getName ()) + " (JUI Code Server)</title>" + """
        <style>
            h1 { font-family: monospace; }
            a {
                color: #0fa1be;
                text-decoration: none;
            }
            a:hover {
                color: #017f99;
                text-decoration: underline;
            }
            .unused { color: grey; }
            .used { color: black; }
            .title { margin-top: 0; }
        </style>
    </head>
    <body>
        <a href=".">""" + StringEscapeUtils.escapeHtml4 (sourceFile.getParent ()) + "</a>" + """
        <h1 class="title">""" + StringEscapeUtils.escapeHtml4 (sourceFile.getName ()) + "</h1>" + """
        <pre class="unused">""");

        BufferedReader lines = new BufferedReader (new InputStreamReader (pageBytes));
        try {
            int lineNumber = 1;
            for (String line = lines.readLine(); line != null; line = lines.readLine()) {
                if (sourceMap.appearsInJavaScript (sourcePath, lineNumber)) {
                    pw.print ("<span class=\"used\">");
                    pw.print (StringEscapeUtils.escapeHtml4 (line));
                    pw.println ("</span>");
                } else {
                    pw.println (StringEscapeUtils.escapeHtml4 (line));
                }
                lineNumber++;
            }
        } finally {
            lines.close();
        }
        pw.print ("""
        </pre>
    </body>
</html>""");
        pw.flush ();
    }
    
}
