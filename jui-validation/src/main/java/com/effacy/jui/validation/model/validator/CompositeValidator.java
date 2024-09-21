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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;
import com.effacy.jui.validation.model.ValidatorBuilder;

public class CompositeValidator<T> implements IValidator<T> {

    /**
     * Compose a composition over the passed validators.
     * 
     * @param <T>
     *                   the value type.
     * @param validators
     *                   the validators.
     * @return the composite.
     */
    @SafeVarargs
    public static <V> CompositeValidator<V> create(IValidator<V>... validators) {
        return new CompositeValidator<V> (validators);
    }

    /**
     * The validators to apply.
     */
    private List<IValidator<T>> validators = new ArrayList<> ();

    /**
     * See {@link #failOnFirst(boolean)}.
     */
    private boolean failOnFirst = true;

    /**
     * Construct over the passed validators.
     * 
     * @param validators
     *                   the validators.
     */
    @SafeVarargs
    public CompositeValidator(IValidator<T>... validators) {
        add (validators);
    }

    /**
     * If should fail on the first failed validator (rather than processing all validators).
     * @param failOnFirst
     * @return
     */
    public CompositeValidator<T> failOnFirst(boolean failOnFirst) {
        this.failOnFirst = failOnFirst;
        return this;
    }

    /**
     * Adds one or more validators to the composite.
     * 
     * @param validators
     *                   the validator(s) to add.
     * @return this validator.
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final CompositeValidator<T> add(IValidator<T>... validators) {
        for (IValidator<? extends T> validator : validators) {
            if (validator == null)
                continue;
            this.validators.add ((IValidator<T>) validator);
        }
        return this;
    }

    /**
     * Adds a validator from composing a message with a value validator.
     * 
     * @param message
     *                  the message.
     * @param validator
     *                  the value validator.
     * @return this validator.
     */
    public CompositeValidator<T> add(String message, IValueValidator<T> validator) {
        return add (ValidatorBuilder.create (message, validator));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValidator#validate(java.lang.Object,
     *      java.util.function.Consumer)
     */
    @Override
    public boolean validate(T value, Consumer<Message> messages) {
        for (IValidator<T> validator : validators) {
            if (!validator.validate (value, messages)) 
                return false;
        }
        return true;
    }

}
