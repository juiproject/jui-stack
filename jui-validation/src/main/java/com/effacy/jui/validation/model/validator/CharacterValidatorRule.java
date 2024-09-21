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
package com.effacy.jui.validation.model.validator;

public class CharacterValidatorRule {

    private boolean space;

    private boolean whitespace;

    private boolean letter;

    private boolean digit;

    private CharSequence characters;

    public CharacterValidatorRule space() {
        this.space = true;
        return this;
    }


    public CharacterValidatorRule whitespace() {
        this.whitespace = true;
        return this;
    }


    public CharacterValidatorRule letter() {
        this.letter = true;
        return this;
    }


    public CharacterValidatorRule digit() {
        this.digit = true;
        return this;
    }


    public CharacterValidatorRule characters(CharSequence characters) {
        this.characters = characters;
        return this;
    }


    /**
     * Tests the validity of the passed character sequence.
     * 
     * @param cs
     *            the character sequence to test.
     * @return {@code true} if passes.
     */
    public boolean test(final CharSequence cs) {
        int sz = cs.length ();
        for (int i = 0; i < sz; i++) {
            char nowChar = cs.charAt (i);
            if (includes (nowChar))
                continue;
            if (letter && Character.isLetter (nowChar))
                continue;
            if (space && Character.isSpaceChar (nowChar))
                continue;
            if (whitespace && Character.isWhitespace (nowChar))
                continue;
            if (digit && Character.isDigit (nowChar))
                continue;
            return false;
        }
        return true;
    }


    /**
     * Determines if the passed characters is included in the configured
     * includes set (if the set is empty then will automatically pass).
     * 
     * @param ch
     *            the character to test.
     * @return {@code true} if it is in the includes set.
     */
    protected boolean includes(char ch) {
        if (characters == null)
            return false;
        if (characters.length() == 0)
            return true;
        int sz = characters.length ();
        for (int i = 0; i < sz; i++) {
            char nowChar = characters.charAt (i);
            if (nowChar == ch)
                return true;
        }
        return false;
    }
}
