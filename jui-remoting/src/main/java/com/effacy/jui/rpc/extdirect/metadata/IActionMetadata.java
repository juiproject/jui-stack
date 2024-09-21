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

import java.util.List;

/**
 * Represents meta-data for an action.
 * 
 * @author Jeremy Buckley
 */
public interface IActionMetadata {

    /**
     * Gets the name of the action.
     * 
     * @return The action name.
     */
    public String getActionName();


    /**
     * Gets meta-data about the methods the action provides.
     * 
     * @return A (non-{@code null}) list of method meta-data.
     */
    public List<IMethodMetadata> getMethodMetadata();


    /**
     * Gets meta-data about records that should be exposed. This is outside the
     * {@code Ext.Direct} specification for the remote API and is not included
     * in that definition, but generates separate JavaScript to declare records.
     * 
     * @return The list of records.
     */
    public List<IRecordMetadata> getRecordMetadata();

}
