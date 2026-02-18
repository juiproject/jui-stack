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

import java.util.Map;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

/**
 * Receives events during markdown parsing. Implementations can use these events
 * to build any target representation (e.g. {@code FormattedText}, a DOM tree,
 * HTML string, etc.).
 * <p>
 * Events are emitted in a hierarchical, balanced order:
 * <pre>
 *   startBlock(PARA)
 *     startLine()
 *       text("Hello ")
 *       formatted("bold", BLD)
 *       text(" world")
 *     endLine()
 *   endBlock(PARA)
 * </pre>
 * For tables the hierarchy is nested:
 * <pre>
 *   startBlock(TABLE)
 *     meta("columns", "2")
 *     meta("headers", "1")
 *     meta("align", "L,R")
 *     startBlock(TROW)
 *       startBlock(TCELL)
 *         startLine()
 *           text("Name")
 *         endLine()
 *       endBlock(TCELL)
 *       startBlock(TCELL)
 *         startLine()
 *           text("Age")
 *         endLine()
 *       endBlock(TCELL)
 *     endBlock(TROW)
 *   endBlock(TABLE)
 * </pre>
 */
public interface IMarkdownEventHandler {

    /**
     * Called when a block begins.
     *
     * @param type
     *             the block type.
     */
    void startBlock(BlockType type);

    /**
     * Called when a block ends. Always paired with a preceding
     * {@link #startBlock(BlockType)} of the same type.
     *
     * @param type
     *             the block type.
     */
    void endBlock(BlockType type);

    /**
     * Called to set metadata on the current block (between {@link #startBlock} and
     * the first child or line).
     *
     * @param name
     *              the metadata key.
     * @param value
     *              the metadata value.
     */
    void meta(String name, String value);

    /**
     * Called when a line begins within a block.
     */
    void startLine();

    /**
     * Called when a line ends. Always paired with a preceding
     * {@link #startLine()}.
     */
    void endLine();

    /**
     * Called for a segment of plain (unformatted) text within a line.
     *
     * @param text
     *             the text content.
     */
    void text(String text);

    /**
     * Called for a segment of formatted text within a line.
     *
     * @param text
     *              the text content (with markers stripped).
     * @param format
     *              the format applied.
     */
    void formatted(String text, FormatType format);

    /**
     * Called for a link within a line.
     *
     * @param label
     *              the link display text.
     * @param url
     *              the link URL.
     */
    void link(String label, String url);

    /**
     * Called for a variable placeholder within a line.
     *
     * @param name
     *             the variable name.
     * @param meta
     *             additional metadata (never {@code null}, may be empty).
     */
    void variable(String name, Map<String, String> meta);
}
