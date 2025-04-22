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
package com.effacy.jui.platform.util.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringSupportTest {

    @Test
    public void test_split() {
        Assertions.assertEquals (1, StringSupport.split("kashkjasdghk").length);
        Assertions.assertEquals (2, StringSupport.split("kashkj\nasdghk").length);
        Assertions.assertEquals (3, StringSupport.split("ka\rhkj\nasdghk").length);
        Assertions.assertEquals (2, StringSupport.split("kashkj\rasdghk").length);
        Assertions.assertEquals (6, StringSupport.split("\r\rkashkj\r\r\rasdghk\r").length);
    }

    /**
     * Tests {@link StringSupport#safe(String)}.
     */
    @Test
    public void test_safe() {
        // Null is converted to an empty string.
        Assertions.assertEquals ("", StringSupport.safe (null));

        // Strings are passed through unchanged.
        Assertions.assertEquals ("", StringSupport.safe (""));
        Assertions.assertEquals ("hubba", StringSupport.safe ("hubba"));

        // Special case (but one that needs to be explicit). No trimming is performed.
        Assertions.assertEquals ("  ", StringSupport.safe ("  "));
        Assertions.assertEquals ("  hubba ", StringSupport.safe ("  hubba "));
    }

    /**
     * Tests {@link StringSupport#empty(String)}.
     */
    @Test
    public void test_empty() {
        // Null string is empty.
        Assertions.assertTrue (StringSupport.empty (null));

        // Various forms of blank.
        Assertions.assertTrue (StringSupport.empty (""));
        Assertions.assertTrue (StringSupport.empty ("   "));

        // Various forms with content.
        Assertions.assertFalse (StringSupport.empty ("s"));
        Assertions.assertFalse (StringSupport.empty (" s"));
        Assertions.assertFalse (StringSupport.empty ("s "));
        Assertions.assertFalse (StringSupport.empty ("   s "));
    }

    /**
     * Tests {@link StringSupport#equals(String, String)}.
     */
    @Test
    public void test_equals() {
        // Equals for blank values.
        Assertions.assertTrue (StringSupport.equals (null, null));
        Assertions.assertTrue (StringSupport.equals (null, ""));
        Assertions.assertTrue (StringSupport.equals (null, "   "));
        Assertions.assertTrue (StringSupport.equals ("", null));
        Assertions.assertTrue (StringSupport.equals ("   ", null));
        Assertions.assertTrue (StringSupport.equals ("   ", " "));

        // Equals up-to trimming.
        Assertions.assertTrue (StringSupport.equals ("hubba", " hubba"));
        Assertions.assertTrue (StringSupport.equals ("hubba", "hubba  "));
        Assertions.assertTrue (StringSupport.equals ("hubba", " hubba  "));
        Assertions.assertTrue (StringSupport.equals ("hubba ", " hubba  "));

        // Not equals.
        Assertions.assertFalse (StringSupport.equals ("hvbba", " hubba"));
        Assertions.assertFalse (StringSupport.equals ("hvbba", "hubba  "));
        Assertions.assertFalse (StringSupport.equals ("hvbba", " hubba  "));
        Assertions.assertFalse (StringSupport.equals ("hvbba ", " hubba  "));
    }
}
