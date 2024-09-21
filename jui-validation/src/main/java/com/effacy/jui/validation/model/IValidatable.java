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

/**
 * Something that is able to be validated.
 *
 * @author Jeremy Buckley
 */
public interface IValidatable {

    /**
     * Determine if the object is valid (this is a state-in-time validation
     * check so is not based on the invalidate state).
     * <p>
     * This does not change the state of the underlying object so the check is
     * logical only.
     * 
     * @return {@code true} if is valid.
     */
    public boolean valid();


    /**
     * See {@link #validate(boolean)}. Convenience that passes {@code true} as
     * the argument.
     * <p>
     * Note that (in general) the thing (or things) being validated must be in
     * an appropriate states. For example, a control should be enabled to be
     * validatable.
     * 
     * @return {@code true} if was valid.
     */
    default public boolean validate() {
        return validate (true);
    }


    /**
     * Determines if the object is valid and if not invalidate it (with
     * messages).
     * 
     * @param clearIfValid
     *            {@code true} if to clear any invalidated state if now valid.
     * @return {@code true} if was valid.
     */
    default public boolean validate(boolean clearIfValid) {
        return valid ();
    }

}
