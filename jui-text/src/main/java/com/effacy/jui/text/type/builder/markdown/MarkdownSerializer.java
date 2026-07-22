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

            // Group all consecutive list blocks — of either marker type — into a single list, so
            // a nested list with mixed markers (e.g. a bullet sublist inside a numbered list)
            // stays one list and ordered numbering resumes correctly after the sublist.
            if (isListBlock(block)) {
                List<FormattedBlock> group = new ArrayList<>();
                while (i < blocks.size() && isListBlock(blocks.get(i))) {
                    group.add(blocks.get(i));
                    i++;
                }
                serializeListGroup(sb, group);
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
            case FENCE:
                String info = (block.getMeta() != null) ? block.meta("info") : null;
                sb.append("```");
                if (info != null && !info.isBlank())
                    sb.append(info);
                sb.append("\n");
                appendLines(sb, block, "\n");
                sb.append("\n```");
                break;
            case QUOTE:
                serializeQuote(sb, block);
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
     * Serializes a block quote: every line is prefixed with {@code > } (an empty
     * line becomes a bare {@code >}), reproducing the markdown the parser consumed.
     */
    private static void serializeQuote(StringBuilder sb, FormattedBlock block) {
        List<FormattedLine> lines = block.getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                sb.append("\n");
            String content = serializeLine(lines.get(i));
            if (content.isEmpty())
                sb.append(">");
            else
                sb.append("> ").append(content);
        }
    }

    /** Upper bound on list nesting depth tracked for ordered numbering. */
    private static final int MAX_LIST_DEPTH = 32;

    private static boolean isListBlock(FormattedBlock block) {
        FormattedBlock.BlockType type = block.getType();
        return (type == FormattedBlock.BlockType.NLIST) || (type == FormattedBlock.BlockType.OLIST);
    }

    /**
     * Serializes a group of consecutive list blocks (of either marker type, each with one or
     * more lines) into a single markdown list. Each block carries its own marker (bullet vs
     * number). Ordered items are numbered <em>per nesting level</em>: descending to a deeper
     * level restarts that level at 1, and a shallower/equal level resumes its running count —
     * so a numbered list resumes after a nested sublist (even an unordered one). Mirrors the
     * editor's on-screen numbering.
     */
    private static void serializeListGroup(StringBuilder sb, List<FormattedBlock> group) {
        int[] counters = new int[MAX_LIST_DEPTH];
        int prevIndent = -1;
        boolean first = true;
        for (FormattedBlock block : group) {
            int depth = Math.max(0, Math.min(block.getIndent(), MAX_LIST_DEPTH - 1));
            boolean ordered = (block.getType() == FormattedBlock.BlockType.OLIST);
            // Descending resets every level deeper than the one we came from (so a new sublevel
            // starts at 1), regardless of marker type; a shallower/equal level is left running.
            if ((prevIndent >= 0) && (depth > prevIndent)) {
                for (int j = prevIndent + 1; j < counters.length; j++)
                    counters[j] = 0;
            }
            String indent = "  ".repeat(depth);
            for (FormattedLine line : block.getLines()) {
                if (!first)
                    sb.append("\n");
                first = false;
                sb.append(indent);
                if (ordered)
                    sb.append(++counters[depth]).append(". ");
                else
                    sb.append("- ");
                sb.append(serializeLine(line));
            }
            prevIndent = depth;
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
        String alignMeta = (table.getMeta() != null) ? table.meta("align") : null;
        String[] align = ((alignMeta != null) && !alignMeta.isEmpty()) ? alignMeta.split(",") : new String[0];
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
                int cols = row.getBlocks().size();
                for (int c = 0; c < cols; c++) {
                    String a = (c < align.length) ? align[c] : "L";
                    sb.append(" ").append(tableSeparator(a)).append(" |");
                }
            }
        }
    }

    /** The header-separator token for a parsed column alignment ({@code C}, {@code R}, else left). */
    private static String tableSeparator(String align) {
        if ("C".equals(align))
            return ":---:";
        if ("R".equals(align))
            return "---:";
        return "---";
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
        if (text == null)
            text = "";
        List<FormattedLine.Format> formatting = line.getFormatting();
        if (text.isEmpty()) {
            // An otherwise-empty line may still carry an inline image (a zero-length
            // IMG format), which must survive serialisation as ![](src).
            if ((formatting != null) && hasLinkOrImage(formatting))
                return serializeInline(text, line);
            return "";
        }
        if ((formatting == null) || formatting.isEmpty())
            return text;
        return serializeInline(text, line);
    }

    /**
     * Serializes a line's text with its inline formatting, links and images in a single pass.
     * <p>
     * Inline format markers (bold/italic/strike/code) are opened and closed by the run of
     * characters they cover — and, crucially, are kept open <em>across</em> a link. So an
     * emphasised span that contains a link round-trips as {@code *… [label](url) …*} rather
     * than being split into separate emphasis runs on either side of the link (which would
     * leave a space against a closing marker and re-parse oddly). Links become
     * {@code [label](url)}; images become {@code ![alt](src){attrs}} (the image's sentinel
     * character is not itself emitted).
     */
    private static String serializeInline(String text, FormattedLine line) {
        int n = text.length();
        List<FormattedLine.Format> formatting = line.getFormatting();

        // Per-character inline formats, excluding A/IMG (those become link/image syntax).
        @SuppressWarnings("unchecked")
        List<FormatType>[] charFormats = new List[n];
        for (int i = 0; i < n; i++)
            charFormats[i] = new ArrayList<>();
        for (FormattedLine.Format fmt : formatting) {
            int start = Math.max(0, fmt.getIndex());
            int end = Math.min(fmt.getIndex() + fmt.getLength(), n);
            for (int i = start; i < end; i++) {
                for (FormatType ft : fmt.getFormats()) {
                    // A/IMG become link/image syntax, not markers — but a link/image may also
                    // carry inline formats (an emphasised link), which still style the label.
                    if ((ft == FormatType.A) || (ft == FormatType.IMG))
                        continue;
                    if (!charFormats[i].contains(ft))
                        charFormats[i].add(ft);
                }
            }
        }

        // Link / image regions in start order.
        List<FormattedLine.Format> regions = new ArrayList<>();
        for (FormattedLine.Format fmt : formatting) {
            if (fmt.getFormats().contains(FormatType.A) || fmt.getFormats().contains(FormatType.IMG))
                regions.add(fmt);
        }
        regions.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));

        StringBuilder sb = new StringBuilder();
        List<FormatType> active = new ArrayList<>();
        int regionIdx = 0;
        int linkEnd = -1;
        String linkUrl = null;

        int i = 0;
        while (i < n) {
            // Close a pending link at its end boundary before this character's format changes,
            // so a format that continues past the link stays open around it.
            if (i == linkEnd) {
                sb.append("](").append((linkUrl != null) ? linkUrl : "").append(")");
                linkEnd = -1;
            }

            // Inline format transitions for this character (driven by the character's own
            // non-link formats — so a format ending exactly at a link/image closes here).
            closeFormats(sb, active, charFormats[i]);
            openFormats(sb, active, charFormats[i]);

            FormattedLine.Format region = (regionIdx < regions.size()) ? regions.get(regionIdx) : null;

            // An image at this position: emit ![alt](src){attrs} and skip its sentinel char(s).
            if ((region != null) && (region.getIndex() == i) && region.getFormats().contains(FormatType.IMG)) {
                int end = Math.min(region.getIndex() + region.getLength(), n);
                appendImage(sb, region, text, i, end);
                regionIdx++;
                i = Math.max(i + 1, end);
                continue;
            }

            // A link starting here: open '[' (after any format markers) and schedule its close.
            if ((region != null) && (region.getIndex() == i) && region.getFormats().contains(FormatType.A)) {
                sb.append("[");
                linkEnd = Math.min(region.getIndex() + region.getLength(), n);
                linkUrl = (region.getMeta() != null) ? region.getMeta().get(FormattedLine.META_LINK) : null;
                regionIdx++;
            }

            sb.append(text.charAt(i));
            i++;
        }

        // A link (or an active format) that runs to the very end of the line.
        if (linkEnd == n)
            sb.append("](").append((linkUrl != null) ? linkUrl : "").append(")");
        closeAllFormats(sb, active);

        // A trailing zero-length image region (an image on an otherwise-empty line).
        while (regionIdx < regions.size()) {
            FormattedLine.Format region = regions.get(regionIdx);
            if (region.getFormats().contains(FormatType.IMG)) {
                int start = Math.min(region.getIndex(), n);
                int end = Math.min(region.getIndex() + region.getLength(), n);
                appendImage(sb, region, text, start, end);
            }
            regionIdx++;
        }

        return sb.toString();
    }

    /** Emits an image as {@code ![alt](src){attrs}} from an IMG format's span and meta. */
    private static void appendImage(StringBuilder sb, FormattedLine.Format region, String text, int start, int end) {
        String src = (region.getMeta() != null) ? region.getMeta().get(FormattedLine.META_IMAGE) : null;
        // The alt text is meta; the span text is the sentinel character (never emitted).
        // Legacy alt-as-span-text content falls back to the span (with the sentinel stripped).
        String alt = (region.getMeta() != null) ? region.getMeta().get(FormattedLine.META_ALT) : null;
        if (alt == null) {
            String span = (start < end) ? text.substring(start, end) : "";
            alt = span.replace(FormattedLine.IMAGE_SENTINEL, "");
        }
        sb.append("![").append(alt).append("](").append((src != null) ? src : "").append(")");
        sb.append(imageAttributes(region));
    }

    /**
     * Determines whether any of the formats is a link or an image.
     */
    private static boolean hasLinkOrImage(List<FormattedLine.Format> formatting) {
        for (FormattedLine.Format fmt : formatting) {
            if (fmt.getFormats().contains(FormatType.A) || fmt.getFormats().contains(FormatType.IMG))
                return true;
        }
        return false;
    }

    /**
     * Builds the optional image attribute suffix ({@code {width=W height=H align=A}})
     * from an image format's meta, or an empty string when it carries none.
     */
    private static String imageAttributes(FormattedLine.Format fmt) {
        if (fmt.getMeta() == null)
            return "";
        StringBuilder attrs = new StringBuilder();
        appendAttribute(attrs, "width", fmt.getMeta().get(FormattedLine.META_WIDTH));
        appendAttribute(attrs, "height", fmt.getMeta().get(FormattedLine.META_HEIGHT));
        appendAttribute(attrs, "align", fmt.getMeta().get(FormattedLine.META_ALIGN));
        appendAttribute(attrs, "margin", fmt.getMeta().get(FormattedLine.META_MARGIN));
        if (attrs.length() == 0)
            return "";
        return "{" + attrs + "}";
    }

    /**
     * Appends a {@code key=value} attribute (space-separated) when the value is present.
     */
    private static void appendAttribute(StringBuilder sb, String key, String value) {
        if ((value == null) || value.isEmpty())
            return;
        if (sb.length() > 0)
            sb.append(' ');
        sb.append(key).append('=').append(value);
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
