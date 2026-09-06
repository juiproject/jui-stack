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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.text.type.FormattedLine.Format;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.builder.markdown.MarkdownSerializer;

/**
 * Compares two {@link FormattedText} documents and expresses the difference as
 * a <em>third document</em>: the new document with the old document's departed
 * content put back in place, marked.
 * <p>
 * The point of returning a document rather than a list of edits is that it
 * renders: hand it to the ordinary renderer (or the {@code FText} fragment) and
 * the result is the document as it reads, marked up — rather than a view of the
 * markdown behind it.
 *
 * <h2>Two granularities</h2>
 *
 * <b>Blocks</b> are matched first, on their <em>markdown</em> (with type and
 * indent), which is the identity a document actually persists with: page
 * content is stored as markdown, so block identifiers do not survive from one
 * version to the next and matching has to be by content. Using the markdown
 * rather than the flattened text means a change of formatting — bolding a word,
 * re-pointing a link, resizing an image — is a change, which it is. A block
 * present on one side only carries {@link #ADDED} or {@link #REMOVED} in its
 * {@code diff} meta-data.
 * <p>
 * <b>Within a block</b>, a removed block and an added block that stand together
 * and are recognisably the same block edited — same type, no child blocks, and
 * more than half their words in common — are merged into one block carrying
 * {@link #CHANGED}, in which the words that went are marked
 * {@link FormatType#DEL} and the words that came {@link FormatType#INS}. So a
 * reworded sentence reads as a sentence with two words struck through and two
 * inserted, rather than as a paragraph replaced.
 * <p>
 * The same alignment runs at all three levels — blocks, then the lines within a
 * merged block, then the words within a merged line — so a block that is
 * several lines long marks only the lines that moved, and a line only the words.
 * Formatting travels with the word it is on: an inserted <b>bold</b> word is
 * marked inserted <em>and</em> stays bold, and links, images and variables are
 * atomic (they match or they do not; they are never split).
 * <p>
 * What is still compared whole: a block with children (a {@code TABLE}) — one
 * altered cell is the old table removed and the new one added — and blocks whose
 * raw content differs (an equation's or a diagram's source). Moves are not
 * detected: a block that moved is a removal and an addition.
 *
 * <h2>Usage</h2>
 *
 * <pre>
 * FormattedTextDiff.Result diff = FormattedTextDiff.diff(
 *     FormattedText.markdown(published),
 *     FormattedText.markdown(working));
 * if (diff.identical()) {
 *     // Nothing to show.
 * } else {
 *     FText.$(el, diff.document()).contentStyle(ContentStyle.document());
 * }
 * </pre>
 *
 * The returned document is for <b>reading only</b>. It interleaves content from
 * two versions, so serialising it back to markdown, or handing it to the
 * editor, would produce a document that never existed. (The inline marks are
 * shed by the markdown serializer, as {@link FormatType#CMT} is, so a document
 * that escapes into a save loses its marking rather than persisting it.)
 */
public class FormattedTextDiff {

    /**
     * Block meta-data key carrying a block's status in a diff document. Absent
     * on an unchanged block.
     */
    public static final String META_DIFF = "diff";

    /**
     * {@link #META_DIFF} value for a block present only in the new document.
     */
    public static final String ADDED = "added";

    /**
     * {@link #META_DIFF} value for a block present only in the old document.
     */
    public static final String REMOVED = "removed";

    /**
     * {@link #META_DIFF} value for a block that was edited: it holds the content
     * of both sides, with the differences marked inline ({@link FormatType#INS}
     * and {@link FormatType#DEL}).
     */
    public static final String CHANGED = "changed";

    /**
     * Ceiling on the size of an alignment table. Beyond it the differing region
     * is reported wholesale (everything old removed, everything new added)
     * rather than aligned — the alignment is quadratic, and a region large
     * enough to reach this is one where an item-by-item reading is not useful
     * anyway. A thousand blocks (or words) each side sits under it.
     */
    private static final int MAX_ALIGNMENT_CELLS = 1000000;

    /**
     * How much of two blocks (or two lines) must be shared before they are taken
     * to be one thing edited rather than two separate things: more than half
     * their words, counted with repeats.
     * <p>
     * Below it, marking word by word would be a fiction — the two have nothing
     * to do with each other, and pairing them produces a block in which almost
     * every word is marked, which is harder to read than the plain statement
     * that one went and another came.
     */
    private static final double PAIRING_THRESHOLD = 0.5;

    /**
     * Separates the parts of a comparison key (the text, its formats, its
     * meta-data) so that a key cannot be spelt two ways — a word ending in a
     * format name must not read as the same key as the word without it.
     */
    private static final char SEPARATOR = (char) 31;

    /**
     * The outcome of a comparison.
     */
    public static class Result {

        private final FormattedText document;
        private final int added;
        private final int removed;
        private final int changed;

        Result(FormattedText document, int added, int removed, int changed) {
            this.document = document;
            this.added = added;
            this.removed = removed;
            this.changed = changed;
        }

        /**
         * The diff document: the new document with removed content reinstated and
         * every difference marked. Read-only — see the class documentation.
         */
        public FormattedText document() {
            return document;
        }

        /**
         * The number of blocks present only in the new document.
         */
        public int added() {
            return added;
        }

        /**
         * The number of blocks present only in the old document.
         */
        public int removed() {
            return removed;
        }

        /**
         * The number of blocks that were edited (marked inline rather than added
         * or removed whole).
         */
        public int changed() {
            return changed;
        }

        /**
         * Whether the two documents compared equal.
         */
        public boolean identical() {
            return ((added == 0) && (removed == 0) && (changed == 0));
        }
    }

    /**
     * Compares two documents.
     *
     * @param from
     *             the old document (may be {@code null}, being nothing).
     * @param to
     *             the new document (may be {@code null}, being nothing).
     * @return the comparison.
     */
    public static Result diff(FormattedText from, FormattedText to) {
        List<FormattedBlock> a = blocks(from);
        List<FormattedBlock> b = blocks(to);

        List<Mark> marks = new ArrayList<>();
        for (int[] op : align(blockKeys(a), blockKeys(b))) {
            if (op[0] == SAME)
                marks.add(new Mark(b.get(op[2]), null));
            else if (op[0] == GONE)
                marks.add(new Mark(a.get(op[1]), REMOVED));
            else
                marks.add(new Mark(b.get(op[2]), ADDED));
        }
        marks = pair(marks);

        FormattedText document = new FormattedText();
        int added = 0;
        int removed = 0;
        int changed = 0;
        for (Mark mark : marks) {
            FormattedBlock block = mark.merged ? mark.block : mark.block.clone(false);
            if (mark.state != null)
                block.meta(META_DIFF, mark.state);
            document.getBlocks().add(block);
            if (ADDED.equals(mark.state))
                added++;
            else if (REMOVED.equals(mark.state))
                removed++;
            else if (CHANGED.equals(mark.state))
                changed++;
        }
        return new Result(document, added, removed, changed);
    }

    /************************************************************************
     * Alignment.
     *
     * One routine, used at every level: blocks within a document, lines within a
     * paired block, words within a paired line. Each level supplies its own keys
     * and decides what a difference means; the matching itself is the same
     * problem three times over.
     ************************************************************************/

    /** Alignment op: the item is in both, at {@code [1]} and {@code [2]}. */
    private static final int SAME = 0;

    /** Alignment op: the item is only in the old side, at {@code [1]}. */
    private static final int GONE = 1;

    /** Alignment op: the item is only in the new side, at {@code [2]}. */
    private static final int NEW = 2;

    /**
     * Aligns two sequences of keys, returning the operations that turn the first
     * into the second, in reading order.
     */
    private static List<int[]> align(String[] ka, String[] kb) {
        List<int[]> ops = new ArrayList<>();
        int n = ka.length;
        int m = kb.length;

        // The head the two sides share. Taken first because most edits are local:
        // trimming what matches at either end is what keeps the table below small for
        // a long document (or a long paragraph) with a short edit in it.
        int start = 0;
        while ((start < n) && (start < m) && ka[start].equals(kb[start])) {
            ops.add(new int[] { SAME, start, start });
            start++;
        }

        // The tail they share (never overlapping the head).
        int endA = n;
        int endB = m;
        while ((endA > start) && (endB > start) && ka[endA - 1].equals(kb[endB - 1])) {
            endA--;
            endB--;
        }

        int len = endA - start;
        int wid = endB - start;
        if ((len > 0) && (wid > 0) && ((((long) len) * ((long) wid)) <= MAX_ALIGNMENT_CELLS)) {
            // lcs[i][j] is the length of the longest common subsequence of the suffixes
            // starting at i and j. Built from the back so the walk below can read it
            // forwards, which is what keeps the output in reading order.
            int[][] lcs = new int[len + 1][wid + 1];
            for (int i = len - 1; i >= 0; i--) {
                for (int j = wid - 1; j >= 0; j--) {
                    if (ka[start + i].equals(kb[start + j]))
                        lcs[i][j] = lcs[i + 1][j + 1] + 1;
                    else
                        lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
            int i = 0;
            int j = 0;
            while ((i < len) && (j < wid)) {
                if (ka[start + i].equals(kb[start + j])) {
                    ops.add(new int[] { SAME, start + i, start + j });
                    i++;
                    j++;
                } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                    // Removals ahead of additions on a tie, so what went reads before what
                    // came — and so the two runs stand together for pairing below.
                    ops.add(new int[] { GONE, start + i, -1 });
                    i++;
                } else {
                    ops.add(new int[] { NEW, -1, start + j });
                    j++;
                }
            }
            while (i < len) {
                ops.add(new int[] { GONE, start + i, -1 });
                i++;
            }
            while (j < wid) {
                ops.add(new int[] { NEW, -1, start + j });
                j++;
            }
        } else {
            for (int i = start; i < endA; i++)
                ops.add(new int[] { GONE, i, -1 });
            for (int j = start; j < endB; j++)
                ops.add(new int[] { NEW, -1, j });
        }

        for (int k = 0; (endA + k) < n; k++)
            ops.add(new int[] { SAME, endA + k, endB + k });
        return ops;
    }

    /************************************************************************
     * Blocks.
     ************************************************************************/

    /**
     * A block on its way into the diff document: the block itself and what is to
     * be said about it.
     */
    private static class Mark {

        final FormattedBlock block;

        final String state;

        /** The block was built here (a merge) rather than taken from a source. */
        final boolean merged;

        Mark(FormattedBlock block, String state) {
            this(block, state, false);
        }

        Mark(FormattedBlock block, String state, boolean merged) {
            this.block = block;
            this.state = state;
            this.merged = merged;
        }
    }

    /**
     * Turns runs of "these went, those came" into "this was edited", wherever the
     * two are recognisably the same block. The alignment puts removals before
     * additions, so a rewritten passage arrives here as a run of removed blocks
     * followed by a run of added ones; they are paired off in order.
     */
    private static List<Mark> pair(List<Mark> marks) {
        List<Mark> out = new ArrayList<>();
        int i = 0;
        while (i < marks.size()) {
            if (!REMOVED.equals(marks.get(i).state)) {
                out.add(marks.get(i));
                i++;
                continue;
            }
            int gone = i;
            while ((gone < marks.size()) && REMOVED.equals(marks.get(gone).state))
                gone++;
            int fresh = gone;
            while ((fresh < marks.size()) && ADDED.equals(marks.get(fresh).state))
                fresh++;

            int pairs = Math.min(gone - i, fresh - gone);
            for (int p = 0; p < pairs; p++) {
                Mark was = marks.get(i + p);
                Mark now = marks.get(gone + p);
                FormattedBlock merged = merge(was.block, now.block);
                if (merged != null) {
                    out.add(new Mark(merged, CHANGED, true));
                } else {
                    // Not the same block edited — say so plainly rather than marking a
                    // block in which nearly every word would be marked.
                    out.add(was);
                    out.add(now);
                }
            }
            for (int q = i + pairs; q < gone; q++)
                out.add(marks.get(q));
            for (int q = gone + pairs; q < fresh; q++)
                out.add(marks.get(q));
            i = fresh;
        }
        return out;
    }

    /**
     * Merges an old block into its new form, marking the difference inline;
     * {@code null} when the two are not one block edited.
     */
    private static FormattedBlock merge(FormattedBlock was, FormattedBlock now) {
        if (was.getType() != now.getType())
            return null;
        // A block with children (a table) is a structure, not a passage: aligning its
        // rows and cells is a different problem, and marking words across it without
        // doing so would attach them to the wrong cells.
        if (!was.getBlocks().isEmpty() || !now.getBlocks().isEmpty())
            return null;
        // Raw content (an equation's or a diagram's source) is not prose and is not
        // marked; if it differs the block is a different block.
        if (!same(was.getContent(), now.getContent()))
            return null;
        if (was.getLines().isEmpty() || now.getLines().isEmpty())
            return null;
        if (similarity(words(was.flatten()), words(now.flatten())) < PAIRING_THRESHOLD)
            return null;

        FormattedBlock merged = now.clone(false);
        merged.setLines(mergeLines(was.getLines(), now.getLines()));
        return merged;
    }

    /**
     * The comparison keys for a run of blocks: type, indent and markdown. The
     * markdown is taken a block at a time (each in its own document, as the
     * transformer does, so list grouping is handled), which is consistent across
     * the two sides being compared and so safe to compare.
     */
    private static String[] blockKeys(List<FormattedBlock> blocks) {
        String[] keys = new String[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            FormattedBlock block = blocks.get(i);
            FormattedText one = new FormattedText();
            one.getBlocks().add(block);
            keys[i] = block.getType().name() + "|" + block.getIndent() + "|" + MarkdownSerializer.serialize(one);
        }
        return keys;
    }

    /************************************************************************
     * Lines.
     ************************************************************************/

    /**
     * Merges the lines of a paired block. Lines are aligned as blocks are, and a
     * removed line paired with an added one is merged word by word; a line with
     * no counterpart is marked whole.
     */
    private static List<FormattedLine> mergeLines(List<FormattedLine> was, List<FormattedLine> now) {
        List<FormattedLine> out = new ArrayList<>();
        List<int[]> ops = align(lineKeys(was), lineKeys(now));
        int i = 0;
        while (i < ops.size()) {
            int[] op = ops.get(i);
            if (op[0] == SAME) {
                out.add(now.get(op[2]).clone());
                i++;
                continue;
            }
            int gone = i;
            while ((gone < ops.size()) && (ops.get(gone)[0] == GONE))
                gone++;
            int fresh = gone;
            while ((fresh < ops.size()) && (ops.get(fresh)[0] == NEW))
                fresh++;

            int pairs = Math.min(gone - i, fresh - gone);
            for (int p = 0; p < pairs; p++) {
                FormattedLine oldLine = was.get(ops.get(i + p)[1]);
                FormattedLine newLine = now.get(ops.get(gone + p)[2]);
                FormattedLine merged = mergeLine(oldLine, newLine);
                if (merged != null) {
                    out.add(merged);
                } else {
                    out.add(marked(oldLine, FormatType.DEL));
                    out.add(marked(newLine, FormatType.INS));
                }
            }
            for (int q = i + pairs; q < gone; q++)
                out.add(marked(was.get(ops.get(q)[1]), FormatType.DEL));
            for (int q = gone + pairs; q < fresh; q++)
                out.add(marked(now.get(ops.get(q)[2]), FormatType.INS));
            i = fresh;
        }
        return out;
    }

    /**
     * Merges one line into another, marking the words that went and those that
     * came; {@code null} when the two are not one line edited (or are too long to
     * align word by word, in which case the caller marks them whole).
     */
    private static FormattedLine mergeLine(FormattedLine was, FormattedLine now) {
        if (similarity(words(was.flatten()), words(now.flatten())) < PAIRING_THRESHOLD)
            return null;
        List<Atom> from = atoms(was);
        List<Atom> to = atoms(now);
        FormattedLine merged = new FormattedLine();
        for (int[] op : align(atomKeys(from), atomKeys(to))) {
            if (op[0] == SAME) {
                append(merged, to.get(op[2]), null);
            } else if (op[0] == GONE) {
                append(merged, from.get(op[1]), FormatType.DEL);
            } else {
                // A word replaced sits between the same two spaces as the word it
                // replaced, so what went and what came would run together into one
                // unreadable token ("oldnew"). They are two words on the page and need
                // the space between them that neither of them owns.
                separate(merged, to.get(op[2]));
                append(merged, to.get(op[2]), FormatType.INS);
            }
        }
        return merged;
    }

    /**
     * Puts a space between a removal and the insertion that follows it, where the text
     * so far does not already end in one.
     */
    private static void separate(FormattedLine line, Atom next) {
        String text = (line.getText() == null) ? "" : line.getText();
        if (text.isEmpty() || next.text.isEmpty())
            return;
        // Only between two words. Punctuation that replaces punctuation does not want a
        // space put through the middle of it.
        if (!Character.isLetterOrDigit(text.charAt(text.length() - 1)) || !Character.isLetterOrDigit(next.text.charAt(0)))
            return;
        if (!endsMarked(line, FormatType.DEL))
            return;
        line.setText(text + " ");
    }

    /** Whether the line's last format carries the given mark and runs to its end. */
    private static boolean endsMarked(FormattedLine line, FormatType mark) {
        List<Format> formats = line.getFormatting();
        if (formats.isEmpty())
            return false;
        Format last = formats.get(formats.size() - 1);
        if ((last.getIndex() + last.getLength()) != line.getText().length())
            return false;
        return last.getFormats().contains(mark);
    }

    /** A copy of the line with the given mark over the whole of it. */
    private static FormattedLine marked(FormattedLine line, FormatType mark) {
        FormattedLine copy = line.clone();
        copy.addFormat(0, copy.length(), mark);
        return copy;
    }

    /**
     * The comparison keys for a run of lines: the line as it would be marked up,
     * so that a line differing only in its formatting still counts as different.
     */
    private static String[] lineKeys(List<FormattedLine> lines) {
        String[] keys = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            StringBuilder sb = new StringBuilder();
            for (Atom atom : atoms(lines.get(i)))
                sb.append(atom.key).append(SEPARATOR);
            keys[i] = sb.toString();
        }
        return keys;
    }

    /************************************************************************
     * Words (and the things that are not words).
     ************************************************************************/

    /**
     * The unit the word-level comparison works in: a run of text carrying one set
     * of formats and one set of meta-data.
     * <p>
     * Runs of ordinary text are broken into words and the gaps between them, so a
     * changed word is a changed word rather than a changed sentence. A link, an
     * image or a variable is one atom whatever its length: they are single things
     * with meta-data attached, and half of one means nothing.
     */
    private static class Atom {

        /** The text this atom contributes ({@code ""} for a zero-length marker). */
        final String text;

        final List<FormatType> formats;

        final Map<String, String> meta;

        /** A zero-length format (the convention a variable is stored under). */
        final boolean marker;

        /** Everything of this atom that makes it the same atom as another. */
        final String key;

        Atom(String text, FormatType[] formats, Map<String, String> meta, boolean marker) {
            this.text = text;
            this.formats = new ArrayList<>();
            if (formats != null) {
                for (FormatType format : formats)
                    this.formats.add(format);
            }
            this.meta = new HashMap<>();
            if (meta != null)
                this.meta.putAll(meta);
            this.marker = marker;

            StringBuilder sb = new StringBuilder(text).append(SEPARATOR);
            for (FormatType format : this.formats)
                sb.append(format.name()).append(',');
            sb.append(SEPARATOR);
            List<String> names = new ArrayList<>(this.meta.keySet());
            Collections.sort(names);
            for (String name : names)
                sb.append(name).append('=').append(this.meta.get(name)).append(';');
            this.key = sb.toString();
        }
    }

    /**
     * Breaks a line into the units the word-level comparison aligns. Taken from
     * the formats directly rather than from {@code sequence()} so that the line
     * can be rebuilt exactly as it was — a zero-length variable stays zero-length,
     * and an image keeps its source and its dimensions.
     */
    private static List<Atom> atoms(FormattedLine line) {
        List<Atom> atoms = new ArrayList<>();
        String text = (line.getText() == null) ? "" : line.getText();
        int idx = 0;
        for (Format format : line.getFormatting()) {
            if (format.getIndex() > idx)
                words(atoms, text.substring(idx, Math.min(format.getIndex(), text.length())), null, null);
            if (format.getLength() == 0) {
                atoms.add(new Atom("", format.formats(), format.getMeta(), true));
                idx = Math.max(idx, format.getIndex());
                continue;
            }
            int end = Math.min(format.getIndex() + format.getLength(), text.length());
            String run = text.substring(format.getIndex(), end);
            if (atomic(format))
                atoms.add(new Atom(run, format.formats(), format.getMeta(), false));
            else
                words(atoms, run, format.formats(), format.getMeta());
            idx = end;
        }
        if (idx < text.length())
            words(atoms, text.substring(idx), null, null);
        return atoms;
    }

    /**
     * Whether the format covers something that is one thing however long it is: a
     * link, an image or a variable given as a labelled span. Marking half of one
     * would be meaningless, and splitting one would lose the meta-data that makes
     * it what it is.
     */
    private static boolean atomic(Format format) {
        if (format.getMeta() == null)
            return false;
        return (format.getMeta().containsKey(FormattedLine.META_LINK)
            || format.getMeta().containsKey(FormattedLine.META_IMAGE)
            || format.getMeta().containsKey(FormattedLine.META_VARIABLE));
    }

    /**
     * Breaks a run of text into words and the whitespace between them, each an
     * atom in its own right. Whitespace is kept as its own atom rather than being
     * carried with a word so that inserting a word between two others marks the
     * word, not the space that had to move to make room for it.
     */
    private static void words(List<Atom> atoms, String run, FormatType[] formats, Map<String, String> meta) {
        int i = 0;
        while (i < run.length()) {
            int kind = kind(run.charAt(i));
            int j = i + 1;
            while ((j < run.length()) && (kind(run.charAt(j)) == kind))
                j++;
            atoms.add(new Atom(run.substring(i, j), formats, meta, false));
            i = j;
        }
    }

    /**
     * What kind of run a character belongs to: whitespace, word, or punctuation.
     * <p>
     * Punctuation is its own run rather than part of the word it follows, so that
     * appending to a sentence marks the words appended and not the full stop that
     * was already there ("payments." → "payments and payouts." is two words
     * inserted, not a sentence replaced).
     */
    private static int kind(char c) {
        if (Character.isWhitespace(c))
            return 0;
        if (Character.isLetterOrDigit(c))
            return 1;
        return 2;
    }

    private static String[] atomKeys(List<Atom> atoms) {
        String[] keys = new String[atoms.size()];
        for (int i = 0; i < atoms.size(); i++)
            keys[i] = atoms.get(i).key;
        return keys;
    }

    /**
     * Appends an atom to a line under construction, optionally marked.
     * <p>
     * Whitespace is never marked: a struck-through space is noise, and a run of
     * marked words reads as one marked passage whether or not the spaces inside
     * it carry the mark.
     */
    private static void append(FormattedLine line, Atom atom, FormatType mark) {
        List<FormatType> formats = new ArrayList<>(atom.formats);
        if ((mark != null) && !blank(atom.text) && !formats.contains(mark))
            formats.add(mark);

        int index = (line.getText() == null) ? 0 : line.getText().length();
        if (atom.marker) {
            Format format = new Format(index, 0, formats.toArray(new FormatType[formats.size()]));
            format.getMeta().putAll(atom.meta);
            line.getFormatting().add(format);
            return;
        }
        if (!formats.isEmpty() || !atom.meta.isEmpty()) {
            Format format = new Format(index, atom.text.length(), formats.toArray(new FormatType[formats.size()]));
            format.getMeta().putAll(atom.meta);
            line.getFormatting().add(format);
        }
        line.setText(((line.getText() == null) ? "" : line.getText()) + atom.text);
    }

    /************************************************************************
     * Internal helpers.
     ************************************************************************/

    private static List<FormattedBlock> blocks(FormattedText text) {
        if ((text == null) || text.empty())
            return new ArrayList<>();
        return text.getBlocks();
    }

    /**
     * How much two passages have in common: twice what they share over what they
     * have between them, counting repeats, so it runs from 0 (nothing in common)
     * to 1 (the same words).
     */
    private static double similarity(List<String> was, List<String> now) {
        if (was.isEmpty() && now.isEmpty())
            return 1.0;
        if (was.isEmpty() || now.isEmpty())
            return 0.0;
        Map<String, Integer> counts = new HashMap<>();
        for (String word : was) {
            Integer count = counts.get(word);
            counts.put(word, (count == null) ? 1 : (count + 1));
        }
        int common = 0;
        for (String word : now) {
            Integer count = counts.get(word);
            if ((count != null) && (count > 0)) {
                counts.put(word, count - 1);
                common++;
            }
        }
        return (2.0 * common) / (was.size() + now.size());
    }

    /** The words of a passage, lower-cased so a capitalisation is not a new word. */
    private static List<String> words(String text) {
        List<String> words = new ArrayList<>();
        if (text == null)
            return words;
        int i = 0;
        while (i < text.length()) {
            while ((i < text.length()) && Character.isWhitespace(text.charAt(i)))
                i++;
            int j = i;
            while ((j < text.length()) && !Character.isWhitespace(text.charAt(j)))
                j++;
            if (j > i)
                words.add(text.substring(i, j).toLowerCase());
            i = j;
        }
        return words;
    }

    private static boolean blank(String text) {
        if ((text == null) || text.isEmpty())
            return true;
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i)))
                return false;
        }
        return true;
    }

    private static boolean same(String a, String b) {
        if ((a == null) || a.isEmpty())
            return ((b == null) || b.isEmpty());
        return a.equals(b);
    }
}
