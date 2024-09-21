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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.client.ErrorRemoteResponse;
import com.effacy.jui.rpc.client.RemoteResponseType;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

/**
 * Abstract support class for mapping exceptions that arise from actions.
 *
 * @author Jeremy Buckley
 */
public abstract class ActionErrorHandler implements IActionErrorHandler {

    /**
     * Commons logging (general).
     */
    private static final Logger LOG_REMOTE = LoggerFactory.getLogger (Router.LOGGER_CHANNEL_ERROR);

    /**
     * {@inheritDoc}
     * <p>
     * Default case not to retry (assuming all error are permanent).
     *
     * @see com.effacy.jui.remote.extdirect.IActionErrorHandler#retryError(java.lang.Throwable)
     */
    @Override
    public boolean retryError(Throwable e) {
        return false;
    }


    /**
     * {@inheritDoc}
     * <p>
     * The default case is to log the error and return an error reponse with a
     * single un-associated field message whose display messageis returned by
     * {@link #getGeneralErrorMessage()}.
     *
     * @see com.effacy.jui.remote.extdirect.IActionErrorHandler#handleError(com.effacy.jui.remote.extdirect.RemoteCallRequest,
     *      java.lang.Throwable)
     */
    @Override
    public Object handleError(RemoteCallRequest call, Throwable e) {
        if (e instanceof ProcessorException) {
            // Sometimes there is an access rights violation tucked away. Here
            // we would prefer to pass this through via the response type.
            RemoteResponseType type = RemoteResponseType.ERROR;
            List<ErrorMessage> messages = new ArrayList<ErrorMessage> ();

            // Determine if all the messages are of the same type (the common
            // type will reside in errorType, otherwise it will be null).
            ProcessorException.ErrorType errorType = null;
            for (ProcessorException.Error err : (ProcessorException) e) {
                if (errorType == null) {
                    errorType = err.getType ();
                } else if (errorType != err.getType ()) {
                    errorType = null;
                    break;
                }
            }

            // If there is a common error then map to the response type.
            if (errorType != null) {
                if (ProcessorException.ErrorType.ACCESS_RIGHTS == errorType)
                    type = RemoteResponseType.ERROR_ACCESS_RIGHTS;
                else if (ProcessorException.ErrorType.SYSTEM == errorType)
                    type = RemoteResponseType.ERROR_SYSTEM;
                else if (ProcessorException.ErrorType.NOT_FOUND == errorType)
                    type = RemoteResponseType.ERROR_NOT_FOUND;
                else if (ProcessorException.ErrorType.VALIDATION == errorType)
                    type = RemoteResponseType.ERROR_VALIDATION;
            }

            // Load up all the errors.
            for (ProcessorException.Error err : (ProcessorException) e) {
                String message = err.getMessage ();
                if (message == null)
                    message = "";
                if (message.startsWith ("{") && message.endsWith ("}"))
                    message = mapMessage (message.substring (1, message.length () - 1), err.getCode ());
                messages.add (new ErrorMessage (err.getPath (), err.getType ().name (), err.getCode (), message));
            }
            return new ErrorRemoteResponse (type, messages);
        }

        if (e instanceof NoProcessorException) {
            return new ErrorRemoteResponse (RemoteResponseType.ERROR_NOT_IMPLEMENTED);
        }

        if (LOG_REMOTE.isErrorEnabled ()) {
            StringBuffer sb = new StringBuffer ();
            sb.append (call.getAction ());
            sb.append (".");
            sb.append (call.getMethod ());
            sb.append ("(");
            if (call.getData () != null) {
                for (int i = 0; i < call.getData ().size (); i++) {
                    if (i > 0)
                        sb.append (",");
                    if (call.getData ().get (i) == null)
                        sb.append ("null");
                    else
                        sb.append (call.getData ().get (i));
                }
            }
            sb.append (")");
            LOG_REMOTE.error (sb.toString (), e);
        }
        return new ErrorRemoteResponse (RemoteResponseType.ERROR, new ErrorMessage (getGeneralErrorMessage ()));
    }


    /**
     * Gets the general error message that is returned as an unassociated field
     * message by {@link #handleError(RemoteCallRequest, Throwable)}.
     * 
     * @return the message.
     */
    protected String getGeneralErrorMessage() {
        return "A problem occurred, try again later.";
    }


    /**
     * Maps a message.
     * 
     * @param message
     *            the message to map.
     * @param code
     *            the associated code (if any).
     * @return the mapped message.
     */
    protected String mapMessage(String message, int code) {
        return message;
    }

}
