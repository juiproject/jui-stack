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
package com.effacy.jui.ui.client.control;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.platform.util.client.With;

public class TextSearchControlCreator {

    /**
     * Convenience to build a control.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *            to configure the control.
     * @return the button instance.
     */
    public static <S> TextSearchControl<S> $(ContainerBuilder<?> el, Consumer<TextSearchControl.Config<S>> cfg) {
        return With.$ (build (cfg), cpt -> el.render (cpt));
    }

    /**
     * Convenience to obtain a configuration.
     * 
     * @return the button configuration instance.
     */
    public static <S> TextSearchControl.Config<S> create() {
        return new TextSearchControl.Config<S> ();
    }

    /**
     * Convenience to build a text search control.
     * 
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <S> TextSearchControl<S> build(LayoutData...data) {
        return build (null, data);
    }

    /**
     * Convenience to build a text search control.
     * 
     * @param cfg
     *             to configure the control.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <S> TextSearchControl<S> build(Consumer<TextSearchControl.Config<S>> cfg, LayoutData...data) {
        return build (cfg, null, data);
    }

    /**
     * Convenience to build a text search control.
     * 
     * @param cfg
     *                to configure the control.
     * @param applier
     *                to apply changes to the created control.
     * @param data
     *                (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <S> TextSearchControl<S> build(Consumer<TextSearchControl.Config<S>> cfg, Consumer<TextSearchControl<S>> applier, LayoutData...data) {
        TextSearchControl.Config<S> config = new TextSearchControl.Config<S> ();
        if (cfg != null)
            cfg.accept (config);
            TextSearchControl<S> ctl = config.build (data);
        if (applier != null)
            applier.accept (ctl);
        return ctl;
    }
}
