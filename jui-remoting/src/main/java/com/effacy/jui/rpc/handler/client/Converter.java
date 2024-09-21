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
package com.effacy.jui.rpc.handler.client;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.rpc.handler.client.query.IPageQuery;

public final class Converter {

    /**
     * Converts a list of source objects to a list of corresponding target
     * objects.
     * 
     * @param <T>
     *            the target type.
     * @param <S>
     *            the source type.
     * @param source
     *            the source list to convert.
     * @param converter
     *            the converter to use.
     * @return The converted listed.
     */
    public static <T, S> List<T> convert(Iterable<S> source, IConverter<S, T> converter) {
        List<T> results = new ArrayList<T> ();
        for (S item : source)
            results.add (converter.convert (item));
        return results;
    }


    /**
     * Converts a list of source objects to a list of corresponding target
     * objects while respecting the pagination specification against the source
     * list.
     * 
     * @param <T>
     *            the target type.
     * @param <S>
     *            the source type.
     * @param source
     *            the source list to convert.
     * @param converter
     *            the converter to use.
     * @param pagination
     *            the pagination specification.
     * @return The converted and confined resulting list.
     */
    public static <T, S> List<T> convert(Iterable<S> source, IConverter<S, T> converter, IPageQuery pagination) {
        if ((pagination == null) || pagination.isUnlimited ())
            return convert (source, converter);

        List<T> results = new ArrayList<T> ();
        int i = 0;
        int start = pagination.startIndex ();
        int end = pagination.endIndex ();
        for (S item : source) {
            if ((i >= start) && (i <= end))
                results.add (converter.convert (item));
            i++;
            if (i > end)
                break;
        }
        return results;
    }


    /**
     * Private constructor.
     */
    private Converter() {
        // Nothing.
    }
}
