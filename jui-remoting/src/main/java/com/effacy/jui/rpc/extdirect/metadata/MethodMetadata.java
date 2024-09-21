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
 * Concrete implementation of {@link IMethodMetadata}.
 * 
 * @author Jeremy Buckley
 */
public class MethodMetadata implements IMethodMetadata {

    /**
     * Transaction modes supported.
     */
    public enum TransactionMode {
        NONE, READ_ONLY, READ_WRITE;
    }

    /**
     * The method name.
     */
    private String methodName;

    /**
     * If is a form handler.
     */
    private boolean formHandler = false;;

    /**
     * The argument types.
     */
    private List<IParameterMetadata> parameters;

    /**
     * The validation groups for the action.
     */
    private List<Class<?>> validationGroups;

    /**
     * The prefix to use for validation mapping (if any).
     */
    private String validationPrefix = "";

    /**
     * If the method requires a transaction.
     */
    private TransactionMode transaction = TransactionMode.NONE;


    /**
     * Constructs the meta-data.
     * 
     * @param methodName
     *            the method name.
     * @param formHandler
     *            if is a form handler.
     * @param parameters
     *            the parameters.
     * @param validationGroups
     *            the validation groups for the action.
     * @param validationPrefix
     *            the prefix to use for validation mapping (if any).
     * @param transactional
     *            if the method requires a transaction.
     */
    public MethodMetadata(String methodName, boolean formHandler, List<IParameterMetadata> parameters, List<Class<?>> validationGroups, String validationPrefix, TransactionMode transaction) {
        this.methodName = methodName;
        this.formHandler = formHandler;
        this.parameters = parameters;
        this.validationGroups = validationGroups;
        this.validationPrefix = (validationPrefix == null) ? "" : validationPrefix.trim ();
        this.transaction = (transaction == null) ? TransactionMode.NONE : transaction;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#getMethodName()
     */
    public String getMethodName() {
        return methodName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#isFormHandler()
     */
    @Override
    public boolean isFormHandler() {
        return formHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#getParameterMetadata()
     */
    @Override
    public List<IParameterMetadata> getParameterMetadata() {
        if (parameters == null)
            parameters = new ArrayList<IParameterMetadata> ();
        return parameters;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#getValidationGroups()
     */
    @Override
    public List<Class<?>> getValidationGroups() {
        if (validationGroups == null)
            validationGroups = new ArrayList<Class<?>> ();
        return validationGroups;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#getValidationPrefix()
     */
    @Override
    public String getValidationPrefix() {
        return validationPrefix;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#isTransactional()
     */
    public boolean isTransactional() {
        return !TransactionMode.NONE.equals (transaction);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.com.effacy.jui.rpc.extdirect.metadata.IMethodMetadata#isTransactionReadOnly()
     */
    @Override
    public boolean isTransactionReadOnly() {
        return TransactionMode.READ_ONLY.equals (transaction);
    }

}
