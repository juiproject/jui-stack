/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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

import com.effacy.jui.text.type.builder.markdown.MarkdownSerializer;

/**
 * Provides block-level transformation operations on {@link FormattedText}.
 * <p>
 * Two styles of editing are supported:
 * <ul>
 * <li><b>Splice</b> — replaces a range of blocks by index. Useful when indices
 * are stable.</li>
 * <li><b>Find/replace</b> — locates blocks by their markdown content and
 * replaces them. More robust when multiple edits are applied in sequence since
 * each operation identifies its target by content rather than position.</li>
 * </ul>
 * All operations return a new {@link FormattedText} instance; the original is
 * not modified.
 */
public class FormattedTextTransformer {

    /************************************************************************
     * Find / replace
     ************************************************************************/

    /**
     * Result of a {@link #findAndReplace} operation.
     */
    public static class ReplaceResult {

        private final FormattedText document;
        private final boolean matched;

        ReplaceResult(FormattedText document, boolean matched) {
            this.document = document;
            this.matched = matched;
        }

        /** The (possibly modified) document. */
        public FormattedText document() { return document; }

        /** Whether the find string was matched. */
        public boolean matched() { return matched; }
    }

    /**
     * Finds a contiguous range of blocks whose serialized markdown matches
     * {@code find} and replaces them with blocks parsed from {@code replace}.
     * <p>
     * Matching is performed by serializing each block to markdown (without
     * numbering) and joining contiguous blocks with {@code "\n\n"} to build
     * candidate strings. The first range that matches is replaced.
     *
     * @param source
     *                  the source document (not modified).
     * @param find
     *                  the markdown content to locate (must not be {@code null}
     *                  or blank).
     * @param replace
     *                  the replacement markdown ({@code null} or blank to
     *                  delete the matched range).
     * @return a {@link ReplaceResult} indicating whether a match was found and
     *         containing the resulting document.
     */
    public static ReplaceResult findAndReplace(FormattedText source, String find, String replace) {
        if (source == null || source.empty() || find == null || find.isBlank())
            return new ReplaceResult(source, false);

        String findNorm = normalise(find);

        // Serialize each block individually.
        List<FormattedBlock> blocks = source.getBlocks();
        String[] blockMds = new String[blocks.size()];
        for (int i = 0; i < blocks.size(); i++)
            blockMds[i] = serializeOneBlock(blocks.get(i));

        // Try every contiguous range [i, j) looking for a match.
        for (int i = 0; i < blocks.size(); i++) {
            StringBuilder candidate = new StringBuilder();
            for (int j = i; j < blocks.size(); j++) {
                if (j > i)
                    candidate.append("\n\n");
                candidate.append(blockMds[j]);

                if (normalise(candidate.toString()).equals(findNorm)) {
                    // Found a match at [i, j+1). Splice in the replacement.
                    FormattedText replacement = null;
                    if (replace != null && !replace.isBlank())
                        replacement = FormattedText.markdown(replace);
                    return new ReplaceResult(splice(source, i, j + 1, replacement), true);
                }
            }
        }

        return new ReplaceResult(source, false);
    }

    /**
     * Applies multiple find/replace operations in sequence. Each operation is
     * applied to the result of the previous one.
     *
     * @param source
     *                 the source document (not modified).
     * @param edits
     *                 the list of edits to apply.
     * @return a {@link BatchResult} with the final document and per-edit
     *         outcomes.
     */
    public static BatchResult batchFindAndReplace(FormattedText source, List<FindReplaceEdit> edits) {
        if (edits == null || edits.isEmpty())
            return new BatchResult(source, List.of());

        FormattedText current = source;
        List<Boolean> outcomes = new ArrayList<>();
        for (FindReplaceEdit edit : edits) {
            if (edit.find() == null || edit.find().isBlank()) {
                // Insert at beginning or end.
                if (edit.replace() != null && !edit.replace().isBlank()) {
                    FormattedText insertion = FormattedText.markdown(edit.replace());
                    if ("beginning".equalsIgnoreCase(edit.position())) {
                        current = splice(current, 0, 0, insertion);
                    } else {
                        current = splice(current, blockCount(current), blockCount(current), insertion);
                    }
                    outcomes.add(true);
                } else {
                    outcomes.add(false);
                }
            } else {
                ReplaceResult result = findAndReplace(current, edit.find(), edit.replace());
                current = result.document();
                outcomes.add(result.matched());
            }
        }
        return new BatchResult(current, outcomes);
    }

    /**
     * A single find/replace edit within a batch.
     */
    public static class FindReplaceEdit {

        private final String find;
        private final String replace;
        private final String position;

        public FindReplaceEdit(String find, String replace) {
            this(find, replace, null);
        }

        public FindReplaceEdit(String find, String replace, String position) {
            this.find = find;
            this.replace = replace;
            this.position = position;
        }

        /** The markdown to find ({@code null}/blank for insert). */
        public String find() { return find; }

        /** The replacement markdown ({@code null}/blank to delete). */
        public String replace() { return replace; }

        /** For inserts: "beginning" or "end" (default). */
        public String position() { return position; }
    }

    /**
     * Result of a batch find/replace.
     */
    public static class BatchResult {

        private final FormattedText document;
        private final List<Boolean> outcomes;

        BatchResult(FormattedText document, List<Boolean> outcomes) {
            this.document = document;
            this.outcomes = outcomes;
        }

        /** The final document after all edits. */
        public FormattedText document() { return document; }

        /** Whether each edit matched (in order). */
        public List<Boolean> outcomes() { return outcomes; }

        /** Whether all edits matched. */
        public boolean allMatched() { return !outcomes.contains(false); }
    }

    /************************************************************************
     * Splice (index-based)
     ************************************************************************/

    /**
     * Replaces the blocks in the range {@code [start, end)} with the blocks from
     * the replacement text.
     *
     * @param source
     *                    the source document (not modified).
     * @param start
     *                    the index of the first block to replace (inclusive,
     *                    0-based).
     * @param end
     *                    the index past the last block to replace (exclusive). Must
     *                    be {@code >= start}.
     * @param replacement
     *                    the replacement content (may be {@code null} or empty to
     *                    delete).
     * @return a new {@link FormattedText} with the splice applied.
     * @throws IndexOutOfBoundsException
     *                                   if {@code start} or {@code end} is out of
     *                                   range.
     */
    public static FormattedText splice(FormattedText source, int start, int end, FormattedText replacement) {
        if (source == null)
            source = new FormattedText();
        List<FormattedBlock> blocks = source.getBlocks();
        int size = blocks.size();

        if (start < 0 || start > size)
            throw new IndexOutOfBoundsException("start index " + start + " out of range [0, " + size + "]");
        if (end < start || end > size)
            throw new IndexOutOfBoundsException("end index " + end + " out of range [" + start + ", " + size + "]");

        FormattedText result = new FormattedText();

        // Blocks before the splice point.
        for (int i = 0; i < start; i++)
            result.getBlocks().add(blocks.get(i).clone());

        // Replacement blocks.
        if (replacement != null) {
            for (FormattedBlock block : replacement.getBlocks())
                result.getBlocks().add(block.clone());
        }

        // Blocks after the splice point.
        for (int i = end; i < size; i++)
            result.getBlocks().add(blocks.get(i).clone());

        return result;
    }

    /**
     * Replaces the blocks in the range {@code [start, end)} with blocks parsed
     * from the given markdown content.
     */
    public static FormattedText splice(FormattedText source, int start, int end, String markdown) {
        FormattedText replacement = null;
        if (markdown != null && !markdown.isBlank())
            replacement = FormattedText.markdown(markdown);
        return splice(source, start, end, replacement);
    }

    /**
     * Returns the number of top-level blocks in the given formatted text.
     */
    public static int blockCount(FormattedText text) {
        if (text == null || text.empty())
            return 0;
        return text.getBlocks().size();
    }

    /************************************************************************
     * Internal helpers
     ************************************************************************/

    /**
     * Serializes a single block to markdown (wrapping it in a temporary
     * FormattedText so the serializer handles list grouping correctly).
     */
    private static String serializeOneBlock(FormattedBlock block) {
        FormattedText temp = new FormattedText();
        temp.getBlocks().add(block);
        return MarkdownSerializer.serialize(temp);
    }

    /**
     * Normalises markdown for comparison: trims, collapses whitespace runs,
     * and strips trailing punctuation differences that don't affect meaning.
     */
    private static String normalise(String md) {
        if (md == null)
            return "";
        return md.strip().replaceAll("\\s+", " ");
    }
}
