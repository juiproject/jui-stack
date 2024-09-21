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
package com.effacy.jui.rpc.extdirect;


/**
 * Remote call response DTO.
 * 
 * @author Jeremy Buckley
 */
public class RemoteCallResponse extends RemoteCall {

    /**
     * The result of the response.
     */
    private Object result;


    /**
     * Default constructor.
     */
    public RemoteCallResponse() {
        // Nothing.
    }


    /**
     * Copy constructor (from a call).
     * 
     * @param call
     *            call to copy.
     */
    public RemoteCallResponse(RemoteCall call) {
        super (call);
    }


    /**
     * Copy constructor.
     * 
     * @param copy
     *            response to copy.
     */
    public RemoteCallResponse(RemoteCallResponse copy) {
        super (copy);
    }


    /**
     * Construct with a remote call base to copy and a result to return.
     * 
     * @param call
     *            the remote call base to copy.
     * @param result
     *            the result.
     */
    public RemoteCallResponse(RemoteCall call, Object result) {
        super (call);
        setResult (result);
    }


    /**
     * Construct with data.
     * 
     * @param action
     *            the action name.
     * @param method
     *            the method name.
     * @param type
     *            the call type.
     * @param tid
     *            the TID.
     */
    public RemoteCallResponse(String action, String method, RemoteCallType type, int tid, String csrfToken) {
        super (action, method, type, tid, csrfToken);
    }


    /**
     * Construct with data.
     * 
     * @param action
     *            the action name.
     * @param method
     *            the method name.
     * @param type
     *            the call type.
     * @param tid
     *            the TID.
     * @param result
     *            the result.
     */
    public RemoteCallResponse(String action, String method, RemoteCallType type, int tid, String csrfToken, Object result) {
        super (action, method, type, tid, csrfToken);
        setResult (result);
    }


    /**
     * Gets the result.
     * 
     * @return The result.
     */
    public Object getResult() {
        return result;
    }


    /**
     * Sets the result.
     * 
     * @param result
     *            the result.
     */
    public void setResult(Object result) {
        this.result = result;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.RemoteCall#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        try {
            RemoteCallResponse castObj = (RemoteCallResponse) obj;
            if (!super.equals (castObj))
                return false;
            if (!super.safeCompare (getResult (), castObj.getResult ()))
                return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
