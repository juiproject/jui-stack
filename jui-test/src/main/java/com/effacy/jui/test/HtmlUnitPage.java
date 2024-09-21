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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class HtmlUnitPage implements IPage {

    public static class Node implements INode {
        
        private DomElement el;

        public Node(DomElement el) {
            this.el = el;
        }

        @Override
        public String nodeName() {
            return el.getNodeName();
        }

        @Override
        public void click() throws Exception {
            // Here we force avoidance of visibility checking.
            el.click (false, false, false, true, true, true, false);
        }

        @Override
        public boolean assignValue(String value) {
            if (el instanceof HtmlTextInput) {
                ((HtmlTextInput) el).setValue (value);
                return true;
            }
            return false;
        }

        public String value() {
            if (el instanceof HtmlTextInput)
                return ((HtmlTextInput) el).getValue();
            return null; 
        }

        @Override
        public String attribute(String name) {
            return el.getAttribute (name);
        }

        @Override
        public String textContent() {
            return el.getTextContent();
        }

        @Override
        public INode parent() {
            DomNode parent = el.getParentNode();
            return !(parent instanceof DomElement) ? null : new Node ((DomElement) parent);
        }

        @Override
        public Optional<INode> matchChild(Predicate<INode> selector) {
            for (DomElement child : el.getChildElements()) {
                Node childNode = new Node (child);
                if (selector.test(childNode))
                    return Optional.of (childNode);
            }
            return Optional.empty();
        }

        @Override
        public List<INode> selectByXPath(String xPath) {
            List<INode> results = new ArrayList<> ();
            for (Object result : el.getByXPath (xPath)) {
                if (result instanceof DomElement)
                    results.add (new Node ((DomElement) result));
            }
            return results;
        }

        @Override
        public INode selectByRef(String testRef, boolean bounded) {
            LOOP: for  (Object foundObj : el.getByXPath (".//*[@test-ref='" + testRef + "']")) {
                if (!(foundObj instanceof DomElement))
                    continue LOOP;
                DomElement foundEl = (DomElement) foundObj;
                // Check if we need to isolate to a component as delineated by the text-cpt
                // attribute.
                if (bounded) {
                    DomElement p = foundEl;
                    // We need to ensure that there is no element between the parent (exclusive)
                    // and the found node (inclusive) that has the test-cpt attribute. If so we 
                    // move onto the next matching element.
                    while ((p != null) && !p.equals (el)) {
                        if (p.getAttribute ("test-cpt") != null)
                            continue LOOP;
                        p = (DomElement) p.getParentNode ();
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
        public DomElement unwrap() {
            return el;
        }

        @Override
        public void print() {
            // Not supported.
        }
        
    }

    private WebClient client;
    private HtmlPage page;
    private DomElement el;
    private String testIdPrefix;

    /**
     * The number of retries to perform when selecting by ID.
     */
    private int retryCount = 10;

    /**
     * The delay between retries (when selecting by ID).
     */
    private long retryDelay = 200;

    public HtmlUnitPage(HtmlPage page) {
        this (page, null);
    }

    public HtmlUnitPage(HtmlPage page, String testIdPrefix) {
        this.client = page.getWebClient ();
        this.page = page;
        this.testIdPrefix = testIdPrefix;
        this.el =  page.getDocumentElement();
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
        int retries = 0;
        while (true) {
            List<Object> matches = this.el.getByXPath ("//*[@test-id='" + resolveTestId (testId) + "']");
            if (!matches.isEmpty () && (matches.get (0) instanceof DomElement))
                return new Node ((DomElement) matches.get (0));
            if (retries++ >= retryCount)
                return null;
            synchronized (this) {
                try {
                    wait (retryDelay);
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }
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
            System.out.println (page.asXml ());
        } else {
            HtmlPage replica = page.cloneNode (true);

            // Remove JS script nodes.
            for (Object obj : replica.getByXPath ("//script[@src]")) {
                if (!(obj instanceof HtmlElement))
                    continue;
                HtmlElement el = (HtmlElement) obj;
                el.remove ();
            }

            // Inline style references.
            for (Object obj : replica.getByXPath ("//link[@href]")) {
                if (!(obj instanceof HtmlElement))
                    continue;
                HtmlElement el = (HtmlElement) obj;
                String resourceSourcePath = el.getAttribute ("href");
                if ((resourceSourcePath != null) && !resourceSourcePath.isBlank ()) {
                    URL resourceRemoteLink = page.getFullyQualifiedUrl (resourceSourcePath);
                    String content = client.getPage(new WebRequest (resourceRemoteLink)).getWebResponse ().getContentAsString();
                    DomElement styleEl = replica.createElement ("style");
                    styleEl.setTextContent (content);
                    el.getParentNode ().appendChild (styleEl);
                }
                el.remove ();
            }

            // Here we need to tidy some things up to make it easier to read and load into a
            // browser.
            String content = replica.asXml ();
            content = content.replace ("}.", "}\n.");
            content = content.replace ("}@font", "}\n@font");
            content = content.replace ("}@keyframes", "}\n@keyframes");
            content = content.replace ("&gt;", ">");
            content = content.replaceAll ("<em(.*)/>", "<em$1></em>");

            // Write out to file.
            System.out.println ("Output to " + file.getAbsolutePath ());
            FileUtils.write (file, content, page.getCharset ());
        }
    }

    @Override
    public INode root() {
        return new Node(el);
    }

    @Override
    public IPage wrap(String testIdPrefix) {
        // Note that we need to extend the passed test ID as it will also be scoped by
        // any other sope.
        return new HtmlUnitPage (page, resolveTestId (testIdPrefix));
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

    protected DomElement find(DomElement base, String testId) {
        if (base == null)
            return null;
        String actualTestId = base.getAttribute ("test-id");
        if (actualTestId == null)
            return null;
        if (!actualTestId.equalsIgnoreCase (testId)) {
            for (DomElement child : base.getChildElements()) {
                DomElement match = find(child, testId);
                if (match != null)
                    return match;
            }
            return null;
        }
        return base;
    }
    
}
