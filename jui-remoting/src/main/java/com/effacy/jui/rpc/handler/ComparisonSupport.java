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
package com.effacy.jui.rpc.handler;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

/**
 * Collection of tools to work with comparisons.
 *
 * @author Jeremy Buckley
 */
public final class ComparisonSupport {

    /**
     * Determines if the values are the same.
     * <p>
     * Note that there is a special case where an empty string (blank that is)
     * is being compared to a <code>null</code>. In this instance they are
     * treated as being the same.
     * <p>
     * If the values are collections then a check is performed only on equality
     * of contents (not order).
     * 
     * @param value1
     *            value 1 to check.
     * @param value2
     *            value 2 to check.
     * @return {@code true} if they are the same.
     */
    public static <T> boolean same(T value1, T value2) {
        if (value1 == value2)
            return true;
        if ((value1 == null) || (value2 == null)) {
            if ((value1 != null) && (value1 instanceof String) && StringUtils.isBlank (((String) value1)))
                return true;
            if ((value2 != null) && (value2 instanceof String) && StringUtils.isBlank (((String) value2)))
                return true;
            return false;
        }
        if (value1 instanceof Collection) {
            if (!(value2 instanceof Collection))
                return false;
            Collection<?> col1 = (Collection<?>) value1;
            Collection<?> col2 = (Collection<?>) value2;
            if (col1.size () != col2.size ())
                return false;
            for (Object obj : col1) {
                if (!col2.contains (obj))
                    return false;
            }
        }
        return value1.equals (value2);
    }


    /**
     * Private constructor.
     */
    private ComparisonSupport() {
        // Nothing.
    }
}
