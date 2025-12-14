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

    /************************************************************************
     * Variable tests.
     ************************************************************************/

    @Test
    public void testVariable_ZeroLengthFormat() {
        // Create a line with a variable marker (zero-length format with variable metadata).
        FormattedLine line = new FormattedLine();
        line.append("Hello ");

        // Add a zero-length format with variable metadata at position 6.
        FormattedLine.Format varFormat = new FormattedLine.Format(6, 0, FormatType.BLD);
        varFormat.getMeta().put(FormattedLine.META_VARIABLE, "userName");
        line.getFormatting().add(varFormat);

        line.setText(line.getText()); // Text remains "Hello "

        List<TextSegment> segments = line.sequence();

        // Should have 2 segments: "Hello " and the variable.
        Assertions.assertEquals(2, segments.size());
        Assertions.assertEquals("Hello ", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
        Assertions.assertEquals("userName", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
    }

    @Test
    public void testVariable_WithSurroundingText() {
        // Create a line: "Dear [variable], welcome!"
        FormattedLine line = new FormattedLine();
        line.setText("Dear , welcome!");

        // Add a zero-length format at position 5 (after "Dear ").
        FormattedLine.Format varFormat = new FormattedLine.Format(5, 0);
        varFormat.getMeta().put(FormattedLine.META_VARIABLE, "recipientName");
        line.getFormatting().add(varFormat);

        List<TextSegment> segments = line.sequence();

        // Should have 3 segments: "Dear ", variable, ", welcome!"
        Assertions.assertEquals(3, segments.size());
        Assertions.assertEquals("Dear ", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
        Assertions.assertEquals("recipientName", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals(", welcome!", segments.get(2).text());
        Assertions.assertFalse(segments.get(2).variable());
    }

    @Test
    public void testVariable_MultipleVariables() {
        // Create a line with multiple variables.
        FormattedLine line = new FormattedLine();
        line.setText("Hello  and !");

        // Variable at position 6 (after "Hello ").
        FormattedLine.Format var1 = new FormattedLine.Format(6, 0);
        var1.getMeta().put(FormattedLine.META_VARIABLE, "firstName");
        line.getFormatting().add(var1);

        // Variable at position 11 (after "Hello  and ").
        FormattedLine.Format var2 = new FormattedLine.Format(11, 0);
        var2.getMeta().put(FormattedLine.META_VARIABLE, "lastName");
        line.getFormatting().add(var2);

        List<TextSegment> segments = line.sequence();

        // Should have 5 segments: "Hello ", var1, " and ", var2, "!"
        Assertions.assertEquals(5, segments.size());
        Assertions.assertEquals("Hello ", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
        Assertions.assertEquals("firstName", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals(" and ", segments.get(2).text());
        Assertions.assertFalse(segments.get(2).variable());
        Assertions.assertEquals("lastName", segments.get(3).text());
        Assertions.assertTrue(segments.get(3).variable());
        Assertions.assertEquals("!", segments.get(4).text());
        Assertions.assertFalse(segments.get(4).variable());
    }

    @Test
    public void testVariable_EmptyVariableName_Ignored() {
        // Empty variable name should be treated as regular format, not a variable.
        FormattedLine line = new FormattedLine();
        line.append("Hello ");
        line.append("world", FormatType.BLD);

        // Add empty variable metadata to the bold format.
        line.getFormatting().get(0).getMeta().put(FormattedLine.META_VARIABLE, "");

        List<TextSegment> segments = line.sequence();

        // Empty variable name should be ignored, so "world" is not a variable.
        Assertions.assertEquals(2, segments.size());
        Assertions.assertEquals("Hello ", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
        Assertions.assertEquals("world", segments.get(1).text());
        Assertions.assertFalse(segments.get(1).variable());
    }

    @Test
    public void testVariable_WithFormatting() {
        // Variable can have formatting applied.
        FormattedLine line = new FormattedLine();
        line.setText("Value: ");

        // Add a variable with bold formatting.
        FormattedLine.Format varFormat = new FormattedLine.Format(7, 0, FormatType.BLD);
        varFormat.getMeta().put(FormattedLine.META_VARIABLE, "amount");
        line.getFormatting().add(varFormat);

        List<TextSegment> segments = line.sequence();

        Assertions.assertEquals(2, segments.size());
        Assertions.assertEquals("Value: ", segments.get(0).text());
        Assertions.assertEquals("amount", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals(1, segments.get(1).formatting().length);
        Assertions.assertEquals(FormatType.BLD, segments.get(1).formatting()[0]);
    }

    @Test
    public void testVariable_WithLink() {
        // Variable can also have a link.
        FormattedLine line = new FormattedLine();
        line.setText("Click ");

        // Add a variable with link metadata.
        FormattedLine.Format varFormat = new FormattedLine.Format(6, 0, FormatType.A);
        varFormat.getMeta().put(FormattedLine.META_VARIABLE, "linkText");
        varFormat.getMeta().put(FormattedLine.META_LINK, "https://example.com");
        line.getFormatting().add(varFormat);

        List<TextSegment> segments = line.sequence();

        Assertions.assertEquals(2, segments.size());
        Assertions.assertEquals("linkText", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals("https://example.com", segments.get(1).link());
    }

    @Test
    public void testTextSegment_VariableAccessor() {
        // Test the TextSegment variable() accessor.
        TextSegment normalSegment = new TextSegment("text", null, null);
        Assertions.assertFalse(normalSegment.variable());

        TextSegment variableSegment = new TextSegment("varName", null, null, true);
        Assertions.assertTrue(variableSegment.variable());
    }

    @Test
    public void testTextSegment_MetaCopied() {
        // Test that metadata is copied to TextSegment (excluding link and variable).
        FormattedLine line = new FormattedLine();
        line.setText("Hello world");

        // Add a format with various metadata (but not variable, so it's a normal segment).
        FormattedLine.Format format = new FormattedLine.Format(0, 5, FormatType.BLD);
        format.getMeta().put(FormattedLine.META_LINK, "https://example.com");
        format.getMeta().put("customKey", "customValue");
        format.getMeta().put("anotherKey", "anotherValue");
        line.getFormatting().add(format);

        List<TextSegment> segments = line.sequence();

        Assertions.assertEquals(2, segments.size());

        // First segment should have custom metadata but not link.
        TextSegment seg = segments.get(0);
        Assertions.assertEquals("Hello", seg.text());
        Assertions.assertTrue(seg.hasMeta());
        Assertions.assertEquals("customValue", seg.meta().get("customKey"));
        Assertions.assertEquals("anotherValue", seg.meta().get("anotherKey"));
        Assertions.assertNull(seg.meta().get(FormattedLine.META_LINK));

        // Link should be available via the link() accessor, not meta.
        Assertions.assertEquals("https://example.com", seg.link());
    }

    @Test
    public void testTextSegment_VariableMetaCopied() {
        // Test that metadata is copied to variable TextSegments (excluding link and variable).
        FormattedLine line = new FormattedLine();
        line.setText("Hello ");

        // Add a variable format with custom metadata.
        FormattedLine.Format format = new FormattedLine.Format(6, 0);
        format.getMeta().put(FormattedLine.META_VARIABLE, "userName");
        format.getMeta().put("customKey", "customValue");
        line.getFormatting().add(format);

        List<TextSegment> segments = line.sequence();

        Assertions.assertEquals(2, segments.size());

        // Variable segment should have custom metadata but not variable key.
        TextSegment varSeg = segments.get(1);
        Assertions.assertEquals("userName", varSeg.text());
        Assertions.assertTrue(varSeg.variable());
        Assertions.assertTrue(varSeg.hasMeta());
        Assertions.assertEquals("customValue", varSeg.meta().get("customKey"));
        Assertions.assertNull(varSeg.meta().get(FormattedLine.META_VARIABLE));
    }

    @Test
    public void testTextSegment_NoMetaWhenEmpty() {
        // Test that hasMeta() returns false when no custom metadata exists.
        FormattedLine line = new FormattedLine();
        line.setText("Hello world");

        // Add a format with only link (no custom metadata).
        FormattedLine.Format format = new FormattedLine.Format(0, 5, FormatType.A);
        format.getMeta().put(FormattedLine.META_LINK, "https://example.com");
        line.getFormatting().add(format);

        List<TextSegment> segments = line.sequence();

        // First segment should not have custom metadata.
        TextSegment seg = segments.get(0);
        Assertions.assertFalse(seg.hasMeta());
        Assertions.assertEquals("https://example.com", seg.link());
    }

    /************************************************************************
     * Variable method tests.
     ************************************************************************/

    @Test
    public void testVariable_Method_Basic() {
        // Test the variable() method creates a proper variable format.
        FormattedLine line = new FormattedLine();
        line.append("Hello ");
        line.variable("userName");
        line.append("!");

        List<TextSegment> segments = line.sequence();

        // Should have 3 segments: "Hello ", variable, "!"
        Assertions.assertEquals(3, segments.size());
        Assertions.assertEquals("Hello ", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
        Assertions.assertEquals("userName", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals("!", segments.get(2).text());
        Assertions.assertFalse(segments.get(2).variable());
    }

    @Test
    public void testVariable_Method_WithFormatting() {
        // Test the variable() method with formatting applied.
        FormattedLine line = new FormattedLine();
        line.append("Value: ");
        line.variable("amount", FormatType.BLD, FormatType.ITL);

        List<TextSegment> segments = line.sequence();

        Assertions.assertEquals(2, segments.size());
        Assertions.assertEquals("Value: ", segments.get(0).text());
        Assertions.assertEquals("amount", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals(2, segments.get(1).formatting().length);
    }

    @Test
    public void testVariable_Method_Chaining() {
        // Test that variable() returns this for chaining.
        FormattedLine line = new FormattedLine()
            .append("Dear ")
            .variable("name")
            .append(", your balance is ")
            .variable("balance", FormatType.BLD)
            .append(".");

        List<TextSegment> segments = line.sequence();

        // Should have 5 segments: "Dear ", name var, ", your balance is ", balance var, "."
        Assertions.assertEquals(5, segments.size());
        Assertions.assertEquals("Dear ", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
        Assertions.assertEquals("name", segments.get(1).text());
        Assertions.assertTrue(segments.get(1).variable());
        Assertions.assertEquals(", your balance is ", segments.get(2).text());
        Assertions.assertFalse(segments.get(2).variable());
        Assertions.assertEquals("balance", segments.get(3).text());
        Assertions.assertTrue(segments.get(3).variable());
        Assertions.assertEquals(1, segments.get(3).formatting().length);
        Assertions.assertEquals(".", segments.get(4).text());
        Assertions.assertFalse(segments.get(4).variable());
    }

    @Test
    public void testVariable_Method_NullOrEmpty_Ignored() {
        // Test that null or empty variable names are ignored.
        FormattedLine line = new FormattedLine();
        line.append("Hello");
        line.variable(null);
        line.variable("");
        line.append(" world");

        List<TextSegment> segments = line.sequence();

        // Should have 1 segment since no formatting was added.
        Assertions.assertEquals(1, segments.size());
        Assertions.assertEquals("Hello world", segments.get(0).text());
        Assertions.assertFalse(segments.get(0).variable());
    }

    /************************************************************************
     * Link method tests.
     ************************************************************************/

    @Test
    public void testLink_Method_Basic() {
        // Test the link() method creates a proper link format.
        FormattedLine line = new FormattedLine();
        line.append("Click ");
        line.link("here", "https://example.com");
        line.append(" for more.");

        List<TextSegment> segments = line.sequence();

        // Should have 3 segments: "Click ", link, " for more."
        Assertions.assertEquals(3, segments.size());
        Assertions.assertEquals("Click ", segments.get(0).text());
        Assertions.assertNull(segments.get(0).link());
        Assertions.assertEquals("here", segments.get(1).text());
        Assertions.assertEquals("https://example.com", segments.get(1).link());
        Assertions.assertTrue(segments.get(1).contains(FormatType.A));
        Assertions.assertEquals(" for more.", segments.get(2).text());
        Assertions.assertNull(segments.get(2).link());
    }

    @Test
    public void testLink_Method_WithAdditionalFormatting() {
        // Test the link() method with additional formatting.
        FormattedLine line = new FormattedLine();
        line.append("Visit ");
        line.link("our website", "https://example.com", FormatType.BLD, FormatType.ITL);

        List<TextSegment> segments = line.sequence();

        Assertions.assertEquals(2, segments.size());
        Assertions.assertEquals("Visit ", segments.get(0).text());
        Assertions.assertEquals("our website", segments.get(1).text());
        Assertions.assertEquals("https://example.com", segments.get(1).link());
        // Should have A, BLD, and ITL formats.
        Assertions.assertTrue(segments.get(1).contains(FormatType.A));
        Assertions.assertTrue(segments.get(1).contains(FormatType.BLD));
        Assertions.assertTrue(segments.get(1).contains(FormatType.ITL));
    }

    @Test
    public void testLink_Method_Chaining() {
        // Test that link() returns this for chaining.
        FormattedLine line = new FormattedLine()
            .append("Go to ")
            .link("Google", "https://google.com")
            .append(" or ")
            .link("Bing", "https://bing.com", FormatType.BLD)
            .append(".");

        List<TextSegment> segments = line.sequence();

        // Should have 5 segments.
        Assertions.assertEquals(5, segments.size());
        Assertions.assertEquals("Go to ", segments.get(0).text());
        Assertions.assertEquals("Google", segments.get(1).text());
        Assertions.assertEquals("https://google.com", segments.get(1).link());
        Assertions.assertEquals(" or ", segments.get(2).text());
        Assertions.assertEquals("Bing", segments.get(3).text());
        Assertions.assertEquals("https://bing.com", segments.get(3).link());
        Assertions.assertTrue(segments.get(3).contains(FormatType.BLD));
        Assertions.assertEquals(".", segments.get(4).text());
    }

    @Test
    public void testLink_Method_NullOrEmptyText_Ignored() {
        // Test that null or empty text is ignored.
        FormattedLine line = new FormattedLine();
        line.append("Hello");
        line.link(null, "https://example.com");
        line.link("", "https://example.com");
        line.append(" world");

        List<TextSegment> segments = line.sequence();

        // Should have 1 segment since link text was empty.
        Assertions.assertEquals(1, segments.size());
        Assertions.assertEquals("Hello world", segments.get(0).text());
    }

    @Test
    public void testLink_Method_NullOrEmptyLink_AppendsText() {
        // Test that null or empty link appends text without link formatting.
        FormattedLine line = new FormattedLine();
        line.append("Click ");
        line.link("here", null, FormatType.BLD);
        line.append(" or ");
        line.link("there", "", FormatType.ITL);

        List<TextSegment> segments = line.sequence();

        // "here" and "there" should be appended with their formats but no link.
        Assertions.assertEquals(4, segments.size());
        Assertions.assertEquals("Click ", segments.get(0).text());
        Assertions.assertEquals("here", segments.get(1).text());
        Assertions.assertNull(segments.get(1).link());
        Assertions.assertTrue(segments.get(1).contains(FormatType.BLD));
        Assertions.assertFalse(segments.get(1).contains(FormatType.A));
        Assertions.assertEquals(" or ", segments.get(2).text());
        Assertions.assertEquals("there", segments.get(3).text());
        Assertions.assertNull(segments.get(3).link());
        Assertions.assertTrue(segments.get(3).contains(FormatType.ITL));
        Assertions.assertFalse(segments.get(3).contains(FormatType.A));
    }
}
