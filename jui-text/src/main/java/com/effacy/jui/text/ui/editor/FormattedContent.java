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
package com.effacy.jui.text.ui.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.effacy.jui.text.type.FormattedBlock;
import com.effacy.jui.text.type.FormattedLine.FormatType;

import elemental2.dom.Element;
import elemental2.dom.Range;

/**
 * Implementation of {@link IFormattedContent} that wraps around a DOM element.
 * Makes use of {@link EditorSupport} to implement the formatting actions.
 */
public class FormattedContent implements IFormattedContent {

    static private Map<FormatType,String> STYLES = new HashMap<>();
    static private Map<String,FormatType> STYLES_INVERSE = new HashMap<>();
    static {
        STYLES.put (FormatType.BLD, "edt-b");
        STYLES.put (FormatType.UL, "edt-u");
        STYLES.put (FormatType.ITL, "edt-i");
        STYLES.put (FormatType.SUP, "edt-sup");
        STYLES.put (FormatType.SUB, "edt-sub");
        STYLES.put (FormatType.CODE, "edt-code");
        STYLES.put (FormatType.STR, "edt-strike");
        STYLES.put (FormatType.HL, "edt-hl");
        STYLES.forEach ((k,v) -> STYLES_INVERSE.put (v, k));
    }

    public static FormatType resolve(String style) {
        return STYLES_INVERSE.get (style);
    }

    public static String resolve(FormatType style) {
        return STYLES.get (style);
    }

    private Element containerEl;

    public FormattedContent(Element containerEl) {
        this.containerEl=  containerEl;
    }

    /**
     * The container element.
     * 
     * @return the element.
     */
    public Element el() {
        return containerEl;
    }

    @Override
    public Range apply(Range range, FormatType format) {
        return EditorSupport.apply (containerEl, range, STYLES.get(format));
    }

    @Override
    public Range remove(Range range, FormatType format) {
        return EditorSupport.clear (containerEl, range, STYLES.get(format));
    }

    @Override
    public Set<FormatType> formats(Range range) {
        Set<FormatType> formats = new HashSet<>();
        for (String style : EditorSupport.styles (containerEl, range)) {
            FormatType format = STYLES_INVERSE.get (style);
            if (format != null)
                formats.add (format);
        }
        return formats;
    }

    public void populate(FormattedBlock content) {
        content.line ();
        EditorSupport.traverse (containerEl, (str,styles) -> {
            FormatType[] formats = new FormatType[styles.length];
            for (int i = 0; i < formats.length; i++)
                formats[i] = FormattedContent.resolve (styles[i]);
            content.lastLine ().append (str, formats);
        }, () -> {
            content.line ();
        });
    }
     
}
