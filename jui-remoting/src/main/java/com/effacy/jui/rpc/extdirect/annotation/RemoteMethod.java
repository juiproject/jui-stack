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

import jakarta.servlet.http.HttpServletRequest;

/**
 * Used to indicate that a method is to be made remotely accessible. This will
 * result in ExtJS creating a proxy JavaScript function that can be used to call
 * this method. The exact signature of this function will depend on the value of
 * {@link #formHandler()}. If this is {@code false} then the function will
 * expect to be passed arguments that match the parameter list of the method
 * (less any Servlet arguments such as {@link HttpServletRequest}). Following
 * this argument list is a callback function and an optional scope to call the
 * callback in. The callback function should expect to be passed a result object
 * and an instance of {@code Ext.Direct.RemotingEvent}. For the case where
 * {@link #formHandler()} is {@code true} then the function will expect to be
 * passed a form, callback and scope. If the forms encryption type is
 * {@code "multipart/form-data"} then it will be treated as an upload.
 * 
 * @author Jeremy Buckley
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RemoteMethod {

    /**
     * The method name to use for this method. Defaults to the declared method
     * name (full method name).
     * 
     * @return The name of the action.
     */
    String name() default "";


    /**
     * Indicates if the method is a form handler so should be passed form data.
     * 
     * @return {@code true} if is a form handler.
     */
    boolean formHandler() default false;


    /**
     * Defines the transaction mode that should be employed (default is
     * {@link TransactionMode#NONE}.
     * 
     * @return the transaction mode.
     */
    TransactionMode transaction() default TransactionMode.NONE;


    /**
     * The validation groups that apply to this remote action (used only when
     * the signature beans employ validation).
     * 
     * @return the validation groups.
     */
    Class<?> [] validationGroups() default {};


    /**
     * The validation prefix to use when mapping violation constraints to field
     * messages.
     * 
     * @return the validation prefix (default is the empty string).
     */
    String validationPrefix() default "";


    /**
     * An optional error handler class.
     * 
     * @return the error handler class to instantiate and use.
     */
    Class<? extends IActionErrorHandler> errorHandler() default IActionErrorHandler.class;

}
