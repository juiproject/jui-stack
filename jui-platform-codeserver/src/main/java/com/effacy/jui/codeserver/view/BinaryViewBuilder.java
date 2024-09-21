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
import java.io.InputStream;

import com.google.gwt.thirdparty.guava.common.io.ByteStreams;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BinaryViewBuilder implements ViewBuilder {

    private String mimeType;
    private InputStream stream;

    public BinaryViewBuilder(String mimeType, InputStream stream) {
        this.mimeType = mimeType;
        this.stream = stream;
    }

    @Override
    public void build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(mimeType);
            ByteStreams.copy(stream, response.getOutputStream());
        } finally {
            stream.close();
        }
    }
    
}
