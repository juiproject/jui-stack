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
package com.effacy.jui.json;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Support for building out DTO's.
 * <p>
 * Since building DTO's can be awkward in layout one can bring to bear the
 * expressive advantages of lambda expressions. In this way one can introduce a
 * natural nesting in a manner that better scopes what aspects of the DTO one is
 * populating.
 */
public final class Builder {

    /**
     * Given a list of objects and an instance of the object type, add that instance
     * to the list and pass configuration of the instance to any provided builder
     * expression.
     * 
     * @param list
     *                 the list to add to.
     * @param instance
     *                 the instance to add and build out.
     * @param builder
     *                 to build out the instance.
     * @return the instance.
     */
    public static <V> V add(List<V> list, V instance, Consumer<V> builder ) {
        if ((instance != null) && (builder != null))
            builder.accept (instance);
        list.add (instance);
        return instance;    
    }

    /**
     * Give a setter (i.e. <code><dto::setId</code>) and an instance of the setter
     * type, assign that instance to (via the setter) and pass configuration of the
     * instance to any provided builder expression.
     * 
     * @param setter
     *                 the setter to invoke.
     * @param instance
     *                 the instance to add and build out.
     * @param builder
     *                 to build out the instance.
     * @return the instance.
     */
    public static <V> V set(Consumer<V> setter, V instance, Consumer<V> builder) {
        if (builder != null)
            builder.accept (instance);
        setter.accept (instance);
        return instance;  
    }

    /**
     * See {@link #add(Consumer, Object, Consumer)} but conditions on the passed
     * source (if the source is {@code null} then the add will not proceed). The
     * source is passed through to the builder as the second instance.
     * 
     * @param setter
     *                 the setter to invoke.
     * @param source
     *                 the source being mapped.
     * @param instance
     *                 the instance to add and build out.
     * @param builder
     *                 to build out the instance.
     * @return the instance.
     */
    public static <V,S> V set(Consumer<V> setter, S source, V instance, BiConsumer<V,S> builder) {
        if (source == null)
            return null;
        if (builder != null)
            builder.accept (instance, source);
        setter.accept (instance);
        return instance;  
    }

    public static <V> V set(Consumer<V> setter, V value) {
        setter.accept(value);
        return value;
    }

    /**
     * Given an instance of some type, pass that instance to any provided builder
     * expression.
     * 
     * @param instance
     *                 the instance to build out.
     * @param builder
     *                 to build out the instance.
     * @return the instance.
     */
    public static <V> V build(V instance, Consumer<V> builder ) {
        if (builder != null)
            builder.accept (instance);
        return instance;    
    }
}
