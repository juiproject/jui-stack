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
package com.effacy.jui.playground.ui.remoting;

import com.effacy.jui.rpc.client.IRemoteMethodCallback;
import com.effacy.jui.rpc.extdirect.client.service.IService;
import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.client.query.Query;
import com.google.gwt.core.client.GWT;

/**
 * Remoting service for processing commands and queries.
 */
public interface ApplicationService extends IService {

    /**
     * Singleton instance.
     */
    public static final ApplicationService INSTANCE = (ApplicationService) GWT.create (ApplicationService.class);

    /**
     * Executes a set of commands.
     * 
     * @param cb
     *            the call back (for error responses).
     * @param commands
     *            the commands to execute.
     */
    public void execute(IRemoteMethodCallback<Void> cb, ICommand... commands);


    /**
     * Performs a remote query.
     * <p>
     * All queries are performed against a context path and make references
     * against that context path.
     * 
     * @param cb
     *            the callback.
     * @param query
     *            the query to perform (this is tied to the response type).
     */
    public <T> void query(IRemoteMethodCallback<T> cb, Query<T> query, ICommand... commands);

}
