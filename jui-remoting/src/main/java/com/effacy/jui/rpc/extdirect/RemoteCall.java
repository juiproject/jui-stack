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
 * This represents a single (un-batched) remote Ext Direct call. This is really
 * intended to be extended to incorporate data.
 * 
 * @author Jeremy Buckley
 */
public abstract class RemoteCall {

    /**
     * The action being invoked.
     */
    private String action;

    /**
     * The method being invoked.
     */
    private String method;

    /**
     * The remote call type.
     */
    private RemoteCallType type;

    /**
     * The transaction ID.
     */
    private int tid;

    /**
     * Any CSRF token.
     */
    private String csrfToken;


    /**
     * Default constructor.
     */
    public RemoteCall() {
        // Nothing.
    }


    /**
     * Copy constructor.
     * 
     * @param copy
     *            the call to copy.
     */
    public RemoteCall(RemoteCall copy) {
        setAction (copy.getAction ());
        setMethod (copy.getMethod ());
        setType (copy.getType ());
        setTid (copy.getTid ());
        setCsrfToken (copy.getCsrfToken ());
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
     * @param csrfToken
     *            the CSRF token.
     */
    public RemoteCall(String action, String method, RemoteCallType type, int tid, String csrfToken) {
        setAction (action);
        setMethod (method);
        setType (type);
        setCsrfToken (csrfToken);
        setTid (tid);
    }


    /**
     * Gets the action name.
     * 
     * @return The action name.
     */
    public String getAction() {
        return action;
    }


    /**
     * Sets the action name.
     * 
     * @param action
     *            the action name.
     */
    public void setAction(String action) {
        this.action = action;
    }


    /**
     * Gets the method name.
     * 
     * @return The method name.
     */
    public String getMethod() {
        return method;
    }


    /**
     * Sets the method name.
     * 
     * @param method
     *            the method name.
     */
    public void setMethod(String method) {
        this.method = method;
    }


    /**
     * Gets the call type.
     * 
     * @return The call type.
     */
    public RemoteCallType getType() {
        return type;
    }


    /**
     * Sets the call type.
     * 
     * @param type
     *            the call type.
     */
    public void setType(RemoteCallType type) {
        this.type = type;
    }


    /**
     * Gets the transaction ID.
     * 
     * @return The transaction ID.
     */
    public int getTid() {
        return tid;
    }


    /**
     * Sets the transaction ID.
     * 
     * @param tid
     *            the transaction ID.
     */
    public void setTid(int tid) {
        this.tid = tid;
    }


    /**
     * Gets any CSRF token supplied.
     * 
     * @return the token.
     */
    public String getCsrfToken() {
        return csrfToken;
    }


    /**
     * Sets the CSRF token.
     * 
     * @param csrfToken
     *            the token.
     */
    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int v1 = (this.action == null) ? 0 : this.action.hashCode ();
        int v2 = (this.method == null) ? 0 : this.method.hashCode ();
        return v1 + v2;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        try {
            RemoteCall castObj = (RemoteCall) obj;
            if (!safeCompare (getAction (), castObj.getAction ()))
                return false;
            if (!safeCompare (getMethod (), castObj.getMethod ()))
                return false;
            if (getTid () != castObj.getTid ())
                return false;
            if (!safeCompare (getType (), castObj.getType ()))
                return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }


    /**
     * Performs a safe object comparison.
     * 
     * @param obj1
     *            the first object.
     * @param obj2
     *            the second object.
     * @return If the object are identical (including {@code null}).
     */
    protected boolean safeCompare(Object obj1, Object obj2) {
        if (obj1 == obj2)
            return true;
        if ((obj1 == null) || (obj2 == null))
            return false;
        return obj1.equals (obj2);
    }

}
