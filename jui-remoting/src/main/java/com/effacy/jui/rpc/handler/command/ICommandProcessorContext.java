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
 * Interface for a context object that is passed through the sequence of command
 * resolutions. The context provides support for recursive command resolution,
 * lookup of previously resolved objects bound to the associated commands
 * reference and caching of custom objects by reference lookup (though this is
 * generally rarely used).
 *
 * @author Jeremy Buckley
 */
public interface ICommandProcessorContext<C extends ICommandProcessorContext<C>> {

    /**
     * Resolves a command and returns an instance of the object to which the
     * command has been applied. It is expected that the returned object will
     * have been saved.
     * 
     * @param command
     *            the command to resolve.
     * @param target
     *            the class type that the command resolves to.
     * @return the resolved (and possibly modified) object referenced by the
     *         command.
     * @throws ProcessorException
     *             if there was a problem.
     */
    public <S extends ICommand, T> T resolve(S command, Class<T> target) throws NoProcessorException, ProcessorException;

}
