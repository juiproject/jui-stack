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

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.platform.util.client.With;

/**
 * Standard creator helper for {@link SplitPanel}.
 */
public class SplitPanelCreator {

    /**
     * Convenience to build a panel.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *             to configure the panel.
     * @return the panel instance.
     */
    public static SplitPanel $(ContainerBuilder<?> el, Consumer<SplitPanel.Config> cfg) {
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
    public static SplitPanel $(ContainerBuilder<?> el, Consumer<SplitPanel.Config> cfg, Consumer<SplitPanel> builder) {
        return With.$ (create (cfg, builder), cpt -> el.render (cpt));
    }
    
    /**
     * Convenience to construct component configuration.
     * 
     * @return the configuration.
     */
    public static SplitPanel.Config create() {
        return new SplitPanel.Config ();
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
    public static SplitPanel create(Consumer<SplitPanel.Config> configurer, Consumer<SplitPanel> builder, LayoutData...data) {
        SplitPanel.Config config = new SplitPanel.Config ();
        if (configurer != null)
            configurer.accept (config);
        SplitPanel panel = config.build (data);
        if (builder != null) 
            builder.accept (panel);
        return panel;
    }

}
