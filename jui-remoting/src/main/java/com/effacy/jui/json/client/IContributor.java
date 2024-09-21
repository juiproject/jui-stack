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
 * When serializing an object to JSON the contributor allows for the injection
 * of additional key-value pairs into json objects that are created during the
 * process.
 * <p>
 * This is generally used to add enhanced properties that can be used for
 * additional purposes.
 * 
 * @author Jeremy Buckley
 */
public interface IContributor<C> {

    /**
     * Contributes to the serialization of the given object.
     * 
     * @param obj
     *            the object to contribute to.
     * @param injector
     *            the injector for the contribution.
     */
    public void contribute(C obj, IContributorInjector injector);
}
