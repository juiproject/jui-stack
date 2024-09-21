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
package com.effacy.jui.playground.ui.editor;

import com.effacy.jui.ui.client.tabs.TabSet;
import com.effacy.jui.ui.client.tabs.TabbedPanel;
import com.effacy.jui.ui.client.tabs.TabbedPanelCreator;

public class EditorShowcase extends TabbedPanel {
    
    public EditorShowcase() {
        super (TabbedPanelCreator.config (cfg -> {
            cfg.style (TabSet.Config.Style.HORIZONTAL);
        }));

        tab ("uc", "Use Case", new UCEditor ());
        tab ("policy", "Policy", new PolicyEditor ());
    }
}
