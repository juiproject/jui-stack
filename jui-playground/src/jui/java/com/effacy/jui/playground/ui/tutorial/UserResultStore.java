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
package com.effacy.jui.playground.ui.tutorial;

import java.util.Collections;
import java.util.List;

import com.effacy.jui.core.client.store.ListPaginatedStore;
import com.effacy.jui.core.client.util.Random;

/**
 * Mock data store for holding {@link UserResult}'s. In a real case you would
 * implement a store that remotely loads data.
 */
public class UserResultStore extends ListPaginatedStore<UserResult> {

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.playground.ui.gallery.ListPaginatedStore#populate(java.util.List)
     */
    @Override
    protected void populate(List<UserResult> records) {
        for (int i = 0; i < 100; i++)
            records.add (new UserResult (Names.NAMES[i], Random.nextInt (99) + 1, null, Random.nextInt (4) + 1));
        Collections.sort (records (), (a, b) -> {
            String av = ((UserResult) a).getName ();
            String bv = ((UserResult) b).getName ();
            return av.compareTo (bv);
        });
    }

    public void sortByName(boolean asc) {
        Collections.sort (records (), (a, b) -> {
            String av = ((UserResult) a).getName ();
            String bv = ((UserResult) b).getName ();
            return !asc ? av.compareTo (bv) : bv.compareTo (av);
        });
        reload (10);
    }

    public void sortByAccess(boolean asc) {
        Collections.sort (records (), (a, b) -> {
            int av = ((UserResult) a).getVisits ();
            int bv = ((UserResult) b).getVisits ();
            int v = asc ? -1 : 1;
            if (av < bv)
                return v;
            if (av == bv)
                return 0;
            return -1 * v;
        });
        reload (10);
    }
}
