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
package com.effacy.jui.playground.it.ui.table;

import java.util.List;

import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlCheckBoxInput;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.effacy.jui.playground.TestApplicationIT;
import com.effacy.jui.playground.it.AbstractIT;
import com.effacy.jui.test.PageTester;
import com.effacy.jui.test.suite.navigation.TabNavigatorTester;

/**
 * Clicks in a table with {@code Table.Config.onclick} set (the "Table clicks"
 * example under Galleries): a cell's own handlers get their clicks and only
 * other clicks reach the row handler.
 */
@SpringBootTest(classes = TestApplicationIT.class)
public class TableClicksITTest extends AbstractIT {

    @Test
    public void cellHandlersRunBeforeRowClick() throws Exception {
        HtmlPage page = openTableClicks ();
        PageTester tester = PageTester.$ (page);
        String name = text (page, "tableclicks.name");
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));

        // (1) A button in a cell runs its own handler, not the row handler.
        tester.click ("tableclicks.open", 300);
        Assertions.assertEquals ("button:" + name, text (page, "tableclicks.lastclick"));

        // (2) Plain cell content runs the row handler.
        tester.click ("tableclicks.name", 300);
        Assertions.assertEquals ("row:" + name, text (page, "tableclicks.lastclick"));

        // (3) The selection checkbox (in the same row as the name) toggles the
        // selection and does not run the row handler.
        HtmlCheckBoxInput selector = page.getFirstByXPath ("(//*[@test-id='tableclicks.name'])[1]/ancestor::tr[1]//input[@type='checkbox']");
        Assertions.assertNotNull (selector, "Unable to find the row selector");
        Assertions.assertFalse (selector.isChecked ());
        tester.click ("tableclicks.open", 300);
        selector.click ();
        synchronized (page) {
            page.wait (300);
        }
        Assertions.assertTrue (selector.isChecked ());
        Assertions.assertEquals ("1 selected", text (page, "tableclicks.selected"));
        Assertions.assertEquals ("button:" + name, text (page, "tableclicks.lastclick"));
    }

    @Test
    public void rowMenuClicksDoNotReachRowClick() throws Exception {
        HtmlPage page = openTableClicks ();
        PageTester tester = PageTester.$ (page);
        String name = text (page, "tableclicks.name");
        String row = "(//*[@test-id='tableclicks.name'])[1]/ancestor::tr[1]";

        // (1) An enabled item in the (hover-activated) row menu runs its own handler,
        // not the row handler. The menu is hidden until hovered, so its parts are
        // clicked regardless of visibility (as PageTester does).
        tester.click ("tableclicks.edit", 300);
        Assertions.assertEquals ("menu:" + name, text (page, "tableclicks.lastclick"));

        // (2) A disabled item, the activator icon and the menu's surface do not run
        // the row handler (the last click is unchanged).
        tester.click ("tableclicks.open", 300);
        Assertions.assertEquals ("button:" + name, text (page, "tableclicks.lastclick"));
        tester.click ("tableclicks.archive", 300);
        Assertions.assertEquals ("button:" + name, text (page, "tableclicks.lastclick"));
        click (page, row + "//div[contains(@class,'juiMenuActivator')]/em");
        Assertions.assertEquals ("button:" + name, text (page, "tableclicks.lastclick"));
        click (page, row + "//ul[contains(@class,'juiMenu')]");
        Assertions.assertEquals ("button:" + name, text (page, "tableclicks.lastclick"));
    }

    /**
     * Opens the "Table clicks" example under Galleries.
     */
    private HtmlPage openTableClicks() throws Exception {
        HtmlPage page = webClient.getPage ("http://localhost/playground?test=true");
        synchronized (page) {
            page.wait (4000);
        }
        PageTester.$ (page)
            .with (TabNavigatorTester.$ ("playgroundui").subclass (), tabs -> {
                tabs.activate ("gallery");
                tabs.validateActiveTab ("gallery");
            })
            // The galleries tab navigator carries no test-id of its own, so its tab is
            // clicked directly.
            .click ("playgroundui.gallery-tab_tableclicks", 1000);
        return page;
    }

    /**
     * Clicks the first element matching the XPath (regardless of its visibility).
     */
    private void click(HtmlPage page, String xpath) throws Exception {
        HtmlElement el = page.getFirstByXPath (xpath);
        Assertions.assertNotNull (el, "Unable to find element " + xpath);
        el.click (false, false, false, true, true, true, false);
        synchronized (page) {
            page.wait (300);
        }
    }

    /**
     * The text of the first element with the given test ID.
     */
    private String text(HtmlPage page, String testId) {
        List<Object> matches = page.getByXPath ("//*[@test-id='" + testId + "']");
        Assertions.assertFalse (matches.isEmpty (), "Unable to find element with test-id=\"" + testId + "\"");
        return ((DomElement) matches.get (0)).getTextContent ().trim ();
    }
}
