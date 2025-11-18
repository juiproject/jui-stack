/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.text.type.FormattedBlock.BlockType;

/**
 * Captures text that is formatted in a rich content sense (i.e custom blocks of
 * information, paragraphs, lists, etc).
 */
@JsonSerializable
public class FormattedText implements Iterable<FormattedBlock> {

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * See {@link #markdown(Function, String...)} but with no line processor.
     */
    public static FormattedText markdown(String... content) {
        return markdown (null, content);
    }

    /**
     * Parses markdown content into a FormattedText object.
     *
     * @param lineProcessor
     *                      optional function to process each line before parsing
     * @param content
     *                      the markdown content blocks to parse
     * @return the formatted text.
     */
    public static FormattedText markdown(Function<String,String> lineProcessor, String... content) {
        return com.effacy.jui.text.type.markdown.MarkdownParser.parse(lineProcessor, content);
    }

    /**
     * Creates formatted text where each content string passed is treated as a
     * separate paragraph.
     * 
     * @param content
     *                the content (paragraphs).
     * @return the formatted text.
     */
    public static FormattedText string(String... content) {
        FormattedText text = new FormattedText();
        for (String part : content) {
            if (part == null)
                continue;
            FormattedBlock blk = new FormattedBlock (BlockType.PARA);
            text.getBlocks ().add (blk);
            blk.add (FormattedLine.string (part));
        }
        return text;
    }

    /**
     * Determines if the formatted text is empty or not.
     * 
     * @return {@code true} if it is.
     */
    public boolean empty() {
        return ((blocks == null) || blocks.isEmpty());
    }

    /**
     * Flattens the content to unformatted text. Line breaks are used for each new
     * block.
     * 
     * @return the flattened text.
     */
    public String flatten() {
        StringBuffer sb = new StringBuffer();
        if (blocks != null) {
            for (int i = 0; i < blocks.size(); i++) {
                if (i > 0)
                    sb.append ("\n\n");
                sb.append (blocks.get(i).flatten());
            }
        }
        return sb.toString ();
    }

    /**
     * Generates a debug string version and allows a consumer to process it. This
     * can be placed inline for debugging with minial code impact.
     * <p>
     * See {@link #debug()}.
     * 
     * @return this formatted text.
     */
    public FormattedText debug(Consumer<String> str) {
        if (str != null)
            str.accept(debug());
        return this;
    }

    /**
     * Generates a string version of the formatted text explicitly to debug the
     * structure. This includes visual markers for structure.
     *
     * @return the debug string.
     */
    public String debug() {
        if ((blocks == null) || blocks.isEmpty())
            return "[EMPTY FormattedText]";

        StringBuilder sb = new StringBuilder();
        sb.append("FormattedText[blocks=").append(blocks.size()).append("]\n");

        for (int i = 0; i < blocks.size(); i++) {
            FormattedBlock block = blocks.get(i);
            sb.append("  [").append(i).append("] Block[type=").append(block.getType());

            if (block.getIndent() > 0)
                sb.append(", indent=").append(block.getIndent());

            sb.append(", lines=").append(block.getLines().size()).append("]\n");

            for (int j = 0; j < block.getLines().size(); j++) {
                FormattedLine line = block.getLines().get(j);
                sb.append("      [").append(j).append("] \"").append(line.getText()).append("\"");

                if ((line.getFormatting() != null) && !line.getFormatting().isEmpty()) {
                    sb.append(" {");
                    for (int k = 0; k < line.getFormatting().size(); k++) {
                        if (k > 0)
                            sb.append(", ");
                        FormattedLine.Format fmt = line.getFormatting().get(k);
                        sb.append(fmt.getFormats().toString())
                          .append("@").append(fmt.getIndex())
                          .append("+").append(fmt.getLength());
                    }
                    sb.append("}");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /************************************************************************
     * Property getters and setters.
     ************************************************************************/
    
    /**
     * See {@link #getBlocks()}.
     */
    private List<FormattedBlock> blocks;

    /**
     * Gets all the block in the formatted text.
     * 
     * @return a list of all blocks (never {@code null} but may be empty).
     */
    public List<FormattedBlock> getBlocks() {
        if (blocks == null)
            blocks = new ArrayList<>();
        return blocks;
    }

    /**
     * Serialisation setter for {@link #getBlocks()}.
     */
    public void setBlocks(List<FormattedBlock> blocks) {
        this.blocks = blocks;
    }

    /************************************************************************
     * Building.
     ************************************************************************/

    /**
     * Adds a block.
     * 
     * @param type
     *                the type of block.
     * @param builder
     *                to build out the block.
     * @return this text instance.
     */
    public FormattedText block(BlockType type, Consumer<FormattedBlock> builder) {
        if (type == null)
            return this;
        FormattedBlock blk = new FormattedBlock (type);
        getBlocks ().add (blk);
        if (builder != null)
            builder.accept (blk);
        return this;
    }

    /**
     * Adds a block.
     * 
     * @param type
     *                the type of block.
     * @return the block.
     */
    public FormattedBlock block(BlockType type) {
        if (type == null)
            return null;
        FormattedBlock blk = new FormattedBlock (type);
        getBlocks ().add (blk);
        return blk;
    }

    /************************************************************************
     * Overrides and contracted behaviour.
     ************************************************************************/

    @Override
    public Iterator<FormattedBlock> iterator() {
        return blocks.iterator();
    }

    @Override
    public String toString() {
        String str = "";
        for (FormattedBlock blk : this)
            str += "<<" + blk.toString () + ">>";
        return str;
    }
}
