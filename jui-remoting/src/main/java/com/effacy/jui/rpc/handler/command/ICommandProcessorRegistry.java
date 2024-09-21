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

/**
 * Given a pair of source and target classes obtains the resolver that resolves
 * between instances of them.
 * 
 * @author Jerenmy Buckley
 */
public interface ICommandProcessorRegistry<C extends ICommandProcessorContext<C>> {

    /**
     * Finds the resolver for the source and target classes.
     * 
     * @param source
     *            the source class.
     * @param target
     *            the target class.
     * @return The associated resolver.
     * @throws NoProcessorException
     *             if no resolver could be identified.
     */
    public <S extends ICommand, T> ICommandProcessor<S, T, C> find(Class<S> source) throws NoProcessorException;
}
