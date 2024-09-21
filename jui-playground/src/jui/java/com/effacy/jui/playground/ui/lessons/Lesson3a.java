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

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.util.Random;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.tabs.TabSet;
import com.effacy.jui.ui.client.tabs.TabbedPanel;
import com.effacy.jui.ui.client.tabs.TabbedPanelCreator;

import elemental2.dom.Element;

public class Lesson3a extends LessonPanel {

    protected Lesson3a() {
        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part A: Tabbed navigation", header -> {
                header.subtitle ("This lession describes how to build simple tabbed navigation.");
            });
        })).update (null);

        add (TabbedPanelCreator.create(cfg -> {
            cfg.style (TabSet.Config.Style.VERTICAL);
            cfg.padding (Insets.em (0));
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            tabs.tab ("tab1", "Tab 1", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 1");
            })).icon (FontAwesome.airFreshener ());
            tabs.tab ("tab2", "Tab 2", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 2");
            })).icon (FontAwesome.allergies ());
            tabs.tab ("tab3", "Tab 3", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 3");
            })).icon (FontAwesome.user ());
            tabs.tab ("tab4", "Tab 4", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 4");
            })).icon (FontAwesome.cow ());
        }));

        add (TabbedPanelCreator.create(cfg -> {
            cfg.style (TabSet.Config.Style.VERTICAL);
            cfg.padding (Insets.em (0));
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            
            tabs.group ("Group 1");
            tabs.tab ("tab1", "Tab 1", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 1");
            })).icon (FontAwesome.airFreshener ());
            tabs.tab ("tab2", "Tab 2", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 2");
            })).icon (FontAwesome.allergies ());

            tabs.group ("Group 2");
            tabs.tab ("tab3", "Tab 3", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 3");
            })).icon (FontAwesome.user ());
            tabs.tab ("tab4", "Tab 4", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 4");
            })).icon (FontAwesome.cow ());

            tabs.group (null).expand ();
            tabs.tab ("settings", "Settings", ComponentCreator.build (r -> {
                P.$ (r).text ("Settings here");
            })).icon (FontAwesome.cog ());
        }));

        add (TabbedPanelCreator.create(cfg -> {
            cfg.style (TabSet.Config.Style.VERTICAL);
            cfg.padding (Insets.em (0));
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            
            // First set of tabs.
            tabs.tab ("tab1", "Tab 1", TabbedPanelCreator.create (cfg -> {
                cfg.style (TabSet.Config.Style.HORIZONTAL_BAR);
                cfg.padding (Insets.em (1));
            }, tabs2 -> {
                tabs2.tab ("taba", "Tab A", ComponentCreator.build (r -> {
                    P.$ (r).text ("This is tab 1.A");
                }));
                tabs2.tab ("tabb", "Tab B", ComponentCreator.build (r -> {
                    P.$ (r).text ("This is tab 1.B");
                }));
            }));

            // Second set of tabs.
            tabs.tab ("tab2", "Tab 2", TabbedPanelCreator.create (cfg -> {
                cfg.style (TabSet.Config.Style.HORIZONTAL_UNDERLINE);
                cfg.padding (Insets.em (1));
            }, tabs2 -> {
                tabs2.tab ("taba", "Tab A", ComponentCreator.build (r -> {
                    P.$ (r).text ("This is tab 2.A");
                }));
                tabs2.tab ("tabb", "Tab B", ComponentCreator.build (r -> {
                    P.$ (r).text ("This is tab 2.B");
                }));
            }));
        }));

        add (TabbedPanelCreator.create(cfg -> {
            cfg.style (TabSet.Config.Style.VERTICAL);
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            tabs.tab ("tab1", "Tab 1", new AwareComponent ("This is tab 1"));
            tabs.tab ("tab2", "Tab 2", ComponentCreator.build (r -> {
                P.$ (r).text ("This is tab 2");
            }));
        }));

        // Example 1
        add (TabbedPanelCreator.create(cfg -> {
            cfg.style (TabSet.Config.Style.VERTICAL);
            cfg.padding (Insets.em (0));
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            tabs.tab ("tab1", "Tab 1", new ExampleA1 (TabSet.Config.Style.HORIZONTAL_BAR));
            tabs.tab ("tab2", "Tab 2", new ExampleA1 (TabSet.Config.Style.HORIZONTAL_UNDERLINE));
        }));

        // Example 2
        add (TabbedPanelCreator.create(cfg -> {
            cfg.style (TabSet.Config.Style.VERTICAL);
            cfg.padding (Insets.em (1));
            cfg.effect (CardFitLayout.Config.Effect.FADE_IN);
        }, tabs -> {
            tabs.css (el -> CSS.HEIGHT.apply(el, Length.px (400)));
            tabs.tab ("tab1", "Tab 1", new ExampleA2 ());
            tabs.tab ("tab2", "Tab 2", new ExampleA2 ());
        }));
    }

    public static class AwareComponent extends SimpleComponent implements INavigationAware {
        
        public AwareComponent(String content) {
            renderer (root -> {
                P.$ (root).text (content);
            });
        }

        @Override
        public void onNavigateTo(NavigationContext context) {
            Logger.info ("Navigated to");
        }

        @Override
        public void onNavigateFrom(INavigateCallback cb) {
            NotificationDialog.confirm (
                "Are you sure?",
                "Are you sure that you want to navigate away from this page?",
                outcome -> {
                if (NotificationDialog.OutcomeType.OK == outcome)
                    cb.proceed ();
            });
        }

    }

    public static class ExampleA1 extends TabbedPanel {

        public ExampleA1(TabSet.Config.Style style) {
            super (new TabbedPanel.Config().style (style));

            tab ("taba", "Tab A", ComponentCreator.build(root -> {
                P.$ (root).text ("Tab A (componentUuid=" + this.getUUID() + ")");
            }));
            tab ("tabb", "Tab B", ComponentCreator.build(root -> {
                P.$ (root).text ("Tab B (componentUuid=" + this.getUUID() + ")");
            }));
        }
    }

    public static class ExampleA2 extends SimpleComponent implements INavigationAware {

        private Element contentsEl;

        private String [] sentences = new String [] {
            "The cat slept on the warm windowsill.",
            "He baked cookies for the school fundraiser.",
            "She went hiking in the nearby forest.",
            "The dog chased the ball with enthusiasm.",
            "She painted the fence blue last weekend.",
            "The sun set behind the tall mountains.",
            "He read a book under the old tree.",
            "The kids played soccer at the local park.",
            "She fixed the broken bike before dinner.",
            "The flowers bloomed in the spring garden."
        };

        public ExampleA2() {
            renderer(root -> {
                P.$(root).by ("contents").text ("Initial content on render");
            }, dom -> {
                contentsEl = dom.first("contents");
            });
        }

        @Override
        public void onNavigateTo(NavigationContext context) {
            if (contentsEl != null)
                contentsEl.innerHTML = sentences[Random.nextInt (sentences.length)];
        }

    }
}
