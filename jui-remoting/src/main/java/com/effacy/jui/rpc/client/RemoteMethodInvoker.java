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

import java.util.ArrayList;
import java.util.List;

/**
 * An invoker for a remote method. This allows one to package up arguments to
 * pass.
 * 
 * @author Jeremy Buckley
 */
public class RemoteMethodInvoker<T> extends RemoteMethod<T> {

    /**
     * The remote method.
     */
    private IRemoteMethod<T> method;

    /**
     * The arguments to pass.
     */
    private List<Object> arguments;


    /**
     * Construct with a method and arguments.
     * 
     * @param method
     *            the method.
     * @param arguments
     *            the arguments to pass.
     */
    public RemoteMethodInvoker(IRemoteMethod<T> method, List<Object> arguments) {
        this.method = method;
        this.arguments = (arguments == null) ? new ArrayList<>() : arguments;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.remote.client.IRemoteMethod#invoke(com.effacy.jui.remote.client.IRemoteMethodCallback,
     *      java.lang.Object[])
     */
    @Override
    public void invoke(IRemoteMethodCallback<T> callback, List<Object> args) {
        // Here was pass through our arguments from the constructor.
        this.method.invoke (callback, this.arguments);
    }

}
