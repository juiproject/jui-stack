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
package com.effacy.jui.core.client.navigation;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NavigationSupport}.
 */
public class NavigationSupportTest {

    @Test
    public void testBuild() {
        // TEST: Empty path.
        Assertions.assertEquals ("", NavigationSupport.build ());
        Assertions.assertEquals ("", NavigationSupport.build ((List<String>) null));

        // TEST: Single component.
        Assertions.assertEquals ("/hubba", NavigationSupport.build ("hubba"));
        Assertions.assertEquals ("/hubba", NavigationSupport.build (list ("hubba")));

        // TEST: Multple components.
        Assertions.assertEquals ("/hubba/bubba/wibble", NavigationSupport.build ("hubba", "bubba", "wibble"));
        Assertions.assertEquals ("/hubba/bubba/wibble", NavigationSupport.build (list ("hubba", "bubba", "wibble")));
    
        // TEST: Trimming
        Assertions.assertEquals ("/hubba/bubba/wibble", NavigationSupport.build ("hubba  ", " bubba", "  wibble   "));
        Assertions.assertEquals ("/hubba/bubba/wibble", NavigationSupport.build (list ("hubba  ", " bubba", "  wibble   ")));

        // TEST: Empty values
        Assertions.assertEquals ("/hubba/wibble", NavigationSupport.build ("hubba  ", " ", "  wibble   "));
        Assertions.assertEquals ("/hubba/wibble", NavigationSupport.build (list ("hubba  ", " ", "  wibble   ")));

        // TEST: Null values
        Assertions.assertEquals ("/hubba", NavigationSupport.build ("hubba  ", null, null));
        Assertions.assertEquals ("/hubba", NavigationSupport.build (list ("hubba  ", null, null)));
        Assertions.assertEquals ("", NavigationSupport.build (list (null, null, null)));
    }

    @Test
    public void testBuildWithMax() {
        // TEST: Various max values.
        Assertions.assertEquals ("/hubba/bubba/wibble", NavigationSupport.build (list ("hubba", "bubba", "wibble"), 0));
        Assertions.assertEquals ("/hubba", NavigationSupport.build (list ("hubba", "bubba", "wibble"), 1));
        Assertions.assertEquals ("/hubba/bubba", NavigationSupport.build (list ("hubba", "bubba", "wibble"), 2));
        Assertions.assertEquals ("/hubba/bubba/wibble", NavigationSupport.build (list ("hubba", "bubba", "wibble"), 3));
    }

    @Test
    public void testSplit() {
        Assertions.assertEquals (list("hubba", "bubba"), NavigationSupport.split ("/hubba/bubba"));
        Assertions.assertEquals (list("hubba", "bubba"), NavigationSupport.split ("/hubba//bubba"));
        Assertions.assertEquals (list("hubba", "bubba"), NavigationSupport.split ("///hubba////bubba///"));

        Assertions.assertEquals (list(), NavigationSupport.split ("///"));
        Assertions.assertEquals (list(), NavigationSupport.split (""));
    }

    /**
     * Converts an array of items to a list.
     * 
     * @param items
     *              the items to convert.
     * @return the items as a list.
     */
    protected List<String> list(String...items) {
        var result = new ArrayList<String>();
        for (String item : items)
            result.add (item);
        return result;
    }
    
}
