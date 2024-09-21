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
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

public class SeleniumPage implements IPage {

    public class Node implements INode {
        
        private WebElement el;

        public Node(WebElement el) {
            this.el = el;
        }

        @Override
        public String nodeName() {
            return el.getTagName ();
        }

        @Override
        public void click() throws Exception {
            fluent ().until (
                d -> ExpectedConditions.elementToBeClickable (el)
            );
            el.click ();
        }

        @Override
        public boolean assignValue(String value) {
            el.sendKeys (value);
            return true;
        }

        public String value() {
            return el.getAttribute ("value");
        }

        @Override
        public String attribute(String name) {
            return el.getAttribute (name);
        }

        @Override
        public String textContent() {
            return el.getText ();
        }

        @Override
        public INode parent() {
            List<WebElement> parent = el.findElements (By.xpath  ("./.."));
            if (parent.isEmpty ())
                return null;
            return new Node (parent.get (0));
        }

        @Override
        public Optional<INode> matchChild(Predicate<INode> selector) {
            for (WebElement child : el.findElements (By.xpath ("./*"))) {
                Node childNode = new Node (child);
                if (selector.test (childNode))
                    return Optional.of (childNode);
            }
            return Optional.empty();
        }

        @Override
        public List<INode> selectByXPath(String xPath) {
            List<INode> results = new ArrayList<> ();
            for (WebElement result : el.findElements(By.xpath (xPath))) {
                results.add (new Node (result));
            }
            return results;
        }

        @Override
        public INode selectByRef(String testRef, boolean bounded) {
            LOOP: for  (WebElement foundEl : el.findElements (By.xpath (".//*[@test-ref='" + testRef + "']"))) {
                // Check if we need to isolate to a component as delineated by the text-cpt
                // attribute.
                if (bounded) {
                    WebElement p = foundEl;
                    // We need to ensure that there is no element between the parent (exclusive)
                    // and the found node (inclusive) that has the test-cpt attribute. If so we 
                    // move onto the next matching element.
                    while ((p != null) && !p.equals (el)) {
                        if (p.getAttribute ("test-cpt") != null)
                            continue LOOP;
                        p = p.findElement (By.xpath ("./.."));
                    }
                }
                // Node has been found.
                return new Node (foundEl);
            }
            // No found element.
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public WebElement unwrap() {
            return el;
        }

        @Override
        public void print() {
            System.out.println (el.getAttribute("innerHTML"));
        }
        
    }

    private WebDriver client;

    private String testIdPrefix;

    /**
     * The number of retries to perform when selecting by ID.
     */
    private int retryCount = 10;

    /**
     * The delay between retries (when selecting by ID).
     */
    private long retryDelay = 200;

    public SeleniumPage(WebDriver driver) {
        this (driver, null);
    }

    public SeleniumPage(WebDriver driver, String testIdPrefix) {
        this.client = driver;
        this.testIdPrefix = testIdPrefix;
    }

    protected FluentWait<WebDriver> fluent() {
        return new FluentWait<> (client)
            .withTimeout (Duration.ofMillis (retryDelay * retryCount))
            .pollingEvery (Duration.ofMillis (retryDelay))
            .ignoring (ElementNotInteractableException.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveTestId(String testId) {
        if (testIdPrefix != null) {
            if ((testId == null) || testId.isBlank())
                return testIdPrefix;
            return testIdPrefix + "." + testId;
        }
        return (testId == null) ? "" : testId;
    }

    @Override
    public INode selectById(String testId) {
        // We build some resilence by attempting some retries. This is based on a
        // selection generally expecting a node to be found.
        fluent ().until (
            d -> (client.findElement (By.xpath ("//*[@test-id='" + resolveTestId (testId) + "']")) != null)
        );
        WebElement el = client.findElement (By.xpath ("//*[@test-id='" + resolveTestId (testId) + "']"));
        return (el == null) ? null : new Node (el);
    }

    /**
     * Prints out to the passed file.
     * <p>
     * If the file is {@code null} then the content will be directed to
     * {@code stdout}. Otherwise it will be filtered to include any referenced CSS,
     * clean up the HTML so it is compliant and remove all JS links. The fully
     * qualified file will written to {@code stdout} and can be opened in a browser
     * from there (for example, on MacOS by running {@code open <file>} from the
     * command line).
     * <p>
     * Note that a temporary file can be created with
     * {@code Files.createTempFile (null, ".html").toFile ()}.
     */
    @Override
    public void print(File file) throws IOException {
        if (file == null) {
            System.out.println (client.getPageSource ());
        } else {
            // Write out to file.
            System.out.println ("Output to " + file.getAbsolutePath ());
            FileUtils.write (file, client.getPageSource(), Charset.defaultCharset());
        }
    }

    @Override
    public INode root() {
        return new Node (client.findElement (By.tagName ("body")));
    }

    @Override
    public IPage wrap(String testIdPrefix) {
        // Note that we need to extend the passed test ID as it will also be scoped by
        // any other sope.
        return new SeleniumPage (client, resolveTestId (testIdPrefix));
    }

    @Override
    public IPage sleep(int millis) {
        synchronized (this) {
            try {
                wait (millis);
            } catch (InterruptedException e) {
                return null;
            }
        }
        return this;
    }
}
