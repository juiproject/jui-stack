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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.state.redux.Action;
import com.effacy.jui.core.client.state.redux.IFailureReason;
import com.effacy.jui.core.client.state.redux.Reducer;
import com.effacy.jui.core.client.state.redux.Redux;
import com.effacy.jui.core.client.state.redux.StateStore;
import com.effacy.jui.core.client.state.redux.State.Outcome;
import com.effacy.jui.platform.util.client.Carrier;

public class ReduxTest {

    /**
     * Convenience to store a failure message.
     */
    protected Carrier<IFailureReason> failure = Carrier.of (null);
    
    @Test
    public void testRegister001() {
        // Carrier.
        Carrier<Integer> peek = Carrier.of (0);

        // Create a state mutator and register a reducer.
        StateStore<Integer> store = new StateStore<> (22);
        store.registerReducer ((s, a) -> {
            if ("increment".equals (a.type ()))
                return Reducer.success (s + 1);
            if ("decrement".equals (a.type ()))
                return Reducer.success (s - 1);
            return Reducer.reject ();
        });
        store.registerReducer ((s, a) -> {
            if ("add".equals (a.type ())) {
                if (a.payload  () == null)
                    return Reducer.failure (IFailureReason.create ("Payload cannot be null"));
                return Reducer.success (s + (Integer) a.payload());
            }
            if ("subtract".equals (a.type ())) {
                if (a.payload  () == null)
                    return Reducer.failure (IFailureReason.create ("Payload cannot be null"));
                return Reducer.success (s - (Integer) a.payload());
            }
            return Reducer.reject ();
        });

        // Register a subscriber that sets the peek.
        store.subscribe (v -> {
            peek.set (v);
        });

        // Register the mutator against the path a/b/c.
        Redux.register (store, "a/b/c");

        // Check that it was registered.
        Assertions.assertNotNull (Redux.state("a/b/c"));
        Assertions.assertEquals (22, Redux.state("a/b/c").get ());

        // Run some non-payload actions.
        Assertions.assertEquals(0, peek.get ());
        assertDispatchSuccess (Action.$ ("a/b/c/increment"));
        Assertions.assertEquals(23, peek.get ());
        assertDispatchSuccess (Action.$ ("a/b/c/increment"));
        Assertions.assertEquals(24, peek.get ());
        assertDispatchSuccess (Action.$ ("a/b/c/decrement"));
        Assertions.assertEquals(23, peek.get ());

        // Run some non-exitent actions.
        assertDispatchReject (Action.$ ("a/b/c/nocommand"));

        // Run some payload actions.
        assertDispatchSuccess (Action.$ ("a/b/c/add", 52));
        Assertions.assertEquals (75, peek.get ());
        assertDispatchSuccess (Action.$ ("a/b/c/subtract", 5));
        Assertions.assertEquals (70, peek.get ());

        // Run some failure payloads.
        assertDispatchFailure (Action.$ ("a/b/c/add", null, failure ()), "Payload cannot be null");
        assertDispatchFailure (Action.$ ("a/b/c/subtract", null, failure ()), "Payload cannot be null");
    }

    /************************************************************************
     * Custom assertions
     ************************************************************************/

    protected void assertDispatchSuccess(Action a) {
        Outcome outcome = Redux.dispatch (a);
        Assertions.assertEquals (Outcome.SUCCESS, outcome);
    }
    
    protected void assertDispatchFailure(Action a, String summary) {
        failure.set (null);
        Outcome outcome = Redux.dispatch (a);
        Assertions.assertEquals (Outcome.FAILURE, outcome);
        if (summary != null) {
            Assertions.assertNotNull (failure.get ());
            Assertions.assertEquals (summary, failure.get ().summary ());
        }
    }

    protected void assertDispatchReject(Action a) {
        Outcome outcome = Redux.dispatch (a);
        Assertions.assertEquals (Outcome.REJECT, outcome);
    }

    protected Consumer<IFailureReason> failure() {
        return (reason -> failure.set (reason));
    }
    
}
