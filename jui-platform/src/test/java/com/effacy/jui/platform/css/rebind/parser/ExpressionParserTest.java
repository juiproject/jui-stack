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
package com.effacy.jui.platform.css.rebind.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionParserTest {
    
    @Test
    public void testExpressionEval() throws Exception {
        assertEval ("hubba.bubb.wibble.Wibble.hubba ()", "hubba.bubb.wibble.Wibble.hubba()");
    }

    protected void assertEval(String expression, String normalisedExpression) throws Exception {
        ExpressionParser.ParsedExpression exp = ExpressionParser.parse ("@eval wibble " + expression + ";");
        Assertions.assertEquals (normalisedExpression, exp.expression ());
        Assertions.assertEquals ("wibble", exp.reference ());
    }

    @Test
    public void testExpressionDef() throws Exception {
        assertDef ("green");
        assertDef ("#e1e1e1");
        assertDef ("#fff");
        assertDef ("0.5em");
        assertDef ("-0.5em");
        assertDef ("8px");
        assertDef ("-8px");
        assertDef ("0");
        assertDef (".2px");
    }

    protected void assertDef(String value) throws Exception {
        ExpressionParser.ParsedExpression exp = ExpressionParser.parse ("@def hubba " + value + ";");
        Assertions.assertEquals ("\"" + value + "\"", exp.expression ());
        Assertions.assertEquals ("hubba", exp.reference ());
    }
}
