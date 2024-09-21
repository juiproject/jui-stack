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
package com.effacy.jui.rpc.handler.client;

import java.util.function.Function;

import com.effacy.jui.rpc.client.IRemoteMethodCallback;
import com.effacy.jui.rpc.extdirect.client.service.AbstractServiceHandler;
import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.client.query.Query;

/**
 * A variation of {@link AbstractServiceHandler} that expects to make use of a
 * {@link IService} with command and query with command processing.
 * <p>
 * When used one calls the various {@code remoteExecute} methods (as needed).
 */
public abstract class AbstractQueryCommandServiceHandler<V,Q extends AbstractQueryCommandServiceHandler<V,Q>> extends AbstractServiceHandler<V,Q> {


    /**
     * Remotely executes the passed commands.
     * 
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, (String) null, (Query<V>) null, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(String notification, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, notification, (Query<V>) null, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(Query<V> query, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, (String) null, query, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public <T> void remoteExecute(Query<T> query, Function<T, V> queryResultConverter, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, (String) null, query, queryResultConverter, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(String notification, Query<V> query, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, notification, query, commands);
    }

    /**
     * Remotely executes the passed commands.
     * 
     * @param query
     *                 the query to perform after executing the commands (may be
     *                 {@code null}).
     * @param commands
     *                 the commands to execute.
     */
    public <T> void remoteExecute(String notification, Query<T> query, Function<T, V> queryResultConverter, ICommand... commands) {
        remoteExecute ((ICompletionCallback) null, notification, query, queryResultConverter, commands);
    }

    /**
     * Remotely executes the passed commands with a call-back on completion.
     * 
     * @param cb
     *                 the call-back.
     * @param query
     *                 the query to perform after executing the commands (may be
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(ICompletionCallback cb, ICommand... commands) {
        remoteExecute (cb, (String) null, (Query<V>) null, commands);
    }

    /**
     * Remotely executes the passed commands with a call-back on completion.
     * 
     * @param cb
     *                 the call-back.
     * @param commands
     *                 the commands to execute.
     */
    public void remoteExecute(ICompletionCallback cb, String notification, Query<V> query, ICommand... commands) {
        remoteExecute (cb, notification, query, v -> v, commands);
    }

    /**
     * Remotely executes the passed commands with a call-back on completion.
     * 
     * @param cb
     *                 the call-back.
     * @param commands
     *                 the commands to execute.
     */
    public <T> void remoteExecute(ICompletionCallback cb, String notification, Query<T> query, Function<T, V> queryResultConverter, ICommand... commands) {
        if (query == null)
            _remoteExecute (startRemoteExecution (cb, notification, null), commands);
        else
            _remoteExecute (startRemoteExecution (cb, notification, queryResultConverter), query, commands);
    }

    /**
     * Called by
     * {@link #remoteExecute(ICompletionCallback, String, Query, Function, ICommand...)
     * to execute a query with optional commands.
     */
    protected abstract <T> void _remoteExecute(IRemoteMethodCallback<T> cb, Query<T> query, ICommand... commands);

    /**
     * Called by
     * {@link #remoteExecute(ICompletionCallback, String, Query, Function, ICommand...)
     * to execute commands only.
     */
    protected abstract void _remoteExecute(IRemoteMethodCallback<Void> cb, ICommand... commands);
}
