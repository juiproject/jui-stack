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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

import com.effacy.jui.platform.core.JuiIncompatible;
import com.google.gwt.core.shared.GwtIncompatible;

/**
 * Converter that is able to convert from type S to T.
 * 
 * @author Jeremy Buckley
 */
@FunctionalInterface
public interface IConverter<S,T> {

    /**
     * Converts from the passed source object type to an instance of the target class type.
     * 
     * @param source
     *               the source object to convert.
     * @return the converted source.
     */
    public T convert(S source);

    /**
     * Convenience to create a converter.
     * <p>
     * The target type must have a non-argument constructor.
     * 
     * @param klass
     *               the klass of the target type.
     * @param mapper
     *               to map the source against an instantiated instance of the
     *               target type.
     * @return the converter.
     */
    @JuiIncompatible
    @GwtIncompatible
    @SuppressWarnings("unchecked")
    public static <S,T> IConverter<S,T> create(Class<T> klass, BiConsumer<S,T> mapper) {
        return new IExtendedConverter<S,T> () {

            @Override
            public T convert(S source) {
                Constructor<?> constructor;
                try {
                    constructor = klass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    T target = (T) constructor.newInstance();
                    apply(source, target);
                    return target;
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException (e);
                }
            }

            @Override
            public void apply(S object, T result) {
                mapper.accept(object, result);
            }

        };
    };

    /**
     * Extends the {@link IConverter} to include application of source data to an
     * existing target instance.
     * <p>
     * This can be useful for chaining.
     */
    public interface IExtendedConverter<S,T> extends IConverter<S,T> {

        /**
         * Applies the data from the source to an existing target.
         * 
         * @param source
         *               the source object.
         * @param target
         *               the target instance.
         */
        public void apply(S source, T target);
        
    }
}
