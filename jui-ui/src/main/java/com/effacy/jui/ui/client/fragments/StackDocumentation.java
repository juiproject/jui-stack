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
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation.Config.Type;

public class StackDocumentation {
    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Stack")
            .className (Stack.class.getCanonicalName ())
            .type (Type.FRAGMENT)
            .description ("Fragment for organising children horizontally or vertically.")
            .example (ComponentCreator.build (cpt -> {
                Stack.$ (cpt).gap (Length.em (0.5)).$ (contents -> {
                    Span.$ (contents).text ("Item 1");
                    Span.$ (contents).text ("Item 2");
                    Span.$ (contents).text ("Item 3");
                });
            }))
        .build ();
    }
}
