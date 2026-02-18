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

import java.util.function.Function;

import com.effacy.jui.text.type.FormattedText;

/**
 * Parses markdown content into a {@link FormattedText} representation.
 * <p>
 * Supports:
 * <ul>
 *   <li>Headings: # H1, ## H2, ### H3</li>
 *   <li>Lists: - item, * item, + item, 1. item</li>
 *   <li>Tables: | header | ... | with alignment</li>
 *   <li>Bold: **text** or __text__</li>
 *   <li>Italic: *text* or _text_</li>
 *   <li>Strikethrough: ~~text~~</li>
 *   <li>Code: `text`</li>
 *   <li>Links: [label](url)</li>
 *   <li>Variables: {{name}} or {{name;key=value}}</li>
 * </ul>
 * <p>
 * This class delegates to {@link MarkdownEventParser} with a
 * {@link FormattedTextMarkdownHandler} for the actual parsing.
 *
 * @see MarkdownEventParser
 * @see FormattedTextMarkdownHandler
 */
public class MarkdownParser {

    /**
     * Parses multiple markdown content blocks into a FormattedText object.
     *
     * @param content
     *                the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(String... content) {
        return FormattedTextMarkdownHandler.parse(content);
    }

    /**
     * Parses multiple markdown content blocks into a FormattedText object.
     *
     * @param partial
     *                {@code true} if the content may be incomplete (e.g. streaming)
     * @param content
     *                the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(boolean partial, String... content) {
        return FormattedTextMarkdownHandler.parse(partial, content);
    }

    /**
     * Parses multiple markdown content blocks into a FormattedText object, applying
     * an optional line processor to each line.
     *
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(Function<String, String> lineProcessor, String... content) {
        return FormattedTextMarkdownHandler.parse(lineProcessor, content);
    }

    /**
     * Parses multiple markdown content blocks into a FormattedText object, applying
     * an optional line processor to each line.
     *
     * @param partial
     *                      {@code true} if the content may be incomplete (e.g.
     *                      streaming)
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(boolean partial, Function<String, String> lineProcessor, String... content) {
        return FormattedTextMarkdownHandler.parse(partial, lineProcessor, content);
    }

    /**
     * Parses markdown content and emits events to the given handler.
     *
     * @param handler
     *                the handler to receive parse events
     * @param content
     *                the markdown content blocks to parse
     */
    public static void parse(IMarkdownEventHandler handler, String... content) {
        MarkdownEventParser.parse(handler, content);
    }

    /**
     * Parses markdown content and emits events to the given handler.
     *
     * @param handler
     *                the handler to receive parse events
     * @param partial
     *                {@code true} if the content may be incomplete (e.g. streaming)
     * @param content
     *                the markdown content blocks to parse
     */
    public static void parse(IMarkdownEventHandler handler, boolean partial, String... content) {
        MarkdownEventParser.parse(handler, partial, content);
    }

    /**
     * Parses markdown content and emits events to the given handler, applying an
     * optional line processor to each line.
     *
     * @param handler
     *                      the handler to receive parse events
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     */
    public static void parse(IMarkdownEventHandler handler, Function<String, String> lineProcessor, String... content) {
        MarkdownEventParser.parse(handler, lineProcessor, content);
    }

    /**
     * Parses markdown content and emits events to the given handler, applying an
     * optional line processor to each line.
     *
     * @param handler
     *                      the handler to receive parse events
     * @param partial
     *                      {@code true} if the content may be incomplete (e.g.
     *                      streaming)
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     */
    public static void parse(IMarkdownEventHandler handler, boolean partial, Function<String, String> lineProcessor, String... content) {
        MarkdownEventParser.parse(handler, partial, lineProcessor, content);
    }
}
