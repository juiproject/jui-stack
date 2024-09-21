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

/**
 * Represents an enhanced state variable. This permits actions to be taken
 * directly on the state.
 * <p>
 * States are not required to implement this, if they do then the framework will
 * account for that accoridingly.
 */
public interface State {

    /**
     * Processes an action on a state. The state may not support actions directly in
     * which case the default implementation return a rejection.
     * <p>
     * Note that where a state supports change then it will modify itself (rather
     * than a clone).
     * 
     * @param action
     *               the action to perform.
     * @return the outcome of the action.
     */
    public default ActionOutcome process(Action action) {
        return State.reject ();
    }

    /**
     * Creates a success outcome.
     * 
     * @return the outcome.
     */
    public static ActionOutcome success() {
        ActionOutcome outcome = new ActionOutcome();
        outcome.outcome = Outcome.SUCCESS;
        return outcome;
    }

    /**
     * Creates a reject (not able to process) outcome.
     * 
     * @return the outcome.
     */
    public static ActionOutcome reject() {
        ActionOutcome outcome = new ActionOutcome();
        outcome.outcome = Outcome.REJECT;
        return outcome;
    }

    /**
     * Creates a failure outcome.
     * 
     * @param reason the reason for the failure.
     * @return the outcome.
     */
    public static ActionOutcome failure(IFailureReason reason) {
        ActionOutcome outcome = new ActionOutcome();
        outcome.outcome = Outcome.FAILURE;
        outcome.reason = reason;
        return outcome;
    }

    /**
     * Primitive outcome state.
     */
    public enum Outcome {
        SUCCESS, FAILURE, REJECT;
    }

    /**
     * Captures the outcome of dispatching an action.
     */
    public static class ActionOutcome {

        /**
         * The outcome state.
         */
        protected Outcome outcome;

        /**
         * The reason for failure.
         */
        protected IFailureReason reason;

         /**
          * The outcome state.
          * @return the state.
          */
        public Outcome outcome() {
            return outcome;
        }

        /**
         * If failed then this is the reason for failure.
         * <p>
         * If this is {@code null} then a default reason is returned.
         * 
         * @return the failure reason.
         */
        public IFailureReason reason() {
            if (reason == null)
                reason = IFailureReason.create ();
            return reason;
        }
    }
}
