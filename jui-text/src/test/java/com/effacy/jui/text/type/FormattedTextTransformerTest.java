package com.effacy.jui.text.type;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedTextTransformer.BatchResult;
import com.effacy.jui.text.type.FormattedTextTransformer.FindReplaceEdit;
import com.effacy.jui.text.type.FormattedTextTransformer.ReplaceResult;
import com.effacy.jui.text.type.builder.markdown.MarkdownSerializer;

public class FormattedTextTransformerTest {

    /************************************************************************
     * Splice tests (retained)
     ************************************************************************/

    @Test
    public void testSpliceReplace() {
        FormattedText doc = FormattedText.markdown("# Title\n\nParagraph one\n\nParagraph two\n\nParagraph three");
        FormattedText result = FormattedTextTransformer.splice(doc, 1, 3, "Replaced content");
        String md = MarkdownSerializer.serialize(result);
        assertTrue(md.contains("# Title"));
        assertTrue(md.contains("Replaced content"));
        assertTrue(md.contains("Paragraph three"));
        assertFalse(md.contains("Paragraph one"));
        assertFalse(md.contains("Paragraph two"));
    }

    @Test
    public void testSpliceInsert() {
        FormattedText doc = FormattedText.markdown("# Title\n\nParagraph one");
        FormattedText result = FormattedTextTransformer.splice(doc, 1, 1, "Inserted paragraph");
        String md = MarkdownSerializer.serialize(result);
        assertTrue(md.indexOf("Inserted") < md.indexOf("Paragraph one"));
    }

    @Test
    public void testSpliceDelete() {
        FormattedText doc = FormattedText.markdown("# Title\n\nTo be removed\n\nKeep this");
        FormattedText result = FormattedTextTransformer.splice(doc, 1, 2, (FormattedText) null);
        String md = MarkdownSerializer.serialize(result);
        assertFalse(md.contains("To be removed"));
        assertTrue(md.contains("Keep this"));
    }

    @Test
    public void testSplicePreservesOriginal() {
        FormattedText doc = FormattedText.markdown("# Title\n\nOriginal");
        FormattedTextTransformer.splice(doc, 1, 2, "Changed");
        assertTrue(MarkdownSerializer.serialize(doc).contains("Original"));
    }

    @Test
    public void testSpliceInvalidThrows() {
        FormattedText doc = FormattedText.markdown("# Title");
        assertThrows(IndexOutOfBoundsException.class, () ->
            FormattedTextTransformer.splice(doc, -1, 0, "x"));
        assertThrows(IndexOutOfBoundsException.class, () ->
            FormattedTextTransformer.splice(doc, 5, 5, "x"));
    }

    /************************************************************************
     * Find / replace tests
     ************************************************************************/

    @Test
    public void testFindAndReplaceSingleBlock() {
        FormattedText doc = FormattedText.markdown("# Title\n\nOld paragraph\n\nKeep this");
        ReplaceResult result = FormattedTextTransformer.findAndReplace(doc, "Old paragraph", "New paragraph");
        assertTrue(result.matched());
        String md = MarkdownSerializer.serialize(result.document());
        assertTrue(md.contains("New paragraph"));
        assertFalse(md.contains("Old paragraph"));
        assertTrue(md.contains("# Title"));
        assertTrue(md.contains("Keep this"));
    }

    @Test
    public void testFindAndReplaceMultipleBlocks() {
        FormattedText doc = FormattedText.markdown("# Title\n\nPara one\n\nPara two\n\nKeep this");
        ReplaceResult result = FormattedTextTransformer.findAndReplace(doc, "Para one\n\nPara two", "Replaced");
        assertTrue(result.matched());
        String md = MarkdownSerializer.serialize(result.document());
        assertTrue(md.contains("Replaced"));
        assertFalse(md.contains("Para one"));
        assertFalse(md.contains("Para two"));
        assertTrue(md.contains("Keep this"));
    }

    @Test
    public void testFindAndReplaceHeading() {
        FormattedText doc = FormattedText.markdown("# Old Title\n\nContent");
        ReplaceResult result = FormattedTextTransformer.findAndReplace(doc, "# Old Title", "# New Title");
        assertTrue(result.matched());
        String md = MarkdownSerializer.serialize(result.document());
        assertTrue(md.contains("# New Title"));
        assertFalse(md.contains("# Old Title"));
    }

    @Test
    public void testFindAndReplaceDelete() {
        FormattedText doc = FormattedText.markdown("# Title\n\nRemove me\n\nKeep");
        ReplaceResult result = FormattedTextTransformer.findAndReplace(doc, "Remove me", null);
        assertTrue(result.matched());
        String md = MarkdownSerializer.serialize(result.document());
        assertFalse(md.contains("Remove me"));
        assertTrue(md.contains("Keep"));
    }

    @Test
    public void testFindAndReplaceNoMatch() {
        FormattedText doc = FormattedText.markdown("# Title\n\nContent");
        ReplaceResult result = FormattedTextTransformer.findAndReplace(doc, "Not here", "Something");
        assertFalse(result.matched());
        assertEquals(doc, result.document());
    }

    @Test
    public void testFindAndReplaceWhitespaceNormalised() {
        FormattedText doc = FormattedText.markdown("# Title\n\nSome content here");
        // Extra whitespace in find should still match.
        ReplaceResult result = FormattedTextTransformer.findAndReplace(doc, "Some  content   here", "Replaced");
        assertTrue(result.matched());
    }

    @Test
    public void testFindAndReplacePreservesOriginal() {
        FormattedText doc = FormattedText.markdown("# Title\n\nOriginal");
        FormattedTextTransformer.findAndReplace(doc, "Original", "Changed");
        assertTrue(MarkdownSerializer.serialize(doc).contains("Original"));
    }

    /************************************************************************
     * Batch tests
     ************************************************************************/

    @Test
    public void testBatchMultipleEdits() {
        FormattedText doc = FormattedText.markdown("# Title\n\nFirst para\n\nSecond para\n\nThird para");
        BatchResult result = FormattedTextTransformer.batchFindAndReplace(doc, List.of(
            new FindReplaceEdit("First para", "Updated first"),
            new FindReplaceEdit("Third para", "Updated third")
        ));
        assertTrue(result.allMatched());
        String md = MarkdownSerializer.serialize(result.document());
        assertTrue(md.contains("Updated first"));
        assertTrue(md.contains("Second para"));
        assertTrue(md.contains("Updated third"));
        assertFalse(md.contains("First para"));
        assertFalse(md.contains("Third para"));
    }

    @Test
    public void testBatchInsertAtEnd() {
        FormattedText doc = FormattedText.markdown("# Title");
        BatchResult result = FormattedTextTransformer.batchFindAndReplace(doc, List.of(
            new FindReplaceEdit(null, "Appended content", "end")
        ));
        assertTrue(result.allMatched());
        String md = MarkdownSerializer.serialize(result.document());
        assertTrue(md.contains("# Title"));
        assertTrue(md.contains("Appended content"));
        assertTrue(md.indexOf("Title") < md.indexOf("Appended"));
    }

    @Test
    public void testBatchInsertAtBeginning() {
        FormattedText doc = FormattedText.markdown("# Title");
        BatchResult result = FormattedTextTransformer.batchFindAndReplace(doc, List.of(
            new FindReplaceEdit(null, "Prepended content", "beginning")
        ));
        assertTrue(result.allMatched());
        String md = MarkdownSerializer.serialize(result.document());
        assertTrue(md.indexOf("Prepended") < md.indexOf("Title"));
    }

    @Test
    public void testBatchPartialMatch() {
        FormattedText doc = FormattedText.markdown("# Title\n\nContent");
        BatchResult result = FormattedTextTransformer.batchFindAndReplace(doc, List.of(
            new FindReplaceEdit("Content", "Updated"),
            new FindReplaceEdit("Not here", "Fail")
        ));
        assertFalse(result.allMatched());
        assertEquals(List.of(true, false), result.outcomes());
        // First edit should still have been applied.
        assertTrue(MarkdownSerializer.serialize(result.document()).contains("Updated"));
    }

    @Test
    public void testBlockCount() {
        assertEquals(0, FormattedTextTransformer.blockCount(null));
        assertEquals(0, FormattedTextTransformer.blockCount(new FormattedText()));
        assertEquals(3, FormattedTextTransformer.blockCount(
            FormattedText.markdown("# Title\n\nPara one\n\nPara two")));
    }
}
