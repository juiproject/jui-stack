package com.effacy.jui.filter.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.StringExpressionBuilder;
import com.effacy.jui.filter.parser.FilterQueryParser.ParsedExpression;

public class FilterQueryParserTest {


    /************************************************************************
     * Form and content tests.
     ************************************************************************/

    /**
     * Tests for case sensitivity.
     */
    @Test
    public void charcase() {
        assertParse("""
            status = "active" AND score >= 75
        """, "(status = \"active\" AND score >= 75)");
        assertParse("""
            status = "active" and score >= 75
        """, "(status = \"active\" AND score >= 75)");
        assertParse("""
            status = "active" OR score >= 75
        """, "(status = \"active\" OR score >= 75)");
        assertParse("""
            status = "active" or score >= 75
        """, "(status = \"active\" OR score >= 75)");
    }

    /**
     * Tests for operator alternatives.
     */
    @Test
    public void charset() {
        assertParse("""
            status = "active"
        """, "status = \"active\"");

        assertParse("""
            status = "Maori"
        """, "status = \"Maori\"");

        assertParse("""
            status = "Māori"
        """, "status = \"Māori\"");

        assertParse("""
            status = "baˈbo͞on"
        """, "status = \"baˈbo͞on\"");
    }

    /**
     * Tests for operator alternatives.
     */
    @Test
    public void alternatives() {
        assertParse("""
            status = "active"
        """, "status = \"active\"");
        assertParse("""
            status IS "active"
        """, "status = \"active\"");
        assertParse("""
            status is "active"
        """, "status = \"active\"");
        assertParse("""
            status == "active"
        """, "status = \"active\"");

        assertParse("""
            NOT status = "active"
        """, "(NOT status = \"active\")");
        assertParse("""
            ! status = "active"
        """, "(NOT status = \"active\")");
        assertParse("""
            ~ status = "active"
        """, "(NOT status = \"active\")");

        assertParse("""
            status != "active"
        """, "status != \"active\"");
        assertParse("""
            status ~= "active"
        """, "status != \"active\"");
        assertParse("""
            status <> "active"
        """, "status != \"active\"");
    }

    /**
     * Tests for operator alternatives.
     */
    @Test
    public void parenthesis() {
        assertParse("""
            (NOT status = "active")
        """, "(NOT status = \"active\")");
        assertParse("""
            ((NOT status = "active"))
        """, "(NOT status = \"active\")");
        assertParse("""
            (NOT (status = "active"))
        """, "(NOT status = \"active\")");

        assertParse("""
            a = 2 OR b = 3
        """, "(a = 2 OR b = 3)");
        assertParse("""
            (a = 2) OR (b = 3)
        """, "(a = 2 OR b = 3)");

        assertParse("""
            a == 1 OR b == 2 AND c == 3
        """, "(a = 1 OR (b = 2 AND c = 3))");
        assertParse("""
            (a == 1 OR b == 2) AND c == 3
        """, "((a = 1 OR b = 2) AND c = 3)");
    }

    /**
     * Tests for operator alternatives.
     */
    @Test
    public void literals() {
        assertParse("""
            (NOT status = ACTIVE)
        """, "(NOT status = ACTIVE)");
    }

    /************************************************************************
     * Operator tests.
     ************************************************************************/
    
    /**
     * Tests the IN for value type.
     */
    @Test
    public void IN_value() {
        try {
            FilterQueryParser.parse("""
                a IN "str"
            """);
            fail("Should not have pased by did");
        } catch (Exception e) {
            // Expected path.
        }
        try {
            FilterQueryParser.parse("""
                a IN 2.7
            """);
            fail("Should not have pased by did");
        } catch (Exception e) {
            // Expected path.
        }
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a IN [1, 2, 3]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a IN [1,2,3]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a IN [2.3, 2.2, 3.87]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a IN [2.3,2.2,3.87]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a IN [ACTIVE, INACTIVE]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a IN [ACTIVE,INACTIVE]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }
    
    /**
     * Tests the NOT_IN for value type.
     */
    @Test
    public void NOT_IN_value() {
        try {
            FilterQueryParser.parse("""
                a NOT IN "str"
            """);
            fail("Should not have pased by did");
        } catch (Exception e) {
            // Expected path.
        }
        try {
            FilterQueryParser.parse("""
                a NOT IN 2.7
            """);
            fail("Should not have pased by did");
        } catch (Exception e) {
            // Expected path.
        }
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a NOT IN [1, 2, 3]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a NOT IN [1,2,3]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a NOT IN [2.3, 2.2, 3.87]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a NOT IN [2.3,2.2,3.87]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a NOT IN [ACTIVE, INACTIVE]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a NOT IN [ACTIVE,INACTIVE]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }

    /**
     * Tests for NULL value parsing.
     */
    @Test
    public void nullValues() {
        // Test NULL equality
        assertParse("""
            field = NULL
        """, "field = null");
        
        // Test lowercase null
        assertParse("""
            field = null
        """, "field = null");
        
        // Test NULL inequality
        assertParse("""
            field != NULL
        """, "field != null");
        
        // Test NULL in list
        assertParse("""
            field IN [1, NULL, 3]
        """, "field IN [1,null,3]");
        
        // Test NOT NULL
        assertParse("""
            NOT field = NULL
        """, "(NOT field = null)");
        
        // Test NULL with boolean operators
        assertParse("""
            field = NULL AND other = "value"
        """, "(field = null AND other = \"value\")");
        
        assertParse("""
            field = NULL OR other = "value"
        """, "(field = null OR other = \"value\")");
    }

    /************************************************************************
     * General tests.
     ************************************************************************/

    @Test
    public void general_01() {
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                status = "active" AND score >= 75
            """);
            assertNotNull(exp);
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("(status = \"active\" AND score >= 75)", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void general_02() {
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                name = "John"
                AND age > 25
                AND (
                    status IN [ACTIVE, PENDING]
                    OR priority = "high"
                    OR NOT (category = "test" AND deleted = true)
                )
                AND (
                    price >= 100.50
                    OR name STARTS WITH "Product"
                )
            """);
            assertNotNull(exp);
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("(((name = \"John\" AND age > 25) AND ((status IN [ACTIVE,PENDING] OR priority = \"high\") OR (NOT (category = \"test\" AND deleted = true)))) AND (price >= 100.5 OR name STARTS WITH \"Product\"))", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }
    
    @Test
    public void general_03() {
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a IN [1, 2, 3]
            """);
            assertNotNull(exp);
            // exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a IN [1,2,3]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }

    /************************************************************************
     * Round-trip tests.
     ************************************************************************/

    /**
     * Tests that the string generated builder (see {@link StringExpressionBuilder})
     * generates output that when parsed and re-generated is the same.
     */
    @Test
    public void roundtrip() throws Exception {
            assertRoundTrip("""
                a IN [1, 2, 3]
            """);
            assertRoundTrip("""
                name = "John"
                AND age > 25
                AND (
                    status IN [ACTIVE, PENDING]
                    OR priority = "high"
                    OR NOT (category = "test" AND deleted = true)
                )
                AND (
                    price >= 100.50
                    OR name STARTS WITH "Product"
                )
            """);
    }

    protected void assertRoundTrip(String input) {
        try {
            String str = FilterQueryParser.parse(input).build(new StringExpressionBuilder());
            String out = FilterQueryParser.parse(str).build(new StringExpressionBuilder());
            assertEquals(str, out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }

    /************************************************************************
     * Support methods.
     ************************************************************************/

    protected void assertParse(String input, String output) {
        try {
            ParsedExpression exp = FilterQueryParser.parse(input);
            assertNotNull(exp);
            String out = exp.build(new StringExpressionBuilder());
            // System.out.println(output);
            // System.out.println(out);
            assertEquals(output, out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }
}
