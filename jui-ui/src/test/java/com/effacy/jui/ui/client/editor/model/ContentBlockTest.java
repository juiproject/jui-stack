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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.ui.client.editor.model.ContentBlock.BlockType;
import com.effacy.jui.ui.client.editor.model.ContentBlock.FormatType;

public class ContentBlockTest {

    @Test
    public void testLength_v1() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("AAA, ")
                .append ("BBB", FormatType.BOLD)
                .append (" and CCC."));
        
        Assertions.assertEquals (17, blk1.length ());
    }

    @Test
    public void testLength_v2() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line.append ("AAA "))
            .line (line -> line.append (" and BBB."));
        
        Assertions.assertEquals (14, blk1.length ());
    }
    
    @Test
    public void testSplit_LeftOfFormatting() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."))
            .indent (2);
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.{indent=2}", blk1.toString());
        Assertions.assertEquals (2, blk1.indent ());
    
        ContentBlock blk2 = blk1.split (10);

        Assertions.assertEquals ("This is to{indent=2}", blk1.toString());
        Assertions.assertEquals (2, blk1.indent ());
        Assertions.assertEquals (" the left, [{11,18} bold]this is the middle[/] and this is the right.{indent=2}", blk2.toString());
        Assertions.assertEquals (2, blk2.indent ());
    }

    @Test
    public void testSplit_RightOfFormatting() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (44);

        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and ", blk1.toString());
        Assertions.assertEquals ("this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_MiddleOfFormatting() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (26);

        Assertions.assertEquals ("This is to the left, [{21,5} bold]this [/]", blk1.toString());
        Assertions.assertEquals ("[{0,13} bold]is the middle[/] and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_LeftEdgeOfFormatting() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (21);

        Assertions.assertEquals ("This is to the left, ", blk1.toString());
        Assertions.assertEquals ("[{0,18} bold]this is the middle[/] and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_RightEdgeOfFormatting() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (39);

        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/]", blk1.toString());
        Assertions.assertEquals (" and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_AtLeftOfLineBreak() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (20);

        Assertions.assertEquals ("This is to the left ", blk1.toString());
        Assertions.assertEquals (" and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_AtRightOfLineBreak() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (21);

        Assertions.assertEquals ("This is to the left ", blk1.toString());
        Assertions.assertEquals (" and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_BeforeLineBreak() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (19);

        Assertions.assertEquals ("This is to the left", blk1.toString());
        Assertions.assertEquals (" [//] and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_AfterLineBreak() {
        ContentBlock blk1 = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        ContentBlock blk2 = blk1.split (22);

        Assertions.assertEquals ("This is to the left [//] ", blk1.toString());
        Assertions.assertEquals ("and this is the right.", blk2.toString());
    }

    @Test
    public void testClone() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."))
            .indent (3);
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.{indent=3}", blk.toString ());
        Assertions.assertEquals (3, blk.indent ());

        // Test clone.
        ContentBlock clone = blk.clone ();
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.{indent=3}", clone.toString ());
        Assertions.assertEquals (3, clone.indent ());
    }

    @Test
    public void testTransform_ParagraphToHeading() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BOLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.", blk.toString());

        // Transform as the same.
        Assertions.assertEquals ("This is to the left, [{21,18} bold]this is the middle[/] and this is the right.", blk.transform(BlockType.PARAGRAPH).toString ());

        // Transform as heading 1.
        Assertions.assertEquals ("This is to the left, this is the middle and this is the right.", blk.transform (BlockType.HEADING_1).toString ());

        // Transform as heading 2.
        Assertions.assertEquals ("This is to the left, this is the middle and this is the right.", blk.transform(BlockType.HEADING_2).toString ());

        // Transform as heading 3.
        Assertions.assertEquals ("This is to the left, this is the middle and this is the right.", blk.transform(BlockType.HEADING_3).toString ());
    }

    @Test
    public void testRemove_SingleLine() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is a line of text"));
        Assertions.assertEquals ("This is a line of text", blk.toString());

        // Remove from the middle.
        Assertions.assertEquals ("This is a  of text", blk.clone().remove(10, 4).toString ());

        // Remove from the left side.
        Assertions.assertEquals (" is a line of text", blk.clone().remove(0, 4).toString ());

        // Remove from the right side.
        Assertions.assertEquals ("This is a line of", blk.clone().remove(17, 5).toString ());

        // Remove all.
        Assertions.assertEquals ("", blk.clone().remove(0, 22).toString ());

        // Remove from the left side (negative start).
        Assertions.assertEquals ("s is a line of text", blk.clone().remove(-1, 4).toString ());
        Assertions.assertEquals ("his is a line of text", blk.clone().remove(-3, 4).toString ());
        Assertions.assertEquals ("This is a line of text", blk.clone().remove(-4, 4).toString ());
        Assertions.assertEquals ("This is a line of text", blk.clone().remove(-40, 4).toString ());

        // Remove from the right side (over extension).
        Assertions.assertEquals ("This is a line of", blk.clone().remove(17, 10).toString ());
        Assertions.assertEquals ("This is a line of te", blk.clone().remove(20, 10).toString ());
        Assertions.assertEquals ("This is a line of text", blk.clone().remove(22, 10).toString ());
        Assertions.assertEquals ("This is a line of text", blk.clone().remove(40, 10).toString ());
    }

    @Test
    public void testRemove_SingleLineWithFormatting() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is a ")
                .append ("line of", FormatType.BOLD)
                .append (" text"));
        Assertions.assertEquals ("This is a [{10,7} bold]line of[/] text", blk.toString());

        // Remove from the middle.
        Assertions.assertEquals ("This is a [{10,3} bold] of[/] text", blk.clone().remove(10, 4).toString ());

        // Remove from the left side.
        Assertions.assertEquals (" is a [{6,7} bold]line of[/] text", blk.clone().remove(0, 4).toString ());

        // Remove from the left bracket of format.
        Assertions.assertEquals ("This is [{8,5} bold]ne of[/] text", blk.clone().remove(8, 4).toString ());

        // Remove from the right side.
        Assertions.assertEquals ("This is a [{10,7} bold]line of[/]", blk.clone().remove(17, 5).toString ());

        // Remove from the right bracket of format.
        Assertions.assertEquals ("This is a [{10,4} bold]line[/]ext", blk.clone().remove(14, 5).toString ());

        // Remove all.
        Assertions.assertEquals ("", blk.clone().remove(0, 22).toString ());
    }

    @Test
    public void testRemove_TwoLines() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is a line"))
            .line (line -> line
                .append ("This is a another line"));
        Assertions.assertEquals ("This is a line[//]This is a another line", blk.toString());

        // Remove from the first line.
        Assertions.assertEquals ("This  a line[//]This is a another line", blk.clone().remove(5, 2).toString ());

        // Remove from the second line.
        Assertions.assertEquals ("This is a line[//]This  another line", blk.clone().remove(20, 4).toString ());

        // Remove across the lines.
        Assertions.assertEquals ("This is a [//]s is a another line", blk.clone().remove(10, 8).toString ());
    }

    @Test
    public void testInsert_SingleLine() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is a line"));
        Assertions.assertEquals ("This is a line", blk.toString());

        // Insert in the middle.
        Assertions.assertEquals ("This  hubba is a line", blk.clone().insert(5, " hubba ").toString ());

        // Insert at the start.
        Assertions.assertEquals (" hubba This is a line", blk.clone().insert(0, " hubba ").toString ());

        // Insert at the end.
        Assertions.assertEquals ("This is a line hubba ", blk.clone().insert(14, " hubba ").toString ());
    }

    @Test
    public void testInsert_SingleLineWithFormatting() {
        ContentBlock blk = new ContentBlock (BlockType.PARAGRAPH)
            .line (line -> line
                .append ("This is a ")
                .append ("line of", FormatType.BOLD)
                .append (" text"));
        Assertions.assertEquals ("This is a [{10,7} bold]line of[/] text", blk.toString());

        // Insert in the of formatting.
        Assertions.assertEquals ("This is a [{10,14} bold]li hubba ne of[/] text", blk.clone().insert(12, " hubba ").toString ());

        // At the left of formatting
        Assertions.assertEquals ("This is a  hubba [{17,7} bold]line of[/] text", blk.clone().insert(10, " hubba ").toString ());
    }
}
