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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A convenience class for performing assertions on properties using
 * lambda-style semantics.
 */
public class With<T> {

    /**
     * The value being wrapped.
     */
    private T value;


    /**
     * Construct around a value.
     * 
     * @param value
     *            the value.
     */
    public With(T value) {
        this.value = value;
    }


    /**
     * Unwrap the value.
     * 
     * @return the unwrapped value.
     */
    public T unwrap() {
        return value;
    }


    /**
     * Given a function that acts on the value and returns a list, perform an
     * action on a wrapped version of each element of the list.
     * 
     * @param <V>
     *            the value type being retrieved.
     * @param f
     *            the extractor.
     * @param action
     *            the action to perform
     * @return this instance.
     */
    public <V> With<T> forEach(Function<T, List<V>> f, Consumer<With<V>> action) {
        for (V item : f.apply (value))
            action.accept (new With<V> (item));
        return this;
    }


    /**
     * Given a function that acts on the value and returns a list, perform an
     * action on a wrapped version of indexed element in the list.
     * 
     * @param <V>
     *            the value type being retrieved.
     * @param f
     *            the extractor.
     * @param idx
     *            the index to get.
     * @param action
     *            the action to perform
     * @return this instance.
     */
    public <V> With<T> get(Function<T, List<V>> f, int idx, Consumer<With<V>> action) {
        action.accept (new With<V> (f.apply (value).get (idx)));
        return this;
    }


    /**
     * Evaluate the passed function on the value.
     * 
     * @param <V>
     *            the value type the function returns.
     * @param f
     *            the function.
     * @return the return value from the function.
     */
    public <V> V eval(Function<T, V> f) {
        return f.apply (value);
    }


    /**
     * Run (or apply) the passed consumer on the value.
     * 
     * @param c
     *            the consumer (as a lambda this is just a collection of
     *            operations).
     * @return this with instance.
     */
    public With<T> run(Consumer<T> c) {
        c.accept (value);
        return this;
    }


    /**
     * Evaluates the passed function on the value (see {@link #eval(Function)}),
     * wraps the returned value with a {@link With} and then runs (applies) the
     * passed consumer on that wrapped return value (see
     * {@link #run(Consumer)}).
     * <p>
     * This is useful for nesting with's without loosing to top value.
     * 
     * @param <V>
     *            the value type the function returns.
     * @param f
     *            the function.
     * @param c
     *            the consumer (as a lambda this is just a collection of
     *            operations).
     * @return this with instance.
     */
    public <V> With<T> with(Function<T, V> f, Consumer<With<V>> c) {
        c.accept (new With<V> (f.apply (value)));
        return this;
    }


    /**
     * Constructs a with.
     * 
     * @param value
     *            the value to wrap.
     * @return the wrapped value.
     */
    public static <T> With<T> $(T value) {
        return new With<T> (value);
    }


    /**
     * Runs commands against a value (but only if the value is non-null).
     * 
     * @param value
     *            the value to run against.
     * @param cmds
     *            the commands to run.
     * @return the passed value.
     */
    @SafeVarargs
    public static <T> T $(T value, Consumer<T>... cmds) {
        if (value == null)
            return null;
        for (Consumer<T> cmd : cmds) {
            if (cmd != null)
                cmd.accept (value);
        }
        return value;
    }
}
