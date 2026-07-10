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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;

public class FormattedBlockTest {

    @Test
    public void testLength_v1() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("AAA, ")
                .append ("BBB", FormatType.BLD)
                .append (" and CCC."));
        
        Assertions.assertEquals (17, blk1.length ());
    }

    @Test
    public void testLength_v2() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line.append ("AAA "))
            .line (line -> line.append (" and BBB."));
        
        Assertions.assertEquals (14, blk1.length ());
    }
    
    @Test
    public void testSplit_LeftOfFormatting() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."))
            .indent (2);
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.{indent=2}", blk1.toString());
        Assertions.assertEquals (2, blk1.getIndent ());
    
        FormattedBlock blk2 = blk1.split (10);

        Assertions.assertEquals ("This is to{indent=2}", blk1.toString());
        Assertions.assertEquals (2, blk1.getIndent ());
        Assertions.assertEquals (" the left, [{11,18} bld]this is the middle[/] and this is the right.{indent=2}", blk2.toString());
        Assertions.assertEquals (2, blk2.getIndent ());
    }

    @Test
    public void testSplit_RightOfFormatting() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (44);

        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and ", blk1.toString());
        Assertions.assertEquals ("this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_MiddleOfFormatting() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (26);

        Assertions.assertEquals ("This is to the left, [{21,5} bld]this [/]", blk1.toString());
        Assertions.assertEquals ("[{0,13} bld]is the middle[/] and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_LeftEdgeOfFormatting() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (21);

        Assertions.assertEquals ("This is to the left, ", blk1.toString());
        Assertions.assertEquals ("[{0,18} bld]this is the middle[/] and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_RightEdgeOfFormatting() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (39);

        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/]", blk1.toString());
        Assertions.assertEquals (" and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_AtLeftOfLineBreak() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (20);

        Assertions.assertEquals ("This is to the left ", blk1.toString());
        Assertions.assertEquals (" and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_AtRightOfLineBreak() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (21);

        Assertions.assertEquals ("This is to the left ", blk1.toString());
        Assertions.assertEquals (" and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_BeforeLineBreak() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (19);

        Assertions.assertEquals ("This is to the left", blk1.toString());
        Assertions.assertEquals (" [//] and this is the right.", blk2.toString());
    }

    @Test
    public void testSplit_AfterLineBreak() {
        FormattedBlock blk1 = new FormattedBlock (BlockType.PARA)
            .line (line -> line.append ("This is to the left "))
            .line (line -> line.append (" and this is the right."));
        Assertions.assertEquals ("This is to the left [//] and this is the right.", blk1.toString());
    
        FormattedBlock blk2 = blk1.split (22);

        Assertions.assertEquals ("This is to the left [//] ", blk1.toString());
        Assertions.assertEquals ("and this is the right.", blk2.toString());
    }

    @Test
    public void testClone() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."))
            .indent (3);
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.{indent=3}", blk.toString ());
        Assertions.assertEquals (3, blk.getIndent ());

        // Test clone.
        FormattedBlock clone = blk.clone ();
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.{indent=3}", clone.toString ());
        Assertions.assertEquals (3, clone.getIndent ());
    }

    @Test
    public void testTransform_ParagraphToHeading() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is to the left, ")
                .append ("this is the middle", FormatType.BLD)
                .append (" and this is the right."));
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.", blk.toString());

        // Transform as the same.
        Assertions.assertEquals ("This is to the left, [{21,18} bld]this is the middle[/] and this is the right.", blk.transform(BlockType.PARA).toString ());

        // Transform as heading 1.
        Assertions.assertEquals ("This is to the left, this is the middle and this is the right.", blk.transform (BlockType.H1).toString ());

        // Transform as heading 2.
        Assertions.assertEquals ("This is to the left, this is the middle and this is the right.", blk.transform(BlockType.H2).toString ());

        // Transform as heading 3.
        Assertions.assertEquals ("This is to the left, this is the middle and this is the right.", blk.transform(BlockType.H3).toString ());
    }

    @Test
    public void testRemove_SingleLine() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
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
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is a ")
                .append ("line of", FormatType.BLD)
                .append (" text"));
        Assertions.assertEquals ("This is a [{10,7} bld]line of[/] text", blk.toString());

        // Remove from the middle.
        Assertions.assertEquals ("This is a [{10,3} bld] of[/] text", blk.clone().remove(10, 4).toString ());

        // Remove from the left side.
        Assertions.assertEquals (" is a [{6,7} bld]line of[/] text", blk.clone().remove(0, 4).toString ());

        // Remove from the left bracket of format.
        Assertions.assertEquals ("This is [{8,5} bld]ne of[/] text", blk.clone().remove(8, 4).toString ());

        // Remove from the right side.
        Assertions.assertEquals ("This is a [{10,7} bld]line of[/]", blk.clone().remove(17, 5).toString ());

        // Remove from the right bracket of format.
        Assertions.assertEquals ("This is a [{10,4} bld]line[/]ext", blk.clone().remove(14, 5).toString ());

        // Remove all.
        Assertions.assertEquals ("", blk.clone().remove(0, 22).toString ());
    }

    @Test
    public void testRemove_TwoLines() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
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
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
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
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> line
                .append ("This is a ")
                .append ("line of", FormatType.BLD)
                .append (" text"));
        Assertions.assertEquals ("This is a [{10,7} bld]line of[/] text", blk.toString());

        // Insert in the of formatting.
        Assertions.assertEquals ("This is a [{10,14} bld]li hubba ne of[/] text", blk.clone().insert(12, " hubba ").toString ());

        // At the left of formatting
        Assertions.assertEquals ("This is a  hubba [{17,7} bld]line of[/] text", blk.clone().insert(10, " hubba ").toString ());
    }

    /**
     * Block offsets count the line break between lines as one character, so an
     * insert targeting the end of a middle line must not drift into a later line.
     */
    @Test
    public void testInsert_multiLineOffsets() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> line.append ("aa"))
            .line (line -> line.append ("bb"))
            .line (line -> line.append ("cc"));

        // Offsets: "aa" [0,2), break 2, "bb" [3,5), break 5, "cc" [6,8).
        FormattedBlock endOfBb = blk.clone ();
        endOfBb.insert (5, "k");
        Assertions.assertEquals ("bbk", endOfBb.getLines ().get (1).getText ());
        Assertions.assertEquals ("cc", endOfBb.getLines ().get (2).getText ());

        FormattedBlock startOfBb = blk.clone ();
        startOfBb.insert (3, "k");
        Assertions.assertEquals ("kbb", startOfBb.getLines ().get (1).getText ());

        FormattedBlock midCc = blk.clone ();
        midCc.insert (7, "k");
        Assertions.assertEquals ("ckc", midCc.getLines ().get (2).getText ());
    }

    /**
     * Splitting a line at the boundary between a character and a following image
     * (cursor at the end of the char) moves the image to the balance block; the
     * character stays.
     */
    @Test
    public void testSplit_imageAfterCharMovesToBalance() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> {
                line.append ("k");
                line.getFormatting ().add (new FormattedLine.Format (1, 0, FormatType.IMG));
            });

        FormattedBlock right = blk.split (1);

        Assertions.assertEquals (1, blk.getLines ().size ());
        Assertions.assertEquals ("k", blk.getLines ().get (0).getText ());
        Assertions.assertEquals (0, blk.getLines ().get (0).getFormatting ().size ());
        Assertions.assertEquals (1, right.getLines ().size ());
        Assertions.assertEquals ("", right.getLines ().get (0).getText ());
        Assertions.assertEquals (1, right.getLines ().get (0).getFormatting ().size ());
        Assertions.assertTrue (right.getLines ().get (0).getFormatting ().get (0).getFormats ().contains (FormatType.IMG));
    }

    /**
     * Deleting a line's last character must keep the line if it still holds a
     * zero-length image (the line is textless, not empty).
     */
    @Test
    public void testRemove_keepsImageOnlyLine() {
        FormattedBlock blk = new FormattedBlock (BlockType.PARA)
            .line (line -> {
                line.append ("s");
                line.getFormatting ().add (new FormattedLine.Format (0, 0, FormatType.IMG));
            });

        blk.remove (0, 1);

        Assertions.assertEquals (1, blk.getLines ().size ());
        Assertions.assertEquals ("", blk.getLines ().get (0).getText ());
        Assertions.assertEquals (1, blk.getLines ().get (0).getFormatting ().size ());
        Assertions.assertTrue (blk.getLines ().get (0).getFormatting ().get (0).getFormats ().contains (FormatType.IMG));
    }
}
