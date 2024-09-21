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
package com.effacy.jui.core.client.store;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.GWTTest;

public class ListStoreTest extends GWTTest {

    @Test
    public void test_add() {
        ListStore<String> store = new ListStore<>();
        store.add("A01", "A02", "A03", "A04", "A05");
        Assertions.assertEquals (5, store.size ());
    }

    @Test
    public void test_build() {
        ListStore<String> store = new ListStore<>(s -> {
            s.add ("A01", "A02", "A03", "A04", "A05");
        });
        Assertions.assertEquals (5, store.size ());
    }

}
