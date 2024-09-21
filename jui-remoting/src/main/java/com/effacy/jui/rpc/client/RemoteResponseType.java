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
package com.effacy.jui.rpc.client;

/**
 * Describes the root cause type for an error in a remote method call.
 * 
 * @author Jeremy Buckley
 */
public enum RemoteResponseType {

    /**
     * No error, the action was successful.
     */
    SUCCESS,

    /**
     * General error.
     */
    ERROR,

    /**
     * More specific system error.
     */
    ERROR_SYSTEM,

    /**
     * More specific validation error.
     */
    ERROR_VALIDATION,

    /**
     * More specific access rights.
     */
    ERROR_ACCESS_RIGHTS,

    /**
     * More specific not found error.
     */
    ERROR_NOT_FOUND,

    /**
     * Service not implemented.
     */
    ERROR_NOT_IMPLEMENTED;

}
