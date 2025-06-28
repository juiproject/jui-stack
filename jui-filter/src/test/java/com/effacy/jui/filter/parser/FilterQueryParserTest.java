package com.effacy.jui.filter.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.StringExpressionBuilder;
import com.effacy.jui.filter.parser.FilterQueryParser.ParsedExpression;

public class FilterQueryParserTest {

    @Test
    public void parse001() {
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
    public void parse002() {
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
            assertEquals("(((name = \"John\" AND age > 25) AND ((status IN [\"ACTIVE\",\"PENDING\"] OR priority = \"high\") OR (NOT (category = \"test\" AND deleted = true)))) AND (price >= 100.5 OR name STARTS WITH \"Product\"))", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }
    
    @Test
    public void parse003() {
        try {
            ParsedExpression exp = FilterQueryParser.parse("""
                a IN [1, 2, 3]
            """);
            assertNotNull(exp);
            exp.print();
            String out = exp.build(new StringExpressionBuilder());
            assertEquals("a IN [1,2,3]", out);
        } catch (Exception e) {
            fail("Parsing threw an exception: " + e.getMessage());
        }
    }
}
