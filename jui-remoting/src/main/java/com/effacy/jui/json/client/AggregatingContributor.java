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

import java.util.ArrayList;
import java.util.List;

/**
 * An aggregating contributors that maintains an internal list of contributors
 * and processes each individually when contributing itself.
 * 
 * @author Jeremy Buckley
 */
public class AggregatingContributor<C> implements IContributor<C> {

    /**
     * The list of contributors in the aggregate.
     */
    private List<IContributor<C>> contributors = new ArrayList<IContributor<C>> ();


    /**
     * Adds a contributor to the aggregate.
     * 
     * @param contributor
     *            the contributor to add.
     */
    public void add(IContributor<C> contributor) {
        if (!contributors.contains (contributor))
            contributors.add (contributor);
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.json.client.IContributor#contribute(java.lang.Object,
     *      com.effacy.jui.json.client.IContributorInjector)
     */
    @Override
    public void contribute(C obj, IContributorInjector injector) {
        if ((obj != null) && !contributors.isEmpty ()) {
            for (IContributor<C> contributor : contributors)
                contributor.contribute (obj, injector);
        }
    }
}
