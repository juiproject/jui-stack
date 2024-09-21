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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.ui.client.editor.model.ContentBlock.FormatType;

public class ContentBlockLineTest {

    @Test
    public void testFormat_SortOrder() {
        ContentBlock.Line.Format format;
        
        format = new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE);
        Assertions.assertEquals (FormatType.UNDERLINE, format.formats ()[0]);
        Assertions.assertEquals (FormatType.STRIKE, format.formats ()[1]);
        Assertions.assertEquals (FormatType.CODE, format.formats ()[2]);

        format = new ContentBlock.Line.Format(0, 0, FormatType.STRIKE, FormatType.CODE, FormatType.UNDERLINE);
        Assertions.assertEquals (FormatType.UNDERLINE, format.formats ()[0]);
        Assertions.assertEquals (FormatType.STRIKE, format.formats ()[1]);
        Assertions.assertEquals (FormatType.CODE, format.formats ()[2]);

        format = new ContentBlock.Line.Format(0, 0, FormatType.UNDERLINE, FormatType.STRIKE, FormatType.CODE);
        Assertions.assertEquals (FormatType.UNDERLINE, format.formats ()[0]);
        Assertions.assertEquals (FormatType.STRIKE, format.formats ()[1]);
        Assertions.assertEquals (FormatType.CODE, format.formats ()[2]);
    }

    @Test
    public void testFormat_Equals() {
        Assertions.assertTrue (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE).equals (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE))
        );

        Assertions.assertTrue (
            new ContentBlock.Line.Format(0, 0, FormatType.UNDERLINE, FormatType.CODE, FormatType.STRIKE).equals (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE))
        );

        Assertions.assertFalse (
            new ContentBlock.Line.Format(0, 0, FormatType.UNDERLINE, FormatType.CODE, FormatType.STRIKE).equals (
            new ContentBlock.Line.Format(1, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE))
        );

        Assertions.assertFalse (
            new ContentBlock.Line.Format(0, 0, FormatType.UNDERLINE, FormatType.CODE, FormatType.STRIKE).equals (
            new ContentBlock.Line.Format(0, 1, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE))
        );

        Assertions.assertFalse (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.HIGHLIGHT).equals (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE))
        );

        Assertions.assertFalse (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE).equals (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE))
        );

        Assertions.assertFalse (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE).equals (
            new ContentBlock.Line.Format(0, 0, FormatType.CODE, FormatType.STRIKE, FormatType.UNDERLINE))
        );
    }
}
