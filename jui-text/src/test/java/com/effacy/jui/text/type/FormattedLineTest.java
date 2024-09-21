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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.FormattedLine.TextSegment;

public class FormattedLineTest {

    @Test
    public void testLine_Sequence() {
        FormattedLine line = new FormattedLine ();
        line.append ("This is ");
        line.append ("some formatted", FormatType.BLD);
        line.append (" text");

        Assertions.assertEquals ("This is [{8,14} bld]some formatted[/] text", line.toString ());

        List<TextSegment> segments = line.sequence ();
        Assertions.assertEquals (3, segments.size ());
        Assertions.assertEquals ("This is ", segments.get(0).text ());
        Assertions.assertEquals (0, segments.get(0).formatting ().length);
        Assertions.assertEquals ("some formatted", segments.get(1).text ());
        Assertions.assertEquals (1, segments.get(1).formatting ().length);
        Assertions.assertEquals (" text", segments.get(2).text ());
        Assertions.assertEquals (0, segments.get(2).formatting ().length);
    }

    @Test
    public void testFormat_SortOrder() {
        FormattedLine.Format format;
        
        format = new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL);
        Assertions.assertEquals (FormatType.UL, format.formats ()[0]);
        Assertions.assertEquals (FormatType.STR, format.formats ()[1]);
        Assertions.assertEquals (FormatType.CODE, format.formats ()[2]);

        format = new FormattedLine.Format(0, 0, FormatType.STR, FormatType.CODE, FormatType.UL);
        Assertions.assertEquals (FormatType.UL, format.formats ()[0]);
        Assertions.assertEquals (FormatType.STR, format.formats ()[1]);
        Assertions.assertEquals (FormatType.CODE, format.formats ()[2]);

        format = new FormattedLine.Format(0, 0, FormatType.UL, FormatType.STR, FormatType.CODE);
        Assertions.assertEquals (FormatType.UL, format.formats ()[0]);
        Assertions.assertEquals (FormatType.STR, format.formats ()[1]);
        Assertions.assertEquals (FormatType.CODE, format.formats ()[2]);
    }

    @Test
    public void testFormat_Equals() {
        Assertions.assertTrue (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL).equals (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL))
        );

        Assertions.assertTrue (
            new FormattedLine.Format(0, 0, FormatType.UL, FormatType.CODE, FormatType.STR).equals (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL))
        );

        Assertions.assertFalse (
            new FormattedLine.Format(0, 0, FormatType.UL, FormatType.CODE, FormatType.STR).equals (
            new FormattedLine.Format(1, 0, FormatType.CODE, FormatType.STR, FormatType.UL))
        );

        Assertions.assertFalse (
            new FormattedLine.Format(0, 0, FormatType.UL, FormatType.CODE, FormatType.STR).equals (
            new FormattedLine.Format(0, 1, FormatType.CODE, FormatType.STR, FormatType.UL))
        );

        Assertions.assertFalse (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.HL).equals (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL))
        );

        Assertions.assertFalse (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL).equals (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR))
        );

        Assertions.assertFalse (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR).equals (
            new FormattedLine.Format(0, 0, FormatType.CODE, FormatType.STR, FormatType.UL))
        );
    }
}
