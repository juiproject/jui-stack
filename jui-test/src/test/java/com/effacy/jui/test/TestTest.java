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
package com.effacy.jui.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

public class TestTest {

    @org.junit.jupiter.api.Test
    public void test_isAsc() {
        List<String> list = new ArrayList<>();
        Test.$ (list).isAsc (v -> v.toString());

        list.add("aaa");
        Test.$ (list).isAsc (v -> v.toString());

        list.add("bbb");
        list.add("ccc");
        list.add("ddd");
        Test.$ (list).isAsc (v -> v.toString());

        list.add("bcc");
        try {
            Test.$ (list).isAsc (v -> v.toString());
            Assertions.fail();
        } catch (AssertionError e) {
            // Expected.
        }
    }

    @org.junit.jupiter.api.Test
    public void test_isDesc() {
        List<String> list = new ArrayList<>();
        Test.$ (list).isDesc (v -> v.toString());

        list.add("ddd");
        Test.$ (list).isDesc (v -> v.toString());

        list.add("ccc");
        list.add("bbb");
        list.add("aaa");
        Test.$ (list).isDesc (v -> v.toString());

        list.add("bcc");
        try {
            Test.$ (list).isDesc (v -> v.toString());
            Assertions.fail();
        } catch (AssertionError e) {
            // Expected.
        }
    }
}
