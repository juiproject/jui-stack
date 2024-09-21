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

import java.util.List;

/**
 * Encapsulates a remote method call in a cleanly abstracted manner.
 * 
 * @param <T>
 *            The base return type of the method.
 * @author Jeremy Buckley
 */
public interface IRemoteMethod<T> {

    /**
     * Invokes a remote method passing a call-back to be invoked when the method
     * has completed and objects to pass to the method.
     * 
     * @param context
     *            A context object (which may be {@code null}) that is passed
     *            through to the callback.
     * @param callback
     *            the callback method.
     * @param args
     *            the arguments to pass to the remote method.
     */
    public void invoke(IRemoteMethodCallback<T> callback, List<Object> args);

    // This was the previous method signature passing args as varargs. The problem
    // with this is that when varags appear as arguments themselves, without
    // concommitant arguments, they get consumed into the varargs here and treated
    // as separate arguments themselve. The resolution was to convert the args here
    // to a list.
    // public void invoke(IRemoteMethodCallback<T> callback, Object... args);

}
