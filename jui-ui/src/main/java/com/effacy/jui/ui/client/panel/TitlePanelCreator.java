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

public class TitlePanelCreator {

    /**
     * Convenience to build a panel.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *             to configure the panel.
     * @return the panel instance.
     */
    public static TitlePanel $(ContainerBuilder<?> el, Consumer<TitlePanel.Config> cfg) {
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
    public static TitlePanel $(ContainerBuilder<?> el, Consumer<TitlePanel.Config> cfg, Consumer<TitlePanel> builder) {
        return With.$ (create (cfg, builder), cpt -> el.render (cpt));
    }
    
    /**
     * Convenience to construct component configuration.
     * 
     * @return the configuration.
     */
    public static TitlePanel.Config create() {
        return new TitlePanel.Config ();
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
    public static TitlePanel create(Consumer<TitlePanel.Config> configurer, Consumer<TitlePanel> builder, LayoutData...data) {
        TitlePanel.Config config = new TitlePanel.Config ();
        if (configurer != null)
            configurer.accept (config);
        TitlePanel panel = config.build (data);
        if (builder != null) 
            builder.accept (panel);
        return panel;
    }

}
