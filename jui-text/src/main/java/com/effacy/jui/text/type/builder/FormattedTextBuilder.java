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
package com.effacy.jui.text.type.builder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.Function;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;
import com.effacy.jui.text.type.FormattedText;

/**
 * An {@link IEventBuilder} implementation that builds a
 * {@link FormattedText} from parsing events.
 * <p>
 * Usage:
 * <pre>
 * FormattedTextMarkdownHandler handler = new FormattedTextMarkdownHandler();
 * FormattedText result = new MarkdownParser().parse(handler, "# Title\n\nSome **bold** text.");
 * </pre>
 *
 * @see MarkdownParser
 */
public class FormattedTextBuilder implements IEventBuilder<FormattedText> {

    /**
     * Parses multiple markdown content blocks into a FormattedText object.
     * See {@link #parse(boolean, Function, String...)} but with no line processor
     * and partial mode disabled.
     *
     * @param content
     *                the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(String... content) {
        return parse(false, null, content);
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
        return parse(partial, null, content);
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
        return parse(false, lineProcessor, content);
    }

    /**
     * Parses multiple markdown content blocks into a FormattedText object, applying
     * an optional line processor to each line.
     *
     * @param partial
     *                      {@code true} if the content may be incomplete (e.g.
     *                      streaming). Unclosed format markers on the last line will
     *                      be treated as formatting rather than literal text.
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     * @return the formatted text
     */
    public static FormattedText parse(boolean partial, Function<String, String> lineProcessor, String... content) {
        return new MarkdownParser()
            .partial(partial)
            .lineProcessor(lineProcessor)
            .parse(new FormattedTextBuilder(), content);
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    /**
     * The formatted text being built from the parsing events. Always
     * non-{@code null} but initially empty until {@link #commence()} is called.
     */
    private FormattedText text = new FormattedText();

    /**
     * Stack of open blocks. Always non-empty during parsing, with the root block at
     * the bottom and the current innermost block at the top. Initially empty until
     * {@link #commence()} is called.
     */
    private Deque<FormattedBlock> blockStack = new ArrayDeque<>();

    /**
     * The current line being built from the parsing events.
     */
    private FormattedLine currentLine;

    @Override
    public void commence() {
        text = new FormattedText();
        blockStack.clear();
        currentLine = null;
    }

    @Override
    public FormattedText result() {
        return text;
    }

    @Override
    public void startBlock(BlockType type) {
        FormattedBlock block = new FormattedBlock(type);
        if (!blockStack.isEmpty())
            blockStack.peek().getBlocks().add(block);
        else
            text.getBlocks().add(block);
        blockStack.push(block);
    }

    @Override
    public void endBlock(BlockType type) {
        blockStack.pop();
    }

    @Override
    public void meta(String name, String value) {
        blockStack.peek().meta(name, value);
    }

    @Override
    public void startLine() {
        currentLine = new FormattedLine();
    }

    @Override
    public void endLine() {
        blockStack.peek().getLines().add(currentLine);
        currentLine = null;
    }

    @Override
    public void text(String text) {
        currentLine.append(text);
    }

    @Override
    public void formatted(String text, FormatType... formats) {
        if ((text == null) || text.isEmpty()) {
            // Zero-length format (e.g. "****") — must build directly since
            // FormattedLine.append() ignores empty text.
            currentLine.getFormatting().add(
                new FormattedLine.Format(currentLine.length(), 0, formats)
            );
            return;
        }
        currentLine.append(text, formats);
    }

    @Override
    public void link(String label, String url) {
        if ((label == null) || label.isEmpty()) {
            // Empty label link — build directly since FormattedLine.link()
            // ignores empty text.
            FormattedLine.Format fmt = new FormattedLine.Format(currentLine.length(), 0, FormatType.A);
            fmt.getMeta().put("link", (url != null) ? url : "");
            currentLine.getFormatting().add(fmt);
            return;
        }
        if ((url == null) || url.isEmpty()) {
            // Empty URL — build directly since FormattedLine.link() drops the
            // link format when URL is empty.
            FormattedLine.Format fmt = new FormattedLine.Format(currentLine.length(), label.length(), FormatType.A);
            fmt.getMeta().put("link", "");
            currentLine.getFormatting().add(fmt);
            currentLine.append(label);
            return;
        }
        currentLine.link(label, url);
    }

    @Override
    public void image(String alt, String src, int width, int height) {
        FormattedLine.Format fmt = new FormattedLine.Format(currentLine.length(), (alt != null) ? alt.length() : 0, FormatType.IMG);
        fmt.getMeta().put(FormattedLine.META_IMAGE, (src != null) ? src : "");
        if (width > 0)
            fmt.getMeta().put(FormattedLine.META_WIDTH, String.valueOf(width));
        if (height > 0)
            fmt.getMeta().put(FormattedLine.META_HEIGHT, String.valueOf(height));
        currentLine.getFormatting().add(fmt);
        if ((alt != null) && !alt.isEmpty())
            currentLine.append(alt);
    }

    @Override
    public void variable(String name, Map<String, String> meta) {
        FormattedLine.Format format = new FormattedLine.Format(currentLine.length(), 0);
        format.getMeta().put(FormattedLine.META_VARIABLE, name);
        if (meta != null)
            format.getMeta().putAll(meta);
        currentLine.getFormatting().add(format);
    }
}
