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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Collection of utilities for working with lists.
 */
public final class ListSupport {

    /**
     * Convenience for invoking {@link Itr#forEach(List, BiConsumer)}.
     */
    public static <V> void forEach(List<V> items, BiConsumer<Itr.IContext,V> visitor) {
        Itr.forEach (items, visitor);
    }

    /**
     * Converts a list from one type to another.
     * 
     * @param <A>
     *                  the from type.
     * @param <B>
     *                  the to type.
     * @param initial
     *                  the initial list to convert.
     * @param converter
     *                  the converting mapper from A to B.
     * @return the converted list.
     */
    public static <A,B> List<B> convert(List<A> initial, Function<A,B> converter) {
        List<B> fin = new ArrayList<>();
        if (initial != null)
            initial.forEach (item -> fin.add ((item == null) ? null : converter.apply (item)));
        return fin;
    }

    /**
     * Converts a list from one type to another.
     * 
     * @param <A>
     *             the base type.
     * @param list
     *             the list to filter.
     * @param test
     *             the test perform.
     * @return the filtered list.
     */
    public static <A> List<A> filter(List<A> list, Predicate<A> test) {
        List<A> fin = new ArrayList<>();
        if (list != null)
            list.forEach (item -> {
                if (test.test(item))
                    fin.add (item);
            });
        return fin;
    }

    /**
     * Finds the first instance of a specific element of the given list.
     * 
     * @param <A>
     *             the base type.
     * @param list
     *             the list to find in.
     * @param test
     *             the test to perform.
     * @return the first instance if found.
     */
    public static <A> Optional<A> find(List<A> list, Predicate<A> test) {
        if (list == null)
            return Optional.empty();
        for (A item : list) {
            if (test.test(item))
                return Optional.of (item);
        }
        return Optional.empty();
    }

    /**
     * Constructs a list from an array.
     * 
     * @param <T>
     *              the item type.
     * @param items
     *              the items to convert to a list.
     * @return the list.
     */
    @SafeVarargs
    public static <T> List<T> list(T... items) {
        List<T> list = new ArrayList<T> ();
        for (T item : items)
            list.add (item);
        return list;
    }

    /**
     * Contract a list of items as a comma-separated list of items.
     * 
     * @param items
     *              the items to contract.
     * @return the comma separated list.
     */
    public static <T> String contract(List<T> items) {
        return contract (items, ",");
    }

    /**
     * Contract a list of items as a separated list of items.
     * 
     * @param <T>
     * @param items
     *                  the items to contract.
     * @param separator
     *                  the separator to use.
     * @return the separated list.
     */
    public static <T> String contract(List<T> items, String separator) {
        String result = "";
        for (int i = 0, len = items.size (); i < len; i++) {
            if (i > 0)
                result += separator;
            result += (items.get (i) == null) ? "null" : items.get (i).toString ();
        }
        return result;
    }

    /**
     * Contract a list of items as a comma-separated list of items.
     * 
     * @param items
     *              the items to contract.
     * @param label
     *                  to map the item to a presentable string.
     * @return the comma separated list.
     */
    public static <T> String contract(List<T> items, Function<T,String> label) {
        return contract (items, label, ",");
    }

    /**
     * Contract a list of items as a separated list of items.
     * 
     * @param items
     *                  the items to contract.
     * @param label
     *                  to map the item to a presentable string.
     * @param separator
     *                  the separator to use.
     * @return the separated list.
     */
    public static <T> String contract(List<T> items, Function<T,String> label, String separator) {
        String result = "";
        if (label == null)
            label = (v -> v.toString ());
        for (int i = 0, len = items.size (); i < len; i++) {
            if (i > 0)
                result += separator;
            result += (items.get (i) == null) ? "null" : label.apply (items.get (i));
        }
        return result;
    }
    /**
     * Splits a string by the given separator. Leading and trailing separators are
     * ignored. Paired separators are treated as separating an empty component.
     * 
     * @param str
     *                  the string to split (it will be trimmed first).
     * @param separator
     *                  the separator to use.
     * @return the list of separated components.
     */
    public static List<String> split(String str, char separator) {
        List<String> results = new ArrayList<> ();

        // Safety trim.
        str = StringSupport.trim (str);

        // Remove any leading separator (so that it does not get counted).
        if (str.startsWith (String.valueOf (separator)))
            str = str.substring (1, str.length ());

        // If the result is empty we are done.
        if (str.isEmpty ())
            return results;

        // Split will generate empty content for paired separators, but not at the end
        // of the string. To accommodate for this we extend the string with a dummy
        // character that will yield the desired split but will introduce an extra
        // component that will need to be removed post-processing.
        boolean extended = false;
        if (str.endsWith (String.valueOf (separator))) {
            extended = true;
            str += "_";
        }

        // Process the split and accumulate in the results.
        for (String part : str.split ("\\" + String.valueOf (separator)))
            results.add (part);

        // Remove the last element if the string was extended.
        if (extended)
            results.remove (results.size () - 1);
        return results;
    }
    
    /**
     * Determines if the passed list contains the passed item under the given
     * equality matcher.
     * 
     * @param <A>
     *                the list type.
     * @param <B>
     *                the item type.
     * @param items
     *                the list of items to check containment within.
     * @param item
     *                the item to test if contains.
     * @param matcher
     *                the equality matcher.
     * @return if the list contains the item.
     */
    public static <A,B> boolean contains(List<A> items, B item, BiFunction<A,B,Boolean> matcher) {
        for (A itm : items) {
            if (matcher.apply(itm, item))
                return true;
        }
        return false;
    }

    /**
     * Obtains all those elements in the prior list that are not in the current
     * list.
     * 
     * @param prior
     *                the prior list of elements.
     * @param current
     *                the current list of elements.
     * @return the ones no longer in the prior.
     */
    public static <A> List<A> removed(List<A> prior, List<A> current) {
        return removed(prior, current, (a,b) -> a.equals (b));
    }

    /**
     * Obtains all those elements in the prior list that are not in the current
     * list.
     * 
     * @param prior
     *                the prior list of elements.
     * @param current
     *                the current list of elements.
     * @return the ones no longer in the prior.
     */
    public static <A,B> List<A> removed(List<A> prior, List<B> current, BiFunction<A,B,Boolean> matcher) {
        List<A> results = new ArrayList<> ();
        prior.forEach(item -> {
            if (!contains (current, item, (b,a) -> matcher.apply (a, b)))
                results.add (item);
        });
        return results;
    }

    /**
     * Obtains all those elements in the current list that are not in the prior
     * list.
     * 
     * @param prior
     *                the prior list of elements.
     * @param current
     *                the current list of elements.
     * @return the ones in the current but not in the prior.
     */
    public static <T> List<T> added(List<T> prior, List<T> current) {
        return removed (current, prior);
    }

    /**
     * Obtains all those elements in the current list that are not in the prior
     * list.
     * 
     * @param prior
     *                the prior list of elements.
     * @param current
     *                the current list of elements.
     * @return the ones no longer in the prior.
     */
    public static <A,B> List<B> added(List<A> prior, List<B> current, BiFunction<A,B,Boolean> matcher) {
        return removed (current, prior, (b,a) -> matcher.apply(a, b));
    }

    /**
     * No construct constructor.
     */
    private ListSupport() {
        // Nothing.
    }
}
