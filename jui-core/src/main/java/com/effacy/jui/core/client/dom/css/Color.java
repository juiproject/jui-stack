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
package com.effacy.jui.core.client.dom.css;

import com.effacy.jui.core.client.dom.css.CSS.ICSSProperty;

public class Color implements ICSSProperty {

    /**
     * Creates an inherit value.
     */
    public static Color inherit() {
        return new Color (null);
    }

    /**
     * Makes use of the raw color value.
     */
    public static Color raw(String color) {
        return new Color (color);
    }

    /**
     * Makes use of a CSS variable.
     */
    public static Color variable(String variable) {
        return new Color ("var(" + variable + ")") ;
    }

    /**
     * The length value.
     */
    private String raw;

    /**
     * Internal constructor. See static methods.
     * 
     * @param value
     *            the length value.
     */
    protected Color(String raw) {
        this.raw = raw;
    }

    @Override
    public String value() {
        if (raw == null)
            return "inherit";
        return raw;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value ();
    }

}

