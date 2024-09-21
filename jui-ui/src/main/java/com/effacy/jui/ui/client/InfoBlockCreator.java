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
package com.effacy.jui.ui.client;

import java.util.function.Consumer;

/**
 * Build tools for {@link InfoBlock}.
 *
 * @author Jeremy Buckley
 */
public class InfoBlockCreator {

    /**
     * Convenience to build an instance.
     * 
     * @param <D>
     *                the data type.
     * @param builder
     *                the builder.
     * @return the instance.
     */
    public static <D> InfoBlock<D> $(Consumer<InfoBlock<D>.InfoBlockCreator> builder) {
        return new InfoBlock.Config<D> (builder).build ();
    }

    /**
     * Convenience to build an instance.
     * 
     * @param <D>
     *                the data type.
     * @param cfg
     *                to configure.
     * @param builder
     *                the builder.
     * @return the instance.
     */
    public static <D> InfoBlock<D> $(Consumer<InfoBlock.Config<D>> cfg, Consumer<InfoBlock<D>.InfoBlockCreator> builder) {
        InfoBlock.Config<D> config = new InfoBlock.Config<D> (builder);
        if (cfg != null)
            cfg.accept (config);
        return config.build ();
    }
}
