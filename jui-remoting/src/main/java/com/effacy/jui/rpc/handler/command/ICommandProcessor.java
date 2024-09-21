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

import com.effacy.jui.rpc.handler.client.command.ICommand;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

/**
 * A resolves takes an instance of a source class and converts it (resolves it)
 * to an instance of the target class. The source class is generally a command
 * while the target is the entity associated with the application of the
 * command.
 * 
 * @author Jeremy Buckley
 */
public interface ICommandProcessor<S extends ICommand, T, C extends ICommandProcessorContext<C>> {

    /**
     * Resolves the passed source (command) to an instance of the target class.
     * 
     * @param source
     *            the source instance to resolve.
     * @param context
     *            the context to use for lookups.
     * @return Instance of the target class.
     * @throws ProcessorException
     *             whenever there is an error.
     */
    public T resolve(S command, C context) throws NoProcessorException, ProcessorException;


    /**
     * Obtains the source class.
     * 
     * @return The source class.
     */
    public Class<S> command();


    /**
     * Obtains the target class.
     * 
     * @return The target class.
     */
    public Class<T> target();
}
