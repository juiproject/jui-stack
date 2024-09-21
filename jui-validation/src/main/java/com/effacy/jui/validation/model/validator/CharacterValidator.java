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

import java.util.function.Consumer;

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;

/**
 * Value validator for a specifc collection of characters.
 * <p>
 * This is actually not JS compatible (due to Character) so a replacement class
 * is provided that validates true in all instances. This allows for sharing of
 * validations.
 *
 * @author Jeremy Buckley
 */
public class CharacterValidator implements IValueValidator<CharSequence> {

    private CharacterValidatorRule rule;

    /**
     * Construct instance of value validator.
     * 
     * @param regex
     *              the character regex.
     */
    public CharacterValidator(CharacterValidatorRule rule) {
        this.rule = rule;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValueValidator#validate(java.lang.Object)
     */
    @Override
    public boolean validate(CharSequence value) {
        if ((value == null) || (rule == null))
            return true;
        return rule.test (value);
    }

    /**
     * Creates a validator from the value validator and message.
     * 
     * @param rule
     *                the character rule to apply.
     * @param message
     *                the message.
     * @return the validator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CharSequence> IValidator<T> validator(CharacterValidatorRule rule, String message) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.CharacterValidator}";
        return (IValidator<T>) new Validator<CharSequence> (message, new CharacterValidator (rule));
    }

    /**
     * Creates a validator from the value validator and message.
     * 
     * @param configurer
     *                   configurer for the character rule.
     * @param message
     *                   the message.
     * @return the validator.
     */
    public static <T extends CharSequence> IValidator<T> validator(Consumer<CharacterValidatorRule> configurer, String message) {
        CharacterValidatorRule rule = new CharacterValidatorRule ();
        if (configurer != null)
            configurer.accept (rule);
        return validator (rule, message);
    }
}
