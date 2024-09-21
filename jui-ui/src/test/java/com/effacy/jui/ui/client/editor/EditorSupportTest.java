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
package com.effacy.jui.ui.client.editor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.htmlunit.WebClient;
import org.htmlunit.corejs.javascript.NativeArray;
import org.htmlunit.html.DomAttr;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.DomText;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.host.dom.Range;
import org.htmlunit.javascript.host.dom.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class EditorSupportTest {

    @Test
    public void testNumberOfCharacters_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is a single line";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double count = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.numberOfCharacters(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (21, count);
        }
    }

    @Test
    public void testNumberOfCharacters_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Thi<span class='i j'>s i</span><span class='i'>s a</span> si<span class='j'>ng</span>le line";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double count = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.numberOfCharacters(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (21, count);
        }
    }

    @Test
    public void testNumberOfCharacters_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/>Line 2<br/>Line 3.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double count = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.numberOfCharacters(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (21, count);
        }
    }

    @Test
    public void testNumberOfCharacters_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/><span class='i'>Li</span>ne 2<br/>Li<span class='j'>ne 3.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double count = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.numberOfCharacters(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (21, count);
        }
    }

    @Test
    public void testPositionOfCursor_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/><span class='i'>Li</span>ne 2<br/>Li<span class='j'>ne 3.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double position;

            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                EditorSupport.positionOfCursor(el,range);
            """).getJavaScriptResult ();
            Assertions.assertEquals (0, position);
        }
    }

    @Test
    public void testPositionOfCursor_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/><span class='i'>Li</span>ne 2<br/>Li<span class='j'>ne 3.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double position;

            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[2].childNodes[0], 1);
                EditorSupport.positionOfCursor(el,range);
            """).getJavaScriptResult ();
            Assertions.assertEquals (8, position);
        }
    }

    @Test
    public void testPositionOfCursor_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/><span class='i'>Li</span>ne 2<br/>Li<span class='j'>ne 3.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double position;

            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[6].childNodes[0], 2);
                EditorSupport.positionOfCursor(el,range);
            """).getJavaScriptResult ();
            Assertions.assertEquals (18, position);
        }
    }

    @Test
    public void testLines_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/>Line 2<br/>Line 3.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray lines = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.lines(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (3, lines.getLength ());
            Assertions.assertEquals ("Line 1", lines.get (0).toString ());
            Assertions.assertEquals ("Line 2", lines.get (1).toString ());
            Assertions.assertEquals ("Line 3.", lines.get (2).toString ());
        }
    }

    @Test
    public void testLines_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/>Line 2<br/><br/>Line 3.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray lines = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.lines(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (4, lines.getLength ());
            Assertions.assertEquals ("Line 1", lines.get (0).toString ());
            Assertions.assertEquals ("Line 2", lines.get (1).toString ());
            Assertions.assertEquals ("", lines.get (2).toString ());
            Assertions.assertEquals ("Line 3.", lines.get (3).toString ());
        }
    }

    @Test
    public void testLines_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Line 1<br/>Line 2<br/>Line 3.<br/>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray lines = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.lines(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (4, lines.getLength ());
            Assertions.assertEquals ("Line 1", lines.get (0).toString ());
            Assertions.assertEquals ("Line 2", lines.get (1).toString ());
            Assertions.assertEquals ("Line 3.", lines.get (2).toString ());
            Assertions.assertEquals ("", lines.get (3).toString ());
        }
    }

    @Test
    public void testLines_v5() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<br/><br/>Line 1<br/>Line 2<br/>Line 3.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray lines = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.lines(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (5, lines.getLength ());
            Assertions.assertEquals ("", lines.get (0).toString ());
            Assertions.assertEquals ("", lines.get (1).toString ());
            Assertions.assertEquals ("Line 1", lines.get (2).toString ());
            Assertions.assertEquals ("Line 2", lines.get (3).toString ());
            Assertions.assertEquals ("Line 3.", lines.get (4).toString ());
        }
    }

    @Test
    public void testLines_v6() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "L<span class='i'>ine</span> 1<br/>Lin<span class='i'>e 2</span><br/>Line 3.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray lines = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport.lines(el);
            """).getJavaScriptResult ();
            Assertions.assertEquals (3, lines.getLength ());
            Assertions.assertEquals ("Line 1", lines.get (0).toString ());
            Assertions.assertEquals ("Line 2", lines.get (1).toString ());
            Assertions.assertEquals ("Line 3.", lines.get (2).toString ());
        }
    }

    /**
     * Issue: End container trailed to the last item and to the end of the item.
     */
    @Test
    public void testOffset_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is <span class='i'>some sample</span> text. And this is another line.<br/>And this is <span class='b s'>a final</span>line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double position;

            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el.childNodes[0], 4);
            """).getJavaScriptResult ();
            Assertions.assertEquals (4, position.intValue ());

            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el.childNodes[1].childNodes[0], 11);
            """).getJavaScriptResult ();
            Assertions.assertEquals (19, position.intValue ());

            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el.childNodes[5].childNodes[0], 3);
            """).getJavaScriptResult ();
            Assertions.assertEquals (67, position.intValue ());
        }
    }

    /**
     * Test case where the range specifies locations within the container and within
     * the span.
     */
    @Test
    public void testOffset_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='i'>This is some text.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Double position;

            // Container left.
            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el, 0);
            """).getJavaScriptResult ();
            Assertions.assertEquals (0, position.intValue ());

            // Container right.
            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el, 1);
            """).getJavaScriptResult ();
            Assertions.assertEquals (18, position.intValue ());

            // Span left.
            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el.childNodes[0], 0);
            """).getJavaScriptResult ();
            Assertions.assertEquals (0, position.intValue ());

            // Span right.
            position = (Double) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._offset(el, el.childNodes[0], 1);
            """).getJavaScriptResult ();
            Assertions.assertEquals (18, position.intValue ());
        }
    }

    @Test
    public void testApply_Unformatted_ReliefAll() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is some sample text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[0], 12);
                EditorSupport.apply (el, range, "b");
            """, "This is ", 8, "some", 4);
            
            Assertions.assertEquals("<div>This is <span class='b'>some</span> sample text.</div>", flatten (contentEl));
        }
    }

    @Test
    public void testApply_Unformatted_ReliefNone() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is some sample text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[0], 25);
                EditorSupport.apply (el, range, "b");
            """, "This is some sample text.", 0, "This is some sample text.", 25);
            
            Assertions.assertEquals("<div><span class='b'>This is some sample text.</span></div>", flatten (contentEl));
        }
    }

    @Test
    public void testApply_Unformatted_ReliefLeft() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is some sample text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 3);
                range.setEnd (el.childNodes[0], 25);
                EditorSupport.apply (el, range, "b");
            """, "Thi", 3, "s is some sample text.", 22);
            
            Assertions.assertEquals("<div>Thi<span class='b'>s is some sample text.</span></div>", flatten (contentEl));
        }
    }

    @Test
    public void testApply_Unformatted_ReliefRight() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is some sample text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[0], 22);
                EditorSupport.apply (el, range, "b");
            """);
            
            Assertions.assertEquals("<div><span class='b'>This is some sample te</span>xt.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-span variant: All content selected.
     */
    @Test
    public void testApply_SingleFormat_InnerIndentNone() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[1].childNodes[0], 11);
                EditorSupport.apply (el, range, "b");
            """);
            
            Assertions.assertEquals ("<div>This is <span class='i b'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-span variant: Indent from left side.
     */
    @Test
    public void testApply_SingleFormat_InnerIndentLeft() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 2);
                range.setEnd (el.childNodes[1].childNodes[0], 11);
                EditorSupport.apply (el, range, "b");
            """);
            
            Assertions.assertEquals ("<div>This is <span class='i'>so</span><span class='i b'>me sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-span variant: Indent from right side.
     */
    @Test
    public void testApply_SingleFormat_InnerIndentRight() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[1].childNodes[0], 10);
                EditorSupport.apply (el, range, "b");
            """);
            
            Assertions.assertEquals ("<div>This is <span class='i b'>some sampl</span><span class='i'>e</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-span variant: Indent from both sides.
     */
    @Test
    public void testApply_SingleFormat_InnerIndentAll() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 2);
                range.setEnd (el.childNodes[1].childNodes[0], 8);
                EditorSupport.apply (el, range, "b");
            """);
            
            Assertions.assertEquals ("<div>This is <span class='i'>so</span><span class='i b'>me sam</span><span class='i'>ple</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Wrap externally, but tighly.
     */
    @Test
    public void testApply_SingleFormat_InnerOutdentNone() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[2], 0);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i b'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Wrap externally, indent on left.
     */
    @Test
    public void testApply_SingleFormat_InnerOutdentLeft() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 5);
                range.setEnd (el.childNodes[2], 0);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This <span class='b'>is </span><span class='i b'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Wrap externally, indent on right.
     */
    @Test
    public void testApply_SingleFormat_InnerOutdentRight() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[2], 3);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i b'>some sample</span><span class='b'> te</span>xt.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Wrap externally, indent both sides.
     */
    @Test
    public void testApply_SingleFormat_InnerOutdentAll() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[2], 3);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>Th<span class='b'>is is </span><span class='i b'>some sample</span><span class='b'> te</span>xt.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket left side with relief.
     */
    @Test
    public void testApply_SingleFormat_LeftReliefAll() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[1].childNodes[0], 3);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>Th<span class='b'>is is </span><span class='i b'>som</span><span class='i'>e sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket left side with relief on left.
     */
    @Test
    public void testApply_SingleFormat_LeftReliefLeft() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[1].childNodes[0], 0);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>Th<span class='b'>is is </span><span class='i'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket left side with relief on right.
     */
    @Test
    public void testApply_SingleFormat_LeftReliefRight() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[1].childNodes[0], 3);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i b'>som</span><span class='i'>e sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket left side with no relief.
     */
    @Test
    public void testApply_SingleFormat_LeftReliefNone() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[1].childNodes[0], 0);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket right side with relief.
     */
    @Test
    public void testApply_SingleFormat_RightReliefAll() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 6);
                range.setEnd (el.childNodes[2], 3);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some s</span><span class='i b'>ample</span><span class='b'> te</span>xt.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket left side with relief on left.
     */
    @Test
    public void testApply_SingleFormat_RightReliefLeft() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 6);
                range.setEnd (el.childNodes[2], 0);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some s</span><span class='i b'>ample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket right side with relief.
     */
    @Test
    public void testApply_SingleFormat_RightReliefRight() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 11);
                range.setEnd (el.childNodes[2], 3);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span><span class='b'> te</span>xt.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Bracket right side with no relief.
     */
    @Test
    public void testApply_SingleFormat_RightReliefNone() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 11);
                range.setEnd (el.childNodes[2], 0);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Disjoint left side, no relief.
     */
    @Test
    public void testApply_SingleFormat_DisjointLeftFull() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[0], 8);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div><span class='b'>This is </span><span class='i'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Disjoint left side, with relief.
     */
    @Test
    public void testApply_SingleFormat_DisjointLeftIndent() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 1);
                range.setEnd (el.childNodes[0], 7);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>T<span class='b'>his is</span> <span class='i'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Disjoint right side, no relief.
     */
    @Test
    public void testApply_SingleFormat_DisjointRightFull() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[2], 0);
                range.setEnd (el.childNodes[2], 6);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span><span class='b'> text.</span></div>", flatten (contentEl));
        }
    }

    /**
     * Sub-sub variant: Disjoint right side, with relief.
     */
    @Test
    public void testApply_SingleFormat_DisjointRightReflief() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[2], 2);
                range.setEnd (el.childNodes[2], 4);
                EditorSupport.apply (el, range, "b");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> t<span class='b'>ex</span>t.</div>", flatten (contentEl));
        }
    }

    /**
     * Application of the same.
     */
    @Test
    public void testApply_SingleFormat_SameInternal() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 2);
                range.setEnd (el.childNodes[1].childNodes[0], 4);
                EditorSupport.apply (el, range, "i");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Application of the same.
     */
    @Test
    public void testApply_SingleFormat_SameBracketLeft() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 6);
                range.setEnd (el.childNodes[1].childNodes[0], 4);
                EditorSupport.apply (el, range, "i");
            """);

            Assertions.assertEquals ("<div>This i<span class='i'>s some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Application of the same.
     */
    @Test
    public void testApply_SingleFormat_SameBracketRight() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 3);
                range.setEnd (el.childNodes[2], 5);
                EditorSupport.apply (el, range, "i");
            """);

            Assertions.assertEquals ("<div>This is <span class='i'>some sample text</span>.</div>", flatten (contentEl));
        }
    }

    /**
     * Application of the same.
     */
    @Test
    public void testApply_SingleFormat_SameAdjacent() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 5);
                range.setEnd (el.childNodes[0], 8);
                EditorSupport.apply (el, range, "i");
            """);

            Assertions.assertEquals ("<div>This <span class='i'>is some sample</span> text.</div>", flatten (contentEl));
        }
    }



    /**
     * Issue: End container trailed to the last item and to the end of the item.
     */
    @Test
    public void testApply_SingleFormat_Issue001() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest_Bracket002.html"));
            DomElement contentEl = page.getElementById ("content");
            
            Assertions.assertEquals ("<div>This is <span class='i'>some sample</span> text.</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[1].childNodes[0], 11);
                EditorSupport.apply (el, range, "b");
            """, "This is ", 8, "some sample", 11);

            Assertions.assertEquals ("<div>This is <span class='i b'>some sample</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Complex full bracketed.
     */
    @Test
    public void testApply_Bracket_Full() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is <span class='i'>some</span> <span class='b'>sample</span> text.<br/>This is a new line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[4], 6);
                EditorSupport.apply (el, range, "c");
            """);
            
            Assertions.assertEquals ("<div><span class='c'>This is </span><span class='i c'>some</span><span class='c'> </span><span class='b c'>sample</span><span class='c'> text.</span><br/>This is a new line.</div>", flatten (contentEl));
        }
    }

    /**
     * Complex partial bracketed.
     */
    @Test
    public void testApply_Bracket_Partial() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is <span class='i'>some</span> <span class='b'>sample</span> text.<br/>This is a new line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[3].childNodes[0], 3);
                EditorSupport.apply (el, range, "c");
            """);
            
            Assertions.assertEquals ("<div><span class='c'>This is </span><span class='i c'>some</span><span class='c'> </span><span class='b c'>sam</span><span class='b'>ple</span> text.<br/>This is a new line.</div>", flatten (contentEl));
        }
    }

    /**
     * Bracketing a line break when there is relief around the break.
     */
    @Test
    public void testApply_LineBreak_Bracketed() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is <span class='i'>some</span> <span class='b'>sample</span> text.<br/>This is a new line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[4], 3);
                range.setEnd (el.childNodes[6], 2);
                EditorSupport.apply (el, range, "c");
            """);
            
            Assertions.assertEquals ("<div>This is <span class='i'>some</span> <span class='b'>sample</span> te<span class='c'>xt.</span><br/><span class='c'>Th</span>is is a new line.</div>", flatten (contentEl));
        }
    }
    
    /**
     * Bracketing a line break when the break is selected directly.
     */
    @Test
    public void testApply_LineBreak_Selected() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is <span class='i'>some</span> <span class='b'>sample</span> text.<br/>This is a new line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[5], 0);
                range.setEnd (el.childNodes[6], 2);
                EditorSupport.apply (el, range, "c");
            """);
            
            Assertions.assertEquals ("<div>This is <span class='i'>some</span> <span class='b'>sample</span> text.<br/><span class='c'>Th</span>is is a new line.</div>", flatten (contentEl));
        }
    }

    /**
     * Range includes a point between two line breaks.
     */
    @Test
    public void testApply_LineBreak_Sequential001() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is a line.<br/><br/><br/>This is another line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 2);
                range.setEnd (el.childNodes[4], 2);
                EditorSupport.apply (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is a line.<br/><br/><br/><span class='c'>Th</span>is is another line.</div>", flatten (contentEl));
        }
    }

    /**
     * Range includes a point between two line breaks.
     */
    @Test
    public void testApply_LineBreak_Sequential002() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is a line.<br/><br/><br/>This is another line.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Pick two BR's as the endpoints. The expectation is that nothing will be done.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 2);
                range.setEnd (el, 3);
                EditorSupport.apply (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is a line.<br/><br/><br/>This is another line.</div>", flatten (contentEl));
        }
    }

    /**
     * Tests when there is an embedded formatting.
     */
    @Test
    public void testClear_SingleFormat_Full_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select end of first text node and begining of last.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 5);
                range.setEnd (el.childNodes[2], 0);
                EditorSupport.clear (el, range, "c");
            """, "This is some text.", 5, "This is some text.", 12);

            Assertions.assertEquals ("<div>This is some text.</div>", flatten (contentEl));
        }
    }

    /**
     * Tests when formatting is applied to all the content and range is based on the
     * span as the container not a text node.
     */
    @Test
    public void testClear_SingleFormat_Full_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='c'>This is some text.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // [0]{0} - [0]{2} (note deliberately going beyond the end in the second
            // specification).
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[0], 2);
                EditorSupport.clear (el, range, "c");
            """, "This is some text.", 0, "This is some text.", 18);

            Assertions.assertEquals ("<div>This is some text.</div>", flatten (contentEl));
        }
    }

    /**
     * Tests when formatting is applied to all the content and range is based on the
     * text node.
     */
    @Test
    public void testClear_SingleFormat_Full_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='c'>This is some text.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // [0][0]{0} - [0][0]{18}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0].childNodes[0], 0);
                range.setEnd (el.childNodes[0].childNodes[0], 18);
                EditorSupport.clear (el, range, "c");
            """, "This is some text.", 0, "This is some text.", 18);

            Assertions.assertEquals ("<div>This is some text.</div>", flatten (contentEl));
        }
    }

    /**
     * Tests when formatting is applied to all the content and range is external.
     */
    @Test
    public void testClear_SingleFormat_Full_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='c'>This is some text.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // {0} - {1}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 0);
                range.setEnd (el, 1);
                EditorSupport.clear (el, range, "c");
            """, "This is some text.", 0, "This is some text.", 18);

            Assertions.assertEquals ("<div>This is some text.</div>", flatten (contentEl));
        }
    }


    @Test
    public void testClear_SingleFormat_Internal01() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select end of first text node and begining of last.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 1);
                range.setEnd (el.childNodes[1].childNodes[0], 6);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This <span class='c'>i</span>s som<span class='c'>e</span> text.</div>", flatten (contentEl));
        }
    }


    @Test
    public void testClear_SingleFormat_Internal02() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select end of first text node and begining of last.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 1);
                range.setEnd (el.childNodes[1].childNodes[0], 7);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This <span class='c'>i</span>s some text.</div>", flatten (contentEl));
        }
    }


    @Test
    public void testClear_SingleFormat_Internal03() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select end of first text node and begining of last.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[1].childNodes[0], 6);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is som<span class='c'>e</span> text.</div>", flatten (contentEl));
        }
    }


    @Test
    public void testClear_SingleFormat_Bracketed() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select end of first text node and begining of last.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[2], 4);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is some text.</div>", flatten (contentEl));
        }
    }


    @Test
    public void testClear_SingleFormat_Staggered_Right() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select middle of span and middle of last text node.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 2);
                range.setEnd (el.childNodes[2], 4);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This <span class='c'>is</span> some text.</div>", flatten (contentEl));
        }
    }


    @Test
    public void testClear_SingleFormat_Staggered_Left() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select middle of span and middle of last text node.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 1);
                range.setEnd (el.childNodes[1].childNodes[0], 2);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is<span class='c'> some</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Tests that text nodes are properly merged after a clear operation.
     */
    @Test
    public void testClear_SingleFormat_TextNodesMerged() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select middle of span and middle of last text node.
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[1].childNodes[0], 7);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is some text.</div>", flatten (contentEl));

            // TEST: There should be one node under the DIV.
            Assertions.assertEquals (1, contentEl.getChildNodes().getLength());
        }
    }

    @Test
    public void testClear_MultipleFormat_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text. This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0]{1} - [1][0]{2}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 1);
                range.setEnd (el.childNodes[1].childNodes[0], 2);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is<span class='c'> some</span> text. This <span class='c'>is some</span> text.</div>", flatten (contentEl));
        }
    }

    @Test
    public void testClear_MultipleFormat_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text. This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0]{1} - [3][0]{2}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 1);
                range.setEnd (el.childNodes[3].childNodes[0], 2);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is some text. This is<span class='c'> some</span> text.</div>", flatten (contentEl));
        }
    }

    @Test
    public void testClear_MultipleFormat_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text. This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [1][0]{0} - [3][0]{7}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[3].childNodes[0], 7);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is some text. This is some text.</div>", flatten (contentEl));
        }
    }

    @Test
    public void testClear_MultipleFormat_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is some</span> text. This <span class='c'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0]{5} - [4]{0}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 5);
                range.setEnd (el.childNodes[4], 0);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This is some text. This is some text.</div>", flatten (contentEl));
        }
    }

    @Test
    public void testClear_MultipleClasses_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c i'>is some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0]{2} - [1][0]{2}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[1].childNodes[0], 2);
                EditorSupport.clear (el, range, "c");
            """);

            Assertions.assertEquals ("<div>This <span class='i'>is</span><span class='c i'> some</span> text.</div>", flatten (contentEl));
        }
    }

    @Test
    public void testClear_MultipleClasses_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c i'>is some</span> text. <span class='c'>Some</span> more <span class='c j'>content</span>.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0]{2} - [6]{2}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[6], 1);
                EditorSupport.clear (el, range, "c");
            """, "This ", 2, ".", 1);

            Assertions.assertEquals ("<div>This <span class='i'>is some</span> text. Some more <span class='j'>content</span>.</div>", flatten (contentEl));
        }
    }

    /**
     * Common formatting of two classes across all content, clearing one class.
     */
    @Test
    public void testClear_MultipleClasses_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='c i'>This is some text.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0][0]{0} - [0][0]{18}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0].childNodes[0], 0);
                range.setEnd (el.childNodes[0].childNodes[0], 18);
                EditorSupport.clear (el, range, "c");
            """, "This is some text.", 0, "This is some text.", 18);

            Assertions.assertEquals ("<div><span class='i'>This is some text.</span></div>", flatten (contentEl));
        }
    }

    /**
     * Case where range is at the end of a formatted block (should not remove the
     * formatting).
     */
    @Test
    public void testClear_MultipleClasses_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c'>is </span><span class='c i'>some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0][0]{0} - [0][0]{18}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 3);
                range.setEnd (el.childNodes[2].childNodes[0], 4);
                EditorSupport.clear (el, range, "c");
            """, "is ", 3, "some", 4);

            Assertions.assertEquals ("<div>This <span class='c'>is </span><span class='i'>some</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Case where range is at the start of a formatted block (should not remove the
     * formatting).
     */
    @Test
    public void testClear_MultipleClasses_v5() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c i'>is </span><span class='c'>some</span> text.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Select [0][0]{0} - [0][0]{18}
            assertScript (page, """
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[2].childNodes[0], 0);
                EditorSupport.clear (el, range, "c");
            """);
            //""", "is ", 3, "some", 4);

            Assertions.assertEquals ("<div>This <span class='i'>is </span><span class='c'>some</span> text.</div>", flatten (contentEl));
        }
    }

    /**
     * Cases where there are two adjacent formatting spans.
     */
    @Test
    public void testStyles_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c i'>is some</span><span class='i'> text</span>.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray result;

            // Select [1][0]{0} - [1][0]{7}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[1].childNodes[0], 7);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "c", "i");

            // Select [1][0]{0} - [2][0]{4}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[2].childNodes[0], 4);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "i");

            // Select [0]{2} - [2][0]{4}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[2].childNodes[0], 4);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result);
        }
    }

    /**
     * Same as {@link #testStyles_v1()} but where a line break is included between
     * the formatting spans.
     */
    @Test
    public void testStyles_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This <span class='c i'>is some</span><br/><span class='i'> text</span>.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray result;

            // Select [1][0]{0} - [1][0]{7}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[1].childNodes[0], 7);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "c", "i");

            // Select [1][0]{0} - [3][0]{4}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 0);
                range.setEnd (el.childNodes[3].childNodes[0], 4);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "i");

            // Select [0]{2} - [3][0]{4}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 2);
                range.setEnd (el.childNodes[3].childNodes[0], 4);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result);
        }
    }

    /**
     * Range is determined be the right endpoint of the prior text node and left
     * endpoint of the next text node.
     */
    @Test
    public void testStyles_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "This is <span class='edt-b'>some</span> content.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray result;

            // Select [0]{8} - [1][0]{7}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 8);
                range.setEnd (el.childNodes[1].childNodes[0], 4);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "edt-b");
        }
    }

    /**
     * Range is determined from the endpoints of the text node under a span.
     */
    @Test
    public void testStyles_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='edt-b'>This is some content</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray result;

            // Select [0]{0} - [0]{20}
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                range.setEnd (el.childNodes[0], 20);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "edt-b");
        }
    }

    /**
     * Range is determined relative to the container (case of the extrema).
     */
    @Test
    public void testStyles_v5() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='edt-b'>This is some content</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray result;

            // Node selection is off the container.
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 0);
                range.setEnd (el, 1);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "edt-b");
        }
    }

    /**
     * Range is determined relative to the container (case of the internal references).
     */
    @Test
    public void testStyles_v6() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            NativeArray result;

            // Node selection is off the container.
            result = (NativeArray) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 1);
                range.setEnd (el, 2);
                EditorSupport.styles (el, range);
            """).getJavaScriptResult ();

            assertStyles (result, "edt-b", "edt-c");
        }
    }

    @Test
    public void testIsCursorAtStart_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 0);
                EditorSupport.isCursorAtStart (el, range);
            """).getJavaScriptResult ();

            Assertions.assertTrue (result);
        }
    }

    @Test
    public void testIsCursorAtStart_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 1);
                EditorSupport.isCursorAtStart (el, range);
            """).getJavaScriptResult ();

            Assertions.assertFalse (result);
        }
    }

    @Test
    public void testIsCursorAtStart_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 1);
                EditorSupport.isCursorAtStart (el, range);
            """).getJavaScriptResult ();

            Assertions.assertFalse (result);
        }
    }

    @Test
    public void testIsCursorAtStart_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0], 0);
                EditorSupport.isCursorAtStart (el, range);
            """).getJavaScriptResult ();

            Assertions.assertTrue (result);
        }
    }

    @Test
    public void testIsCursorAtStart_v5() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0].childNodes[0], 0);
                EditorSupport.isCursorAtStart (el, range);
            """).getJavaScriptResult ();

            Assertions.assertTrue (result);
        }
    }

    @Test
    public void testIsCursorAtStart_v6() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[0].childNodes[0], 1);
                EditorSupport.isCursorAtStart (el, range);
            """).getJavaScriptResult ();

            Assertions.assertFalse (result);
        }
    }

    @Test
    public void testIsCursorAtEnd_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 3);
                EditorSupport.isCursorAtEnd (el, range);
            """).getJavaScriptResult ();

            Assertions.assertTrue (result);
        }
    }

    @Test
    public void testIsCursorAtEnd_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el, 2);
                EditorSupport.isCursorAtEnd (el, range);
            """).getJavaScriptResult ();

            Assertions.assertFalse (result);
        }
    }

    @Test
    public void testIsCursorAtEnd_v3() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[2], 5);
                EditorSupport.isCursorAtEnd (el, range);
            """).getJavaScriptResult ();

            Assertions.assertTrue (result);
        }
    }

    @Test
    public void testIsCursorAtEnd_v4() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span> End.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[2], 4);
                EditorSupport.isCursorAtEnd (el, range);
            """).getJavaScriptResult ();

            Assertions.assertFalse (result);
        }
    }

    @Test
    public void testIsCursorAtEnd_v5() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 20);
                EditorSupport.isCursorAtEnd (el, range);
            """).getJavaScriptResult ();

            Assertions.assertFalse (result);
        }
    }

    @Test
    public void testIsCursorAtEnd_v6() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "Start. <span class='edt-b edt-c'>This is some content.</span>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            // Node selection is off the container.
            boolean result = (Boolean) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                var range = new Range();
                range.setStart (el.childNodes[1].childNodes[0], 21);
                EditorSupport.isCursorAtEnd (el, range);
            """).getJavaScriptResult ();

            Assertions.assertTrue (result);
        }
    }

    /**
     * Position at various points in a formatted block of text (no line breaks).
     */
    @Test
    public void testPositionCursorAt_v1() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "AAA <span class='edt-b edt-c'>BBB</span> CCC.";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Range result;

            // Node selection is off the container.
            result = (Range) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._positionCursorAt (el, 2);
            """).getJavaScriptResult ();

            Assertions.assertEquals (2, result.getStartOffset());
            Assertions.assertEquals ("AAA ", ((Text) result.getStartContainer()).getTextContent());

            result = (Range) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._positionCursorAt (el, 5);
            """).getJavaScriptResult ();

            Assertions.assertEquals (1, result.getStartOffset());
            Assertions.assertEquals ("BBB", ((Text) result.getStartContainer()).getTextContent());

            result = (Range) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._positionCursorAt (el, 10);
            """).getJavaScriptResult ();

            Assertions.assertEquals (3, result.getStartOffset());
            Assertions.assertEquals (" CCC.", ((Text) result.getStartContainer()).getTextContent());
        }
    }

    /**
     * Position at end of block where block contains a line break.
     * <p>
     * Motivating issue: "node not defined" in _positionAtCursor at test for line
     * break.
     */
    @Test
    public void testPositionCursorAt_v2() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "AAA<br/>bbBB";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow (load ("EditorSupportTest.html", content));
            DomElement contentEl = page.getElementById ("content");
            Assertions.assertEquals ("<div>" + content + "</div>", flatten (contentEl));

            Range result;

            // Node selection is off the container.
            result = (Range) page.executeJavaScript ("""
                var el = document.getElementById ("content");
                EditorSupport._positionCursorAt (el, 8);
            """).getJavaScriptResult ();

            Assertions.assertEquals (4, result.getStartOffset());
            Assertions.assertEquals ("bbBB", ((Text) result.getStartContainer()).getTextContent());
        }
    }

    /************************************************************************
     * Assertions.
     ************************************************************************/

    public void assertStyles(NativeArray data, String... styles) {
        Assertions.assertEquals (styles.length, data.getLength ());
        Set<String> stylesFromData = new HashSet<>();
        for (Object datum : data)
            stylesFromData.add ((String) datum);
        for (String style : styles)
            Assertions.assertTrue (stylesFromData.contains(style), () -> "Excpected to find stle \"" + style + "\"");
    }

    public void assertScript(HtmlPage page, String script) {
        page.executeJavaScript (script);
    }

    public void assertScript(HtmlPage page, String script, String startContainer, int startOffset, String endContainer, int endOffset) {
        Range range = (Range) page.executeJavaScript (script).getJavaScriptResult ();

        Assertions.assertEquals (startContainer, ((Text) range.getStartContainer()).getTextContent());
        Assertions.assertEquals (startOffset, range.getStartOffset());
        Assertions.assertEquals (endContainer, ((Text) range.getEndContainer()).getTextContent());
        Assertions.assertEquals (endOffset, range.getEndOffset());
    }

    public String flatten(DomNode node) {
        StringBuffer sb = new StringBuffer();
        flatten (node, sb);
        return sb.toString();
    }

    void flatten(DomNode node, StringBuffer sb) {
        if (node instanceof DomText) {
            sb.append(node.getTextContent());
        } else if (node.getChildNodes().getLength() == 0) {
            sb.append("<");
            sb.append(node.getNodeName());
            DomAttr attr = (DomAttr) node.getAttributes ().getNamedItem ("class");
            if (attr != null) {
                sb.append (" class=\"");
                sb.append (attr.getValue ());
                sb.append ("\"");
            }
            sb.append("/>");
        } else {
            sb.append("<");
            sb.append(node.getNodeName());
            DomAttr attr = (DomAttr) node.getAttributes ().getNamedItem ("class");
            if (attr != null) {
                sb.append (" class='");
                sb.append (attr.getValue ());
                sb.append ("'");
            }
            sb.append(">");
            node.getChildNodes().forEach (child -> flatten(child, sb));
            sb.append("</");
            sb.append(node.getNodeName());
            sb.append(">");
        }
    }

    

    /************************************************************************
     * Loading up resources.
     ************************************************************************/

    /**
     * Loads an HTML file from the given resource (local to this package) and
     * injects the JS if needed. It also inserts the given content.
     */
    protected String load(String file, String content) throws Exception {
        String str = load (file, false);
        str = str.replace("{CONTENT}", content);
        return str;
    }

    /**
     * Loads an HTML file from the given resource (local to this package) and
     * injects the JS if needed.
     */
    protected String load(String file) throws Exception {
        return load (file, false);
    }

    /**
     * Loads an HTML file from the given resource (local to this package) and
     * injects the JS if needed.
     */
    protected String load(String file, boolean print) throws Exception {
        try (InputStream is = EditorSupportTest.class.getResourceAsStream(file)) {
            String str = IOUtils.toString(is, StandardCharsets.UTF_8);
            if (str.contains("{EDITORJS}"))
                str = str.replace("{EDITORJS}", loadEditorJs());
            if (print)
                System.out.println (str);
            return str;
        }
    }

    /**
     * Loads the editor JS file contents.
     */
    protected String loadEditorJs() throws Exception {
        try (InputStream is = EditorSupportTest.class.getResourceAsStream("/com/effacy/jui/ui/public/editor.js")) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
