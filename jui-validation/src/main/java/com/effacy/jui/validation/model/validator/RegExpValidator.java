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

import org.gwtproject.regexp.shared.RegExp;

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;

public class RegExpValidator implements IValueValidator<CharSequence> {

    /**
     * The regexp to process.
     */
    private RegExp expression;

    /**
     * Construct instance of value validator.
     * 
     * @param expression
     *              the character regex.
     */
    public RegExpValidator(RegExp expression) {
        this.expression = expression;
    }

    /**
     * Construct instance of value validator.
     * 
     * @param expression
     *              the character regex.
     */
    public RegExpValidator(String expression) {
        this.expression = RegExp.compile (expression);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValueValidator#validate(java.lang.Object)
     */
    @Override
    public boolean validate(CharSequence value) {
        if ((value == null) || (expression == null))
            return true;
        return expression.test (value.toString());
    }

    /**
     * Creates a validator from the value validator and message.
     * 
     * @param expression
     *                the regexp to apply.
     * @param message
     *                the message.
     * @return the validator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CharSequence> IValidator<T> validator(String expression, String message) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.RegExpValidator}";
        return (IValidator<T>) new Validator<CharSequence> (message, new RegExpValidator (expression));
    }

    /**
     * Creates a validator from the value validator and message.
     * 
     * @param expression
     *                the regexp to apply.
     * @param message
     *                the message.
     * @return the validator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CharSequence> IValidator<T> validator(RegExp expression, String message) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.RegExpValidator}";
        return (IValidator<T>) new Validator<CharSequence> (message, new RegExpValidator (expression));
    }

}
