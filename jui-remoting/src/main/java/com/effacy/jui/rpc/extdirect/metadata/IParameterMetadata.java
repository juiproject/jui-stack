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
package com.effacy.jui.rpc.extdirect.metadata;

/**
 * Meta-data for a method parameter. Note that currently the parameter name is
 * not currently used and is in place to support a potential later development
 * by Ext Direct to pass associative arrays as parameters.
 * 
 * @author Jeremy Buckley
 */
public interface IParameterMetadata {

    /**
     * Gets the name of the parameter.
     * 
     * @return The parameter name.
     */
    public String getParameterName();


    /**
     * Gets the type of parameter.
     * 
     * @return The parameter type.
     */
    public Class<?> getParameterType();
}
