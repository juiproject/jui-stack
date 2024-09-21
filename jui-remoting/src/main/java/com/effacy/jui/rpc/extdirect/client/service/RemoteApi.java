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

import java.util.List;
import java.util.Map;

import com.effacy.jui.json.annotation.JsonSerializable;

/**
 * Describes all the actions and methods available in a remote API.
 * 
 * @author Steve Baker
 */
@JsonSerializable
public class RemoteApi {

    /**
     * Entry point URL.
     */
    private String url;

    /**
     * API type.
     */
    private String type;

    /**
     * Login URL, used when session has expired.
     */
    private String loginUrl;

    /**
     * Map where service actions are the key and a list of available methods is
     * the value.
     */
    private Map<String, List<RemoteApiMethod>> actions;


    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }


    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }


    /**
     * @return the type
     */
    public String getType() {
        return type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * @return the loginUrl
     */
    public String getLoginUrl() {
        return loginUrl;
    }


    /**
     * @param loginUrl
     *            the loginUrl to set
     */
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }


    /**
     * @return the actions
     */
    public Map<String, List<RemoteApiMethod>> getActions() {
        return actions;
    }


    /**
     * @param actions
     *            the actions to set
     */
    public void setActions(Map<String, List<RemoteApiMethod>> actions) {
        this.actions = actions;
    }

}
