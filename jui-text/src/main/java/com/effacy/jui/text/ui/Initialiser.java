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
package com.effacy.jui.text.ui;

import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.platform.core.client.EntryPoint;
import com.effacy.jui.platform.util.client.ScriptInjector;

/**
 * Performs any global initialisation for the module.
 */
public class Initialiser implements EntryPoint {

    @Override
    public void onModuleLoad() {
        CSSInjector.injectFromModuleBase ("jui_text_fragments.css");
        ScriptInjector.injectFromModuleBase ("jui_text_editor.js");
    }
}
