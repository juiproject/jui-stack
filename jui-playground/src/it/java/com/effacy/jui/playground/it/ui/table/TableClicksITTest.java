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

import java.net.URL;
import java.util.Arrays;
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

    @Test
    public void rowMenuNativeContentKeepsDefaultAction() throws Exception {
        HtmlPage page = openTableClicks ();
        String row = "(//*[@test-id='tableclicks.name'])[1]/ancestor::tr[1]";

        // (1) A checkbox in the (hover-activated) row menu toggles and does not run
        // the row handler.
        HtmlCheckBoxInput check = page.getFirstByXPath (row + "//*[@test-id='tableclicks.check']");
        Assertions.assertNotNull (check, "Unable to find the row menu checkbox");
        Assertions.assertFalse (check.isChecked ());
        click (page, row + "//*[@test-id='tableclicks.check']");
        Assertions.assertTrue (check.isChecked ());
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));

        // (2) A link in the row menu navigates and does not run the row handler.
        click (page, row + "//*[@test-id='tableclicks.link']");
        Assertions.assertEquals ("row-link", currentUrl ().getRef ());
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));
    }

    @Test
    public void hoverMenuNativeContentKeepsDefaultAction() throws Exception {
        HtmlPage page = openTableClicks ();

        // (1) A checkbox in a hover menu (outside the table) toggles.
        HtmlCheckBoxInput check = page.getFirstByXPath ("//*[@test-id='tableclicks.outside.check']");
        Assertions.assertNotNull (check, "Unable to find the menu checkbox");
        Assertions.assertFalse (check.isChecked ());
        click (page, "//*[@test-id='tableclicks.outside.check']");
        Assertions.assertTrue (check.isChecked ());

        // (2) A link in the menu navigates.
        click (page, "//*[@test-id='tableclicks.outside.link']");
        Assertions.assertEquals ("outside-link", currentUrl ().getRef ());
    }

    @Test
    public void clickRowMenuTogglesAndClicksDoNotReachRowClick() throws Exception {
        HtmlPage page = openTableClicks ();
        String name = text (page, "tableclicks.name");
        String row = "(//*[@test-id='tableclicks.name'])[1]/ancestor::tr[1]";
        String activator = row + "//*[@test-id='tableclicks.click.check']/ancestor::div[contains(@class,'juiMenuActivator')][1]";

        // (1) The activator icon opens and closes the (click-activated) row menu and
        // does not run the row handler.
        Assertions.assertFalse (isOpen (page, activator));
        click (page, activator + "/em");
        Assertions.assertTrue (isOpen (page, activator));
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));
        click (page, activator + "/em");
        Assertions.assertFalse (isOpen (page, activator));
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));

        // (2) An enabled item runs its own handler (not the row handler) and closes
        // the menu.
        click (page, activator + "/em");
        click (page, row + "//*[@test-id='tableclicks.click.edit']");
        Assertions.assertEquals ("clickmenu:" + name, text (page, "tableclicks.lastclick"));
        Assertions.assertFalse (isOpen (page, activator));

        // (3) A disabled item and the menu's surface do not run the row handler (the
        // last click is unchanged).
        click (page, activator + "/em");
        click (page, row + "//*[@test-id='tableclicks.click.archive']");
        Assertions.assertEquals ("clickmenu:" + name, text (page, "tableclicks.lastclick"));
        click (page, activator + "//ul[contains(@class,'juiMenu')]");
        Assertions.assertEquals ("clickmenu:" + name, text (page, "tableclicks.lastclick"));
    }

    @Test
    public void clickRowMenuNativeContentKeepsDefaultAction() throws Exception {
        HtmlPage page = openTableClicks ();
        String row = "(//*[@test-id='tableclicks.name'])[1]/ancestor::tr[1]";
        String activator = row + "//*[@test-id='tableclicks.click.check']/ancestor::div[contains(@class,'juiMenuActivator')][1]";

        // (1) A checkbox in the (click-activated) row menu toggles and does not run
        // the row handler. As with any click in the menu, the menu toggles (closes).
        HtmlCheckBoxInput check = page.getFirstByXPath (row + "//*[@test-id='tableclicks.click.check']");
        Assertions.assertNotNull (check, "Unable to find the row menu checkbox");
        click (page, activator + "/em");
        Assertions.assertTrue (isOpen (page, activator));
        Assertions.assertFalse (check.isChecked ());
        click (page, row + "//*[@test-id='tableclicks.click.check']");
        Assertions.assertTrue (check.isChecked ());
        Assertions.assertFalse (isOpen (page, activator));
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));

        // (2) A link in the row menu navigates and does not run the row handler.
        click (page, activator + "/em");
        click (page, row + "//*[@test-id='tableclicks.click.link']");
        Assertions.assertEquals ("row-click-link", currentUrl ().getRef ());
        Assertions.assertEquals ("none", text (page, "tableclicks.lastclick"));
    }

    @Test
    public void clickMenuNativeContentKeepsDefaultAction() throws Exception {
        HtmlPage page = openTableClicks ();
        String activator = "//*[@test-id='tableclicks.outside.clickcheck']/ancestor::div[contains(@class,'juiMenuActivator')][1]";

        // (1) The activator icon opens and closes a click-activated menu (outside the
        // table).
        Assertions.assertFalse (isOpen (page, activator));
        click (page, activator + "/em");
        Assertions.assertTrue (isOpen (page, activator));
        click (page, activator + "/em");
        Assertions.assertFalse (isOpen (page, activator));

        // (2) A checkbox in the menu toggles.
        HtmlCheckBoxInput check = page.getFirstByXPath ("//*[@test-id='tableclicks.outside.clickcheck']");
        Assertions.assertNotNull (check, "Unable to find the menu checkbox");
        Assertions.assertFalse (check.isChecked ());
        click (page, activator + "/em");
        click (page, "//*[@test-id='tableclicks.outside.clickcheck']");
        Assertions.assertTrue (check.isChecked ());

        // (3) A link in the menu navigates.
        click (page, activator + "/em");
        click (page, "//*[@test-id='tableclicks.outside.clicklink']");
        Assertions.assertEquals ("outside-click-link", currentUrl ().getRef ());
    }

    /**
     * Opens the "Table clicks" example under Galleries (waiting for each part to
     * render rather than for a fixed time).
     */
    private HtmlPage openTableClicks() throws Exception {
        HtmlPage page = webClient.getPage ("http://localhost/playground?test=true");
        waitFor (page, "playgroundui");
        PageTester.$ (page)
            .with (TabNavigatorTester.$ ("playgroundui").subclass (), tabs -> {
                tabs.activate ("gallery");
                tabs.validateActiveTab ("gallery");
            });
        // The galleries tab navigator carries no test-id of its own, so its tab is
        // clicked directly.
        waitFor (page, "playgroundui.gallery-tab_tableclicks");
        PageTester.$ (page).click ("playgroundui.gallery-tab_tableclicks", 0);
        waitFor (page, "tableclicks.name");
        return page;
    }

    /**
     * Waits (for up to 20s) for an element with the given test ID to appear.
     */
    private void waitFor(HtmlPage page, String testId) throws Exception {
        for (int i = 0; i < 200; i++) {
            if (page.getFirstByXPath ("//*[@test-id='" + testId + "']") != null)
                return;
            synchronized (page) {
                page.wait (100);
            }
        }
        Assertions.fail ("Timed out waiting for element with test-id=\"" + testId + "\"");
    }

    /**
     * The URL of the page currently in the browser window.
     */
    private URL currentUrl() {
        return webClient.getCurrentWindow ().getEnclosedPage ().getUrl ();
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
     * Whether the menu activator matching the XPath is open.
     */
    private boolean isOpen(HtmlPage page, String xpath) {
        DomElement el = page.getFirstByXPath (xpath);
        Assertions.assertNotNull (el, "Unable to find element " + xpath);
        return Arrays.asList (el.getAttribute ("class").split ("\\s+")).contains ("open");
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
