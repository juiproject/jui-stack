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

import java.util.function.Consumer;

import com.effacy.jui.core.client.store.IStore;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.playground.ui.tutorial.DashboardResult.Data;
import com.effacy.jui.playground.ui.tutorial.DashboardResult.Data.Period;
import com.effacy.jui.playground.ui.tutorial.DashboardResult.Page;

public class MockDataService {

    /**
     * Mocks the loading of data from the server. This is purposely simplified and
     * does not include error handling.
     * 
     * @param receiver invoked when the data is loaded.
     */
    public static void queryDashboardData(Consumer<DashboardResult> receiver) {
        // Here we mimic a loading of data simulating a delay of 300ms.
        TimerSupport.timer (() -> {
            DashboardResult data = new DashboardResult ();

            data.getTrafficData ().add (new Data (Period.THIS_WEEK, 23, 45, 75, 22, 45, 78, 55));
            data.getTrafficData ().add (new Data (Period.LAST_WEEK, 20, 5, 40, 35, 50, 70, 80));
            data.getTrafficData ().add (new Data (Period.WEEKLY_AV, 28, 34, 67, 3, 23, 46, 100));
            data.getTrafficData ().add (new Data (Period.MONTHLY_AV, 8, 8, 23, 56, 88, 43, 22));

            data.getUserData ().add (new Data (Period.THIS_WEEK, 23, 45, 75, 22, 45, 78, 55));
            data.getUserData ().add (new Data (Period.LAST_WEEK, 20, 5, 40, 35, 50, 70, 80));
            data.getUserData ().add (new Data (Period.WEEKLY_AV, 28, 34, 67, 3, 23, 46, 100));
            data.getUserData ().add (new Data (Period.MONTHLY_AV, 8, 8, 23, 56, 88, 43, 22));

            data.getTopPages ().add (new Page ("Home", "Landing page", 63, 2304));
            data.getTopPages ().add (new Page ("About", "About page", 22, 892));
            data.getTopPages ().add (new Page ("Investment", "About > Investment", 10, 261));
            data.getTopPages ().add (new Page ("People", "About > People", 8, 201));

            receiver.accept (data);
        }, 300);
    }
}
