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
package com.effacy.jui.playground.it.ui.navigation;

import java.util.List;

import org.htmlunit.html.DomElement;
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
 * Tools at the end of a tab navigator's strip, and a hidden tab group (the
 * "Tools &amp; hidden" example under Test suite, Tabs).
 */
@SpringBootTest(classes = TestApplicationIT.class)
public class TabNavigatorToolsITTest extends AbstractIT {

    /**
     * The tools are drawn beside the strip (not in it), and the hidden group's tab
     * is not drawn.
     */
    @Test
    public void toolsDrawnAndHiddenTabNot() throws Exception {
        HtmlPage page = openTools ();

        PageTester.$ (page).with (TabNavigatorTester.$ (testIdOf (page, "tabtools")).subclass (), tabs -> {
            tabs.validateTabs ("overview", "lifecycle", "variants");
            tabs.validateTabsNotDrawn ("analysis");
            tabs.validateTools (true);
            tabs.validateActiveTab ("overview");
        });

        // The selector is in the tools cell, which sits beside the strip (the
        // header) rather than in it, so the strip's styling of its lists does not
        // reach the selector's menu.
        String tools = toolsOf ("tabtools");
        Assertions.assertNotNull (page.getFirstByXPath (tools + "//*[@test-id and contains(@test-id, 'selector')]"), "Selector not in the tools");
        Assertions.assertNotNull (page.getFirstByXPath (tools + "/preceding-sibling::header"), "Tools not beside the strip");
        Assertions.assertNull (page.getFirstByXPath (tools + "/ancestor::header"), "Tools in the strip");
        Assertions.assertNotNull (page.getFirstByXPath (tools + "//ul[contains(@class,'juiMenu')]"), "Menu not in the tools");
    }

    /**
     * A navigator without tools is built as before: the strip directly in the
     * wrap, and no tools cell.
     */
    @Test
    public void withoutToolsBuiltAsBefore() throws Exception {
        HtmlPage page = openTools ();
        click (page, "tabtools-tab_variants");
        waitFor (page, "variant-horizontal-underline-plain");

        PageTester.$ (page).with (TabNavigatorTester.$ (testIdOf (page, "variant-horizontal-underline-plain")).subclass (), tabs -> {
            tabs.validateTabs ("one", "two");
            tabs.validateTools (false);
        });
        PageTester.$ (page).with (TabNavigatorTester.$ (testIdOf (page, "variant-horizontal-underline-tools")).subclass (), tabs -> {
            tabs.validateTabs ("one", "two");
            tabs.validateTools (true);
        });
        Assertions.assertNotNull (page.getFirstByXPath ("(" + byTestId ("variant-horizontal-underline-plain") + ")[1]/div/header"), "Strip not in the wrap");
        Assertions.assertNotNull (page.getFirstByXPath ("(" + byTestId ("variant-horizontal-underline-tools") + ")[1]/div/div/header"), "Strip not in the strip cell");
    }

    /**
     * The hidden tab is reached by reference (activate) from the menu in the tools:
     * it is activated, its own default page shown, the navigation path updated,
     * and still it is not drawn.
     */
    @Test
    public void hiddenTabByReference() throws Exception {
        HtmlPage page = openTools ();
        openMenu (page);
        click (page, "tabtools.menu.analysis");

        Assertions.assertEquals ("analysis", stateOf (page, "tabtools"));
        Assertions.assertEquals ("comparison", stateOf (page, "tabtools.analysis"));
        waitForHash (page, "#/tabs/tools/analysis/comparison");
        PageTester.$ (page).with (TabNavigatorTester.$ (testIdOf (page, "tabtools")).subclass (), tabs -> {
            tabs.validateTabsNotDrawn ("analysis");
        });

        // None of the drawn tabs shows as active.
        Assertions.assertNull (page.getFirstByXPath ("(" + byTestId ("tabtools") + ")[1]/div/div/header//li[@item and contains(concat(' ', normalize-space(@class), ' '), ' active ')]"), "A drawn tab is active");

        // And a drawn tab is reached again from there.
        click (page, "tabtools-tab_overview");
        Assertions.assertEquals ("overview", stateOf (page, "tabtools"));
        waitForHash (page, "#/tabs/tools/overview");
    }

    /**
     * A page of the hidden tab is reached by path (navigate) from the menu in the
     * tools.
     */
    @Test
    public void hiddenTabByPath() throws Exception {
        HtmlPage page = openTools ();
        openMenu (page);
        click (page, "tabtools.menu.moves");

        Assertions.assertEquals ("analysis", stateOf (page, "tabtools"));
        Assertions.assertEquals ("moves", stateOf (page, "tabtools.analysis"));
        waitForHash (page, "#/tabs/tools/analysis/moves");

        openMenu (page);
        click (page, "tabtools.menu.threats");
        Assertions.assertEquals ("threats", stateOf (page, "tabtools.analysis"));
        waitForHash (page, "#/tabs/tools/analysis/threats");
    }

    /**
     * A page of the hidden tab is reached by a deep link (a change to the browser's
     * location), from elsewhere in the application.
     */
    @Test
    public void hiddenTabByDeepLink() throws Exception {
        HtmlPage page = webClient.getPage ("http://localhost/playground?test=true");
        waitFor (page, "playgroundui");

        page.executeJavaScript ("location.hash = '/tabs/tools/analysis/threats';");
        waitForXPath (page, toolsOf ("tabtools"));
        waitForState (page, "tabtools.analysis", "threats");

        Assertions.assertEquals ("analysis", stateOf (page, "tabtools"));
        Assertions.assertEquals ("threats", stateOf (page, "tabtools.analysis"));
        PageTester.$ (page).with (TabNavigatorTester.$ (testIdOf (page, "tabtools")).subclass (), tabs -> {
            tabs.validateTabsNotDrawn ("analysis");
        });
    }

    /**
     * The tools are rendered with the navigator and disposed of with it.
     */
    @Test
    public void toolsDisposedWithNavigator() throws Exception {
        HtmlPage page = openTools ();
        click (page, "tabtools-tab_lifecycle");
        waitFor (page, "tabtools.mount");

        click (page, "tabtools.count");
        Assertions.assertEquals ("0 live", text (page, "tabtools.live"));

        click (page, "tabtools.mount");
        waitFor (page, "tabtools.tool.a");
        Assertions.assertNotNull (page.getFirstByXPath (toolsOf ("tabtools.mounted") + byTestId ("tabtools.tool.b")), "Tool B not in the tools");
        click (page, "tabtools.count");
        Assertions.assertEquals ("2 live", text (page, "tabtools.live"));

        click (page, "tabtools.unmount");
        Assertions.assertNull (page.getFirstByXPath (byTestId ("tabtools.tool.a")));
        click (page, "tabtools.count");
        Assertions.assertEquals ("0 live", text (page, "tabtools.live"));

        click (page, "tabtools.mount");
        waitFor (page, "tabtools.tool.a");
        click (page, "tabtools.count");
        Assertions.assertEquals ("2 live", text (page, "tabtools.live"));
        click (page, "tabtools.unmount");
        click (page, "tabtools.count");
        Assertions.assertEquals ("0 live", text (page, "tabtools.live"));
    }

    /**
     * Opens the "Tools &amp; hidden" example (under Test suite, Tabs).
     */
    private HtmlPage openTools() throws Exception {
        HtmlPage page = webClient.getPage ("http://localhost/playground?test=true");
        waitFor (page, "playgroundui");
        PageTester.$ (page)
            .with (TabNavigatorTester.$ ("playgroundui").subclass (), tabs -> {
                tabs.activate ("tabs");
                tabs.validateActiveTab ("tabs");
            });
        waitFor (page, "playgroundui.tabs-tab_tools");
        click (page, "playgroundui.tabs-tab_tools");
        waitForXPath (page, toolsOf ("tabtools"));
        return page;
    }

    /**
     * Opens the selector's (click-activated) menu in the tools.
     */
    private void openMenu(HtmlPage page) throws Exception {
        String activator = toolsOf ("tabtools") + "//div[contains(@class,'juiMenuActivator')]";
        HtmlElement em = page.getFirstByXPath (activator + "/em");
        Assertions.assertNotNull (em, "Unable to find the selector's activator");
        em.click ();
        synchronized (page) {
            page.wait (300);
        }
        DomElement el = page.getFirstByXPath (activator);
        Assertions.assertTrue (el.getAttribute ("class").contains ("open"), "Menu not open");
    }

    /**
     * The location's hash.
     */
    private String hash(HtmlPage page) {
        return String.valueOf (page.executeJavaScript ("location.hash").getJavaScriptResult ());
    }

    /**
     * Waits (for up to 5s) for the location's hash to be the given one. A tab's
     * navigator rendered for the first time records its default page before the
     * page navigated to, so the hash can pass through that first.
     */
    private void waitForHash(HtmlPage page, String expected) throws Exception {
        for (int i = 0; i < 50; i++) {
            if (expected.equals (hash (page)))
                return;
            synchronized (page) {
                page.wait (100);
            }
        }
        Assertions.assertEquals (expected, hash (page));
    }

    /**
     * The (full) test ID of the first element with the given test ID (see
     * {@link #byTestId(String)}).
     */
    private String testIdOf(HtmlPage page, String testId) {
        DomElement el = page.getFirstByXPath (byTestId (testId));
        Assertions.assertNotNull (el, "Unable to find element with test-id=\"" + testId + "\"");
        return el.getAttribute ("test-id");
    }

    /**
     * The active tab (test state) of the navigator with the given test ID.
     */
    private String stateOf(HtmlPage page, String testId) {
        DomElement el = page.getFirstByXPath (byTestId (testId));
        Assertions.assertNotNull (el, "Unable to find element with test-id=\"" + testId + "\"");
        return el.getAttribute ("test-state");
    }

    /**
     * Waits (for up to 20s) for the navigator with the given test ID to have the
     * given active tab.
     */
    private void waitForState(HtmlPage page, String testId, String state) throws Exception {
        for (int i = 0; i < 200; i++) {
            DomElement el = page.getFirstByXPath (byTestId (testId));
            if ((el != null) && state.equals (el.getAttribute ("test-state")))
                return;
            synchronized (page) {
                page.wait (100);
            }
        }
        Assertions.fail ("Timed out waiting for test-id=\"" + testId + "\" to have test-state=\"" + state + "\"");
    }

    /**
     * XPath for the elements with the given test ID, or one ending in it. A
     * component's test ID is prefixed with its parent's once it is adopted.
     */
    private String byTestId(String testId) {
        return "//*[(@test-id='" + testId + "') or (substring(@test-id, string-length(@test-id) - " + testId.length () + ") = '." + testId + "')]";
    }

    /**
     * Clicks the first element with the given test ID (see {@link #byTestId(String)}).
     */
    private void click(HtmlPage page, String testId) throws Exception {
        HtmlElement el = page.getFirstByXPath (byTestId (testId));
        Assertions.assertNotNull (el, "Unable to find element with test-id=\"" + testId + "\"");
        el.click ();
        synchronized (page) {
            page.wait (300);
        }
    }

    /**
     * Waits (for up to 20s) for an element with the given test ID to appear.
     */
    private void waitFor(HtmlPage page, String testId) throws Exception {
        waitForXPath (page, byTestId (testId));
    }

    /**
     * Waits (for up to 20s) for an element matching the given XPath to appear.
     */
    private void waitForXPath(HtmlPage page, String xpath) throws Exception {
        for (int i = 0; i < 200; i++) {
            if (page.getFirstByXPath (xpath) != null)
                return;
            synchronized (page) {
                page.wait (100);
            }
        }
        Assertions.fail ("Timed out waiting for element " + xpath);
    }

    /**
     * XPath for the tools cell of the (first) navigator with the given test ID
     * (see {@link #byTestId(String)}). The cell carries the test reference
     * {@code tools}.
     */
    private String toolsOf(String testId) {
        return "((" + byTestId (testId) + ")[1]//*[@test-ref='tools'])[1]";
    }

    /**
     * The text of the first element with the given test ID.
     */
    private String text(HtmlPage page, String testId) {
        List<Object> matches = page.getByXPath (byTestId (testId));
        Assertions.assertFalse (matches.isEmpty (), "Unable to find element with test-id=\"" + testId + "\"");
        return ((DomElement) matches.get (0)).getTextContent ().trim ();
    }
}
