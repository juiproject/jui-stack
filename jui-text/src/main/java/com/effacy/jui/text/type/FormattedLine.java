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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.platform.util.client.Logger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gwt.core.shared.GwtIncompatible;

@JsonSerializable
public class FormattedLine {

    /**
     * Various format types that can be applied in the line.
     */
    public enum FormatType {

        /**
         * Bold.
         */
        BLD,
        
        /**
         * Underline
         */
        UL,
        
        /**
         * Italic
         */
        ITL,
        
        /**
         * Strikethrough
         */
        STR,
        
        /**
         * Superscript
         */
        SUP,
        
        /**
         * Subscript
         */
        SUB,
        
        /**
         * Code
         */
        CODE,
        
        /**
         * Highlight
         */
        HL,
        
        /**
         * Anchor (expects {@code link} metadata).
         */
        A;

        public boolean is(FormatType... types) {
            for (FormatType type : types) {
                if (type == this)
                    return true;
            }
            return false;
        }
    }

    /**
     * Describes a region of text along with the formatting applied.
     */
    @JsonSerializable
    public static class Format {

        /**
         * See {@link #getFormats()}.
         */
        private FormatType[] formats;
        
        /**
         * See {@link #getIndex()}.
         */
        private int index;
        
        /**
         * See {@link #getLength()}.
         */
        private int length;

        /**
         * See {@link #getMeta()}.
         */
        @JsonInclude(Include.NON_NULL)
        private Map<String,String> meta;

        /**
         * Serialisation constructor.
         */
        protected Format() {
            // Nothing.
        }

        /**
         * Construct with formatting data.
         * 
         * @param index
         *                see {@link #getIndex()}.
         * @param length
         *                see {@link #getLength()}.
         * @param formats
         *                see {@link #getFormats()}.
         */
        public Format(int index, int length, FormatType... formats) {
            this.index = index;
            this.length = length;
            this.formats = (formats == null) ? new FormatType[0] : formats;
            Arrays.sort (this.formats);
        }

        /**
         * Copy constructor.
         * 
         * @param copy
         *             the format to copy.
         */
        public Format(Format copy) {
            this.index = copy.index;
            this.length = copy.length;
            this.formats = copy.formats;
            this.meta = copy.meta;
        }

        /************************************************************************
         * Property setters and getters.
         ************************************************************************/

        /**
         * The formats that are being applied in this formatting segment.
         * 
         * @return the formats.
         */
        public List<FormatType> getFormats() {
            if (formats == null)
                return new ArrayList<> ();
            return Arrays.asList (formats);
        }

        /**
         * Serialisation setter for {@link #getFormats()}.
         */
        public void setFormats(List<FormatType> formats) {
            this.formats = (formats == null) ? null : formats.toArray (new FormatType[formats.size ()]);
        }

        /**
         * The formats as an array.
         * 
         * @return the formats.
         */
        public FormatType[] formats() {
            if (formats == null)
                return new FormatType[0];
            return formats;
        } 
        /**
         * The index into the line where this formatting segment begins.
         * 
         * @return the index (from 0).
         */
        public int getIndex() {
            return index;
        }

        /**
         * Serialisation setter for {@link #getIndex()}.
         */
        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * The length (in characters) of the formatted segment.
         * 
         * @return the length.
         */
        public int getLength() {
            return length;
        }

        /**
         * Serialisation setter for {@link #getLength()}.
         */
        public void setLength(int length) {
            this.length = length;
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
         * Overrides and contracted behaviour.
         ************************************************************************/

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Format))
                return false;
            Format other = (Format) obj;
            if (length != other.length)
                return false;
            if (index != other.index)
                return false;
            if (formats ().length != other.formats ().length)
                return false;
            for (int i = 0; i < formats ().length; i++) {
                if (formats ()[i] != other.formats ()[i])
                    return false;
            }
            return true;
        }


    }

    /**
     * Used to capture a block of text with its commensurate formatting.
     */
    public static class TextSegment {

        /**
         * See {@link #text()}.
         */
        private String text;

        /**
         * See {@link #formatting()}.
         */
        private FormatType[] formating;

        TextSegment(String text, FormatType[] formatting) {
            this.text = text;
            this.formating = (formatting == null) ? new FormatType[0] : formatting;
        }

        /**
         * The text to be formatted.
         * @return the text.
         */
        public String text() {
            return text;
        }

        /**
         * The formatting to apply.
         * 
         * @return the formatting (can be an empty array).
         */
        public FormatType[] formatting() {
            return formating;
        }

        /**
         * Determines if the segment contains the given formatting.
         * 
         * @param type
         *             the type to check.
         * @return {@code true} if it does.
         */
        public boolean contains(FormatType type) {
            for (FormatType t : formating) {
                if (t == type)
                    return true;
            }
            return false;
        }
    }
    /************************************************************************
     * Construction.
     ************************************************************************/

    /**
     * Consructs an empty line (zero length).
     */
    public FormattedLine() {
        text = "";
    }

    /**
     * Clones the line.
     */
    public FormattedLine clone() {
        FormattedLine line = new FormattedLine();
        line.text = text;
        getFormatting ().forEach (f -> line.getFormatting ().add (new Format (f)));
        return line;
    }

    @GwtIncompatible
    public static FormattedLine markdown(String line) {
        FormattedLine l = new FormattedLine ();
        l.text = line.replaceAll ("\\*", "");

        int idx = 0;
        int startCounter = 0;
        int formatStart = -1;
        while ((idx = line.indexOf ("*")) >= 0) {
            if (formatStart < 0) {
                formatStart = idx - startCounter;
            } else {
                int formatLength = idx - startCounter - formatStart + 1;
                if (formatLength > 0)
                    l.getFormatting ().add (new Format (formatStart, formatLength, FormatType.BLD));
            }
            startCounter++;
            if (idx >= line.length ())
                break;
            line = line.substring (idx + 1);
        }
        
        return l;
    }

    public static FormattedLine string(String line) {
        FormattedLine l = new FormattedLine ();
        l.text = line;
        return l;
    }

    /************************************************************************
     * Property setters and getters.
     ************************************************************************/

    /**
     * The text content (see {@link #getText ()}).
     */
    protected String text;

    /**
     * The formatting applied to the text.
     * <p>
     * Note that this should be internally consistent in that formatting consists of
     * non-overlapping regions in increasing order by index. Format operations are
     * internal to the line and construction of a line is incremental. As such this
     * should not need to be asserted.
     * <p>
     * See {@link #getFormatting()}.
     */
    @JsonInclude(Include.NON_NULL)
    protected List<Format> formatting;

    /**
     * The text content of the line.
     * 
     * @return the text content (non-{@code null} but may be empty).
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return List<Format> return the formatting
     */
    public List<Format> getFormatting() {
        if (formatting == null)
            formatting = new ArrayList<>();
        return formatting;
    }

    /**
     * @param formatting the formatting to set
     */
    public void setFormatting(List<Format> formatting) {
        this.formatting = formatting;
    }

    /************************************************************************
     * Derived properties.
     ************************************************************************/

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
     * Sequences the formatted text into contiguous block of formatted text (no
     * formatting simply results in an unformatted block).
     * 
     * @return the sequence.
     */
    public List<TextSegment> sequence() {
        // normalise ();
        List<TextSegment> result = new ArrayList<>();
        if ((formatting == null) || formatting.isEmpty ()) {
            result.add (new TextSegment (text, null));
        } else {
            int idx = 0;
            for (Format fmt : formatting) {
                if (fmt.index > idx) {
                    result.add (new TextSegment (text.substring(idx, fmt.index), null));
                    idx = fmt.index;
                }
                result.add (new TextSegment (text.substring (idx, fmt.index + fmt.length), fmt.formats));
                idx = fmt.index + fmt.length;
            }
            if (idx < text.length())
            result.add (new TextSegment (text.substring(idx), null));
        }
        return result;
    }

    /**
     * Flattens the content to unformatted text.
     * 
     * @return the flattened text.
     */
    public String flatten() {
        return (text == null) ? "" : text;
    }

    /************************************************************************
     * Behaviours.
     ************************************************************************/

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
        if (!getFormatting ().isEmpty ()) {
            for (Format format : new ArrayList<>(getFormatting ())) {
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
                    getFormatting ().remove (format);
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
        for (Format format : getFormatting ()) {
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
    public FormattedLine append(String text, FormatType... formats) {
        if ((text == null) || (text.length () == 0))
            return this;
        text = text.replace ('\u00a0', ' ');
        text = text.replaceAll ("[\\u0000-\\u001F\\u007F-\\u009F\\u061C\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", "");
        if (formats.length > 0) {
            Format format = new Format (this.text.length (), text.length (), formats);
            getFormatting ().add (format);
        }
        this.text += text;
        return this;
    }

    /**
     * Strips all formatting.
     */
    public void stripFormatting() {
        getFormatting ().clear();
    }

    /**
     * Merge the passed line into this line.
     * 
     * @param other
     *              the line to merge in.
     */
    public void merge(FormattedLine other) {
        if (other == null)
            return;
        int offset = text.length ();
        text = text += other.text;
        for (Format format : other.getFormatting ())
            getFormatting ().add (new Format (format.index + offset, format.length, format.formats));
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
    public FormattedLine split(int idx) {
        if (idx <= 0) {
            FormattedLine line = clone ();
            text = "";
            getFormatting ().clear ();
            return line;
        }
        if (idx >= length())
            return new FormattedLine();
        FormattedLine line = new FormattedLine();
        line.text = this.text.substring (idx);
        this.text = this.text.substring (0, idx);
        if (!getFormatting ().isEmpty ()) {
            List<Format> divvy = new ArrayList <> (getFormatting ());
            getFormatting ().clear ();
            for (Format format : divvy) {
                if (format.index + format.length <= idx) {
                    // Entirely to the left so add back to the left side (unchanged).
                    getFormatting ().add (format);
                } else if (format.index >= idx) {
                    // Entirely to the right so add to the right side but update the index (offset
                    // by -idx).
                    line.getFormatting ().add (new Format (format.index - idx, format.length, format.formats));
                } else {
                    int diff = idx - format.index;
                    // Add a portion back to the left side (length adjusted.)
                    getFormatting ().add (new Format (format.index, diff, format.formats));
                    // Add a portion to the right (begining at 0 and length adjusted).
                    line.getFormatting ().add (new Format (0, format.length - diff, format.formats));
                }
            }
        }
        return line;
    }


    /**
     * Visits the formatting.
     * 
     * @param visitor
     *                the visitor.
     */
    public void formatting(Consumer<Format> visitor) {
        getFormatting ().forEach (visitor);
    }

    /**
     * Traverses the elements of the line inclusive of the formatting.
     * <p>
     * This breaks the text content into formatted blocks and traverses those blocks.
     * 
     * @param visitor
     *                the visitor.
     */
    public void traverse(BiConsumer<String,FormatType[]> visitor) {
        // TODO: We need to properly order and merge the formats for safety.
        int idx = 0;

        for (Format format : getFormatting ()) {
            if (format.getIndex () > idx) {
                Logger.warn (" [idx=" + idx + ",fmt_idx=" + format.getIndex() + ",fmt_len" + format.getLength() + "] \"" + text + "\"(" + text.length() + ")");
                visitor.accept (text.substring (idx, format.getIndex ()), new FormatType[0]);
                idx = format.getIndex ();
            }
            visitor.accept (text.substring(idx, idx + format.getLength()), format.formats());
            idx += format.getLength ();
        }

        // Tail end after last format.
        visitor.accept (text.substring (idx, text.length ()), new FormatType[0]);
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
        if (getFormatting ().size() == 0)
            return;
        Collections.sort (getFormatting (), (o1, o2) -> {
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

    @Override
    public String toString() {
        String str = this.text;
        for (int i = getFormatting ().size() - 1; i >= 0; i--) {
            Format format = getFormatting ().get(i);
            String right = str.substring (format.index + format.length);
            String left = str.substring (0, format.index + format.length);
            String middle = left.substring (format.index);
            left = left.substring (0, format.index);
            str = left + "[{" + format.index + "," + format.length + "}";
            for (FormatType type : format.getFormats()) {
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
}
