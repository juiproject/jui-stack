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

import java.util.function.Consumer;

/**
 * Used to resolve some value asynchronously and allow for an error condition.
 */
@FunctionalInterface
public interface IResolver<T> {

    /**
     * Resolve the underlying value passing it through to the receiver.
     * 
     * @param receiver
     *                 to receive the value.
     * @param failure
     *                 to invoke on failure with an error message.
     */
    public void resolve(Consumer<T> receiver, Consumer<String> failure);

    /**
     * Pass-through resolver where the value is known ahead of time.
     * 
     * @param value
     *              the value.
     * @return the resolver.
     */
    public static <V> IResolver<V> of(V value) {
        return (r,f) -> r.accept (value);
    }
}
