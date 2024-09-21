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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.Header;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Text;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationHandler;
import com.effacy.jui.core.client.navigation.INavigationHandlerWithProvider;
import com.effacy.jui.core.client.navigation.NavigationHandlerRouter;
import com.effacy.jui.core.client.navigation.NavigationSupport;
import com.effacy.jui.core.client.navigation.NavigationHandlerRouter.RegistrationItem;
import com.effacy.jui.core.client.util.TriConsumer;
import com.effacy.jui.platform.util.client.Promise;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;

public class Lesson3c extends LessonPanel implements INavigationHandlerWithProvider {

    private ReportNavigation reportNavigation;

    protected Lesson3c() {
        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part C: Custom navigation", header -> {
                header.subtitle ("This lession explores creating a custom navigation component.");
            });
        })).update (null);

        List<ReportNavigation.Report> reports = new ArrayList<>();
        reports.add (new ReportNavigation.Report ("report1", "Report 1", "The first interactive report", ComponentCreator.build (root -> {
            P.$ (root).text ("Interactive report 1");
        })));
        reports.add (new ReportNavigation.Report ("report2", "Report 2", "The second interactive report", ComponentCreator.build (root -> {
            P.$ (root).text ("Interactive report 2");
        })));
        reports.add (new ReportNavigation.Report ("report3", "Report 3", "The third interactive report", ComponentCreator.build (root -> {
            P.$ (root).text ("Interactive report 3");
        })));
        reports.add (new ReportNavigation.Report ("report4", "Report 4", "The fourth interactive report", ComponentCreator.build (root -> {
            P.$ (root).text ("Interactive report 4");
        })));
        add (reportNavigation = new ReportNavigation (reports))
            .css (el -> CSS.HEIGHT.apply (el, Length.px (400)));
    }

    @Override
    public INavigationHandler handler() {
        return reportNavigation.handler ();
    }

    public static class ReportNavigation extends SimpleComponent implements INavigationHandlerWithProvider {

        public static class Report extends RegistrationItem {

            private String reference;

            private String title;

            private String description;

            public Report(String reference, String title, String description, IComponent component) {
                super (component);
                this.reference = reference;
                this.title = title;
                this.description = description;
                this.component = component;
            }
        }

        private static final String REGION_BODY = "body";

        private Element headerEl;

        private List<Report> reports;

        public ReportNavigation(List<Report> reports) {
            this.reports = reports;

            reports.forEach (report -> {
                findRegionPoint (REGION_BODY).add (report.component ());
                navigationRouter.register (report);
            });
        }

        @Override
        protected INodeProvider buildNode(Element el, Config data) {
            return Wrap.$ (el).$ (root -> { 
                root.style ("lesson3_navigation");
                Div.$ (root).$ (wrap -> {
                    wrap.style ("wrap");
                    Header.$ (wrap).$ (header -> {
                        header.by ("header");
                        H2.$ (header).text ("Reports");
                    });
                    Div.$ (wrap).$ (body -> {
                        body.style ("body");
                        body.use (region (REGION_BODY, new CardFitLayout.Config ().build ()));
                    });
                    Div.$ (wrap).$ (cards -> {
                        cards.style ("card");
                        Div.$ (cards).style ("wrap").$ (inner -> {
                            reports.forEach (card -> {
                                Div.$ (inner).$ (
                                    H3.$().text (card.title),
                                    P.$ ().text (card.description)
                                ).onclick (e -> {
                                    navigationRouter.navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false), card.reference);
                                });
                            });
                        });
                    });
                });
            }).build (dom -> {
                headerEl = dom.first ("header");
            });
        }

        private Object TOP = new Object();

        private NavigationHandlerRouter navigationRouter = new NavigationHandlerRouter () {

            @Override
            protected void onNavigationForward(NavigationContext context, List<String> path, TriConsumer<NavigationContext, List<String>, Object> propagator) {
                Report card = null;
                List<String> childPath = NavigationSupport.copy (path);
                if ((path != null) && !path.isEmpty ()) {
                    String child = path.get(0);
                    childPath.remove(0);
                    for (Report report : reports) {
                        if (child.equals(report.reference))
                            card = report;
                    }
                }

                // Use TOP for the top-level (when there is no child path).
                propagator.accept (context, childPath, (card != null) ? card : TOP);
            }

            @Override
            protected Promise<ActivateOutcome> onChildActivated(Object child) {
                if (TOP == child) {
                    getRoot().classList.remove("body");
                    buildInto(headerEl, header -> {
                        H2.$ (header).text ("Reports");
                    });
                    return Promise.create (ActivateOutcome.ACTIVATED);
                }

                getRoot().classList.add("body");
                Report card = (Report) child;
                IComponent cpt = card.component ();
                Promise<ActivateOutcome> promise = Promise.create ();
                buildInto(headerEl, header -> {
                    Div.$ (header).style("crumb").$ (crumb -> {
                        Span.$ (crumb).onclick (e -> {
                            navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false));
                        }).text ("Reports");
                        Em.$ (crumb).style (FontAwesome.chevronRight ());
                        Span.$ (crumb).text (card.title);
                    });
                    H2.$ (header).$ (
                        A.$ ().$ (
                            Em.$ ().style(FontAwesome.chevronLeft ())
                        ).onclick(e -> {
                            navigate (new NavigationContext (NavigationContext.Source.INTERNAL, false));
                        }),
                        Text.$ (card.title)
                    );
                });
                ((CardFitLayout) findRegionPoint (REGION_BODY).getLayout ()).activate (cpt).onFulfillment (v -> promise.fulfill (v));
                return promise;
            }
        };

        @Override
        public INavigationHandler handler() {
            return navigationRouter;
        }
    }

}