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
package com.effacy.jui.ui.client.button;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.ComponentCreatorSupport;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;


public class ButtonCreator {

    /**
     * Convenience to build a button.
     * 
     * @param el
     *            the element to build into.
     * @param cfg
     *            to configure the button.
     * @return the button instance.
     */
    public static Button $(IDomInsertableContainer<?> el, Consumer<Button.Config> cfg) {
        return $ (el, cfg, null);
    }

    /**
     * Convenience to build a button.
     * 
     * @param el
     *                the element to build into.
     * @param cfg
     *                to configure the button.
     * @param builder
     *                to act on the constructed button.
     * @return the button instance.
     */
    public static Button $(IDomInsertableContainer<?> el, Consumer<Button.Config> cfg, Consumer<Button> builder) {
        return ComponentCreatorSupport.$ (el, new Button.Config (), cfg, builder);
    }

    /**
     * Convenience to obtain a button configuration.
     * 
     * @return the button confguration instance.
     */
    public static Button.Config config() {
        return new Button.Config ();
    }
    
    /**
     * Convenience to build a button.
     * 
     * @param cfg
     *             to configure the button.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the button instance.
     */
    public static Button build(Consumer<Button.Config> cfg, LayoutData...data) {
        return build (cfg, null, data);
    }

    /**
     * Convenience to build a button.
     * 
     * @param cfg
     *             to configure the button.
     * @param builder
     *                to act on the constructed button.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the button instance.
     */
    public static Button build(Consumer<Button.Config> cfg, Consumer<Button> builder, LayoutData...data) {
        return ComponentCreatorSupport.build (new Button.Config (), cfg, builder, data);
    }

    /************************************************************************
     * Historical
     ************************************************************************/
    
    /**
     * Convenience to build a button.
     * 
     * @param cfg
     *             to configure the button.
     * @param data
     *             (optional) layout data to associate with the instance.
     * @return the button instance.
     * @deprecated use {@link #build(Consumer, LayoutData...)} instead.
     */
    public static Button create(Consumer<Button.Config> cfg, LayoutData...data) {
        return build (cfg, data);
    }
}
