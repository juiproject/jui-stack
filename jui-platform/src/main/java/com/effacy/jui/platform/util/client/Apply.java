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
package com.effacy.jui.platform.util.client;

import java.util.function.Consumer;

/**
 * Used to conveniently apply actions to objects.
 *
 * @author Jeremy Buckley
 */
public class Apply {

    /**
     * Applies actions to the passed value and returns the value.
     * 
     * @param <C>
     *                the value type.
     * @param value
     *                the value to apply to.
     * @param applier
     *                to apply to the value.
     * @return the passed value.
     */
    public static <C> C $(C value, Consumer<C> applier) {
        if (applier != null)
            applier.accept (value);
        return value;
    }
}
