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

import org.gwtproject.regexp.shared.RegExp;

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;

/**
 * Validates an email. The case of an empty email is deemed permissible (as it
 * can be tested separately).
 * <p>
 * The validator makes use of a simple regular expression (see s10 of
 * https://www.baeldung.com/java-email-validation-regex). However this can be
 * substituted for a different validator by assigning a function to
 * {@link #VALIDATOR}. An example would be server-side to make use of (say) the
 * Apache email validator.
 */
public class EmailValidator implements IValueValidator<CharSequence> {

    /**
     * The underlying validator to use. If not set then the default is used.
     */
    public static Function<String,Boolean> VALIDATOR;

    /**
     * Used as the regular expression evaluator for the default validator.
     */
    private static final RegExp PATTERN = RegExp.compile ("^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$");
    
    @Override
    public boolean validate(CharSequence value) {
        if (value == null)
            return true;
        String str = value.toString ().trim ();
        if (str.isEmpty())
            return true;
        if (VALIDATOR == null) {
            // Provide the default validator which is JS safe. This should be OK in a
            // multi-threaded case as the default assignment is the same each time so
            // overwriting is not an issue.
            VALIDATOR = (email) -> {
                return PATTERN.test (email);
            };
        }
        return VALIDATOR.apply(str);
    }

    /**
     * Creates a validator from the value validator and message.
     * 
     * @param min
     *                the minimum number of characters (default is 0).
     * @param max
     *                the maximum number of characters (default is no maximum).
     * @param message
     *                the message.
     * @return the validator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CharSequence> IValidator<T> validator(String message) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.EmailValidator}";
            return (IValidator<T>) new Validator<CharSequence> (message, new EmailValidator ());
    }
}
