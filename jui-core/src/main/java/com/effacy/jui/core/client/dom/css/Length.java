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

public final class Length implements ICSSProperty {

    public enum Unit {
        PX("px"), PCT("%"), EM("em"), EX("ex"), PT ("pt"), PC("pc"), IN("in"), CM("cm"), MM("mm"), VH("vh"), VW("vw"), CAP("cap"), CH("ch"), LH("lh");

        private String text;

        private Unit(String text) {
            this.text = text;
        }
    
        public String text() {
            return text;
        }

      }

    /**
     * Auto size.
     */
    public static Length auto() {
        return new Length (null);
    }


    /**
     * Size in centimeters.
     */
    public static Length cm(double amt) {
        return new Length (amt + Unit.CM.text ());
    }


    /**
     * Size in centimeters.
     */
    public static Length cm(int amt) {
        return new Length (amt + Unit.CM.text ());
    }


    /**
     * Size as multiple of the 'font-size' of the relevant font.
     */
    public static Length em(double amt) {
        return new Length (amt + Unit.EM.text ());
    }


    /**
     * Size as multiple of the 'font-size' of the relevant font.
     */
    public static Length em(int amt) {
        return new Length (amt + Unit.EM.text ());
    }


    /**
     * Size as multiple of the 'x-height' of the relevant font.
     */
    public static Length ex(double amt) {
        return new Length (amt + Unit.EX.text ());
    }


    /**
     * Size as multiple of the 'x-height' of the relevant font.
     */
    public static Length ex(int amt) {
        return new Length (amt + Unit.EX.text ());
    }


    /**
     * Size in inches.
     */
    public static Length in(double amt) {
        return new Length (amt + Unit.IN.text ());
    }


    /**
     * Size in inches.
     */
    public static Length in(int amt) {
        return new Length (amt + Unit.IN.text ());
    }


    /**
     * Size in millimeters.
     */
    public static Length mm(double amt) {
        return new Length (amt + Unit.MM.text ());
    }


    /**
     * Size in millimeters.
     */
    public static Length mm(int amt) {
        return new Length (amt + Unit.MM.text ());
    }


    /**
     * Size in picas.
     */
    public static Length pc(double amt) {
        return new Length (amt + Unit.PC.text ());
    }


    /**
     * Size in picas.
     */
    public static Length pc(int amt) {
        return new Length (amt + Unit.PC.text ());
    }


    /**
     * Size in percentage units.
     */
    public static Length pct(double amt) {
        return new Length (amt + Unit.PCT.text ());
    }


    /**
     * Size in percentage units.
     */
    public static Length pct(int amt) {
        return new Length (amt + Unit.PCT.text ());
    }


    /**
     * Size in point.
     */
    public static Length pt(double amt) {
        return new Length (amt + Unit.PT.text ());
    }


    /**
     * Size in point.
     */
    public static Length pt(int amt) {
        return new Length (amt + Unit.PT.text ());
    }


    /**
     * Size in pixels.
     */
    public static Length px(double amt) {
        return new Length (amt + Unit.PX.text ());
    }


    /**
     * Size in pixels.
     */
    public static Length px(int amt) {
        return new Length (amt + Unit.PX.text ());
    }

    /**
     * The length value.
     */
    private String text;

    /**
     * Internal constructor. See static methods.
     * 
     * @param value
     *            the length value.
     */
    protected Length(String text) {
        this.text = text;
    }

    /**
     * Determines if the length is auto.
     * 
     * @return {@code true} if it is.
     */
    public boolean isAuto() {
        return (text == null);
    }

    @Override
    public String value() {
        if (text == null)
            return "auto";
        return text;
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
