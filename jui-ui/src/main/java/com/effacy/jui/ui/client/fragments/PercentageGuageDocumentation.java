/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation.Config.Type;

public class PercentageGuageDocumentation {
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("PercentageGuage")
            .className (PercentageGuage.class.getCanonicalName ())
            .type (Type.FRAGMENT)
            .description ("Fragment for displaying percentage progress guage.")
            .example (ComponentCreator.build (cpt -> {
                PercentageGuage.$ (cpt, 45)
                    .css("width: 4em;");
            }))
            .option("percentage", opt -> {
                opt.constructor ();
                opt.required ();
                opt.description ("The percentage progress that should be displayed.");
                opt.valueDescription("An integer between 0 and 100 (inclusive).");
            })
        .build ();
    }
}
