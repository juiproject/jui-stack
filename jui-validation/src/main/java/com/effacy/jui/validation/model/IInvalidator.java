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
package com.effacy.jui.validation.model;

import java.util.ArrayList;
import java.util.List;

public interface IInvalidator extends IValidatable {

    /**
     * If the object is currently in an invalidated state.
     * <p>
     * This is distinct from {@link #valid()} as an invalidated state remains
     * under the control of the method on this invalidator and not directly tied
     * with the validty of the object itself.
     * 
     * @return {@code true} if it is.
     */
    public boolean isInvalid();


    /**
     * Invalidates with the list of messages (where there are any).
     * 
     * @param messages
     *            the messages.
     */
    default public void invalidate(String... messages) {
        List<String> listOfMessages = new ArrayList<> ();
        for (String message : messages) {
            if (message == null)
                continue;
            listOfMessages.add (message);
        }
        invalidate (listOfMessages);
    }


    /**
     * Invalidates with the list of messages (where there are any). This will
     * first clear any existing messages and apply only those passed.
     * 
     * @param messages
     *            the messages.
     */
    public void invalidate(List<String> messages);


    /**
     * Given an collection of external messages and a path determines if this
     * invalidator can accept that message and if so will invalidate along with
     * it.
     * <p>
     * The messages that were accepted will be removed from the passed list of
     * messages.
     * 
     * @param messages
     *            the messages to accept.
     * @return {@code true} if accepted.
     */
    public boolean accept(List<? extends IErrorMessage> messages);


    /**
     * Clears any invalidation state.
     */
    public void clear();


    /**
     * Enables (or disables) validation.
     * <p>
     * If disabled then {@link #invalidate(List)} and
     * {@link #invalidate(String...)} will not operate but {@link #accept(List)}
     * will still process the passed message (and remove them) but will not
     * update the invalidation state. The method {@link #clear()} will still
     * clear any invalidation state.
     * 
     * @param enable
     *            {@code true} to enable (and {@code false} to disable).
     */
    public void enable(boolean enable);

    /**
     * Any messages that have been assigned via {@link #invalidate(List)}.
     * 
     * @return the messags.
     */
    public List<String> messages();
}
