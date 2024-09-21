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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.IPage.INode;
import com.effacy.jui.test.component.ComponentTester;

public class TabSetTester extends ComponentTester<TabSetTester> {

    protected Map<String,INode> tabs = new HashMap<>();

    public TabSetTester(String testId) {
        super(testId, "tabset");
    }

    @Override
    public void resolve() {
        super.resolve();

        for (INode tab : el.selectByXPath (".//li[@item]")) {
            String reference = tab.attribute("item");
            if (reference.isBlank())
                continue;
            tabs.put (reference, tab);
        }
    }
    
    /**
     * Validates that the given tabs are existent.
     * 
     * @param tabs the tab references.
     * @return this tester instance.
     */
    public TabSetTester validateTabs(String...tabs) {
        validate(()-> {
            for (String tab : tabs)
                Assertions.assertTrue (TabSetTester.this.tabs.containsKey (tab), "TabSetTester[test-id=\"" + testId + "\"].validateTabs::Could not find \"" + tab + "\"");
        });
        return this;
    }

    public TabSetTester validateActiveTab(String tab) {
        validate (()-> {
            Assertions.assertEquals(tab, el.attribute("test-state"), "TabSetTester[test-id=\"" + testId + "\"].validateActiveTab");
        });
        return this;
    }

    public TabSetTester activate(String tab) {
        return activate(tab, 1000);
    }

    public TabSetTester activate(String tab, long delay) {
        Assertions.assertTrue(tabs.containsKey (tab), "Unable to find tab to activate item=\"" + tab + "\"");
        try {
            tabs.get(tab).click();
            sleep(delay);
        } catch (Exception e) {
            Assertions.fail("Failed to click button test-id\"" + testId + "\"", e);
        }
        return this;
    }

}
