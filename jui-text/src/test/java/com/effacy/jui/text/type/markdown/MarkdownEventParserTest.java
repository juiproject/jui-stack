package com.effacy.jui.text.type.markdown;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;

/**
 * Tests for {@link MarkdownEventParser}. Verifies both the event sequence
 * (using a recording handler) and the {@link MarkdownEventParser.FormattedTextHandler}
 * (by comparing output to {@link MarkdownParser}).
 */
public class MarkdownEventParserTest {

    /************************************************************************
     * FormattedTextHandler parity tests — same output as MarkdownParser.
     ************************************************************************/

    @Test
    public void testEmptyInput() {
        FormattedText result = parseToFormattedText("");
        assertTrue(result.getBlocks().isEmpty());
    }

    @Test
    public void testNullInput() {
        FormattedText result = parseToFormattedText((String) null);
        assertTrue(result.getBlocks().isEmpty());
    }

    @Test
    public void testPlainText() {
        assertParityWithMarkdownParser("Hello world");
    }

    @Test
    public void testBold() {
        assertParityWithMarkdownParser("Hello **bold** world");
    }

    @Test
    public void testItalic() {
        assertParityWithMarkdownParser("Hello *italic* world");
    }

    @Test
    public void testStrikethrough() {
        assertParityWithMarkdownParser("Hello ~~strike~~ world");
    }

    @Test
    public void testCode() {
        assertParityWithMarkdownParser("Hello `code` world");
    }

    @Test
    public void testLink() {
        assertParityWithMarkdownParser("Visit [our site](http://example.com) now");
    }

    @Test
    public void testVariable() {
        assertParityWithMarkdownParser("Hello {{name;format=upper}}!");
    }

    @Test
    public void testHeadings() {
        assertParityWithMarkdownParser("# Heading 1");
        assertParityWithMarkdownParser("## Heading 2");
        assertParityWithMarkdownParser("### Heading 3");
    }

    @Test
    public void testList() {
        assertParityWithMarkdownParser("- First\n- Second\n- Third");
    }

    @Test
    public void testOrderedList() {
        assertParityWithMarkdownParser("1. First\n2. Second\n3. Third");
    }

    @Test
    public void testMultipleParagraphs() {
        assertParityWithMarkdownParser("First paragraph.\n\nSecond paragraph.");
    }

    @Test
    public void testTable() {
        assertParityWithMarkdownParser(
            "| Name | Age |\n" +
            "|------|-----|\n" +
            "| Alice | 30 |"
        );
    }

    @Test
    public void testTableAlignment() {
        assertParityWithMarkdownParser(
            "| Left | Center | Right |\n" +
            "|:-----|:------:|------:|\n" +
            "| a | b | c |"
        );
    }

    @Test
    public void testTableWithFormatting() {
        assertParityWithMarkdownParser(
            "| Header |\n" +
            "|--------|\n" +
            "| **bold** and *italic* |"
        );
    }

    @Test
    public void testMixedDocument() {
        assertParityWithMarkdownParser(
            "# Title\n\n" +
            "Some text with **bold**.\n\n" +
            "- Item 1\n- Item 2\n\n" +
            "| A | B |\n|---|---|\n| 1 | 2 |\n\n" +
            "Final paragraph."
        );
    }

    @Test
    public void testLineProcessor() {
        FormattedText expected = MarkdownParser.parse(line -> line.trim(), "  # Title  ");
        FormattedText actual = parseToFormattedText(line -> line.trim(), "  # Title  ");
        assertFormattedTextEquals(expected, actual);
    }

    /************************************************************************
     * Event sequence tests — verify the handler receives correct events.
     ************************************************************************/

    @Test
    public void testParagraphEvents() {
        RecordingHandler handler = parse("Hello world");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello world)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testFormattedEvents() {
        RecordingHandler handler = parse("Hello **bold** world");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "formatted(bold, BLD)",
            "text( world)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkEvents() {
        RecordingHandler handler = parse("[click](http://x.com)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(click, http://x.com)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableEvents() {
        RecordingHandler handler = parse("Hi {{name}}!");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hi )",
            "variable(name, {})",
            "text(!)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testHeadingEvents() {
        RecordingHandler handler = parse("## Title");
        handler.assertEvents(
            "startBlock(H2)",
            "startLine()",
            "text(Title)",
            "endLine()",
            "endBlock(H2)"
        );
    }

    @Test
    public void testListEvents() {
        RecordingHandler handler = parse("- A\n- B");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(A)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(B)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testOrderedListEvents() {
        RecordingHandler handler = parse("1. A\n2. B");
        handler.assertEvents(
            "startBlock(OLIST)",
            "startLine()",
            "text(A)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(B)",
            "endLine()",
            "endBlock(OLIST)"
        );
    }

    @Test
    public void testOrderedListWithIntroLine() {
        // An introductory non-list line followed by ordered items (single
        // newlines) splits into a PARA for the intro and OLIST items for
        // the list, matching standard markdown behaviour.
        RecordingHandler handler = parse(
            "To construct a proper Hegelian triad for your thesis defense:\n" +
            "1.  Begin with the abstract universal as your thesis statement.\n" +
            "2.  Identify the negation that reveals the internal contradiction of the thesis.\n" +
            "3.  Optionally, synthesize the aufhebung. Note that premature synthesis without genuine negation is automatically rejected.\n\n" +
            "**Further reading:** [The Phenomenology of Spirit](/hegel/phenom_spirit) | [Science of Logic](/hegel/science_logic)"
        );
        handler.assertEvents(
            // Intro line as PARA.
            "startBlock(PARA)",
            "startLine()",
            "text(To construct a proper Hegelian triad for your thesis defense:)",
            "endLine()",
            "endBlock(PARA)",
            // Ordered list items.
            "startBlock(OLIST)",
            "startLine()",
            "text(Begin with the abstract universal as your thesis statement.)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Identify the negation that reveals the internal contradiction of the thesis.)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Optionally, synthesize the aufhebung. Note that premature synthesis without genuine negation is automatically rejected.)",
            "endLine()",
            "endBlock(OLIST)",
            // Second paragraph — bold + links.
            "startBlock(PARA)",
            "startLine()",
            "formatted(Further reading:, BLD)",
            "text( )",
            "link(The Phenomenology of Spirit, /hegel/phenom_spirit)",
            "text( | )",
            "link(Science of Logic, /hegel/science_logic)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testTableEvents() {
        RecordingHandler handler = parse(
            "| H1 | H2 |\n" +
            "|:---|---:|\n" +
            "| a | b |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 2)",
            "meta(headers, 1)",
            "meta(align, L,R)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(H1)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(H2)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // Body row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(a)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(b)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    @Test
    public void testEmptyCellEvents() {
        RecordingHandler handler = parse(
            "| A | B |\n" +
            "|---|---|\n" +
            "| x |   |"
        );
        // Find the second TCELL in the body row — it should have no startLine/endLine.
        List<String> events = handler.events;
        // Locate the body row's second TCELL.
        int bodyRowStart = -1;
        int trowCount = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).equals("startBlock(TROW)")) {
                trowCount++;
                if (trowCount == 2) {
                    bodyRowStart = i;
                    break;
                }
            }
        }
        assertTrue(bodyRowStart >= 0);
        // Second TCELL in the body row should be: startBlock(TCELL), endBlock(TCELL)
        // with no startLine/endLine between them.
        int firstTcellEnd = handler.indexOf("endBlock(TCELL)", bodyRowStart);
        int secondTcellStart = handler.indexOf("startBlock(TCELL)", firstTcellEnd + 1);
        assertTrue(secondTcellStart >= 0);
        assertEquals("endBlock(TCELL)", events.get(secondTcellStart + 1));
    }

    /************************************************************************
     * Partial-mode tests — unclosed markers treated as formatting.
     ************************************************************************/

    @Test
    public void testPartialBold() {
        RecordingHandler handler = parsePartial("Hello **bold");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "formatted(bold, BLD)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialItalic() {
        RecordingHandler handler = parsePartial("Hello *italic");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "formatted(italic, ITL)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialStrikethrough() {
        RecordingHandler handler = parsePartial("Some ~~strike");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Some )",
            "formatted(strike, STR)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialCode() {
        RecordingHandler handler = parsePartial("Use `code");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Use )",
            "formatted(code, CODE)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialMixedClosedAndUnclosed() {
        RecordingHandler handler = parsePartial("**done** and *partial");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "formatted(done, BLD)",
            "text( and )",
            "formatted(partial, ITL)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialUnclosedNotInLastParagraph() {
        // Unclosed marker in first paragraph (followed by \n\n) should remain literal.
        RecordingHandler handler = parsePartial("Hello **bold\n\nSecond paragraph.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello **bold)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Second paragraph.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialIncompleteLink() {
        // Incomplete link should remain as literal text.
        RecordingHandler handler = parsePartial("Click [here](http");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Click [here](http)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialIncompleteVariable() {
        // Incomplete variable should remain as literal text.
        RecordingHandler handler = parsePartial("Hello {{name");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello {{name)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialNoUnclosedMarkers() {
        // When there are no unclosed markers, partial behaves the same as non-partial.
        RecordingHandler handler = parsePartial("Hello **bold** world");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "formatted(bold, BLD)",
            "text( world)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testPartialFormattedText() {
        // Verify FormattedText output with partial mode.
        FormattedText result = MarkdownParser.parse(true, "Hello **bold");
        assertEquals(1, result.getBlocks().size());
        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Hello bold", line.getText());
        assertEquals(1, line.getFormatting().size());
        FormattedLine.Format fmt = line.getFormatting().get(0);
        assertEquals(6, fmt.getIndex());
        assertEquals(4, fmt.getLength());
        assertTrue(fmt.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testPartialInHeading() {
        RecordingHandler handler = parsePartial("## Title with **bold");
        handler.assertEvents(
            "startBlock(H2)",
            "startLine()",
            "text(Title with )",
            "formatted(bold, BLD)",
            "endLine()",
            "endBlock(H2)"
        );
    }

    @Test
    public void testPartialInList() {
        RecordingHandler handler = parsePartial("- Item with **bold");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(Item with )",
            "formatted(bold, BLD)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    /************************************************************************
     * Helpers.
     ************************************************************************/

    private FormattedText parseToFormattedText(String... content) {
        FormattedTextMarkdownHandler handler = new FormattedTextMarkdownHandler();
        MarkdownEventParser.parse(handler, content);
        return handler.result();
    }

    private FormattedText parseToFormattedText(java.util.function.Function<String, String> lineProcessor, String... content) {
        FormattedTextMarkdownHandler handler = new FormattedTextMarkdownHandler();
        MarkdownEventParser.parse(handler, lineProcessor, content);
        return handler.result();
    }

    private void assertParityWithMarkdownParser(String markdown) {
        FormattedText expected = MarkdownParser.parse(markdown);
        FormattedText actual = parseToFormattedText(markdown);
        assertFormattedTextEquals(expected, actual);
    }

    private void assertFormattedTextEquals(FormattedText expected, FormattedText actual) {
        assertEquals(expected.getBlocks().size(), actual.getBlocks().size(),
            "Block count mismatch.\nExpected: " + expected.debug() + "\nActual: " + actual.debug());
        for (int i = 0; i < expected.getBlocks().size(); i++)
            assertBlockEquals(expected.getBlocks().get(i), actual.getBlocks().get(i), "block[" + i + "]");
    }

    private void assertBlockEquals(FormattedBlock expected, FormattedBlock actual, String path) {
        assertEquals(expected.getType(), actual.getType(), path + ".type");
        // Meta.
        assertEquals(expected.getMeta(), actual.getMeta(), path + ".meta");
        // Lines.
        assertEquals(expected.getLines().size(), actual.getLines().size(), path + ".lines.size");
        for (int i = 0; i < expected.getLines().size(); i++)
            assertLineEquals(expected.getLines().get(i), actual.getLines().get(i), path + ".line[" + i + "]");
        // Child blocks.
        assertEquals(expected.getBlocks().size(), actual.getBlocks().size(), path + ".blocks.size");
        for (int i = 0; i < expected.getBlocks().size(); i++)
            assertBlockEquals(expected.getBlocks().get(i), actual.getBlocks().get(i), path + ".block[" + i + "]");
    }

    private void assertLineEquals(FormattedLine expected, FormattedLine actual, String path) {
        assertEquals(expected.getText(), actual.getText(), path + ".text");
        assertEquals(expected.getFormatting().size(), actual.getFormatting().size(), path + ".formatting.size");
        for (int i = 0; i < expected.getFormatting().size(); i++) {
            FormattedLine.Format ef = expected.getFormatting().get(i);
            FormattedLine.Format af = actual.getFormatting().get(i);
            assertEquals(ef.getIndex(), af.getIndex(), path + ".format[" + i + "].index");
            assertEquals(ef.getLength(), af.getLength(), path + ".format[" + i + "].length");
            assertEquals(ef.getFormats(), af.getFormats(), path + ".format[" + i + "].formats");
            assertEquals(ef.getMeta(), af.getMeta(), path + ".format[" + i + "].meta");
        }
    }

    private RecordingHandler parse(String markdown) {
        RecordingHandler handler = new RecordingHandler();
        MarkdownEventParser.parse(handler, markdown);
        return handler;
    }

    private RecordingHandler parsePartial(String markdown) {
        RecordingHandler handler = new RecordingHandler();
        MarkdownEventParser.parse(handler, true, markdown);
        return handler;
    }

    /**
     * Handler that records all events as strings for assertion.
     */
    private static class RecordingHandler implements IMarkdownEventHandler {

        List<String> events = new ArrayList<>();

        @Override
        public void startBlock(BlockType type) {
            events.add("startBlock(" + type + ")");
        }

        @Override
        public void endBlock(BlockType type) {
            events.add("endBlock(" + type + ")");
        }

        @Override
        public void meta(String name, String value) {
            events.add("meta(" + name + ", " + value + ")");
        }

        @Override
        public void startLine() {
            events.add("startLine()");
        }

        @Override
        public void endLine() {
            events.add("endLine()");
        }

        @Override
        public void text(String text) {
            events.add("text(" + text + ")");
        }

        @Override
        public void formatted(String text, FormatType format) {
            events.add("formatted(" + text + ", " + format + ")");
        }

        @Override
        public void link(String label, String url) {
            events.add("link(" + label + ", " + url + ")");
        }

        @Override
        public void variable(String name, Map<String, String> meta) {
            events.add("variable(" + name + ", " + meta + ")");
        }

        void assertEvents(String... expected) {
            assertEquals(List.of(expected), events,
                "Event sequence mismatch.\nExpected:\n  " + String.join("\n  ", expected)
                + "\nActual:\n  " + String.join("\n  ", events));
        }

        int indexOf(String event, int fromIndex) {
            for (int i = fromIndex; i < events.size(); i++) {
                if (events.get(i).equals(event))
                    return i;
            }
            return -1;
        }
    }
}
