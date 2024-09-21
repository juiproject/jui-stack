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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.state.redux.IFailureReason;
import com.effacy.jui.core.client.state.redux.Reducer;
import com.effacy.jui.core.client.state.redux.Reducer.ReducerOutcome;
import com.effacy.jui.core.client.state.redux.State.Outcome;

public class ReducerOutcomeTest {
    
    @Test
    public void testSuccess() {
        ReducerOutcome<Integer> outcome = Reducer.success (34);
        Assertions.assertEquals (Outcome.SUCCESS, outcome.outcome ());
        Assertions.assertEquals (34, outcome.state ());
    }

    @Test
    public void testReject() {
        ReducerOutcome<Integer> outcome = Reducer.reject ();
        Assertions.assertEquals (Outcome.REJECT, outcome.outcome ());
    }

    @Test
    public void testFailure() {
        // TEST: Failure reason default.
        Assertions.assertNotNull (IFailureReason.create ());
        Assertions.assertNotNull (IFailureReason.create ().summary ());

        // TEST: Failure reason from summary.
        Assertions.assertNotNull (IFailureReason.create ("This is some reason"));
        Assertions.assertEquals ("This is some reason", IFailureReason.create ("This is some reason").summary ());

        // TEST: A null reason uses the default reason.
        ReducerOutcome<Integer> outcome = Reducer.failure (null);
        Assertions.assertEquals (Outcome.FAILURE, outcome.outcome ());
        Assertions.assertNotNull (outcome.reason ());
        Assertions.assertEquals (IFailureReason.create ().summary (), outcome.reason ().summary ());

        // TEST: A constructed reason.
        outcome = Reducer.failure (IFailureReason.create ("Some reason"));
        Assertions.assertEquals (Outcome.FAILURE, outcome.outcome ());
        Assertions.assertNotNull (outcome.reason ());
        Assertions.assertEquals ("Some reason", outcome.reason ().summary ());
    }
}
