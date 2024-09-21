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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.validation.model.IErrorMessage;

/**
 * A field message is an error condition that is mapped to a primary field (form
 * field or UI control) and optional associated fields.
 * 
 * @author Steve Baker
 */
@JsonSerializable
public class ErrorMessage implements IErrorMessage {

    /**
     * The immediate context of the violation.
     */
    private String path;

    /**
     * The type of violation.
     */
    private String type;

    /**
     * Code describing the violation.
     */
    private int code = -1;

    /**
     * Message describing the violation.
     */
    private String message;

    /**
     * Serialization constructor.
     */
    protected ErrorMessage() {
        // Nothing.
    }


    /**
     * Construct with a single unassociated message.
     * 
     * @param message
     *            the message.
     */
    public ErrorMessage(String message) {
        this.message = message;
    }


    /**
     * Construct with a message associated with a field.
     * 
     * @param field
     *            the field.
     * @param message
     *            the message.
     */
    public ErrorMessage(String path, String message) {
        this.path = path;
        this.message = message;
    }


    /**
     * Constructs a full message.
     * 
     * @param field
     *            the field.
     * @param path
     *            an optional path to contextualize the field.
     * @param type
     *            the type of error.
     * @param code
     *            the error code (as relevant).
     * @param message
     *            the message.
     */
    public ErrorMessage(String path, String type, int code, String message) {
        this.path = path;
        this.type = type;
        this.code = code;
        this.message = message;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IErrorMessage#getPath()
     */
    @Override
    public String getPath() {
        if (path == null)
            path = "";
        return path;
    }


    /**
     * Setter for {@link #getPath()}.
     */
    public void setPath(String path) {
        this.path = path;
    }


    /**
     * A type for the error.
     * 
     * @return the type of error.
     */
    public String getType() {
        if (type == null)
            type = "";
        return type;
    }


    /**
     * Setter for {@link #getType()}.
     */
    public void setType(String type) {
        this.type = type;
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
     * Setter for {@link #getClass()}.
     */
    public void setCode(int code) {
        this.code = code;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IErrorMessage#getMessage()
     */
    @Override
    public String getMessage() {
        if (message == null)
            message = "";
        return message;
    }


    /**
     * Setter for {@link #getMessage()}.
     */
    public void setMessage(String message) {
        this.message = message;
    }


    /**
     * Determines if the message is a general message that is not associated to
     * any given field. This occurs when the field is {@code null} or is empty.
     * 
     * @return {@code true} if the message is a general one.
     */
    public boolean isGeneral() {
        String field = getPath ();
        return (field == null) ? true : field.trim ().isEmpty ();
    }


    /**
     * Extracts a list of all general messages in a list of field messages (see
     * {@link ErrorMessage#isGeneral()}).
     * 
     * @param messages
     *            the messages to extract from.
     * @return those messages that are general (may be an empty list).
     */
    public static List<ErrorMessage> extractGeneralMessages(List<ErrorMessage> messages) {
        List<ErrorMessage> general = new ArrayList<ErrorMessage> ();
        if (messages != null) {
            for (ErrorMessage message : messages) {
                if (message.isGeneral ())
                    general.add (message);
            }
        }
        return general;
    }


    /**
     * Extracts the first general message (content). If no general message is
     * present then the empty string is returned.
     * 
     * @param messages
     *            the messages to check.
     * @return the first general message or the empty string.
     */
    public static String extractGeneralMessage(List<ErrorMessage> messages) {
        return extractGeneralMessage (messages, "");
    }


    /**
     * Extracts the first general message (content). If no general message is
     * present then the default message is returned.
     * 
     * @param messages
     *            the messages to check.
     * @param defaultMessage
     *            the default message to return if there are no general
     *            messages.
     * @return the first general message or the default message.
     */
    public static String extractGeneralMessage(List<ErrorMessage> messages, String defaultMessage) {
        for (ErrorMessage message : messages) {
            String target = message.getPath ();
            if ((target == null) ? true : target.trim ().isEmpty ())
                return message.getMessage ();
        }
        return defaultMessage;
    }


    /**
     * Determines if there are any general messages in the passed list of
     * messages.
     * 
     * @param messages
     *            the messages to check for existence of a general message.
     * @return {@code true} if there is a general message.
     */
    public static boolean hasGeneralMessage(List<ErrorMessage> messages) {
        if (messages != null) {
            for (ErrorMessage message : messages) {
                if (message.isGeneral ())
                    return true;
            }
        }
        return false;
    }

}
