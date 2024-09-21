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
 * Generally used to wrap another contributor while contributing in ones own
 * right.
 * 
 * @author Jeremy Buckley
 */
public class DelegatingContributor<C> implements IContributor<C> {

    /**
     * An optional contributor to delegate to.
     */
    private IContributor<C> delegate;


    /**
     * Construct with a contributor to delegate to.
     * 
     * @param delegate
     *            the delegate.
     */
    public DelegatingContributor(IContributor<C> delegate) {
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.json.client.IContributor#contribute(java.lang.Object,
     *      com.effacy.jui.json.client.IContributorInjector)
     */
    @Override
    public void contribute(C obj, IContributorInjector injector) {
        if (delegate != null)
            delegate.contribute (obj, injector);
    }

}
