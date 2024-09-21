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

import java.util.Map;

/**
 * A special type of contributor that injects content at every opportunity.
 * 
 * @author Jeremy Buckley
 */
public class InjectionContributor<C> extends DelegatingContributor<C> {

    /**
     * The content to inject.
     */
    private Map<String, Object> injection;


    /**
     * Constructs with content to inject (everywhere).
     * 
     * @param injection
     *            the content to inject.
     */
    public InjectionContributor(Map<String, ?> injection) {
        this (null, injection);
    }


    /**
     * Constructs with content to inject (everywhere).
     * 
     * @param injection
     *            the content to inject.
     */
    @SuppressWarnings("unchecked")
    public InjectionContributor(IContributor<C> delegate, Map<String, ?> injection) {
        super (delegate);
        this.injection = (Map<String, Object>) injection;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.json.client.IContributor#contribute(java.lang.Object,
     *      com.effacy.jui.json.client.IContributorInjector)
     */
    @Override
    public void contribute(C obj, IContributorInjector injector) {
        super.contribute (obj, injector);
        if (injection != null) {
            for (String key : injection.keySet ())
                injector.put (key, injection.get (key));
        }
    }

}
