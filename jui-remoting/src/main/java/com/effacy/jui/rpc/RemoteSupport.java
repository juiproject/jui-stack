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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.effacy.jui.rpc.client.Remote;
import com.effacy.jui.rpc.handler.client.command.C;
import com.effacy.jui.rpc.handler.client.command.E;
import com.effacy.jui.rpc.handler.client.command.V;

/**
 * Support for {@link Remote} classes.
 *
 * @author Jeremy Buckley
 */
public final class RemoteSupport {

    /**
     * Obtains a list of the declared fields on a DTO object. Special handling
     * is provided for fields of type {@link V} and {@link E} to determine the
     * value and set status of the underlying field value.
     * 
     * @param obj
     *            the object to extract the fields from.
     * @return the fields.
     */
    public static List<FieldPair> getDeclaredFields(Object obj) {
        List<FieldPair> pairs = new ArrayList<FieldPair> ();
        Set<String> fieldNames = new HashSet<String> ();
        Class<?> klass = obj.getClass ();
        while ((klass != null) && !klass.getPackage ().toString ().startsWith ("java") && !klass.equals (Remote.class) && !klass.equals (E.class) && !klass.equals (C.class)) {
            for (Field field : klass.getDeclaredFields ()) {
                String fieldName = field.getName ();
                if (fieldNames.contains (fieldName) || fieldName.startsWith ("$"))
                    continue;
                try {
                    field.setAccessible (true);
                    Object value = field.get (obj);
                    if (value instanceof V) {
                        if (((V<?>) value).isSet ()) {
                            value = ((V<?>) value).getValue ();
                            pairs.add (new FieldPair (fieldName, value, true));
                        }
                    } else if (value instanceof E) {
                        if (((E) value).dirty ())
                            pairs.add (new FieldPair (fieldName, value, false));
                    } else if (value != null)
                        pairs.add (new FieldPair (fieldName, value, (value != null)));
                } catch (Throwable e) {
                    pairs.add (new FieldPair (fieldName, "???", false));
                }

            }
            klass = klass.getSuperclass ();
        }
        return pairs;
    }


    /**
     * Gets the simple name of the passed class.
     * 
     * @param klass
     *            the class to obtain the simple name for.
     * @return the simple name of the class.
     */
    public static String getSimpleName(Class<?> klass) {
        return klass.getSimpleName ();
    }


    /**
     * Private constructor for the utility class.
     */
    private RemoteSupport() {
        // Nothing.
    }

    /**
     * Represents a field by name, its value and if it has been set (assigned).
     */
    public static class FieldPair {

        /**
         * The field name.
         */
        private String name;

        /**
         * The value of the field.
         */
        private Object value;

        /**
         * If the field has been set.
         */
        private boolean set;


        /**
         * Constructs a field pair.
         * 
         * @param name
         *            the name of the field.
         * @param value
         *            the value of the field.
         * @param set
         *            if the field has been set.
         */
        public FieldPair(String name, Object value, boolean set) {
            this.name = name;
            this.value = value;
        }


        /**
         * Gets the name of the field (aka property name).
         * 
         * @return the field name.
         */
        public String getName() {
            return name;
        }


        /**
         * Gets the value assigned to the field.
         * 
         * @return the field value.
         */
        public Object getValue() {
            return value;
        }


        /**
         * Determines if the field has been set.
         * 
         * @return {@code true} if it has.
         */
        public boolean isSet() {
            return set;
        }


        /**
         * Gets a human readable expression for the field (the field name
         * prefixed by <code>(set)</code> if it has been set.
         * 
         * @return the name to display for the field.
         */
        public String getNameLabel() {
            if (set)
                return "(set)" + name;
            return name;
        }

    }
}
