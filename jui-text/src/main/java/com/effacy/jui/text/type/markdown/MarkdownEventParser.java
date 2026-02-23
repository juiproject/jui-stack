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
package com.effacy.jui.text.type.markdown;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

/**
 * Event-based markdown parser that emits structural events to an
 * {@link IMarkdownEventHandler} rather than building objects directly. This allows
 * different output representations (e.g. {@link FormattedText}, a DOM tree, an
 * HTML string) to be produced from the same parsing logic.
 * <p>
 * Supports the same syntax as {@link MarkdownParser}: headings, lists, tables,
 * bold, italic, strikethrough, code, links, and variables.
 *
 * @see IMarkdownEventHandler
 * @see MarkdownParser
 */
public class MarkdownEventParser {

    /**
     * Parses markdown content and emits events to the handler.
     *
     * @param handler
     *                the handler to receive events.
     * @param content
     *                the markdown content blocks to parse.
     */
    public static void parse(IMarkdownEventHandler handler, String... content) {
        parse(handler, false, null, content);
    }

    /**
     * Parses markdown content and emits events to the handler.
     *
     * @param handler
     *                the handler to receive events.
     * @param partial
     *                {@code true} if the content may be incomplete (e.g. streaming).
     *                Unclosed format markers on the last line will be treated as
     *                formatting rather than literal text.
     * @param content
     *                the markdown content blocks to parse.
     */
    public static void parse(IMarkdownEventHandler handler, boolean partial, String... content) {
        parse(handler, partial, null, content);
    }

    /**
     * Parses markdown content with an optional line processor and emits events to
     * the handler.
     *
     * @param handler
     *                      the handler to receive events.
     * @param lineProcessor
     *                      optional function to process each line before parsing.
     * @param content
     *                      the markdown content blocks to parse.
     */
    public static void parse(IMarkdownEventHandler handler, Function<String, String> lineProcessor, String... content) {
        parse(handler, false, lineProcessor, content);
    }

    /**
     * Parses markdown content with an optional line processor and emits events to
     * the handler.
     *
     * @param handler
     *                      the handler to receive events.
     * @param partial
     *                      {@code true} if the content may be incomplete (e.g.
     *                      streaming). Unclosed format markers on the last line will
     *                      be treated as formatting rather than literal text.
     * @param lineProcessor
     *                      optional function to process each line before parsing.
     * @param content
     *                      the markdown content blocks to parse.
     */
    public static void parse(IMarkdownEventHandler handler, boolean partial, Function<String, String> lineProcessor, String... content) {
        if ((handler == null) || (content == null) || (content.length == 0))
            return;

        // Join all content blocks with double newlines to preserve block separation.
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < content.length; i++) {
            if (content[i] != null) {
                if ((combined.length() > 0) && !combined.toString().endsWith("\n\n"))
                    combined.append("\n\n");
                combined.append(content[i]);
            }
        }

        parseInternal(combined.toString(), lineProcessor, handler, partial);
    }

    /************************************************************************
     * Internal parsing.
     ************************************************************************/

    private static void parseInternal(String markdown, Function<String, String> lineProcessor, IMarkdownEventHandler handler, boolean partial) {
        if ((markdown == null) || markdown.isEmpty())
            return;

        String[] paragraphs = markdown.split("\\n\\n+");

        // Determine the index of the last non-empty paragraph for partial handling.
        int lastParaIndex = -1;
        if (partial) {
            for (int p = paragraphs.length - 1; p >= 0; p--) {
                if (!paragraphs[p].trim().isEmpty()) {
                    lastParaIndex = p;
                    break;
                }
            }
        }

        for (int p = 0; p < paragraphs.length; p++) {
            String para = paragraphs[p];
            if (para.trim().isEmpty())
                continue;

            boolean partialBlock = (p == lastParaIndex);

            String[] lines = para.split("\\n");

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
                    String[] listLines = new String[lines.length - listStart];
                    System.arraycopy(lines, listStart, listLines, 0, listLines.length);
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

    /************************************************************************
     * Heading.
     ************************************************************************/

    private static void emitHeading(IMarkdownEventHandler handler, String line, boolean partial) {
        String trimmed = line.trim();
        BlockType headingType = BlockType.PARA;
        String content = trimmed;

        if (trimmed.startsWith("### ")) {
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

    /**
     * Finds the index of the first line that starts a list tail — i.e. a list
     * item where all subsequent non-empty lines are also list items. Returns
     * {@code -1} if no such transition exists. Starts scanning at index 1 so
     * that a block whose first line is already a list item (handled by
     * {@link #isListBlock}) is not matched here.
     */
    private static int findListTransition(String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty())
                continue;
            if (!isListItem(lines[i]))
                continue;
            boolean allList = true;
            for (int j = i + 1; j < lines.length; j++) {
                if (lines[j].trim().isEmpty())
                    continue;
                if (!isListItem(lines[j])) {
                    allList = false;
                    break;
                }
            }
            if (allList)
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

    private static void emitList(IMarkdownEventHandler handler, String[] lines, boolean partial) {
        for (int l = 0; l < lines.length; l++) {
            if (lines[l].trim().isEmpty())
                continue;

            boolean partialLine = partial && (l == lines.length - 1);
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

    private static void emitTable(IMarkdownEventHandler handler, String[] lines, boolean partial) {
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

        // Header row.
        emitTableRow(handler, lines[0], columns);

        // Body rows (skip separator at index 1).
        for (int i = 2; i < lines.length; i++) {
            if (lines[i].trim().isEmpty())
                continue;
            emitTableRow(handler, lines[i], columns);
        }

        handler.endBlock(BlockType.TABLE);
    }

    private static void emitTableRow(IMarkdownEventHandler handler, String line, int columns) {
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

    /**
     * Emits inline content events for a single line of markdown text.
     *
     * @param handler
     *                the handler to receive events.
     * @param line
     *                the line text.
     * @param partial
     *                {@code true} if this line may be incomplete. Unclosed format
     *                markers will be treated as formatting rather than literal text.
     */
    private static void emitLineContent(IMarkdownEventHandler handler, String line, boolean partial) {
        // Find links, variables, and format markers (same logic as MarkdownParser).
        List<LinkInfo> links = findLinks(line);
        List<VariableInfo> variables = findVariables(line);

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

        // Emit events left-to-right.
        int textPos = 0;
        int i = 0;
        int linkIdx = 0;
        int varIdx = 0;

        // Track the last unmatched opening marker for partial handling.
        FormatMarker lastUnmatched = null;

        while ((i < markers.size()) || (linkIdx < links.size()) || (varIdx < variables.size())) {
            FormatMarker start = (i < markers.size()) ? markers.get(i) : null;
            LinkInfo link = (linkIdx < links.size()) ? links.get(linkIdx) : null;
            VariableInfo variable = (varIdx < variables.size()) ? variables.get(varIdx) : null;

            int startPos = (start != null) ? start.position : Integer.MAX_VALUE;
            int linkPos = (link != null) ? link.startPos : Integer.MAX_VALUE;
            int varPos = (variable != null) ? variable.startPos : Integer.MAX_VALUE;

            if ((varPos <= startPos) && (varPos <= linkPos) && (variable != null)) {
                // Emit text before variable.
                if (variable.startPos > textPos)
                    handler.text(line.substring(textPos, variable.startPos));

                handler.variable(variable.name, variable.meta);

                textPos = variable.endPos;
                varIdx++;
            } else if ((linkPos <= startPos) && (link != null)) {
                // Emit text before link.
                if (link.startPos > textPos)
                    handler.text(line.substring(textPos, link.startPos));

                handler.link(link.label, link.url);

                textPos = link.endPos;
                linkIdx++;
            } else if (start != null) {
                FormatMarker end = findMatchingEnd(markers, i);

                if (end != null) {
                    // Emit text before formatted region.
                    if (start.position > textPos)
                        handler.text(line.substring(textPos, start.position));

                    // Emit formatted content.
                    int contentStart = start.position + start.marker.length();
                    int contentEnd = end.position;
                    handler.formatted(line.substring(contentStart, contentEnd), start.type);

                    textPos = end.position + end.marker.length();
                    i = markers.indexOf(end) + 1;
                } else {
                    // No matching end — track for partial handling.
                    if (partial && (start.position >= textPos))
                        lastUnmatched = start;
                    i++;
                }
            }
        }

        // Emit remaining text, applying partial formatting if applicable.
        if (textPos < line.length()) {
            if ((lastUnmatched != null) && (lastUnmatched.position >= textPos)) {
                // Emit text before the unmatched marker.
                if (lastUnmatched.position > textPos)
                    handler.text(line.substring(textPos, lastUnmatched.position));
                // Emit content after the marker as formatted.
                int contentStart = lastUnmatched.position + lastUnmatched.marker.length();
                if (contentStart < line.length())
                    handler.formatted(line.substring(contentStart), lastUnmatched.type);
            } else {
                handler.text(line.substring(textPos));
            }
        }
    }

    /************************************************************************
     * Inline helpers (same as MarkdownParser).
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
                    links.add(new LinkInfo(labelStart, urlEnd + 1, line.substring(labelStart + 1, labelEnd), line.substring(urlStart, urlEnd)));
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
                if (!prevUnderscore && !nextUnderscore)
                    markers.add(new FormatMarker(i, "_", FormatType.ITL));
            }
        }
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

    private record LinkInfo(int startPos, int endPos, String label, String url) {}

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

}
