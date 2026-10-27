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
 * Components placed in table cells with {@code Cpt.$} (the "Table components"
 * example under Galleries): they get their own events, before any row click
 * handler, and are released when their rows are re-rendered or removed.
 */
@SpringBootTest(classes = TestApplicationIT.class)
public class TableComponentsITTest extends AbstractIT {

    @Test
    public void componentHandlersRunBeforeRowClick() throws Exception {
        HtmlPage page = openTableComponents ();
        String name = text (page, "tablecpts.name");
        Assertions.assertEquals ("none", text (page, "tablecpts.lastclick"));

        // (1) A button component in a cell of a table with a row handler runs its own
        // handler and not the row handler.
        clickButton (page, "tablecpts.open");
        Assertions.assertEquals ("button:" + name, text (page, "tablecpts.lastclick"));

        // (2) Plain cell content still runs the row handler.
        click (page, "tablecpts.name");
        Assertions.assertEquals ("row:" + name, text (page, "tablecpts.lastclick"));

        // (3) A button component in a cell of a table without a row handler runs its
        // own handler.
        String plainName = text (page, "tablecpts.plain.name");
        clickButton (page, "tablecpts.plain.open");
        Assertions.assertEquals ("plain:" + plainName, text (page, "tablecpts.lastclick"));
    }

    @Test
    public void componentsReleasedOnRerenderAndReload() throws Exception {
        HtmlPage page = openTableComponents ();
        String name = text (page, "tablecpts.name");
        click (page, "tablecpts.count");
        Assertions.assertEquals ("10 live", text (page, "tablecpts.live"));

        // (1) Re-rendering the rows in place releases the components of the earlier
        // render: as many are live as there are rows, and each row holds one button
        // that still gets its clicks.
        click (page, "tablecpts.refresh");
        click (page, "tablecpts.count");
        Assertions.assertEquals ("10 live", text (page, "tablecpts.live"));
        Assertions.assertEquals (10, count (page, "tablecpts.open"));
        clickButton (page, "tablecpts.open");
        Assertions.assertEquals ("button:" + name, text (page, "tablecpts.lastclick"));

        // (2) Reversing the rows (which re-inserts those out of order) releases the
        // components of the rows replaced, and the buttons get their clicks.
        String last = textAt (page, "tablecpts.name", 9);
        click (page, "tablecpts.reverse");
        Assertions.assertEquals (last, text (page, "tablecpts.name"));
        click (page, "tablecpts.count");
        Assertions.assertEquals ("10 live", text (page, "tablecpts.live"));
        Assertions.assertEquals (10, count (page, "tablecpts.open"));
        clickButton (page, "tablecpts.open");
        Assertions.assertEquals ("button:" + last, text (page, "tablecpts.lastclick"));

        // (3) Reloading with fewer rows releases the components of the rows removed.
        click (page, "tablecpts.reload");
        click (page, "tablecpts.count");
        Assertions.assertEquals ("5 live", text (page, "tablecpts.live"));
        Assertions.assertEquals (5, count (page, "tablecpts.open"));
        click (page, "tablecpts.name");
        Assertions.assertEquals ("row:" + name, text (page, "tablecpts.lastclick"));
        clickButton (page, "tablecpts.open");
        Assertions.assertEquals ("button:" + name, text (page, "tablecpts.lastclick"));
    }

    @Test
    public void controlClicksDoNotReachRowClick() throws Exception {
        HtmlPage page = openTableComponents ();
        String row = "(" + byTestId ("tablecpts.name") + ")[1]/ancestor::tr[1]";

        // (1) The checkbox of a check control in a cell toggles and does not run the
        // row handler.
        HtmlCheckBoxInput check = page.getFirstByXPath (row + "//input[@type='checkbox']");
        Assertions.assertNotNull (check, "Unable to find the check control's checkbox");
        Assertions.assertFalse (check.isChecked ());
        check.click (false, false, false, true, true, true, false);
        synchronized (page) {
            page.wait (300);
        }
        Assertions.assertTrue (check.isChecked ());
        Assertions.assertEquals ("none", text (page, "tablecpts.lastclick"));

        // (2) A click in the input of a text control in a cell does not run the row
        // handler.
        HtmlElement input = page.getFirstByXPath ("(" + byTestId ("tablecpts.note") + ")[1]//input");
        Assertions.assertNotNull (input, "Unable to find the text control's input");
        input.click (false, false, false, true, true, true, false);
        synchronized (page) {
            page.wait (300);
        }
        Assertions.assertEquals ("none", text (page, "tablecpts.lastclick"));
    }

    /**
     * Opens the "Table components" example under Galleries (waiting for each part
     * to render rather than for a fixed time).
     */
    private HtmlPage openTableComponents() throws Exception {
        HtmlPage page = webClient.getPage ("http://localhost/playground?test=true");
        waitFor (page, "playgroundui");
        PageTester.$ (page)
            .with (TabNavigatorTester.$ ("playgroundui").subclass (), tabs -> {
                tabs.activate ("gallery");
                tabs.validateActiveTab ("gallery");
            });
        // The galleries tab navigator carries no test-id of its own, so its tab is
        // clicked directly.
        waitFor (page, "playgroundui.gallery-tab_tablecomponents");
        PageTester.$ (page).click ("playgroundui.gallery-tab_tablecomponents", 0);
        waitFor (page, "tablecpts.name");
        waitFor (page, "tablecpts.plain.name");
        return page;
    }

    /**
     * XPath for the elements with the given test ID, or one ending in it. A
     * component's test ID is prefixed with its parent's once it is adopted (as
     * cell components are by the table).
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
     * Clicks the button component with the given test ID (its anchor, which is what
     * the button handles clicks on).
     */
    private void clickButton(HtmlPage page, String testId) throws Exception {
        HtmlElement el = page.getFirstByXPath ("(" + byTestId (testId) + ")[1]//*[@test-ref='action']");
        Assertions.assertNotNull (el, "Unable to find button with test-id=\"" + testId + "\"");
        el.click ();
        synchronized (page) {
            page.wait (300);
        }
    }

    /**
     * Waits (for up to 20s) for an element with the given test ID to appear.
     */
    private void waitFor(HtmlPage page, String testId) throws Exception {
        for (int i = 0; i < 200; i++) {
            if (page.getFirstByXPath (byTestId (testId)) != null)
                return;
            synchronized (page) {
                page.wait (100);
            }
        }
        Assertions.fail ("Timed out waiting for element with test-id=\"" + testId + "\"");
    }

    /**
     * The number of elements with the given test ID.
     */
    private int count(HtmlPage page, String testId) {
        return page.getByXPath (byTestId (testId)).size ();
    }

    /**
     * The text of the element with the given test ID at the given (zero-based)
     * position.
     */
    private String textAt(HtmlPage page, String testId, int index) {
        List<Object> matches = page.getByXPath (byTestId (testId));
        Assertions.assertTrue (matches.size () > index, "Unable to find element " + index + " with test-id=\"" + testId + "\"");
        return ((DomElement) matches.get (index)).getTextContent ().trim ();
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
