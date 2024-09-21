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
package com.effacy.jui.test.suite.tabs;

import com.effacy.jui.test.component.ComponentTester;

/**
 * Used to test a {@link TabbedPanel}
 */
public class TabbedPanelTester extends ComponentTester<TabbedPanelTester> {

    public static TabbedPanelTester $(String testId) {
        return new TabbedPanelTester (testId);
    }

    /**
     * For testing the embodied tab set.
     */
    protected TabSetTester tabSet;

    public TabbedPanelTester(String testId) {
        super(testId, "tabbedpanel");
        this.tabSet = register (new TabSetTester (testId + ".tabset"));
    }

    public TabbedPanelTester activate(String tab) {
        tabSet.activate(tab);
        return this;
    }
    
    /**
     * Validates that the given tabs are existent.
     * 
     * @param tabs the tab references.
     * @return this tester instance.
     */
    public TabbedPanelTester validateTabs(String...tabs) {
        tabSet.validateTabs(tabs);
        return this;
    }

    public TabbedPanelTester validateActiveTab(String tab) {
        tabSet.validateActiveTab(tab);
        return this;
    }
}
