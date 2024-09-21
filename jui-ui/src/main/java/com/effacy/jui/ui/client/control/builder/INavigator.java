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
package com.effacy.jui.ui.client.control.builder;

import com.effacy.jui.core.client.Invoker;

public interface INavigator {

    public enum Directions {

        PREVIOUS(true, false),
        
        NEXT(false, true),
        
        PREVOUS_NEXT(true, true),
        
        NONE(false, false);

        private boolean previous;

        private boolean next;

        private Directions(boolean previous, boolean next) {
            this.previous = previous;
            this.next = next;
        }

        /**
         * If there is a previous step.
         * @return {@code true} if there is.
         */
        public boolean previous() {
            return previous;
        }

        /**
         * If there is a next step.
         * 
         * @return {@code true} if there is.
         */
        public boolean next() {
            return next;
        }

        /**
         * Perform the given action if there is a previous.
         * 
         * @param action
         *               the action to perform.
         * @return this direction.
         */
        public Directions previous(Invoker action) {
            if (previous())
                action.invoke ();
            return this;
        }

        /**
         * Perform the given action if at the start (no previous).
         * 
         * @param action
         *               the action to perform.
         * @return this direction.
         */
        public Directions start(Invoker action) {
            if (!previous())
                action.invoke ();
            return this;
        }

        /**
         * Perform the given action if there is a next.
         * 
         * @param action
         *               the action to perform.
         * @return this direction.
         */
        public Directions next(Invoker action) {
            if (next())
                action.invoke ();
            return this;
        }

        /**
         * Perform the given action if at the end (no next).
         * 
         * @param action
         *               the action to perform.
         * @return this direction.
         */
        public Directions end(Invoker action) {
            if (!next())
                action.invoke ();
            return this;
        }

        /**
         * Perform the given action if there is no previous and no next.
         * 
         * @param action
         *               the action to perform.
         * @return this direction.
         */
        public Directions none(Invoker action) {
            if (!next() && !previous())
                action.invoke ();
            return this;
        }
    }

    /**
     * The current step (indexed from 0).
     * 
     * @return the current step.
     */
    public int step();

    /**
     * The total number of steps (on the current branch).
     * 
     * @return the total.
     */
    public int range();

    /**
     * Resets the navigation flow back to the start.
     */
    public void reset();

    /**
     * Navigate to the previous step and optionally reset the control on the page
     * being navigated away from.
     * 
     * @param clear
     *              {@code true} to clear the controls on the page.
     * @return the available directions.
     */
    public Directions previous(boolean clear);

    /**
     * Navigate to the previous step.
     * 
     * @return the available directions.
     */
    default public Directions previous() {
        return previous (false);
    }

    /**
     * Navigate to the next step.
     * 
     * @return the available directions.
     */
    public Directions next();

    /**
     * The directions that are available.
     * 
     * @return the directions.
     */
    default public Directions directions() {
        if (step() == 0)
            return (step() >= range() - 1) ? Directions.NONE : Directions.NEXT;
        return (step() >= range() - 1) ? Directions.PREVIOUS : Directions.PREVOUS_NEXT;
    }
}
