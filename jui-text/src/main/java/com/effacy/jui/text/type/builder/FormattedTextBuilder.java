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

import com.effacy.jui.text.type.DefaultBlockIdGenerator;
import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.IBlockIdGenerator;

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

    /**
     * Generator used to assign block identifiers during construction.
     */
    private IBlockIdGenerator blockIdGenerator = new DefaultBlockIdGenerator();

    /**
     * Overrides the block identifier generator used during construction.
     * 
     * @param generator
     *                  the generator to use. If {@code null}, the default
     *                  generator is restored.
     * @return this instance.
     */
    public FormattedTextBuilder blockIdGenerator(IBlockIdGenerator generator) {
        this.blockIdGenerator = (generator != null) ? generator : new DefaultBlockIdGenerator();
        return this;
    }

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
        block.ensureId(blockIdGenerator);
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
        FormattedBlock block = blockStack.peek();
        // "indent" is a first-class block property (read by the serializer, renderer and
        // editor via getIndent()), not a free-form meta entry — bridge it onto the field so
        // parsed nesting is preserved rather than silently flattened.
        if ("indent".equals(name)) {
            try {
                block.setIndent(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                // Ignore a malformed indent value (leave the default level 0).
            }
            return;
        }
        block.meta(name, value);
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
        link(label, url, new FormatType[0]);
    }

    @Override
    public void link(String label, String url, FormatType... formats) {
        if (formats == null)
            formats = new FormatType[0];
        if ((label == null) || label.isEmpty()) {
            // Empty label link — build directly since FormattedLine.link()
            // ignores empty text.
            FormatType[] all = withLinkFormat(formats);
            FormattedLine.Format fmt = new FormattedLine.Format(currentLine.length(), 0, all);
            fmt.getMeta().put("link", (url != null) ? url : "");
            currentLine.getFormatting().add(fmt);
            return;
        }
        if ((url == null) || url.isEmpty()) {
            // Empty URL — build directly since FormattedLine.link() drops the
            // link format when URL is empty.
            FormatType[] all = withLinkFormat(formats);
            FormattedLine.Format fmt = new FormattedLine.Format(currentLine.length(), label.length(), all);
            fmt.getMeta().put("link", "");
            currentLine.getFormatting().add(fmt);
            currentLine.append(label);
            return;
        }
        // FormattedLine.link() always includes the A (link) format and applies any extras.
        currentLine.link(label, url, formats);
    }

    /** Prepends {@link FormatType#A} to a set of extra formats (kept distinct). */
    private static FormatType[] withLinkFormat(FormatType[] formats) {
        FormatType[] all = new FormatType[formats.length + 1];
        all[0] = FormatType.A;
        System.arraycopy(formats, 0, all, 1, formats.length);
        return all;
    }

    @Override
    public void image(String alt, String src, int width, int height) {
        image(alt, src, width, height, null);
    }

    @Override
    public void image(String alt, String src, int width, int height, String align) {
        image(alt, src, width, height, align, -1);
    }

    @Override
    public void image(String alt, String src, int width, int height, String align, int margin) {
        // An image occupies exactly one sentinel character (see
        // FormattedLine.IMAGE_SENTINEL) so caret positions fall unambiguously before
        // or after it; the alt text is carried as meta, not as span text.
        FormattedLine.Format fmt = new FormattedLine.Format(currentLine.length(), 1, FormatType.IMG);
        fmt.getMeta().put(FormattedLine.META_IMAGE, (src != null) ? src : "");
        if ((alt != null) && !alt.isEmpty())
            fmt.getMeta().put(FormattedLine.META_ALT, alt);
        if (width > 0)
            fmt.getMeta().put(FormattedLine.META_WIDTH, String.valueOf(width));
        if (height > 0)
            fmt.getMeta().put(FormattedLine.META_HEIGHT, String.valueOf(height));
        if ((align != null) && !align.isEmpty())
            fmt.getMeta().put(FormattedLine.META_ALIGN, align);
        if (margin > 0)
            fmt.getMeta().put(FormattedLine.META_MARGIN, String.valueOf(margin));
        currentLine.getFormatting().add(fmt);
        currentLine.setText(currentLine.getText() + FormattedLine.IMAGE_SENTINEL);
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
