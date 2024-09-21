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
 * Represents meta-data for a method.
 * 
 * @author Jeremy Buckley
 */
public interface IMethodMetadata {

    /**
     * Gets the method name.
     * 
     * @return The method name.
     */
    public String getMethodName();


    /**
     * Determines if the method is a form handler.
     * 
     * @return If is a form handler.
     */
    public boolean isFormHandler();


    /**
     * Gets the types of arguments.
     * 
     * @return The types of the arguments.
     */
    public List<IParameterMetadata> getParameterMetadata();


    /**
     * These are optional validation groups to apply to the mapped form beans.
     * This allows a bean to be used across multiple actions where an action is
     * only interested in a portion of the fields in the bean.
     * 
     * @return The validation groups.
     */
    public List<Class<?>> getValidationGroups();


    /**
     * This is an optional (returns an empty string if not used) prefix to use
     * when mapping constraint violations to messages against fields.
     * 
     * @return The prefix to use (default should be empty string and never
     *         {@code null}).
     */
    public String getValidationPrefix();


    /**
     * Does the method call require a transaction.
     * 
     * @return {@code true} if the method does require a transaction to operate.
     */
    public boolean isTransactional();


    /**
     * Determines if the transaction can be read only.
     * 
     * @return {@code true} if when a transaction is required it may be read
     *         only.
     */
    public boolean isTransactionReadOnly();

}
