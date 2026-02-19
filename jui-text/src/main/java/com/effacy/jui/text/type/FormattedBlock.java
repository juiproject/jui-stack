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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.platform.util.client.Itr;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonSerializable
public class FormattedBlock {

    /**
     * Describes content constraints for blocks. This can be used for validation and
     * for guiding user interfaces.
     */
    public enum BlockTypeConstraint {

        /**
         * Lines only.
         */
        LINES,
        
        /**
         * Blocks only.
         */
        BLOCKS,

        /**
         * Either lines or blocks but not both.
         * <p>
         * This can be softly enforced by allowing for both but only processing line
         * where there are lines, otherwise processing blocks.
         */
        LINES_OR_BLOCKS,
        
        /**
         * Content only (i.e. no lines or blocks).
         */
        CONTENT,
        
        /**
         * Lines and blocks but no content.
         */
        CONTENT_AND_LINES;
    }

    /**
     * Various types of block.
     */
    public enum BlockType {

        /**
         * Paragraph.
         */
        PARA(BlockTypeConstraint.LINES),
        
        /**
         * Header level 1.
         */
        H1(BlockTypeConstraint.LINES),
        
        /**
         * Header level 2.
         */
        H2(BlockTypeConstraint.LINES),
        
        /**
         * Header level 3.
         */
        H3(BlockTypeConstraint.LINES),
        
        /**
         * Numbered list.
         */
        NLIST(BlockTypeConstraint.LINES),
        
        /**
         * Equation.
         */
        EQN(BlockTypeConstraint.CONTENT_AND_LINES),
        
        /**
         * Diagram.
         */
        DIA(BlockTypeConstraint.CONTENT_AND_LINES),

        /**
         * Table.
         */
        TABLE(BlockTypeConstraint.BLOCKS),
        
        /**
         * Table row (only for tables).
         */
        TROW(BlockTypeConstraint.BLOCKS),
        
        /**
         * Table cell (only for table rows).
         */
        TCELL(BlockTypeConstraint.LINES_OR_BLOCKS);

        /**
         * See {@link #constraint()}.
         */
        private BlockTypeConstraint constraint;

        /**
         * Private constructor.
         */
        private BlockType(BlockTypeConstraint constraint) {
            this.constraint = constraint;
        }

        /**
         * Determines if this is one of the given types.
         * 
         * @param types
         *              the types to test against.
         * @return {@code true} if this is one of the given types.
         */
        public boolean is(BlockType... types) {
            for (BlockType type : types) {
                if (type == this)
                    return true;
            }
            return false;
        }

        /**
         * The constraints on the content of this block type. This is used for
         * validation and for guiding user interfaces.
         * 
         * @return the content constraints.
         */
        public BlockTypeConstraint constraint() {
            return constraint;
        }
    }

    /************************************************************************
     * Construction
     ************************************************************************/

    /**
     * Serialisation constructor.
     */
    protected FormattedBlock() {
        // Nothing.
    }

    /**
     * Construct of the given type.
     * 
     * @param type
     *             the type.
     */
    public FormattedBlock(BlockType type) {
        this.type = type;
    }

    /************************************************************************
     * Property setters and getters.
     ************************************************************************/

    /**
     * See {@link #getType()}.
     */
    private BlockType type;

    /**
     * See {@link #getLines()}.
     */
    @JsonInclude(Include.NON_NULL)
    private List<FormattedLine> lines;

    /**
     * See {@link #getBlocks()}.
     */
    private List<FormattedBlock> blocks;

    /**
     * See {@link #getContent()}.
     */
    private String content;
    
    /**
     * See {@link #getMeta()}.
     */
    @JsonInclude(Include.NON_NULL)
    private Map<String,String> meta;

    /**
     * See {@link #getIndent()}.
     */
    private int indent;

    /**
     * The block style.
     * 
     * @return the style
     */
    public BlockType getType() {
        if (type == null)
            type = BlockType.PARA;
        return type;
    }

    /**
     * Serialisation setter for {@link #getStyle()}.
     */
    public void setType(BlockType type) {
        this.type = type;
    }

    /**
     * The content of the block as a line of formatted text.
     * 
     * @return the (formatted) content.
     */
    public List<FormattedLine> getLines() {
        if (lines == null)
            lines = new ArrayList<>();
        return lines;
    }

    /**
     * Serialisation setter for {@link #getLines()}.
     */
    public void setLines(List<FormattedLine> lines) {
        this.lines = lines;
    }

    /**
     * The child blocks of this block. This is used for blocks that have a
     * hierarchical structure such as tables (where the child blocks are rows and
     * the rows have cells as child blocks).
     * 
     * @return the child blocks.
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

    /**
     * The content of the block as a single string. This is used for blocks that
     * have no formatting such as equations and diagrams (lines can be used for
     * captions).
     * 
     * @return the content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Serialisation setter for {@link #getContent()}.
     */
    public void setContent(String content) {
        this.content = content;
    }   

    /**
     * Level of indentation for the block.
     * 
     * @return the level (0 is none).
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Serialisation setter for {@link #getIndent()}.
     */
    public void setIndent(int indent) {
        this.indent = indent;
    }

    /**
     * Meta-data associated with the formatting.
     * 
     * @return any metadata.
     */
    public Map<String,String> getMeta() {
        if (meta == null)
            meta = new HashMap<> ();
        return meta;
    }

    /**
     * Serialisation setter for {@link #getMeta()}.
     */
    public void setMeta(Map<String,String> meta) {
        this.meta = meta;
    }

    /************************************************************************
     * General properties.
     ************************************************************************/

    /**
     * The total length of the block (being the sum of the length of the lines or
     * the length of the child blocks, or the length of the content).
     * 
     * @return the length.
     */
    public int length() {
        int length = 0;
        if ((lines != null) && !lines.isEmpty()) {
            for (int i = 0; i < getLines ().size (); i++) {
                if (i > 0)
                    length++;
                length += getLines ().get (i).length ();
            }
        }
        if ((blocks != null) && !blocks.isEmpty()) {
            for (FormattedBlock child : getBlocks())
                length += child.length ();
        }
        if (content != null)
            length += content.length();
        return length;
    }

    /************************************************************************
     * Line properties and operations.
     ************************************************************************/

     /**
      * Determines if the type is one of the given ones.
      * 
      * @param type
      *             the types to test against.
      * @return {@code true} if it is one of the types.
      */
     public boolean typeIs(BlockType... types) {
        for (BlockType type : types) {
            if (getType () == type)
                return true;
        }
        return false;
     }

    /**
     * Obtains the last line in the collection of lines for this block. If there are
     * no lines then one will be created.
     * 
     * @return the last line.
     */
    public FormattedLine lastLine() {
        if (getLines ().isEmpty ())
            return line ();
        return getLines ().get (getLines ().size() - 1);
    }

    /**
     * Contracts the lines into a single string using line breaks for new lines and
     * with no consideration to formatting.
     * 
     * @return the composed content.
     */
    public String contractLines() {
        String str = "";
        for (int i = 0; i < getLines ().size(); i++) {
            if (i > 0)
                str += "\n";
            str += getLines ().get(i);
        }
        return str;
    }

    /************************************************************************
     * Behaviour (building and modifying).
     ************************************************************************/

    /**
     * Assigns a meta-data value.
     * 
     * @param name
     *              the name of the meta-data field.
     * @param value
     *              the value of the field.
     * @return this instance.
     */
    public FormattedBlock meta(String name, String value) {
        if (name != null) {
            if (value == null)
                getMeta().remove(name);
            else
                getMeta().put(name, value);
        }
        return this;
    }

    /**
     * Gets a meta-data field.
     * 
     * @param name
     *             the name of the field.
     * @return the associated value.
     */
    public String meta(String name) {
        return getMeta().get(name);
    }

    /**
     * Assign a level of indentation.
     * 
     * @param indent
     *               the level.
     */
    public FormattedBlock indent(int indent) {
        this.indent = Math.min (5, Math.max(0, indent));
        return this;
    }

    /**
     * Removes from this block the text starting at the start index and consisting
     * of the given number of characters.
     * 
     * @param start
     *              the starting index.
     * @param len
     *              the number of characters.
     * @return this block instance.
     */
    public FormattedBlock remove(int start, int len) {
        if (start < 0) {
            len += start;
            start = 0;
        }
        if (len <= 0)
            return this;

        for (FormattedLine line : new ArrayList<>(getLines ())) {
            int ll = line.length ();
            if (len > 0) {
                line.remove (start, len);
                if (line.length() <= 0)
                    getLines ().remove (line);
            }
            start -= ll;
            start -= 1; // For the new line.
            if (start < 0) {
                len += start;
                start = 0;
            }
            if (len <= 0)
                break;
        }
        return this;
    }

    /**
     * Inserts text content into the block at the given starting location.
     * 
     * @param start
     *              the starting location (less than 0 or beyond end will append).
     * @param text
     *              the text content to insert.
     * @return this content block (modified).
     */
    public FormattedBlock insert(int start, String text) {
        // Ensure there is something to insert.
        if ((text == null) || (text.length() == 0))
            return this;
        // A negative start means at the end.
        if ((start < 0) || (start >= length ())) {
            lastLine ().append (text);
            return this;
        }
        // Insert internally.
        for (FormattedLine line : new ArrayList<>(getLines ())) {
            if (line.length() >= start) {
                line.insert (start, text);
                return this;
            }
            start -= line.length ();
        }
        return this;
    }

    /**
     * Inserts the passed content into the block at the given starting location.
     * 
     * @param start
     *              the starting location (less than 0 or beyond end will append).
     * @param blk
     *              the block to insert.
     * @return this content block (modified).
     */
    public FormattedBlock insert(int start, FormattedBlock blk) {
        if ((blk == null) || (type != blk.type) || blk.getLines ().isEmpty())
            return this;
        if ((blk.getLines ().size () == 0) && (blk.getLines ().get (0).formatting.size () == 0))
            return insert (start, blk.getLines ().get (0).text);
        // A negative start means at the end.
        if ((start < 0) || (start >= length ())) {
            lastLine ().merge (blk.getLines ().get (0));
            for (int i = 1; i < blk.getLines ().size(); i++)
                add (blk.getLines ().get (i));
            return this;
        }
        // Insert internally.
        for (FormattedLine line : new ArrayList<>(getLines ())) {
            // if (line.length() >= start) {
            //     line.insert (start, text);
            //     return this;
            // }
            // start -= line.length ();
        }
        return this;
    }

    /**
     * Merge the passed block with this block. This block will be modified.
     * 
     * @param other
     *              the other block to merge (must be of the same type).
     * @return a new merged block.
     */
    public void merge(FormattedBlock other) {
        if (other == null)
            return;
        if (type != other.type)
            return;
        if (getLines ().isEmpty ()) {
            other.getLines ().forEach (line -> add (line));
        } else {
            Itr.forEach (other.getLines (), (c,v) -> {
                if (c.first())
                    lastLine ().merge (v);
                else
                    getLines ().add (v.clone ());
            });
            
        }
    }

    /**
     * Splits this block at the given index returning the balance.
     * <p>
     * Note that splitting at a line break (a line break is considered a point
     * rather than a character itself, in HTML a line break node is used to
     * demarcate a line break but that node is not considered a character) will
     * remove that line break. When dealing with paragraphs (and the like) this
     * gives the effect of turning a line break into a paragraph break which is a
     * more natural expectation than retaining the
     * line break.
     * 
     * @param idx
     *            the index to split at.
     * @return
     *         the balance.
     */
    public FormattedBlock split(int idx) {
        if (idx <= 0) {
            FormattedBlock blk = clone ();
            getLines ().clear ();
            return blk;
        }
        FormattedBlock blk = new FormattedBlock (type);
        blk.indent = indent;
        List<FormattedLine> divvy = new ArrayList<> (getLines ());
        getLines ().clear ();
        for (FormattedLine line : divvy) {
            int offset = idx;
            idx -= line.length ();
            // This adjusts for the assumed line break.
            idx--;
            if (offset <= 0) {
                blk.getLines ().add (line);
            } else if (line.length () <= offset) {
                getLines ().add (line);
            } else if (offset < line.length ()) {
                blk.getLines ().add (line.split (offset));
                getLines ().add (line);
            }
        }
        return blk;
    }

    /**
     * Transform this block to a block of the given type.
     * 
     * @param type
     *             the new type.
     * @return the transformed block.
     */
    public FormattedBlock transform(FormattedBlock.BlockType type) {
        if (type == null)
            return null;
        if (type == this.type)
            return this;
        FormattedBlock blk = clone ();
        blk.type = type;

        // Strip out any formatting for headings, etc.
        if (!blk.typeIs (BlockType.PARA, BlockType.NLIST))
            blk.getLines ().forEach (line -> line.stripFormatting ());
        return blk;
    }

    public FormattedBlock clone() {
        FormattedBlock blk = new FormattedBlock (type);
        blk.indent = indent;
        getLines ().forEach (line -> blk.add (line));
        return blk;
    }

    /**
     * Adds an (initially) empty line.
     * 
     * @return the added line.
     */
    public FormattedLine line() {
        FormattedLine line = new FormattedLine ();
        getLines ().add (line);
        return line;
    }

    /**
     * Adds and configures a line.
     * 
     * @param builder
     *                to configure the line.
     * @return this block instance.
     */
    public FormattedBlock line(Consumer<FormattedLine> builder) {
        FormattedLine line = line ();
        if (builder != null)
            builder.accept (line);
        return this;
    }

    /**
     * Adds the passed line without formatting.
     * 
     * @param line
     *              the line to add.
     * @return this block instance.
     */
    public FormattedBlock line(String line) {
        if (line != null)
            line (l -> l.append (line));
        return this;
    }

    /**
     * Adds lines derived from a single string where each line is split by a line
     * break (i.e. a <code>\n</code>).
     * 
     * @param lines
     *              the lines to split.
     * @return this block instance.
     */
    public FormattedBlock split(String lines) {
        if (lines != null) {
            for (String line : lines.split("\n"))
                line (l -> l.append (line));
        }
        return this;
    }

    /**
     * Adds a clone of the passed line.
     * 
     * @param line
     *             the line to clode.
     * @return the cloned line (that was added).
     */
    public FormattedLine add(FormattedLine line) {
        FormattedLine nline = line.clone ();
        getLines ().add (nline);
        return nline;
    }

    /**
     * Flattens the content to unformatted text. Line breaks are used for each new
     * line.
     * 
     * @return the flattened text.
     */
    public String flatten() {
        StringBuffer sb = new StringBuffer();
        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0)
                    sb.append("\n");
                sb.append (lines.get(i).flatten());
            }
        }
        return sb.toString ();
    }

    /************************************************************************
     * Overrides and contracted behaviour.
     ************************************************************************/
        
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < getLines ().size(); i++) {
            if (i > 0)
                str += "[//]";
            str += getLines ().get (i).toString ();
        }
        if ((meta != null) && !meta.isEmpty()) {
            str += "{meta=}";
            for (Entry<String,String> item : meta.entrySet()) {
                str += "<";
                str += item.getKey ();
                str += "=";
                String value = item.getValue ();
                if (value == null)
                    str += "null";
                else if (value.length() > 100)
                    str += value.substring(0, 100) + "...";
                else
                    str += value;
                str += ">";
            }
            str += "}";
        }
        if (indent > 0) {
            str += "{indent=";
            str += indent;
            str += "}";
        }
        return str;
    }
}
