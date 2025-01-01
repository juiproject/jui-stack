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
package com.effacy.jui.playground.ui.lessons;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerProvider;
import com.effacy.jui.playground.ui.PlaygroundApp;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;

public class Lesson3b extends LessonPanel implements INavigationHandlerProvider {

    private TabNavigator tabbedPanel;

    protected Lesson3b() {
        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part B: Browser url", header -> {
                header.subtitle ("This lession describes how to hook up to the browser URL.");
            });
        })).update (null);

        tabbedPanel = add (TabNavigatorCreator.create(cfg -> {
            cfg.style (TabNavigator.Config.Style.VERTICAL);
            cfg.padding (Insets.em (1));
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            tabs.tab ("tab1", "Tab 1", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 1");
            })).icon (FontAwesome.airFreshener ());
            tabs.tab ("tab2", "Tab 2", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 2");
            })).icon (FontAwesome.allergies ());
            tabs.tab ("tab3", "Tab 3", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 3");
            })).icon (FontAwesome.user ());
            tabs.tab ("tab4", "Tab 4", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 4");
            })).icon (FontAwesome.cow ());
        }));

        // Example 1
        add (ButtonCreator.build(cfg -> {
            cfg.label ("Navigate to first lesson");
            cfg.handler (() -> {
                PlaygroundApp.navigation ("/lessions/lesson1/lesson1a");
            });
        }));
    }

    public INavigationHandler handler() {
        return tabbedPanel.handler();
    }

}


