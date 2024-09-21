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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation.Config.Type;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.DomGlobal;

public class IconDocumentation {
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Icon")
            .className (Icon.class.getCanonicalName ())
            .type (Type.FRAGMENT)
            .description ("Fragment for displaying an icon that is optionally clickable.")
            .example (ComponentCreator.build (cpt -> {
                Icon.$ (cpt, FontAwesome.airFreshener ())
                    .onclick (() -> DomGlobal.alert ("Clicked!"));
            }))
            .option("icon", opt -> {
                opt.constructor ();
                opt.required ();
                opt.description ("The icon that should be displayed (CSS).");
                opt.valueDescription("A valid CSS class (e.g. a FontAwesome style).");
            })
            .option("style", opt -> {
                opt.description ("Any additional CSS style to apply.");
                opt.valueDescription("A valid CSS class.");
            })
            .option("size", opt -> {
                opt.description ("Applies a font size to the icon.");
                opt.valueDescription("A valid length.");
            })
            .option("color", opt -> {
                opt.description ("Applies a color to the icon.");
                opt.valueDescription("A valid color specifier.");
            })
            .option("onclick", opt -> {
                opt.description ("An action handler to be called when the icon is clicked on.");
                opt.valueDescription("A lambda expression (of type Invoker).");
            })
        .build ();
    }
}
