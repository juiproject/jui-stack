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

import com.effacy.jui.core.client.state.redux.State.ActionOutcome;
import com.effacy.jui.core.client.state.redux.State.Outcome;

@FunctionalInterface
public interface Reducer<S> {

    /**
     * Applies an action to the passed state.
     * <p>
     * Any changes should (generally) only act on a clone of the state.
     *
     * @param state
     *               the state to apply to.
     * @param action
     *               the action to apply.
     * @return the outcome of the action.
     */
    public ReducerOutcome<S> reduce(S state, Action action);

    /**
     * Construct a successful outcome with the revised state.
     * 
     * @param <S>   the state type.
     * @param state
     *              the revised state.
     * @return the outcome.
     */
    public static <S> ReducerOutcome<S> success(S state) {
        ReducerOutcome<S> outcome = new ReducerOutcome<S>();
        outcome.outcome = Outcome.SUCCESS;
        outcome.state = state;
        return outcome;
    }
    
    /**
     * Construct a reject (cannot process) outcome.
     * 
     * @param <S>   the state type.
     * @return the outcome.
     */
    public static <S> ReducerOutcome<S> reject() {
        ReducerOutcome<S> outcome = new ReducerOutcome<S>();
        outcome.outcome = Outcome.REJECT;
        return outcome;
    }

    /**
     * Construct a failure outcome with the reason for failure.
     * 
     * @param <S>    the state type.
     * @param reason
     *               the reason for failure.
     * @return the outcome.
     */
    public static <S> ReducerOutcome<S> failure(IFailureReason reason) {
        if (reason == null) {
            reason = new IFailureReason () {

                @Override
                public String summary() {
                    return "No reason provided";
                }
                
            };
        }
        ReducerOutcome<S> outcome = new ReducerOutcome<S>();
        outcome.outcome = Outcome.FAILURE;
        outcome.reason = reason;
        return outcome;
    }

    /**
     * Captures the outcome of dispatching an action.
     */
    public static class ReducerOutcome<S> extends ActionOutcome {

        /**
         * See {@link #state()}.
         */
        protected S state;

        /**
         * The revised state (for a successful outcome).
         * @return the state.
         */
        public S state() {
            return state;
        }

   }

}
