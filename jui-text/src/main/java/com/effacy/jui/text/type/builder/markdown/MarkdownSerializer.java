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
package com.effacy.jui.text.type.builder.markdown;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedText;

/**
 * Serializes a {@link FormattedText} document to markdown. This is the reverse
 * of {@link MarkdownParser}.
 * <p>
 * Only formatting that can be represented in standard markdown is emitted.
 * Unsupported inline formats (underline, superscript, subscript, highlight) are
 * silently dropped.
 * <p>
 * Usage:
 * <pre>
 *   // Simple (static convenience):
 *   String md = MarkdownSerializer.serialize(formattedText);
 *
 *   // With block numbering:
 *   String md = new MarkdownSerializer()
 *       .numberBlocks(true)
 *       .serialize(formattedText);
 * </pre>
 */
public class MarkdownSerializer {

    /************************************************************************
     * Configuration
     ************************************************************************/

    /**
     * See {@link #numberBlocks(boolean)}.
     */
    private boolean numberBlocks;

    /**
     * Enables block numbering in the output. When enabled, each top-level block
     * (or group of consecutive list blocks) is prefixed with its zero-based
     * index, e.g. {@code [0] # Heading}.
     * <p>
     * This is useful for providing block references for targeted editing
     * operations.
     *
     * @param numberBlocks
     *                     {@code true} to enable numbering.
     * @return this serializer for chaining.
     */
    public MarkdownSerializer numberBlocks(boolean numberBlocks) {
        this.numberBlocks = numberBlocks;
        return this;
    }

    /************************************************************************
     * Serialization
     ************************************************************************/

    /**
     * Static convenience for serializing without any configuration.
     *
     * @param text
     *             the formatted text to serialize (may be {@code null}).
     * @return the markdown string (never {@code null}).
     */
    public static String serialize(FormattedText text) {
        return new MarkdownSerializer().toMarkdown(text);
    }

    /**
     * Serializes the given {@link FormattedText} to a markdown string using this
     * serializer's configuration.
     *
     * @param text
     *             the formatted text to serialize (may be {@code null}).
     * @return the markdown string (never {@code null}).
     */
    public String toMarkdown(FormattedText text) {
        if ((text == null) || text.empty())
            return "";
        StringBuilder sb = new StringBuilder();
        List<FormattedBlock> blocks = text.getBlocks();
        int i = 0;
        int blockNumber = 0;
        while (i < blocks.size()) {
            if (sb.length() > 0)
                sb.append("\n\n");
            FormattedBlock block = blocks.get(i);

            if (numberBlocks)
                sb.append("[").append(blockNumber++).append("] ");

            // Group consecutive list blocks of the same type into a single list.
            if (block.getType() == FormattedBlock.BlockType.NLIST || block.getType() == FormattedBlock.BlockType.OLIST) {
                FormattedBlock.BlockType listType = block.getType();
                List<FormattedBlock> group = new ArrayList<>();
                while (i < blocks.size() && blocks.get(i).getType() == listType) {
                    group.add(blocks.get(i));
                    i++;
                }
                serializeListGroup(sb, group, listType);
            } else {
                serializeBlock(sb, block, 0);
                i++;
            }
        }
        return sb.toString();
    }

    /************************************************************************
     * Block serialization (static helpers)
     ************************************************************************/

    private static void serializeBlock(StringBuilder sb, FormattedBlock block, int depth) {
        switch (block.getType()) {
            case H1:
                sb.append("# ");
                appendLines(sb, block, " ");
                break;
            case H2:
                sb.append("## ");
                appendLines(sb, block, " ");
                break;
            case H3:
                sb.append("### ");
                appendLines(sb, block, " ");
                break;
            case H4:
                sb.append("#### ");
                appendLines(sb, block, " ");
                break;
            case H5:
                sb.append("##### ");
                appendLines(sb, block, " ");
                break;
            case CODE:
                String lang = (block.getMeta() != null) ? block.meta("lang") : null;
                sb.append("```");
                if (lang != null && !lang.isBlank())
                    sb.append(lang);
                sb.append("\n");
                appendLines(sb, block, "\n");
                sb.append("\n```");
                break;
            case NLIST:
                appendListItems(sb, block, "- ", depth);
                break;
            case OLIST:
                appendListItems(sb, block, null, depth);
                break;
            case TABLE:
                serializeTable(sb, block);
                break;
            case PARA:
            default:
                appendLines(sb, block, "\n");
                break;
        }
    }

    /**
     * Serializes a group of consecutive list blocks (each block may have one or
     * more lines) into a single markdown list.
     */
    private static void serializeListGroup(StringBuilder sb, List<FormattedBlock> group, FormattedBlock.BlockType listType) {
        boolean ordered = (listType == FormattedBlock.BlockType.OLIST);
        int itemNumber = 1;
        boolean first = true;
        for (FormattedBlock block : group) {
            String indent = "  ".repeat(block.getIndent());
            for (FormattedLine line : block.getLines()) {
                if (!first)
                    sb.append("\n");
                first = false;
                sb.append(indent);
                if (ordered) {
                    sb.append(itemNumber++).append(". ");
                } else {
                    sb.append("- ");
                }
                sb.append(serializeLine(line));
            }
        }
    }

    /**
     * Appends lines from a block, joining with the given separator. Each line
     * has inline formatting applied.
     */
    private static void appendLines(StringBuilder sb, FormattedBlock block, String separator) {
        List<FormattedLine> lines = block.getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                sb.append(separator);
            sb.append(serializeLine(lines.get(i)));
        }
    }

    /**
     * Appends list items. Each line in the block becomes a list item. Indentation
     * is applied based on the block's indent level plus any additional depth.
     */
    private static void appendListItems(StringBuilder sb, FormattedBlock block, String marker, int depth) {
        String indent = "  ".repeat(block.getIndent() + depth);
        List<FormattedLine> lines = block.getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                sb.append("\n");
            sb.append(indent);
            if (marker != null) {
                sb.append(marker);
            } else {
                sb.append(i + 1).append(". ");
            }
            sb.append(serializeLine(lines.get(i)));
        }
    }

    /**
     * Serializes a table block. The first TROW is treated as the header row with
     * a separator line following it.
     */
    private static void serializeTable(StringBuilder sb, FormattedBlock table) {
        List<FormattedBlock> rows = table.getBlocks();
        if (rows == null || rows.isEmpty())
            return;
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0)
                sb.append("\n");
            FormattedBlock row = rows.get(r);
            sb.append("|");
            for (FormattedBlock cell : row.getBlocks()) {
                sb.append(" ");
                appendLines(sb, cell, " ");
                sb.append(" |");
            }
            if (r == 0) {
                sb.append("\n|");
                for (int c = 0; c < row.getBlocks().size(); c++)
                    sb.append(" --- |");
            }
        }
    }

    /************************************************************************
     * Inline formatting
     ************************************************************************/

    /**
     * Serializes a single {@link FormattedLine} to markdown, applying inline
     * formatting markers.
     */
    static String serializeLine(FormattedLine line) {
        String text = line.getText();
        if (text == null || text.isEmpty())
            return "";
        List<FormattedLine.Format> formatting = line.getFormatting();
        if (formatting == null || formatting.isEmpty())
            return text;

        // Build a per-character map of active formats.
        @SuppressWarnings("unchecked")
        List<FormatType>[] charFormats = new List[text.length()];
        for (int i = 0; i < text.length(); i++)
            charFormats[i] = new ArrayList<>();

        for (FormattedLine.Format fmt : formatting) {
            int start = fmt.getIndex();
            int end = Math.min(start + fmt.getLength(), text.length());
            for (int i = start; i < end; i++) {
                for (FormatType ft : fmt.getFormats()) {
                    if (!charFormats[i].contains(ft))
                        charFormats[i].add(ft);
                }
            }
        }

        // Check if there are any link or image formats — if so, delegate to
        // the link-aware path which handles [text](url) syntax directly.
        boolean hasLinks = false;
        for (FormattedLine.Format fmt : formatting) {
            if (fmt.getFormats().contains(FormatType.A) || fmt.getFormats().contains(FormatType.IMG)) {
                hasLinks = true;
                break;
            }
        }
        if (hasLinks)
            return postProcessLinks(text, line);

        // Simple path: no links/images, just inline formatting.
        StringBuilder sb = new StringBuilder();
        List<FormatType> active = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            closeFormats(sb, active, charFormats[i]);
            openFormats(sb, active, charFormats[i]);
            sb.append(text.charAt(i));
        }
        closeAllFormats(sb, active);

        return sb.toString();
    }

    /**
     * Builds a markdown string for a line that contains link or image formatting.
     * Links and images use {@code [text](url)} / {@code ![alt](url)} syntax while
     * other inline formatting is applied around them.
     */
    private static String postProcessLinks(String text, FormattedLine line) {
        List<FormattedLine.Format> linkFormats = new ArrayList<>();
        for (FormattedLine.Format fmt : line.getFormatting()) {
            if (fmt.getFormats().contains(FormatType.A) || fmt.getFormats().contains(FormatType.IMG))
                linkFormats.add(fmt);
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0;

        // Sort link formats by index.
        linkFormats.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));

        for (FormattedLine.Format fmt : linkFormats) {
            int start = fmt.getIndex();
            int end = Math.min(start + fmt.getLength(), text.length());

            // Emit text before this link with inline formatting.
            if (start > pos) {
                sb.append(serializeSpan(text, pos, start, line.getFormatting()));
            }

            String linkText = text.substring(start, end);
            boolean isImage = fmt.getFormats().contains(FormatType.IMG);

            if (isImage) {
                String src = (fmt.getMeta() != null) ? fmt.getMeta().get(FormattedLine.META_IMAGE) : null;
                sb.append("![").append(linkText).append("](").append(src != null ? src : "").append(")");
            } else {
                String href = (fmt.getMeta() != null) ? fmt.getMeta().get(FormattedLine.META_LINK) : null;
                sb.append("[").append(linkText).append("](").append(href != null ? href : "").append(")");
            }

            pos = end;
        }

        // Emit remaining text.
        if (pos < text.length())
            sb.append(serializeSpan(text, pos, text.length(), line.getFormatting()));

        return sb.toString();
    }

    /**
     * Serializes a span of text with applicable inline formatting (excluding
     * links/images which are handled separately).
     */
    private static String serializeSpan(String text, int from, int to, List<FormattedLine.Format> allFormats) {
        if (from >= to)
            return "";

        // Build per-character format sets for this span.
        @SuppressWarnings("unchecked")
        List<FormatType>[] charFormats = new List[to - from];
        for (int i = 0; i < charFormats.length; i++)
            charFormats[i] = new ArrayList<>();

        for (FormattedLine.Format fmt : allFormats) {
            // Skip link/image — handled externally.
            if (fmt.getFormats().contains(FormatType.A) || fmt.getFormats().contains(FormatType.IMG))
                continue;
            int fStart = Math.max(fmt.getIndex(), from) - from;
            int fEnd = Math.min(fmt.getIndex() + fmt.getLength(), to) - from;
            for (int i = fStart; i < fEnd; i++) {
                for (FormatType ft : fmt.getFormats()) {
                    if (!charFormats[i].contains(ft))
                        charFormats[i].add(ft);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        List<FormatType> active = new ArrayList<>();
        for (int i = 0; i < charFormats.length; i++) {
            closeFormats(sb, active, charFormats[i]);
            openFormats(sb, active, charFormats[i]);
            sb.append(text.charAt(from + i));
        }
        closeAllFormats(sb, active);
        return sb.toString();
    }

    private static void openFormats(StringBuilder sb, List<FormatType> active, List<FormatType> current) {
        // Open in a defined order: bold, italic, strikethrough, code.
        for (FormatType ft : new FormatType[] { FormatType.BLD, FormatType.ITL, FormatType.STR, FormatType.CODE }) {
            if (current.contains(ft) && !active.contains(ft)) {
                sb.append(openMarker(ft));
                active.add(ft);
            }
        }
    }

    private static void closeFormats(StringBuilder sb, List<FormatType> active, List<FormatType> current) {
        // Close in reverse order of opening.
        for (int j = active.size() - 1; j >= 0; j--) {
            FormatType ft = active.get(j);
            // Skip link/image — handled in post-processing.
            if (ft == FormatType.A || ft == FormatType.IMG)
                continue;
            if (!current.contains(ft)) {
                sb.append(closeMarker(ft));
                active.remove(j);
            }
        }
    }

    private static void closeAllFormats(StringBuilder sb, List<FormatType> active) {
        for (int j = active.size() - 1; j >= 0; j--) {
            FormatType ft = active.get(j);
            if (ft != FormatType.A && ft != FormatType.IMG)
                sb.append(closeMarker(ft));
        }
        active.clear();
    }

    private static String openMarker(FormatType ft) {
        switch (ft) {
            case BLD: return "**";
            case ITL: return "*";
            case STR: return "~~";
            case CODE: return "`";
            default: return "";
        }
    }

    private static String closeMarker(FormatType ft) {
        return openMarker(ft);
    }
}
