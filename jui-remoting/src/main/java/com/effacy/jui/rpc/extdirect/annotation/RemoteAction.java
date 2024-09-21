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

import com.effacy.jui.rpc.extdirect.IActionErrorHandler;
import com.effacy.jui.rpc.extdirect.metadata.MethodMetadata.TransactionMode;

/**
 * Use to make a type as being a remote action. Use this to specify an action
 * name (rather than the default).
 * 
 * @author Jeremy Buckley
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemoteAction {

    /**
     * The action name to use for this action. Defaults to the (simple) name of
     * the type.
     * 
     * @return The name of the action.
     */
    String name() default "";


    /**
     * Defines the transaction mode that should be employed (default is
     * {@link TransactionMode#NONE}.
     * 
     * @return the transaction mode.
     */
    TransactionMode transaction() default TransactionMode.NONE;


    /**
     * An optional error handler class.
     * 
     * @return The error handler class to instantiate and use.
     */
    Class<? extends IActionErrorHandler> errorHandler() default IActionErrorHandler.class;


    /**
     * The validation prefix to use when mapping violation constraints to field
     * messages. This will apply to all methods unless the method provides an
     * override.
     * 
     * @return The validation prefix (default is the empty string).
     */
    String validationPrefix() default "";

}
