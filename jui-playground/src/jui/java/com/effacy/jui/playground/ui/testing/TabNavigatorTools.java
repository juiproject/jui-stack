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
package com.effacy.jui.playground.ui.testing;

import java.util.function.Consumer;

import com.effacy.jui.core.client.Invoker;
import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IDisposeListener;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.H4;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.playground.ui.shared.TextComponent;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.fragments.Btn;
import com.effacy.jui.ui.client.fragments.Menu;
import com.effacy.jui.ui.client.fragments.Menu.MenuFragment;
import com.effacy.jui.ui.client.fragments.MenuActivator;
import com.effacy.jui.ui.client.fragments.MenuItem;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;

import elemental2.dom.Element;

/**
 * A tab navigator with tools at the end of its strip and a hidden group (see
 * {@link TabNavigator.Config#tools(IComponent...)} and
 * {@link com.effacy.jui.ui.client.navigation.TabCollection.ITabGroupConfig#hidden()}).
 * <p>
 * The selector in the tools opens a menu reaching the pages of the hidden
 * <i>analysis</i> tab (itself a navigator with its strip hidden), by reference
 * and by path. The <i>lifecycle</i> tab mounts and unmounts a navigator with
 * tools, counting the tools that are live; the <i>variants</i> tab draws each
 * variant without and with tools, side by side.
 */
public class TabNavigatorTools extends TabNavigator {

    public TabNavigatorTools() {
        super (TabNavigatorCreator.config ().variant (TabNavigator.Config.Variant.HORIZONTAL_UNDERLINE).padding (Insets.em (1)).testId ("tabtools"));

        tab ("overview", "Overview", new TextComponent ("The selector at the right of the strip reaches the pages of a tab that is not drawn."));
        tab ("lifecycle", "Lifecycle", new Lifecycle ());
        tab ("variants", "Variants", new Variants ());

        // Reached from the selector in the tools, not from a tab.
        group ("Analysis").hidden ();
        tab ("analysis", "Analysis", TabNavigatorCreator.create (cfg -> {
            cfg.variant (TabNavigator.Config.Variant.HORIZONTAL);
            cfg.tab ("comparison", "Comparison", new TextComponent ("Comparison page"));
            cfg.tab ("moves", "Moves", new TextComponent ("Moves page"));
            cfg.tab ("threats", "Threats", new TextComponent ("Threats page"));
        }, nav -> nav.hideNavigation ()));

        tools (new AnalysisSelector (() -> activate ("analysis"), page -> navigate ("analysis", page)));
    }

    /**
     * The selector placed in the tools: a label and a click-activated menu.
     */
    public static class AnalysisSelector extends SimpleComponent {

        public AnalysisSelector(Invoker activate, Consumer<String> navigate) {
            renderer (root -> {
                root.css ("display: flex; align-items: center; gap: 0.25em;");
                Span.$ (root).testId ("tabtools.selector").text ("Analysis");
                MenuFragment menu = Menu.$ (MenuActivator.$ (root).clickToActivate ()).width (Length.em (12));
                MenuItem.$ (menu).testId ("tabtools.menu.analysis").label ("Analysis").onclick (activate);
                MenuItem.$ (menu).testId ("tabtools.menu.comparison").label ("Comparison").onclick (() -> navigate.accept ("comparison"));
                MenuItem.$ (menu).testId ("tabtools.menu.moves").label ("Moves").onclick (() -> navigate.accept ("moves"));
                MenuItem.$ (menu).testId ("tabtools.menu.threats").label ("Threats").onclick (() -> navigate.accept ("threats"));
            });
        }
    }

    /**
     * Mounts and unmounts a (vertical) navigator with tools, counting the tools
     * rendered and not yet disposed of.
     */
    public static class Lifecycle extends SimpleComponent {

        private Element holderEl;

        private Element liveEl;

        private int created;

        private int disposed;

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                Div.$ (root).css ("display: flex; gap: 0.5em; align-items: center; margin-bottom: 1em;").$ (bar -> {
                    Btn.$ (bar, "Mount").testId ("tabtools.mount").onclick (() -> mount ());
                    Btn.$ (bar, "Unmount").testId ("tabtools.unmount").onclick (() -> unmount ());
                    Btn.$ (bar, "Count").testId ("tabtools.count").onclick (() -> liveEl.textContent = (created - disposed) + " live");
                    Span.$ (bar).testId ("tabtools.live").text ("0 live").use (n -> liveEl = (Element) n);
                });
                Div.$ (root).css ("height: 240px; border: 1px solid #ddd;").use (n -> holderEl = (Element) n);
            }).build ();
        }

        protected void mount() {
            TabNavigator navigator = TabNavigatorCreator.create (cfg -> {
                cfg.variant (TabNavigator.Config.Variant.VERTICAL).testId ("tabtools.mounted");
                cfg.tab ("first", "First", new TextComponent ("First"));
                cfg.tab ("second", "Second", new TextComponent ("Second"));
                cfg.tools (tool ("tabtools.tool.a", "Tool A"), tool ("tabtools.tool.b", "Tool B"));
            });
            buildInto (holderEl, el -> Cpt.$ (el, navigator));
        }

        protected void unmount() {
            buildInto (holderEl, el -> {});
        }

        protected IComponent tool(String testId, String label) {
            Button btn = ButtonCreator.build (cfg -> {
                cfg.testId (testId);
                cfg.label (label);
            });
            created++;
            btn.addListener (IDisposeListener.create (c -> disposed++));
            return btn;
        }
    }

    /**
     * Each variant without tools and with them, side by side.
     */
    public static class Variants extends SimpleComponent {

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                variant (root, "horizontal", TabNavigator.Config.Variant.HORIZONTAL);
                variant (root, "horizontal-underline", TabNavigator.Config.Variant.HORIZONTAL_UNDERLINE);
                variant (root, "horizontal-bar", TabNavigator.Config.Variant.HORIZONTAL_BAR);
                variant (root, "vertical", TabNavigator.Config.Variant.VERTICAL);
                variant (root, "vertical-icon", TabNavigator.Config.Variant.VERTICAL_ICON);
                variant (root, "vertical-alt", TabNavigator.Config.Variant.VERTICAL_ALT);
                variant (root, "vertical-compact", TabNavigator.Config.Variant.VERTICAL_COMPACT);
            }).build ();
        }

        protected void variant(IDomInsertableContainer<?> parent, String name, TabNavigator.Config.Variant variant) {
            H4.$ (parent).text (name);
            Div.$ (parent).css ("display: flex; gap: 1em; margin-bottom: 1.5em;").$ (row -> {
                Div.$ (row).css ("flex: 1; height: 180px; border: 1px dashed #ccc;").$ (box -> {
                    Cpt.$ (box, navigator (name + "-plain", variant, false));
                });
                Div.$ (row).css ("flex: 1; height: 180px; border: 1px dashed #ccc;").$ (box -> {
                    Cpt.$ (box, navigator (name + "-tools", variant, true));
                });
            });
        }

        protected TabNavigator navigator(String testId, TabNavigator.Config.Variant variant, boolean tools) {
            return TabNavigatorCreator.create (cfg -> {
                cfg.variant (variant).testId ("variant-" + testId);
                cfg.tab ("one", "One", new TextComponent ("One")).icon (FontAwesome.house ());
                cfg.tab ("two", "Two", new TextComponent ("Two")).icon (FontAwesome.users ());
                if (tools)
                    cfg.tools (ButtonCreator.build (b -> b.label ("Tool")));
            });
        }
    }
}
