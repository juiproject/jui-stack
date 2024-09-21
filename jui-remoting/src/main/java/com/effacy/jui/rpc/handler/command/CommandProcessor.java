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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.exception.SystemProcessorException;

/**
 * Support class for implementing {@link ICommandProcessor}'s.
 * 
 * @author Jeremy Buckley
 */
public abstract class CommandProcessor<CMD extends ICommand, ETY, CTX extends ICommandProcessorContext<CTX>> implements ICommandProcessor<CMD, ETY, CTX> {

    /**
     * The source class.
     */
    private Class<CMD> command;

    /**
     * The target class.
     */
    private Class<ETY> target;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger (CommandProcessor.class);

    /**
     * Construct with prescribed source and target classes.
     * 
     * @param source
     *               the source class.
     * @param target
     *               the target class.
     */
    protected CommandProcessor(Class<CMD> command, Class<ETY> target) {
        this.command = command;
        this.target = target;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.command.ICommandProcessor#command()
     */
    @Override
    public Class<CMD> command() {
        return command;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.command.ICommandProcessor#target()
     */
    @Override
    public Class<ETY> target() {
        return target;
    }

    /**
     * Converts the passed throwable to a {@link ProcessorException} and throws
     * that.
     * <p>
     * This is useful for implementations of
     * {@link ICommandProcessor#resolve(ICommand, ICommandProcessorContext)} to
     * translate exception from within the application to standard ones.
     * 
     * @param e
     *          the exception to convert.
     * @throws ProcessorException
     *                            the associated compliant exception.
     */
    protected void throwFor(Throwable e) throws ProcessorException {
        if (e instanceof ProcessorException)
            throw (ProcessorException) e;
        log (e);
        throw new SystemProcessorException (e);
    }

    /**
     * Logs an uncaught exception.
     * 
     * @param e
     *          the exception.
     */
    protected void log(Throwable e) {
        LOG.error ("Uncaught exception", e);
    }

}
