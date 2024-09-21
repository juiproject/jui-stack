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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.effacy.jui.core.client.state.redux.State.Outcome;
import com.effacy.jui.platform.util.client.ListSupport;

/**
 * A composition of mutators that makes use of a tree of composites that can be
 * traversed using the path model.
 */
public class CompositeStateMutator implements IStateMutator<CompositeStateMutator.CompositeState> {

    public class CompositeState implements State, IStateMutator<State> {

        /**
         * Child mutators. Can be coposites or terminal mutators.
         */
        private Map<String,IStateMutator<?>> states = new HashMap<> ();

        @Override
        public State state() {
            return this;
        }
 
        /**
         * Given a path finds the registered mutator.
         * <p>
         * This path must only go as far as the nesting structure native to the child
         * composite states. That means that if a custom mutator us registered that
         * resolve the tail of the path then this call will fail.
         * 
         * @param path
         *             the path to resolve.
         * @return the matching mutator (or {@code null}).
         */
        public IStateMutator<?> mutator(String... path) {
            if (path.length == 0)
                return null;
            IStateMutator<?> mutator = states.get (path[0]);
            if (mutator == null)
                return null;
            if (path.length > 1) {
                if (mutator instanceof CompositeState)
                    return ((CompositeState) mutator).mutator (Action.reduce(path));
                return null;
            }
            return mutator;
        }

        @Override
        public Outcome dispatch(Action action) {
            if (action.length () <= 1)
                return Outcome.REJECT;
            String type = action.type ();
            IStateMutator<?> mutator = states.get (type);
            if (mutator == null)
                return Outcome.REJECT;
            return mutator.dispatch (action.next ());
        }

        /**
         * Register a mutator against the given path.
         * 
         * @param mutator
         *                the mutator to register.
         * @param path
         *                the path to register against.
         */
        public void register(IStateMutator<?> mutator, String[] path) {
            if (path.length == 1) {
                if (states.containsKey (path[0]))
                    throw new DuplicatPathException (path);
                states.put (path[0], mutator);
            } else {
                CompositeState state = new CompositeState ();
                states.put (path[0], state);
                state.register(mutator, Action.reduce (path));
            }
        }
    }

    /**
     * Root state.
     */
    private CompositeState state = new CompositeState ();

    @Override
    public CompositeState state() {
        return state;
    }

    /**
     * Obtains the underlying state at the end of the given path.
     * <p>
     * The passed path may be a sequence of path elements or one path that may be
     * slash sperated.
     * 
     * @param path
     *             the path to map to the desired state.
     * @return the associated state (empty if not found).
     */
    @SuppressWarnings("unchecked")
    public <S> Optional<S> state(String... path) {
        if ((path == null) || (path.length == 0))
            return Optional.empty ();
        if (path.length == 1)
            path = path[0].split ("/");
        IStateMutator<S> mutator = (IStateMutator<S>) state.mutator(path);
        return Optional.ofNullable (mutator.state ());
    }

    @Override
    public Outcome dispatch(Action action) {
        return state.dispatch (action);
    }

    /**
     * Registers the given mutator against the given path.
     * 
     * @param mutator
     *                the mutator to register.
     * @param path
     *                the path to register against (see {@link #state(String...)}
     *                for a description of how the path is constructed).
     */
    public void register(IStateMutator<?> mutator, String... path) {
        if ((mutator == null) || (path == null) || (path.length == 0))
            return ;
        if (path.length == 1)
            path = path[0].split ("/");
        try {
            state.register (mutator, path);
        } catch (DuplicatPathException e) {
            throw new DuplicatPathException (path);
        }
    }

    /**
     * Thrown when there is a conflict of path.
     */
    public static class DuplicatPathException extends RuntimeException {
        public DuplicatPathException(String[] path) {
            super ("Duplicate path registration: " + ListSupport.contract(ListSupport.list (path), "/"));
        }
    }
}
