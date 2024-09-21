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

import java.util.Collection;
import java.util.function.Supplier;

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;

/**
 * Value validator to check for being empty (including {@code null}).
 *
 * @author Jeremy Buckley
 */
public class NotEmptyValidator implements IValueValidator<Object> {

    /**
     * Construct instance of value validator.
     */
    public NotEmptyValidator() {
        super ();
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValueValidator#validate(java.lang.Object)
     */
    @Override
    public boolean validate(Object value) {
        if (value == null)
            return false;
        if (value instanceof String)
            return !((String) value).trim ().isEmpty ();
        if (value instanceof Collection)
            return !((Collection<?>) value).isEmpty ();
        return true;
    }


    /**
     * Creates a validator from the value validator and message.
     * 
     * @param message
     *            the message.
     * @return the validator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> IValidator<T> validator(String message) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.SizeValidator}";
        return (IValidator<T>) new Validator<Object> (message, new NotEmptyValidator ());
    }


    /**
     * Creates a validator from the value validator and message.
     * 
     * @param message
     *                the message.
     * @param test
     *                an additional test to perform before passing onto the
     *                validator.
     * @return the validator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> IValidator<T> validator(String message, Supplier<Boolean> test) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.SizeValidator}";
        return (IValidator<T>) new Validator<Object> (message, new NotEmptyValidator ()).precondition (test);
    }

}
