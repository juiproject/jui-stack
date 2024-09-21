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

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IActionMetadata}.
 * 
 * @author Jeremy Buckley
 */
public class ActionMetadata implements IActionMetadata {

    /**
     * The name of the action.
     */
    protected String actionName;

    /**
     * The method meta-data associated to the action.
     */
    private List<IMethodMetadata> methodMetadata;

    /**
     * The record meta-data associated to the action.
     */
    private List<IRecordMetadata> recordMetadata;


    /**
     * Default construct. Name should be set explicitly.
     */
    protected ActionMetadata() {
        // Nothing.
    }


    /**
     * Construct with name and no initial meta-data.
     * 
     * @param actionName
     *            the name of the action.
     */
    public ActionMetadata(String actionName) {
        this.actionName = actionName;
    }


    /**
     * Construct with name and meta-data.
     * 
     * @param actionName
     *            the name of the action.
     * @param methodMetadata
     *            the method meta-data associated to the action.
     */
    public ActionMetadata(String actionName, List<IMethodMetadata> methodMetadata) {
        this.actionName = actionName;
        this.methodMetadata = methodMetadata;
    }


    /**
     * Construct with name and meta-data.
     * 
     * @param actionName
     *            the name of the action.
     * @param methodMetadata
     *            the method meta-data associated to the action.
     * @param recordMetadata
     *            the record meta-data associated to the action.
     * @param validationGroups
     *            the validation groups for the action.
     */
    public ActionMetadata(String actionName, List<IMethodMetadata> methodMetadata, List<IRecordMetadata> recordMetadata) {
        this.actionName = actionName;
        this.methodMetadata = methodMetadata;
        this.recordMetadata = recordMetadata;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IActionMetadata#getActionName()
     */
    @Override
    public String getActionName() {
        return actionName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IActionMetadata#getMethodMetadata()
     */
    @Override
    public List<IMethodMetadata> getMethodMetadata() {
        if (methodMetadata == null)
            methodMetadata = new ArrayList<IMethodMetadata> ();
        return methodMetadata;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IActionMetadata#getRecordMetadata()
     */
    @Override
    public List<IRecordMetadata> getRecordMetadata() {
        if (recordMetadata == null)
            recordMetadata = new ArrayList<IRecordMetadata> ();
        return recordMetadata;
    }

}
