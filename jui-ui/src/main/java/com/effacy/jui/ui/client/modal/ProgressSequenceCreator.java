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
package com.effacy.jui.ui.client.modal;

import java.util.function.Consumer;

import com.effacy.jui.core.client.component.ComponentCreatorSupport;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;

public class ProgressSequenceCreator {
    
    public static ProgressSequence $(ContainerBuilder<?> el, Consumer<ProgressSequence.Config> cfg) {
        return $ (el, cfg, null);
    }

    public static ProgressSequence $(ContainerBuilder<?> el, Consumer<ProgressSequence.Config> cfg, Consumer<ProgressSequence> builder) {
        return ComponentCreatorSupport.$ (el, new ProgressSequence.Config (), cfg, builder);
    }

    public static ProgressSequence.Config config() {
        return new ProgressSequence.Config ();
    }
    
    public static ProgressSequence build(Consumer<ProgressSequence.Config> cfg, LayoutData...data) {
        return build (cfg, null, data);
    }

    public static ProgressSequence build(Consumer<ProgressSequence.Config> cfg, Consumer<ProgressSequence> builder, LayoutData...data) {
        return ComponentCreatorSupport.build (new ProgressSequence.Config (), cfg, builder, data);
    }
}
