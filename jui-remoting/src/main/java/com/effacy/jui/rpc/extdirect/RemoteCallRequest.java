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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This captures an incoming request.
 * 
 * @author Jeremy Buckley
 */
public class RemoteCallRequest extends RemoteCall {

    /**
     * The data being passed.
     */
    private List<Object> data;

    /**
     * Failed message.
     */
    private String failMessage = null;


    /**
     * Default constructor.
     */
    public RemoteCallRequest() {
        super ();
    }


    /**
     * Copy constructor.
     * 
     * @param copy
     *            request to copy.
     */
    public RemoteCallRequest(RemoteCallRequest copy) {
        super (copy);
        this.failMessage = copy.failMessage;
        getData ().addAll (copy.getData ());
    }


    /**
     * Construct with data. Note that although no data is passed, the data will
     * be treated as an empty list of objects.
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
    public RemoteCallRequest(String action, String method, RemoteCallType type, int tid, String csrfToken) {
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
     * @param data
     *            the data.
     */
    public RemoteCallRequest(String action, String method, RemoteCallType type, int tid, String csrfToken, List<Object> data) {
        super (action, method, type, tid, csrfToken);
        setData (data);
    }


    /**
     * Gets the data list.
     * 
     * @return The data list (never {@code null}).
     */
    public List<Object> getData() {
        if (data == null)
            data = new ArrayList<Object> ();
        return data;
    }


    /**
     * Sets the data list.
     * 
     * @param data
     *            the data list.
     */
    public void setData(List<Object> data) {
        this.data = data;
    }


    /**
     * Fail the remote call for a specific reason. When {@link #validate()} is
     * called, is will throw an exception returning this message.
     * 
     * @param failMessage
     *            the fail message to return when {@link #validate()} is called.
     */
    public void fail(String failMessage) {
        this.failMessage = failMessage;
    }


    /**
     * Validates the call structure. This will throw an exception if
     * {@link #fail(String)} has been called and passed a message or if there is
     * no action, method or type specified.
     * 
     * @throws InvalidCallRequestException
     *             If the call does not validate.
     */
    public void validate() throws InvalidCallRequestException {
        if (failMessage != null)
            throw new InvalidCallRequestException (this, failMessage);
        if (getAction () == null)
            throw new InvalidCallRequestException (this, "No action specified.");
        if (getMethod () == null)
            throw new InvalidCallRequestException (this, "No method specified.");
        if (getType () == null)
            throw new InvalidCallRequestException (this, "No type specified.");
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.extdirect.RemoteCall#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        try {
            RemoteCallRequest castObj = (RemoteCallRequest) obj;
            if (!super.equals (castObj))
                return false;
            if (!super.safeCompare (getData (), castObj.getData ()))
                return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer ();
        sb.append ("action=\"");
        sb.append (getAction ());
        sb.append ("\", method=\"");
        sb.append (getMethod ());
        sb.append ("\", type=\"");
        sb.append (getType ());
        sb.append ("\", tid=");
        sb.append (getTid ());
        sb.append ("\", data=[");
        boolean bFirst = true;
        for (Object datum : getData ()) {
            if (bFirst)
                bFirst = false;
            else
                sb.append (',');
            if (datum == null) {
                sb.append ("null");
            } else if (!datum.getClass ().isArray ()) {
                sb.append ('\"');
                sb.append (datum.toString ());
                sb.append ('\"');
            } else {
                sb.append ("[");
                for (int i = 0, len = Array.getLength (datum); i < len; i++) {
                    if (i > 0)
                        sb.append (',');
                    Object elm = Array.get (datum, i);
                    if (elm == null)
                        sb.append ("null");
                    else
                        sb.append (elm.toString ());
                }
                sb.append ("]");
            }
        }
        sb.append (']');
        return sb.toString ();
    }
}
