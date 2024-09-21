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

public interface ICommandProcessorExecutor<C extends ICommandProcessorContext<C>> {

    /**
     * Processes a sequence of command in a given context. Generally a public
     * method is created that will create an appropriate context.
     * 
     * @param context
     *            the context to process the group of commands in.
     * @param commands
     *            the commands to process.
     * @return The result of the last comman processed.
     * @throws ProcessorException
     *             on (application) error.
     */
    public Object process(C context, ICommand... commands) throws NoProcessorException, ProcessorException;
}
