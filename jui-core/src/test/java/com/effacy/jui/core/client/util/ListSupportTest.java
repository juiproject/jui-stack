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
package com.effacy.jui.core.client.util;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.platform.util.client.ListSupport;

public class ListSupportTest {

    /**
     * Tests {@link ListSupport#split(String, char)}.
     */
    @Test
    public void testSplit() {
        assertEquals (ListSupport.split ("a/b/c", '/'), "a", "b", "c");
        assertEquals (ListSupport.split ("/a/b/c/", '/'), "a", "b", "c");
        assertEquals (ListSupport.split ("//a/b/c/", '/'), "", "a", "b", "c");
        assertEquals (ListSupport.split ("//a/b///c/", '/'), "", "a", "b", "", "", "c");
        assertEquals (ListSupport.split ("/a/b/c//", '/'), "a", "b", "c", "");
        assertEquals (ListSupport.split ("/", '/'));
        assertEquals (ListSupport.split ("//", '/'), "");
    }

    /**
     * Asserts that the list contains the passed items in the same order as stated.
     * 
     * @param list
     *              the list to test.
     * @param items
     *              the items to test that constitute (in the same order) the list.
     */
    protected void assertEquals(List<String> list, String... items) {
        Assertions.assertNotNull (list);
        Assertions.assertEquals (list.size (), items.length);
        for (int i = 0; i < items.length; i++)
            Assertions.assertEquals (list.get (i), items[i]);
    }

}
