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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;

public class Validator<T> implements IValidator<T> {

    protected String message;

    protected IValueValidator<T> validator;

    protected Map<String, String> replacements;

    protected Supplier<Boolean> precondition;

    public Validator(String message, IValueValidator<T> validator) {
        this.message = message;
        this.validator = validator;
    }

    /**
     * Record a replacement key-value pair to perform replacement on the message
     * body.
     * 
     * @param key
     *              the key.
     * @param value
     *              the value to replace the key by.
     * @return this validator.
     */
    public Validator<T> replace(String key, Object value) {
        if (value == null)
            return this;
        if (replacements == null)
            replacements = new HashMap<> ();
        replacements.put (key, value.toString ());
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValidator#validate(java.lang.Object,
     *      java.util.function.Consumer)
     */
    @Override
    public boolean validate(T value, Consumer<Message> messages) {
        if (!test (value)) {
            String msg = (message == null) ? "" : message;
            if (value != null)
                msg = msg.replace ("{value}", value.toString ());
            if (replacements != null) {
                for (Map.Entry<String, String> replacement : replacements.entrySet ()) {
                    if (replacement.getKey () != null)
                        msg = msg.replace ("{" + replacement.getKey () + "}", replacement.getValue ());
                }
            }
            messages.accept (new Message (msg));
            return false;
        }
        return true;
    }

    /**
     * Underlying test for validity.
     * 
     * @param value
     *              the value to test.
     * @return {@code true} if valid (or if any precondition fails).
     */
    protected boolean test(T value) {
        if ((precondition != null) && !precondition.get())
            return true;
        if (validator == null)
            return true;
        return validator.validate (value);
    }

    /**
     * Assigns a precondition. When a test of validity is performed the precondition
     * is checked for truth. If is true then the test proceeded.
     * 
     * @param precondition
     *                     the precondition.
     * @return this validator instance.
     */
    public Validator<T> precondition(Supplier<Boolean> precondition) {
        this.precondition = precondition;
        return this;
    }
}
