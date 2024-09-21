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
package com.effacy.jui.rpc.extdirect.metadata;

/**
 * The field type for a field.
 */
public enum FieldType {
    /**
     * Determine automatically.
     */
    AUTO(true),

    /**
     * Textual value.
     */
    STRING(true),

    /**
     * Integral number.
     */
    INT(false),

    /**
     * Floating point number.
     */
    FLOAT(false),

    /**
     * Boolean value.
     */
    BOOLEAN(false),

    /**
     * Date field.
     */
    DATE(true);

    /**
     * If the underlying value needs to be quoted.
     */
    private boolean quotable;


    /**
     * Construct with data.
     * 
     * @param quotable
     *            if the underlying value needs to be quoted.
     */
    private FieldType(boolean quotable) {
        this.quotable = quotable;
    }


    /**
     * Determines if the underlying values of this type should be quoted.
     * 
     * @return {@code true} if the values should be quoted.
     */
    public boolean isQuotable() {
        return quotable;
    }

}
