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
package com.effacy.jui.platform.css.client;

import java.util.Map;

/**
 * Used to declare a CSS resource class provides methods to obtain the CSS to
 * inject as well as a mechanism of injection.
 */
public interface CssDeclaration {

    /**
     * Obtains the CSS as text to inject.
     * 
     * @return the text.
     */
    public String getCssText();

    /**
     * Ensures the CSS has been injected.
     * 
     * @return {@code true} if was injected this time (first injection).
     */
    public boolean ensureInjected();

    /**
     * Map of select to CSS declarations. This is only generated if instructed to.
     * 
     * @return the map.
     */
    default public Map<String,Map<String,String>> getCssDeclarations() {
        return null;
    }
    
}
