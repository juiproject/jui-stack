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
package com.effacy.jui.playground.ui.lessons;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.effacy.jui.core.client.MockData;
import com.effacy.jui.core.client.store.ListPaginatedStore;
import com.effacy.jui.core.client.util.Random;

public class SampleRecordStore extends ListPaginatedStore<SampleRecord> {

    @Override
    protected void populate(List<SampleRecord> records) {
        for (int i = 0; i < 100; i++) {
            Date date = new Date (new Date().getTime() - Random.nextInt (200) * 24 * 60 * 60 * 1000);
            records.add (new SampleRecord (MockData.NAMES[i], MockData.EMAILS[i], Random.nextInt (100), date, Random.nextInt (4) + 1));
        }
        Collections.sort (records (), (a, b) -> {
            String av = ((SampleRecord) a).getName ();
            String bv = ((SampleRecord) b).getName ();
            return av.compareTo (bv);
        });
    }

    public void sortByName(boolean asc) {
        Collections.sort (records (), (a, b) -> {
            String av = ((SampleRecord) a).getName ();
            String bv = ((SampleRecord) b).getName ();
            return !asc ? av.compareTo (bv) : bv.compareTo (av);
        });
        reload (10);
    }

    public void sortByRating(boolean asc) {
        Collections.sort (records (), (a, b) -> {
            int av = ((SampleRecord) a).getRating ();
            int bv = ((SampleRecord) b).getRating ();
            return !asc ? (av - bv) : (bv - av);
        });
        reload (10);
    }

}