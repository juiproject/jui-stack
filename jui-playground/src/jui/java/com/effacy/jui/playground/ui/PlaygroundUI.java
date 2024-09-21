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
package com.effacy.jui.playground.ui;

import com.effacy.jui.core.client.Debug;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.playground.ui.control.ControlSection;
import com.effacy.jui.playground.ui.dialogs.DialogExamples;
import com.effacy.jui.playground.ui.editor.EditorExamples;
import com.effacy.jui.playground.ui.editor.EditorShowcase;
import com.effacy.jui.playground.ui.gallery.GalleryExamples;
import com.effacy.jui.playground.ui.lessons.Lessons;
import com.effacy.jui.playground.ui.samples.Samples;
import com.effacy.jui.playground.ui.shared.TextComponent;
import com.effacy.jui.playground.ui.testing.ControlSuite;
import com.effacy.jui.playground.ui.tutorial.reference.Tutorial;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.NotificationDialog.OutcomeType;
import com.effacy.jui.ui.client.explorer.ComponentExplorer;
import com.effacy.jui.ui.client.explorer.Themes;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;
import com.effacy.jui.ui.client.panel.PanelCreator;

/**
 * The main application component. This contains (as tabs) the various clusters
 * of components for demonstration (and testing) purposes.
 *
 * @author Jeremy Buckley
 */
public class PlaygroundUI extends TabNavigator {

    public PlaygroundUI() {
        super (new TabNavigator.Config ().style (TabNavigator.Config.Style.VERTICAL).padding (Insets.em (0)));
        
        group ("Getting started");
        tab ("lessions", "Lessons", new Lessons ());

        // We need to do this since Charba (for the charts) does not operate well with
        // HtmlUnit (makes use of the Reflect API which has not been fully implemented
        // by HtmlUnit).
        if (!Debug.isTestMode()) {
            tab ("tutorial", "Tutorial", new Tutorial ());
        } else {
            tab("tutorial", "Tutorial",
                PanelCreator.build (cfg -> cfg.padding (Insets.em (1)), builder -> builder.add (ComponentCreator.build (root -> {
                    P.$ (root).text ("The tutorial is not enabled in test mode due to complications between HtmlUnit and Charba");
                }))));
        }
        
        group ("Cookbook");
        tab ("samples", "Samples", new Samples ());
        tab ("controls", "Controls", new ControlSection ());
        tab ("gallery", "Galleries", new GalleryExamples ());
        tab ("editor", "Rich text editor", new EditorExamples ());
        tab ("peditor", "Page editor", new EditorShowcase ());
        tab ("dialogs", "Dialogs & modals", new DialogExamples ());

        group ("Library");
        tab ("library-themes", "Theme", new Themes ());
        tab ("library-components", "Components", TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_BAR);
            ComponentExplorer.components (cfg);
            cfg.tab("a", "Panel", PanelCreator.build());
            cfg.tab("b", "Accordion", PanelCreator.build());
            cfg.tab("c", "Table", PanelCreator.build());
            cfg.tab("d", "Gallery", PanelCreator.build());
        }));
        tab ("library-controls", "Controls", TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_BAR);
            ComponentExplorer.controls (cfg);
        }));
        tab ("library-fragments", "Fragments", TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_BAR);
            ComponentExplorer.fragments (cfg);
        }));

        group ("Test suite");
        tab("tabs", "Tabs", TabNavigatorCreator.create (tab1 -> {
            tab1.style (TabNavigator.Config.Style.VERTICAL);
            tab1.tab ("tab1", "Standard", TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.HORIZONTAL).padding (Insets.em (1));
                cfg.tab ("tab1", "Sub-tab 1", new TextComponent ("Sub-tab 1"));
                cfg.tab ("tab2", "Sub-tab 2", new TextComponent ("Sub-tab 2"));
            })).icon (FontAwesome.allergies ()).indicator("new");
            tab1.tab ("tab2", "Bar", TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.HORIZONTAL_BAR).padding (Insets.em (1));
                cfg.tab ("tab1", "Sub-tab 1", new TextComponent ("Sub-tab 1"));
                cfg.tab ("tab2", "Sub-tab 2", new TextComponent ("Sub-tab 2"));
            })).icon (FontAwesome.adjust ());
            tab1.tab ("tab3", "Underline", TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE).padding (Insets.em (1));
                cfg.tab ("tab1", "Sub-tab 1", new TextComponent ("Sub-tab 1"));
                cfg.tab ("tab2", "Sub-tab 2", new TextComponent ("Sub-tab 2"));
            })).icon (FontAwesome.appleAlt ());
            tab1.group ("Group");
            tab1.tab ("tab4", "Vertical alt", TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.VERTICAL_ALT).padding (Insets.em (1));
                cfg.tab ("tab1", "Sub-tab 1", new TextComponent ("Sub-tab 1"));
                cfg.tab ("tab2", "Sub-tab 2", new TextComponent ("Sub-tab 2"));
            })).icon (FontAwesome.umbrellaBeach ());
            tab1.tab ("tab5", "Compact", TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.VERTICAL_COMPACT).padding (Insets.em (1));
                cfg.tab ("tab1", "Sub-tab 1", new TextComponent ("Sub-tab 1"));
                cfg.tab ("tab2", "Sub-tab 2", new TextComponent ("Sub-tab 2"));
            })).icon (FontAwesome.airFreshener ());
            tab1.tab ("tab6", "Icon only", TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.VERTICAL_ICON).padding (Insets.em (1));
                cfg.tab ("tab1", "Sub-tab 1", new TextComponent ("Sub-tab 1")).icon (FontAwesome.users ());
                cfg.tab ("tab2", "Sub-tab 2", new TextComponent ("Sub-tab 2")).icon (FontAwesome.groupArrowsRotate ());
            })).icon (FontAwesome.airFreshener ());
            tab1.tab ("blocking", "Blocking",  new NavigationAwareComponent ()).icon (FontAwesome.notEqual ());
        }));
        tab ("testing-controls", "Controls", new ControlSuite ());
    }

    public static class NavigationAwareComponent extends Component<Component.Config> implements INavigationAware {

        @Override
        protected INodeProvider buildNode(Component.Config data) {
            return DomBuilder.div (div -> {
                div.p ().text ("This is a navigation aware component");
            }).build ();
        }

        @Override
        public void onNavigateFrom(INavigateCallback cb) {
            NotificationDialog.confirm ("Navigation check", "Are you sure you want to navigate away from this page?", t -> {
                if (t == OutcomeType.OK)
                    cb.proceed ();
            });
        }

    }

}
