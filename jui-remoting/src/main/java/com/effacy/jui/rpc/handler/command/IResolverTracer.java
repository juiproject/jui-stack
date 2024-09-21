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
import com.effacy.jui.rpc.handler.exception.ProcessorException;

/**
 * Can be used to trace resolution of command.
 *
 * @author Jeremy Buckley
 */
public interface IResolverTracer {

    /**
     * The nature by which the resolution was performed.
     */
    public enum ResolutionType {
        LOOKUP, CACHE, RESOLVER;
    }

    /**
     * Called prior to resolving a command.
     * 
     * @param command
     *            the command being resolved.
     */
    public void resolving(ICommand command);


    /**
     * Called after a command has been resolved.
     * 
     * @param command
     *            the command that was resolved.
     * @param entity
     *            the resolved entity.
     * @param type
     *            the manner in which the resolution was performed.
     */
    public void resolved(ICommand command, Object entity, ResolutionType type);


    /**
     * Called if resolving a command failed.
     * 
     * @param command
     *            the command that failed.
     * @param error
     *            the exception being reported.
     */
    public void failed(ICommand command, ProcessorException error);
}
