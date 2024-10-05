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
import com.effacy.jui.platform.core.JuiIncompatible;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.google.gwt.core.shared.GwtIncompatible;

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
    @JuiIncompatible
    @GwtIncompatible
    public static FormattedText markdown(String... content) {
        return markdown (null, content);
    }

    @JuiIncompatible
    @GwtIncompatible
    public static FormattedText markdown(Function<String,String> lineProcessor, String... content) {
        FormattedText text = new FormattedText();
        for (String part : content) {
            if (part == null)
                continue;
            for (String str : part.split (System.lineSeparator ())) {
                if (lineProcessor != null)
                    str = lineProcessor.apply (str);
                if (str == null)
                    continue;
                FormattedBlock blk = new FormattedBlock (BlockType.PARA);
                text.getBlocks ().add (blk);
                if (str.startsWith ("* ")) {
                    blk.getLines ().add (FormattedLine.markdown (str.substring (2)));
                    blk.setIndent (1);
                    blk.setType (BlockType.NLIST);
                } else
                    blk.getLines ().add (FormattedLine.markdown (str));
            }
        }
        return text;
    }

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
