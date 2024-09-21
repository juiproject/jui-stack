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
package com.effacy.jui.json.client;

/**
 * An injector is what a {@link IContributor} uses to inject its contribution to
 * the serialization process.
 * 
 * @author Jeremy Buckley
 */
public interface IContributorInjector {

    /**
     * Contribute a key-value pair to the serialized object. If a key matches a
     * property of the associated object being serialized then the property will
     * be overridden.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value to assign (this will be serialized through normal
     *            process).
     */
    public void put(String key, Object value);


    /**
     * Indicates that the serialization process is being overridden by this
     * contributor rather than the contributor contributing to the process.
     */
    public void override();
}
