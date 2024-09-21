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
package com.effacy.jui.rpc.extdirect;

/**
 * A special exception that wraps another exception and is processed up by the
 * remoting framework as if the wrapped exception were thrown.
 * <p>
 * This can be used to propagate exceptions through the transaction boundaries
 * without forcing a rollback (the remoting frameworks handles this
 * automatically and the application it is used in needs to ensure that this is
 * supported).
 * <p>
 * There are only a few cases where this should be used (where errors are being
 * returned but the transaction is expected to commit). Generally one should
 * avoid such scenarios.
 */
public class PassThroughException extends Exception {

    /**
     * See {@link #exception()}.
     */
    private Exception exception;

    /**
     * Construct wrapping the passed exception.
     * @param exception
     * the exception to pass through.
     */
    public PassThroughException (Exception exception) {
        this.exception = exception;
    }

    /**
     * The exception being passed through.
     * 
     * @return the exception.
     */
    public Exception exception() {
        return exception;
    }
}
