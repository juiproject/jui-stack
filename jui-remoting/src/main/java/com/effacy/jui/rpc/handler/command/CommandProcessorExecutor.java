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
package com.effacy.jui.rpc.handler.command;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

/**
 * Support class for implementing a processor of a {@link CommandGroup}.
 * 
 * @author Jeremy Buckley
 */
public class CommandProcessorExecutor<C extends ICommandProcessorContext<C>> implements ICommandProcessorExecutor<C> {

    /**
     * Listens to command executions.
     */
    public interface ICommandExecutorListener<C> {

        /**
         * Invoked when a command has been processed.
         * <p>
         * Take care as exceptions from this will be absorbed and ignored.
         * 
         * @param command
         *            the command.
         * @param outcome
         *            the outcome of the command.
         */
        public void onCommandProcessed(C context, ICommand command, Object outcome, long time);


        /**
         * Invoked when a command has been processed but results in an error.
         * <p>
         * Take care as exceptions from this will be absorbed and ignored.
         * 
         * @param command
         *            the command.
         * @param exception
         *            the exception arising.
         */
        public void onCommandProcessedInError(C context, ICommand command, Throwable exception);

    }

    /**
     * The command registry.
     */
    protected ICommandProcessorRegistry<C> registry;

    /**
     * Listeners to command executions.
     */
    protected List<ICommandExecutorListener<C>> listeners = new ArrayList<ICommandExecutorListener<C>> ();

    /**
     * Construct with an empty registry.
     */
    public CommandProcessorExecutor() {
        this.registry = new CommandProcessorRegistry<C> ();
    }


    /**
     * Construct with the passed registry.
     * 
     * @param registry
     */
    public CommandProcessorExecutor(ICommandProcessorRegistry<C> registry) {
        this.registry = registry;
    }


    /**
     * Sets the registry.
     * 
     * @param registry
     *            the registry.
     */
    public void setRegistry(ICommandProcessorRegistry<C> registry) {
        this.registry = registry;
    }


    /**
     * Adds a listener.
     * 
     * @param listener
     *            the listener to add.
     */
    public void addListener(ICommandExecutorListener<C> listener) {
        if (listener == null)
            return;
        if (!listeners.contains (listener))
            listeners.add (listener);
    }


    /**
     * Removes a listener.
     * 
     * @param listener
     *            the listener to remove.
     */
    public void removeListener(ICommandExecutorListener<C> listener) {
        if (listener == null)
            return;
        if (listeners.contains (listener))
            listeners.remove (listener);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.handler.command.ICommandProcessorExecutor#process(com.effacy.jui.rpc.handler.command.ICommandProcessorContext,
     *      com.effacy.jui.rpc.handler.client.command.shared.ICommand[])
     */
    @SuppressWarnings("unchecked")
    public Object process(C context, ICommand... commands) throws NoProcessorException, ProcessorException {
        if (context instanceof IResolverRegistryInjectable)
            ((IResolverRegistryInjectable<C>) context).setRegistry (registry);
        Object outcome = null;
        for (ICommand cmd : commands) {
            if (cmd != null) {
                try {
                    long startTime = System.currentTimeMillis ();
                    outcome = onCommandProcessed (context, cmd, context.resolve (cmd, null));
                    if (listeners != null) {
                        for (ICommandExecutorListener<C> listener : listeners) {
                            try {
                                listener.onCommandProcessed (context, cmd, outcome, System.currentTimeMillis () - startTime);
                            } catch (Throwable e) {
                                // Absorb.
                            }
                        }
                    }
                } catch (ProcessorException e) {
                    dispatchCommandInError (context, cmd, e);
                    throw e;
                } catch (RuntimeException e) {
                    dispatchCommandInError (context, cmd, e);
                    throw e;
                }
            }
        }
        return outcome;
    }


    /**
     * Convenience.
     */
    private void dispatchCommandInError(C context, ICommand command, Throwable exception) {
        onCommandProcessedInError (context, command, exception);
        if (listeners != null) {
            for (ICommandExecutorListener<C> listener : listeners) {
                try {
                    listener.onCommandProcessedInError (context, command, exception);
                } catch (Throwable e) {
                    // Absorb.
                }
            }
        }
    }


    /**
     * Invoked when a command has been processed. Allows for the modification of
     * the outcome.
     * <p>
     * Take care as exceptions from this will be filtered upward.
     * 
     * @param command
     *            the command.
     * @param outcome
     *            the outcome of the command.
     * @return the (possibly modified) outcome.
     */
    protected Object onCommandProcessed(C context, ICommand command, Object outcome) {
        return outcome;
    }


    /**
     * Invoked when a command has been processed but results in an error.
     * <p>
     * Take care as exceptions from this will be filtered upward.
     * 
     * @param command
     *            the command.
     * @param exception
     *            the exception arising.
     */
    protected void onCommandProcessedInError(C context, ICommand command, Throwable exception) {
        // Nothing.
    }

}
