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
package com.effacy.jui.core.client;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Supplement to standard functions (i.e. {@link Consumer}) but to detail
 * something that can simply be invoked.
 *
 * @author Jeremy Buckley
 */
@FunctionalInterface
public interface Invoker {

    /**
     * Invokes the invoker.
     */
    public void invoke();

    /**
     * Composes this invoker with the passed invoker to give an invoker that, when
     * invoked, invokes this invoker followed by the passed invoker.
     *
     * @param after
     *              the operation to perform after this operation
     * @return a composed {@code Invoker} that performs in sequence this operation
     *         followed by the {@code after} operation
     * @throws NullPointerException
     *                              if {@code after} is null
     */
    default Invoker andThen(Invoker after) {
        Objects.requireNonNull (after);
        return () -> {
            invoke ();
            after.invoke ();
        };
    }
}
