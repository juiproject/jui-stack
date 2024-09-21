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

import java.util.Optional;

import com.effacy.jui.core.client.state.redux.State.Outcome;

public class Redux {

    public static <S extends State> Optional<S> state(String... path) {
        return STORE.state (path);
    }

    public static Outcome dispatch(Action action) {
        return STORE.dispatch (action);
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
    public static void register(IStateMutator<?> mutator, String... path) {
        STORE.register (mutator, path);
    }

    private static final CompositeStateMutator STORE = new CompositeStateMutator();
}
