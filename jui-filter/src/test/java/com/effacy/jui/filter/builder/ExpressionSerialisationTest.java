package com.effacy.jui.filter.builder;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.ExpressionSerialisationTest.TestFilter.Fields;
import com.effacy.jui.filter.builder.ExpressionSerialisationTest.TestFilter.Status;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;
import com.effacy.jui.filter.parser.FilterQueryParser.FilterQueryParserException;

/**
 * Test serialisation.
 */
public class ExpressionSerialisationTest {

    /**
     * Enums can be tricky as the parsed version will be represented as a literal
     * and so need mapping.
     */
    @Test
    public void values_enum() throws Exception {
        try {
            var exp = TestFilter.term(Fields.STATUS, Operator.IN, new Status[] { Status.STATUS1, Status.STATUS2 });
            exp = TestFilter.deserialise(TestFilter.serialise(exp));
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            var exp = TestFilter.term(Fields.STATUS, Operator.IN, Status.STATUS1);
            exp = TestFilter.deserialise(TestFilter.serialise(exp));
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        try {
            var exp = TestFilter.term(Fields.STATUS, Operator.EQ, Status.STATUS1);
            exp = TestFilter.deserialise(TestFilter.serialise(exp));
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
    }
    
    /**
     * An array value for IN should serialise correctly.
     */
    @Test
    public void values_in_array() throws Exception {
        var exp = TestFilter.term(Fields.VERSION, Operator.IN, new Long[] { 10L, 20L });
        Assertions.assertEquals("VERSION IN [10,20]", TestFilter.serialise(exp));
    }

    /**
     * A collection value for IN should serialise correctly (not double-wrapped).
     */
    @Test
    public void values_in_collection() throws Exception {
        var exp = TestFilter.term(Fields.VERSION, Operator.IN, List.of(10L, 20L));
        Assertions.assertEquals("VERSION IN [10,20]", TestFilter.serialise(exp));
    }

    /**
     * An array value for NOT_IN should serialise correctly.
     */
    @Test
    public void values_notIn_array() throws Exception {
        var exp = TestFilter.term(Fields.VERSION, Operator.NOT_IN, new Long[] { 10L });
        Assertions.assertEquals("VERSION NOT IN [10]", TestFilter.serialise(exp));
    }

    /**
     * A collection value for NOT_IN should serialise correctly (not double-wrapped).
     */
    @Test
    public void values_notIn_collection() throws Exception {
        var exp = TestFilter.term(Fields.VERSION, Operator.NOT_IN, List.of(10L));
        Assertions.assertEquals("VERSION NOT IN [10]", TestFilter.serialise(exp));
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
