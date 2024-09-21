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
package com.effacy.jui.rpc.client;

/**
 * Represents a method registry instance.
 * 
 * @author Jeremy Buckley
 */
public interface IMethodRegistry {

    /**
     * The number of registered methods.
     * 
     * @return The method count.
     */
    public int getMethodCount();


    /**
     * Register a method implementation.
     * 
     * @param action
     *            Name of action.
     * @param method
     *            Name of method.
     * @param impl
     *            Implementation to register.
     */
    public void register(String action, String method, IRemoteMethod<?> impl);


    /**
     * Get a method implementation with the given action and method name.
     * 
     * @param action
     *            Name of action.
     * @param method
     *            Name of method.
     * @return The method, or if no such method has been registered, return a
     *         method that always responds with
     *         {@link RemoteResponseType#NOT_IMPLEMENTED}.
     */
    public <T> IRemoteMethod<T> lookup(String action, String method);
}
