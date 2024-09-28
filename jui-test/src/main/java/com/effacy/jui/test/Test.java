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
package com.effacy.jui.test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

/**
 * Convenience for testing objects. Wraps an object and allows assertions to be
 * make against attributes of the object in a chained fashion.
 * <p>
 * The priniple gain afforded by this is a simplified expression of bulk
 * assertions in a more readable form. 
 */
public class Test<T> {

    /**
     * Wraps the passed value.
     * 
     * @param value
     *              the value to wrap.
     * @return the wrapped value.
     */
    public static <T> Test<T> $(T value) {
        return new Test<T> (value);
    }

    /**
     * The value being wrapped.
     */
    private T value;

    /**
     * Construct around a value.
     * 
     * @param value
     *              the value.
     */
    public Test(T value) {
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
     * Assertionsion failure (generates an assertion exception). See
     * {@link Assertions#fail()}.
     */
    public void fail() {
        Assertions.fail ();
    }

    /**
     * Perform a conditional check Test an associated action. Returns a
     * {@link IfElse} which allows for additional conditional check (under the
     * if-else model) Test a final action. If there is no final action then call
     * {@link IfElse#done()} will return this test instance.
     * 
     * @param f
     *               the when conditional.
     * @param action
     *               the action to perform.
     * @return an if-else.
     */
    public IfElse<T> when(Function<T, Boolean> f, Consumer<Test<T>> action) {
        IfElse<T> ifElse = new IfElse<T> (this);
        return ifElse.when (f, action);
    }

    /**
     * Given a function that acts on the value and returns a list, perform an action
     * on a wrapped version of each element of the list.
     * 
     * @param <V>
     *               the value type being retrieved.
     * @param f
     *               the extractor.
     * @param action
     *               the action to perform
     * @return this test instance.
     */
    public <V> Test<T> forEach(Function<T, List<V>> f, Consumer<Test<V>> action) {
        for (V item : f.apply (value))
            action.accept (new Test<V> (item));
        return this;
    }

    /**
     * See {@link #get(Function, Consumer)}.
     */
    public <V> Test<T> with(Function<T, V> f, Consumer<Test<V>> action) {
        return get (f, action);
    }

    /**
     * Given a function that acts on the value and returns a list, perform an action
     * on a wrapped version of indexed element in the list.
     * 
     * @param <V>
     *               the value type being retrieved.
     * @param f
     *               the extractor.
     * @param action
     *               the action to perform
     * @return this test instance.
     */
    public <V> Test<T> get(Function<T, V> f, Consumer<Test<V>> action) {
        action.accept (new Test<V> (f.apply (value)));
        return this;
    }

    /**
     * Given a function that acts on the value and returns a list, perform an action
     * on a wrapped version of indexed element in the list.
     * 
     * @param <V>
     *               the value type being retrieved.
     * @param f
     *               the extractor.
     * @param idx
     *               the index to get.
     * @param action
     *               the action to perform
     * @return this test instance.
     */
    public <V> Test<T> get(Function<T, List<V>> f, int idx, Consumer<Test<V>> action) {
        action.accept (new Test<V> (f.apply (value).get (idx)));
        return this;
    }

    /**
     * Given a function that acts on the value and returns a list, perform an action
     * on a wrapped version of the first element in the list matching the predicate
     * (which must exist).
     * 
     * @param <V>
     *                the value type being retrieved.
     * @param f
     *                the extractor.
     * @param matcher
     *                the matcher predicate.
     * @param action
     *                the action to perform
     * @return this test instance.
     */
    public <V> Test<T> get(Function<T, List<V>> f, Predicate<V> matcher, Consumer<Test<V>> action) {
        List<V> matches = f.apply (value).stream ().filter (matcher).collect (Collectors.toList ());
        if (matches.isEmpty ())
            Assertions.fail ("Unable to find match in list");
        action.accept (new Test<V> (matches.get (0)));
        return this;
    }

    /**
     * Given a function that acts on the value and returns a list, match an element
     * in the list and return a wrapped version of that value.
     * 
     * @param <V>
     *            the value type being retrieved.
     * @param f
     *            the extractor.
     * @param m
     *            the matcher.
     * @return the wrapped value of the match (if no match then a wrapped
     *         {@code null} is returned).
     */
    public <V> Test<V> find(Function<T, List<V>> f, Predicate<V> m) {
        for (V item : f.apply (value)) {
            if (m.test (item))
                return new Test<V> (item);
        }
        return new Test<V> (null);
    }

    /**
     * See {@link #find(Function, Predicate)} but applies the result to the passed
     * consumer and return this.
     */
    public <V> Test<T> find(Function<T, List<V>> f, Predicate<V> m, Consumer<Test<V>> c) {
        Test<V> item = find (f, m);
        c.accept (item);
        return this;
    }

    /**
     * Given a function that acts on the value and returns a list, match an element
     * in the list and return a wrapped version of that value.
     * 
     * @param <V>
     *            the value type being retrieved.
     * @param f
     *            the extractor.
     * @param m
     *            the matcher.
     * @return this test instance.
     */
    public <V> Test<T> exists(Function<T, List<V>> f, Predicate<V> m) {
        for (V item : f.apply (value)) {
            if (m.test (item))
                return this;
        }
        Assertions.fail ("entry does not exist");
        return this;
    }

    /**
     * Given a function that acts on the value and returns a list, match an element
     * in the list and return a wrapped version of that value.
     * 
     * @param <V>
     *            the value type being retrieved.
     * @param f
     *            the extractor.
     * @param m
     *            the matcher.
     * @return this test instance.
     */
    public <V> Test<T> notExists(Function<T, List<V>> f, Predicate<V> m) {
        for (V item : f.apply (value)) {
            if (m.test (item))
                Assertions.fail ("entry exists but was not expected to");
        }
        return this;
    }

    /**
     * Evaluates the passed function and returns the result.
     * 
     * @param <V>
     *            the value type being retrieved.
     * @param f
     *            the evaluator.
     * @return the evaluated value.
     */
    public <V> V eval(Function<T, V> f) {
        return f.apply (value);
    }

    /**
     * Convenience to operate on the wrapped object.
     * 
     * @param c
     *          to consumer the wrapped object and perform any actions.
     * @return this test instance.
     */
    public Test<T> group(Consumer<T> c) {
        c.accept (value);
        return this;
    }

    /**
     * Assertionss that the value being wrapped is {@code null}.
     * 
     * @return this test instance.
     */
    public Test<T> isNull() {
        Assertions.assertNull (value);
        return this;
    }

    /**
     * Assertionss that the value being wrapped is non-{@code null}.
     * 
     * @return this test instance.
     */
    public Test<T> isNotNull() {
        Assertions.assertNotNull (value);
        return this;
    }

    /**
     * Asserts the mapped value is null.
     * 
     * @param f
     *          to retrive the test value from the wrapped object.
     * @return this test instance.
     */
    public <V> Test<T> isNull(Function<T, V> f) {
        Assertions.assertNull (f.apply (value));
        return this;
    }

    /**
     * Asserts te mapped value is not null.
     * 
     * @param f
     *          to retrive the test value from the wrapped object.
     * @return this test instance.
     */
    public <V> Test<T> isNotNull(Function<T, V> f) {
        Assertions.assertNotNull (f.apply (value));
        return this;
    }

    /**
     * Asserts the mapped string is not blank.
     * 
     * @param f
     *          to retrive the test value from the wrapped object.
     * @return this test instance.
     */
    public <V> Test<T> isNotBlank(Function<T, String> f) {
        Assertions.assertFalse (StringUtils.isBlank (f.apply (value)));
        return this;
    }

    /**
     * Asserts the mapped string is blank.
     * 
     * @param f
     *          to retrive the test value from the wrapped object.
     * @return this test instance.
     */
    public <V> Test<T> isBlank(Function<T, String> f) {
        Assertions.assertTrue (StringUtils.isBlank (f.apply (value)));
        return this;
    }

    /**
     * See {@link #isBlank(Function)}.
     */
    public Test<T> isEmpty(Function<T, String> f) {
        return isBlank(f);
    }

    /**
     * See {@link #isNotBlank(Function)}.
     */
    public Test<T> isNotEmpty(Function<T, String> f) {
        return isNotBlank(f);
    }

    public Test<T> isTrue(Function<T, Boolean> f) {
        Assertions.assertTrue (f.apply (value));
        return this;
    }

    public Test<T> isFalse(Function<T, Boolean> f) {
        Assertions.assertFalse (f.apply (value));
        return this;
    }

    /**
     * Extracts a list then converts the list to a list of comparable values. Then
     * asserts the resultant list is in ascending order.
     * 
     * @param f
     *               extracts the target list.
     * @param mapper
     *               maps the values in the list to strings.
     * @return this test instance.
     */
    public <V,W extends Comparable<W>> Test<T> isAsc(Function<T, List<V>> f, Function<V,W> mapper) {
        Test.$ (f.apply(value)).isAsc(mapper);
        return this;
    }

    /**
     * Extracts a list then converts the list to a list of comparable values. Then
     * asserts the resultant list is in ascending order.
     * <p>
     * Value type must be a list.
     * 
     * @param mapper
     *               maps the values in the list to strings.
     * @return this test instance.
     */
    @SuppressWarnings("unchecked")
    public <V,W extends Comparable<W>> Test<T> isAsc(Function<V,W> mapper) {
        List<W> mapped = ((List<V>)value).stream().map(mapper).filter(v -> (v != null)).collect(Collectors.toList());
        if (mapped.size() <= 1)
            return this;
            W prior = mapped.get(0);
        for (int i = 1; i < mapped.size(); i++) {
            W current = mapped.get(i);
            if (prior.compareTo(current) > 0)
                Assertions.fail("List not in ascending orderr");
            prior = current;
        }
        return this;
    }

    /**
     * Extracts a list then converts the list to a list of comparable values. Then
     * asserts the resultant list is in ascending order.
     * 
     * @param f
     *               extracts the target list.
     * @param mapper
     *               maps the values in the list to strings.
     * @return this test instance.
     */
    public <V,W extends Comparable<W>> Test<T> isDesc(Function<T, List<V>> f, Function<V,W> mapper) {
        Test.$ (f.apply(value)).isDesc(mapper);
        return this;
    }

    /**
     * Extracts a list then converts the list to a list of comparable values. Then
     * asserts the resultant list is in ascending order.
     * <p>
     * Value type must be a list.
     * 
     * @param mapper
     *               maps the values in the list to strings.
     * @return this test instance.
     */
    @SuppressWarnings("unchecked")
    public <V,W extends Comparable<W>> Test<T> isDesc(Function<V,W> mapper) {
        List<W> mapped = ((List<V>)value).stream().map(mapper).filter(v -> (v != null)).collect(Collectors.toList());
        if (mapped.size() <= 1)
            return this;
        W prior = mapped.get(0);
        for (int i = 1; i < mapped.size(); i++) {
            W current = mapped.get(i);
            if (prior.compareTo(current) < 0)
                Assertions.fail("List not in descending orderr");
            prior = current;
        }
        return this;
    }

    /**
     * Extracts a value and tests that it matched the expected value.
     * 
     * @param f
     *                 the extractor (to obtain the test value from the wrapped
     *                 object).
     * @param expected
     *                 the expected value.
     * @return this test instance.
     */
    public <V> Test<T> is(Function<T, V> f, V expected) {
        Assertions.assertEquals (expected, f.apply (value));
        return this;
    }

    /**
     * Extracts a value and tests that it does not match the "expected" value.
     * 
     * @param f
     *                 the extractor (to obtain the test value from the wrapped
     *                 object).
     * @param expected
     *                 the "expected" value (not to match).
     * @return this test instance.
     */
    public <V> Test<T> isNot(Function<T, V> f, V expected) {
        Assertions.assertNotEquals (expected, f.apply (value));
        return this;
    }

    /**
     * See {@link #is(Function, Object)} but for an enum.
     */
    public <E extends Enum<E>> Test<T> is(Function<T, E> f, E expected) {
        Assertions.assertEquals (expected, f.apply (value));
        return this;
    }

    /**
     * See {@link #isNot(Function, Object)} but for an enum.
     */
    public <E extends Enum<E>> Test<T> isNot(Function<T, E> f, E expected) {
        Assertions.assertNotEquals (expected, f.apply (value));
        return this;
    }

    /**
     * See {@link #is(Function, Object)} but for a string.
     */
    public Test<T> is(Function<T, String> f, String expected) {
        Assertions.assertEquals (expected, f.apply (value));
        return this;
    }

    /**
     * See {@link #isNot(Function, Object)} but for a string.
     */
    public Test<T> isNot(Function<T, String> f, String expected) {
        Assertions.assertNotEquals (expected, f.apply (value));
        return this;
    }

    /**
     * Performs a count on each of the matching items and asserts that count.
     * 
     * @param <V>
     *            the value type for matching.
     * @param f
     *            the extractor.
     * @param m
     *            the matcher.
     * @return this test instance.
     */
    public <V> Test<T> isCount(int expected, Function<T, List<V>> f, Predicate<V> m) {
        int count = 0;
        for (V item : f.apply (value)) {
            if (m.test (item))
                count++;
        }
        Assertions.assertEquals (expected, count);
        return this;
    }

    /**
     * Used to implement an if-else conditional.
     */
    protected static class IfElse<T> {

        private Test<T> with;

        private boolean evaluated = false;

        protected IfElse(Test<T> with) {
            this.with = with;
        }

        public IfElse<T> when(Function<T, Boolean> f, Consumer<Test<T>> action) {
            if (!evaluated) {
                // We can perform the action then mark as having been evaluated.
                if (f.apply (with.unwrap ())) {
                    action.accept (with);
                    evaluated = true;
                }
            }
            return this;
        }

        public Test<T> otherwise(Consumer<Test<T>> action) {
            if (!evaluated) {
                // We can perform the action.
                action.accept (with);
            }
            return with;
        }

        public Test<T> done() {
            return with;
        }
    }
}
