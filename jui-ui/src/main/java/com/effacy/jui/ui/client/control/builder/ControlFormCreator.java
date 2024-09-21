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
package com.effacy.jui.ui.client.control.builder;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.css.Insets;

/**
 * Creator methods for {@link ControlForm}.
 */
public final class ControlFormCreator {

    /**
     * Customisable supplier for generating standard dialog configurations.
     * <p>
     * See {@link #createForDialog()}.
     */
    public static final Consumer<ControlForm.Config> DIALOG_CONFIG = (cfg) -> {
        cfg.style (ControlForm.Config.Style.COMPACT).focusOnReset ().startingDepth (1).padding (Insets.em (2.5, 2));
    };
    

    /**
     * Convenience to build a control form and inject it into an element.
     * 
     * @param el
     *                the element to build into.
     * @param cfg
     *                to configure the control.
     * @param builder
     *                to build the form.
     * @return the button instance.
     */
    public static <SRC,DST> ControlForm<SRC,DST> $(ContainerBuilder<?> el, Consumer<ControlForm.Config> cfg, Consumer<ControlForm<SRC,DST>> builder) {
        ControlForm<SRC,DST> form = build (cfg, builder);
        if (el != null)
            el.insert (form);
        return form;
    }

    /**
     * Convenience to obtain a configuration.
     * 
     * @return the button configuration instance.
     */
    public static ControlForm.Config create() {
        return new ControlForm.Config ();
    }

    /**
     * Creates a standard configuration for use in dialogs.
     * 
     * @return the configuration.
     */
    public static ControlForm.Config createForDialog() {
        return configureForDialog (null);
    }

    /**
     * Configures the passed configuration for use in a dialog.
     * 
     * @param config
     *               the configuration (if {@code null} then one will be created.
     * @return the passed configuration now configured.
     */
    public static ControlForm.Config configureForDialog(ControlForm.Config config) {
        if (config == null)
            config = create ();
        DIALOG_CONFIG.accept (config);
        return config;
    }

    /**
     * Convenience to build a control.
     * 
     * @param cfg
     *             to configure the control.
     * @param builder
     *                to build the form.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <SRC,DST> ControlForm<SRC,DST> build(Consumer<ControlForm.Config> cfg, Consumer<ControlForm<SRC,DST>> builder, LayoutData...data) {
        ControlForm.Config config = new ControlForm.Config ();
        if (cfg != null)
            cfg.accept (config);
        ControlForm<SRC,DST> form = new ControlForm<SRC,DST> (config);
        if (builder != null)
            builder.accept (form);
        if ((data != null) && (data.length > 0))
            form.setLayoutData(data [0]);
        return form;
    }

    /**
     * Convenience to build a control.
     * 
     * @param builder
     *                to build the form.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <SRC,DST> ControlForm<SRC,DST> build(Consumer<ControlForm<SRC,DST>> builder, LayoutData...data) {
        return build (null, builder, data);
    }
}
