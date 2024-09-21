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

public class MultiSelectionControlCreator {

    /**
     * Convenience to build a control.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *            to configure the control.
     * @return the button instance.
     */
    public static <V>  MultiSelectionControl<V> $(ContainerBuilder<?> el, Consumer< MultiSelectionControl.Config<V>> cfg) {
        return With.$ (create (cfg), cpt -> el.render (cpt));
    }
    
    /**
     * Convenience to obtain a configuration.
     * 
     * @return the button configuration instance.
     */
    public static <V>  MultiSelectionControl.Config<V> create() {
        return new  MultiSelectionControl.Config<V> ();
    }

    /**
     * See {@link #build(Consumer, LayoutData...)}.
     * 
     * @deprecated use {@link #build(Consumer, LayoutData...)}.
     */
    public static <V>  MultiSelectionControl<V> create(Consumer< MultiSelectionControl.Config<V>> configurer, LayoutData...data) {
        return build (configurer, data);
    }

    /**
     * Convenience to build a selection control.
     * 
     * @param configurer
     *                   to configure the control.
     * @param data
     *                   (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <V>  MultiSelectionControl<V> build(Consumer< MultiSelectionControl.Config<V>> configurer, LayoutData...data) {
        return build (configurer, null, data);
    }
    
    /**
     * Convenience to build a selection control.
     * 
     * @param configurer
     *                   to configure the control.
     * @param ctl
     *                   to configure the control.
     * @param data
     *                   (optional) layout data to associate with the instance.
     * @return the control instance.
     */
    public static <V>  MultiSelectionControl<V> build(Consumer< MultiSelectionControl.Config<V>> configurer, Consumer< MultiSelectionControl<V>> ctl,  LayoutData...data) {
        MultiSelectionControl.Config<V> config = new  MultiSelectionControl.Config<V> ();
        if (configurer != null)
            configurer.accept (config);
            MultiSelectionControl<V> control = config.build (data);
        if (ctl != null)
            ctl.accept (control);
        return control;
    }
}
