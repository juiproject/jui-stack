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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtproject.json.client.JSONObject;

import com.effacy.jui.json.client.Serializer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.rpc.extdirect.client.ExtDirectRemoteMethod;
import com.effacy.jui.rpc.extdirect.client.service.RemoteApi;
import com.effacy.jui.rpc.extdirect.client.service.RemoteApiMethod;

import elemental2.core.JsObject;

/**
 * A registry of {@link IRemoteMethod} methods, looked up by action and method.
 * 
 * @author Steve Baker
 */
public final class MethodRegistry implements IMethodRegistry {

    /**
     * Default instance.
     */
    private static MethodRegistry INSTANCE;

    /**
     * Default unimplemented method.
     */
    private static MockRemoteMethod<?> UNIMPLEMENTED_METHOD;

    /**
     * Map containing methods.
     */
    private final Map<String, IRemoteMethod<?>> registry = new HashMap<String, IRemoteMethod<?>> ();

    /**
     * Factory for creating remote methods.
     */
    private IRemoteMethodFactory factory;

    /**
     * Private constructor.
     */
    private MethodRegistry(IRemoteMethodFactory factory) {
        this.factory = factory;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.client.remote.client.IMethodRegistry#getMethodCount()
     */
    @Override
    public int getMethodCount() {
        return registry.size ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.client.remote.client.IMethodRegistry#register(java.lang.String,
     *      java.lang.String, com.effacy.jui.remote.client.IRemoteMethod)
     */
    @Override
    public void register(String action, String method, IRemoteMethod<?> impl) {
        String regKey = getRegKey (action, method);
        registry.put (regKey, impl);
    }


    /**
     * Registers all remote methods defined in the {@link RemoteApi}.
     * 
     * @param remoteApi
     *            Remote API containing method definitions.
     */
    public void registerRemoteApi(RemoteApi remoteApi) {
        for (String serviceName : remoteApi.getActions ().keySet ()) {
            List<RemoteApiMethod> actions = remoteApi.getActions ().get (serviceName);
            for (RemoteApiMethod method : actions)
                register (serviceName, method.getName (), (IRemoteMethod<Object>) factory.create (remoteApi.getUrl (), serviceName, method.getName (), method.getLen ()));
        }
    }


    /**
     * Gets a registration key based on the requested action and method.
     * 
     * @param action
     *            the action name.
     * @param method
     *            the method name in the action.
     * @return The lookup key for the pair.
     */
    private static String getRegKey(String action, String method) {
        return action + "::" + method;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.client.remote.client.IMethodRegistry#lookup(java.lang.String,
     *      java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> IRemoteMethod<T> lookup(String action, String method) {
        String regKey = getRegKey (action, method);
        IRemoteMethod<T> impl = (IRemoteMethod<T>) registry.get (regKey);
        if (impl != null)
            return impl;
        Logger.log ("No such service method " + regKey);
        return getUnimplementedMethod ();

    }


    /**
     * Gets an unimplemented method.
     * 
     * @param <T>
     *            the return type.
     * @return The method.
     */
    @SuppressWarnings("unchecked")
    private static <T> IRemoteMethod<T> getUnimplementedMethod() {
        if (UNIMPLEMENTED_METHOD == null) {
            UNIMPLEMENTED_METHOD = new MockRemoteMethod<T> () {

                @Override
                protected RemoteResponse<T> process(Object... args) {
                    return new RemoteResponse<T> (RemoteResponseType.ERROR_NOT_IMPLEMENTED);
                }

            };
        }
        return (IRemoteMethod<T>) UNIMPLEMENTED_METHOD;
    }


    /**
     * Registers a remote API structure.
     * 
     * @param remoteApiJs
     *            the remote API structure.
     */
    protected void registerRemote(JsObject remoteApiJs) {
        if (remoteApiJs != null) {
            JSONObject remoteApiJson = new JSONObject (remoteApiJs);
            RemoteApi remoteApi = Serializer.getInstance ().deSerialize (remoteApiJson, RemoteApi.class);
            registerRemoteApi (remoteApi);
        }
    }


    /**
     * Registers the remote methods. This expects to find a javascript variable
     * named {@code RemotingApi} declared prior to invocation. This is typically
     * obtained from inclusion of the remoting API GET method call against a
     * standard Ext Direct remoting stack.
     */
    public native void registerRemoteMethods()
    /*-{
      this.@com.effacy.jui.rpc.client.MethodRegistry::registerRemote(Lelemental2/core/JsObject;)($wnd.RemotingApi);
    }-*/;


    /**
     * Initialize the registry with the default (ExtDirect) method factory.
     */
    public static void init() {
        init (ExtDirectRemoteMethod.FACTORY);
    }


    /**
     * Initialize the registry with the specified method factory.
     * 
     * @param factory
     *            the factory.
     */
    public static void init(IRemoteMethodFactory factory) {
        if (factory == null)
            factory = ExtDirectRemoteMethod.FACTORY;
        if (INSTANCE != null)
            throw new IllegalStateException ("Registry already initialized.");
        INSTANCE = new MethodRegistry (factory);
        INSTANCE.registerRemoteMethods ();
    }


    /**
     * Gets the method registry. Note that if the registry has not been
     * initialized prior to calling this, it will be initialized with the
     * default (ExtDirect) method factory.
     * 
     * @return The method registry.
     */
    public static MethodRegistry getInstance() {
        if (INSTANCE == null)
            init ();
        return INSTANCE;
    }
}
