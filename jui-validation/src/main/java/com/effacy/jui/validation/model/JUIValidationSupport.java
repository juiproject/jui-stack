package com.effacy.jui.validation.model;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Native JS functions.
 * <p>
 * See <code>public/jui_validation.js</code>.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class JUIValidationSupport {

    /**
     * Determines if the passed character is a letter or not.
     * 
     * @param c
     *          the character to test.
     * @return {@code true} if it is.
     */
    public static native boolean isLetter(char c);
}
