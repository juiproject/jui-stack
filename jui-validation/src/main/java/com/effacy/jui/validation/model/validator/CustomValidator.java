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
import java.util.function.Function;

import com.effacy.jui.validation.model.IValidator;

/**
 * Custom validator. CustomValidator.
 *
 * @author Jeremy Buckley
 */
public class CustomValidator<T> implements IValidator<T> {

    public static <T> CustomValidator<T> create(String message, Function<T, Boolean> validator) {
        return new CustomValidator<T> (message, validator);
    }

    private Message message;

    private Function<T, Boolean> validator;

    public CustomValidator(String message, Function<T, Boolean> validator) {
        this.message = new Message (message);
        this.validator = validator;
    }


    public CustomValidator(Message message, Function<T, Boolean> validator) {
        this.message = message;
        this.validator = validator;
    }


    @Override
    public boolean validate(T value, Consumer<Message> messages) {
        if (validator.apply (value))
            return true;
        messages.accept (message);
        return false;
    }

}
