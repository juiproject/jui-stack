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
package com.effacy.jui.rpc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.effacy.command.dto.C;
import com.effacy.command.dto.E;
import com.effacy.command.dto.V;

public final class RemoteSupport {

    public static List<FieldPair> getDeclaredFields(Object obj) {
        return Collections.emptyList ();
    }


    public static String getSimpleName(Class<?> klass) {
        return "";
    }


    private RemoteSupport() {
        // Nothing.
    }

    public static class FieldPair {

        private String name;

        private Object value;

        private boolean set;


        public FieldPair(String name, Object value, boolean set) {
            this.name = name;
            this.value = value;
        }


        public String getName() {
            return name;
        }


        public Object getValue() {
            return value;
        }


        public String getNameLabel() {
            if (set)
                return "(set)" + name;
            return name;
        }

    }
}
