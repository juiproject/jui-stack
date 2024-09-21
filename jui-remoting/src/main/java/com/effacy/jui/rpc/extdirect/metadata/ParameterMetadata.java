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
 * Implementation of {@link IParameterMetadata}.
 * 
 * @author Jeremy Buckley
 */
public class ParameterMetadata implements IParameterMetadata {

    /**
     * The parameter name.
     */
    private String parameterName;

    /**
     * The parameter type.
     */
    private Class<?> parameterType;


    /**
     * Constructs with name and type.
     * 
     * @param parameterName
     *            the parameter name.
     * @param parameterType
     *            the parameter type.
     */
    public ParameterMetadata(String parameterName, Class<?> parameterType) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IParameterMetadata#getParameterName()
     */
    @Override
    public String getParameterName() {
        return parameterName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IParameterMetadata#getParameterType()
     */
    @Override
    public Class<?> getParameterType() {
        return parameterType;
    }

}
