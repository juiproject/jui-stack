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
package com.effacy.jui.playground.ui.samples;

import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.ui.client.tabs.TabSet;
import com.effacy.jui.ui.client.tabs.TabbedPanel;
import com.effacy.jui.ui.client.tabs.TabbedPanelCreator;

/**
 * Holds the various examples used in the tutorial.
 *
 * @author Jeremy Buckley
 */
public class Samples extends TabbedPanel {

    public Samples() {
        super (TabbedPanelCreator.config ().style (TabSet.Config.Style.HORIZONTAL_UNDERLINE).padding (Insets.em (2)));

        tab ("components", "Components", new Components ());
        tab ("containers", "Containers", new Containers ());
        tab ("interop", "Interoperability", new Interoperability ());
        tab ("remoting", "Remoting", new Remoting ());
    }

}
