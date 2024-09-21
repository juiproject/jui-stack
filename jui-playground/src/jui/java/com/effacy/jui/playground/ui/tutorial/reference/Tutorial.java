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
package com.effacy.jui.playground.ui.tutorial.reference;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.ui.client.tabs.TabSet;
import com.effacy.jui.ui.client.tabs.TabSet.Config.Style;
import com.effacy.jui.ui.client.tabs.TabbedPanel;
import com.effacy.jui.ui.client.tabs.TabbedPanelCreator;
import com.google.gwt.core.client.GWT;

public class Tutorial extends TabbedPanel {

    static {
        CSSInjector.injectFromModuleBase ("tutorial.css");
    }

    public Tutorial() {
        super (TabbedPanelCreator.config ().color ("#f0f0f0").style (ROUNDED_TABSET));

        tab ("dashboard", "Dashboard", new Dashboard ()).icon (FontAwesome.gauge ());
        tab ("inbox", "Inbox", PanelCreator.build ()).icon (FontAwesome.envelope ());
        tab ("tasks", "Tasks", PanelCreator.build ()).icon (FontAwesome.listCheck ());
    }

    /*******************************************************************************
     * Tab set style
     *******************************************************************************/

    private static final Style ROUNDED_TABSET = Style.create (RoundTabSetLocalCSS.instance (), true, FontAwesome.minus (), FontAwesome.plus ());

    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        TabSet.ILocalCSS.CSS,
        TabSet.ILocalCSS.CSS_OVERRIDE,
        "com/effacy/jui/ui/client/tabs/TabSet_Vertical.css",
        "com/effacy/jui/playground/ui/tutorial/reference/TabSet_Rounded.css"
    })
    public static abstract class RoundTabSetLocalCSS implements TabSet.ILocalCSS {

        private static RoundTabSetLocalCSS STYLES;

        public static TabSet.ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (RoundTabSetLocalCSS) GWT.create (RoundTabSetLocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
