package com.effacy.jui.text.type.markdown;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

public class MarkdownParserTest {

    @Test
    public void testEmptyInput() {
        FormattedText result = MarkdownParser.parse("");
        assertTrue(result.getBlocks().isEmpty());
    }

    @Test
    public void testNullInput() {
        FormattedText result = MarkdownParser.parse(null);
        assertTrue(result.getBlocks().isEmpty());
    }

    @Test
    public void testPlainText() {
        FormattedText result = MarkdownParser.parse("Hello world");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, block.getType());
        assertEquals(1, block.getLines().size());

        FormattedLine line = block.getLines().get(0);
        assertEquals("Hello world", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testBoldDoubleAsterisk() {
        FormattedText result = MarkdownParser.parse("This is **bold** text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is bold text", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testBoldDoubleUnderscore() {
        FormattedText result = MarkdownParser.parse("This is __bold__ text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is bold text", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testItalicSingleAsterisk() {
        FormattedText result = MarkdownParser.parse("This is *italic* text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is italic text", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(6, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testItalicSingleUnderscore() {
        FormattedText result = MarkdownParser.parse("This is _italic_ text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is italic text", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(6, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testStrikethrough() {
        FormattedText result = MarkdownParser.parse("This is ~~strikethrough~~ text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is strikethrough text", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(13, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.STR));
    }

    @Test
    public void testCode() {
        FormattedText result = MarkdownParser.parse("This is `code` text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is code text", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.CODE));
    }

    @Test
    public void testMultipleFormatsInOneLine() {
        FormattedText result = MarkdownParser.parse("This is **bold** and *italic* and `code`");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is bold and italic and code", line.getText());
        assertEquals(3, line.getFormatting().size());

        // Bold
        FormattedLine.Format format1 = line.getFormatting().get(0);
        assertEquals(8, format1.getIndex());
        assertEquals(4, format1.getLength());
        assertTrue(format1.getFormats().contains(FormatType.BLD));

        // Italic
        FormattedLine.Format format2 = line.getFormatting().get(1);
        assertEquals(17, format2.getIndex());
        assertEquals(6, format2.getLength());
        assertTrue(format2.getFormats().contains(FormatType.ITL));

        // Code
        FormattedLine.Format format3 = line.getFormatting().get(2);
        assertEquals(28, format3.getIndex());
        assertEquals(4, format3.getLength());
        assertTrue(format3.getFormats().contains(FormatType.CODE));
    }

    @Test
    public void testSingleNewlineWithinParagraph() {
        FormattedText result = MarkdownParser.parse("Line one\nLine two");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, block.getType());
        assertEquals(2, block.getLines().size());

        assertEquals("Line one", block.getLines().get(0).getText());
        assertEquals("Line two", block.getLines().get(1).getText());
    }

    @Test
    public void testDoubleNewlineCreatesParagraphs() {
        FormattedText result = MarkdownParser.parse("Paragraph one\n\nParagraph two");

        assertEquals(2, result.getBlocks().size());

        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, block1.getType());
        assertEquals(1, block1.getLines().size());
        assertEquals("Paragraph one", block1.getLines().get(0).getText());

        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(BlockType.PARA, block2.getType());
        assertEquals(1, block2.getLines().size());
        assertEquals("Paragraph two", block2.getLines().get(0).getText());
    }

    @Test
    public void testTripleNewlineCreatesParagraphs() {
        FormattedText result = MarkdownParser.parse("Paragraph one\n\n\nParagraph two");

        assertEquals(2, result.getBlocks().size());
        assertEquals("Paragraph one", result.getBlocks().get(0).getLines().get(0).getText());
        assertEquals("Paragraph two", result.getBlocks().get(1).getLines().get(0).getText());
    }

    @Test
    public void testMultipleParagraphsWithMultipleLines() {
        FormattedText result = MarkdownParser.parse("Line 1a\nLine 1b\n\nLine 2a\nLine 2b");

        assertEquals(2, result.getBlocks().size());

        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(2, block1.getLines().size());
        assertEquals("Line 1a", block1.getLines().get(0).getText());
        assertEquals("Line 1b", block1.getLines().get(1).getText());

        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(2, block2.getLines().size());
        assertEquals("Line 2a", block2.getLines().get(0).getText());
        assertEquals("Line 2b", block2.getLines().get(1).getText());
    }

    @Test
    public void testUnclosedBoldMarker() {
        FormattedText result = MarkdownParser.parse("This is **unclosed");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // Unclosed markers should be treated as literal text
        assertEquals("This is **unclosed", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testUnclosedItalicMarker() {
        FormattedText result = MarkdownParser.parse("This is *unclosed");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is *unclosed", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testEmptyBoldMarkers() {
        FormattedText result = MarkdownParser.parse("This is **** empty");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is  empty", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(8, format.getIndex());
        assertEquals(0, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testConsecutiveFormattedSections() {
        FormattedText result = MarkdownParser.parse("**bold** *italic*");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("bold italic", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format format1 = line.getFormatting().get(0);
        assertEquals(0, format1.getIndex());
        assertEquals(4, format1.getLength());
        assertTrue(format1.getFormats().contains(FormatType.BLD));

        FormattedLine.Format format2 = line.getFormatting().get(1);
        assertEquals(5, format2.getIndex());
        assertEquals(6, format2.getLength());
        assertTrue(format2.getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testFormattedTextAtStart() {
        FormattedText result = MarkdownParser.parse("**bold** at start");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("bold at start", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(0, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testFormattedTextAtEnd() {
        FormattedText result = MarkdownParser.parse("at end **bold**");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("at end bold", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(7, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testWholeLineFormatted() {
        FormattedText result = MarkdownParser.parse("**entire line is bold**");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("entire line is bold", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(0, format.getIndex());
        assertEquals(19, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testMixedBoldMarkers() {
        // Markers must match - ** matches **, not __
        FormattedText result = MarkdownParser.parse("**bold__ text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // Should not format since markers don't match
        assertEquals("**bold__ text", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testFormattingWithNewlines() {
        FormattedText result = MarkdownParser.parse("**bold** text\n*italic* text");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(2, block.getLines().size());

        FormattedLine line1 = block.getLines().get(0);
        assertEquals("bold text", line1.getText());
        assertEquals(1, line1.getFormatting().size());
        assertTrue(line1.getFormatting().get(0).getFormats().contains(FormatType.BLD));

        FormattedLine line2 = block.getLines().get(1);
        assertEquals("italic text", line2.getText());
        assertEquals(1, line2.getFormatting().size());
        assertTrue(line2.getFormatting().get(0).getFormats().contains(FormatType.ITL));
    }

    @Test
    public void testEmptyLineWithinParagraph() {
        FormattedText result = MarkdownParser.parse("Line one\n\nLine two");

        // Double newline creates new paragraph, not empty line within paragraph
        assertEquals(2, result.getBlocks().size());
    }

    @Test
    public void testComplexExample() {
        String markdown = "This is a **bold** statement with *italic* words.\n" +
                         "It has `code snippets` and ~~strikethrough~~ too.\n\n" +
                         "Second paragraph is **entirely bold**.";

        FormattedText result = MarkdownParser.parse(markdown);

        assertEquals(2, result.getBlocks().size());

        // First paragraph
        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(2, block1.getLines().size());

        FormattedLine line1 = block1.getLines().get(0);
        assertEquals("This is a bold statement with italic words.", line1.getText());
        assertEquals(2, line1.getFormatting().size());

        FormattedLine line2 = block1.getLines().get(1);
        assertEquals("It has code snippets and strikethrough too.", line2.getText());
        assertEquals(2, line2.getFormatting().size());

        // Second paragraph
        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(1, block2.getLines().size());
        assertEquals("Second paragraph is entirely bold.", block2.getLines().get(0).getText());
        assertEquals(1, block2.getLines().get(0).getFormatting().size());
    }

    @Test
    public void testOnlyWhitespace() {
        FormattedText result = MarkdownParser.parse("   \n\n   ");

        // Whitespace-only paragraphs should be skipped
        assertTrue(result.getBlocks().isEmpty());
    }

    @Test
    public void testSpecialCharactersInText() {
        FormattedText result = MarkdownParser.parse("Text with **special & < > chars**");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Text with special & < > chars", line.getText());
        assertEquals(1, line.getFormatting().size());
    }

    @Test
    public void testHeadingH1() {
        FormattedText result = MarkdownParser.parse("# Main Heading");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H1, block.getType());
        assertEquals(1, block.getLines().size());
        assertEquals("Main Heading", block.getLines().get(0).getText());
    }

    @Test
    public void testHeadingH2() {
        FormattedText result = MarkdownParser.parse("## Section Heading");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H2, block.getType());
        assertEquals(1, block.getLines().size());
        assertEquals("Section Heading", block.getLines().get(0).getText());
    }

    @Test
    public void testHeadingH3() {
        FormattedText result = MarkdownParser.parse("### Subsection Heading");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H3, block.getType());
        assertEquals(1, block.getLines().size());
        assertEquals("Subsection Heading", block.getLines().get(0).getText());
    }

    @Test
    public void testHeadingWithFormatting() {
        FormattedText result = MarkdownParser.parse("# This is **bold** heading");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H1, block.getType());

        FormattedLine line = block.getLines().get(0);
        assertEquals("This is bold heading", line.getText());
        assertEquals(1, line.getFormatting().size());
        assertTrue(line.getFormatting().get(0).getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testMultipleHeadings() {
        FormattedText result = MarkdownParser.parse("# Title\n\n## Section 1\n\nSome text\n\n## Section 2");

        assertEquals(4, result.getBlocks().size());

        assertEquals(BlockType.H1, result.getBlocks().get(0).getType());
        assertEquals("Title", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.H2, result.getBlocks().get(1).getType());
        assertEquals("Section 1", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.PARA, result.getBlocks().get(2).getType());
        assertEquals("Some text", result.getBlocks().get(2).getLines().get(0).getText());

        assertEquals(BlockType.H2, result.getBlocks().get(3).getType());
        assertEquals("Section 2", result.getBlocks().get(3).getLines().get(0).getText());
    }

    @Test
    public void testHeadingWithWhitespace() {
        FormattedText result = MarkdownParser.parse("  ## Heading with leading spaces  ");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H2, block.getType());
        assertEquals("Heading with leading spaces", block.getLines().get(0).getText());
    }

    @Test
    public void testInvalidHeading() {
        // Heading markers without space should not be treated as headings
        FormattedText result = MarkdownParser.parse("#NoSpace");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, block.getType());
        assertEquals("#NoSpace", block.getLines().get(0).getText());
    }

    @Test
    public void testHashInMiddleOfLine() {
        // # in the middle of line should not create a heading
        FormattedText result = MarkdownParser.parse("This is # not a heading");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, block.getType());
        assertEquals("This is # not a heading", block.getLines().get(0).getText());
    }

    @Test
    public void testUnorderedListDash() {
        FormattedText result = MarkdownParser.parse("- Item 1\n- Item 2\n- Item 3");

        // Each list item is now a separate block
        assertEquals(3, result.getBlocks().size());

        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(BlockType.NLIST, block1.getType());
        assertEquals(1, block1.getLines().size());
        assertEquals("Item 1", block1.getLines().get(0).getText());

        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(BlockType.NLIST, block2.getType());
        assertEquals(1, block2.getLines().size());
        assertEquals("Item 2", block2.getLines().get(0).getText());

        FormattedBlock block3 = result.getBlocks().get(2);
        assertEquals(BlockType.NLIST, block3.getType());
        assertEquals(1, block3.getLines().size());
        assertEquals("Item 3", block3.getLines().get(0).getText());
    }

    @Test
    public void testUnorderedListAsterisk() {
        FormattedText result = MarkdownParser.parse("* First\n* Second\n* Third");

        // Each list item is now a separate block
        assertEquals(3, result.getBlocks().size());

        assertEquals(BlockType.NLIST, result.getBlocks().get(0).getType());
        assertEquals("First", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(1).getType());
        assertEquals("Second", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(2).getType());
        assertEquals("Third", result.getBlocks().get(2).getLines().get(0).getText());
    }

    @Test
    public void testUnorderedListPlus() {
        FormattedText result = MarkdownParser.parse("+ Alpha\n+ Beta\n+ Gamma");

        // Each list item is now a separate block
        assertEquals(3, result.getBlocks().size());

        assertEquals(BlockType.NLIST, result.getBlocks().get(0).getType());
        assertEquals("Alpha", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(1).getType());
        assertEquals("Beta", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(2).getType());
        assertEquals("Gamma", result.getBlocks().get(2).getLines().get(0).getText());
    }

    @Test
    public void testOrderedList() {
        FormattedText result = MarkdownParser.parse("1. First item\n2. Second item\n3. Third item");

        // Each list item is now a separate block
        assertEquals(3, result.getBlocks().size());

        assertEquals(BlockType.NLIST, result.getBlocks().get(0).getType());
        assertEquals("First item", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(1).getType());
        assertEquals("Second item", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(2).getType());
        assertEquals("Third item", result.getBlocks().get(2).getLines().get(0).getText());
    }

    @Test
    public void testListWithFormatting() {
        FormattedText result = MarkdownParser.parse("- This is **bold**\n- This is *italic*\n- This is `code`");

        // Each list item is now a separate block
        assertEquals(3, result.getBlocks().size());

        // Bold
        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(BlockType.NLIST, block1.getType());
        FormattedLine line1 = block1.getLines().get(0);
        assertEquals("This is bold", line1.getText());
        assertEquals(1, line1.getFormatting().size());
        assertTrue(line1.getFormatting().get(0).getFormats().contains(FormatType.BLD));

        // Italic
        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(BlockType.NLIST, block2.getType());
        FormattedLine line2 = block2.getLines().get(0);
        assertEquals("This is italic", line2.getText());
        assertEquals(1, line2.getFormatting().size());
        assertTrue(line2.getFormatting().get(0).getFormats().contains(FormatType.ITL));

        // Code
        FormattedBlock block3 = result.getBlocks().get(2);
        assertEquals(BlockType.NLIST, block3.getType());
        FormattedLine line3 = block3.getLines().get(0);
        assertEquals("This is code", line3.getText());
        assertEquals(1, line3.getFormatting().size());
        assertTrue(line3.getFormatting().get(0).getFormats().contains(FormatType.CODE));
    }

    @Test
    public void testListSeparatedFromParagraph() {
        FormattedText result = MarkdownParser.parse("This is a paragraph.\n\n- Item 1\n- Item 2\n\nAnother paragraph.");

        // Now we have: 1 paragraph + 2 list items + 1 paragraph = 4 blocks
        assertEquals(4, result.getBlocks().size());

        assertEquals(BlockType.PARA, result.getBlocks().get(0).getType());
        assertEquals("This is a paragraph.", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(1).getType());
        assertEquals("Item 1", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(2).getType());
        assertEquals("Item 2", result.getBlocks().get(2).getLines().get(0).getText());

        assertEquals(BlockType.PARA, result.getBlocks().get(3).getType());
        assertEquals("Another paragraph.", result.getBlocks().get(3).getLines().get(0).getText());
    }

    @Test
    public void testMixedDocument() {
        String markdown = "# Document Title\n\n" +
                         "Introduction paragraph.\n\n" +
                         "## Features\n\n" +
                         "- Feature **one**\n" +
                         "- Feature *two*\n" +
                         "- Feature `three`\n\n" +
                         "Conclusion.";

        FormattedText result = MarkdownParser.parse(markdown);

        // Now: H1 + PARA + H2 + 3 NLIST items + PARA = 7 blocks
        assertEquals(7, result.getBlocks().size());

        // Title
        assertEquals(BlockType.H1, result.getBlocks().get(0).getType());
        assertEquals("Document Title", result.getBlocks().get(0).getLines().get(0).getText());

        // Introduction
        assertEquals(BlockType.PARA, result.getBlocks().get(1).getType());
        assertEquals("Introduction paragraph.", result.getBlocks().get(1).getLines().get(0).getText());

        // Features heading
        assertEquals(BlockType.H2, result.getBlocks().get(2).getType());
        assertEquals("Features", result.getBlocks().get(2).getLines().get(0).getText());

        // Features list items (each is a separate block)
        assertEquals(BlockType.NLIST, result.getBlocks().get(3).getType());
        assertEquals(BlockType.NLIST, result.getBlocks().get(4).getType());
        assertEquals(BlockType.NLIST, result.getBlocks().get(5).getType());

        // Conclusion
        assertEquals(BlockType.PARA, result.getBlocks().get(6).getType());
        assertEquals("Conclusion.", result.getBlocks().get(6).getLines().get(0).getText());
    }

    @Test
    public void testListItemWithMultipleWords() {
        FormattedText result = MarkdownParser.parse("- This is a longer list item with many words");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.NLIST, block.getType());
        assertEquals(1, block.getLines().size());
        assertEquals("This is a longer list item with many words", block.getLines().get(0).getText());
    }

    @Test
    public void testOrderedListWithDifferentNumbers() {
        FormattedText result = MarkdownParser.parse("1. First\n5. Fifth\n10. Tenth");

        // Each list item is now a separate block
        assertEquals(3, result.getBlocks().size());

        assertEquals(BlockType.NLIST, result.getBlocks().get(0).getType());
        assertEquals("First", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(1).getType());
        assertEquals("Fifth", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.NLIST, result.getBlocks().get(2).getType());
        assertEquals("Tenth", result.getBlocks().get(2).getLines().get(0).getText());
    }

    @Test
    public void testVarargsWithMultipleContentBlocks() {
        FormattedText result = MarkdownParser.parse("First block", "Second block", "Third block");

        assertEquals(3, result.getBlocks().size());
        assertEquals(BlockType.PARA, result.getBlocks().get(0).getType());
        assertEquals("First block", result.getBlocks().get(0).getLines().get(0).getText());

        assertEquals(BlockType.PARA, result.getBlocks().get(1).getType());
        assertEquals("Second block", result.getBlocks().get(1).getLines().get(0).getText());

        assertEquals(BlockType.PARA, result.getBlocks().get(2).getType());
        assertEquals("Third block", result.getBlocks().get(2).getLines().get(0).getText());
    }

    @Test
    public void testVarargsWithNullValues() {
        FormattedText result = MarkdownParser.parse("First block", null, "Third block");

        assertEquals(2, result.getBlocks().size());
        assertEquals("First block", result.getBlocks().get(0).getLines().get(0).getText());
        assertEquals("Third block", result.getBlocks().get(1).getLines().get(0).getText());
    }

    @Test
    public void testLineProcessorUppercase() {
        FormattedText result = MarkdownParser.parse(
            line -> line.toUpperCase(),
            "Hello world",
            "This is **bold**"
        );

        assertEquals(2, result.getBlocks().size());
        assertEquals("HELLO WORLD", result.getBlocks().get(0).getLines().get(0).getText());
        assertEquals("THIS IS BOLD", result.getBlocks().get(1).getLines().get(0).getText());
    }

    @Test
    public void testLineProcessorFiltering() {
        FormattedText result = MarkdownParser.parse(
            line -> line.startsWith("#") ? null : line,
            "Keep this line",
            "# Filter this line",
            "Keep this too"
        );

        // The filtered line becomes empty and the block is skipped entirely
        assertEquals(2, result.getBlocks().size());
        assertEquals("Keep this line", result.getBlocks().get(0).getLines().get(0).getText());
        assertEquals("Keep this too", result.getBlocks().get(1).getLines().get(0).getText());
    }

    @Test
    public void testLineProcessorWithPrefixRemoval() {
        FormattedText result = MarkdownParser.parse(
            line -> line.startsWith("> ") ? line.substring(2) : line,
            "> This is a quote",
            "> **Bold quote**"
        );

        // Each vararg creates a separate block, so we have 2 blocks
        assertEquals(2, result.getBlocks().size());

        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(1, block1.getLines().size());
        assertEquals("This is a quote", block1.getLines().get(0).getText());

        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(1, block2.getLines().size());
        assertEquals("Bold quote", block2.getLines().get(0).getText());

        // Verify bold formatting is preserved
        FormattedLine line2 = block2.getLines().get(0);
        assertEquals(1, line2.getFormatting().size());
        assertTrue(line2.getFormatting().get(0).getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testComplexParagraphWithListFollowing() {
        String markdown = """
This is *some* supporting **guidance**:

* Guidance 1
* Guidance 2
""";

        FormattedText result = MarkdownParser.parse(markdown);

        // Now we have 3 blocks: 1 paragraph + 2 list items (each is a separate block)
        assertEquals(3, result.getBlocks().size());

        // First block: paragraph with mixed formatting
        FormattedBlock para = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, para.getType());
        assertEquals(1, para.getLines().size());

        FormattedLine paraLine = para.getLines().get(0);
        assertEquals("This is some supporting guidance:", paraLine.getText());
        assertEquals(2, paraLine.getFormatting().size());

        // Check italic "some"
        FormattedLine.Format italic = paraLine.getFormatting().get(0);
        assertEquals(8, italic.getIndex());
        assertEquals(4, italic.getLength());
        assertTrue(italic.getFormats().contains(FormatType.ITL));

        // Check bold "guidance"
        FormattedLine.Format bold = paraLine.getFormatting().get(1);
        assertEquals(24, bold.getIndex());
        assertEquals(8, bold.getLength());
        assertTrue(bold.getFormats().contains(FormatType.BLD));

        // Second block: first list item
        FormattedBlock list1 = result.getBlocks().get(1);
        assertEquals(BlockType.NLIST, list1.getType());
        assertEquals(1, list1.getLines().size());
        assertEquals("Guidance 1", list1.getLines().get(0).getText());

        // Third block: second list item
        FormattedBlock list2 = result.getBlocks().get(2);
        assertEquals(BlockType.NLIST, list2.getType());
        assertEquals(1, list2.getLines().size());
        assertEquals("Guidance 2", list2.getLines().get(0).getText());
    }

    @Test
    public void testComplexMixedFormattingInList() {
        String markdown = "* This is **bold** text\n" +
                         "* This is *italic* text\n" +
                         "* This has `code` formatting\n" +
                         "* This has ~~strikethrough~~ text";

        FormattedText result = MarkdownParser.parse(markdown);

        // Each list item is now a separate block
        assertEquals(4, result.getBlocks().size());

        // Check bold in first item
        FormattedBlock block1 = result.getBlocks().get(0);
        assertEquals(BlockType.NLIST, block1.getType());
        FormattedLine line1 = block1.getLines().get(0);
        assertEquals("This is bold text", line1.getText());
        assertEquals(1, line1.getFormatting().size());
        assertTrue(line1.getFormatting().get(0).getFormats().contains(FormatType.BLD));

        // Check italic in second item
        FormattedBlock block2 = result.getBlocks().get(1);
        assertEquals(BlockType.NLIST, block2.getType());
        FormattedLine line2 = block2.getLines().get(0);
        assertEquals("This is italic text", line2.getText());
        assertEquals(1, line2.getFormatting().size());
        assertTrue(line2.getFormatting().get(0).getFormats().contains(FormatType.ITL));

        // Check code in third item
        FormattedBlock block3 = result.getBlocks().get(2);
        assertEquals(BlockType.NLIST, block3.getType());
        FormattedLine line3 = block3.getLines().get(0);
        assertEquals("This has code formatting", line3.getText());
        assertEquals(1, line3.getFormatting().size());
        assertTrue(line3.getFormatting().get(0).getFormats().contains(FormatType.CODE));

        // Check strikethrough in fourth item
        FormattedBlock block4 = result.getBlocks().get(3);
        assertEquals(BlockType.NLIST, block4.getType());
        FormattedLine line4 = block4.getLines().get(0);
        assertEquals("This has strikethrough text", line4.getText());
        assertEquals(1, line4.getFormatting().size());
        assertTrue(line4.getFormatting().get(0).getFormats().contains(FormatType.STR));
    }

    @Test
    public void testMultipleParagraphsWithVariedFormatting() {
        String markdown = "This paragraph has **bold** and *italic* text.\n\n" +
                         "This paragraph has `code` and ~~strikethrough~~.\n\n" +
                         "This paragraph has **bold _and_ italic** combined.";

        FormattedText result = MarkdownParser.parse(markdown);

        assertEquals(3, result.getBlocks().size());

        // First paragraph
        FormattedBlock para1 = result.getBlocks().get(0);
        assertEquals(BlockType.PARA, para1.getType());
        FormattedLine line1 = para1.getLines().get(0);
        assertEquals("This paragraph has bold and italic text.", line1.getText());
        assertEquals(2, line1.getFormatting().size());

        // Second paragraph
        FormattedBlock para2 = result.getBlocks().get(1);
        assertEquals(BlockType.PARA, para2.getType());
        FormattedLine line2 = para2.getLines().get(0);
        assertEquals("This paragraph has code and strikethrough.", line2.getText());
        assertEquals(2, line2.getFormatting().size());

        // Third paragraph
        FormattedBlock para3 = result.getBlocks().get(2);
        assertEquals(BlockType.PARA, para3.getType());
        FormattedLine line3 = para3.getLines().get(0);
        assertEquals("This paragraph has bold _and_ italic combined.", line3.getText());
        // Should have bold formatting (the _and_ is inside the bold markers)
        assertEquals(1, line3.getFormatting().size());
        assertTrue(line3.getFormatting().get(0).getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testHeadingFollowedByFormattedParagraphAndList() {
        String markdown = "## Important Section\n\n" +
                         "This section contains **critical** information about:\n\n" +
                         "- Point *one*\n" +
                         "- Point **two**\n" +
                         "- Point `three`";

        FormattedText result = MarkdownParser.parse(markdown);

        // Now: H2 + PARA + 3 NLIST items = 5 blocks
        assertEquals(5, result.getBlocks().size());

        // Heading
        FormattedBlock heading = result.getBlocks().get(0);
        assertEquals(BlockType.H2, heading.getType());
        assertEquals("Important Section", heading.getLines().get(0).getText());

        // Paragraph with bold
        FormattedBlock para = result.getBlocks().get(1);
        assertEquals(BlockType.PARA, para.getType());
        FormattedLine paraLine = para.getLines().get(0);
        assertEquals("This section contains critical information about:", paraLine.getText());
        assertEquals(1, paraLine.getFormatting().size());
        assertTrue(paraLine.getFormatting().get(0).getFormats().contains(FormatType.BLD));

        // List items (each is a separate block)
        // Check italic in first item
        FormattedBlock list1 = result.getBlocks().get(2);
        assertEquals(BlockType.NLIST, list1.getType());
        FormattedLine item1 = list1.getLines().get(0);
        assertEquals("Point one", item1.getText());
        assertEquals(1, item1.getFormatting().size());
        assertTrue(item1.getFormatting().get(0).getFormats().contains(FormatType.ITL));

        // Check bold in second item
        FormattedBlock list2 = result.getBlocks().get(3);
        assertEquals(BlockType.NLIST, list2.getType());
        FormattedLine item2 = list2.getLines().get(0);
        assertEquals("Point two", item2.getText());
        assertEquals(1, item2.getFormatting().size());
        assertTrue(item2.getFormatting().get(0).getFormats().contains(FormatType.BLD));

        // Check code in third item
        FormattedBlock list3 = result.getBlocks().get(4);
        assertEquals(BlockType.NLIST, list3.getType());
        FormattedLine item3 = list3.getLines().get(0);
        assertEquals("Point three", item3.getText());
        assertEquals(1, item3.getFormatting().size());
        assertTrue(item3.getFormatting().get(0).getFormats().contains(FormatType.CODE));
    }

    @Test
    public void testComplexDocumentStructure() {
        String markdown = "# Main Title\n\n" +
                         "Introduction with **bold** and *italic* text.\n\n" +
                         "## Section One\n\n" +
                         "Some content here.\n\n" +
                         "* First item with `code`\n" +
                         "* Second item with ~~strikethrough~~\n\n" +
                         "### Subsection\n\n" +
                         "More **important** details.";

        FormattedText result = MarkdownParser.parse(markdown);

        // Now: H1 + PARA + H2 + PARA + 2 NLIST items + H3 + PARA = 8 blocks
        assertEquals(8, result.getBlocks().size());

        // Main title
        assertEquals(BlockType.H1, result.getBlocks().get(0).getType());
        assertEquals("Main Title", result.getBlocks().get(0).getLines().get(0).getText());

        // Introduction paragraph
        assertEquals(BlockType.PARA, result.getBlocks().get(1).getType());
        FormattedLine intro = result.getBlocks().get(1).getLines().get(0);
        assertEquals("Introduction with bold and italic text.", intro.getText());
        assertEquals(2, intro.getFormatting().size());

        // Section heading
        assertEquals(BlockType.H2, result.getBlocks().get(2).getType());
        assertEquals("Section One", result.getBlocks().get(2).getLines().get(0).getText());

        // Content paragraph
        assertEquals(BlockType.PARA, result.getBlocks().get(3).getType());
        assertEquals("Some content here.", result.getBlocks().get(3).getLines().get(0).getText());

        // List items (each is a separate block)
        assertEquals(BlockType.NLIST, result.getBlocks().get(4).getType());
        assertEquals(BlockType.NLIST, result.getBlocks().get(5).getType());

        // Subsection heading
        assertEquals(BlockType.H3, result.getBlocks().get(6).getType());
        assertEquals("Subsection", result.getBlocks().get(6).getLines().get(0).getText());

        // Final paragraph
        FormattedBlock finalPara = result.getBlocks().get(7);
        assertEquals(BlockType.PARA, finalPara.getType());
        FormattedLine finalLine = finalPara.getLines().get(0);
        assertEquals("More important details.", finalLine.getText());
        assertEquals(1, finalLine.getFormatting().size());
        assertTrue(finalLine.getFormatting().get(0).getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testDebugOutput() {
        String markdown = "This is *some* supporting **guidance**:\n\n" +
                         "* Guidance 1\n" +
                         "* Guidance 2";

        FormattedText result = MarkdownParser.parse(markdown);
        String debug = result.debug();

        // Verify debug output contains key structural information
        // Now 3 blocks: 1 PARA + 2 NLIST (each list item is a separate block)
        assertTrue(debug.contains("FormattedText[blocks=3]"));
        assertTrue(debug.contains("Block[type=PARA"));
        assertTrue(debug.contains("Block[type=NLIST"));
        assertTrue(debug.contains("\"This is some supporting guidance:\""));
        assertTrue(debug.contains("\"Guidance 1\""));
        assertTrue(debug.contains("\"Guidance 2\""));

        // Verify formatting information is present
        assertTrue(debug.contains("[ITL]@8+4")); // italic "some" at position 8, length 4
        assertTrue(debug.contains("[BLD]@24+8")); // bold "guidance" at position 24, length 8

        // Print for manual inspection during development
        System.out.println("Debug output:\n" + debug);
    }

    @Test
    public void testDebugOutputEmpty() {
        FormattedText empty = new FormattedText();
        String debug = empty.debug();

        assertEquals("[EMPTY FormattedText]", debug);
    }

    @Test
    public void testDebugOutputWithComplexFormatting() {
        String markdown = "# Heading\n\n" +
                         "Text with **bold** and *italic* and `code`.";

        FormattedText result = MarkdownParser.parse(markdown);
        String debug = result.debug();

        // Verify all blocks are shown
        assertTrue(debug.contains("FormattedText[blocks=2]"));
        assertTrue(debug.contains("Block[type=H1"));
        assertTrue(debug.contains("Block[type=PARA"));

        // Verify all formatting is shown
        assertTrue(debug.contains("[BLD]"));
        assertTrue(debug.contains("[ITL]"));
        assertTrue(debug.contains("[CODE]"));

        System.out.println("Complex formatting debug:\n" + debug);
    }

    @Test
    public void testSimpleLink() {
        FormattedText result = MarkdownParser.parse("Click [here](https://example.com) for more.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Click here for more.", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("https://example.com", format.getMeta().get("link"));
    }

    @Test
    public void testLinkAtStart() {
        FormattedText result = MarkdownParser.parse("[Google](https://google.com) is a search engine.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Google is a search engine.", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(0, format.getIndex());
        assertEquals(6, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("https://google.com", format.getMeta().get("link"));
    }

    @Test
    public void testLinkAtEnd() {
        FormattedText result = MarkdownParser.parse("Visit [our site](https://example.org)");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Visit our site", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(8, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("https://example.org", format.getMeta().get("link"));
    }

    @Test
    public void testMultipleLinks() {
        FormattedText result = MarkdownParser.parse("See [link1](http://one.com) and [link2](http://two.com).");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("See link1 and link2.", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format format1 = line.getFormatting().get(0);
        assertEquals(4, format1.getIndex());
        assertEquals(5, format1.getLength());
        assertTrue(format1.getFormats().contains(FormatType.A));
        assertEquals("http://one.com", format1.getMeta().get("link"));

        FormattedLine.Format format2 = line.getFormatting().get(1);
        assertEquals(14, format2.getIndex());
        assertEquals(5, format2.getLength());
        assertTrue(format2.getFormats().contains(FormatType.A));
        assertEquals("http://two.com", format2.getMeta().get("link"));
    }

    @Test
    public void testLinkWithBoldText() {
        FormattedText result = MarkdownParser.parse("This is **bold** and [a link](http://example.com).");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is bold and a link.", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format bold = line.getFormatting().get(0);
        assertEquals(8, bold.getIndex());
        assertEquals(4, bold.getLength());
        assertTrue(bold.getFormats().contains(FormatType.BLD));

        FormattedLine.Format link = line.getFormatting().get(1);
        assertEquals(17, link.getIndex());
        assertEquals(6, link.getLength());
        assertTrue(link.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", link.getMeta().get("link"));
    }

    @Test
    public void testLinkBeforeBoldText() {
        FormattedText result = MarkdownParser.parse("[link](http://example.com) and **bold**.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("link and bold.", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format link = line.getFormatting().get(0);
        assertEquals(0, link.getIndex());
        assertEquals(4, link.getLength());
        assertTrue(link.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", link.getMeta().get("link"));

        FormattedLine.Format bold = line.getFormatting().get(1);
        assertEquals(9, bold.getIndex());
        assertEquals(4, bold.getLength());
        assertTrue(bold.getFormats().contains(FormatType.BLD));
    }

    @Test
    public void testLinkInListItem() {
        FormattedText result = MarkdownParser.parse("- Check [this](http://example.com) out");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.NLIST, block.getType());

        FormattedLine line = block.getLines().get(0);
        assertEquals("Check this out", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(4, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", format.getMeta().get("link"));
    }

    @Test
    public void testLinkInHeading() {
        FormattedText result = MarkdownParser.parse("# Check [the docs](http://docs.com)");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H1, block.getType());

        FormattedLine line = block.getLines().get(0);
        assertEquals("Check the docs", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(8, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("http://docs.com", format.getMeta().get("link"));
    }

    @Test
    public void testIncompleteLinkNotParsed() {
        // Missing closing parenthesis
        FormattedText result = MarkdownParser.parse("This is [incomplete](http://example.com text");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is [incomplete](http://example.com text", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testBracketsWithoutLinkNotParsed() {
        // Brackets not followed by parentheses
        FormattedText result = MarkdownParser.parse("This is [not a link] here.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is [not a link] here.", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testLinkWithEmptyLabel() {
        FormattedText result = MarkdownParser.parse("Click [](http://example.com) here.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Click  here.", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(0, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", format.getMeta().get("link"));
    }

    @Test
    public void testLinkWithEmptyUrl() {
        FormattedText result = MarkdownParser.parse("Click [label]() here.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Click label here.", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(5, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("", format.getMeta().get("link"));
    }

    @Test
    public void testLinkWithSpecialCharactersInUrl() {
        FormattedText result = MarkdownParser.parse("See [docs](http://example.com/path?query=value&other=1#anchor).");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("See docs.", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals("http://example.com/path?query=value&other=1#anchor", format.getMeta().get("link"));
    }

    @Test
    public void testConsecutiveLinks() {
        FormattedText result = MarkdownParser.parse("[one](http://one.com)[two](http://two.com)");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("onetwo", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format format1 = line.getFormatting().get(0);
        assertEquals(0, format1.getIndex());
        assertEquals(3, format1.getLength());
        assertEquals("http://one.com", format1.getMeta().get("link"));

        FormattedLine.Format format2 = line.getFormatting().get(1);
        assertEquals(3, format2.getIndex());
        assertEquals(3, format2.getLength());
        assertEquals("http://two.com", format2.getMeta().get("link"));
    }

    @Test
    public void testLinkOnlyLine() {
        FormattedText result = MarkdownParser.parse("[Click me](http://example.com)");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Click me", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(0, format.getIndex());
        assertEquals(8, format.getLength());
        assertTrue(format.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", format.getMeta().get("link"));
    }

    /************************************************************************
     * Variable parsing tests
     ************************************************************************/

    @Test
    public void testSimpleVariable() {
        FormattedText result = MarkdownParser.parse("Hello {{name}}!");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Hello !", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(0, format.getLength());
        assertEquals("name", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableAtStart() {
        FormattedText result = MarkdownParser.parse("{{greeting}} World!");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals(" World!", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(0, format.getIndex());
        assertEquals(0, format.getLength());
        assertEquals("greeting", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableAtEnd() {
        FormattedText result = MarkdownParser.parse("Hello {{name}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Hello ", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(6, format.getIndex());
        assertEquals(0, format.getLength());
        assertEquals("name", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testMultipleVariables() {
        FormattedText result = MarkdownParser.parse("Dear {{title}} {{name}},");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Dear  ,", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format format1 = line.getFormatting().get(0);
        assertEquals(5, format1.getIndex());
        assertEquals(0, format1.getLength());
        assertEquals("title", format1.getMeta().get(FormattedLine.META_VARIABLE));

        FormattedLine.Format format2 = line.getFormatting().get(1);
        assertEquals(6, format2.getIndex());
        assertEquals(0, format2.getLength());
        assertEquals("name", format2.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableWithMetadata() {
        FormattedText result = MarkdownParser.parse("Value: {{amount;format=currency;precision=2}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Value: ", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(7, format.getIndex());
        assertEquals(0, format.getLength());
        assertEquals("amount", format.getMeta().get(FormattedLine.META_VARIABLE));
        assertEquals("currency", format.getMeta().get("format"));
        assertEquals("2", format.getMeta().get("precision"));
    }

    @Test
    public void testVariableNameWithSpecialChars() {
        // Test variable name with dashes, underscores, periods, dollar signs, and colons
        FormattedText result = MarkdownParser.parse("{{user-name_field.value$type:id}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals("user-name_field.value$type:id", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableWithBoldText() {
        FormattedText result = MarkdownParser.parse("This is **bold** and {{variable}} text.");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("This is bold and  text.", line.getText());
        assertEquals(2, line.getFormatting().size());

        // Bold formatting
        FormattedLine.Format bold = line.getFormatting().get(0);
        assertEquals(8, bold.getIndex());
        assertEquals(4, bold.getLength());
        assertTrue(bold.getFormats().contains(FormatType.BLD));

        // Variable
        FormattedLine.Format variable = line.getFormatting().get(1);
        assertEquals(17, variable.getIndex());
        assertEquals(0, variable.getLength());
        assertEquals("variable", variable.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableWithLink() {
        FormattedText result = MarkdownParser.parse("Hello {{name}}, visit [our site](http://example.com).");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("Hello , visit our site.", line.getText());
        assertEquals(2, line.getFormatting().size());

        // Variable
        FormattedLine.Format variable = line.getFormatting().get(0);
        assertEquals(6, variable.getIndex());
        assertEquals(0, variable.getLength());
        assertEquals("name", variable.getMeta().get(FormattedLine.META_VARIABLE));

        // Link
        FormattedLine.Format link = line.getFormatting().get(1);
        assertEquals(14, link.getIndex());
        assertEquals(8, link.getLength());
        assertTrue(link.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", link.getMeta().get("link"));
    }

    @Test
    public void testInvalidVariableNameIgnored() {
        // Variable names cannot contain spaces
        FormattedText result = MarkdownParser.parse("Hello {{invalid name}}!");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // Invalid variable should be left as literal text
        assertEquals("Hello {{invalid name}}!", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testEmptyVariableIgnored() {
        FormattedText result = MarkdownParser.parse("Hello {{}}!");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // Empty variable should be left as literal text
        assertEquals("Hello {{}}!", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testUnclosedVariableIgnored() {
        FormattedText result = MarkdownParser.parse("Hello {{name!");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // Unclosed variable should be left as literal text
        assertEquals("Hello {{name!", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testVariableInListItem() {
        FormattedText result = MarkdownParser.parse("- Item for {{user}}");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.NLIST, block.getType());

        FormattedLine line = block.getLines().get(0);
        assertEquals("Item for ", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals("user", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableInHeading() {
        FormattedText result = MarkdownParser.parse("# Welcome {{user}}");

        assertEquals(1, result.getBlocks().size());
        FormattedBlock block = result.getBlocks().get(0);
        assertEquals(BlockType.H1, block.getType());

        FormattedLine line = block.getLines().get(0);
        assertEquals("Welcome ", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals("user", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableMetadataWithPeriodInFieldName() {
        FormattedText result = MarkdownParser.parse("{{value;field.name=test}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals("value", format.getMeta().get(FormattedLine.META_VARIABLE));
        assertEquals("test", format.getMeta().get("field.name"));
    }

    @Test
    public void testVariableMetadataWithSpecialCharsInValue() {
        FormattedText result = MarkdownParser.parse("{{date;format=yyyy-MM-dd HH:mm:ss}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals("date", format.getMeta().get(FormattedLine.META_VARIABLE));
        assertEquals("yyyy-MM-dd HH:mm:ss", format.getMeta().get("format"));
    }

    @Test
    public void testVariableSequenceReturnsVariable() {
        FormattedText result = MarkdownParser.parse("Hello {{name}}!");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        var segments = line.sequence();

        assertEquals(3, segments.size());

        // "Hello "
        assertEquals("Hello ", segments.get(0).text());
        assertFalse(segments.get(0).variable());

        // Variable marker - text() returns the variable name for variable segments
        // Note: META_VARIABLE is excluded from meta() - use text() for the variable name
        assertEquals("name", segments.get(1).text());
        assertTrue(segments.get(1).variable());

        // "!"
        assertEquals("!", segments.get(2).text());
        assertFalse(segments.get(2).variable());
    }

    @Test
    public void testVariableWithInvalidChars() {
        // Variable names cannot contain special characters like @, #, etc.
        FormattedText result = MarkdownParser.parse("{{invalid@name}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // Should be treated as literal text since @ is not a valid variable character
        assertEquals("{{invalid@name}}", line.getText());
        assertTrue(line.getFormatting().isEmpty());
    }

    @Test
    public void testConsecutiveVariables() {
        FormattedText result = MarkdownParser.parse("{{first}}{{second}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("", line.getText());
        assertEquals(2, line.getFormatting().size());

        FormattedLine.Format format1 = line.getFormatting().get(0);
        assertEquals(0, format1.getIndex());
        assertEquals("first", format1.getMeta().get(FormattedLine.META_VARIABLE));

        FormattedLine.Format format2 = line.getFormatting().get(1);
        assertEquals(0, format2.getIndex());
        assertEquals("second", format2.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableOnlyLine() {
        FormattedText result = MarkdownParser.parse("{{username}}");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        assertEquals("", line.getText());
        assertEquals(1, line.getFormatting().size());

        FormattedLine.Format format = line.getFormatting().get(0);
        assertEquals(0, format.getIndex());
        assertEquals(0, format.getLength());
        assertEquals("username", format.getMeta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testVariableInsideLink() {
        FormattedText result = MarkdownParser.parse("Hello, visit [our site named {{site-name}}](http://example.com).");

        FormattedLine line = result.getBlocks().get(0).getLines().get(0);
        // The link label includes the variable syntax literally since variables inside links
        // are not processed (the link region excludes variable parsing).
        assertEquals("Hello, visit our site named {{site-name}}.", line.getText());
        assertEquals(1, line.getFormatting().size());

        // Should have a link format containing the literal {{site-name}} in the label.
        FormattedLine.Format link = line.getFormatting().get(0);
        assertEquals(13, link.getIndex());
        assertEquals(28, link.getLength()); // "our site named {{site-name}}"
        assertTrue(link.getFormats().contains(FormatType.A));
        assertEquals("http://example.com", link.getMeta().get("link"));
    }
}
