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

import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.panel.PanelCreator;

public class CheckControlDocumentation {
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Checkbox")
            .className (CheckControl.class.getCanonicalName())
            .description ("A checkbox control.")
            .example (PanelCreator.build (cfg -> cfg.layout (VertLayout.$ ().spacing (Length.em (0.5)).build ()), builder -> {
                builder.add (CheckControlCreator.create ().label ("Standard check box").build ());
                builder.add (CheckControlCreator.create ().label ("Slider check box").slider ().padding (Insets.px (3)).build ());
            }))
        .build ();
    }
}
