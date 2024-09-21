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
package com.effacy.jui.playground.remoting.api;

import com.effacy.jui.rpc.handler.Executor;
import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.client.query.Query;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException.ErrorType;

public class ApiExecutor extends Executor<QueryContext, CommandContext> {

    /**
     * Executes a collection of commands in the given role.
     * 
     * @param commands
     *                 the commands to execute.
     * @return the outcome of the last command.
     * @throws NoProcessorException
     *                              if the processor for the command could not be
     *                              found.
     * @throws ProcessorException
     *                              if there was a problem executing the command.
     */
    public void execute(ICommand... commands) throws NoProcessorException, ProcessorException {
        CommandContext ctx = new CommandContext();
        super.execute (ctx, commands);
    }

    /**
     * Performs a query with optional commands to execute prior to the query.
     * 
     * @param query
     *                 the query to perform.
     * @param commands
     *                 the commands to execute prior to executing the query.
     * @return the expected result from the query.
     * @throws NoProcessorException
     *                              if there was no query processor.
     * @throws ProcessorException
     *                              if there was a problem processing the commands.
     */
    @SuppressWarnings("unchecked")
    public <T> T query(Query<T> query, ICommand... commands) throws NoProcessorException, ProcessorException {
        // No query then generate suitable exception.
        if (query == null)
            throw new NoProcessorException (Void.class);

        // Run any commands.
        if ((commands != null) && (commands.length > 0))
            execute (commands);

        // Perform the query.
        try {
            return (T) super.query (new QueryContext(), query);
        } catch (RuntimeException e) {
            throw new ProcessorException ().add (ErrorType.SYSTEM, "There was a problem and we have taken note of if. You can try again later.");
        }
    }
}
