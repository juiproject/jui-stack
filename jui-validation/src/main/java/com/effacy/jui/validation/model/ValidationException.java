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
import java.util.Iterator;
import java.util.List;

import com.effacy.jui.validation.model.IValidator.Message;

public class ValidationException extends Exception implements Iterable<Message> {

    /**
     * Serialisation ID.
     */
    private static final long serialVersionUID = 6906435938466868500L;

    private List<Message> messages = new ArrayList<> ();

    public ValidationException() {
        super ();
    }

    public ValidationException(List<Message> messages) {
        if (messages != null)
            this.messages.addAll (messages);
    }

    public ValidationException add(String message) {
        return add (new Message (message));
    }

    public ValidationException add(Message message) {
        messages.add (message);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Message> iterator() {
        return messages.iterator ();
    }

    /**
     * The total number of messages.
     * 
     * @return the number.
     */
    public int size() {
        return messages.size();
    }

    /**
     * Merges the messages from the passed exception into this on.
     * 
     * @param e
     *          the exception to merge message from.
     * @return this exception.
     */
    public ValidationException merge(ValidationException e) {
        if (e != null)
            this.messages.addAll (e.messages);
        return this;
    }

    /**
     * Determines if there is a message that matches the path.
     * 
     * @param path
     *             the path to check.
     * @return {@code true} if there is a match.
     */
    public boolean matchByPath(String path) {
        for (Message message : messages) {
            if ((path == message.getPath ()))
                return true;
            if ((path != null) && path.equals (message.getPath ()))
                return true;
        }
        return false;
    }

}
