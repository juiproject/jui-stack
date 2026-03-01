package com.effacy.jui.text.ui.editor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.htmlunit.WebClient;
import org.htmlunit.corejs.javascript.NativeArray;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EditorSupportTest {

    /************************************************************************
     * charCount.
     ************************************************************************/

    @Test
    public void testCharCount_plainText() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            Assertions.assertEquals(11, count);
        }
    }

    @Test
    public void testCharCount_withFormatting() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">He<span class='b'>llo</span> world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            Assertions.assertEquals(11, count);
        }
    }

    @Test
    public void testCharCount_withLineBreaks() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Line 1<br/>Line 2</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            Assertions.assertEquals(13, count);
        }
    }

    @Test
    public void testCharCount_withVariable() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hi <span contenteditable=\"false\">Name</span> there</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            Assertions.assertEquals(13, count);
        }
    }

    @Test
    public void testCharCount_empty() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\"></div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            Assertions.assertEquals(0, count);
        }
    }

    @Test
    public void testCharCount_multipleLineBreaks() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">A<br/>B<br/>C</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            // "A" + BR + "B" + BR + "C" = 1 + 1 + 1 + 1 + 1 = 5
            Assertions.assertEquals(5, count);
        }
    }

    @Test
    public void testCharCount_formattingAndLineBreaks() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">He<span class='b'>llo</span><br/><span class='i'>world</span></div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double count = (Double) page.executeJavaScript("""
                EditorSupport2.charCount(document.querySelector('[data-block-index="0"]'));
            """).getJavaScriptResult();
            // "Hello" + BR + "world" = 5 + 1 + 5 = 11
            Assertions.assertEquals(11, count);
        }
    }

    /************************************************************************
     * Internal helpers: _lines.
     ************************************************************************/

    @Test
    public void testLines_plainText() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                EditorSupport2._lines(document.querySelector('[data-block-index="0"]'), null);
            """).getJavaScriptResult();
            Assertions.assertEquals(1, result.getLength());
            assertLine("Hello world", result, 0);
        }
    }

    @Test
    public void testLines_withBR() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Line 1<br/>Line 2</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                EditorSupport2._lines(document.querySelector('[data-block-index="0"]'), null);
            """).getJavaScriptResult();
            Assertions.assertEquals(2, result.getLength());
            assertLine("Line 1", result, 0);
            assertLine("Line 2", result, 1);
        }
    }

    @Test
    public void testLines_withFormatting() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">He<span class='b'>llo</span> world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                EditorSupport2._lines(document.querySelector('[data-block-index="0"]'), null);
            """).getJavaScriptResult();
            Assertions.assertEquals(1, result.getLength());
            assertLine("Hello world", result, 0);
        }
    }

    @Test
    public void testLines_multipleBR() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">A<br/>B<br/>C</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                EditorSupport2._lines(document.querySelector('[data-block-index="0"]'), null);
            """).getJavaScriptResult();
            Assertions.assertEquals(3, result.getLength());
            assertLine("A", result, 0);
            assertLine("B", result, 1);
            assertLine("C", result, 2);
        }
    }

    @Test
    public void testLines_withStopNode() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello <span id=\"stop\">world</span> more</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var block = document.querySelector('[data-block-index="0"]');
                var stop = document.getElementById('stop');
                EditorSupport2._lines(block, stop);
            """).getJavaScriptResult();
            Assertions.assertEquals(1, result.getLength());
            assertLine("Hello ", result, 0);
        }
    }

    /************************************************************************
     * Event helpers.
     ************************************************************************/

    @Test
    public void testGetInputType() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            String result = (String) page.executeJavaScript("""
                EditorSupport2.getInputType({inputType: 'insertText'});
            """).getJavaScriptResult();
            Assertions.assertEquals("insertText", result);
        }
    }

    @Test
    public void testGetInputType_deleteContentBackward() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            String result = (String) page.executeJavaScript("""
                EditorSupport2.getInputType({inputType: 'deleteContentBackward'});
            """).getJavaScriptResult();
            Assertions.assertEquals("deleteContentBackward", result);
        }
    }

    @Test
    public void testGetInputType_null() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Object result = page.executeJavaScript("""
                EditorSupport2.getInputType(null);
            """).getJavaScriptResult();
            Assertions.assertNull(result);
        }
    }

    @Test
    public void testGetInputData() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            String result = (String) page.executeJavaScript("""
                EditorSupport2.getInputData({data: 'a'});
            """).getJavaScriptResult();
            Assertions.assertEquals("a", result);
        }
    }

    @Test
    public void testGetInputData_null() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Object result = page.executeJavaScript("""
                EditorSupport2.getInputData(null);
            """).getJavaScriptResult();
            Assertions.assertNull(result);
        }
    }

    @Test
    public void testGetClipboardText() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            String result = (String) page.executeJavaScript("""
                EditorSupport2.getClipboardText({clipboardData: {getData: function(t) { return 'pasted text'; }}});
            """).getJavaScriptResult();
            Assertions.assertEquals("pasted text", result);
        }
    }

    @Test
    public void testGetClipboardText_null() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Object result = page.executeJavaScript("""
                EditorSupport2.getClipboardText(null);
            """).getJavaScriptResult();
            Assertions.assertNull(result);
        }
    }

    @Test
    public void testGetClipboardText_noClipboardData() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">x</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Object result = page.executeJavaScript("""
                EditorSupport2.getClipboardText({});
            """).getJavaScriptResult();
            Assertions.assertNull(result);
        }
    }

    /************************************************************************
     * setCursor + readSelection round-trip.
     ************************************************************************/

    @Test
    public void testCursorRoundTrip_singleBlock() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 5);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 5, 0, 5);
        }
    }

    @Test
    public void testCursorRoundTrip_start() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 0);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 0, 0, 0);
        }
    }

    @Test
    public void testCursorRoundTrip_end() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 11);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 11, 0, 11);
        }
    }

    @Test
    public void testCursorRoundTrip_secondBlock() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello</div><div data-block-index=\"1\">World</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 1, 3);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 1, 3, 1, 3);
        }
    }

    @Test
    public void testCursorRoundTrip_withFormatting() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">He<span class='b'>llo</span> world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 4);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 4, 0, 4);
        }
    }

    @Test
    public void testCursorRoundTrip_afterLineBreak() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Line 1<br/>Line 2</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            // Offset 8 = "Line 1" (6) + BR (1) + "L" (1) = cursor at position 8
            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 8);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 8, 0, 8);
        }
    }

    /************************************************************************
     * setSelection + readSelection round-trip.
     ************************************************************************/

    @Disabled("HtmlUnit does not support Selection.extend()")
    @Test
    public void testSelectionRoundTrip_sameBlock() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setSelection(editor, 0, 2, 0, 8);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 2, 0, 8);
        }
    }

    @Disabled("HtmlUnit does not support Selection.extend()")
    @Test
    public void testSelectionRoundTrip_acrossBlocks() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello</div><div data-block-index=\"1\">World</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setSelection(editor, 0, 3, 1, 2);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 3, 1, 2);
        }
    }

    @Disabled("HtmlUnit does not support Selection.extend()")
    @Test
    public void testSelectionRoundTrip_fullBlock() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello world</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setSelection(editor, 0, 0, 0, 11);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 0, 0, 11);
        }
    }

    /************************************************************************
     * readSelection edge cases.
     ************************************************************************/

    @Test
    public void testReadSelection_noSelection() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hello</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Object result = page.executeJavaScript("""
                var editor = document.getElementById('editor');
                document.getSelection().removeAllRanges();
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            Assertions.assertNull(result);
        }
    }

    /************************************************************************
     * Cell operations.
     ************************************************************************/

    private static final String CELL_CONTENT = """
        <div data-block-index="0" contenteditable="false">\
        <table><tr><td>\
        <div contenteditable="true" data-table-index="0-0">Hello</div>\
        </td></tr></table>\
        </div>""";

    @Test
    public void testMoveCursorToStart() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", CELL_CONTENT));

            Double result = (Double) page.executeJavaScript("""
                var cell = document.querySelector('[data-table-index="0-0"]');
                EditorSupport2.moveCursorToStart(cell);
                EditorSupport2.cursorOffsetInCell(cell);
            """).getJavaScriptResult();
            Assertions.assertEquals(0, result);
        }
    }

    @Test
    public void testMoveCursorToEnd() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", CELL_CONTENT));

            Double result = (Double) page.executeJavaScript("""
                var cell = document.querySelector('[data-table-index="0-0"]');
                EditorSupport2.moveCursorToEnd(cell);
                EditorSupport2.cursorOffsetInCell(cell);
            """).getJavaScriptResult();
            Assertions.assertEquals(5, result);
        }
    }

    @Test
    public void testCellFromSelection() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", CELL_CONTENT));

            Object result = page.executeJavaScript("""
                var cell = document.querySelector('[data-table-index="0-0"]');
                EditorSupport2.moveCursorToStart(cell);
                var editor = document.getElementById('editor');
                var found = EditorSupport2.cellFromSelection(editor);
                found ? found.getAttribute('data-table-index') : null;
            """).getJavaScriptResult();
            Assertions.assertEquals("0-0", result);
        }
    }

    @Test
    public void testCellFromSelection_notInCell() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Plain text</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Object result = page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 3);
                EditorSupport2.cellFromSelection(editor);
            """).getJavaScriptResult();
            Assertions.assertNull(result);
        }
    }

    @Disabled("HtmlUnit does not support Selection.extend()")
    @Test
    public void testSetSelectionInCell_roundTrip() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", CELL_CONTENT));

            NativeArray result = (NativeArray) page.executeJavaScript("""
                var cell = document.querySelector('[data-table-index="0-0"]');
                EditorSupport2.setSelectionInCell(cell, 1, 4);
                EditorSupport2.selectionInCell(cell);
            """).getJavaScriptResult();
            assertArray(result, 1, 4);
        }
    }

    @Test
    public void testCursorOffsetInCell_notInCell() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Plain text</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            Double result = (Double) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 3);
                var cell = document.createElement('div');
                EditorSupport2.cursorOffsetInCell(cell);
            """).getJavaScriptResult();
            Assertions.assertEquals(-1, result);
        }
    }

    /************************************************************************
     * CEF (contenteditable="false") variable handling.
     ************************************************************************/

    @Test
    public void testCursorRoundTrip_afterVariable() throws Exception {
        try (WebClient webClient = new WebClient()) {
            // "Hi " (3) + CEF "Name" (4) + " there" (6) = 13 chars
            String content = "<div data-block-index=\"0\">Hi <span contenteditable=\"false\">Name</span> there</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            // Place cursor at offset 7 = after "Hi Name" = start of " there"
            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 7);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 7, 0, 7);
        }
    }

    @Test
    public void testCursorRoundTrip_beforeVariable() throws Exception {
        try (WebClient webClient = new WebClient()) {
            String content = "<div data-block-index=\"0\">Hi <span contenteditable=\"false\">Name</span> there</div>";
            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(load("EditorSupportTest.html", content));

            // Place cursor at offset 3 = after "Hi " = before the variable
            NativeArray result = (NativeArray) page.executeJavaScript("""
                var editor = document.getElementById('editor');
                EditorSupport2.setCursor(editor, 0, 3);
                EditorSupport2.readSelection(editor);
            """).getJavaScriptResult();
            assertArray(result, 0, 3, 0, 3);
        }
    }

    /************************************************************************
     * Assertions.
     ************************************************************************/

    private void assertLine(String expected, NativeArray result, int index) {
        Assertions.assertEquals(expected, String.valueOf(result.get(index)),
            () -> "Line[" + index + "]: expected \"" + expected + "\"");
    }

    private void assertArray(NativeArray result, int... expected) {
        Assertions.assertNotNull(result, "Expected non-null array");
        Assertions.assertEquals(expected.length, result.getLength(),
            () -> "Array length mismatch: expected " + expected.length + " but got " + result.getLength());
        for (int i = 0; i < expected.length; i++) {
            int actual = ((Number) result.get(i)).intValue();
            int idx = i;
            Assertions.assertEquals(expected[i], actual,
                () -> "Array[" + idx + "]: expected " + expected[idx] + " but got " + actual);
        }
    }

    /************************************************************************
     * Loading resources.
     ************************************************************************/

    protected String load(String file, String content) throws Exception {
        String str = load(file);
        str = str.replace("{CONTENT}", content);
        return str;
    }

    protected String load(String file) throws Exception {
        try (InputStream is = EditorSupportTest.class.getResourceAsStream(file)) {
            String str = IOUtils.toString(is, StandardCharsets.UTF_8);
            if (str.contains("{EDITORJS}"))
                str = str.replace("{EDITORJS}", loadEditorJs());
            return str;
        }
    }

    protected String loadEditorJs() throws Exception {
        try (InputStream is = EditorSupportTest.class.getResourceAsStream("/com/effacy/jui/text/public/jui_text_editor2.js")) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
