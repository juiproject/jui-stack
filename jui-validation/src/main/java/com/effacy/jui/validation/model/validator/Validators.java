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

import java.util.function.Function;

import com.effacy.jui.validation.model.IValidator;

public final class Validators {
    
    /**
     * Creates an instance of {@link NotEmptyValidator}.
     * 
     * @param <T>     the value type.
     * @param message
     *                the message to display.
     * @return the validator.
     */
    public static <T extends Object> IValidator<T> notEmpty(String message) {
        return NotEmptyValidator.validator (message);
    }

    /**
     * Creates an instance of {@link LengthValidator}.
     * 
     * @param <T>     the value type.
     * @param min
     *                the minimum number of characters (default is 0).
     * @param max
     *                the maximum number of characters (default is no maximum).
     * @param message
     *                the message to display.
     * @return the validator.
     */
    public static <T extends CharSequence> IValidator<T> length(int min, int max, String message) {
        return LengthValidator.validator (min, max, message);
    }

    /**
     * Creates an instance of {@link RegExpValidator}.
     * 
     * @param <T>     the value type.
     * @param expression
     *                the regexp to apply.
     * @param message
     *                the message to display.
     * @return the validator.
     */
    public static <T extends CharSequence> IValidator<T> regexp(String expression, String message) {
        return RegExpValidator.validator (expression, message);
    }

    /**
     * Creates an instance of {@link CharacterValidator}.
     * 
     * @param <T>     the value type.
     * @param rule
     *                the character rule to apply.
     * @param message
     *                the message to display.
     * @return the validator.
     */
    public static <T extends CharSequence> IValidator<T> character(CharacterValidatorRule rule, String message) {
        return CharacterValidator.validator (rule, message);
    }

    /**
     * Creates an instance of {@link CustomValidator}.
     * 
     * @param <T>       the value type.
     * @param validator
     *                  the validator rule to apply.
     * @param message
     *                  the message to display.
     * @return the validator.
     */
    public static <T> CustomValidator<T> custom(Function<T, Boolean> validator, String message) {
        return CustomValidator.create (message, validator);
    }
}
