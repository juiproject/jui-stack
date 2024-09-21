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
package com.effacy.jui.validation.model;

import com.effacy.jui.validation.model.validator.CompositeValidator;
import com.effacy.jui.validation.model.validator.LengthValidator;
import com.effacy.jui.validation.model.validator.Validator;

public class ValidatorBuilder {

    /**
     * Creates a validator from a value validator and a message.
     * 
     * @param message
     *            the message to assign.
     * @param validator
     *            the value validator.
     * @return the validator.
     */
    public static <T> IValidator<T> create(String message, IValueValidator<T> validator) {
        return new Validator<T> (message, validator);
    }


    /**
     * Composes multiple validators.
     * 
     * @param <T>
     * @param validators
     *            the validators to compose.
     * @return a composite validator.
     */
    @SafeVarargs
    public static <T> IValidator<T> compose(IValidator<T>... validators) {
        return new CompositeValidator<T> (validators);
    }


    /**
     * Convenience to obtains a size validator (see
     * {@link LengthValidator}).
     * 
     * @param message
     *            the message to display.
     * @param min
     *            the minimum number of characters (use -1 for the default which
     *            is 0).
     * @param max
     *            the maximum number of characters (use -1 for the default which
     *            is {@link Integer#MAX_VALUE}).
     * @return
     */
    public static IValidator<CharSequence> size(int min, int max, String message) {
        return LengthValidator.validator (min, max, message);
    }
}
