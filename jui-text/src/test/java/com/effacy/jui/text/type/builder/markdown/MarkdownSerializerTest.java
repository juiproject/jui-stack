package com.effacy.jui.text.type.builder.markdown;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedText;

/**
 * Tests for {@link MarkdownSerializer}. Most tests verify round-tripping
 * through {@link MarkdownParser} then {@link MarkdownSerializer}.
 */
public class MarkdownSerializerTest {

    @Test
    public void testNull() {
        assertEquals("", MarkdownSerializer.serialize(null));
    }

    @Test
    public void testEmpty() {
        assertEquals("", MarkdownSerializer.serialize(new FormattedText()));
    }

    @Test
    public void testPlainParagraph() {
        FormattedText ft = FormattedText.markdown("Hello world");
        assertEquals("Hello world", MarkdownSerializer.serialize(ft));
    }

    @Test
    public void testMultipleParagraphs() {
        FormattedText ft = FormattedText.markdown("First paragraph\n\nSecond paragraph");
        assertEquals("First paragraph\n\nSecond paragraph", MarkdownSerializer.serialize(ft));
    }

    @Test
    public void testHeadings() {
        FormattedText ft = FormattedText.markdown("# Heading 1\n\n## Heading 2\n\n### Heading 3");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("# Heading 1"));
        assertTrue(result.contains("## Heading 2"));
        assertTrue(result.contains("### Heading 3"));
    }

    @Test
    public void testBold() {
        FormattedText ft = FormattedText.markdown("This is **bold** text");
        String result = MarkdownSerializer.serialize(ft);
        assertEquals("This is **bold** text", result);
    }

    @Test
    public void testItalic() {
        FormattedText ft = FormattedText.markdown("This is *italic* text");
        String result = MarkdownSerializer.serialize(ft);
        assertEquals("This is *italic* text", result);
    }

    @Test
    public void testBoldAndItalic() {
        FormattedText ft = FormattedText.markdown("This is ***bold italic*** text");
        String result = MarkdownSerializer.serialize(ft);
        // Both bold and italic markers should be present.
        assertTrue(result.contains("**"));
        assertTrue(result.contains("*"));
        assertTrue(result.contains("bold italic"));
    }

    @Test
    public void testStrikethrough() {
        FormattedText ft = FormattedText.markdown("This is ~~struck~~ text");
        String result = MarkdownSerializer.serialize(ft);
        assertEquals("This is ~~struck~~ text", result);
    }

    @Test
    public void testInlineCode() {
        FormattedText ft = FormattedText.markdown("Use `code` here");
        String result = MarkdownSerializer.serialize(ft);
        assertEquals("Use `code` here", result);
    }

    @Test
    public void testCodeBlock() {
        FormattedText ft = FormattedText.markdown("```java\nint x = 1;\n```");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("```"));
        assertTrue(result.contains("int x = 1;"));
    }

    @Test
    public void testUnorderedList() {
        FormattedText ft = FormattedText.markdown("- Item 1\n- Item 2\n- Item 3");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("- Item 1"));
        assertTrue(result.contains("- Item 2"));
        assertTrue(result.contains("- Item 3"));
    }

    @Test
    public void testOrderedList() {
        FormattedText ft = FormattedText.markdown("1. First\n2. Second\n3. Third");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("1. First"));
        assertTrue(result.contains("2. Second"));
        assertTrue(result.contains("3. Third"));
    }

    @Test
    public void testLink() {
        FormattedText ft = FormattedText.markdown("Click [here](https://example.com) now");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("[here](https://example.com)"));
    }

    @Test
    public void testTable() {
        FormattedText ft = FormattedText.markdown("| A | B |\n| --- | --- |\n| 1 | 2 |");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("|"));
        assertTrue(result.contains("---"));
    }

    @Test
    public void testMixedContent() {
        String md = "# Title\n\nA paragraph with **bold** and *italic*.\n\n- Item one\n- Item two";
        FormattedText ft = FormattedText.markdown(md);
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("# Title"));
        assertTrue(result.contains("**bold**"));
        assertTrue(result.contains("*italic*"));
        assertTrue(result.contains("- Item one"));
    }

    @Test
    public void testNumberBlocksOff() {
        FormattedText ft = FormattedText.markdown("# Title\n\nParagraph");
        String result = new MarkdownSerializer().toMarkdown(ft);
        assertFalse(result.contains("[0]"));
        assertFalse(result.contains("[1]"));
    }

    @Test
    public void testNumberBlocksOn() {
        FormattedText ft = FormattedText.markdown("# Title\n\nParagraph one\n\nParagraph two");
        String result = new MarkdownSerializer().numberBlocks(true).toMarkdown(ft);
        assertTrue(result.contains("[0] # Title"));
        assertTrue(result.contains("[1] Paragraph one"));
        assertTrue(result.contains("[2] Paragraph two"));
    }

    @Test
    public void testNumberBlocksWithList() {
        FormattedText ft = FormattedText.markdown("# Title\n\n- Item one\n- Item two\n\nAfter list");
        String result = new MarkdownSerializer().numberBlocks(true).toMarkdown(ft);
        assertTrue(result.contains("[0] # Title"));
        // The list group gets a single block number.
        assertTrue(result.contains("[1] - Item one"));
        assertTrue(result.contains("[2] After list"));
    }

    @Test
    public void testStaticSerializeEqualsDefault() {
        FormattedText ft = FormattedText.markdown("# Hello\n\nWorld");
        assertEquals(MarkdownSerializer.serialize(ft), new MarkdownSerializer().toMarkdown(ft));
    }
}
