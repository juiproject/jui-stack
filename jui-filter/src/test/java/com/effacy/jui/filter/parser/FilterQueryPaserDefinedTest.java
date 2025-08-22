package com.effacy.jui.filter.parser;

import org.junit.jupiter.api.Test;

import com.effacy.jui.filter.builder.ExpressionBuilder;
import com.effacy.jui.filter.builder.ExpressionBuilder.Expression;
import com.effacy.jui.filter.builder.Field;
import com.effacy.jui.filter.builder.IExpressionBuilder.Operator;
import com.effacy.jui.filter.parser.FilterQueryParser.FilterQueryParserException;

public class FilterQueryPaserDefinedTest {

    /**
     * Test across different operators.
     */
    @Test
    public void operators() throws Exception {
        ReviewFilter.deserialise("status == DRAFT");
        ReviewFilter.deserialise("status is DRAFT");
        ReviewFilter.deserialise("status in [DRAFT]");
    }

    /************************************************************************
     * Filter sample.
     ************************************************************************/

    public class ReviewFilter {

        public enum ReviewStatus {
            DRAFT, ACTIVE, ARCHIVED;
        }

        public enum Fields implements Field {

            KEYWORDS(new StringType(Operator.EQ, Operator.CONTAINS, Operator.ENDS_WITH, Operator.STARTS_WITH)),
            STATUS(new EnumType<ReviewStatus>(ReviewStatus.class, Operator.EQ, Operator.NEQ, Operator.IN, Operator.NOT_IN))
            ;

            private Type type;
            private Fields(Type type) { this.type = type; }
            public Type type() { return type; }
        }

        private static ExpressionBuilder<Fields> INSTANCE = new ExpressionBuilder<>(Fields.class);


        public static String serialise(Expression<Fields> exp) {
            return INSTANCE.serialise(exp);
        }

        public static Expression<Fields> deserialise(String str) throws FilterQueryParserException {
            return INSTANCE.deserialise(str);
        }
    }
}
