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

import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;

import elemental2.dom.Element;

public class Dashboard extends Component<Component.Config> implements INavigationAware, IResetable {

    // TODO: The dashboard data.
    private DashboardResult value;

    // TODO: The unique users graph.
    private ChartComponent users;

    // TODO: The traffic graph.
    private ChartComponent traffic;

    /**
     * Summary panel of top-pages.
     */
    private Summarizer<DashboardResult> summary;

    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return DomBuilder.el (el, root -> {
            // TODO: Build out component.
        }).build ();
    }
    

    /**
     * Invoked when navigated to, here we perform a data load for the dashboard.
     *
     * @see com.effacy.jui.core.client.navigation.INavigationAware#onNavigateTo(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext)
     */
    @Override
    public void onNavigateTo(NavigationContext context) {
        // Mask during loading, this will be unmasked when loaded.
        mask ();

        // Load the data.
        MockDataService.queryDashboardData(data -> {
            update (data);
            unmask ();
        });        
    }

    /**
     * Updates the dashboard with new data.
     */
    public void update(DashboardResult data) {
        this.value = data;
        // TODO: Update the summary component.
        reset ();
    }
}
