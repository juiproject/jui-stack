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
package com.effacy.jui.text.ui.editor.tools;

import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.ui.editor.Block;
import com.effacy.jui.text.ui.editor.IFormattedContent;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Range;

/**
 * Tool explicitly for operating on {@link IFormattedContent}. The various tool
 * types and distinguided by the formatting the apply (see {@link FormatType}).
 * <p>
 * Tools can be activated based on context underwhich they operate.
 */
public class FormattedTextTool extends ButtonTool {

    /**
     * Creates a tools instance from the given format.
     * 
     * @param format
     *               the format that the tool should represent.
     * @return the tool instance.
     */
    public static FormattedTextTool tool(FormatType format) {
        if (format == null)
            throw new IllegalArgumentException ("Passed a null format");
        String icon;
        String label;
        if (FormatType.BLD == format) {
            icon = FontAwesome.bold ();
            label = "Bold";
        } else if (FormatType.CODE == format) {
            icon = FontAwesome.code ();
            label = "Code";
        } else if (FormatType.ITL == format) {
            icon = FontAwesome.italic ();
            label = "Italic";
        } else if (FormatType.STR == format) {
            icon = FontAwesome.strikethrough ();
            label = "Strike-through";
        } else if (FormatType.SUP == format) {
            icon = FontAwesome.superscript ();
            label = "Superscript";
        } else if (FormatType.SUB == format) {
            icon = FontAwesome.subscript ();
            label = "Subscript";
        } else if (FormatType.UL == format) {
            icon = FontAwesome.underline ();
            label = "Underline";
        } else if (FormatType.HL == format) {
            icon = FontAwesome.highlighter ();
            label = "Highlight";
        } else 
            throw new IllegalArgumentException ("Unable to find format " + format.name ());
        return new FormattedTextTool (icon, label, format);
    }

    /**
     * The format type the tool represents.
     */
    private FormatType format;

    /**
     * If the tool should remove the format rather than apply it.
     */
    private boolean remove;

    /**
     * Consruct an instance of the tool.
     * 
     * @param icon
     *               the icon to use.
     * @param label
     *               the display label.
     * @param format
     *               the format that it represents.
     */
    private FormattedTextTool(String icon, String label, FormatType format) {
        super (icon, label);
        this.format = format;
    }

    /**
     * Applies the relevant formatting to the active block and selection.
     */
    public boolean apply(IToolContext ctx) {
        Block block = ctx.block ();
        if (!(block instanceof IFormattedContent))
            return false;
        if (!((IFormattedContent) block).applies (format))
            return false;

        Range range = ctx.range ();
        // Logger.info ("APPLY: " + Debug.print (range));
        if (remove)
            range = ((IFormattedContent) block).remove (range, format);
        else
            range = ((IFormattedContent) block).apply (range, format);
        ctx.updateRange (range);
        return false;
    }

    /**
     * Determines the activation state of the tool based on the block that is active
     * and any selection in the block.
     */
    public void activate(IToolContext ctx) {
        Block block = ctx.block ();
        if ((ctx.range() == null) || !(block instanceof IFormattedContent) || !((IFormattedContent) block).applies (format)) {
            deactivate ();
            return;
        }
        this.remove = ((IFormattedContent) block).formats (ctx.range ()).contains (format);

        super.activate (ctx);
        if (remove)
            rootEl.classList.add (styles.applied ());
        else
            rootEl.classList.remove (styles.applied ());
    }
    
}
