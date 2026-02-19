package com.effacy.jui.filter.builder;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.Field.DateTimeType;
import com.effacy.jui.filter.builder.Field.DateType;
import com.effacy.jui.filter.builder.Field.DynamicEnumType;
import com.effacy.jui.filter.builder.FieldTest.TestFilter.Fields;
import com.effacy.jui.filter.builder.FieldTest.TestFilter.Status;
import com.effacy.jui.filter.builder.FieldTest.TestFilter.StatusOther;
import com.effacy.jui.filter.builder.IExpressionBuilder.Literal;
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
    public void values_dynamic_enum() {
        // Valid value (exact case).
        try {
            TestFilter.term(Fields.DOC_TYPE, Operator.EQ, "resolution").validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        // Valid value (case-insensitive).
        try {
            TestFilter.term(Fields.DOC_TYPE, Operator.EQ, "RESOLUTION").validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        // Invalid value.
        try {
            TestFilter.term(Fields.DOC_TYPE, Operator.EQ, "bogus").validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("'bogus' is not a valid value for DocumentType: DOC_TYPE EQ bogus", e.getMessage());
        }
        // Non-string value rejected.
        try {
            TestFilter.term(Fields.DOC_TYPE, Operator.EQ, 22).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected enum DocumentType: DOC_TYPE EQ 22", e.getMessage());
        }
        // IN with valid values.
        try {
            TestFilter.term(Fields.DOC_TYPE, Operator.IN, new String[] { "resolution", "deed" }).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        // IN with an invalid value in the array.
        try {
            TestFilter.term(Fields.DOC_TYPE, Operator.IN, new String[] { "resolution", "bogus" }).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertTrue(e.getMessage().startsWith("'bogus' is not a valid value for DocumentType"));
        }
    }

    @Test
    public void values_dynamic_enum_transform() throws ExpressionBuildException {
        DynamicEnumType type = new DynamicEnumType("DocumentType", List.of("resolution", "deed", "other"));

        // Case-insensitive resolution returns canonical form.
        Assertions.assertEquals("resolution", type.transform("RESOLUTION"));
        Assertions.assertEquals("deed", type.transform("Deed"));
        Assertions.assertEquals("other", type.transform("other"));

        // Literal resolution.
        Assertions.assertEquals("resolution", type.transform(new Literal("Resolution")));

        // Null passes through.
        Assertions.assertNull(type.transform(null));

        // Array resolution.
        Object result = type.transform(new String[] { "RESOLUTION", "deed" });
        Assertions.assertInstanceOf(String[].class, result);
        String[] arr = (String[]) result;
        Assertions.assertEquals("resolution", arr[0]);
        Assertions.assertEquals("deed", arr[1]);

        // Literal array resolution.
        result = type.transform(new Object[] { new Literal("Resolution"), new Literal("DEED") });
        Assertions.assertInstanceOf(String[].class, result);
        arr = (String[]) result;
        Assertions.assertEquals("resolution", arr[0]);
        Assertions.assertEquals("deed", arr[1]);
    }

    @Test
    public void values_dynamic_enum_serialisation() throws Exception {
        // Round-trip through serialisation and deserialisation.
        var exp = TestFilter.term(Fields.DOC_TYPE, Operator.EQ, "resolution");
        String serialised = TestFilter.serialise(exp);
        var restored = TestFilter.deserialise(serialised);
        restored.validate();
    }

    @Test
    public void values_date() {
        // Valid ISO date.
        try {
            TestFilter.term(Fields.BIRTH_DATE, Operator.EQ, "2020-01-01").validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        // Non-padded date rejected by ISO parser.
        try {
            TestFilter.term(Fields.BIRTH_DATE, Operator.EQ, "2025-2-1").validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("invalid date: 2025-2-1: BIRTH_DATE EQ 2025-2-1", e.getMessage());
        }
        // Non-string rejected.
        try {
            TestFilter.term(Fields.BIRTH_DATE, Operator.EQ, 20200101).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected a date: BIRTH_DATE EQ 20200101", e.getMessage());
        }
        // Null accepted.
        try {
            TestFilter.term(Fields.BIRTH_DATE, Operator.EQ, null).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void values_date_transform() throws ExpressionBuildException {
        DateType type = new DateType();

        // ISO string to Date.
        Assertions.assertEquals(new Date(120, 0, 1), type.transform("2020-01-01"));

        // Literal to Date.
        Assertions.assertEquals(new Date(125, 5, 15), type.transform(new Literal("2025-06-15")));

        // Null passes through.
        Assertions.assertNull(type.transform(null));

        // Already a Date passes through.
        Date d = new Date();
        Assertions.assertSame(d, type.transform(d));

        // Invalid format throws.
        try {
            type.transform("not-a-date");
            Assertions.fail("expected to fail");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("invalid date: not-a-date", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void values_date_custom_parser() throws ExpressionBuildException {
        // Custom parser accepting d/M/yyyy.
        DateType.Parser custom = str -> {
            String[] parts = str.split("/");
            if (parts.length != 3)
                throw new ExpressionBuildException("invalid date: " + str);
            try {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return new Date(year - 1900, month - 1, day);
            } catch (NumberFormatException e) {
                throw new ExpressionBuildException("invalid date: " + str);
            }
        };
        DateType type = new DateType(custom);
        Assertions.assertEquals(new Date(125, 1, 1), type.transform("1/2/2025"));

        // Chained: ISO first, then custom fallback.
        DateType.Parser combined = DateType.ISO.or(custom);
        DateType chained = new DateType(combined);
        Assertions.assertEquals(new Date(125, 1, 1), chained.transform("2025-02-01"));
        Assertions.assertEquals(new Date(125, 1, 1), chained.transform("1/2/2025"));
    }

    @Test
    public void values_datetime() {
        // Valid ISO date string (date-only accepted by DateTimeType).
        try {
            TestFilter.term(Fields.CREATED, Operator.GTE, "2020-01-01").validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        // Valid ISO datetime string.
        try {
            TestFilter.term(Fields.CREATED, Operator.LT, "2025-12-31T23:59:59").validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
        // Non-string rejected.
        try {
            TestFilter.term(Fields.CREATED, Operator.EQ, 20200101).validate();
            Assertions.fail("expected to fail validation");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("expected a date/time: CREATED EQ 20200101", e.getMessage());
        }
        // Null accepted.
        try {
            TestFilter.term(Fields.CREATED, Operator.EQ, null).validate();
        } catch (ExpressionBuildException e) {
            Assertions.fail("expected to pass validation: " + e.getMessage());
        }
    }

    @Test
    public void values_datetime_transform() throws ExpressionBuildException {
        DateTimeType type = new DateTimeType();

        // Date-only string to Date (midnight UTC).
        Date result = (Date) type.transform("2020-01-01");
        Assertions.assertNotNull(result);

        // DateTime string to Date.
        result = (Date) type.transform("2025-12-31T23:59:59");
        Assertions.assertNotNull(result);

        // Literal to Date.
        result = (Date) type.transform(new Literal("2020-06-15"));
        Assertions.assertNotNull(result);

        // Null passes through.
        Assertions.assertNull(type.transform(null));

        // Already a Date passes through.
        Date d = new Date();
        Assertions.assertSame(d, type.transform(d));

        // Invalid format throws.
        try {
            type.transform("not-a-date");
            Assertions.fail("expected to fail");
        } catch (ExpressionBuildException e) {
            Assertions.assertEquals("invalid date/time: not-a-date", e.getMessage());
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
            STATUS(new EnumType<Status>(Status.class)),
            DOC_TYPE(new DynamicEnumType("DocumentType", List.of("resolution", "deed", "other"))),
            BIRTH_DATE(new DateType(Operator.EQ, Operator.NEQ, Operator.GT, Operator.GTE, Operator.LT, Operator.LTE)),
            CREATED(new DateTimeType(Operator.EQ, Operator.NEQ, Operator.GT, Operator.GTE, Operator.LT, Operator.LTE));

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
