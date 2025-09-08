/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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

public class Rational implements ICSSProperty {

    /**
     * Construct a rational number.
     * 
     * @param value
     *              the value.
     * @return the number.
     */
    public static Rational of(double value) {
        return new Rational (value);
    }

    /**
     * The underlying value.
     */
    private double value;

    /**
     * Construct with a value.
     * 
     * @param value
     *              the value.
     */
    public Rational(double value) {
        this.value = value;
    }

    @Override
    public String value() {
        return Double.toString (value);
    }

}