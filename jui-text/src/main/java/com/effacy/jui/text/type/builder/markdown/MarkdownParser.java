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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.builder.FormattedTextBuilder;
import com.effacy.jui.text.type.builder.IEventBuilder;
import com.effacy.jui.text.type.FormattedText;

/**
 * Parses markdown content, emitting events to an {@link IEventBuilder}.
 * <p>
 * Configure optional parameters then call {@link #parse(IEventBuilder, String...)}:
 * <pre>
 *   new MarkdownParser()
 *       .partial(true)
 *       .lineProcessor(line -&gt; line.trim())
 *       .variableResolver(name -&gt; lookupValue(name))
 *       .parse(handler, content);
 * </pre>
 * <p>
 * Static convenience methods are provided for producing {@link FormattedText}
 * directly:
 * <pre>
 *   FormattedText result = MarkdownParser.markdown("# Hello **world**");
 * </pre>
 *
 * @see IEventBuilder
 * @see FormattedTextBuilder
 */
public class MarkdownParser {

    /**
     * Convenience to create a parser and perform a parse.
     */
    public static <T> T parse(Consumer<MarkdownParser> parser, IEventBuilder<T> handler, String... content) {
        MarkdownParser markdownParser = new MarkdownParser();
        if (parser != null)
            parser.accept(markdownParser);
        return markdownParser.parse(handler, content);
    }

    /************************************************************************
     * Instance (builder) API
     ************************************************************************/

    /**
     * See {@link #partial(boolean)}.
     */
    private boolean partial;

    /**
     * See {@link #lineProcessor(Function)}.
     */
    private Function<String, String> lineProcessor;

    /**
     * See {@link #variableResolver(Function)}.
     */
    private Function<String, String> variableResolver;

    /**
     * The type of URL being resolved.
     */
    public enum UrlType {

        /**
         * A link URL ({@code [label](url)}).
         */
        LINK,

        /**
         * An image source URL ({@code ![alt](url)}).
         */
        IMAGE;
    }

    /**
     * See {@link #urlResolver(BiFunction)}.
     */
    private BiFunction<String, UrlType, String> urlResolver;

    /**
     * Marks the content as potentially incomplete (e.g. streaming). Unclosed
     * format markers on the last line will be treated as formatting rather than
     * literal text.
     *
     * @param partial
     *                {@code true} if the content may be incomplete.
     * @return this parser for chaining.
     */
    public MarkdownParser partial(boolean partial) {
        this.partial = partial;
        return this;
    }

    /**
     * Assigns a line processor that transforms each line before parsing.
     *
     * @param lineProcessor
     *                      the line processor function.
     * @return this parser for chaining.
     */
    public MarkdownParser lineProcessor(Function<String, String> lineProcessor) {
        this.lineProcessor = lineProcessor;
        return this;
    }

    /**
     * Assigns a variable resolver that maps variable names to replacement text.
     * When a {@code {{name}}} variable is encountered and the resolver returns a
     * non-null value, the replacement text is emitted as plain text instead of a
     * variable event. If the resolver returns {@code null}, the variable passes
     * through unchanged.
     *
     * @param variableResolver
     *                         the resolver function.
     * @return this parser for chaining.
     */
    public MarkdownParser variableResolver(Function<String, String> variableResolver) {
        this.variableResolver = variableResolver;
        return this;
    }

    /**
     * Assigns a URL resolver that transforms link and image URLs. When a link
     * {@code [label](url)} or image {@code ![alt](url)} is encountered, the
     * resolver is called with the URL and a {@link UrlType} indicating whether
     * it is a link or an image. If it returns a non-null value, that value
     * replaces the original URL. If it returns {@code null}, the original URL
     * is used unchanged.
     *
     * @param urlResolver
     *                    the resolver function (url, type) → mapped url.
     * @return this parser for chaining.
     */
    public MarkdownParser urlResolver(BiFunction<String, UrlType, String> urlResolver) {
        this.urlResolver = urlResolver;
        return this;
    }

    /**
     * Parses the given markdown content blocks, emitting events to the handler.
     *
     * @param <T>
     *                the type of the handler's result.
     * @param handler
     *                the handler to receive parse events.
     * @param content
     *                the markdown content blocks to parse.
     * @return the handler's result.
     */
    public <T> T parse(IEventBuilder<T> handler, String... content) {
        if ((handler == null) || (content == null) || (content.length == 0))
            return handler.result();
        handler.commence();
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < content.length; i++) {
            if (content[i] != null) {
                if ((combined.length() > 0) && !combined.toString().endsWith("\n\n"))
                    combined.append("\n\n");
                combined.append(content[i]);
            }
        }
        parseBlocks(combined.toString(), lineProcessor, handler, partial);
        return handler.result();
    }

    /************************************************************************
     * Block-level parsing.
     ************************************************************************/

    private void parseBlocks(String markdown, Function<String, String> lineProcessor, IEventBuilder<?> handler, boolean partial) {
        if ((markdown == null) || markdown.isEmpty())
            return;
        List<ParsedBlock> blocks = splitBlocks(markdown);

        // Determine the index of the last non-empty block for partial handling.
        int lastBlockIndex = partial ? (blocks.size() - 1) : -1;

        for (int p = 0; p < blocks.size(); p++) {
            ParsedBlock block = blocks.get(p);
            boolean partialBlock = (p == lastBlockIndex);

            if (block.code) {
                emitCodeBlock(handler, block, partialBlock);
                continue;
            }

            String[] lines = block.lines.toArray(new String[0]);

            // Apply line processor if provided.
            if (lineProcessor != null) {
                for (int i = 0; i < lines.length; i++) {
                    String processed = lineProcessor.apply(lines[i]);
                    lines[i] = (processed == null) ? "" : processed;
                }
            }

            // Skip if all lines are empty after processing.
            boolean allEmpty = true;
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty)
                continue;

            if ((lines.length == 1) && lines[0].trim().startsWith("#")) {
                emitHeading(handler, lines[0], partialBlock);
            } else if (isTableBlock(lines)) {
                emitTable(handler, lines, partialBlock);
            } else if (isListBlock(lines)) {
                emitList(handler, lines, partialBlock);
            } else {
                // Check for a list starting mid-paragraph (e.g. intro text
                // followed by list items without a blank line separator).
                int listStart = findListTransition(lines);
                if (listStart > 0) {
                    handler.startBlock(BlockType.PARA);
                    for (int l = 0; l < listStart; l++) {
                        handler.startLine();
                        if (!lines[l].isEmpty())
                            emitLineContent(handler, lines[l], false);
                        handler.endLine();
                    }
                    handler.endBlock(BlockType.PARA);

                    // Find where the contiguous list items end.
                    int listEnd = lines.length;
                    for (int l = listStart; l < lines.length; l++) {
                        if (lines[l].trim().isEmpty())
                            continue;
                        if (!isListItem(lines[l])) {
                            listEnd = l;
                            break;
                        }
                    }

                    // Copy list lines; append any trailing non-list continuation
                    // to the last list item (single-newline continuation).
                    String[] listLines = new String[listEnd - listStart];
                    System.arraycopy(lines, listStart, listLines, 0, listLines.length);
                    if (listEnd < lines.length) {
                        StringBuilder continuation = new StringBuilder(listLines[listLines.length - 1]);
                        for (int l = listEnd; l < lines.length; l++) {
                            if (!lines[l].trim().isEmpty())
                                continuation.append(" ").append(lines[l].trim());
                        }
                        listLines[listLines.length - 1] = continuation.toString();
                    }
                    emitList(handler, listLines, partialBlock);
                } else {
                    handler.startBlock(BlockType.PARA);
                    for (int l = 0; l < lines.length; l++) {
                        boolean partialLine = partialBlock && (l == lines.length - 1);
                        handler.startLine();
                        if (!lines[l].isEmpty())
                            emitLineContent(handler, lines[l], partialLine);
                        handler.endLine();
                    }
                    handler.endBlock(BlockType.PARA);
                }
            }
        }
    }

    private List<ParsedBlock> splitBlocks(String markdown) {
        String[] rawLines = markdown.split("\\n", -1);
        List<ParsedBlock> blocks = new ArrayList<>();
        int i = 0;
        while (i < rawLines.length) {
            while ((i < rawLines.length) && rawLines[i].trim().isEmpty())
                i++;
            if (i >= rawLines.length)
                break;

            String trimmed = rawLines[i].trim();
            if (trimmed.startsWith("```")) {
                ParsedBlock block = new ParsedBlock();
                block.code = true;
                block.lang = trimmed.substring(3).trim();
                i++;
                while (i < rawLines.length) {
                    if (rawLines[i].trim().startsWith("```")) {
                        i++;
                        break;
                    }
                    block.lines.add(rawLines[i]);
                    i++;
                }
                blocks.add(block);
                continue;
            }

            ParsedBlock block = new ParsedBlock();
            while (i < rawLines.length) {
                if (rawLines[i].trim().startsWith("```"))
                    break;
                if (rawLines[i].trim().isEmpty())
                    break;
                block.lines.add(rawLines[i]);
                i++;
            }
            if (!block.lines.isEmpty())
                blocks.add(block);
        }

        // Merge consecutive list-only blocks so that blank lines between list
        // items do not restart the list numbering.
        List<ParsedBlock> merged = new ArrayList<>();
        for (ParsedBlock block : blocks) {
            if (block.code) {
                merged.add(block);
                continue;
            }
            if (!merged.isEmpty() && !merged.get(merged.size() - 1).code
                && isListBlock(merged.get(merged.size() - 1).lines.toArray(new String[0]))
                && isListBlock(block.lines.toArray(new String[0]))) {
                merged.get(merged.size() - 1).lines.addAll(block.lines);
            } else {
                merged.add(block);
            }
        }
        return merged;
    }

    private static boolean isFenceLine(String line) {
        return (line != null) && line.trim().startsWith("```");
    }

    private void emitCodeBlock(IEventBuilder<?> handler, ParsedBlock block, boolean partial) {
        handler.startBlock(BlockType.CODE);
        if ((block.lang != null) && !block.lang.isEmpty())
            handler.meta("lang", block.lang);
        for (int i = 0; i < block.lines.size(); i++) {
            boolean partialLine = partial && (i == (block.lines.size() - 1));
            handler.startLine();
            if (!block.lines.get(i).isEmpty())
                handler.text(block.lines.get(i));
            else if (partialLine && partial)
                handler.text("");
            handler.endLine();
        }
        handler.endBlock(BlockType.CODE);
    }

    private static class ParsedBlock {
        boolean code;
        String lang;
        List<String> lines = new ArrayList<>();
    }

    /************************************************************************
     * Heading.
     ************************************************************************/

    private void emitHeading(IEventBuilder<?> handler, String line, boolean partial) {
        String trimmed = line.trim();
        BlockType headingType = BlockType.PARA;
        String content = trimmed;

        if (trimmed.startsWith("##### ")) {
            headingType = BlockType.H5;
            content = trimmed.substring(6);
        } else if (trimmed.startsWith("#### ")) {
            headingType = BlockType.H4;
            content = trimmed.substring(5);
        } else if (trimmed.startsWith("### ")) {
            headingType = BlockType.H3;
            content = trimmed.substring(4);
        } else if (trimmed.startsWith("## ")) {
            headingType = BlockType.H2;
            content = trimmed.substring(3);
        } else if (trimmed.startsWith("# ")) {
            headingType = BlockType.H1;
            content = trimmed.substring(2);
        }

        handler.startBlock(headingType);
        handler.startLine();
        emitLineContent(handler, content, partial);
        handler.endLine();
        handler.endBlock(headingType);
    }

    /************************************************************************
     * List.
     ************************************************************************/

    private static int findListTransition(String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty())
                continue;
            if (!isListItem(lines[i]))
                continue;
            boolean contiguous = true;
            boolean seenNonList = false;
            for (int j = i + 1; j < lines.length; j++) {
                if (lines[j].trim().isEmpty())
                    continue;
                if (isListItem(lines[j])) {
                    if (seenNonList) {
                        contiguous = false;
                        break;
                    }
                } else {
                    seenNonList = true;
                }
            }
            if (contiguous)
                return i;
        }
        return -1;
    }

    private static boolean isListBlock(String[] lines) {
        if ((lines == null) || (lines.length == 0))
            return false;
        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;
            if (!isListItem(line))
                return false;
        }
        return true;
    }

    private static boolean isListItem(String line) {
        String trimmed = line.trim();
        if (trimmed.length() < 2)
            return false;

        char first = trimmed.charAt(0);
        if (((first == '-') || (first == '*') || (first == '+')) && Character.isWhitespace(trimmed.charAt(1)))
            return true;

        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            for (int i = 0; i < dotIndex; i++) {
                if (!Character.isDigit(trimmed.charAt(i)))
                    return false;
            }
            if (((dotIndex + 1) < trimmed.length()) && Character.isWhitespace(trimmed.charAt(dotIndex + 1)))
                return true;
        }

        return false;
    }

    private void emitList(IEventBuilder<?> handler, String[] lines, boolean partial) {
        for (int l = 0; l < lines.length; l++) {
            if (lines[l].trim().isEmpty())
                continue;

            boolean partialLine = partial && (l == lines.length - 1);

            // Determine indent level from leading whitespace (2+ spaces or 1 tab = 1 level).
            int spaces = 0;
            for (int i = 0; i < lines[l].length(); i++) {
                if (lines[l].charAt(i) == ' ')
                    spaces++;
                else if (lines[l].charAt(i) == '\t')
                    spaces += 3;
                else
                    break;
            }
            int indent = (spaces + 1) / 3;

            String trimmed = lines[l].trim();
            String content = "";
            boolean ordered = false;

            char first = trimmed.charAt(0);
            if ((first == '-') || (first == '*') || (first == '+')) {
                content = trimmed.substring(trimmed.indexOf(' ') + 1);
            } else {
                int dotIndex = trimmed.indexOf('.');
                if (dotIndex > 0) {
                    content = trimmed.substring(dotIndex + 1).trim();
                    ordered = true;
                }
            }

            BlockType type = ordered ? BlockType.OLIST : BlockType.NLIST;
            handler.startBlock(type);
            if (indent > 0)
                handler.meta("indent", String.valueOf(indent));
            handler.startLine();
            emitLineContent(handler, content, partialLine);
            handler.endLine();
            handler.endBlock(type);
        }
    }

    /************************************************************************
     * Table.
     ************************************************************************/

    private static boolean isTableBlock(String[] lines) {
        if ((lines == null) || (lines.length < 2))
            return false;
        if (!isTableRow(lines[0]))
            return false;
        if (!isTableSeparator(lines[1]))
            return false;
        for (int i = 2; i < lines.length; i++) {
            if (lines[i].trim().isEmpty())
                continue;
            if (!isTableRow(lines[i]))
                return false;
        }
        return true;
    }

    private static boolean isTableRow(String line) {
        String trimmed = line.trim();
        return (trimmed.length() >= 3) && (trimmed.charAt(0) == '|');
    }

    private static boolean isTableSeparator(String line) {
        String trimmed = line.trim();
        if ((trimmed.length() < 3) || (trimmed.charAt(0) != '|'))
            return false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if ((c != '|') && (c != '-') && (c != ':') && !Character.isWhitespace(c))
                return false;
        }
        return trimmed.indexOf('-') >= 0;
    }

    private void emitTable(IEventBuilder<?> handler, String[] lines, boolean partial) {
        String[] separatorCells = splitTableRow(lines[1]);
        int columns = separatorCells.length;
        String[] alignments = new String[columns];
        for (int i = 0; i < columns; i++) {
            String cell = separatorCells[i].trim();
            boolean leftColon = cell.startsWith(":");
            boolean rightColon = cell.endsWith(":");
            if (leftColon && rightColon)
                alignments[i] = "C";
            else if (rightColon)
                alignments[i] = "R";
            else
                alignments[i] = "L";
        }

        handler.startBlock(BlockType.TABLE);
        handler.meta("columns", String.valueOf(columns));
        handler.meta("headers", "1");

        StringBuilder align = new StringBuilder();
        for (int i = 0; i < alignments.length; i++) {
            if (i > 0)
                align.append(",");
            align.append(alignments[i]);
        }
        handler.meta("align", align.toString());

        emitTableRow(handler, lines[0], columns);

        for (int i = 2; i < lines.length; i++) {
            if (lines[i].trim().isEmpty())
                continue;
            emitTableRow(handler, lines[i], columns);
        }

        handler.endBlock(BlockType.TABLE);
    }

    private void emitTableRow(IEventBuilder<?> handler, String line, int columns) {
        handler.startBlock(BlockType.TROW);

        String[] cells = splitTableRow(line);
        for (int i = 0; i < columns; i++) {
            handler.startBlock(BlockType.TCELL);
            if (i < cells.length) {
                String cellContent = cells[i].trim();
                if (!cellContent.isEmpty()) {
                    handler.startLine();
                    emitLineContent(handler, cellContent, false);
                    handler.endLine();
                }
            }
            handler.endBlock(BlockType.TCELL);
        }

        handler.endBlock(BlockType.TROW);
    }

    private static String[] splitTableRow(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("|"))
            trimmed = trimmed.substring(1);
        if (trimmed.endsWith("|"))
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed.split("\\|", -1);
    }

    /************************************************************************
     * Inline content.
     ************************************************************************/

    private void emitLineContent(IEventBuilder<?> handler, String line, boolean partial) {
        List<LinkInfo> links = findLinks(line);
        List<VariableInfo> variables = findVariables(line);

        // Find *** (bold+italic) regions first and emit them directly,
        // splitting the line into segments for normal processing.
        List<int[]> boldItalicRegions = findBoldItalicRegions(line);
        if (!boldItalicRegions.isEmpty()) {
            int pos = 0;
            for (int[] region : boldItalicRegions) {
                if (region[0] > pos)
                    emitLineContent(handler, line.substring(pos, region[0]), false);
                handler.formatted(line.substring(region[0] + 3, region[1]), FormatType.BLD, FormatType.ITL);
                pos = region[1] + 3;
            }
            if (pos < line.length())
                emitLineContent(handler, line.substring(pos), partial);
            return;
        }

        List<FormatMarker> markers = new ArrayList<>();
        findMarkers(line, "**", FormatType.BLD, markers);
        findMarkers(line, "__", FormatType.BLD, markers);
        findMarkers(line, "~~", FormatType.STR, markers);
        findMarkers(line, "`", FormatType.CODE, markers);
        findItalicMarkers(line, markers);

        removeMarkersWithinLinks(markers, links);
        removeVariablesWithinLinks(variables, links);
        removeMarkersWithinVariables(markers, variables);

        markers.sort((a, b) -> a.position - b.position);

        int textPos = 0;
        int i = 0;
        int linkIdx = 0;
        int varIdx = 0;

        FormatMarker lastUnmatched = null;

        while ((i < markers.size()) || (linkIdx < links.size()) || (varIdx < variables.size())) {
            FormatMarker start = (i < markers.size()) ? markers.get(i) : null;
            LinkInfo link = (linkIdx < links.size()) ? links.get(linkIdx) : null;
            VariableInfo variable = (varIdx < variables.size()) ? variables.get(varIdx) : null;

            int startPos = (start != null) ? start.position : Integer.MAX_VALUE;
            int linkPos = (link != null) ? link.startPos : Integer.MAX_VALUE;
            int varPos = (variable != null) ? variable.startPos : Integer.MAX_VALUE;

            if ((varPos <= startPos) && (varPos <= linkPos) && (variable != null)) {
                if (variable.startPos > textPos)
                    handler.text(line.substring(textPos, variable.startPos));

                String resolved = (variableResolver != null) ? variableResolver.apply(variable.name) : null;
                if (resolved != null)
                    handler.text(resolved);
                else
                    handler.variable(variable.name, variable.meta);

                textPos = variable.endPos;
                varIdx++;
            } else if ((linkPos <= startPos) && (link != null)) {
                if (link.startPos > textPos)
                    handler.text(line.substring(textPos, link.startPos));

                String resolvedUrl = link.url;
                if (urlResolver != null) {
                    String mapped = urlResolver.apply(resolvedUrl, link.image ? UrlType.IMAGE : UrlType.LINK);
                    if (mapped != null)
                        resolvedUrl = mapped;
                }
                if (link.image)
                    handler.image(link.label, resolvedUrl, link.width, link.height);
                else
                    handler.link(link.label, resolvedUrl);

                textPos = link.endPos;
                linkIdx++;
            } else if (start != null) {
                FormatMarker end = findMatchingEnd(markers, i);

                if (end != null) {
                    if (start.position > textPos)
                        handler.text(line.substring(textPos, start.position));

                    int contentStart = start.position + start.marker.length();
                    int contentEnd = end.position;
                    int endIndex = markers.indexOf(end);
                    emitFormattedSpan(handler, line, contentStart, contentEnd, markers, i + 1, endIndex, start.type);

                    textPos = end.position + end.marker.length();
                    i = endIndex + 1;
                } else {
                    if (partial && (start.position >= textPos))
                        lastUnmatched = start;
                    i++;
                }
            }
        }

        if (textPos < line.length()) {
            if ((lastUnmatched != null) && (lastUnmatched.position >= textPos)) {
                if (lastUnmatched.position > textPos)
                    handler.text(line.substring(textPos, lastUnmatched.position));
                int contentStart = lastUnmatched.position + lastUnmatched.marker.length();
                if (contentStart < line.length())
                    handler.formatted(line.substring(contentStart), lastUnmatched.type);
            } else {
                handler.text(line.substring(textPos));
            }
        }
    }

    /**
     * Emits a formatted span, checking for nested marker pairs inside the
     * region and splitting into segments when found. For example, the italic
     * span in {@code *text **bold** more*} emits three formatted events:
     * {@code formatted("text ", ITL)}, {@code formatted("bold", ITL, BLD)},
     * {@code formatted(" more", ITL)}.
     */
    private void emitFormattedSpan(IEventBuilder<?> handler, String line, int start, int end, List<FormatMarker> markers, int fromIdx, int toIdx, FormatType outerType) {
        // Look for matched pairs among markers between fromIdx and toIdx.
        int pos = start;
        int idx = fromIdx;
        boolean foundNested = false;
        while (idx < toIdx) {
            FormatMarker innerStart = markers.get(idx);
            if ((innerStart.position < start) || (innerStart.position >= end)) {
                idx++;
                continue;
            }
            FormatMarker innerEnd = null;
            for (int j = idx + 1; j < toIdx; j++) {
                FormatMarker candidate = markers.get(j);
                if ((candidate.type == innerStart.type) && candidate.marker.equals(innerStart.marker)) {
                    innerEnd = candidate;
                    break;
                }
            }
            if (innerEnd == null) {
                idx++;
                continue;
            }

            foundNested = true;

            // Text before inner pair — formatted with outer type only.
            if (innerStart.position > pos) {
                handler.formatted(line.substring(pos, innerStart.position), outerType);
            }

            // Inner content — formatted with both types.
            int innerContentStart = innerStart.position + innerStart.marker.length();
            int innerContentEnd = innerEnd.position;
            handler.formatted(line.substring(innerContentStart, innerContentEnd), outerType, innerStart.type);

            pos = innerEnd.position + innerEnd.marker.length();
            idx = markers.indexOf(innerEnd) + 1;
        }

        if (!foundNested) {
            // No nested markers — emit as a single formatted event.
            handler.formatted(line.substring(start, end), outerType);
        } else if (pos < end) {
            // Remaining text after the last nested pair.
            handler.formatted(line.substring(pos, end), outerType);
        }
    }

    /************************************************************************
     * Inline helpers.
     ************************************************************************/

    private static List<LinkInfo> findLinks(String line) {
        List<LinkInfo> links = new ArrayList<>();
        int pos = 0;
        while (pos < line.length()) {
            int labelStart = line.indexOf('[', pos);
            if (labelStart == -1)
                break;
            int labelEnd = line.indexOf(']', labelStart + 1);
            if (labelEnd == -1)
                break;
            if (((labelEnd + 1) < line.length()) && (line.charAt(labelEnd + 1) == '(')) {
                int urlStart = labelEnd + 2;
                int urlEnd = line.indexOf(')', urlStart);
                if (urlEnd != -1) {
                    boolean isImage = (labelStart > 0) && (line.charAt(labelStart - 1) == '!');
                    int startPos = isImage ? (labelStart - 1) : labelStart;
                    String rawUrl = line.substring(urlStart, urlEnd);
                    int imgWidth = -1;
                    int imgHeight = -1;
                    if (isImage) {
                        // Parse optional dimensions: "url =WxH", "url =W", "url =xH".
                        int eqIdx = rawUrl.lastIndexOf(" =");
                        if (eqIdx >= 0) {
                            String dims = rawUrl.substring(eqIdx + 2);
                            rawUrl = rawUrl.substring(0, eqIdx);
                            int xIdx = dims.indexOf('x');
                            if (xIdx < 0) {
                                // Width only: "=300"
                                imgWidth = parsePixelValue(dims);
                            } else if (xIdx == 0) {
                                // Height only: "=x200"
                                imgHeight = parsePixelValue(dims.substring(1));
                            } else {
                                // Both: "=300x200"
                                imgWidth = parsePixelValue(dims.substring(0, xIdx));
                                imgHeight = parsePixelValue(dims.substring(xIdx + 1));
                            }
                        }
                    }
                    links.add(new LinkInfo(startPos, urlEnd + 1, line.substring(labelStart + 1, labelEnd), rawUrl, isImage, imgWidth, imgHeight));
                    pos = urlEnd + 1;
                    continue;
                }
            }
            pos = labelStart + 1;
        }
        return links;
    }

    private static void removeMarkersWithinLinks(List<FormatMarker> markers, List<LinkInfo> links) {
        if (links.isEmpty())
            return;
        markers.removeIf(marker -> {
            for (LinkInfo link : links) {
                if ((marker.position >= link.startPos) && (marker.position < link.endPos))
                    return true;
            }
            return false;
        });
    }

    private static void removeVariablesWithinLinks(List<VariableInfo> variables, List<LinkInfo> links) {
        if (links.isEmpty())
            return;
        variables.removeIf(variable -> {
            for (LinkInfo link : links) {
                if ((variable.startPos >= link.startPos) && (variable.endPos <= link.endPos))
                    return true;
            }
            return false;
        });
    }

    private static void removeMarkersWithinVariables(List<FormatMarker> markers, List<VariableInfo> variables) {
        if (variables.isEmpty())
            return;
        markers.removeIf(marker -> {
            for (VariableInfo var : variables) {
                if ((marker.position >= var.startPos) && (marker.position < var.endPos))
                    return true;
            }
            return false;
        });
    }

    private static void findMarkers(String line, String marker, FormatType type, List<FormatMarker> markers) {
        int pos = 0;
        while ((pos = line.indexOf(marker, pos)) != -1) {
            markers.add(new FormatMarker(pos, marker, type));
            pos += marker.length();
        }
    }

    private static void findItalicMarkers(String line, List<FormatMarker> markers) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '*') {
                boolean prevStar = (i > 0) && (line.charAt(i - 1) == '*');
                boolean nextStar = ((i + 1) < line.length()) && (line.charAt(i + 1) == '*');
                if (!prevStar && !nextStar)
                    markers.add(new FormatMarker(i, "*", FormatType.ITL));
            } else if (c == '_') {
                boolean prevUnderscore = (i > 0) && (line.charAt(i - 1) == '_');
                boolean nextUnderscore = ((i + 1) < line.length()) && (line.charAt(i + 1) == '_');
                if (prevUnderscore || nextUnderscore)
                    continue;
                boolean prevWord = (i > 0) && Character.isLetterOrDigit(line.charAt(i - 1));
                boolean nextWord = ((i + 1) < line.length()) && Character.isLetterOrDigit(line.charAt(i + 1));
                if (prevWord && nextWord)
                    continue;
                markers.add(new FormatMarker(i, "_", FormatType.ITL));
            }
        }
    }

    private static List<int[]> findBoldItalicRegions(String line) {
        List<int[]> regions = new ArrayList<>();
        int pos = 0;
        while (pos < line.length() - 2) {
            int start = line.indexOf("***", pos);
            if (start == -1)
                break;
            boolean prevStar = (start > 0) && (line.charAt(start - 1) == '*');
            int endRun = start + 3;
            while ((endRun < line.length()) && (line.charAt(endRun) == '*'))
                endRun++;
            if (prevStar || ((endRun - start) != 3)) {
                pos = endRun;
                continue;
            }
            int close = line.indexOf("***", start + 3);
            if (close == -1)
                break;
            int closeEnd = close + 3;
            while ((closeEnd < line.length()) && (line.charAt(closeEnd) == '*'))
                closeEnd++;
            if ((closeEnd - close) != 3) {
                pos = closeEnd;
                continue;
            }
            regions.add(new int[] { start, close });
            pos = close + 3;
        }
        return regions;
    }

    private static FormatMarker findMatchingEnd(List<FormatMarker> markers, int startIndex) {
        FormatMarker start = markers.get(startIndex);
        for (int i = startIndex + 1; i < markers.size(); i++) {
            FormatMarker candidate = markers.get(i);
            if ((candidate.type == start.type) && candidate.marker.equals(start.marker))
                return candidate;
        }
        return null;
    }

    private record LinkInfo(int startPos, int endPos, String label, String url, boolean image, int width, int height) {}

    private record FormatMarker(int position, String marker, FormatType type) {}

    private record VariableInfo(int startPos, int endPos, String name, Map<String, String> meta) {}

    private static List<VariableInfo> findVariables(String line) {
        List<VariableInfo> variables = new ArrayList<>();
        int pos = 0;
        while (pos < line.length() - 3) {
            int start = line.indexOf("{{", pos);
            if (start == -1)
                break;
            int end = line.indexOf("}}", start + 2);
            if (end == -1)
                break;
            String content = line.substring(start + 2, end);
            VariableInfo varInfo = parseVariableContent(start, end + 2, content);
            if (varInfo != null)
                variables.add(varInfo);
            pos = end + 2;
        }
        return variables;
    }

    private static VariableInfo parseVariableContent(int startPos, int endPos, String content) {
        if ((content == null) || content.isEmpty())
            return null;
        Map<String, String> meta = new LinkedHashMap<>();
        String[] parts = content.split(";");
        String name = parts[0].trim();
        if (!isValidVariableName(name))
            return null;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            int eqPos = part.indexOf('=');
            if (eqPos > 0) {
                String key = part.substring(0, eqPos).trim();
                String value = part.substring(eqPos + 1);
                if (isValidMetaFieldName(key) && isValidMetaValue(value))
                    meta.put(key, value);
            }
        }
        return new VariableInfo(startPos, endPos, name, meta);
    }

    private static boolean isValidVariableName(String name) {
        if ((name == null) || name.isEmpty())
            return false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && (c != '-') && (c != '_') && (c != '.') && (c != '$') && (c != ':'))
                return false;
        }
        return true;
    }

    private static boolean isValidMetaFieldName(String name) {
        if ((name == null) || name.isEmpty())
            return false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && (c != '.'))
                return false;
        }
        return true;
    }

    private static boolean isValidMetaValue(String value) {
        if (value == null)
            return false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c == '\n') || (c == '\r') || (c == '}'))
                return false;
        }
        return true;
    }

    /**
     * Parses a pixel dimension value from a string. Returns {@code -1} if the
     * string is empty or not a valid positive integer.
     */
    private static int parsePixelValue(String value) {
        if ((value == null) || value.isEmpty())
            return -1;
        try {
            int v = Integer.parseInt(value);
            return (v > 0) ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
