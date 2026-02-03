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
import java.util.List;
import java.util.function.Function;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedBlock.BlockType;

public class MarkdownParser {

    /**
     * Parses multiple markdown content blocks into a FormattedText object.
     * See {@link #parse(Function, String...)} but with no line processor.
     *
     * @param content
     *                the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(String... content) {
        return parse(null, content);
    }

    /**
     * Parses multiple markdown content blocks into a FormattedText object, applying
     * an optional line processor to each line.
     *
     * Supports:
     * - Headings: # H1, ## H2, ### H3
     * - Lists: - item, * item, + item, 1. item
     * - Bold: **text** or __text__
     * - Italic: *text* or _text_
     * - Strikethrough: ~~text~~
     * - Code: `text`
     * - Links: [label](url)
     * - Newlines in paragraphs
     * - Double newlines create new paragraphs
     *
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(Function<String, String> lineProcessor, String... content) {
        if ((content == null) || (content.length == 0))
            return new FormattedText();

        // Join all content blocks with double newlines to preserve block separation
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < content.length; i++) {
            if (content[i] != null) {
                if ((combined.length() > 0) && !combined.toString().endsWith("\n\n"))
                    combined.append("\n\n");
                combined.append(content[i]);
            }
        }

        return parseInternal(combined.toString(), lineProcessor);
    }

    /**
     * Parses markdown text into a FormattedText object.
     *
     * Supports:
     * - Headings: # H1, ## H2, ### H3
     * - Lists: - item, * item, + item, 1. item
     * - Bold: **text** or __text__
     * - Italic: *text* or _text_
     * - Strikethrough: ~~text~~
     * - Code: `text`
     * - Links: [label](url)
     * - Newlines in paragraphs
     * - Double newlines create new paragraphs
     *
     * @param markdown
     *                 the markdown text to parse
     * @return the formatted text
     */
    private static FormattedText parseInternal(String markdown, Function<String, String> lineProcessor) {
        if ((markdown == null) || markdown.isEmpty())
            return new FormattedText();

        FormattedText text = new FormattedText();

        // Split by double newlines to identify paragraphs
        String[] paragraphs = markdown.split("\\n\\n+");

        for (String para : paragraphs) {
            if (para.trim().isEmpty())
                continue;

            // Split by single newlines within the paragraph
            String[] lines = para.split("\\n");

            // Apply line processor if provided
            if (lineProcessor != null) {
                for (int i = 0; i < lines.length; i++) {
                    String processed = lineProcessor.apply(lines[i]);
                    if (processed == null) {
                        lines[i] = "";
                    } else {
                        lines[i] = processed;
                    }
                }
            }

            // After line processing, check if all lines are empty and skip if so
            boolean allEmpty = true;
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty)
                continue;

            // Check if this is a heading (single line starting with #)
            if ((lines.length == 1) && lines[0].trim().startsWith("#")) {
                parseHeading(text, lines[0]);
            } else if (isListBlock(lines)) {
                // Parse as list
                parseList(text, lines);
            } else {
                // Regular paragraph
                FormattedBlock block = text.block(BlockType.PARA);
                for (String line : lines) {
                    if (line.isEmpty()) {
                        // Empty line within a paragraph
                        block.line("");
                    } else {
                        block.getLines().add(parseLine(line));
                    }
                }
            }
        }

        return text;
    }

    /**
     * Parses a heading line and adds it to the formatted text.
     *
     * @param text the formatted text to add the heading to
     * @param line the heading line
     */
    private static void parseHeading(FormattedText text, String line) {
        String trimmed = line.trim();
        BlockType headingType = BlockType.PARA;
        String content = trimmed;

        // Determine heading level
        if (trimmed.startsWith("### ")) {
            headingType = BlockType.H3;
            content = trimmed.substring(4);
        } else if (trimmed.startsWith("## ")) {
            headingType = BlockType.H2;
            content = trimmed.substring(3);
        } else if (trimmed.startsWith("# ")) {
            headingType = BlockType.H1;
            content = trimmed.substring(2);
        } else {
            // Not a valid heading, treat as paragraph
            headingType = BlockType.PARA;
        }

        FormattedBlock block = text.block(headingType);
        block.getLines().add(parseLine(content));
    }

    /**
     * Determines if a block of lines represents a list.
     *
     * @param lines
     *              the lines to check
     * @return {@code true} if all lines are list items
     */
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

    /**
     * Determines if a line is a list item.
     *
     * @param line
     *             the line to check
     * @return {@code true} if the line is a list item
     */
    private static boolean isListItem(String line) {
        String trimmed = line.trim();
        if (trimmed.length() < 2)
            return false;

        // Unordered list markers: -, *, +
        char first = trimmed.charAt(0);
        if (((first == '-') || (first == '*') || (first == '+')) && Character.isWhitespace(trimmed.charAt(1)))
            return true;

        // Ordered list markers: 1., 2., etc.
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

    /**
     * Parses a list block and adds it to the formatted text.
     * Each list item creates a separate NLIST block.
     *
     * @param text
     *              the formatted text to add the list to
     * @param lines
     *              the list lines
     */
    private static void parseList(FormattedText text, String[] lines) {
        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;

            String trimmed = line.trim();
            String content = "";

            // Extract content after list marker
            char first = trimmed.charAt(0);
            if ((first == '-') || (first == '*') || (first == '+')) {
                // Unordered list
                content = trimmed.substring(trimmed.indexOf(' ') + 1);
            } else {
                // Ordered list
                int dotIndex = trimmed.indexOf('.');
                if (dotIndex > 0)
                    content = trimmed.substring(dotIndex + 1).trim();
            }

            // Create a separate block for each list item
            FormattedBlock block = text.block(BlockType.NLIST);
            FormattedLine formattedLine = parseLine(content);
            block.getLines().add(formattedLine);
        }
    }

    /**
     * Parses a single line of markdown text into a FormattedLine.
     *
     * @param line
     *             the line to parse
     * @return the formatted line
     */
    private static FormattedLine parseLine(String line) {
        FormattedLine formattedLine = new FormattedLine();

        // First, find and process links [label](url)
        List<LinkInfo> links = findLinks(line);

        // Find variables {{name}} or {{name;key=value}}
        List<VariableInfo> variables = findVariables(line);

        // Track format markers and their positions
        List<FormatMarker> markers = new ArrayList<>();

        // Find all format markers
        findMarkers(line, "**", FormatType.BLD, markers);      // **bold**
        findMarkers(line, "__", FormatType.BLD, markers);      // __bold__
        findMarkers(line, "~~", FormatType.STR, markers);      // ~~strike~~
        findMarkers(line, "`", FormatType.CODE, markers);      // `code`

        // For italic, we need to be careful not to match bold markers
        findItalicMarkers(line, markers);

        // Remove markers that fall within link regions (they would interfere)
        removeMarkersWithinLinks(markers, links);

        // Remove variables that fall within link regions (variables inside links are literal)
        removeVariablesWithinLinks(variables, links);

        // Remove markers that fall within variable regions
        removeMarkersWithinVariables(markers, variables);

        // Sort markers by position
        markers.sort((a, b) -> a.position - b.position);

        // Build the formatted line
        StringBuilder plainText = new StringBuilder();
        int textPos = 0;
        int i = 0;
        int linkIdx = 0;
        int varIdx = 0;

        while ((i < markers.size()) || (linkIdx < links.size()) || (varIdx < variables.size())) {
            // Determine which comes first: a format marker, a link, or a variable
            FormatMarker start = (i < markers.size()) ? markers.get(i) : null;
            LinkInfo link = (linkIdx < links.size()) ? links.get(linkIdx) : null;
            VariableInfo variable = (varIdx < variables.size()) ? variables.get(varIdx) : null;

            // Find the earliest position
            int startPos = (start != null) ? start.position : Integer.MAX_VALUE;
            int linkPos = (link != null) ? link.startPos : Integer.MAX_VALUE;
            int varPos = (variable != null) ? variable.startPos : Integer.MAX_VALUE;

            if ((varPos <= startPos) && (varPos <= linkPos) && (variable != null)) {
                // Process variable
                if (variable.startPos > textPos)
                    plainText.append(line, textPos, variable.startPos);

                // Create a zero-length format for the variable at the current position
                int formatStartPos = plainText.length();
                FormattedLine.Format format = new FormattedLine.Format(formatStartPos, 0);
                format.getMeta().put(FormattedLine.META_VARIABLE, variable.name);
                // Add any additional metadata
                if (variable.meta != null)
                    format.getMeta().putAll(variable.meta);
                formattedLine.getFormatting().add(format);

                textPos = variable.endPos;
                varIdx++;
            } else if ((linkPos <= startPos) && (link != null)) {
                // Process link
                if (link.startPos > textPos)
                    plainText.append(line, textPos, link.startPos);

                int formatStartPos = plainText.length();
                plainText.append(link.label);

                FormattedLine.Format format = new FormattedLine.Format(formatStartPos, link.label.length(), FormatType.A);
                format.getMeta().put("link", link.url);
                formattedLine.getFormatting().add(format);

                textPos = link.endPos;
                linkIdx++;
            } else if (start != null) {
                // Find matching end marker
                FormatMarker end = findMatchingEnd(markers, i);

                if (end != null) {
                    // Add text before this formatted region
                    if (start.position > textPos)
                        plainText.append(line, textPos, start.position);

                    // Add formatted text
                    int contentStart = start.position + start.marker.length();
                    int contentEnd = end.position;
                    String content = line.substring(contentStart, contentEnd);

                    int formatStartPos = plainText.length();
                    plainText.append(content);

                    formattedLine.getFormatting().add(
                        new FormattedLine.Format(formatStartPos, content.length(), start.type)
                    );

                    textPos = end.position + end.marker.length();
                    i = markers.indexOf(end) + 1;
                } else {
                    // No matching end, treat as literal text
                    i++;
                }
            }
        }

        // Add remaining text
        if (textPos < line.length())
            plainText.append(line.substring(textPos));

        formattedLine.setText(plainText.toString());
        return formattedLine;
    }

    /**
     * Finds all markdown links [label](url) in the text.
     *
     * @param line
     *             the line to search
     * @return list of link information
     */
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

            // Check for immediately following (url)
            if (((labelEnd + 1) < line.length()) && (line.charAt(labelEnd + 1) == '(')) {
                int urlStart = labelEnd + 2;
                int urlEnd = line.indexOf(')', urlStart);
                if (urlEnd != -1) {
                    String label = line.substring(labelStart + 1, labelEnd);
                    String url = line.substring(urlStart, urlEnd);
                    links.add(new LinkInfo(labelStart, urlEnd + 1, label, url));
                    pos = urlEnd + 1;
                    continue;
                }
            }

            pos = labelStart + 1;
        }

        return links;
    }

    /**
     * Removes format markers that fall within link regions.
     */
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

    /**
     * Removes format markers that fall within variable regions.
     */
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

    /**
     * Removes variables that fall within link regions.
     * Variables inside link labels are treated as literal text.
     */
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

    /**
     * Information about a markdown link.
     */
    private record LinkInfo(int startPos, int endPos, String label, String url) {}

    /**
     * Finds markdown markers in the text.
     */
    private static void findMarkers(String line, String marker, FormatType type, List<FormatMarker> markers) {
        int pos = 0;
        while ((pos = line.indexOf(marker, pos)) != -1) {
            markers.add(new FormatMarker(pos, marker, type));
            pos += marker.length();
        }
    }

    /**
     * Finds italic markers, being careful to exclude bold markers.
     */
    private static void findItalicMarkers(String line, List<FormatMarker> markers) {
        // Find single * or _ that are not part of ** or __
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '*') {
                // Check if this is a single * (not part of **)
                boolean prevStar = (i > 0) && (line.charAt(i - 1) == '*');
                boolean nextStar = ((i + 1) < line.length()) && (line.charAt(i + 1) == '*');
                if (!prevStar && !nextStar)
                    markers.add(new FormatMarker(i, "*", FormatType.ITL));
            } else if (c == '_') {
                // Check if this is a single _ (not part of __)
                boolean prevUnderscore = (i > 0) && (line.charAt(i - 1) == '_');
                boolean nextUnderscore = ((i + 1) < line.length()) && (line.charAt(i + 1) == '_');
                if (!prevUnderscore && !nextUnderscore)
                    markers.add(new FormatMarker(i, "_", FormatType.ITL));
            }
        }
    }

    /**
     * Finds the matching end marker for a start marker.
     */
    private static FormatMarker findMatchingEnd(List<FormatMarker> markers, int startIndex) {
        FormatMarker start = markers.get(startIndex);

        for (int i = startIndex + 1; i < markers.size(); i++) {
            FormatMarker candidate = markers.get(i);
            if ((candidate.type == start.type) && candidate.marker.equals(start.marker))
                return candidate;
        }

        return null;
    }

    /**
     * Represents a markdown format marker in the text.
     */
    private record FormatMarker(int position, String marker, FormatType type) {}

    /**
     * Information about a variable reference.
     *
     * @param startPos
     *                 the start position of the variable in the original text
     *                 (including the opening braces)
     * @param endPos
     *                 the end position of the variable in the original text
     *                 (after the closing braces)
     * @param name
     *                 the variable name
     * @param meta
     *                 metadata key-value pairs (may be empty, never null)
     */
    private record VariableInfo(int startPos, int endPos, String name, java.util.Map<String, String> meta) {}

    /**
     * Finds all variable references {{name}} or {{name;key=value;...}} in the
     * text.
     * <p>
     * Variable name can consist of letters, numbers, dashes, underscores, periods,
     * dollar signs and colons only.
     * <p>
     * Metadata field names can only consist of letters, numbers and periods.
     * <p>
     * Metadata values can contain any standard character but no newlines or closing
     * braces.
     *
     * @param line
     *             the line to search
     * @return list of variable information
     */
    private static List<VariableInfo> findVariables(String line) {
        List<VariableInfo> variables = new ArrayList<>();
        int pos = 0;

        while (pos < line.length() - 3) {
            // Look for {{
            int start = line.indexOf("{{", pos);
            if (start == -1)
                break;

            // Look for }}
            int end = line.indexOf("}}", start + 2);
            if (end == -1)
                break;

            // Extract the content between {{ and }}
            String content = line.substring(start + 2, end);

            // Parse variable name and optional metadata
            VariableInfo varInfo = parseVariableContent(start, end + 2, content);
            if (varInfo != null)
                variables.add(varInfo);

            pos = end + 2;
        }

        return variables;
    }

    /**
     * Parses the content between {{ and }} into a VariableInfo.
     *
     * @param startPos
     *                 position of the opening {{
     * @param endPos
     *                 position after the closing }}
     * @param content
     *                 the content between the braces
     * @return the variable info, or null if invalid
     */
    private static VariableInfo parseVariableContent(int startPos, int endPos, String content) {
        if ((content == null) || content.isEmpty())
            return null;

        java.util.Map<String, String> meta = new java.util.LinkedHashMap<>();

        // Split by semicolon to separate variable name from metadata
        String[] parts = content.split(";");
        String name = parts[0].trim();

        // Validate variable name: letters, numbers, dashes, underscores, periods,
        // dollar signs and colons
        if (!isValidVariableName(name))
            return null;

        // Parse metadata if present
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            int eqPos = part.indexOf('=');
            if (eqPos > 0) {
                String key = part.substring(0, eqPos).trim();
                String value = part.substring(eqPos + 1);

                // Validate metadata field name: letters, numbers and periods only
                if (isValidMetaFieldName(key) && isValidMetaValue(value))
                    meta.put(key, value);
            }
        }

        return new VariableInfo(startPos, endPos, name, meta);
    }

    /**
     * Validates a variable name.
     * <p>
     * Valid characters: letters, numbers, dashes, underscores, periods, dollar
     * signs and colons.
     */
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

    /**
     * Validates a metadata field name.
     * <p>
     * Valid characters: letters, numbers and periods only.
     */
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

    /**
     * Validates a metadata value.
     * <p>
     * Cannot contain newlines or closing braces.
     */
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
