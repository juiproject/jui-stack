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
package com.effacy.jui.rpc.handler.exception;

/**
 * Thrown if an entity was not found.
 * 
 * @author Jeremy Buckley
 */
public class ReadOnlyProcessorException extends ProcessorException {

    /**
     * Unique serialisation ID.
     */
    private static final long serialVersionUID = 9045633977970468482L;

    public ReadOnlyProcessorException() {
        this (-1, null);
    }


    public ReadOnlyProcessorException(int errorCode) {
        this (errorCode, null);
    }


    public ReadOnlyProcessorException(String message) {
        this (-1, message);
    }


    public ReadOnlyProcessorException(int errorCode, String message) {
        super (ErrorType.READ_ONLY, errorCode, message);
    }

}
