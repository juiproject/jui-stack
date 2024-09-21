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
package com.effacy.jui.core.client;

import com.effacy.jui.core.client.observable.ListenerOracle;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.core.shared.GWT;

public class GWTTestBridge extends GWTBridge {

    private static GWTTestBridge BRIDGE;

    public static void init() {
        if (BRIDGE == null) {
            BRIDGE = new GWTTestBridge ();
            GWT.setBridge (BRIDGE);
        }
    }

    @Override
    public <T> T create(Class<?> classLiteral) {
        if (classLiteral == ListenerOracle.class)
            return (T) new ListenerOracleMock ();
        return null;
    }

    @Override
    public String getVersion() {
        return "UNIT-TEST";
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public void log(String message, Throwable e) {
        // Nothing.
    }
}
