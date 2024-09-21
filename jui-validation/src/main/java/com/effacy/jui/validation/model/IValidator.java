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
import java.util.function.Consumer;

/**
 * A validator that is able to validate the validity of a value.
 *
 * @author Jeremy Buckley
 */
@FunctionalInterface
public interface IValidator<T> {

    /**
     * Encapsulates an error message.
     */
    public static class Message {

        /**
         * See {@link #getPath()}.
         */
        private String path;

        /**
         * See {@link #getMessage()}.
         */
        private String message;

        /**
         * See {@link #getCode()}.
         */
        private int code = -1;

        /**
         * Construct a message.
         * 
         * @param message
         *                the base message to report.
         */
        public Message(String message) {
            this.message = message;
        }

        /**
         * Assigns a path discriminator for the message.
         * 
         * @param path
         *             the path discriminator.
         * @return this message instance.
         */
        public Message path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Assigns an error code along with the message.
         * 
         * @param code
         *             an associated error code.
         * @return this message instance.
         */
        public Message code(int code) {
            this.code = code;
            return this;
        }

        /**
         * An optional path discriminator for the message.
         * 
         * @return the path for the message.
         */
        public String getPath() {
            return path;
        }

        /**
         * The base message.
         * 
         * @return the message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Any associated error code.
         * 
         * @return the code.
         */
        public int getCode() {
            return code;
        }

        /**
         * Remaps the path (if it matches the from path) to the given path.
         * 
         * @param fromPath
         *                 the from path to map from (if {@code null} then will match
         *                 automatically).
         * @param toPath
         *                 the to path to map to (i.e. the path will be replaced with
         *                 this)
         * @return this message instance.
         */
        public Message remap(String fromPath, String toPath) {
            if (path == null) {
                if (fromPath == null)
                    path = toPath;
            } else if ((fromPath == null) || path.equals(fromPath))
                path = toPath;
            return this;
        }
    }

    /**
     * Validates the passed value.
     * 
     * @param value
     *                 the value to validate.
     * @param messages
     *                 a consumer to add messages to.
     * @return {@code true} if the value is valid.
     */
    public boolean validate(T value, Consumer<Message> messages);

    /**
     * Validates the passed value.
     * 
     * @param value
     *                 the value to validate.
     * @param path
     *                 (optional) path to apply to any created message.
     * @param messages
     *                 a consumer to add messages to.
     * @return {@code true} if the value is valid.
     */
    default boolean validate(T value, String path, Consumer<Message> messages) {
        return validate (value, msg -> messages.accept (msg.path (path)));
    }

    /**
     * Validates the passed value throwing an exception if not valid.
     * 
     * @param value
     *              the value to validate.
     * @return the passed value.
     * @throws ValidationException
     *                             if the value does not pass validation (with the
     *                             validation messages of which there could be more
     *                             than one).
     */
    default <V extends T> V validate(V value) throws ValidationException {
        List<Message> messages = new ArrayList<> ();
        if (!validate (value, msg -> messages.add (msg)))
            throw new ValidationException (messages);
        return value;
    }

    /**
     * Validates the passed value throwing an exception if not valid.
     * 
     * @param value
     *              the value to validate.
     * @param path
     *              a path to include,
     * @return the passed value.
     * @throws ValidationException
     *                             if the value does not pass validation (with the
     *                             validation messages of which there could be more
     *                             than one).
     */
    default <V extends T> V validate(V value, String path) throws ValidationException {
        List<Message> messages = new ArrayList<> ();
        if (!validate (value, msg -> messages.add (msg.path (path))))
            throw new ValidationException (messages);
        return value;
    }

}
