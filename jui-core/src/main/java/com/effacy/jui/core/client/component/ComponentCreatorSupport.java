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
package com.effacy.jui.core.client.component;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;

/**
 * Tools to support the creation of creators (helpers).
 */
public class ComponentCreatorSupport {

    /**
     * Used to implement the creator build method family.
     */
    public static <C extends Component.Config, CPT extends Component<C>> CPT build(C config, Consumer<C> configurer, Consumer<CPT> builder, LayoutData... data) {
        if (configurer != null)
            configurer.accept (config);
        CPT cpt = config.build (data);
        if (builder != null)
            builder.accept (cpt);
        return cpt;
    }

    /**
     * Used to implement the creator $ method family.
     */
    public static <C extends Component.Config, CPT extends Component<C>> CPT $(IDomInsertableContainer<?> el, C config, Consumer<C> configurer, Consumer<CPT> builder, LayoutData... data) {
        CPT cpt = build (config, configurer, builder, data);
        el.insert (cpt);
        return cpt;
    }

    /**
     * Used to implement the creator $ method family (where a component instance is
     * given).
     */
    public static <CPT extends Component<?>> CPT $(IDomInsertableContainer<?> el, CPT cpt) {
        el.insert (cpt);
        return cpt;
    }
    
}
