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
package com.effacy.jui.rpc.extdirect.client.service;

import com.effacy.jui.json.annotation.JsonSerializable;

/**
 * Describes a remote method which can be called.
 * 
 * @author Steve Baker
 */
@JsonSerializable
public class RemoteApiMethod {

    /**
     * Method name.
     */
    private String name;

    /**
     * Number of method parameters.
     */
    private int len;

    /**
     * Whether this method needs a form handler.
     */
    private boolean formHandler;


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return the len
     */
    public int getLen() {
        return len;
    }


    /**
     * @param len
     *            the len to set
     */
    public void setLen(int len) {
        this.len = len;
    }


    /**
     * @return the formHandler
     */
    public boolean isFormHandler() {
        return formHandler;
    }


    /**
     * @param formHandler
     *            the formHandler to set
     */
    public void setFormHandler(boolean formHandler) {
        this.formHandler = formHandler;
    }

}
