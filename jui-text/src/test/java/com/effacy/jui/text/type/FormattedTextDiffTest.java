/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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
package com.effacy.jui.text.type;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

public class FormattedTextDiffTest {

    /**
     * The diff of a document with itself marks nothing and reproduces the document.
     */
    @Test
    public void identical() {
        FormattedText a = FormattedText.markdown("# Title\n\nOne.\n\nTwo.");
        FormattedTextDiff.Result result = FormattedTextDiff.diff(a, FormattedText.markdown("# Title\n\nOne.\n\nTwo."));

        Assertions.assertTrue(result.identical());
        Assertions.assertEquals(0, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(states(a), states(result.document()));
        Assertions.assertEquals("# Title\n\nOne.\n\nTwo.", result.document().toMarkdown());
    }

    /**
     * A block inserted in the middle is the only block marked; the blocks either side of
     * it are matched rather than being reported as replaced.
     */
    @Test
    public void insertion() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("One.\n\nTwo."),
            FormattedText.markdown("One.\n\nMiddle.\n\nTwo."));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(List.of("-", FormattedTextDiff.ADDED, "-"), states(result.document()));
        Assertions.assertEquals("Middle.", text(result.document(), 1));
    }

    /**
     * A deleted block is put back in place, marked, so the diff document reads in the
     * order the old document did.
     */
    @Test
    public void deletion() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("One.\n\nMiddle.\n\nTwo."),
            FormattedText.markdown("One.\n\nTwo."));

        Assertions.assertEquals(0, result.added());
        Assertions.assertEquals(1, result.removed());
        Assertions.assertEquals(List.of("-", FormattedTextDiff.REMOVED, "-"), states(result.document()));
        Assertions.assertEquals("Middle.", text(result.document(), 1));
    }

    /**
     * A reworded block is one block, marked word by word: the block reads as the new
     * text with the word that went struck through beside the word that came.
     */
    @Test
    public void rewording() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("# Title\n\nThe old wording is here.\n\nTail."),
            FormattedText.markdown("# Title\n\nThe new wording is here.\n\nTail."));

        Assertions.assertEquals(0, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(1, result.changed());
        Assertions.assertEquals(List.of("-", FormattedTextDiff.CHANGED, "-"), states(result.document()));

        FormattedLine line = result.document().getBlocks().get(1).getLines().get(0);
        Assertions.assertEquals("The old new wording is here.", line.getText());
        Assertions.assertEquals("old", marked(line, FormatType.DEL));
        Assertions.assertEquals("new", marked(line, FormatType.INS));
    }

    /**
     * Only the words that moved are marked — the words either side of them are not
     * touched, and carry no marking format at all.
     */
    @Test
    public void rewordingMarksOnlyTheWords() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("Payments settle within three working days."),
            FormattedText.markdown("Payments settle within two working days."));

        FormattedLine line = result.document().getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("three", marked(line, FormatType.DEL));
        Assertions.assertEquals("two", marked(line, FormatType.INS));
        // Everything else is plain.
        for (FormattedLine.Format format : line.getFormatting()) {
            String covered = line.getText().substring(format.getIndex(), format.getIndex() + format.getLength());
            Assertions.assertTrue(covered.equals("three") || covered.equals("two"),
                "unexpected formatting over '" + covered + "'");
        }
    }

    /**
     * Words appended to a block are marked as an insertion; nothing is marked removed.
     */
    @Test
    public void wordsAppended() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("The scope is payments."),
            FormattedText.markdown("The scope is payments and payouts."));

        Assertions.assertEquals(1, result.changed());
        FormattedLine line = result.document().getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("The scope is payments and payouts.", line.getText());
        Assertions.assertEquals("", marked(line, FormatType.DEL));
        // The full stop is its own token, so it is not dragged into the insertion.
        Assertions.assertEquals("and payouts", marked(line, FormatType.INS));
    }

    /**
     * Two blocks with nothing in common are not forced into one: below the pairing
     * threshold they stay a removal and an addition, which reads better than a block in
     * which every word is marked.
     */
    @Test
    public void unrelatedBlocksAreNotPaired() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("# Title\n\nSettlement runs overnight in Sydney.\n\nTail."),
            FormattedText.markdown("# Title\n\nRefunds are approved by the merchant.\n\nTail."));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(1, result.removed());
        Assertions.assertEquals(0, result.changed());
        Assertions.assertEquals(
            List.of("-", FormattedTextDiff.REMOVED, FormattedTextDiff.ADDED, "-"),
            states(result.document()));
    }

    /**
     * Formatting travels with the word: an emphasised word that is inserted is marked
     * inserted and stays emphasised.
     */
    @Test
    public void insertedWordKeepsItsFormatting() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("The scope is payments only."),
            FormattedText.markdown("The scope is **payments** only."));

        Assertions.assertEquals(1, result.changed());
        FormattedLine line = result.document().getBlocks().get(0).getLines().get(0);
        boolean boldInsert = false;
        for (FormattedLine.Format format : line.getFormatting()) {
            List<FormatType> formats = format.getFormats();
            if (formats.contains(FormatType.INS) && formats.contains(FormatType.BLD))
                boldInsert = true;
        }
        Assertions.assertTrue(boldInsert, "the inserted word should still be bold");
        Assertions.assertEquals("payments", marked(line, FormatType.INS));
        Assertions.assertEquals("payments", marked(line, FormatType.DEL));
    }

    /**
     * A link is one thing: re-pointing it marks the whole link rather than picking
     * through its label, and the merged line keeps both hrefs.
     */
    @Test
    public void linkRepointedIsAtomic() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("See [the policy](https://a.example) for detail."),
            FormattedText.markdown("See [the policy](https://b.example) for detail."));

        Assertions.assertEquals(1, result.changed());
        FormattedLine line = result.document().getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("the policy", marked(line, FormatType.DEL));
        Assertions.assertEquals("the policy", marked(line, FormatType.INS));
        List<String> hrefs = new ArrayList<>();
        for (FormattedLine.Format format : line.getFormatting()) {
            String href = format.getMeta().get(FormattedLine.META_LINK);
            if (href != null)
                hrefs.add(href);
        }
        Assertions.assertEquals(List.of("https://a.example", "https://b.example"), hrefs);
    }

    /**
     * A multi-line block marks the line that changed and leaves the others alone.
     */
    @Test
    public void multiLineBlockMarksOnlyTheChangedLine() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("First line here\nSecond line here"),
            FormattedText.markdown("First line here\nSecond line altered"));

        Assertions.assertEquals(1, result.changed());
        FormattedBlock block = result.document().getBlocks().get(0);
        Assertions.assertEquals(2, block.getLines().size());
        Assertions.assertTrue(block.getLines().get(0).getFormatting().isEmpty(),
            "the unchanged line should carry no marking");
        Assertions.assertEquals("here", marked(block.getLines().get(1), FormatType.DEL));
        Assertions.assertEquals("altered", marked(block.getLines().get(1), FormatType.INS));
    }

    /**
     * The inline marks have no markdown representation, so a diff document that escapes
     * into a save loses its marking rather than persisting it as content.
     */
    @Test
    public void marksDoNotSerialiseToMarkdown() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("The old wording is here."),
            FormattedText.markdown("The new wording is here."));

        Assertions.assertEquals("The old new wording is here.", result.document().toMarkdown());
    }

    /**
     * A move is reported as a removal and an addition (no move detection), but the block
     * that did not move is still matched.
     */
    @Test
    public void move() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("A.\n\nB.\n\nC."),
            FormattedText.markdown("B.\n\nC.\n\nA."));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(1, result.removed());
        Assertions.assertEquals(
            List.of(FormattedTextDiff.REMOVED, "-", "-", FormattedTextDiff.ADDED),
            states(result.document()));
    }

    /**
     * Formatting is part of the identity of both a block and a word: emphasising a word
     * is a change, and it is marked on that word rather than over the whole block.
     */
    @Test
    public void formattingOnlyChange() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("A word here."),
            FormattedText.markdown("A **word** here."));

        Assertions.assertEquals(0, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(1, result.changed());

        FormattedLine line = result.document().getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("word", marked(line, FormatType.DEL));
        Assertions.assertEquals("word", marked(line, FormatType.INS));
    }

    /**
     * So is the block's type: the same words promoted to a heading are a change.
     */
    @Test
    public void typeChange() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("Overview"),
            FormattedText.markdown("## Overview"));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(1, result.removed());
        Assertions.assertEquals(BlockType.PARA, result.document().getBlocks().get(0).getType());
        Assertions.assertEquals(BlockType.H2, result.document().getBlocks().get(1).getType());
    }

    /**
     * A table is compared whole — one altered cell is the old table removed and the new
     * one added, with the nested rows and cells carried through intact.
     */
    @Test
    public void tableChangedWhole() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("| A | B |\n| --- | --- |\n| 1 | 2 |"),
            FormattedText.markdown("| A | B |\n| --- | --- |\n| 1 | 9 |"));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(1, result.removed());
        Assertions.assertEquals(2, result.document().getBlocks().size());
        FormattedBlock removed = result.document().getBlocks().get(0);
        Assertions.assertEquals(BlockType.TABLE, removed.getType());
        // Header row and body row, cells intact.
        Assertions.assertEquals(2, removed.getBlocks().size());
        Assertions.assertEquals(2, removed.getBlocks().get(1).getBlocks().size());
    }

    /**
     * An empty (or absent) old document makes everything an addition, which is the
     * comparison a first version is read against.
     */
    @Test
    public void againstNothing() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(null, FormattedText.markdown("One.\n\nTwo."));

        Assertions.assertEquals(2, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(
            List.of(FormattedTextDiff.ADDED, FormattedTextDiff.ADDED),
            states(result.document()));
    }

    /**
     * And an emptied document makes everything a removal.
     */
    @Test
    public void emptied() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(FormattedText.markdown("One.\n\nTwo."), null);

        Assertions.assertEquals(0, result.added());
        Assertions.assertEquals(2, result.removed());
        Assertions.assertEquals(
            List.of(FormattedTextDiff.REMOVED, FormattedTextDiff.REMOVED),
            states(result.document()));
    }

    /**
     * List items are blocks like any other: one added item among several is the only
     * thing marked.
     */
    @Test
    public void listItemAdded() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("- One\n- Three"),
            FormattedText.markdown("- One\n- Two\n- Three"));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(List.of("-", FormattedTextDiff.ADDED, "-"), states(result.document()));
    }

    /**
     * The diff document is assembled from two documents, so it carries no block
     * identifiers — nothing downstream should be able to mistake it for either.
     */
    @Test
    public void carriesNoBlockIds() {
        FormattedText from = FormattedText.markdown("One.\n\nTwo.");
        FormattedText to = FormattedText.markdown("One.\n\nThree.");
        // The parser assigns ids, so this is a real question.
        Assertions.assertNotNull(from.getBlocks().get(0).getId());

        for (FormattedBlock block : FormattedTextDiff.diff(from, to).document())
            Assertions.assertFalse(block.hasId());
    }

    /**
     * The source documents are not modified by the comparison (the diff document is
     * assembled from copies).
     */
    @Test
    public void sourcesUntouched() {
        FormattedText from = FormattedText.markdown("One.\n\nTwo.");
        FormattedText to = FormattedText.markdown("One.\n\nThree.");
        FormattedTextDiff.diff(from, to);

        for (FormattedBlock block : from)
            Assertions.assertNull(block.meta(FormattedTextDiff.META_DIFF));
        for (FormattedBlock block : to)
            Assertions.assertNull(block.meta(FormattedTextDiff.META_DIFF));
    }

    /**
     * A page as it is actually edited: a heading reworded, a paragraph left alone, a
     * sentence tightened, a list item added and another dropped. Each is reported at the
     * granularity it happened at rather than everything being reported as a replacement.
     */
    @Test
    public void aPageAsItIsActuallyEdited() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("""
                ## Settlement model

                Funds are held by the acquirer until settlement.

                Settlement runs overnight on business days.

                - Card payments
                - Direct debits
                """),
            FormattedText.markdown("""
                ## Settlement and payout model

                Funds are held by the acquirer until settlement.

                Settlement runs overnight on business days.

                - Card payments
                - Direct debits (AU only)
                - Wallet payments
                """));

        // The heading and one list item were edited; an item was added; the two
        // paragraphs nobody touched are not marked at all.
        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(0, result.removed());
        Assertions.assertEquals(2, result.changed());
        Assertions.assertEquals(
            List.of(FormattedTextDiff.CHANGED, "-", "-", "-", FormattedTextDiff.CHANGED, FormattedTextDiff.ADDED),
            states(result.document()));

        FormattedLine heading = result.document().getBlocks().get(0).getLines().get(0);
        Assertions.assertEquals("Settlement and payout model", heading.getText());
        Assertions.assertEquals("and payout", marked(heading, FormatType.INS));
        Assertions.assertEquals("", marked(heading, FormatType.DEL));

        FormattedLine item = result.document().getBlocks().get(4).getLines().get(0);
        Assertions.assertEquals("Direct debits (AU only)", item.getText());
        Assertions.assertEquals("( AU only )", marked(item, FormatType.INS));
        Assertions.assertEquals("", marked(item, FormatType.DEL));
    }

    /**
     * A list item replaced by an unrelated one is not forced into a word-level marking:
     * below the threshold it stays a removal and an addition.
     */
    @Test
    public void unrelatedListItemIsNotPaired() {
        FormattedTextDiff.Result result = FormattedTextDiff.diff(
            FormattedText.markdown("- Card payments\n- Direct debits"),
            FormattedText.markdown("- Card payments\n- Wallet payments"));

        Assertions.assertEquals(1, result.added());
        Assertions.assertEquals(1, result.removed());
        Assertions.assertEquals(0, result.changed());
    }

    /************************************************************************
     * Helpers
     ************************************************************************/

    /**
     * The diff state of each block in document order, an unmarked block being
     * {@code "-"}.
     */
    private List<String> states(FormattedText text) {
        List<String> states = new ArrayList<>();
        for (FormattedBlock block : text) {
            String state = block.meta(FormattedTextDiff.META_DIFF);
            states.add((state == null) ? "-" : state);
        }
        return states;
    }

    private String text(FormattedText document, int index) {
        return document.getBlocks().get(index).flatten();
    }

    /**
     * The text of a line carrying the given mark, runs joined by a space — what a reader
     * would see struck through (or underlined) in that line.
     */
    private String marked(FormattedLine line, FormatType mark) {
        StringBuilder sb = new StringBuilder();
        for (FormattedLine.Format format : line.getFormatting()) {
            if (!format.getFormats().contains(mark))
                continue;
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(line.getText().substring(format.getIndex(), format.getIndex() + format.getLength()));
        }
        return sb.toString();
    }
}
