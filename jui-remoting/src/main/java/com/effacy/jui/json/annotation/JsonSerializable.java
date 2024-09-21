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
package com.effacy.jui.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.effacy.jui.json.rebind.SerializationGenerator;

/**
 * A convenient annotation to apply to all classes that need to be serialized
 * (or to interfaces of such classes). This tells the
 * {@link SerializationGenerator} to create a serializer (and de-serializer) for
 * the class.
 * 
 * @author Jeremy Buckley
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonSerializable {

    /**
     * Determines if setters are required (if not then the serializer will
     * processes properties based only on existence of getters).
     * 
     * @return {@code true} if setters are required to determine properties (not
     *         just getters) - default is {@code true}.
     */
    public boolean settersRequired() default true;


    /**
     * Defines the type assignment strategy (this results in an addition field
     * <code>_type</code> being inserted into the resulting JSON with a value
     * that is determined by the mode selected).
     * <p>
     * The type strategy will be inherited by all sub-classes (and implementing
     * classes in the case of the annotation being applied to an interface).
     * 
     * @return the type assignment strategy to use (deafult is
     *         {@link TypeMode#NONE}).
     */
    public TypeMode type() default TypeMode.NONE;

}
