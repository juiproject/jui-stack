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
package com.effacy.jui.playground.it.ui.button;

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.effacy.jui.playground.TestApplicationIT;
import com.effacy.jui.playground.it.AbstractIT;
import com.effacy.jui.test.suite.navigation.TabNavigatorTester;
import com.effacy.jui.test.PageTester;

/**
 * Buttons with options (the "Buttons" tab under "Test suite", see
 * {@code ButtonSuite}): a button without options builds and behaves as before;
 * the chevron of a split button opens and closes its menu; Escape and a click
 * outside (on the backdrop) close it; Enter chooses the active option, which
 * runs after the menu has closed; disabling the button closes it; a menu button
 * opens its options in place of its handler.
 */
@SpringBootTest(classes = TestApplicationIT.class)
public class ButtonOptionsITTest extends AbstractIT {

    /**
     * A button without options has the DOM it always had: a root holding only the
     * outer, which holds only the action anchor (with its spinner, icon and label
     * and no menu attributes). Clicking runs its handler.
     */
    @Test
    public void buttonWithoutOptionsUnchanged() throws Exception {
        HtmlPage page = openButtons ();

        HtmlElement root = find (page, "buttons.plain");
        List<DomElement> rootChildren = children (root);
        Assertions.assertEquals (1, rootChildren.size (), "Root holds only the outer");
        Assertions.assertEquals ("div", rootChildren.get (0).getTagName ());
        List<DomElement> outerChildren = children (rootChildren.get (0));
        Assertions.assertEquals (1, outerChildren.size (), "Outer holds only the action");
        DomElement action = outerChildren.get (0);
        Assertions.assertEquals ("a", action.getTagName ());
        Assertions.assertEquals ("action", action.getAttribute ("test-ref"));
        Assertions.assertEquals ("javascript:;", action.getAttribute ("href"));
        Assertions.assertFalse (action.hasAttribute ("aria-haspopup"));
        Assertions.assertFalse (action.hasAttribute ("aria-expanded"));
        Assertions.assertEquals (List.of ("i", "em", "span"), tags (children (action)));
        Assertions.assertEquals ("Plain", children (action).get (2).getTextContent ());

        click (page, action (page, "buttons.plain"));
        Assertions.assertEquals ("plain:1", status (page));
        click (page, action (page, "buttons.plain"));
        Assertions.assertEquals ("plain:2", status (page));
    }

    /**
     * The chevron of a split button opens the menu (its rows in the order added,
     * with their roles and test IDs) and closes it again; the action runs the
     * button's own handler.
     */
    @Test
    public void splitChevronOpensAndCloses() throws Exception {
        HtmlPage page = openButtons ();

        HtmlElement toggle = find (page, "buttons.split-toggle");
        Assertions.assertEquals ("menu", toggle.getAttribute ("aria-haspopup"));
        Assertions.assertEquals ("false", toggle.getAttribute ("aria-expanded"));
        Assertions.assertEquals ("More options for Add requirement", toggle.getAttribute ("aria-label"));
        Assertions.assertNull (menu (page, "buttons.split"));

        click (page, toggle);
        HtmlElement menu = menu (page, "buttons.split");
        Assertions.assertNotNull (menu, "Menu opens");
        Assertions.assertEquals ("menu", menu.getAttribute ("role"));
        Assertions.assertEquals ("true", toggle.getAttribute ("aria-expanded"));
        List<DomElement> rows = children (menu);
        Assertions.assertEquals (4, rows.size ());
        String [] ids = { "REQ", "RULE", "CON", "Non-functional requirement" };
        for (int i = 0; i < ids.length; i++) {
            Assertions.assertEquals ("menuitem", rows.get (i).getAttribute ("role"));
            Assertions.assertTrue (rows.get (i).getAttribute ("test-id").endsWith ("buttons.split-option-" + ids[i]), rows.get (i).getAttribute ("test-id"));
        }
        Assertions.assertTrue (rows.get (0).getTextContent ().contains ("Requirement"));
        Assertions.assertTrue (rows.get (0).getTextContent ().contains ("Something the system must do"));

        // The menu is placed against the window.
        Assertions.assertEquals ("fixed", page.executeJavaScript ("getComputedStyle(document.querySelector(\"[test-id$='buttons.split-menu']\")).position").getJavaScriptResult ().toString ());

        click (page, toggle);
        Assertions.assertNull (menu (page, "buttons.split"), "Menu closes");
        Assertions.assertEquals ("false", toggle.getAttribute ("aria-expanded"));
        Assertions.assertEquals ("none", status (page));

        click (page, action (page, "buttons.split"));
        Assertions.assertEquals ("split:main", status (page));
        Assertions.assertNull (menu (page, "buttons.split"));
    }

    /**
     * Escape closes the menu, as does a click outside it (on the backdrop beneath
     * it); neither runs anything.
     */
    @Test
    public void escapeAndClickOutsideClose() throws Exception {
        HtmlPage page = openButtons ();

        click (page, find (page, "buttons.split-toggle"));
        Assertions.assertNotNull (menu (page, "buttons.split"));
        key (page, "Escape");
        Assertions.assertNull (menu (page, "buttons.split"), "Escape closes");

        click (page, find (page, "buttons.split-toggle"));
        Assertions.assertNotNull (menu (page, "buttons.split"));
        HtmlElement backdrop = page.getFirstByXPath ("(" + byTestId ("buttons.split") + ")[1]//*[@test-ref='backdrop']");
        Assertions.assertNotNull (backdrop, "Backdrop beneath the open menu");
        click (page, backdrop);
        Assertions.assertNull (menu (page, "buttons.split"), "A click outside closes");
        Assertions.assertEquals ("none", status (page));

        // Tab also closes.
        click (page, find (page, "buttons.split-toggle"));
        key (page, "Tab");
        Assertions.assertNull (menu (page, "buttons.split"), "Tab closes");
    }

    /**
     * Down and Up move through the options (wrapping) and Enter chooses the active
     * one: the menu closes, then the option's handler runs (it sees the menu
     * closed), and the trigger is not clicked again.
     */
    @Test
    public void enterChoosesAfterClosing() throws Exception {
        HtmlPage page = openButtons ();

        HtmlElement toggle = find (page, "buttons.split-toggle");
        toggle.focus ();
        click (page, toggle);
        Assertions.assertNotNull (menu (page, "buttons.split"));
        key (page, "ArrowDown");
        key (page, "ArrowDown");
        key (page, "ArrowDown");
        key (page, "ArrowUp");
        Assertions.assertEquals (1, activeIndex (page, "buttons.split"));
        key (page, "Enter");
        Assertions.assertNull (menu (page, "buttons.split"), "Choosing closes the menu");
        Assertions.assertEquals ("split:RULE closed", status (page));

        // Up from none wraps to the last.
        click (page, toggle);
        key (page, "ArrowUp");
        Assertions.assertEquals (3, activeIndex (page, "buttons.split"));
        key (page, "Enter");
        Assertions.assertEquals ("split:NFR closed", status (page));
        Assertions.assertNull (menu (page, "buttons.split"));

        // Down on the closed trigger opens with the first active.
        keyOn (page, "buttons.split-toggle", "ArrowDown");
        Assertions.assertNotNull (menu (page, "buttons.split"));
        Assertions.assertEquals (0, activeIndex (page, "buttons.split"));
        key (page, "Enter");
        Assertions.assertEquals ("split:REQ closed", status (page));
    }

    /**
     * Disabling the button closes its menu, and a disabled button's chevron does
     * not open it.
     */
    @Test
    public void disablingCloses() throws Exception {
        HtmlPage page = openButtons ();

        click (page, find (page, "buttons.split-toggle"));
        Assertions.assertNotNull (menu (page, "buttons.split"));
        click (page, find (page, "buttons.disable"));
        Assertions.assertNull (menu (page, "buttons.split"), "Disabling closes");
        Assertions.assertEquals ("false", find (page, "buttons.split-toggle").getAttribute ("aria-expanded"));

        click (page, find (page, "buttons.split-toggle"));
        Assertions.assertNull (menu (page, "buttons.split"), "Disabled does not open");

        click (page, find (page, "buttons.enable"));
        click (page, find (page, "buttons.split-toggle"));
        Assertions.assertNotNull (menu (page, "buttons.split"), "Enabled opens again");
    }

    /**
     * A menu button opens its options from the button itself (with a chevron after
     * its label) and its own handler is not used; clicking an option closes the
     * menu then runs it.
     */
    @Test
    public void menuButtonOpensOptions() throws Exception {
        HtmlPage page = openButtons ();

        HtmlElement action = action (page, "buttons.menu");
        Assertions.assertEquals ("menu", action.getAttribute ("aria-haspopup"));
        Assertions.assertEquals ("false", action.getAttribute ("aria-expanded"));
        Assertions.assertEquals (List.of ("i", "span", "em"), tags (children (action)));
        Assertions.assertNull (page.getFirstByXPath ("(" + byTestId ("buttons.menu") + ")[1]//*[@test-ref='toggle']"), "No separate chevron");

        click (page, action);
        Assertions.assertNotNull (menu (page, "buttons.menu"));
        Assertions.assertEquals ("true", action.getAttribute ("aria-expanded"));
        Assertions.assertEquals ("none", status (page), "The button's handler is not used");

        click (page, find (page, "buttons.menu-option-RULE"));
        Assertions.assertNull (menu (page, "buttons.menu"));
        Assertions.assertEquals ("menu:RULE closed", status (page));

        click (page, action);
        click (page, action);
        Assertions.assertNull (menu (page, "buttons.menu"), "The button closes it again");
        Assertions.assertEquals ("menu:RULE closed", status (page));
    }

    /**
     * The split button in a container that clips its overflow opens a menu placed
     * against the window (whether it is clipped is checked in a browser, as
     * HtmlUnit does no layout).
     */
    @Test
    public void clippedContainer() throws Exception {
        HtmlPage page = openButtons ();

        click (page, find (page, "buttons.clipped-toggle"));
        Assertions.assertNotNull (menu (page, "buttons.clipped"));
        Assertions.assertEquals ("fixed", page.executeJavaScript ("getComputedStyle(document.querySelector(\"[test-id$='buttons.clipped-menu']\")).position").getJavaScriptResult ().toString ());
        click (page, find (page, "buttons.clipped-option-CON"));
        Assertions.assertEquals ("clipped:CON closed", status (page));
    }

    /************************************************************************
     * Support
     ************************************************************************/

    /**
     * Opens the "Buttons" tab under "Test suite".
     */
    private HtmlPage openButtons() throws Exception {
        HtmlPage page = webClient.getPage ("http://localhost/playground?test=true");
        waitFor (page, "playgroundui");
        PageTester.$ (page)
            .with (TabNavigatorTester.$ ("playgroundui").subclass (), tabs -> {
                tabs.activate ("testing-buttons");
                tabs.validateActiveTab ("testing-buttons");
            });
        waitFor (page, "buttons.status");
        waitFor (page, "buttons.transformed");
        return page;
    }

    /**
     * XPath for the elements with the given test ID, or one ending in it (a
     * component's test ID is prefixed with its parent's once adopted).
     */
    private String byTestId(String testId) {
        return "//*[(@test-id='" + testId + "') or (substring(@test-id, string-length(@test-id) - " + testId.length () + ") = '." + testId + "')]";
    }

    private HtmlElement find(HtmlPage page, String testId) {
        HtmlElement el = page.getFirstByXPath (byTestId (testId));
        Assertions.assertNotNull (el, "Unable to find element with test-id=\"" + testId + "\"");
        return el;
    }

    /**
     * The action anchor of the button with the given test ID.
     */
    private HtmlElement action(HtmlPage page, String testId) {
        HtmlElement el = page.getFirstByXPath ("(" + byTestId (testId) + ")[1]//*[@test-ref='action']");
        Assertions.assertNotNull (el, "Unable to find the action of button test-id=\"" + testId + "\"");
        return el;
    }

    /**
     * The open menu of the button with the given test ID (or {@code null}).
     */
    private HtmlElement menu(HtmlPage page, String testId) {
        return page.getFirstByXPath (byTestId (testId + "-menu"));
    }

    /**
     * The index of the active option of the open menu (its row carries a class the
     * others do not).
     */
    private int activeIndex(HtmlPage page, String testId) {
        List<DomElement> rows = children (menu (page, testId));
        // The active row is the one whose classes differ from the rest.
        for (int i = 0; i < rows.size (); i++) {
            String cls = rows.get (i).getAttribute ("class");
            int same = 0;
            for (DomElement row : rows) {
                if (row.getAttribute ("class").equals (cls))
                    same++;
            }
            if ((same == 1) && (rows.size () > 1))
                return i;
        }
        return -1;
    }

    private String status(HtmlPage page) {
        return find (page, "buttons.status").getTextContent ();
    }

    private void click(HtmlPage page, HtmlElement el) throws Exception {
        el.click ();
        pause (page);
    }

    /**
     * Dispatches a key-down for the given key on the body (the menu listens on the
     * document).
     */
    private void key(HtmlPage page, String key) throws Exception {
        page.executeJavaScript ("document.body.dispatchEvent(new KeyboardEvent('keydown', { key: '" + key + "', bubbles: true, cancelable: true }));");
        pause (page);
    }

    /**
     * Dispatches a key-down for the given key on the element with the given test
     * ID.
     */
    private void keyOn(HtmlPage page, String testId, String key) throws Exception {
        page.executeJavaScript ("document.querySelector(\"[test-id$='" + testId + "']\").dispatchEvent(new KeyboardEvent('keydown', { key: '" + key + "', bubbles: true, cancelable: true }));");
        pause (page);
    }

    private void pause(HtmlPage page) throws Exception {
        synchronized (page) {
            page.wait (300);
        }
    }

    private List<DomElement> children(DomElement el) {
        List<DomElement> children = new ArrayList<> ();
        for (DomElement child : el.getChildElements ())
            children.add (child);
        return children;
    }

    private List<String> tags(List<DomElement> els) {
        List<String> tags = new ArrayList<> ();
        els.forEach (el -> tags.add (el.getTagName ()));
        return tags;
    }

    /**
     * Waits (up to 10s) for an element with the given test ID to appear.
     */
    private void waitFor(HtmlPage page, String testId) throws Exception {
        for (int i = 0; i < 100; i++) {
            if (page.getFirstByXPath (byTestId (testId)) != null)
                return;
            synchronized (page) {
                page.wait (100);
            }
        }
        Assertions.fail ("Timed out waiting for test-id=\"" + testId + "\"");
    }
}
