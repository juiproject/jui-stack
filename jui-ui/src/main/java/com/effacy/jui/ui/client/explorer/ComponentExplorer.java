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
package com.effacy.jui.ui.client.explorer;

import com.effacy.jui.ui.client.NotificationDialogDocumentation;
import com.effacy.jui.ui.client.button.ButtonDocumentation;
import com.effacy.jui.ui.client.control.CheckControlDocumentation;
import com.effacy.jui.ui.client.control.MultiCheckControlDocumentation;
import com.effacy.jui.ui.client.control.SelectionControlDocumentation;
import com.effacy.jui.ui.client.control.SelectionGroupControlDocumentation;
import com.effacy.jui.ui.client.control.TextAreaControlDocumentation;
import com.effacy.jui.ui.client.control.TextControlDocumentation;
import com.effacy.jui.ui.client.fragments.CardDocumentation;
import com.effacy.jui.ui.client.fragments.DividerDocumentation;
import com.effacy.jui.ui.client.fragments.IconDocumentation;
import com.effacy.jui.ui.client.fragments.StackDocumentation;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorDocumentation;

/**
 * Collection of JUI components.
 */
public class ComponentExplorer extends TabNavigator {

    public ComponentExplorer() {
        this (true, true, true);
    }

    public ComponentExplorer(boolean components, boolean controls, boolean fragments) {
        super (new TabNavigator.Config ().style (TabNavigator.Config.Style.HORIZONTAL_BAR));
        if (components)
            components (this);
        if (controls)
            controls (this);
        if (fragments)
            fragments (this);
    }

    public static void components(TabNavigator panel) {
        panel.tab("button", "Button", ButtonDocumentation.documentation ());
        panel.tab("tabnavigator", "Tab navigator", TabNavigatorDocumentation.documentation ());
        panel.tab("notificationdialog", "Notification dialog", NotificationDialogDocumentation.documentation ());
    }

    public static void components(TabNavigator.Config panel) {
        panel.tab("button", "Button", ButtonDocumentation.documentation ());
        panel.tab("tabnavigator", "Tab navigator", TabNavigatorDocumentation.documentation ());
        panel.tab("notificationdialog", "Notification dialog", NotificationDialogDocumentation.documentation ());
    }

    public static void controls(TabNavigator panel) {
        panel.tab("check", "Checkbox", CheckControlDocumentation.documentation());
        panel.tab("multicheck", "Multi-check", MultiCheckControlDocumentation.documentation ());
        panel.tab("text", "Text", TextControlDocumentation.documentation ());
        panel.tab("textarea", "Text area", TextAreaControlDocumentation.documentation ());
        panel.tab("selection", "Selection", SelectionControlDocumentation.documentation ());
        panel.tab("selectiongroup", "Selection group", SelectionGroupControlDocumentation.documentation ());
    }

    public static void controls(TabNavigator.Config panel) {
        panel.tab("check", "Checkbox", CheckControlDocumentation.documentation());
        panel.tab("multicheck", "Multi-check", MultiCheckControlDocumentation.documentation ());
        panel.tab("text", "Text", TextControlDocumentation.documentation ());
        panel.tab("textarea", "Text area", TextAreaControlDocumentation.documentation ());
        panel.tab("selection", "Selection", SelectionControlDocumentation.documentation ());
        panel.tab("selectiongroup", "Selection group", SelectionGroupControlDocumentation.documentation ());
    }

    public static void fragments(TabNavigator panel) {
        panel.tab("card", "Card", CardDocumentation.documentation ());
        panel.tab("divider", "Divider", DividerDocumentation.documentation ());
        panel.tab("icon", "Icon", IconDocumentation.documentation ());
        panel.tab("stack", "Stack", StackDocumentation.documentation ());
    }

    public static void fragments(TabNavigator.Config panel) {
        panel.tab("card", "Card", CardDocumentation.documentation ());
        panel.tab("divider", "Divider", DividerDocumentation.documentation ());
        panel.tab("icon", "Icon", IconDocumentation.documentation ());
        panel.tab("stack", "Stack", StackDocumentation.documentation ());
    }
    
}
