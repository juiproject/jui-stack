package com.effacy.jui.validation.model.validator;

import com.effacy.jui.platform.util.client.Logger;

import com.effacy.jui.validation.model.JUIValidationSupport;

public class CharacterSupport {

    public static boolean isLetter(char c) {
        // Delegate to native JS (efficient and to support unicode).
        return JUIValidationSupport.isLetter(c);
    }

    public static boolean isSpaceChar(char c) {
        return Character.isSpaceChar(c);
    }

    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }

    public static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

}
