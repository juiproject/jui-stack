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

import com.effacy.jui.core.client.state.redux.State.Outcome;
import com.effacy.jui.platform.util.client.Logger;

/**
 * Default implementation of {@link IStateStore}. Makes use of a {@link IStateMutator}.
 */
public class StateStore<S> implements IStateStore<S> {

    /**
     * The mutator delegated to.
     */
    private IStateMutator<S> mutator;

    /**
     * Subscribers registered to listen to changes.
     */
    private List<SubscriberWrapper> subscribers = new ArrayList<> ();

    /**
     * Construct with no initial state.
     */
    public StateStore() {
        this.mutator = new StateMutator<>();
    }

    /**
     * Construct with initial state.
     * 
     * @param initialState
     *                     the initial state.
     */
    public StateStore(S initialState) {
        this.mutator = new StateMutator<> (initialState);
    }

    /**
     * Construct with a given mutator.
     * 
     * @param mutator
     *                the mutator.
     */
    public StateStore(IStateMutator<S> mutator) {
        this.mutator = mutator;
    }
    
    /**
     * Registeres a reducer.
     * 
     * @param reducer   the reducer.
     * @param initState the initial state for the reducer (if using an alternative
     *                  to that of the mutator).
     * @return this mutator instance.
     */
    public StateStore<S> registerReducer(Reducer<S> reducer, S initState) {
        if (mutator instanceof StateMutator)
            ((StateMutator<S>) mutator).registerReducer (reducer, initState);
         return this;
    }

    /**
     * Registeres a reducer.
     * 
     * @param reducer   the reducer.
     * @return this mutator instance.
     */
    public StateStore<S> registerReducer(Reducer<S> reducer) {
        if (mutator instanceof StateMutator)
            ((StateMutator<S>) mutator).registerReducer (reducer);
         return this;
    }
    
    @Override
    public S state() {
        return mutator.state ();
    }

    @Override
    public Outcome dispatch(Action action) {
        Outcome outcome = mutator.dispatch (action);
        if (outcome == Outcome.SUCCESS) {
            S state = mutator.state ();
            subscribers.forEach (subscriber -> subscriber.update (state));
        }
        return (outcome == null) ? Outcome.FAILURE : outcome;
    }

    @Override
    public SubscriberHandler subscribe(Subscriber<S> subscriber) {
        if (subscriber == null)
            return new SubscriberWrapper (null);
        SubscriberWrapper wrapper = new SubscriberWrapper(subscriber);
        subscribers.add (wrapper);
        return wrapper;
    }

    /**
     * Wrapper around a subscriber.
     */
    class SubscriberWrapper implements SubscriberHandler, Subscriber<S> {

        /**
         * The subscriber.
         */
        private Subscriber<S> subscriber;

        /**
         * Construct with a subscriber.
         * 
         * @param subscriber
         *                   the subscriber.
         */
        public SubscriberWrapper(Subscriber<S> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void unsubscribe() {
            subscriber = null;
            subscribers.remove (this);
        }

        @Override
        public void update(S state) {
            if (subscriber != null) {
                try {
                    subscriber.update (state);
                } catch (Throwable e) {
                    Logger.reportUncaughtException (e);
                }
            }
        }
        
    }
    
}
