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
package com.effacy.jui.playground.ui.tutorial.reference;

import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.playground.ui.tutorial.DashboardResult;
import com.effacy.jui.playground.ui.tutorial.MockDataService;
import com.effacy.jui.playground.ui.tutorial.DashboardResult.Data.Period;

import elemental2.dom.Element;

public class Dashboard extends Component<Component.Config> implements INavigationAware, IResetable {

    /**
     * The data backing the dashboard.
     */
    private DashboardResult value;

    /**
     * Unique users graph.
     */
    private ChartComponent users = new ChartComponent.Config () //
        .title ("Unique users by day") //
        .type (ChartComponent.Config.Type.BAR) //
        .labels ("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") //
        .option ("This week", () -> (value == null) ? null : value.findUserData (Period.THIS_WEEK)) //
        .option ("Last week", () -> (value == null) ? null : value.findUserData (Period.LAST_WEEK)) //
        .option ("Weekly average", () -> (value == null) ? null : value.findUserData (Period.WEEKLY_AV)) //
        .option ("Monthly average", () -> (value == null) ? null : value.findUserData (Period.MONTHLY_AV)).build ();

    /**
     * Traffic graph.
     */
    private ChartComponent traffic = new ChartComponent.Config () //
        .title ("Trafffic by day") //
        .type (ChartComponent.Config.Type.RADAR) //
        .labels ("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") //
        .option ("This week", () -> (value == null) ? null : value.findTrafficData (Period.THIS_WEEK)) //
        .option ("Last week", () -> (value == null) ? null : value.findTrafficData (Period.LAST_WEEK)) //
        .option ("Weekly average", () -> (value == null) ? null : value.findTrafficData (Period.WEEKLY_AV)) //
        .option ("Monthly average", () -> (value == null) ? null : value.findTrafficData (Period.MONTHLY_AV)).build ();

    /**
     * Summary panel of top-pages.
     */
    private Summarizer<DashboardResult> summary = new Summarizer<DashboardResult> (item -> {
        item.title (d -> "Top pages").subtitle (d -> "Most accessed pages by unique users");
        item.section (d -> d.getTopPages (), section -> section.title (d -> d.getName ()) //
            .subtitle (d -> d.getDescription ()) //
            .percent (d -> d.getPercentage ()) //
            .quantity (d -> d.getHits ()));
    });

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return DomBuilder.el (el, root -> {
            root.addClassName ("dashboard");
            root.div (upper -> {
                upper.addClassName ("upper");
                upper.div ().addClassName ("item", "item11") //
                        .apply (attach (users));
                upper.div ().addClassName ("item", "item12") //
                        .apply (attach (traffic));
            });
            root.div (lower -> {
                lower.addClassName ("lower");
                lower.div ().addClassName ("item", "item21") //
                        .apply (attach (new UsersGallery ()));
                lower.div ().addClassName ("item", "item22") //
                        .apply (attach (summary));
            });
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
            update(data);
            unmask();
        });        
    }

    /**
     * Updates the dashboard with new data.
     */
    public void update(DashboardResult data) {
        this.value = data;
        summary.update (data);
        reset ();
    }

}
