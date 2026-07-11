package com.effacy.jui.text.type.builder.markdown;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.builder.FormattedTextBuilder;

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
    public void testFenceRoundTrip() {
        String md = "```mermaid\ngraph TD;\nA-->B;\n```";
        FormattedText ft = new MarkdownParser().fence(info -> "mermaid".equals(info))
            .parse(new FormattedTextBuilder(), md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    @Test
    public void testBlockQuoteRoundTrip() {
        String md = "> **What this is.** A short note\n> that wraps onto a second line.";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    @Test
    public void testBlockQuoteWithBlankLineRoundTrip() {
        String md = "> First paragraph.\n>\n> Second paragraph.";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
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

    /**
     * Nested <em>ordered</em> lists number per level: a sublist restarts at 1 and the parent
     * resumes its own count afterwards (not one running counter across all levels).
     */
    @Test
    public void testNestedOrderedListNumbering() {
        FormattedTextBuilder b = new FormattedTextBuilder();
        olistItem(b, "Item1", 0);
        olistItem(b, "222", 0);
        olistItem(b, "sddasda", 0);
        olistItem(b, "asdas", 1);
        olistItem(b, "dasdas", 1);
        olistItem(b, "asdas", 0);
        olistItem(b, "333", 1);
        olistItem(b, "444", 0);

        String out = MarkdownSerializer.serialize(b.result());
        assertEquals(
            "1. Item1\n" +
            "2. 222\n" +
            "3. sddasda\n" +
            "  1. asdas\n" +
            "  2. dasdas\n" +
            "4. asdas\n" +
            "  1. 333\n" +
            "5. 444",
            out);
    }

    /**
     * A numbered list with an <em>unordered</em> sublist nested inside it: the sublist uses
     * bullets and the parent numbering resumes (3, 4) after it, rather than restarting.
     */
    @Test
    public void testOrderedListWithUnorderedSublist() {
        FormattedTextBuilder b = new FormattedTextBuilder();
        listItem(b, FormattedBlock.BlockType.OLIST, "Step one", 0);
        listItem(b, FormattedBlock.BlockType.OLIST, "Step two", 0);
        listItem(b, FormattedBlock.BlockType.NLIST, "note a", 1);
        listItem(b, FormattedBlock.BlockType.NLIST, "note b", 1);
        listItem(b, FormattedBlock.BlockType.OLIST, "Step three", 0);
        listItem(b, FormattedBlock.BlockType.OLIST, "Step four", 0);

        assertEquals(
            "1. Step one\n" +
            "2. Step two\n" +
            "  - note a\n" +
            "  - note b\n" +
            "3. Step three\n" +
            "4. Step four",
            MarkdownSerializer.serialize(b.result()));
    }

    /**
     * A bullet list with an <em>ordered</em> sublist nested inside it: the sublist numbers
     * 1, 2 (restarting per level) while the parent stays bulleted.
     */
    @Test
    public void testUnorderedListWithOrderedSublist() {
        FormattedTextBuilder b = new FormattedTextBuilder();
        listItem(b, FormattedBlock.BlockType.NLIST, "Fruit", 0);
        listItem(b, FormattedBlock.BlockType.OLIST, "apple", 1);
        listItem(b, FormattedBlock.BlockType.OLIST, "pear", 1);
        listItem(b, FormattedBlock.BlockType.NLIST, "Veg", 0);
        listItem(b, FormattedBlock.BlockType.OLIST, "carrot", 1);

        assertEquals(
            "- Fruit\n" +
            "  1. apple\n" +
            "  2. pear\n" +
            "- Veg\n" +
            "  1. carrot",
            MarkdownSerializer.serialize(b.result()));
    }

    /**
     * Three levels with markers changing per level and an ordered level resuming after a
     * deeper sublist: outer ordered (1, 2), inner ordered restarts (1, 2) then resumes (3).
     */
    @Test
    public void testMixedNestingThreeLevels() {
        FormattedTextBuilder b = new FormattedTextBuilder();
        listItem(b, FormattedBlock.BlockType.OLIST, "A", 0);
        listItem(b, FormattedBlock.BlockType.OLIST, "B", 1);
        listItem(b, FormattedBlock.BlockType.NLIST, "b-note", 2);
        listItem(b, FormattedBlock.BlockType.OLIST, "C", 1);
        listItem(b, FormattedBlock.BlockType.OLIST, "D", 0);

        assertEquals(
            "1. A\n" +
            "  1. B\n" +
            "    - b-note\n" +
            "  2. C\n" +
            "2. D",
            MarkdownSerializer.serialize(b.result()));
    }

    private static void olistItem(FormattedTextBuilder b, String text, int indent) {
        listItem(b, FormattedBlock.BlockType.OLIST, text, indent);
    }

    private static void listItem(FormattedTextBuilder b, FormattedBlock.BlockType type, String text, int indent) {
        b.startBlock(type);
        if (indent > 0)
            b.meta("indent", String.valueOf(indent));
        b.startLine();
        b.text(text);
        b.endLine();
        b.endBlock(type);
    }

    /**
     * Deeply nested lists authored externally (4-space steps) parse to the right relative
     * levels, serialize to the canonical 2-space-per-level form, and re-parse identically —
     * so nesting survives a Rich ⇄ Markdown round-trip at any depth.
     */
    @Test
    public void testNestedListRoundTrip() {
        String md = "- A\n    - B\n        - C\n    - B2\n- A2";
        FormattedText ft = FormattedText.markdown(md);

        List<FormattedBlock> blocks = ft.getBlocks();
        assertEquals(5, blocks.size());
        assertEquals(0, blocks.get(0).getIndent());   // A
        assertEquals(1, blocks.get(1).getIndent());   // B
        assertEquals(2, blocks.get(2).getIndent());   // C
        assertEquals(1, blocks.get(3).getIndent());   // B2
        assertEquals(0, blocks.get(4).getIndent());   // A2

        // Serialisation normalises to 2 spaces per level.
        String out = MarkdownSerializer.serialize(ft);
        assertEquals("- A\n  - B\n    - C\n  - B2\n- A2", out);

        // Re-parsing the serialised form yields identical levels (stable round-trip).
        List<FormattedBlock> reblocks = FormattedText.markdown(out).getBlocks();
        assertEquals(5, reblocks.size());
        assertEquals(0, reblocks.get(0).getIndent());
        assertEquals(1, reblocks.get(1).getIndent());
        assertEquals(2, reblocks.get(2).getIndent());
        assertEquals(1, reblocks.get(3).getIndent());
        assertEquals(0, reblocks.get(4).getIndent());
    }

    @Test
    public void testLink() {
        FormattedText ft = FormattedText.markdown("Click [here](https://example.com) now");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("[here](https://example.com)"));
    }

    /**
     * A link that runs to the end of a line still closes correctly (its {@code ](url)} is
     * emitted after the final character rather than being dropped).
     */
    @Test
    public void testLinkAtEndOfLine() {
        FormattedText ft = FormattedText.markdown("See [the docs](https://example.com/docs)");
        assertEquals("See [the docs](https://example.com/docs)", MarkdownSerializer.serialize(ft));
    }

    /**
     * An emphasised span containing a link round-trips exactly — the emphasis stays open across
     * the link rather than being split around it, and the link is not duplicated. Regression for
     * a parser bug where the whole span (raw link syntax included) was emitted as one run and the
     * links were then re-processed, doubling the content on the second link onward.
     */
    @Test
    public void testItalicWrappingLinks() {
        String md = "*If you are seeking advice refer to [Development](#development) under [Operating scenarios](#operating-scenarios).*";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /** A single link inside emphasis round-trips as one emphasised link (no split, no dup). */
    @Test
    public void testItalicWrappingSingleLink() {
        String md = "*see [here](#x) now*";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /** Bold wrapping a link round-trips exactly. */
    @Test
    public void testBoldWrappingLink() {
        String md = "**click [here](https://example.com) please**";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /**
     * The rich⇄markdown round trip is idempotent for emphasis-wrapping-links: a second
     * parse/serialize cycle produces the same markdown as the first (no drift, no duplication).
     */
    @Test
    public void testItalicWrappingLinksIdempotent() {
        String md = "*a [x](#x) b [y](#y) c*";
        String once = MarkdownSerializer.serialize(FormattedText.markdown(md));
        String twice = MarkdownSerializer.serialize(FormattedText.markdown(once));
        assertEquals(once, twice);
        assertEquals(md, once);
    }

    @Test
    public void testTable() {
        FormattedText ft = FormattedText.markdown("| A | B |\n| --- | --- |\n| 1 | 2 |");
        String result = MarkdownSerializer.serialize(ft);
        assertTrue(result.contains("|"));
        assertTrue(result.contains("---"));
    }

    /** A basic table round-trips exactly (header, separator, body rows). */
    @Test
    public void testTableRoundTrip() {
        String md = "| A | B |\n| --- | --- |\n| 1 | 2 |\n| 3 | 4 |";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(FormattedBlock.BlockType.TABLE, ft.getBlocks().get(0).getType());
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /** Column alignment (left / centre / right) survives the round-trip. */
    @Test
    public void testTableAlignmentRoundTrip() {
        // Left is emitted as "---" (the default), centre as ":---:", right as "---:".
        String md = "| L | C | R |\n| --- | :---: | ---: |\n| a | b | c |";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals("L,C,R", ft.getBlocks().get(0).meta("align"));
        String out = MarkdownSerializer.serialize(ft);
        assertEquals(md, out);
        // And the alignment is still there after a second parse.
        assertEquals("L,C,R", FormattedText.markdown(out).getBlocks().get(0).meta("align"));
    }

    /** An explicit left marker (":---") normalises to "---" but stays left-aligned. */
    @Test
    public void testTableExplicitLeftNormalises() {
        FormattedText ft = FormattedText.markdown("| A | B |\n| :--- | ---: |\n| 1 | 2 |");
        assertEquals("| A | B |\n| --- | ---: |\n| 1 | 2 |", MarkdownSerializer.serialize(ft));
    }

    /** Inline formatting inside cells round-trips. */
    @Test
    public void testTableWithFormattingRoundTrip() {
        String md = "| **Name** | Value |\n| --- | --- |\n| `id` | *x* |";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /** Empty cells are preserved as blank columns. */
    @Test
    public void testTableEmptyCellsRoundTrip() {
        String md = "| A | B | C |\n| --- | --- | --- |\n| 1 |  | 3 |";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /** A single-column table round-trips. */
    @Test
    public void testTableSingleColumnRoundTrip() {
        String md = "| Item |\n| --- |\n| one |\n| two |";
        FormattedText ft = FormattedText.markdown(md);
        assertEquals(md, MarkdownSerializer.serialize(ft));
    }

    /** A table with many body rows: the separator appears once (after the header only) and
     *  every data row round-trips in order. */
    @Test
    public void testTableManyRowsRoundTrip() {
        String md = "| Name | Age | City |\n"
                  + "| --- | ---: | :---: |\n"
                  + "| Alice | 30 | London |\n"
                  + "| Bob | 25 | Paris |\n"
                  + "| Carol | 41 | Berlin |\n"
                  + "| Dave | 19 | Rome |";
        FormattedText ft = FormattedText.markdown(md);

        FormattedBlock table = ft.getBlocks().get(0);
        assertEquals(FormattedBlock.BlockType.TABLE, table.getType());
        assertEquals(5, table.getBlocks().size());   // 1 header row + 4 body rows (no separator row in the model)
        assertEquals("L,R,C", table.meta("align"));

        assertEquals(md, MarkdownSerializer.serialize(ft));
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

    @Test
    public void testImageRoundTrip() {
        String md = "![](http://example.com/a.png)";
        assertEquals(md, MarkdownSerializer.serialize(FormattedText.markdown(md)));
    }

    @Test
    public void testImageWithSizeRoundTrip() {
        String md = "![](http://example.com/a.png){width=200 height=150}";
        assertEquals(md, MarkdownSerializer.serialize(FormattedText.markdown(md)));
    }

    @Test
    public void testImageWithAlignRoundTrip() {
        String md = "![](http://example.com/a.png){align=center}";
        assertEquals(md, MarkdownSerializer.serialize(FormattedText.markdown(md)));
    }

    @Test
    public void testImageWithAllAttributesRoundTrip() {
        String md = "![alt text](http://example.com/a.png){width=320 height=240 align=right margin=12}";
        assertEquals(md, MarkdownSerializer.serialize(FormattedText.markdown(md)));
    }

    @Test
    public void testImageWithMarginRoundTrip() {
        String md = "![](http://example.com/a.png){margin=8}";
        assertEquals(md, MarkdownSerializer.serialize(FormattedText.markdown(md)));
    }
}
