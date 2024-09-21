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
package com.effacy.jui.core.client.state;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.effacy.jui.platform.util.client.Logger;

public class LifecycleStateVariable<V extends LifecycleStateVariable<V>> extends StateVariable<V> {

    /**
     * A default error message. This can be set statically.
     */
    public static String DEFAULT_MESSAGE = "Unknown system error, please try again later.";

    /**
     * The possible states of the variable.
     */
    public enum State {
        LOADING, ERROR, UNEXPECTED, OK, INVALIDATED;
    }

    /**
     * The current state.
     */
    protected State state = State.LOADING;

    /**
     * Errors while in the error state.
     */
    private List<String> errors = new ArrayList<>();

    protected LifecycleStateVariable() {
        // Nothing.
    }

    protected LifecycleStateVariable(State state) {
        if (state != null)
            this.state = state;
    }

    /**
     * See {@link StateVariable#onModify()}.
     * <p>
     * When invoked transitions the state to {@link State#OK}.
     */
    @Override
    protected void onModify() {
        this.state = State.OK;
    }

    /**
     * The state.
     */
    public State state() {
        return state;
    }

    /**
     * If the state is loading.
     * 
     * @return {@code true} if it is.
     */
    public boolean isLoading() {
        return State.LOADING == state;
    }
    
    /**
     * If the state is in error.
     * 
     * @return {@code true} if it is.
     */
    public boolean isError() {
        return State.ERROR == state;    
    }
    
    /**
     * If the state is unexpected for some reason that is not related to a
     * definitive (i.e. reported) error.
     * 
     * @return {@code true} if it is.
     */
    public boolean isUnexpected() {
        return State.UNEXPECTED == state;
    }

    /**
     * Determines if the state is invalidated.
     * 
     * @return {@code true} if it is.
     */
    public boolean isInvalidated() {
        return State.INVALIDATED == state;
    }

    /**
     * If the state has an affirmed variable.
     * 
     * @return {@code true} if it is.
     */
    public boolean isOK() {
        return State.OK == state;
    }

    /**
     * Obtains the errors as recorded against the state variable.
     * 
     * @return the errors.
     */
    public List<String> errors() {
        return errors;
    }

    /**
     * Marks as OK leaving the value unchanged.
     */
    public void ok() {
        ok (false);
    }

    /**
     * See {@link #ok()}.
     * 
     * @param force
     *              forces an emit event if not changed.
     */
    public void ok(boolean force) {
        if (state == State.OK) {
            if (debug)
                Logger.log ("{state:ok_already} [" + toString() + "]");
            if (force)
                emit ();
        } else {
            if (debug)
                Logger.log ("{state:ok} [" + toString() + "]");
            state = State.OK;
            emit ();
        }
    }

    /**
     * See {@link #error(List, String)} but with no remote response.
     * 
     * @param errors
     *               the errors to register.
     */
    public void error(String... errors) {
        List<String> lerrors = new ArrayList<> ();
        for (String error : errors) {
            if (error != null)
                lerrors.add (error);
        }
        error (lerrors, (String) null);
    }

    /**
     * See {@link #error(List, String)} but with no remote response.
     * 
     * @param errors
     *               the errors to register.
     */
    public void error(List<String> errors) {
        error (errors, (String) null);
    }

    /**
     * See {@link #error(List, String)} but with no remote response.
     * 
     * @param errors
     *               the errors to register.
     * @param messageMapper
     *                      to map errors to messages.
     */
    public <E> void error(List<E> errors, Function<E,String> messageMapper) {
        error (convert (errors, messageMapper), (String) null);
    }

    /**
     * Transitions to the error state.
     * <p>
     * After this method has been called {@link #isError()} will return
     * {@code true}. The passed
     * errors will be available by a call to {@link #errors()}. If there are no
     * errors passed an error will be generated.
     * 
     * @param errors
     *                     the errors to register.
     * @param defaultError
     *                     a default error message if the passed errors are
     *                     {@code null} or empty.
     */
    public <E> void error(List<E> errors, Function<E,String> messageMapper, String defaultError) {
        error (convert (errors, messageMapper), defaultError);
    }

    /**
     * Transitions to the error state.
     * <p>
     * After this method has been called {@link #isError()} will return
     * {@code true}. The passed
     * errors will be available by a call to {@link #errors()}. If there are no
     * errors passed an error will be generated.
     * 
     * @param errors
     *                     the errors to register.
     * @param defaultError
     *                     a default error message if the passed errors are
     *                     {@code null} or empty.
     */
    public void error(List<String> errors, String defaultError) {
        if (debug)
            Logger.log ("{state:error} [" + toString() + "]");
        state = State.ERROR;
        this.errors.clear ();
        if ((errors == null) || errors.isEmpty()) {
            if (defaultError != null)
                this.errors.add (defaultError);
            else
                this.errors.add (DEFAULT_MESSAGE);
        } else {
            this.errors.addAll (errors);
        }
        emit ();
    }

    /**
     * Transition to the unexpected state.
     * <p>
     * After this method has been called {@link #isUnexpected()} will return
     * {@code true}.
     */
    public void unexpected() {
        unexpected (false);
    }

    /**
     * See {@link #unexpected()}.
     * 
     * @param force
     *              forces an emit event if not changed.
     */
    public void unexpected(boolean force) {
        if (State.UNEXPECTED == state) {
            if (debug)
                Logger.log ("{state:unexpected_already} [" + toString() + "]");
            if (force)
                emit ();
        } else {
            if (debug)
                Logger.log ("{state:unexpected} [" + toString() + "]");
            state = State.UNEXPECTED;
            emit ();
        }
    }

    /**
     * Transition to the loading state.
     * <p>
     * After this method has been called {@link #isLoading()} will return
     * {@code true}.
     */
    public void loading() {
        loading (false);
    }

    /**
     * See {@link #loading()}.
     * 
     * @param force
     *              forces an emit event if not changed.
     */
    public void loading(boolean force) {
        if (State.LOADING == state) {
            if (debug)
                Logger.log ("{state:loading_already} [" + toString() + "]");
            if (force)
                emit ();
        } else {
            if (debug)
                Logger.log ("{state:loading} [" + toString() + "]");
            state = State.LOADING;
            emit ();
        }
    }

    /**
     * Transition to the invalidated state.
     * <p>
     * After this method has been called {@link #isInvalidated()} will return
     * {@code true}.
     */
    public void invalidate() {
        invalidate (false);
    }

    /**
     * See {@link #invalidate()}.
     * 
     * @param force
     *              forces an emit event if not changed.
     */
    public void invalidate(boolean force) {
        if (State.INVALIDATED == state) {
            if (debug)
                Logger.log ("{state:invalidate_already} [" + toString() + "]");
            if (force)
                emit ();
        } else {
            if (debug)
                Logger.log ("{state:invalidate} [" + toString() + "]");
            state = State.INVALIDATED;
            emit ();
        }
    }

    /**
     * Convenience to convert a list of objects to error messages.
     * 
     * @param <V>
     * @param errors
     *                      the errors to convert.
     * @param messageMapper
     *                      to map errors to messages.
     * @return the mapped values.
     */
    public static <V> List<String> convert(List<V> errors, Function<V,String> messageMapper) {
        List<String> results = new ArrayList<> ();
        if ((errors == null) || errors.isEmpty())
            return results;
        errors.forEach (error -> results.add (messageMapper.apply (error)));
        return results;
    }

    @Override
    public String toString() {
        return super.toString() + "::" + ((state == null) ? "NULL" : state.name ());
    }
}
