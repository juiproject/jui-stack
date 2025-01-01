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

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.panel.SplitPanel;


/**
 * Renders out the themes.
 */
public class Themes extends SplitPanel {

    public Themes() {
        super (new SplitPanel.Config () //
                .vertical () //
                .scrollable () //
        // .separator () //
        );
        addOther (InfoBlockCreator.<Integer>$ (cfg -> cfg.padding (Insets.em (2, 1.75, 2, 0.75)), builder -> {
            builder.header ("Theme", header -> {
                header.subtitle ("Theming is applied using CSS variables defined in Theme.css. These can be modified by providing an override as per the Theme documentation (see the documentation hub).");
            });
        })).update (ThemeRenderer.ThemeStyle.colors ().size ());

        TabNavigator tabbed = add (new TabNavigator.Config ().style(TabNavigator.Config.Style.HORIZONTAL_UNDERLINE).build ());
        tabbed.tab ("palette", "Palette", new Component<Component.Config> (null, new ThemeRenderer ().convert (a -> ThemeRenderer.ThemeStyle.colors ())));
        tabbed.tab ("topography", "Topography", new Component<Component.Config> (null, new ThemeRenderer ().convert (a -> ThemeRenderer.ThemeStyle.topography ())));
        tabbed.tab ("components", "Components", new Component<Component.Config> (null, new ThemeRenderer ().convert (a -> ThemeRenderer.ThemeStyle.components ())));
    }

}

