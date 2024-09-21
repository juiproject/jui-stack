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
package com.effacy.jui.rpc.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.effacy.jui.rpc.extdirect.RouterLogger;
import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.command.CommandProcessorExecutor;
import com.effacy.jui.rpc.handler.command.CommandProcessorRegistry;
import com.effacy.jui.rpc.handler.command.ICommandProcessor;
import com.effacy.jui.rpc.handler.command.ICommandProcessorContext;
import com.effacy.jui.rpc.handler.command.CommandProcessorExecutor.ICommandExecutorListener;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException.ErrorType;
import com.effacy.jui.rpc.handler.query.IQueryProcessor;
import com.effacy.jui.rpc.handler.query.QueryProcessorExecutor;
import com.effacy.jui.rpc.handler.query.QueryProcessorRegistry;

/**
 * Support class for implementing entry points for command and query processing.
 * 
 * @author Jeremy Buckley
 */
public abstract class Executor<Q, C extends ICommandProcessorContext<C>> {

    /**
     * Query executor.
     */
    private QueryProcessorExecutor<Q> queryExecutor;

    /**
     * Processor registry.
     */
    private QueryProcessorRegistry<Q> queryRegistry = new QueryProcessorRegistry<Q> ();

    /**
     * Command executor.
     */
    private CommandProcessorExecutor<C> commandExecutor;

    /**
     * Command registry.
     */
    private CommandProcessorRegistry<C> commandRegistry = new CommandProcessorRegistry<C> ();

    /**
     * Constructs instance of the executor.
     */
    protected Executor() {
        queryExecutor = new QueryProcessorExecutor<Q> (queryRegistry);
        commandExecutor = new CommandProcessorExecutor<C> (commandRegistry);
        commandExecutor.addListener (new ICommandExecutorListener<C> () {

            @Override
            public void onCommandProcessed(C context, ICommand command, Object outcome, long time) {
                Executor.this.onCommandProcessed (context, command, outcome, time);
            }


            @Override
            public void onCommandProcessedInError(C context, ICommand command, Throwable exception) {
                Executor.this.onCommandProcessedInError (context, command, exception);
            }

        });
    }


    /**
     * Callback (see
     * {@link ICommandExecutorListener#onCommandProcessed(ICommand, Object)}).
     */
    protected void onCommandProcessed(C context, ICommand command, Object outcome, long time) {
        // Nothing.
    }


    /**
     * Callback (see
     * {@link ICommandExecutorListener#onCommandProcessedInError(ICommand, Throwable)}).
     */
    protected void onCommandProcessedInError(C context, ICommand command, Throwable exception) {
        // Nothing.
    }


    /**
     * Performs a query.
     * 
     * @param context
     *            the context in which to perform the query.
     * @param query
     *            the query to perform.
     * @return the result of performing the query.
     * @throws NoProcessorException
     *             if no processor could be found for the query (or the passed
     *             query is {@code null}).
     * @throws ProcessorException
     *             if there was an application exception.
     */
    protected Object query(Q context, Object query) throws NoProcessorException, ProcessorException {
        if (query == null)
            throw new NoProcessorException (Void.class);
        try {
            return queryExecutor.process (context, query);
        } catch (NoProcessorException | ProcessorException e) {
            throw e;
        } catch (Throwable e) {
            throw convertUncaughtException (e);
        }
    }


    /**
     * Executes a collection of commands in the given role.
     * 
     * @param context
     *            the command context to execute the commands within.
     * @param commands
     *            the commands to execute.
     * @return The outcome of the last command.
     * @throws ProcessorException
     *             If there was a problem executing the command.
     */
    protected Object execute(C context, ICommand... commands) throws NoProcessorException, ProcessorException {
        try {
            if ((commands == null) || (commands.length == 0))
                return null;
            return commandExecutor.process (context, commands);
        } catch (NoProcessorException | ProcessorException e) {
            throw e;
        } catch (Throwable e) {
            throw convertUncaughtException (e);
        }
    }


    /**
     * Normally a processor will through a {@link ProcessorException} however
     * there could be cases where an un-checked throwable slips by. This
     * provides a means to properly handle those cases to generate a
     * {@link ProcessorException} suitable for propagation.
     * <p>
     * The default behaviour is to create a system based
     * {@link ProcessorException.Error} and log the underlying exception.
     * 
     * @param e
     *            the exception to convert.
     * @return the converted exception.
     */
    protected final ProcessorException convertUncaughtException(Throwable e) {
        if (e instanceof ProcessorException)
            return (ProcessorException) e;
        ProcessorException ve = new ProcessorException ();
        ve.add (ErrorType.SYSTEM, error -> {
            error.message (StringUtils.trimToEmpty (e.getMessage ()));
        });
        RouterLogger.uncaught (e);
        return ve;
    }


    /**
     * Adds a processor.
     * 
     * @param processor
     *            the processor to add.
     */
    public void add(IQueryProcessor<Q> processor) {
        queryRegistry.add (processor);
    }


    /**
     * Sets the processors.
     * 
     * @param processors
     *            the processors.
     */
    public void setProcessors(List<IQueryProcessor<Q>> processors) {
        queryRegistry.setProcessors (processors);
    }


    /**
     * Adds a single resolver.
     * 
     * @param resolver
     *            the resolver to add.
     */
    public void add(ICommandProcessor<?, ?, C> resolver) {
        commandRegistry.add (resolver);
    }


    /**
     * Sets a list of resolvers.
     * 
     * @param resolvers
     *            the resolvers.
     */
    public void setResolvers(List<ICommandProcessor<?, ?, C>> resolvers) {
        commandRegistry.setResolvers (resolvers);
    }

}
