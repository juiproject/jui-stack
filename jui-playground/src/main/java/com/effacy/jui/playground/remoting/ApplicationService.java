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

import org.springframework.beans.factory.annotation.Autowired;

import com.effacy.jui.playground.remoting.api.ApiExecutor;
import com.effacy.jui.rpc.client.RemoteResponse;
import com.effacy.jui.rpc.extdirect.annotation.RemoteAction;
import com.effacy.jui.rpc.extdirect.annotation.RemoteMethod;
import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.client.query.Query;

@RemoteAction(name = "ApplicationService", errorHandler = ApplicationRPCErrorHandler.class)
public class ApplicationService {

    /**
     * The underlying executor to delegate to.
     */
    @Autowired
    private ApiExecutor executor;

    /**
     * Performs the execution of commands.
     * 
     * @param commands
     *                     the commands to execute.
     * @return The response.ContextualizedCommandList
     */
    @RemoteMethod
    public RemoteResponse<Void> execute(ICommand... commands) throws Exception {
        executor.execute (commands);
        return new RemoteResponse<Void> ();
    }

    /**
     * Performs a query and optional execution of commands.
     * 
     * @param query
     *                     the query being performed.
     * @param commands
     *                     the (optional) commands to execute.
     * @return The return DTO as specified by the command.
     */
    @RemoteMethod
    public <T> RemoteResponse<T> query(Query<T> query, ICommand... commands) throws Exception {
        if (commands.length == 0)
            return new RemoteResponse<T> (executor.query (query));
        return new RemoteResponse<T> (executor.query (query, commands));
    }
    
}
