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
package com.effacy.jui.text.ui.fragment;

import java.util.HashMap;
import java.util.Map;

import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedLine.FormatType;

/**
 * Used to generate DOM from a {@link FormattedLine}.
 * <p>
 * This operates in a special manner in that content is written directly into
 * the parent, including text nodes and formatting. The parent node is adorned
 * with the CSS {@link #CSS_STYLE} to which the child elements are styled. This
 * is a little different that normal when a fragment tends to have a singular
 * DOM element as its root. This approach makes it a little more flexible in
 * terms of writing out formatted text (it is used by {@link FText}).
 */
public class FLine extends Fragment<FLine> {

    /**
     * The fragment CSS styled applied to the parent.
     */
    public static final String CSS_STYLE = "juiFragFLine";

    public static FLine $(FormattedLine text) {
        return $ (null, text);
    }

    public static FLine $(IDomInsertableContainer<?> parent, FormattedLine text) {
        FLine frg = new FLine (text);
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public FLine(FormattedLine line) {
        // Here we want to build directly into the parent, but we need to style the
        // parent.
        super (parent -> {
            line.sequence ().forEach (segment -> {
                if (segment.formatting ().length == 0) {
                    Text.$ (parent, segment.text ());
                } else {
                    if (segment.contains(FormatType.A)) {
                        String href = segment.link();
                        if (href == null)
                            A.$ (parent).text (segment.text ());
                        else if (href.startsWith("http"))
                            A.$ (parent, href).text (segment.text ()).attr("target", "_blank");
                        else
                            A.$ (parent, href).text (segment.text ());
                    } else if (segment.contains (FormatType.CODE)) {
                        Span.$ (parent).text (segment.text ()).style ("fmt_code");
                    } else {
                        Span.$ (parent).$ (span -> {
                            for (FormattedLine.FormatType format : segment.formatting ()) {
                                String style = STYLES.get (format);
                                if (style != null)
                                    span.style ("fmt_" + style);
                            }
                            Text.$ (span, segment.text ());
                        });
                    }
                }
            });
        });
        super.parentStyleHook = CSS_STYLE;
    }

    protected static Map<FormattedLine.FormatType,String> STYLES = new HashMap<>();
    static {
        STYLES.put (FormattedLine.FormatType.BLD, "bold");
        STYLES.put (FormattedLine.FormatType.CODE, "code");
        STYLES.put (FormattedLine.FormatType.HL, "highlight");
        STYLES.put (FormattedLine.FormatType.ITL, "italic");
        STYLES.put (FormattedLine.FormatType.STR, "strike");
        STYLES.put (FormattedLine.FormatType.SUB, "subscript");
        STYLES.put (FormattedLine.FormatType.SUP, "superscript");
        STYLES.put (FormattedLine.FormatType.UL, "underline");
    }
}

