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

import com.effacy.jui.validation.model.IValidator;
import com.effacy.jui.validation.model.IValueValidator;

/**
 * Value validator for the length of a character sequence.
 *
 * @author Jeremy Buckley
 */
public class LengthValidator implements IValueValidator<CharSequence> {

    /**
     * See constructor.
     */
    private int min = 0;

    /**
     * See constructor.
     */
    private int max = Integer.MAX_VALUE;

    /**
     * Construct instance of value validator.
     * 
     * @param min
     *            the minimum number of characters (default is 0).
     * @param max
     *            the maximum number of characters (default is no maximum).
     */
    public LengthValidator(int min, int max) {
        this.min = (min < 0) ? 0 : min;
        this.max = (max < 0) ? Integer.MAX_VALUE : max;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IValueValidator#validate(java.lang.Object)
     */
    @Override
    public boolean validate(CharSequence value) {
        if (value == null)
            return true;
        int length = value.length ();
        return (length >= min) && (length <= max);
    }

    /**
     * Creates a validator from the value validator and message.
     * <p>
     * The passed message can include <code>{min}</code> and <code>{max}</code> in
     * its content and these will be replaced by the respective min and max
     * parameters that have been passed.
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
    public static <T extends CharSequence> IValidator<T> validator(int min, int max, String message) {
        if (message == null)
            message = "{com.effacy.jui.validation.model.validator.SizeValidator}";
        return (IValidator<T>) new Validator<CharSequence> (message, new LengthValidator (min, max)).replace ("min", min).replace ("max", max);
    }

}
