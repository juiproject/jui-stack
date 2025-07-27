package com.effacy.jui.filter.builder;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.FieldTest.TestFilter.Fields;
import com.effacy.jui.filter.builder.FieldTest.TestFilter.Status;
import com.effacy.jui.filter.builder.FieldTest.TestFilter.StatusOther;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;
import com.effacy.jui.filter.parser.FilterQueryParser.FilterQueryParserException;

public class FieldTest {

    @Test
    public void values_string() {
        try {
            TestFilter.term(Fields.KEYWORDS, Operator.CONTAINS, "hubba").validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.KEYWORDS, Operator.CONTAINS, 22).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected a string: KEYWORDS CONTAINS 22", e.getMessage());
        }
        try {
            TestFilter.term(Fields.KEYWORDS, Operator.CONTAINS, 22.3).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected a string: KEYWORDS CONTAINS 22.3", e.getMessage());
        }
        try {
            TestFilter.term(Fields.KEYWORDS, Operator.CONTAINS, Status.STATUS2).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected a string: KEYWORDS CONTAINS STATUS2", e.getMessage());
        }
    }
    
    @Test
    public void values_integral() {
        try {
            TestFilter.term(Fields.VERSION, Operator.EQ, "hubba").validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected an integer: VERSION EQ hubba", e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.EQ, 22).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.EQ, 22.3).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected an integer: VERSION EQ 22.3", e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.EQ, Status.STATUS2).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected an integer: VERSION EQ STATUS2", e.getMessage());
        }
    }
    
    @Test
    public void values_decimal() {
        try {
            TestFilter.term(Fields.AMOUNT, Operator.EQ, "hubba").validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected an integer: AMOUNT EQ hubba", e.getMessage());
        }
        try {
            TestFilter.term(Fields.AMOUNT, Operator.EQ, 22).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.AMOUNT, Operator.EQ, 22.3).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.AMOUNT, Operator.IN, new Double [] { 22.4, 22.3 }).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.AMOUNT, Operator.EQ, Status.STATUS2).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected an integer: AMOUNT EQ STATUS2", e.getMessage());
        }
    }
    
    @Test
    public void values_enum() {
        try {
            TestFilter.term(Fields.STATUS, Operator.EQ, "hubba").validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected enum Status: STATUS EQ hubba", e.getMessage());
        }
        try {
            TestFilter.term(Fields.STATUS, Operator.EQ, 22).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected enum Status: STATUS EQ 22", e.getMessage());
        }
        try {
            TestFilter.term(Fields.STATUS, Operator.EQ, 22.3).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected enum Status: STATUS EQ 22.3", e.getMessage());
        }
        try {
            TestFilter.term(Fields.STATUS, Operator.EQ, StatusOther.STATUS2).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected enum Status: STATUS EQ STATUS2", e.getMessage());
        }
        try {
            TestFilter.term(Fields.STATUS, Operator.EQ, Status.STATUS2).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.STATUS, Operator.IN, Status.STATUS1).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.STATUS, Operator.IN, new Status[] { Status.STATUS1, Status.STATUS2 }).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
    }

    @Test
    public void test_operator() {
        try {
            TestFilter.term(Fields.VERSION, Operator.EQ, 2).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.NEQ, 2L).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.IN, 2).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.NOT_IN, 2).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.IN, new Long[] { 2L, 3L }).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.NOT_IN, new Integer[] { 3, 4 }).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }

        try {
            TestFilter.term(Fields.VERSION, Operator.LT, 2).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("invalid operator: VERSION LT 2", e.getMessage());
        }
        try {
            TestFilter.term(Fields.VERSION, Operator.CONTAINS, 2).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("invalid operator: VERSION CONTAINS 2", e.getMessage());
        }
    }

    /************************************************************************
     * Test filter
     ************************************************************************/

    public class TestFilter {

        public enum Status {
            STATUS1, STATUS2, STATUS3;
        }

        public enum StatusOther {
            STATUS1, STATUS2, STATUS3
        }

        public enum Fields implements Field {

            KEYWORDS(new StringType()),
            VERSION(new IntegralType(Operator.EQ, Operator.IN, Operator.NEQ, Operator.NOT_IN)),
            AMOUNT(new DecimalType()),
            STATUS(new EnumType<Status>(Status.class));

            private Type type;
            private Fields(Type type) { this.type = type; }
            public Type type() { return type; }
        }

        private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<>(Fields.class);

        public static String serialise(Expression<Fields> exp) throws ExpressionBuildException {
            return INSTANCE.serialise(exp);
        }

        public static Expression<Fields> deserialise(String str) throws FilterQueryParserException {
            return INSTANCE.deserialise(str);
        }

        // AND, OR and NOT production methods.

        @SafeVarargs
        public static Expression<Fields> and(Expression<Fields>... expressions) {
            return INSTANCE.and(List.of(expressions));
        }

        public static Expression<Fields> and(List<Expression<Fields>> expressions) {
            return INSTANCE.and(expressions);
        }

        @SafeVarargs
        public static Expression<Fields> or(Expression<Fields>... expressions) {
            return INSTANCE.or(List.of(expressions));
        }

        public static Expression<Fields> or(List<Expression<Fields>> expressions) {
            return INSTANCE.or(expressions);
        }

        public static Expression<Fields> not(Expression<Fields> expression) {
            return INSTANCE.not(expression);
        }

        // Fundamental (comparison) expressions.

        public static Expression<Fields> term(Fields field, Operator op, Object value) {
            return INSTANCE.term(field, op, value);
        }
    }
}
