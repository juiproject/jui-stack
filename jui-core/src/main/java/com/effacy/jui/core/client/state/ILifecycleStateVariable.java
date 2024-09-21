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
package com.effacy.jui.core.client.state;

/**
 * A type of {@link IStateVariable} that include a lifecycle state of the data
 * being held.
 */
public interface ILifecycleStateVariable<S, V extends ILifecycleStateVariable<S,V>> extends IStateVariable<V> {
    
    /**
     * Obtains the lifecycle state.
     * 
     * @return the state.
     */
    public S state();

    /**
     * Determines if the lifecycle state is OK. This means that the data is in a
     * freely accessible state.
     * <p>
     * It is possible that is is mapped to more than one state were the state
     * deconstructs OK into marginal variations. For example a list could be loaded
     * and loaded but filters (so is not a full representated of the underlying
     * data). This is a valid lifecycle state but both represent valid data.
     * 
     * @return {@code true} if is.
     */
    public boolean isOk();
}
