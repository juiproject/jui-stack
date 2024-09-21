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
package com.effacy.jui.core.client.state.redux;

import java.util.function.Consumer;

public class Action {

    public static Action $(String type, Object payload, Consumer<IFailureReason> failure) {
        return new Action (type, payload, failure);
    }

    public static Action $(String type, Object payload) {
        return new Action (type, payload, null);
    }

    public static Action $(String type, Consumer<IFailureReason> failure) {
        return new Action (type, null, failure);
    }

    public static Action $(String type) {
        return new Action (type, null, null);
    }

    /**
     * The type path (see also {@link #type()} and {@link #next()}).
     */
    private String[] type;

    /**
     * See {@link #payload()}.
     */
    private Object payload;

    /**
     * See {@link #fail(IFailureReason)}.
     */
    private Consumer<IFailureReason> failure;

    /**
     * Construct an action.
     * @param type the action type (slash separated path).
     * @param payload (optional) and applicable payload.
     * @param failure (optional) callback for any failure.
     */
    public Action(String type, Object payload, Consumer<IFailureReason> failure) {
        this ((type == null) ? new String [0] : type.split ("/"), payload, failure);
    }

    /**
     * See {@link #Action(String, Object, Consumer)} except that the type is
     * provided as an array.
     */
    public Action(String[]type, Object payload, Consumer<IFailureReason> failure) {
        this.type = (type == null) ? new String [0] : type;
        this.payload = payload;
        this.failure = failure;
    }

    /**
     * The type of action.
     * <p>
     * Where the type has been provided as a path this is the first element on the
     * path. If the path is empty this will be {@code null}.
     * 
     * @return type the type of action.
     */
    public String type() {
        if (type.length == 0)
            return null;
        return type[0];
    }

    /**
     * The payload associated with the action.
     * 
     * @return payload the payload.
     */
    @SuppressWarnings("unchecked")
    public <V> V payload() {
        return (V) payload;
    }

    /**
     * Determines the length of the type path. If this is 1 then we have reached the
     * end of the specification.
     * 
     * @return the length.
     */
    public int length() {
        return type.length;
    }

    /**
     * Gets a version of the action that is next-in-line along the type path.
     * <p>
     * The change is actually applied to this instance so what is returned is actually {@code this}.
     * 
     * @return the next (advanced) action.
     */
    public Action next() {
        this.type = reduce (this.type);
        return this;
    }

    /**
     * Invoked when there is a failure.
     * 
     * @param reason the reason for the failure.
     */
    void fail(IFailureReason reason) {
        if (failure != null)
            failure.accept (reason);
    }

    /**
     * Convenience to remove the first element from the passed array.
     * <p>
     * If the array is {@code null} or empty the an empty array is returned.
     * 
     * @param path the path array.
     * @return the reduced form with the first element removed.
     */
    public static String[] reduce(String[] path) {
        if ((path == null) || (path.length == 0))
            return new String[0];
        String [] split = new String [path.length - 1];
        for (int i = 1;  i < path.length; i++)
            split[i-1] = path[i];
        return split;
    }
}
