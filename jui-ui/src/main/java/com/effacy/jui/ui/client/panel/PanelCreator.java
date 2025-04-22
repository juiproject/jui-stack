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
package com.effacy.jui.ui.client.panel;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.Config.Zone;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.platform.util.client.With;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;

/**
 * Standard creator helper for {@link Panel}.
 */
public class PanelCreator {

    /**
     * Convenience to build a panel.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *             to configure the panel.
     * @return the panel instance.
     */
    public static Panel $(ContainerBuilder<?> el, Consumer<Panel.Config> cfg) {
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
    public static Panel $(ContainerBuilder<?> el, Consumer<Panel.Config> cfg, Consumer<Panel> builder) {
        return With.$ (build (cfg, builder), cpt -> el.render (cpt));
    }

    /**
     * Creates a configuration instance.
     * 
     * @return the configuration instance.
     */
    public static Panel.Config config() {
        return new Panel.Config ();
    }

    /**
     * Convenience to construct component configuration.
     * 
     * @return the configuration.
     */
    public static Panel build(LayoutData...data) {
        return build (null, null, data);
    }

    /**
     * Convenience to construct component configuration.
     * 
     * @param configurer
     *                   to configure the panel.
     * @param data
     *                   (optional) layout data to apply.
     * 
     * @return the configuration.
     */
    public static Panel build(Consumer<Panel.Config> configurer, LayoutData...data) {
        return build (configurer, null, data);
    }

    /**
     * Convenience to construct component configuration.
     * 
     * @param configurer
     *                   to configure the panel.
     * @param builder
     *                   to build the contents.
     * @param data
     *                   (optional) layout data to apply.
     * 
     * @return the configuration.
     */
    public static Panel build(Consumer<Panel.Config> configurer, Consumer<Panel> builder, LayoutData...data) {
        Panel.Config config = new Panel.Config ();
        if (configurer != null)
            configurer.accept (config);
        Panel panel = config.build (data);
        if (builder != null) 
            builder.accept (panel);
        return panel;
    }

    /**
     * See {@link #buttonBar(Consumer, Consumer)} but with no configurer or builder.
     */
    public static Panel buttonBar() {
        return buttonBar (null);
    }

    /**
     * See {@link #buttonBar(Consumer, Consumer)} but with no builder.
     * 
     * @param configurer
     *                (optional) to build out the configuration.
     * @return the panel suitably configured.
     */
    public static Panel buttonBar(Consumer<Panel.Config> configurer) {
        return buttonBar (configurer, null);
    }

    /**
     * Configured panel for holding buttons. Makes use of an {@link ActionBarLayout}
     * with two zones (first one left and second one right). Insets of 2px are
     * applied to each zone to provide room for buttons to display their focus
     * outline.
     * 
     * @param configurer
     *                   (optional) to build out the configuration.
     * @param builder
     *                   (optional) to build out the panel.
     * @return the panel suitably configured.
     */
    public static Panel buttonBar(Consumer<Panel.Config> configurer, Consumer<Panel> builder) {
        Panel.Config cfg = new Panel.Config ();
        cfg.layout (new ActionBarLayout.Config ().insets (Insets.px (2)).zone (Zone.$ (HAlignment.LEFT), Zone.$ (HAlignment.RIGHT)).build ());
        if (configurer != null)
            configurer.accept (cfg);
        Panel panel = cfg.build ();
        if (builder != null)
            builder.accept (panel);
        return panel;
    }

    /**
     * Create a button bar that has one zone that is right aligned.
     * 
     * @param configurer
     *                   (optional) to build out the configuration.
     * @param builder
     *                   (optional) to build out the panel.
     * @return the panel suitably configured.
     */
    public static Panel buttonBarRightAligned(Consumer<Panel.Config> configurer, Consumer<Panel> builder) {
        Panel.Config cfg = new Panel.Config ();
        cfg.layout (new ActionBarLayout.Config ().insets (Insets.px (0, 2)).zone (Zone.$ (HAlignment.RIGHT)).build ());
        if (configurer != null)
            configurer.accept (cfg);
        Panel panel = cfg.build ();
        if (builder != null)
            builder.accept (panel);
        return panel;
    }
}
