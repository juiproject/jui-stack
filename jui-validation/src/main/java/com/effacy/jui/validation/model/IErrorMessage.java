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

public interface IErrorMessage {

    /**
     * Path to the target (i.e. field) of the error.
     * 
     * @return the path to the target of the error.
     */
    public String getPath();

    /**
     * Gets an optional code that may describe the error. This is used in situations
     * where the possible error cases are enumerated and better identify the
     * problem.
     * 
     * @return the code (or -1 if none has been assigned).
     */
    public int getCode();

    /**
     * Gets the user visible message describing what the error is.
     * 
     * @return the error message.
     */
    public String getMessage();

    /**
     * Convenience to create an error message from its parts.
     * 
     * @param path
     *                the path.
     * @param code
     *                the code.
     * @param message
     *                the message.
     * @return the composite.
     */
    public static IErrorMessage create(String path, int code, String message) {
        return new IErrorMessage () {

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.validation.model.IErrorMessage#getPath()
             */
            @Override
            public String getPath() {
                return path;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.validation.model.IErrorMessage#getCode()
             */
            @Override
            public int getCode() {
                return code;
            }

            /**
             * {@inheritDoc}
             *
             * @see com.effacy.jui.validation.model.IErrorMessage#getMessage()
             */
            @Override
            public String getMessage() {
                return message;
            }

        };
    }
}
