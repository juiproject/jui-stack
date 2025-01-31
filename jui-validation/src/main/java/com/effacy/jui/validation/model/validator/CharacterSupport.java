package com.effacy.jui.validation.model.validator;

/**
 * Support for dealing with determination of character types.
 * <p>
 * A client-side version resides under <code>super</code> (in resources) that
 * makes use of {@link JUIValidationSupport} (and native JS).
 */
public class CharacterSupport {

    /**
     * See {@link Character#isLetter(char)}.
     */
    public static boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    /**
     * See {@link Character#isSpaceChar(char)}.
     */
    public static boolean isSpaceChar(char c) {
        return Character.isSpaceChar(c);
    }

    /**
     * See {@link Character#isWhitespace(char)}.
     */
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    /**
     * See {@link Character#isDigit(char)}.
     */
    public static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

}
