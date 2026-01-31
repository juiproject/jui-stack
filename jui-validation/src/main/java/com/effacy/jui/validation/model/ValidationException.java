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

/**
 * Exception representing one or more validation errors.
 */
public class ValidationException extends Exception implements Iterable<Message> {

    /**
     * Serialisation ID.
     */
    private static final long serialVersionUID = 6906435938466868500L;

    /**
     * The validation messages.
     */
    private List<Message> messages = new ArrayList<> ();

    /**
     * Constructs an empty validation exception.
     * <p>
     * Add messages with {@link #add(String)} or {@link #add(Message)}.
     */
    public ValidationException() {
        super ();
    }

    /**
     * Construct with initial messages.
     * 
     * @param messages
     *                 the messages to add.
     */
    public ValidationException(List<Message> messages) {
        if (messages != null)
            this.messages.addAll (messages);
    }

    /**
     * Construct with a single message.
     * 
     * @param message
     *                the message.
     */
    public ValidationException(String message) {
        add(message);
    }

    /**
     * Construct with a single message and path.
     * 
     * @param message
     *                the message.
     * @param path
     *                the path.
     */
    public ValidationException(String message, String path) {
        add(new Message(message).path(path));
    }

    /**
     * Adds a message to the exception.
     * 
     * @param message
     *                the message to add.
     * @return this exception.
     */
    public ValidationException add(String message) {
        return add (new Message (message));
    }

    /**
     * Adds a message to the exception.
     * 
     * @param message
     *                the message to add.
     * @return this exception.
     */
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
     * Merges the messages from the passed exception into this one.
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

    /**
     * Creates a single string containing the error messages. Useful for logging.
     * 
     * @return the flattened validation messages.
     */
    public String flatten() {
        StringBuffer sb = new StringBuffer();
        forEach(msg -> {
            if (sb.length() != 0)
                sb.append("; ");
            if (msg.getPath() != null) {
                sb.append(msg.getPath());
                sb.append("->");
            }
            sb.append(msg.getMessage());
        });
        return sb.toString();
    }

}
