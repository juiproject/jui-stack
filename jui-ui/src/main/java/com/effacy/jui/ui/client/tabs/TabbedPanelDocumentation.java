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
package com.effacy.jui.ui.client.tabs;

import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.ui.client.tabs.TabSet.Config.Style;

public class TabbedPanelDocumentation {

    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Tabbed panel")
            .className (TabbedPanel.class.getCanonicalName())
            .description ("A panel whose children linked to a tab set.")
            .example (TabbedPanelCreator.create (cfg -> {
                cfg.style (Style.HORIZONTAL);
            }, tab-> {
                tab.tab ("tab1", "Tab 1", PanelCreator.build ());
                tab.tab ("tab2", "Tab 2", PanelCreator.build ());
            }))
        .build ();
    }
}
