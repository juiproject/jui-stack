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
package com.effacy.jui.ui.client.navigation;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.platform.util.client.With;

public class TabNavigatorCreator {
    
    /**
     * Convenience to build a panel.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *             to configure the panel.
     * @return the panel instance.
     */
    public static TabNavigator $(ContainerBuilder<?> el, Consumer<TabNavigator.Config> cfg) {
        return $(el, cfg, null);
    }

    /**
     * Convenience to build a panel.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *                to configure the panel.
     * @param builder to build out the contents (alternative to configuration).
     * @return the panel instance.
     */
    public static TabNavigator $(ContainerBuilder<?> el, Consumer<TabNavigator.Config> cfg, Consumer<TabNavigator> builder) {
        return With.$ (create (cfg, builder), cpt -> el.render (cpt));
    }

    /**
     * Convenience to construct component configuration.
     * 
     * @return the configuration.
     */
    public static TabNavigator.Config config() {
        return new TabNavigator.Config ();
    }

    /**
     * Convenience to construct component configuration.
     * 
     * @param builder
     *                to build out the configuration.
     * @return the configuration.
     */
    public static TabNavigator.Config config(Consumer<TabNavigator.Config> builder) {
        TabNavigator.Config cfg =  new TabNavigator.Config ();
        if (builder != null)
            builder.accept (cfg);
        return cfg;
    }

    /**
     * Convenience to build a panel.
     * 
     * @param cfg
     *             to configure the panel.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static TabNavigator create(Consumer<TabNavigator.Config> cfg, LayoutData...data) {
        return create(cfg, null, data);
    }

    /**
     * Convenience to build a panel.
     * 
     * @param cfg
     *                to configure the panel.
     * @param builder to build out the contents (alternative to configuration).
     * @param data
     *                (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static TabNavigator create(Consumer<TabNavigator.Config> cfg, Consumer<TabNavigator> builder, LayoutData...data) {
        TabNavigator.Config config = new TabNavigator.Config ();
        if (cfg != null)
            cfg.accept (config);
        TabNavigator panel = config.build (data);
        if (builder != null)
            builder.accept (panel);
        return panel;
    }
}
