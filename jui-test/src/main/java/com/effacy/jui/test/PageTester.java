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
package com.effacy.jui.test;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;

import com.effacy.jui.test.IPage.INode;
import com.effacy.jui.test.modal.ModalDialogTester;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageTester implements ITester<PageTester> {

    /**
     * Convenience to construct around an {@code HtmlPage} as the source.
     * 
     * @param page
     *             the page source.
     * @return the associated tester.
     */
    public static PageTester $(HtmlPage page) {
        return new PageTester( new HtmlUnitPage (page, null));
    }
    
    /**
     * Loads a page using a {@link WebClient}.
     * 
     * @param client
     *               the client to use.
     * @param url
     *               the URL to load.
     * @param delay
     *               the delay post load to wait for the JS to complete.
     * @return the page tester.
     * @throws Exception on error.
     */
    public static PageTester $(WebClient client, String url, long delay) throws Exception {
        HtmlPage page = client.getPage (url);
        if (delay > 0) {
            synchronized (page) {
                page.wait(delay);
            }
        }
        return $ (page);
    }

    /**
     * Loads a page using a {@link WebClient} and uses a separate test to determine
     * when the JS has loaded (this is run with a 500ms delay upto a wait of 10s).
     * 
     * @param client    the client to use.
     * @param url       the URL to load.
     * @param delayTest the test for the page to determined when deemed loaded (i.e.
     *                  to test that the JS has loaded).
     * @return the page tester.
     * @throws Exception on error.
     */
    public static PageTester $(WebClient client, String url, Predicate<HtmlPage> delayTest) throws Exception {
        HtmlPage page = client.getPage (url);
        LOOP: for (int i = 0; i < 20; i++) {
            synchronized (page) {
                page.wait (500);
            }
            if (delayTest.test (page))
                break LOOP;
        }
        return $ (page);
    }

    /**
     * Convenience to construct around an {@code SeleniumPage} as the source.
     * 
     * @param page
     *             the page source.
     * @return the associated tester.
     */
    public static PageTester $(WebDriver driver) {
        return new PageTester (new SeleniumPage (driver, null));
    }
    
    /**
     * Loads a page using a {@link WebClient}.
     * 
     * @param client
     *               the client to use.
     * @param url
     *               the URL to load.
     * @param delay
     *               the delay post load to wait for the JS to complete.
     * @return the page tester.
     * @throws Exception on error.
     */
    public static PageTester $(WebDriver driver, String url, long delay) throws Exception {
        driver.get (url);
        if (delay > 0) {
            synchronized (driver) {
                driver.wait (delay);
            }
        }
        return $ (driver);
    }

    /**
     * Loads a page using a {@link WebClient} and uses a separate test to determine
     * when the JS has loaded (this is run with a 500ms delay upto a wait of 10s).
     * 
     * @param client
     *                  the client to use.
     * @param url
     *                  the URL to load.
     * @param delayTest
     *                  the test for the page to determined when deemed loaded (i.e.
     *                  to test that the JS has loaded).
     * @return the page tester.
     * @throws Exception on error.
     */
    public static PageTester $(WebDriver driver, String url, Predicate<WebDriver> delayTest) throws Exception {
        driver.get (url);
        LOOP: for (int i = 0; i < 20; i++) {
            synchronized (driver) {
                driver.wait (500);
            }
            if (delayTest.test (driver))
                break LOOP;
        }
        return $ (driver);
    }

    /**
     * The page being acted upon.
     */
    private IPage page;

    /**
     * Construct with an {@link IPage} as the source.
     * 
     * @param page the page to test against.
     */
    public PageTester(IPage page) {
        this.page = page;
    }

    /**
     * Prints the page.
     * 
     * @return this tester instance.
     */
    public PageTester print () {
        try {
            page.print ();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Prints the page.
     * 
     * @param file the file to print out to.
     * @return this tester instance.
     */
    public PageTester print (File file) throws IOException {
        page.print (file);
        return this;
    }

    /**
     * Prints the page.
     * 
     * @param file the file (by way of a supplier).
     * @return this tester instance.
     */
    public PageTester print(Supplier<File> file) throws IOException {
        page.print (file.get ());
        return this;
    }

    public PageTester sleep(int millis) {
        page.sleep (millis);
        return this;
    }

    @Override
    public <T extends IResolvable> PageTester with(T resolver, Consumer<T> with) {
        if (resolver != null) {
            resolver.resolve(page);
            if (with != null)
                with.accept (resolver);
        }
        return this;
    }

    public PageTester validate(IValidatable validatable) {
        if (validatable != null) {
            if (validatable instanceof IResolvable)
                ((IResolvable) validatable).resolve (page);
            validatable.validate();
        }
        return this;
    }

    public PageTester click(IClickable<?> clickable) {
        return click (clickable, 100);
    }

    public PageTester click(IClickable<?> clickable, long delay) {
        if (clickable instanceof IValidatable)
            validate((IValidatable) clickable);
        clickable.click (delay);
        return this;
    }

    public PageTester click(String testId) {
        return click (testId, 100);
    }
    
    public PageTester click(String testId, long delay) {
        INode el = page.selectById (testId);
        Assertions.assertNotNull(el, "Unable to find element to click with test-id=\"" + testId + "\"");
        try {
            el.click();
            if (delay > 0) {
                synchronized (page) {
                    page.wait(delay);
                }
            }
        } catch (Exception e) {
            Assertions.fail("Exception while clicking", e);
        }
        return this;
    }

    public PageTester assign(IAssignable<?> assignable, String value) {
        if (assignable instanceof IValidatable)
            validate((IValidatable) assignable);
        assignable.assign (value);
        return this;
    }

    public PageTester assign(String testId, String value) {
        INode el = page.selectById (testId);
        Assertions.assertNotNull (el, "Unable to find element to assign with test-id=\"" + testId + "\"");
        if (!el.assignValue (value))
            Assertions.fail ("Element cannot be assigned a value test-id=\"" + testId + "\"");
        return this;
    }

    /**
     * Locate and work with the modal of the given test ID.
     * 
     * @param testId the test ID of the modal.
     * @param with   to operate on the modal.
     * @return this page tester instance.
     */
    public PageTester modal(String testId, Consumer<ModalDialogTester> with) {
        return with (ModalDialogTester.$(testId), with);
    }

    /**
     * Locate and work with the modal of the given test ID. The modal will be a
     * subclass of `Modal` of the type specified.
     * 
     * @param testId   the test ID of the modal.
     * @param subClass the subclass (lower-case including enclosing class sperated
     *                 by an underscore, use {@code null} if no class test to be
     *                 performed).
     * @param with     to operate on the modal.
     * @return this page tester instance.
     */
    public PageTester modal(String testId, String subClass, Consumer<ModalDialogTester> with) {
        return with (ModalDialogTester.$(testId).subclass (subClass), with);
    }
}
