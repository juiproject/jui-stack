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

import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;

/**
 * Collection of code for various lessons arranged as a tabbed panel with each
 * tab corresponding to a lession.
 */
public class Lessons extends TabNavigator {
    
    static {
        // Used to demonstrate semi-global CSS. This will capture updates on change but
        // later one can either imbed as local CSS or in a truely global CSS directly in
        // the entry HTML.
        CSSInjector.injectFromModuleBase ("lessons.css"); 
    }

    public Lessons() {
        super (TabNavigatorCreator.config ().style (TabNavigator.Config.Style.HORIZONTAL_BAR).padding (Insets.em (0)).effect (CardFitLayout.Config.Effect.FADE_IN));

        // The various lessons.
        tab ("lesson1", "Lesson 1", TabNavigatorCreator.create (
            cfg -> { cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
            cfg.tab("lesson1a", "Part A", new Lesson1a ());
            cfg.tab("lesson1b", "Part B", new Lesson1b ());
            cfg.tab("lesson1c", "Part C", new Lesson1c ());
            cfg.tab("lesson1d", "Part D", new Lesson1d ());
            cfg.tab("lesson1e", "Part E", new Lesson1e ());
        }));

        tab ("lesson2", "Lesson 2", TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
            cfg.tab("lesson2a", "Part A", new Lesson2a ());
            cfg.tab("lesson2b", "Part B", new Lesson2b ());
            cfg.tab("lesson2c", "Part C", new Lesson2c ());
        }));

        tab ("lesson3", "Lesson 3", TabNavigatorCreator.create (cfg ->  {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
            cfg.tab("lesson3a", "Part A", new Lesson3a ());
            cfg.tab("lesson3b", "Part B", new Lesson3b ());
            cfg.tab("lesson3c", "Part C", new Lesson3c ());
        }));

        tab ("lesson4", "Lesson 4", TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
            cfg.tab("lesson4a", "Part A", new Lesson4a ());
            cfg.tab("lesson4b", "Part B", new Lesson4b ());
            cfg.tab("lesson4c", "Part C", new Lesson4c ());
            cfg.tab("lesson4d", "Part D", new Lesson4d ());
        }));

        tab ("lesson5", "Lesson 5", TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
            cfg.tab("lesson5a", "Part A", new Lesson5a ());
            cfg.tab("lesson5b", "Part B", new Lesson5b ());
            cfg.tab("lesson5c", "Part C", new Lesson5c ());
        }));
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

        // This class is used to scope the global CSS to avoid conflicts with the rest
        // of the application.
        getRoot ().classList.add ("lessons");
    }

    
}
