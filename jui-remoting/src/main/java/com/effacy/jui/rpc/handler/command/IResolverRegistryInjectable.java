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

/**
 * Implemented by classes that can have a registry injected into them.
 *
 * @author Jeremy Buckley
 */
public interface IResolverRegistryInjectable<C extends ICommandProcessorContext<C>> {

    /**
     * Assigns a registry to the injectable.
     * 
     * @param registry
     *            the registry to assign.
     */
    public void setRegistry(ICommandProcessorRegistry<C> registry);
}
