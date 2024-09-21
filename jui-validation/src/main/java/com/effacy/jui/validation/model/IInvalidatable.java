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

import java.util.List;

/**
 * Something that renders a state of invalidation.
 */
public interface IInvalidatable {

    /**
     * Invalidate with the passed error messages.
     * <p>
     * The error messages are optional and should be displayed if present. If not
     * present (either empty or {@code null}) then an error state is still expected
     * to be rendered.
     * 
     * @param errors
     *               (optional) specific error messages to render.
     */
    public void invalidate(List<? extends IErrorMessage> errors);

    /**
     * Clears the invalidation state.
     */
    public void clearInvalid();
}
