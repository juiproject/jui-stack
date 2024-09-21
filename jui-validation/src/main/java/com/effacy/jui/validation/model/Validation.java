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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.validation.model.IValidator.Message;

/**
 * Convenience to collect validations and generate a {@link ValidationException}
 * if there are violations. This allows for multiple validation messages to be
 * accumulated rather than stopping at the first.
 * <p>
 * The simplest approach is to employ {@link #validate(Consumer)} and bundle all
 * the validations within the consumer. This will automatically throw an
 * exception if any of the validations fail (and will accumulate all validation
 * failures).
 *
 * @author Jeremy Buckley
 */
public class Validation {

    /**
     * The accumulated validation messages.
     */
    private List<Message> messages = new ArrayList<> ();

    /**
     * Marks that there has been a validation failure.
     */
    private boolean invalid = false;

    /**
     * Forces an invalidation with the given path and message.
     * 
     * @param path
     *                the path to associate the message with.
     * @param message
     *                the message.
     */
    public void invalidate(String path, String message) {
        invalid = true;
        messages.add (new Message(message).path (path));
    }

    /**
     * Validates the passed value against the given validation and associate any
     * generated message with the given path specifier
     * 
     * @param validator
     *                  the validator to employ.
     * @param value
     *                  the value to validate.
     * @param path
     *                  the path to associated messages with.
     * @return the passed value (being validated).
     */
    public <V, T extends V> T validate(IValidator<V> validator, T value, String path) {
        if (!validator.validate (value, path, msg -> messages.add (msg)))
            invalid = true;
        return value;
    }

    /**
     * Validates the passed value against the given validation and associate any
     * generated message with the given path specifier
     * 
     * @param validator
     *                  the validator to employ.
     * @param value
     *                  the value to validate.
     * @return the passed value (being validated).
     */
    public <V, T extends V> T validate(IValidator<V> validator, T value) {
        if (!validator.validate (value, msg -> messages.add (msg)))
            invalid = true;
        return value;
    }

    /**
     * Performs a check for validation failure and if so throws a
     * {@link ValidationException} with the generated messages from the failures.
     * 
     * @throws ValidationException
     *                             on failure.
     */
    public void check() throws ValidationException {
        if (invalid)
            throw new ValidationException (messages);
    }

    /**
     * Convenience to bundle validations and perform a validation check.
     * 
     * @param runner
     *               bundling of validation checks (and possible assignments).
     * @throws ValidationException
     *                             on validation failure(s).
     */
    public static void validate(Consumer<Validation> runner) throws ValidationException {
        Validation val = new Validation ();
        runner.accept (val);
        val.check ();
    }
}
