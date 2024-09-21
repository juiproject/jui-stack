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
package com.effacy.jui.ui.client.editor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.platform.util.client.Itr;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;

public class ContentBlock {

    /**
     * Various block types (TODO: consider changing to a String to allow custom
     * blocks).
     */
    public enum BlockType {
        PARAGRAPH, HEADING_1, HEADING_2, HEADING_3, NUMBERED_LIST, EQUATION, DIAGRAM;

        /**
         * Determines if this is one of the passed types.
         * @param types
         * the types to check for.
         * @return {@code true} if so.
         */
        public boolean is(ContentBlock.BlockType...types) {
            for (ContentBlock.BlockType type : types) {
                if (this == type)
                    return true;
            }
            return false;
        }
    }

    /**
     * Various format types.
     */
    public enum FormatType {
        BOLD, UNDERLINE, ITALIC, STRIKE, SUPERSCRIPT, SUBSCRIPT, CODE, HIGHLIGHT;
    }

    /**
     * The type of content block this is.
     */
    private ContentBlock.BlockType type;

    /**
     * The level of indentation for the block.
     */
    private int indent = 0;

    /**
     * The lines of (generally formatted) text in the block.
     */
    private List<ContentBlock.Line> lines = new ArrayList<>();

    /**
     * Any applicable caption.
     */
    private String caption;

    /**
     * Construct over a given type.
     * 
     * @param type
     *             the type.
     */
    public ContentBlock(ContentBlock.BlockType type) {
        this.type = type;
    }

    /**
     * The level of indentation for the block.
     * 
     * @return the level (0 means no indentation).
     */
    public int indent() {
        return indent;
    }

    /**
     * Assign a level of indentation.
     * 
     * @param indent
     *               the level.
     */
    public ContentBlock indent(int indent) {
        this.indent = Math.min (5, Math.max(0, indent));
        return this;
    }

    /**
     * The total length of the block (being the sum of the length of the lines).
     * 
     * @return the length.
     */
    public int length() {
        int length = 0;
        for (int i = 0; i < lines.size (); i++) {
            if (i > 0)
                length++;
            length += lines.get (i).length ();
        }
        return length;
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
    public ContentBlock remove(int start, int len) {
        if (start < 0) {
            len += start;
            start = 0;
        }
        if (len <= 0)
            return this;

        for (ContentBlock.Line line : new ArrayList<>(lines)) {
            int ll = line.length ();
            if (len > 0) {
                line.remove (start, len);
                if (line.length() <= 0)
                    lines.remove (line);
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
    public ContentBlock insert(int start, String text) {
        // Ensure there is something to insert.
        if ((text == null) || (text.length() == 0))
            return this;
        // A negative start means at the end.
        if ((start < 0) || (start >= length ())) {
            lastLine ().append (text);
            return this;
        }
        // Insert internally.
        for (ContentBlock.Line line : new ArrayList<>(lines)) {
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
    public ContentBlock insert(int start, ContentBlock blk) {
        if ((blk == null) || (type != blk.type) || blk.lines.isEmpty())
            return this;
        if ((blk.lines.size () == 0) && (blk.lines.get (0).formatting.size () == 0))
            return insert (start, blk.lines.get (0).text);
        // A negative start means at the end.
        if ((start < 0) || (start >= length ())) {
            lastLine ().merge (blk.lines.get (0));
            for (int i = 1; i < blk.lines.size(); i++)
                add (blk.lines.get (i));
            return this;
        }
        // Insert internally.
        for (ContentBlock.Line line : new ArrayList<>(lines)) {
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
    public void merge(ContentBlock other) {
        if (other == null)
            return;
        if (type != other.type)
            return;
        if (lines ().isEmpty ()) {
            other.lines ().forEach (line -> { 
                add (line);
            });
        } else {
            Itr.forEach (other.lines (), (c,v) -> {
                if (c.first())
                    lastLine ().merge (v);
                else
                    lines ().add (v.clone ());
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
    public ContentBlock split(int idx) {
        if (idx <= 0) {
            ContentBlock blk = clone ();
            this.lines.clear ();
            return blk;
        }
        ContentBlock blk = new ContentBlock (type);
        blk.indent = indent;
        List<ContentBlock.Line> divvy = new ArrayList<> (lines);
        lines.clear ();
        for (ContentBlock.Line line : divvy) {
            int offset = idx;
            idx -= line.length ();
            // This adjusts for the assumed line break.
            idx--;
            if (offset <= 0) {
                blk.lines.add (line);
            } else if (line.length() <= offset) {
                this.lines.add (line);
            } else if (offset < line.length()) {
                blk.lines.add (line.split(offset));
                this.lines.add (line);
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
    public ContentBlock transform(ContentBlock.BlockType type) {
        if (type == null)
            return null;
        if (type == this.type)
            return this;
        ContentBlock blk = clone ();
        blk.type = type;

        // Strip out any formatting for headings, etc.
        if (!type.is (BlockType.PARAGRAPH, BlockType.NUMBERED_LIST))
            blk.lines ().forEach (line -> line.stripFormatting ());
        return blk;
    }

    public ContentBlock clone() {
        ContentBlock blk = new ContentBlock (type);
        blk.indent = indent;
        lines.forEach (line -> blk.add (line));
        return blk;
    }

    public String toJson() {
        String json = "{";
        json += "\"type\":\"";
        json += type.name ();
        json += "\",\"lines\":[";
        for (int i = 0; i < lines.size (); i++) {
            if (i > 0)
                json += ",";
            json += lines.get (i).toJson ();
        }
        json += "]}";
        return json;
    }

    /**
     * The type of block this is.
     * @return the type.
     */
    public ContentBlock.BlockType type() {
        return type;
    }

    /**
     * The lines of (potentially) formatted text.
     * 
     * @return the lines.
     */
    public List<ContentBlock.Line> lines() {
        return lines;
    }

    /**
     * Contracts the lines into a single string using line breaks for new lines and
     * with no consideration to formatting.
     * 
     * @return the composed content.
     */
    public String contractLines() {
        String str = "";
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                str += "\n";
            str += lines.get(i);
        }
        return str;
    }

    /**
     * Gets any assigned caption.
     * 
     * @return the caption.
     */
    public String caption() {
        return caption;
    }

    /**
     * Adds an (initially) empty line.
     * 
     * @return the added line.
     */
    public ContentBlock.Line line() {
        ContentBlock.Line line = new Line ();
        lines.add (line);
        return line;
    }

    /**
     * Adds and configures a line.
     * 
     * @param builder
     *                to configure the line.
     * @return this block instance.
     */
    public ContentBlock line(Consumer<ContentBlock.Line> builder) {
        ContentBlock.Line line = line ();
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
    public ContentBlock line(String line) {
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
    public ContentBlock split(String lines) {
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
    public ContentBlock.Line add(ContentBlock.Line line) {
        ContentBlock.Line nline = line.clone ();
        lines.add (nline);
        return nline;
    }

    /**
     * Obtains the last line in the collection of lines for this block. If there are
     * no lines then one will be created.
     * 
     * @return the last line.
     */
    public ContentBlock.Line lastLine() {
        if (lines.isEmpty ())
            return line ();
        return lines.get (lines.size() - 1);
    }

    /**
     * Assigns a caption.
     */
    public ContentBlock caption(String caption) {
        this.caption = caption;
        return this;
    }
        
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                str += "[//]";
            str += lines.get (i).toString ();
        }
        if (!StringSupport.empty (caption)) {
            str += "{caption=}";
            str += caption;
            str += "}";
        }
        if (indent > 0) {
            str += "{indent=";
            str += indent;
            str += "}";
        }
        return str;
    }

    public static class Line {

        /**
         * Describes a region of text along with the formatting applied.
         */
        public static class Format {
            private ContentBlock.FormatType[] formats;
            private int index;
            private int length;

            public Format(int index, int length, ContentBlock.FormatType... formats) {
                this.index = index;
                this.length = length;
                this.formats = (formats == null) ? new ContentBlock.FormatType[0] : formats;
                Arrays.sort (this.formats);
            }

            public Format(Line.Format copy) {
                this.index = copy.index;
                this.length = copy.length;
                this.formats = copy.formats;
            }

            public String toJson() {
                String json = "{";
                json += "\"index\":" + index;
                json += ",\"length\":" + length;
                json += ",\"formats\":[";
                for (int i = 0; i < formats.length; i++) {
                    if (i > 0)
                        json += ",";
                    json += "\"";
                    json += formats[i].name ();
                    json += "\"";
                }
                json += "]}";
                return json;
            }

            public int index() {
                return index;
            }

            public int length() {
                return length;
            }

            public ContentBlock.FormatType[] formats() {
                if (formats == null)
                    return new ContentBlock.FormatType[0];
                return formats;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Format))
                    return false;
                Format other = (Format) obj;
                if (length != other.length)
                    return false;
                if (index != other.index)
                    return false;
                if (formats.length != other.formats.length)
                    return false;
                for (int i = 0; i < formats.length; i++) {
                    if (formats[i] != other.formats[i])
                        return false;
                }
                return true;
            }
        }

        /**
         * The text content (see {@link #text ()}).
         */
        private String text;

        /**
         * The formatting applied to the text.
         * <p>
         * Note that this should be internally consistent in that formatting consists of
         * non-overlapping regions in increasing order by index. Format operations are
         * internal to the line and construction of a line is incremental. As such this
         * should not need to be asserted.
         */
        private List<Line.Format> formatting = new ArrayList<>();

        /**
         * Consructs an empty line (zero length).
         */
        public Line() {
            text = "";
        }

        /**
         * Clones the line.
         */
        public ContentBlock.Line clone() {
            ContentBlock.Line line = new Line();
            line.text = text;
            formatting.forEach (f -> line.formatting.add (new Format (f)));
            return line;
        }

        /**
         * Removes the range from the line and returns the line.
         * 
         * @param start
         *              the start point.
         * @param len
         *              the length to remove.
         * @return {@code true} if content was removed.
         */
        public boolean remove(int start, int len) {
            if (start < 0) {
                len += start;
                start = 0;
            }
            if (len <= 0)
                return false;
            if (start >= text.length())
                return false;
            int end = start + len;
            if (end > text.length())
                end = text.length ();
            text = text.substring(0, start) + text.substring (end);
            if (!formatting.isEmpty ()) {
                for (Line.Format format : new ArrayList<>(formatting)) {
                    if ((format.index >= start) && (format.index < end)) {
                        // Start of format is in range. Reduce the length to match and adjust the start.
                        format.length = (format.index + format.length <= end) ? end - format.index - 1 : format.length - (end - format.index);
                        format.index = start;
                    } else if ((format.index + format.length >= start) && (format.index + format.length < end)) {
                        // End of format is in range. Reduce the length.
                        format.length = start - format.index;
                    } else if (format.index >= end) {
                        format.index -= len;
                    }
                    if (format.length <= 0)
                        formatting.remove (format);
                }
            }
            return true;
        }

        public void insert(int start, String text) {
            if ((text == null) || (text.length() == 0))
                return;
            if ((start < 0) || (start >= this.text.length())) {
                this.text += text;
                return;
            }
            int len = text.length();
            this.text = this.text.substring (0, start) + text + this.text.substring (start);
            for (Line.Format format : formatting) {
                if (format.index >= start) {
                    format.index += len;
                } else if (format.index + format.length >= start) {
                    format.length += len;
                }
            }
        }

        /**
         * Appends a region of text with formatting. Note that no attempt is made to
         * merge adjacent formatted blocks. However, this is additive so regions will
         * not overlap and will be increasing.
         * <p>
         * Note that if no formatting is present then no format will be applied and if
         * the passed text is empty (zero length, whitespace is valid content) nothing
         * is changed.
         * <p>
         * During assignment any non-breaking spaces will be replaced by normal spaces.
         * 
         * @param text
         *                the text to append.
         * @param formats
         *                the formatting to apply.
         * @return this line.
         */
        public ContentBlock.Line append(String text, ContentBlock.FormatType... formats) {
            if ((text == null) || (text.length() == 0))
                return this;
            text = text.replace ('\u00a0', ' ');
            text = text.replaceAll ("[\\u0000-\\u001F\\u007F-\\u009F\\u061C\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", "");
            if (formats.length > 0) {
                Line.Format format = new Format (this.text.length(), text.length(), formats);
                formatting.add (format);
            }
            this.text += text;
            return this;
        }

        /**
         * Strips all formatting.
         */
        public void stripFormatting() {
            formatting.clear();
        }

        /**
         * Merge the passed line into this line.
         * 
         * @param other
         *              the line to merge in.
         */
        public void merge(ContentBlock.Line other) {
            if (other == null)
                return;
            int offset = text.length ();
            text = text += other.text;
            for (Line.Format format : other.formatting)
                formatting.add (new Format (format.index + offset, format.length, format.formats));
        }

        /**
         * Splits this line at the given index. Returned is the right portion of the
         * split. If the index is 0 or less then this line is set to empty and the
         * returned line is a clone. If the index the the length (or greater) that the
         * line the the line it unchanged and an empty line returned.
         * 
         * @param idx
         *            the index into the line to split at.
         * @return the left side of the split (may be empty, meaning of zero length).
         */
        public ContentBlock.Line split(int idx) {
            if (idx <= 0) {
                ContentBlock.Line line = clone ();
                text = "";
                formatting.clear();
                return line;
            }
            if (idx >= length())
                return new Line();
            ContentBlock.Line line = new Line();
            line.text = this.text.substring (idx);
            this.text = this.text.substring (0, idx);
            if (!this.formatting.isEmpty()) {
                List<Line.Format> divvy = new ArrayList <> (this.formatting);
                this.formatting.clear ();
                for (Line.Format format : divvy) {
                    if (format.index + format.length <= idx) {
                        // Entirely to the left so add back to the left side (unchanged).
                        this.formatting.add (format);
                    } else if (format.index >= idx) {
                        // Entirely to the right so add to the right side but update the index (offset
                        // by -idx).
                        line.formatting.add (new Format (format.index - idx, format.length, format.formats));
                    } else {
                        int diff = idx - format.index;
                        // Add a portion back to the left side (length adjusted.)
                        this.formatting.add (new Format (format.index, diff, format.formats));
                        // Add a portion to the right (begining at 0 and length adjusted).
                        line.formatting.add (new Format (0, format.length - diff, format.formats));
                    }
                }
            }
            return line;
        }

        /**
         * The length of the line (in characters). Note that formatting is imposed as
         * meta-data so does not impact the length.
         * 
         * @return the length in characters.
         */
        public int length() {
            return text.length ();
        }

        /**
         * The text content of the line.
         * 
         * @return the text content (non-{@code null} but may be empty).
         */
        public String text() {
            return text;
        }

        /**
         * Visits the formatting.
         * 
         * @param visitor
         *                the visitor.
         */
        public void formatting(Consumer<Line.Format> visitor) {
            formatting.forEach (visitor);
        }

        /**
         * Converts the line to JSON.
         * 
         * @return the JSON string.
         */
        public String toJson() {
            String json = "{";
            json += "\"text\":\"";
            json += text.replace ("\"", "\\\"");
            json += "\",\"formatting\":[";
            for (int i = 0; i < formatting.size (); i++) {
                if (i > 0)
                    json += ",";
                json += formatting.get (i).toJson ();
            }
            json += "]}";
            return json;
        }


        /**
         * Traverses the elements of the line inclusive of the formatting.
         * <p>
         * This breaks the text content into formatted blocks and traverses those blocks.
         * 
         * @param visitor
         *                the visitor.
         */
        public void traverse(BiConsumer<String,ContentBlock.FormatType[]> visitor) {
            // TODO: We need to properly order and merge the formats for safety.
            int idx = 0;

            for (Line.Format format : formatting) {
                if (format.index () > idx) {
                    Logger.warn (" [idx=" + idx + ",fmt_idx=" + format.index() + ",fmt_len" + format.length() + "] \"" + text + "\"(" + text.length() + ")");
                    visitor.accept (text.substring (idx, format.index ()), new ContentBlock.FormatType[0]);
                    idx = format.index ();
                }
                visitor.accept (text.substring(idx, idx + format.length()), format.formats());
                idx += format.length ();
            }

            // Tail end after last format.
            visitor.accept (text.substring (idx, text.length ()), new ContentBlock.FormatType[0]);
        }

        @Override
        public String toString() {
            String str = this.text;
            for (int i = formatting.size() - 1; i >= 0; i--) {
                Line.Format format = formatting.get(i);
                String right = str.substring(format.index + format.length);
                String left = str.substring(0, format.index + format.length);
                String middle = left.substring(format.index);
                left = left.substring(0, format.index);
                str = left + "[{" + format.index + "," + format.length + "}";
                for (ContentBlock.FormatType type : format.formats()) {
                    str += ' ';
                    if (type == null)
                        str += "null";
                    else
                        str += type.name().toLowerCase();
                }
                str += "]"+ middle + "[/]";
                str += right;
            }
            return str;
        }

        /**
         * Normalises the line content. This ensures that the formats are correctly
         * sorted and separated.
         * <p>
         * Note that this is not currently used as the current set of operations
         * preserve the order. Use this when an operation (such as an insert with
         * formatted text) is employed.
         */
        protected void normalise() {
            if (formatting.size() == 0)
                return;
            Collections.sort (formatting, (o1, o2) -> {
                if (o1.index < o2.index)
                    return -1;
                if (o1.index > o2.index)
                    return 1;
                if (o1.length < o2.length)
                    return -1;
                if (o1.length > o2.length)
                    return 1;
                if (o1.formats.length < o2.formats.length)
                    return -1;
                if (o1.formats.length > o2.formats.length)
                    return 1;
                return 0;
            });
        }
    }
}