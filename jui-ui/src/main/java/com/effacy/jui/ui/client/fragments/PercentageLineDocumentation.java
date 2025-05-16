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

public class PercentageLineDocumentation {
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("PercentageLine")
            .className (PercentageLine.class.getCanonicalName ())
            .type (Type.FRAGMENT)
            .description ("Fragment for displaying a horizontal percentage progress bar.")
            .example (ComponentCreator.build (cpt -> {
                PercentageLine.$ (cpt, 45)
                    .css("width: 10em;");
            }))
            .option("percentage", opt -> {
                opt.constructor ();
                opt.required ();
                opt.description ("The percentage progress that should be displayed.");
                opt.valueDescription("An integer between 0 and 100 (inclusive).");
            })
            .option("label", opt -> {
                opt.description ("An optional display label that appears above the bar on the left (the percentage progress appears on the same line but to the right, if there is no label the percentage appears to the right of the bar).");
                opt.valueDescription("Text content.");
            })
            .option("progress", opt -> {
                opt.description ("If present replaces the percentage progress label.");
                opt.valueDescription("Conditional text content.");
            })
        .build ();
    }
}
