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
package com.effacy.jui.rpc.extdirect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.effacy.jui.rpc.extdirect.Router;

/**
 * Represents a special configuration property (on a router) to send back with
 * the provider API configuration meta-data. This is specified with a name and a
 * value. The value is processed as per
 * {@link Router#setRemotingConfig(java.util.Map)}.
 * <p>
 * Generally this is used in conjunction with {@link RemoteProperties}, but may
 * be used along.
 * 
 * @author Jeremy Buckley
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemoteProperty {

    /**
     * Specified the name of the property.
     * 
     * @return The property name.
     */
    String name();


    /**
     * Specifies the value of the property, which will be processed as per
     * {@link Router#setRemotingConfig(java.util.Map)}.
     * 
     * @return The property value.
     */
    String value();
}
