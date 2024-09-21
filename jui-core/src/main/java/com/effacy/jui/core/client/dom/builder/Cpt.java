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
package com.effacy.jui.core.client.dom.builder;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.Component.Config;
import com.effacy.jui.core.client.component.IComponent;

/**
 * See {@link Insert}.
 * <p>
 * Used to insert components. Its functionally equivalent to {@link Insert} but
 * with a more targeted name for clarity.
 */
public class Cpt {

    /**
     * Inserts the passed children into the parent.
     * 
     * @param parent
     *                 the parent to insert the children into.
     * @param children
     *                 the children to insert.
     */
    public static void $(IDomInsertableContainer<?> parent, IComponent...children) {
        parent.insert (children);
    }

    /**
     * This takes and returns a component. This is really only so that we can
     * maintain consistency with the use of helper classes (when they return a
     * builder).
     * 
     * @param cpt
     *            the component (for insertion).
     * @return the passed component.
     */
    public static IComponent $(IComponent cpt) {
        return cpt;
    }

    /**
     * Convenience to act on a component in situ with a lambda expression.
     * 
     * @param cpt
     *                the component to act on.
     * @param builder
     *                the builder to act on the component.
     * @return the passed component.
     */
    public static <CPT extends IComponent> CPT build(CPT cpt, Consumer<CPT> builder) {
        if (builder != null)
            builder.accept (cpt);
        return cpt;
    }

    /**
     * Convenience to act on a component in situ with a lambde expression while
     * building that component from configuration.
     * 
     * @param cfg
     *                the component configuration.
     * @param builder
     *                the builder to act on the component.
     * @return the passed component.
     */
    public static <CPT extends IComponent> CPT build(Config cfg, Consumer<CPT> builder) {
        CPT cpt = cfg.build();
        if (builder != null)
            builder.accept (cpt);
        return cpt;
    }

    /**
     * Convenience to act on a component in situ with a lambde expression while
     * building that component from configuration.
     * 
     * @param config
     *                the component configuration.
     * @param cfg
     *                to further modify the configuration.
     * @param builder
     *                the builder to act on the component.
     * @return the passed component.
     */
    public static <CPT extends IComponent, CFG extends Config> CPT build(CFG config, Consumer<CFG> cfg, Consumer<CPT> builder) {
        if (cfg != null)
            cfg.accept (config);
        CPT cpt = config.build();
        if (builder != null)
            builder.accept (cpt);
        return cpt;
    }
}
