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
package com.effacy.jui.rpc.extdirect.client;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.json.annotation.JsonSerializable;

/**
 * Remote call wrapper for the ExtDirect protocol. This has been extended to
 * support cross site request forgery using double submit cookies.
 *
 * @author Jeremy Buckley
 */
@JsonSerializable
public class ExtDirectRemoteCall {

    /**
     * The action service.
     */
    private String action;

    /**
     * The method in the action service.
     */
    private String method;

    /**
     * The underlying protocol form.
     */
    private String type = "rpc";

    /**
     * Locally unique transaction reference count.
     */
    private int tid;

    /**
     * Cross site token.
     */
    private String csrfToken;

    /**
     * Parameter data being passed.
     */
    private List<Object> data = new ArrayList<Object> ();


    /**
     * Serialization constructor.
     */
    public ExtDirectRemoteCall() {
        // Nothing.
    }


    /**
     * Construct with core data.
     * 
     * @param action
     *            the action service.
     * @param method
     *            the method in the service.
     * @param tid
     *            the transaction ID that will be passed back in the response.
     * @param args
     *            the method parameters.
     */
    public ExtDirectRemoteCall(String action, String method, int tid, Object... args) {
        this (action, method, tid, null, args);
    }


    /**
     * Construct with core data.
     * 
     * @param action
     *            the action service.
     * @param method
     *            the method in the service.
     * @param tid
     *            the transaction ID that will be passed back in the response.
     * @param csrfToken
     *            CSRF token (optional).
     * @param args
     *            the method parameters.
     */
    public ExtDirectRemoteCall(String action, String method, int tid, String csrfToken, Object... args) {
        this.action = action;
        this.method = method;
        this.tid = tid;
        this.csrfToken = csrfToken;
        if (args != null) {
            for (Object arg : args)
                data.add (arg);
        }
    }


    /**
     * Obtains the action service being invoked.
     * 
     * @return the action service.
     */
    public String getAction() {
        return action;
    }


    /**
     * Obtains the method being invoked in the action service.
     * 
     * @return the method being invoked.
     */
    public String getMethod() {
        return method;
    }


    /**
     * Obtains the protocol sub-type. This is defined by the ExtDirect protocol
     * and is for future expansion. Currently just returns <code>"rpc"</code>.
     * 
     * @return the protocol sub-type.
     */
    public String getType() {
        return type;
    }


    /**
     * The transaction ID that will be returned with the response.
     * 
     * @return the trransaction ID (count).
     */
    public int getTid() {
        return tid;
    }


    /**
     * Obtains the CSRF token that the server should validate. This is optional.
     * 
     * @return the token.
     */
    public String getCsrfToken() {
        return csrfToken;
    }


    /**
     * Sets the CSRF token. See {@link #getCsrfToken()}.
     * 
     * @param csrfToken
     *            the token.
     */
    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }


    /**
     * Obtains the parameters passed.
     * 
     * @return the arguments.
     */
    public List<Object> getData() {
        if (data == null)
            data = new ArrayList<Object> ();
        return data;
    }


    /**
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }


    /**
     * @param method
     *            the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * @param tid
     *            the tid to set
     */
    public void setTid(int tid) {
        this.tid = tid;
    }


    /**
     * @param arguments
     *            the arguments to set
     */
    public void setData(List<Object> arguments) {
        this.data = arguments;
    }

}
