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
package com.effacy.jui.core.client.dom.css;

import com.effacy.jui.core.client.dom.css.CSS.ICSSProperty;

public enum Cursor implements ICSSProperty {
    
	DEFAULT("default"),
    AUTO("auto"),
    CROSSHAIR("crosshair"),
    POINTER("pointer"),
    MOVE("move"),
    E_RESIZE("e-resize"),
    NE_RESIZE("ne-resize"),
    NW_RESIZE("nw-resize"),
    N_RESIZE("n-resize"),
    SE_RESIZE("se-resize"),
    SW_RESIZE("sw-resize"),
    S_RESIZE("s-resize"),
    W_RESIZE("w-resize"),
    TEXT("text"),
    WAIT("wait"),
    HELP("help"),
    COL_RESIZE("col-resize"),
    ROW_RESIZE("row-resize");

    private String value;

    private Cursor(String value) {
        this.value = value;
    }

	@Override
	public String value() {
		return value;
	}
    
}