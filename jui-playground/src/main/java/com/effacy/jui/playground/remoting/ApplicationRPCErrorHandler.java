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
package com.effacy.jui.playground.remoting;

import com.effacy.jui.rpc.extdirect.ActionErrorHandler;
import com.effacy.jui.rpc.extdirect.RemoteCallRequest;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

public class ApplicationRPCErrorHandler extends ActionErrorHandler {

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.gwt.remote.extdirect.IActionErrorHandler#handleError(com.effacy.gwt.remote.extdirect.RemoteCallRequest,
     *      java.lang.Throwable)
     */
    @Override
    public Object handleError(RemoteCallRequest call, Throwable ex) {
        // Leaving open to expand on as necessary.
        return super.handleError (call, ex);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.gwt.remote.extdirect.IActionErrorHandler#retryError(java.lang.Throwable)
     */
    @Override
    public boolean retryError(Throwable e) {
        if (e instanceof ProcessorException)
            return ((ProcessorException) e).isRetry ();
        return false;
    }
}
