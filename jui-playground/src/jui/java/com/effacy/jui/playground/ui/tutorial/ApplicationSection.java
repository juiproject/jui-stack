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
package com.effacy.jui.playground.ui.tutorial;

import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;

/**
 * The top-level section that contains the tabs, the first of which is the
 * dashboard being developed.
 */
public class ApplicationSection extends TabNavigator {

    public ApplicationSection() {
        // TODO: Modify the configuration to support the rounded style.
        super (TabNavigatorCreator.config ().color ("#f0f0f0").style (TabNavigator.Config.Style.VERTICAL));

        // TODO: Add the tabs here.
    }
}

