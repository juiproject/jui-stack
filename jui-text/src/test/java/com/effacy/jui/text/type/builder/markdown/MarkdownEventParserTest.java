package com.effacy.jui.text.type.builder.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.builder.FormattedTextBuilder;
import com.effacy.jui.text.type.builder.IEventBuilder;

/**
 * Tests for {@link MarkdownParser}. Verifies both the event sequence
 * (using a recording handler) and the {@link FormattedTextBuilder}
 * (by comparing output to {@link MarkdownParser#parseToFormattedText(String...)}).
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
    public void testFencedCodeBlock() {
        assertParityWithMarkdownParser(
            "```yaml\n" +
            "Allocation:\n" +
            "  relationships:\n" +
            "    shareholders:\n" +
            "      fields:\n" +
            "        order:\n" +
            "          type: integer\n" +
            "```"
        );
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
        FormattedText expected = FormattedText.markdown(line -> line.trim(), "  # Title  ");
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
    public void testFencedCodeBlockEvents() {
        RecordingHandler handler = parse(
            "```yaml\n" +
            "Allocation:\n" +
            "  relationships:\n" +
            "```"
        );
        handler.assertEvents(
            "startBlock(CODE)",
            "meta(lang, yaml)",
            "startLine()",
            "text(Allocation:)",
            "endLine()",
            "startLine()",
            "text(  relationships:)",
            "endLine()",
            "endBlock(CODE)"
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
    public void testOrderedListWithTrailingContinuation() {
        // Single newline after the last list item — the trailing text is
        // part of the second list item (no blank line to break the block).
        RecordingHandler handler = parse(
            "To prepare a proper cup of tea:\n" +
            "1.  Boil fresh water and warm the **teapot** by rinsing it (this step is often skipped but matters).\n" +
            "2.  Steep for **five** minutes.\n" +
            "Removing the leaves too early produces a weak and disappointing brew.\n"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(To prepare a proper cup of tea:)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Boil fresh water and warm the )",
            "formatted(teapot, BLD)",
            "text( by rinsing it (this step is often skipped but matters).)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Steep for )",
            "formatted(five, BLD)",
            "text( minutes. Removing the leaves too early produces a weak and disappointing brew.)",
            "endLine()",
            "endBlock(OLIST)"
        );
    }

    @Test
    public void testOrderedListWithTrailingParagraph() {
        // Double newline after the last list item — the trailing text
        // becomes a separate paragraph.
        RecordingHandler handler = parse(
            "To prepare a proper cup of tea:\n" +
            "1.  Boil fresh water and warm the **teapot** by rinsing it (this step is often skipped but matters).\n" +
            "2.  Steep for **five** minutes.\n\n" +
            "Removing the leaves too early produces a weak and disappointing brew.\n"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(To prepare a proper cup of tea:)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Boil fresh water and warm the )",
            "formatted(teapot, BLD)",
            "text( by rinsing it (this step is often skipped but matters).)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Steep for )",
            "formatted(five, BLD)",
            "text( minutes.)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(PARA)",
            "startLine()",
            "text(Removing the leaves too early produces a weak and disappointing brew.)",
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
        FormattedTextBuilder handler = new FormattedTextBuilder();
        new MarkdownParser().partial(true).parse(handler, "Hello **bold");
        FormattedText result = handler.result();
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
     * Edge cases — formatting and list issues.
     ************************************************************************/

    /**
     * Issue 4: Triple asterisk (bold+italic) is handled by the dedicated
     * {@code findBoldItalicRegions} path and emits a single formatted event
     * with both BLD and ITL types.
     */
    @Test
    public void testIssue4_tripleAsterisk() {
        RecordingHandler handler = parse("This is ***bold italic*** text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(bold italic, [BLD, ITL])",
            "text( text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /**
     * Issue 4 variant: bold markers nested inside italic markers are split
     * into segments — outer text gets ITL, inner bold gets both ITL and BLD.
     */
    @Test
    public void testIssue4_boldInsideItalic() {
        RecordingHandler handler = parse("This is *italic and **bold** inside* text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(italic and , ITL)",
            "formatted(bold, [ITL, BLD])",
            "formatted( inside, ITL)",
            "text( text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /**
     * Issue 4 variant: full italic sentence with bold inside.
     */
    @Test
    public void testIssue4_italicSentenceWithBoldInside() {
        RecordingHandler handler = parse("*Here is a sentance in italics with **some bold text** in the middle.*");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "formatted(Here is a sentance in italics with , ITL)",
            "formatted(some bold text, [ITL, BLD])",
            "formatted( in the middle., ITL)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /**
     * Issue 5: Underscores between word characters (e.g. identifiers) are not
     * treated as italic markers.
     */
    @Test
    public void testIssue5_underscoresInIdentifiers() {
        RecordingHandler handler = parse("Use the some_variable_name field");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Use the some_variable_name field)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /**
     * Issue 5 variant: multiple underscored identifiers are all treated as
     * plain text.
     */
    @Test
    public void testIssue5_multipleUnderscoreIdentifiers() {
        RecordingHandler handler = parse("Set my_var and your_var to zero");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Set my_var and your_var to zero)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /**
     * Issue 7: Two ordered lists separated by prose paragraphs are correctly
     * kept as distinct lists.
     */
    @Test
    public void testIssue7_separateOrderedListsWithProse() {
        RecordingHandler handler = parse("First list:\n\n1. Alpha\n2. Beta\n\nSecond list:\n\n1. Gamma\n2. Delta");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(First list:)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Alpha)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Beta)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(PARA)",
            "startLine()",
            "text(Second list:)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Gamma)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Delta)",
            "endLine()",
            "endBlock(OLIST)"
        );
    }

    /**
     * Issue 7 variant: two ordered lists back to back with only blank lines
     * are merged into a single continuous list by the parser's merge step
     * (consecutive list-only paragraphs are combined). This documents the
     * current behaviour.
     */
    @Test
    public void testIssue7_backToBackOrderedLists() {
        RecordingHandler handler = parse("1. First\n2. Second\n\n1. Alpha\n2. Beta");
        handler.assertEvents(
            "startBlock(OLIST)",
            "startLine()",
            "text(First)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Second)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Alpha)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Beta)",
            "endLine()",
            "endBlock(OLIST)"
        );
    }

    /**
     * Nested list: numbered items with blank lines between them and indented
     * bullet sub-items. The blank lines between top-level items must not split
     * the list.
     */
    @Test
    public void testNestedListWithBlankLines() {
        String markdown = """
                The following is a nested list of items:

                1.  **First item:** Here is a sub-item
                    *   **Subitem:** this is a sub-item.

                1.  **Second item:**
                    *   sub-item 1.
                    *   sub-item 2.
                    *   sub-item 3.

                1.  **Select Reviewees:**
                    *   sub-item 1.
                    *   sub-item 2.""";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            // Intro paragraph.
            "startBlock(PARA)",
            "startLine()",
            "text(The following is a nested list of items:)",
            "endLine()",
            "endBlock(PARA)",
            // First ordered item.
            "startBlock(OLIST)",
            "startLine()",
            "formatted(First item:, BLD)",
            "text( Here is a sub-item)",
            "endLine()",
            "endBlock(OLIST)",
            // Nested unordered sub-item.
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(  )",
            "formatted(Subitem:, BLD)",
            "text( this is a sub-item.)",
            "endLine()",
            "endBlock(NLIST)",
            // Second ordered item.
            "startBlock(OLIST)",
            "startLine()",
            "formatted(Second item:, BLD)",
            "endLine()",
            "endBlock(OLIST)",
            // Nested unordered sub-items.
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(  sub-item 1.)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(  sub-item 2.)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(  sub-item 3.)",
            "endLine()",
            "endBlock(NLIST)",
            // Third ordered item.
            "startBlock(OLIST)",
            "startLine()",
            "formatted(Select Reviewees:, BLD)",
            "endLine()",
            "endBlock(OLIST)",
            // Nested unordered sub-items.
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(  sub-item 1.)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(  sub-item 2.)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    /**
     * Unordered list with 3-space indented sub-items. Three spaces must be
     * recognised as indent level 1.
     */
    @Test
    public void testNestedListThreeSpaceIndent() {
        String markdown = "Here is a list:\n" +
            "- item 1\n" +
            "- item 2\n" +
            "- item 3\n" +
            "   - sub-item 1\n" +
            "   - sub-item 2\n" +
            "   - sub-item 3";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            // Intro paragraph.
            "startBlock(PARA)",
            "startLine()",
            "text(Here is a list:)",
            "endLine()",
            "endBlock(PARA)",
            // Top-level items.
            "startBlock(NLIST)",
            "startLine()",
            "text(item 1)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(item 2)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(item 3)",
            "endLine()",
            "endBlock(NLIST)",
            // Sub-items — indent level 1.
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(sub-item 1)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(sub-item 2)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(sub-item 3)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    /**
     * Unordered list with 2-space indented sub-items. Two spaces must be
     * recognised as indent level 1.
     */
    @Test
    public void testNestedListTwoSpaceIndent() {
        String markdown = """
                Here is a list.

                - Item 1.

                - Item 2:
                  - sub-item 1
                  - sub-item 2""";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            // Intro paragraph.
            "startBlock(PARA)",
            "startLine()",
            "text(Here is a list.)",
            "endLine()",
            "endBlock(PARA)",
            // Top-level items.
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 1.)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 2:)",
            "endLine()",
            "endBlock(NLIST)",
            // Sub-items — indent level 1.
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(sub-item 1)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "meta(indent, 1)",
            "startLine()",
            "text(sub-item 2)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    /************************************************************************
     * Formatting edge cases.
     ************************************************************************/

    @Test
    public void testBoldDoubleUnderscoreEvents() {
        RecordingHandler handler = parse("This is __bold__ text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "text( text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testItalicSingleUnderscoreEvents() {
        RecordingHandler handler = parse("This is _italic_ text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(italic, ITL)",
            "text( text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testMultipleFormatsInOneLineEvents() {
        RecordingHandler handler = parse("This is **bold** and *italic* and `code`");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "text( and )",
            "formatted(italic, ITL)",
            "text( and )",
            "formatted(code, CODE)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testUnclosedBoldMarkerEvents() {
        RecordingHandler handler = parse("This is **unclosed");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is **unclosed)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testUnclosedItalicMarkerEvents() {
        RecordingHandler handler = parse("This is *unclosed");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is *unclosed)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testEmptyBoldMarkersEvents() {
        RecordingHandler handler = parse("This is **** empty");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(, BLD)",
            "text( empty)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testConsecutiveFormattedSectionsEvents() {
        RecordingHandler handler = parse("**bold** *italic*");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "formatted(bold, BLD)",
            "text( )",
            "formatted(italic, ITL)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testFormattedTextAtStartEvents() {
        RecordingHandler handler = parse("**bold** at start");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "formatted(bold, BLD)",
            "text( at start)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testFormattedTextAtEndEvents() {
        RecordingHandler handler = parse("at end **bold**");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(at end )",
            "formatted(bold, BLD)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testWholeLineFormattedEvents() {
        RecordingHandler handler = parse("**entire line is bold**");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "formatted(entire line is bold, BLD)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testMixedBoldMarkersEvents() {
        RecordingHandler handler = parse("**bold__ text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(**bold__ text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testSpecialCharactersInFormattedTextEvents() {
        RecordingHandler handler = parse("Text with **special & < > chars**");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Text with )",
            "formatted(special & < > chars, BLD)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Paragraph structure.
     ************************************************************************/

    @Test
    public void testSingleNewlineWithinParagraphEvents() {
        RecordingHandler handler = parse("Line one\nLine two");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Line one)",
            "endLine()",
            "startLine()",
            "text(Line two)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testDoubleNewlineCreatesParagraphsEvents() {
        RecordingHandler handler = parse("Paragraph one\n\nParagraph two");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Paragraph one)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Paragraph two)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testTripleNewlineCreatesParagraphsEvents() {
        RecordingHandler handler = parse("Paragraph one\n\n\nParagraph two");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Paragraph one)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Paragraph two)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testMultipleParagraphsWithMultipleLinesEvents() {
        RecordingHandler handler = parse("Line 1a\nLine 1b\n\nLine 2a\nLine 2b");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Line 1a)",
            "endLine()",
            "startLine()",
            "text(Line 1b)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Line 2a)",
            "endLine()",
            "startLine()",
            "text(Line 2b)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testOnlyWhitespaceEvents() {
        RecordingHandler handler = parse("   \n\n   ");
        assertTrue(handler.events.isEmpty());
    }

    @Test
    public void testFormattingWithNewlinesEvents() {
        RecordingHandler handler = parse("**bold** text\n*italic* text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "formatted(bold, BLD)",
            "text( text)",
            "endLine()",
            "startLine()",
            "formatted(italic, ITL)",
            "text( text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testEmptyLineWithinParagraphEvents() {
        RecordingHandler handler = parse("Line one\n\nLine two");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Line one)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Line two)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Heading variations.
     ************************************************************************/

    @Test
    public void testHeadingH1Events() {
        RecordingHandler handler = parse("# Main Heading");
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(Main Heading)",
            "endLine()",
            "endBlock(H1)"
        );
    }

    @Test
    public void testHeadingH2Events() {
        RecordingHandler handler = parse("## Section Heading");
        handler.assertEvents(
            "startBlock(H2)",
            "startLine()",
            "text(Section Heading)",
            "endLine()",
            "endBlock(H2)"
        );
    }

    @Test
    public void testHeadingH3Events() {
        RecordingHandler handler = parse("### Subsection Heading");
        handler.assertEvents(
            "startBlock(H3)",
            "startLine()",
            "text(Subsection Heading)",
            "endLine()",
            "endBlock(H3)"
        );
    }

    @Test
    public void testHeadingWithFormattingEvents() {
        RecordingHandler handler = parse("# This is **bold** heading");
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "text( heading)",
            "endLine()",
            "endBlock(H1)"
        );
    }

    @Test
    public void testMultipleHeadingsEvents() {
        RecordingHandler handler = parse("# Title\n\n## Section 1\n\nSome text\n\n## Section 2");
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(Title)",
            "endLine()",
            "endBlock(H1)",
            "startBlock(H2)",
            "startLine()",
            "text(Section 1)",
            "endLine()",
            "endBlock(H2)",
            "startBlock(PARA)",
            "startLine()",
            "text(Some text)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(H2)",
            "startLine()",
            "text(Section 2)",
            "endLine()",
            "endBlock(H2)"
        );
    }

    @Test
    public void testHeadingWithWhitespaceEvents() {
        RecordingHandler handler = parse("  ## Heading with leading spaces  ");
        handler.assertEvents(
            "startBlock(H2)",
            "startLine()",
            "text(Heading with leading spaces)",
            "endLine()",
            "endBlock(H2)"
        );
    }

    @Test
    public void testInvalidHeadingEvents() {
        RecordingHandler handler = parse("#NoSpace");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(#NoSpace)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testHashInMiddleOfLineEvents() {
        RecordingHandler handler = parse("This is # not a heading");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is # not a heading)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * List variations.
     ************************************************************************/

    @Test
    public void testUnorderedListDashEvents() {
        RecordingHandler handler = parse("- Item 1\n- Item 2\n- Item 3");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 1)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 2)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 3)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testUnorderedListAsteriskEvents() {
        RecordingHandler handler = parse("* First\n* Second\n* Third");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(First)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Second)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Third)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testUnorderedListPlusEvents() {
        RecordingHandler handler = parse("+ Alpha\n+ Beta\n+ Gamma");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(Alpha)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Beta)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Gamma)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testOrderedListFullEvents() {
        RecordingHandler handler = parse("1. First item\n2. Second item\n3. Third item");
        handler.assertEvents(
            "startBlock(OLIST)",
            "startLine()",
            "text(First item)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Second item)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Third item)",
            "endLine()",
            "endBlock(OLIST)"
        );
    }

    @Test
    public void testListWithFormattingEvents() {
        RecordingHandler handler = parse("- This is **bold**\n- This is *italic*\n- This is `code`");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(This is )",
            "formatted(italic, ITL)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(This is )",
            "formatted(code, CODE)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testListSeparatedFromParagraphEvents() {
        RecordingHandler handler = parse("This is a paragraph.\n\n- Item 1\n- Item 2\n\nAnother paragraph.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is a paragraph.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 1)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Item 2)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(PARA)",
            "startLine()",
            "text(Another paragraph.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testListItemWithMultipleWordsEvents() {
        RecordingHandler handler = parse("- This is a longer list item with many words");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(This is a longer list item with many words)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testOrderedListWithDifferentNumbersEvents() {
        RecordingHandler handler = parse("1. First\n5. Fifth\n10. Tenth");
        handler.assertEvents(
            "startBlock(OLIST)",
            "startLine()",
            "text(First)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Fifth)",
            "endLine()",
            "endBlock(OLIST)",
            "startBlock(OLIST)",
            "startLine()",
            "text(Tenth)",
            "endLine()",
            "endBlock(OLIST)"
        );
    }

    /************************************************************************
     * Varargs and line processors.
     ************************************************************************/

    @Test
    public void testVarargsWithMultipleContentBlocksEvents() {
        RecordingHandler handler = parseVarargs("First block", "Second block", "Third block");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(First block)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Second block)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Third block)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVarargsWithNullValuesEvents() {
        RecordingHandler handler = parseVarargs("First block", null, "Third block");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(First block)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Third block)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLineProcessorUppercaseEvents() {
        RecordingHandler handler = parseWithProcessor(
            line -> line.toUpperCase(),
            "Hello world", "This is **bold**"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(HELLO WORLD)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(THIS IS )",
            "formatted(BOLD, BLD)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLineProcessorFilteringEvents() {
        RecordingHandler handler = parseWithProcessor(
            line -> line.startsWith("#") ? null : line,
            "Keep this line", "# Filter this line", "Keep this too"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Keep this line)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Keep this too)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLineProcessorWithPrefixRemovalEvents() {
        RecordingHandler handler = parseWithProcessor(
            line -> line.startsWith("> ") ? line.substring(2) : line,
            "> This is a quote", "> **Bold quote**"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is a quote)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "formatted(Bold quote, BLD)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Complex documents.
     ************************************************************************/

    @Test
    public void testComplexExampleEvents() {
        String markdown = "This is a **bold** statement with *italic* words.\n" +
                         "It has `code snippets` and ~~strikethrough~~ too.\n\n" +
                         "Second paragraph is **entirely bold**.";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is a )",
            "formatted(bold, BLD)",
            "text( statement with )",
            "formatted(italic, ITL)",
            "text( words.)",
            "endLine()",
            "startLine()",
            "text(It has )",
            "formatted(code snippets, CODE)",
            "text( and )",
            "formatted(strikethrough, STR)",
            "text( too.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(Second paragraph is )",
            "formatted(entirely bold, BLD)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testMixedDocumentEvents() {
        String markdown = "# Document Title\n\n" +
                         "Introduction paragraph.\n\n" +
                         "## Features\n\n" +
                         "- Feature **one**\n" +
                         "- Feature *two*\n" +
                         "- Feature `three`\n\n" +
                         "Conclusion.";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(Document Title)",
            "endLine()",
            "endBlock(H1)",
            "startBlock(PARA)",
            "startLine()",
            "text(Introduction paragraph.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(H2)",
            "startLine()",
            "text(Features)",
            "endLine()",
            "endBlock(H2)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Feature )",
            "formatted(one, BLD)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Feature )",
            "formatted(two, ITL)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Feature )",
            "formatted(three, CODE)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(PARA)",
            "startLine()",
            "text(Conclusion.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testComplexParagraphWithListEvents() {
        String markdown = "This is *some* supporting **guidance**:\n\n" +
                         "* Guidance 1\n" +
                         "* Guidance 2\n";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(some, ITL)",
            "text( supporting )",
            "formatted(guidance, BLD)",
            "text(:)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Guidance 1)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Guidance 2)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testComplexMixedFormattingInListEvents() {
        String markdown = "* This is **bold** text\n" +
                         "* This is *italic* text\n" +
                         "* This has `code` formatting\n" +
                         "* This has ~~strikethrough~~ text";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "text( text)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(This is )",
            "formatted(italic, ITL)",
            "text( text)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(This has )",
            "formatted(code, CODE)",
            "text( formatting)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(This has )",
            "formatted(strikethrough, STR)",
            "text( text)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testMultipleParagraphsWithVariedFormattingEvents() {
        String markdown = "This paragraph has **bold** and *italic* text.\n\n" +
                         "This paragraph has `code` and ~~strikethrough~~.\n\n" +
                         "This paragraph has **bold _and_ italic** combined.";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This paragraph has )",
            "formatted(bold, BLD)",
            "text( and )",
            "formatted(italic, ITL)",
            "text( text.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(This paragraph has )",
            "formatted(code, CODE)",
            "text( and )",
            "formatted(strikethrough, STR)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(PARA)",
            "startLine()",
            "text(This paragraph has )",
            "formatted(bold , BLD)",
            "formatted(and, [BLD, ITL])",
            "formatted( italic, BLD)",
            "text( combined.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testHeadingFollowedByFormattedParagraphAndListEvents() {
        String markdown = "## Important Section\n\n" +
                         "This section contains **critical** information about:\n\n" +
                         "- Point *one*\n" +
                         "- Point **two**\n" +
                         "- Point `three`";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(H2)",
            "startLine()",
            "text(Important Section)",
            "endLine()",
            "endBlock(H2)",
            "startBlock(PARA)",
            "startLine()",
            "text(This section contains )",
            "formatted(critical, BLD)",
            "text( information about:)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Point )",
            "formatted(one, ITL)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Point )",
            "formatted(two, BLD)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Point )",
            "formatted(three, CODE)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testComplexDocumentStructureEvents() {
        String markdown = "# Main Title\n\n" +
                         "Introduction with **bold** and *italic* text.\n\n" +
                         "## Section One\n\n" +
                         "Some content here.\n\n" +
                         "* First item with `code`\n" +
                         "* Second item with ~~strikethrough~~\n\n" +
                         "### Subsection\n\n" +
                         "More **important** details.";
        RecordingHandler handler = parse(markdown);
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(Main Title)",
            "endLine()",
            "endBlock(H1)",
            "startBlock(PARA)",
            "startLine()",
            "text(Introduction with )",
            "formatted(bold, BLD)",
            "text( and )",
            "formatted(italic, ITL)",
            "text( text.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(H2)",
            "startLine()",
            "text(Section One)",
            "endLine()",
            "endBlock(H2)",
            "startBlock(PARA)",
            "startLine()",
            "text(Some content here.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(NLIST)",
            "startLine()",
            "text(First item with )",
            "formatted(code, CODE)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(NLIST)",
            "startLine()",
            "text(Second item with )",
            "formatted(strikethrough, STR)",
            "endLine()",
            "endBlock(NLIST)",
            "startBlock(H3)",
            "startLine()",
            "text(Subsection)",
            "endLine()",
            "endBlock(H3)",
            "startBlock(PARA)",
            "startLine()",
            "text(More )",
            "formatted(important, BLD)",
            "text( details.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Link edge cases.
     ************************************************************************/

    @Test
    public void testSimpleLinkEvents() {
        RecordingHandler handler = parse("Click [here](https://example.com) for more.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Click )",
            "link(here, https://example.com)",
            "text( for more.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkAtStartEvents() {
        RecordingHandler handler = parse("[Google](https://google.com) is a search engine.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(Google, https://google.com)",
            "text( is a search engine.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkAtEndEvents() {
        RecordingHandler handler = parse("Visit [our site](https://example.org)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Visit )",
            "link(our site, https://example.org)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testMultipleLinksEvents() {
        RecordingHandler handler = parse("See [link1](http://one.com) and [link2](http://two.com).");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(See )",
            "link(link1, http://one.com)",
            "text( and )",
            "link(link2, http://two.com)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkWithBoldTextEvents() {
        RecordingHandler handler = parse("This is **bold** and [a link](http://example.com).");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "text( and )",
            "link(a link, http://example.com)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkBeforeBoldTextEvents() {
        RecordingHandler handler = parse("[link](http://example.com) and **bold**.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(link, http://example.com)",
            "text( and )",
            "formatted(bold, BLD)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkInListItemEvents() {
        RecordingHandler handler = parse("- Check [this](http://example.com) out");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(Check )",
            "link(this, http://example.com)",
            "text( out)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testLinkInHeadingEvents() {
        RecordingHandler handler = parse("# Check [the docs](http://docs.com)");
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(Check )",
            "link(the docs, http://docs.com)",
            "endLine()",
            "endBlock(H1)"
        );
    }

    @Test
    public void testIncompleteLinkNotParsedEvents() {
        RecordingHandler handler = parse("This is [incomplete](http://example.com text");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is [incomplete](http://example.com text)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testBracketsWithoutLinkNotParsedEvents() {
        RecordingHandler handler = parse("This is [not a link] here.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is [not a link] here.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkWithEmptyLabelEvents() {
        RecordingHandler handler = parse("Click [](http://example.com) here.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Click )",
            "link(, http://example.com)",
            "text( here.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkWithEmptyUrlEvents() {
        RecordingHandler handler = parse("Click [label]() here.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Click )",
            "link(label, )",
            "text( here.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkWithSpecialCharsInUrlEvents() {
        RecordingHandler handler = parse("See [docs](http://example.com/path?query=value&other=1#anchor).");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(See )",
            "link(docs, http://example.com/path?query=value&other=1#anchor)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testConsecutiveLinksEvents() {
        RecordingHandler handler = parse("[one](http://one.com)[two](http://two.com)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(one, http://one.com)",
            "link(two, http://two.com)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testLinkOnlyLineEvents() {
        RecordingHandler handler = parse("[Click me](http://example.com)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(Click me, http://example.com)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Image tests.
     ************************************************************************/

    @Test
    public void testSimpleImageEvents() {
        RecordingHandler handler = parse("Here is an image: ![alt text](http://example.com/img.png)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Here is an image: )",
            "image(alt text, http://example.com/img.png)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageAtStartEvents() {
        RecordingHandler handler = parse("![logo](http://example.com/logo.png) Company Name");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(logo, http://example.com/logo.png)",
            "text( Company Name)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageWithEmptyAltEvents() {
        RecordingHandler handler = parse("![](http://example.com/img.png)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(, http://example.com/img.png)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageWithLinkEvents() {
        RecordingHandler handler = parse("![photo](http://example.com/photo.jpg) and [link](http://example.com)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(photo, http://example.com/photo.jpg)",
            "text( and )",
            "link(link, http://example.com)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageInListEvents() {
        RecordingHandler handler = parse("- ![icon](icon.png) Item text");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "image(icon, icon.png)",
            "text( Item text)",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testLinkNotConfusedWithImageEvents() {
        // A regular link should not be treated as an image.
        RecordingHandler handler = parse("[not an image](http://example.com)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(not an image, http://example.com)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageWithWidthAndHeight() {
        RecordingHandler handler = parse("![photo](img.png =300x200)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(photo, img.png, w=300, h=200)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageWithWidthOnly() {
        RecordingHandler handler = parse("![photo](img.png =300)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(photo, img.png, w=300)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageWithHeightOnly() {
        RecordingHandler handler = parse("![photo](img.png =x200)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(photo, img.png, h=200)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testImageDimensionsWithFullUrl() {
        RecordingHandler handler = parse("![logo](http://example.com/logo.png =150x50)");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(logo, http://example.com/logo.png, w=150, h=50)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * URL resolver tests.
     ************************************************************************/

    @Test
    public void testUrlResolverMapsLinkUrl() {
        RecordingHandler handler = parseWithUrlResolver(
            (url, type) -> url.replace("/api/", "/v2/api/"),
            "Click [here](/api/docs) for docs."
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Click )",
            "link(here, /v2/api/docs)",
            "text( for docs.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testUrlResolverMapsImageSrc() {
        RecordingHandler handler = parseWithUrlResolver(
            (url, type) -> "https://cdn.example.com" + url,
            "![photo](/images/photo.jpg)"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(photo, https://cdn.example.com/images/photo.jpg)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testUrlResolverReturnsNullKeepsOriginal() {
        RecordingHandler handler = parseWithUrlResolver(
            (url, type) -> null,
            "[link](http://example.com)"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "link(link, http://example.com)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testUrlResolverDifferentiatesLinkAndImage() {
        RecordingHandler handler = parseWithUrlResolver(
            (url, type) -> {
                if (type == MarkdownParser.UrlType.IMAGE)
                    return "https://cdn.example.com" + url;
                return "https://www.example.com" + url;
            },
            "![img](/a.png) and [link](/b)"
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "image(img, https://cdn.example.com/a.png)",
            "text( and )",
            "link(link, https://www.example.com/b)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Variable edge cases.
     ************************************************************************/

    @Test
    public void testSimpleVariableEvents() {
        RecordingHandler handler = parse("Hello {{name}}!");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "variable(name, {})",
            "text(!)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableAtStartEvents() {
        RecordingHandler handler = parse("{{greeting}} World!");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "variable(greeting, {})",
            "text( World!)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableAtEndEvents() {
        RecordingHandler handler = parse("Hello {{name}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "variable(name, {})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testMultipleVariablesEvents() {
        RecordingHandler handler = parse("Dear {{title}} {{name}},");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Dear )",
            "variable(title, {})",
            "text( )",
            "variable(name, {})",
            "text(,)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableWithMetadataEvents() {
        RecordingHandler handler = parse("Value: {{amount;format=currency;precision=2}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Value: )",
            "variable(amount, {format=currency, precision=2})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableNameWithSpecialCharsEvents() {
        RecordingHandler handler = parse("{{user-name_field.value$type:id}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "variable(user-name_field.value$type:id, {})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableWithBoldTextEvents() {
        RecordingHandler handler = parse("This is **bold** and {{variable}} text.");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(This is )",
            "formatted(bold, BLD)",
            "text( and )",
            "variable(variable, {})",
            "text( text.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableWithLinkEvents() {
        RecordingHandler handler = parse("Hello {{name}}, visit [our site](http://example.com).");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello )",
            "variable(name, {})",
            "text(, visit )",
            "link(our site, http://example.com)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testInvalidVariableNameIgnoredEvents() {
        RecordingHandler handler = parse("Hello {{invalid name}}!");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello {{invalid name}}!)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testEmptyVariableIgnoredEvents() {
        RecordingHandler handler = parse("Hello {{}}!");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello {{}}!)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testUnclosedVariableIgnoredEvents() {
        RecordingHandler handler = parse("Hello {{name!");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello {{name!)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableInListItemEvents() {
        RecordingHandler handler = parse("- Item for {{user}}");
        handler.assertEvents(
            "startBlock(NLIST)",
            "startLine()",
            "text(Item for )",
            "variable(user, {})",
            "endLine()",
            "endBlock(NLIST)"
        );
    }

    @Test
    public void testVariableInHeadingEvents() {
        RecordingHandler handler = parse("# Welcome {{user}}");
        handler.assertEvents(
            "startBlock(H1)",
            "startLine()",
            "text(Welcome )",
            "variable(user, {})",
            "endLine()",
            "endBlock(H1)"
        );
    }

    @Test
    public void testVariableMetadataWithPeriodInFieldNameEvents() {
        RecordingHandler handler = parse("{{value;field.name=test}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "variable(value, {field.name=test})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableMetadataWithSpecialCharsInValueEvents() {
        RecordingHandler handler = parse("{{date;format=yyyy-MM-dd HH:mm:ss}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "variable(date, {format=yyyy-MM-dd HH:mm:ss})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableWithInvalidCharsEvents() {
        RecordingHandler handler = parse("{{invalid@name}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text({{invalid@name}})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testConsecutiveVariablesEvents() {
        RecordingHandler handler = parse("{{first}}{{second}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "variable(first, {})",
            "variable(second, {})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableOnlyLineEvents() {
        RecordingHandler handler = parse("{{username}}");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "variable(username, {})",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testVariableInsideLinkEvents() {
        RecordingHandler handler = parse("Hello, visit [our site named {{site-name}}](http://example.com).");
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Hello, visit )",
            "link(our site named {{site-name}}, http://example.com)",
            "text(.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    /************************************************************************
     * Table variations.
     ************************************************************************/

    @Test
    public void testSimpleTableEvents() {
        RecordingHandler handler = parse(
            "| Name | Age |\n" +
            "|------|-----|\n" +
            "| Alice | 30 |\n" +
            "| Bob | 25 |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 2)",
            "meta(headers, 1)",
            "meta(align, L,L)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Name)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Age)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // First body row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Alice)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(30)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // Second body row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Bob)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(25)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    @Test
    public void testTableAlignmentEvents() {
        RecordingHandler handler = parse(
            "| Left | Center | Right |\n" +
            "|:-----|:------:|------:|\n" +
            "| a | b | c |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 3)",
            "meta(headers, 1)",
            "meta(align, L,C,R)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Left)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Center)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Right)",
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
            "startBlock(TCELL)",
            "startLine()",
            "text(c)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    @Test
    public void testTableWithInlineFormattingEvents() {
        RecordingHandler handler = parse(
            "| Header |\n" +
            "|--------|\n" +
            "| **bold** and *italic* |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 1)",
            "meta(headers, 1)",
            "meta(align, L)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Header)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // Body row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "formatted(bold, BLD)",
            "text( and )",
            "formatted(italic, ITL)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    @Test
    public void testTableWithEmptyCellsEvents() {
        RecordingHandler handler = parse(
            "| A | B | C |\n" +
            "|---|---|---|\n" +
            "| x |   | z |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 3)",
            "meta(headers, 1)",
            "meta(align, L,L,L)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(A)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(B)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(C)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // Body row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(x)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(z)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    @Test
    public void testTableFewerCellsThanColumnsEvents() {
        RecordingHandler handler = parse(
            "| A | B | C |\n" +
            "|---|---|---|\n" +
            "| x |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 3)",
            "meta(headers, 1)",
            "meta(align, L,L,L)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(A)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(B)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(C)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // Body row — one cell present, two empty.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(x)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    @Test
    public void testTableWithSurroundingContentEvents() {
        RecordingHandler handler = parse(
            "Some text before.\n\n" +
            "| Col1 | Col2 |\n" +
            "|------|------|\n" +
            "| a | b |\n\n" +
            "Some text after."
        );
        handler.assertEvents(
            "startBlock(PARA)",
            "startLine()",
            "text(Some text before.)",
            "endLine()",
            "endBlock(PARA)",
            "startBlock(TABLE)",
            "meta(columns, 2)",
            "meta(headers, 1)",
            "meta(align, L,L)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Col1)",
            "endLine()",
            "endBlock(TCELL)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Col2)",
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
            "endBlock(TABLE)",
            "startBlock(PARA)",
            "startLine()",
            "text(Some text after.)",
            "endLine()",
            "endBlock(PARA)"
        );
    }

    @Test
    public void testTableWithLinkEvents() {
        RecordingHandler handler = parse(
            "| Link |\n" +
            "|------|\n" +
            "| [click](http://example.com) |"
        );
        handler.assertEvents(
            "startBlock(TABLE)",
            "meta(columns, 1)",
            "meta(headers, 1)",
            "meta(align, L)",
            // Header row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "text(Link)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            // Body row.
            "startBlock(TROW)",
            "startBlock(TCELL)",
            "startLine()",
            "link(click, http://example.com)",
            "endLine()",
            "endBlock(TCELL)",
            "endBlock(TROW)",
            "endBlock(TABLE)"
        );
    }

    /************************************************************************
     * Helpers.
     ************************************************************************/

    private FormattedText parseToFormattedText(String... content) {
        return new MarkdownParser().parse(new FormattedTextBuilder(), content);
    }

    private FormattedText parseToFormattedText(java.util.function.Function<String, String> lineProcessor, String... content) {
        return new MarkdownParser().lineProcessor(lineProcessor).parse(new FormattedTextBuilder(), content);
    }

    private void assertParityWithMarkdownParser(String markdown) {
        FormattedText expected = FormattedText.markdown(markdown);
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
        new MarkdownParser().parse(handler, markdown);
        return handler;
    }

    private RecordingHandler parsePartial(String markdown) {
        RecordingHandler handler = new RecordingHandler();
        new MarkdownParser().partial(true).parse(handler, markdown);
        return handler;
    }

    private RecordingHandler parseVarargs(String... content) {
        RecordingHandler handler = new RecordingHandler();
        new MarkdownParser().parse(handler, content);
        return handler;
    }

    private RecordingHandler parseWithProcessor(java.util.function.Function<String, String> lineProcessor, String... content) {
        RecordingHandler handler = new RecordingHandler();
        new MarkdownParser().lineProcessor(lineProcessor).parse(handler, content);
        return handler;
    }

    private RecordingHandler parseWithUrlResolver(java.util.function.BiFunction<String, MarkdownParser.UrlType, String> urlResolver, String markdown) {
        RecordingHandler handler = new RecordingHandler();
        new MarkdownParser().urlResolver(urlResolver).parse(handler, markdown);
        return handler;
    }

    /**
     * Handler that records all events as strings for assertion.
     */
    private static class RecordingHandler implements IEventBuilder<List<String>> {

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
        public void formatted(String text, FormatType... formats) {
            if (formats.length == 1)
                events.add("formatted(" + text + ", " + formats[0] + ")");
            else
                events.add("formatted(" + text + ", " + java.util.Arrays.toString(formats) + ")");
        }

        @Override
        public void link(String label, String url) {
            events.add("link(" + label + ", " + url + ")");
        }

        @Override
        public void image(String alt, String src, int width, int height) {
            StringBuilder sb = new StringBuilder("image(").append(alt).append(", ").append(src);
            if (width > 0)
                sb.append(", w=").append(width);
            if (height > 0)
                sb.append(", h=").append(height);
            sb.append(")");
            events.add(sb.toString());
        }

        @Override
        public void variable(String name, Map<String, String> meta) {
            events.add("variable(" + name + ", " + meta + ")");
        }

        @Override
        public List<String> result() {
            return events;
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
