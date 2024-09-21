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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.state.redux.Reducer.ReducerOutcome;
import com.effacy.jui.core.client.state.redux.State.ActionOutcome;
import com.effacy.jui.core.client.state.redux.State.Outcome;
import com.effacy.jui.platform.util.client.Logger;

/**
 * Default implementation of {@link IStateMutator}.
 */
public class StateMutator<S> implements IStateMutator<S> {

    /**
     * The current state.
     */
    private S state;

    /**
     * Collection of regisered reducers.
     */
    private List<ReducerWrapper> reducers = new ArrayList<>();

    /**
     * Construct with no initial state.
     */
    public StateMutator() {
        // Nothing.
    }

    /**
     * Create with an initial state.
     * 
     * @param initialState
     *                     the initial state.
     */
    public StateMutator(S initialState) {
        this.state = initialState;
    }
    
    /**
     * Registeres a reducer.
     * 
     * @param reducer   the reducer.
     * @param initState the initial state for the reducer (if using an alternative
     *                  to that of the mutator).
     * @return this mutator instance.
     */
    public StateMutator<S> registerReducer(Reducer<S> reducer, S initState) {
         reducers.add (new ReducerWrapper (reducer, initState));
         return this;
    }

    /**
     * Registeres a reducer.
     * 
     * @param reducer   the reducer.
     * @return this mutator instance.
     */
    public StateMutator<S> registerReducer(Reducer<S> reducer) {
        return registerReducer (reducer, null);
    }

    @Override
    public S state() {
        return state;
    }

    @Override
    public Outcome dispatch(Action action) {
        if (action == null)
            return Outcome.REJECT;

        // Work through the reducers.
        for (ReducerWrapper reducer : reducers) {
            ActionOutcome outcome = reducer.reduce (action);
            if (outcome.outcome == Outcome.FAILURE) {
                action.fail (outcome.reason ());
                return Outcome.FAILURE;
            }
            if (outcome.outcome  == Outcome.SUCCESS) 
                return Outcome.SUCCESS;
        }

        // Check if the state can accept the action.
        if (state instanceof State) {
            ActionOutcome outcome = ((State) state).process (action);
            if (outcome != null) {
                if (outcome.outcome == Outcome.FAILURE) {
                    try {
                        action.fail (outcome.reason ());
                    } catch (Throwable e) {
                        Logger.reportUncaughtException (e);
                    }
                    return Outcome.FAILURE;
                }
                if (outcome.outcome == Outcome.SUCCESS)
                    return Outcome.SUCCESS;
            }
        }

        // Here we are not processing the action.
        return Outcome.REJECT;
    }

    class ReducerWrapper {
        
        private Reducer<S> reducer;
        
        private S initialState;

        public ReducerWrapper(Reducer<S> reducer, S initialState) {
            this.reducer = reducer;
            this.initialState = initialState;
        }

        public ActionOutcome reduce(Action action) {
            S state = StateMutator.this.state;
            if (state == null)
                state = initialState;
            ReducerOutcome<S> outcome = reducer.reduce (state, action);
            if (outcome == null)
                return State.reject ();
            if (outcome.outcome == Outcome.SUCCESS) {
                if (outcome.state () != null)
                    StateMutator.this.state = outcome.state ();
            }
            return outcome;
        }
    }
    
}
