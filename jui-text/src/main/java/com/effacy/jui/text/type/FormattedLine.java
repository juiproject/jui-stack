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
import com.effacy.jui.platform.core.JuiIncompatible;
import com.effacy.jui.platform.util.client.Logger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gwt.core.shared.GwtIncompatible;

@JsonSerializable
public class FormattedLine {

    /**
     * Meta-data key for link information.
     */
    public static final String META_LINK = "link";

    /**
     * Meta-data key for variable information.
     */
    public static final String META_VARIABLE = "variable";

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

        /**
         * See {@link #link()}.
         */
        private String link;

        /**
         * See {@link #variable()}.
         */
        private boolean variable;

        /**
         * See {@link #meta()}.
         */
        private Map<String,String> meta;

        TextSegment(String text, FormatType[] formatting, String link) {
            this(text, formatting, link, false);
        }

        TextSegment(String text, FormatType[] formatting, String link, boolean variable) {
            this.text = text;
            this.formating = (formatting == null) ? new FormatType[0] : formatting;
            this.link = link;
            this.variable = variable;
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
         * The link (if any).
         *
         * @return the link.
         */
        public String link() {
            return link;
        }

        /**
         * Determines if this segment represents a variable. When {@code true},
         * {@link #text()} returns the display label and the variable name is
         * available via {@code meta().get(META_VARIABLE)}.
         *
         * @return {@code true} if this is a variable segment.
         */
        public boolean variable() {
            return variable;
        }

        /**
         * Meta-data associated with the segment.
         * 
         * @return any metadata.
         */
        public Map<String,String> meta() {
            if (meta == null)
                meta = new HashMap<> ();
            return meta;
        }

        /**
         * Determines if the segment has meta-data.
         * 
         * @return {@code true} if it does.
         */
        public boolean hasMeta() {
            return (meta != null) && !meta.isEmpty();
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

    @JuiIncompatible
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
     * Assigns formatting.
     * 
     * @param formatting
     *                   the formatting to assign.
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
            result.add (new TextSegment (text, null, null));
        } else {
            int idx = 0;
            for (Format fmt : formatting) {
                String link = (fmt.getMeta() != null) ? fmt.getMeta().get(META_LINK) : null;
                String variable = (fmt.getMeta() != null) ? fmt.getMeta().get(META_VARIABLE) : null;
                if (fmt.index > idx)
                    result.add (new TextSegment (text.substring(idx, fmt.index), null, null));
                TextSegment segment;
                if ((variable != null) && !variable.isEmpty()) {
                    // Variable segment: use underlying line text if present (label-as-text
                    // convention), otherwise fall back to the variable name (zero-length
                    // convention from the variable() builder method).
                    String varText = (fmt.length > 0) ? text.substring(fmt.index, fmt.index + fmt.length) : variable;
                    segment = new TextSegment (varText, fmt.formats, link, true);
                } else {
                    segment = new TextSegment (text.substring (fmt.index, fmt.index + fmt.length), fmt.formats, link);
                }
                // Copy metadata (excluding link which is exposed via link()).
                if (fmt.getMeta() != null) {
                    for (Map.Entry<String,String> entry : fmt.getMeta().entrySet()) {
                        if (!META_LINK.equals(entry.getKey()))
                            segment.meta().put(entry.getKey(), entry.getValue());
                    }
                }
                result.add(segment);
                idx = fmt.index + fmt.length;
            }
            if (idx < text.length())
                result.add (new TextSegment (text.substring(idx), null, null));
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
                    // Start of format is in range. Keep only the portion
                    // that extends beyond the deletion (if any).
                    format.length = format.length - (end - format.index);
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
                // Variable formats are atomic — never expand them on adjacent insertion.
                if ((format.meta != null) && format.meta.containsKey(META_VARIABLE))
                    continue;
                format.length += len;
            }
        }
    }

    /**
     * Appends a link at the current position. The link text is appended and formatted
     * with the {@link FormatType#A} format type along with any additional formats.
     *
     * @param text
     *                the link text to display (if {@code null} or empty, nothing is added).
     * @param link
     *                the URL to link to (if {@code null} or empty, text is appended without link).
     * @param formats
     *                optional additional formatting to apply (A format is always included).
     * @return this line.
     */
    public FormattedLine link(String text, String link, FormatType... formats) {
        if ((text == null) || text.isEmpty())
            return this;
        if ((link == null) || link.isEmpty())
            return append(text, formats);
        text = text.replace('\u00a0', ' ');
        text = text.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\u061C\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]", "");
        FormatType[] allFormats = new FormatType[formats.length + 1];
        allFormats[0] = FormatType.A;
        System.arraycopy(formats, 0, allFormats, 1, formats.length);
        Format format = new Format(this.text.length(), text.length(), allFormats);
        format.getMeta().put(META_LINK, link);
        getFormatting().add(format);
        this.text += text;
        return this;
    }

    /**
     * Appends a variable placeholder at the current position. The variable will be
     * resolved at render time when {@link #sequence()} is called.
     * <p>
     * Variables are represented as zero-length format regions with the variable name
     * stored in metadata. When sequenced, variable segments will have
     * {@link TextSegment#variable()} return {@code true} and {@link TextSegment#text()}
     * return the variable name.
     *
     * @param name
     *                the variable name (if {@code null} or empty, nothing is added).
     * @param formats
     *                optional formatting to apply to the variable when rendered.
     * @return this line.
     */
    public FormattedLine variable(String name, FormatType... formats) {
        if ((name == null) || name.isEmpty())
            return this;
        Format format = new Format(this.text.length(), 0, formats);
        format.getMeta().put(META_VARIABLE, name);
        getFormatting().add(format);
        return this;
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
     * Adds a {@link FormatType} to every character in the range
     * {@code [start, start+len)}. Existing formats in the range gain the type;
     * gaps are filled with new formats containing just the type. Metadata on
     * existing formats is preserved when they are split at range boundaries.
     *
     * @param start
     *              start offset in the line.
     * @param len
     *              number of characters.
     * @param type
     *              the format type to add.
     */
    public void addFormat(int start, int len, FormatType type) {
        if (len <= 0)
            return;
        int end = start + len;
        List<Format> result = new ArrayList<>();
        int coveredUpTo = start;

        for (Format f : getFormatting()) {
            int fEnd = f.index + f.length;

            // Entirely before range.
            if (fEnd <= start) {
                result.add(f);
                continue;
            }

            // Entirely after range — fill any remaining gap first.
            if (f.index >= end) {
                if (coveredUpTo < end)
                    result.add(new Format(coveredUpTo, end - coveredUpTo, type));
                coveredUpTo = end;
                result.add(f);
                continue;
            }

            // Gap between previous coverage and this format (within range).
            int gapStart = Math.max(start, coveredUpTo);
            if (gapStart < f.index)
                result.add(new Format(gapStart, f.index - gapStart, type));

            // Pre-overlap portion (before our range).
            if (f.index < start) {
                Format pre = new Format(f.index, start - f.index, f.formats());
                if (f.meta != null)
                    pre.meta = new HashMap<>(f.meta);
                result.add(pre);
            }

            // Overlap portion — add the type.
            int overlapStart = Math.max(f.index, start);
            int overlapEnd = Math.min(fEnd, end);
            Format overlap = new Format(overlapStart, overlapEnd - overlapStart, addType(f.formats(), type));
            if (f.meta != null)
                overlap.meta = new HashMap<>(f.meta);
            result.add(overlap);

            // Post-overlap portion (after our range).
            if (fEnd > end) {
                Format post = new Format(end, fEnd - end, f.formats());
                if (f.meta != null)
                    post.meta = new HashMap<>(f.meta);
                result.add(post);
            }

            coveredUpTo = Math.max(coveredUpTo, overlapEnd);
        }

        // Trailing gap.
        if (coveredUpTo < end)
            result.add(new Format(coveredUpTo, end - coveredUpTo, type));

        getFormatting().clear();
        getFormatting().addAll(result);
    }

    /**
     * Removes a {@link FormatType} from every character in the range
     * {@code [start, start+len)}. Formats that lose their last type (and have
     * no metadata) are removed entirely. Metadata is preserved on remaining
     * fragments.
     *
     * @param start
     *              start offset in the line.
     * @param len
     *              number of characters.
     * @param type
     *              the format type to remove.
     */
    public void removeFormat(int start, int len, FormatType type) {
        if (len <= 0)
            return;
        int end = start + len;
        List<Format> result = new ArrayList<>();

        for (Format f : getFormatting()) {
            int fEnd = f.index + f.length;

            // No overlap with the range — keep unchanged.
            if ((fEnd <= start) || (f.index >= end)) {
                result.add(f);
                continue;
            }

            // Pre-overlap portion.
            if (f.index < start) {
                Format pre = new Format(f.index, start - f.index, f.formats());
                if (f.meta != null)
                    pre.meta = new HashMap<>(f.meta);
                result.add(pre);
            }

            // Overlap portion — remove the type.
            int overlapStart = Math.max(f.index, start);
            int overlapEnd = Math.min(fEnd, end);
            FormatType[] reduced = removeType(f.formats(), type);
            if ((reduced.length > 0) || ((f.meta != null) && !f.meta.isEmpty())) {
                Format overlap = new Format(overlapStart, overlapEnd - overlapStart, reduced);
                if (f.meta != null)
                    overlap.meta = new HashMap<>(f.meta);
                result.add(overlap);
            }

            // Post-overlap portion.
            if (fEnd > end) {
                Format post = new Format(end, fEnd - end, f.formats());
                if (f.meta != null)
                    post.meta = new HashMap<>(f.meta);
                result.add(post);
            }
        }

        getFormatting().clear();
        getFormatting().addAll(result);
    }

    /**
     * Checks whether every character in {@code [start, start+len)} is covered
     * by a format containing the given type.
     *
     * @param start
     *              start offset in the line.
     * @param len
     *              number of characters.
     * @param type
     *              the format type to check.
     * @return {@code true} if the entire range is covered.
     */
    public boolean hasFormat(int start, int len, FormatType type) {
        if (len <= 0)
            return false;
        int end = start + len;
        int coveredUpTo = start;

        for (Format f : getFormatting()) {
            if (f.index >= end)
                break;
            int fEnd = f.index + f.length;
            if (fEnd <= coveredUpTo)
                continue;
            if (f.index > coveredUpTo)
                return false;
            boolean hasType = false;
            for (FormatType ft : f.formats()) {
                if (ft == type) {
                    hasType = true;
                    break;
                }
            }
            if (!hasType)
                return false;
            coveredUpTo = Math.min(fEnd, end);
        }
        return coveredUpTo >= end;
    }

    private static FormatType[] addType(FormatType[] existing, FormatType type) {
        for (FormatType ft : existing) {
            if (ft == type)
                return existing;
        }
        FormatType[] result = new FormatType[existing.length + 1];
        System.arraycopy(existing, 0, result, 0, existing.length);
        result[existing.length] = type;
        Arrays.sort(result);
        return result;
    }

    private static FormatType[] removeType(FormatType[] existing, FormatType type) {
        int idx = -1;
        for (int i = 0; i < existing.length; i++) {
            if (existing[i] == type) {
                idx = i;
                break;
            }
        }
        if (idx < 0)
            return existing;
        FormatType[] result = new FormatType[existing.length - 1];
        System.arraycopy(existing, 0, result, 0, idx);
        System.arraycopy(existing, idx + 1, result, idx, existing.length - idx - 1);
        return result;
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
