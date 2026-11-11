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
package com.effacy.jui.ui.client.navigation;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.ui.client.navigation.TabCollection.TabGroupConfig;

public class TabCollectionTest {

    /**
     * A hidden group's tabs are found by reference (which is what registers them
     * with the navigator and makes them navigable).
     */
    @Test
    public void hiddenGroupTabsAreFound() {
        TabCollection tabs = new TabCollection ();
        tabs.tab ("overview", "Overview", (TabNavigator.ITabActivator) null);
        tabs.group ("Analysis").hidden ();
        tabs.tab ("analysis", "Analysis", (TabNavigator.ITabActivator) null);

        Assertions.assertNotNull (tabs.findTab ("analysis"));
        Assertions.assertEquals ("Analysis", tabs.findTab ("analysis").label);
        Assertions.assertFalse (tabs.isEmpty ());

        List<TabGroupConfig> groups = tabs.getTabGroups ();
        Assertions.assertEquals (2, groups.size ());
        Assertions.assertFalse (groups.get (0).hidden);
        Assertions.assertTrue (groups.get (1).hidden);
    }

    /**
     * A hidden group is never marked first (the first group drawn with a heading):
     * it is not drawn.
     */
    @Test
    public void hiddenGroupIsNotFirst() {
        TabCollection tabs = new TabCollection ();
        tabs.group ("Hidden").hidden ();
        tabs.tab ("a", "A", (TabNavigator.ITabActivator) null);
        tabs.group ("Shown");
        tabs.tab ("b", "B", (TabNavigator.ITabActivator) null);

        List<TabGroupConfig> groups = tabs.getTabGroups ();
        Assertions.assertFalse (groups.get (0).first);
        Assertions.assertTrue (groups.get (1).first);
    }

    /**
     * A group can be shown again (hidden(false)) and is then marked first as
     * before.
     */
    @Test
    public void hiddenCanBeReversed() {
        TabCollection tabs = new TabCollection ();
        tabs.group ("Group").hidden ().hidden (false);
        tabs.tab ("a", "A", (TabNavigator.ITabActivator) null);

        List<TabGroupConfig> groups = tabs.getTabGroups ();
        Assertions.assertFalse (groups.get (0).hidden);
        Assertions.assertTrue (groups.get (0).first);
    }
}
