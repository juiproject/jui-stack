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
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.explorer.ComponentDocumentation.Config.Type;
import com.effacy.jui.ui.client.fragments.Btn.BtnFragment;
import com.effacy.jui.ui.client.icon.FontAwesome;

public class BtnDocumentation {

    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Btn")
            .className (Btn.class.getCanonicalName ())
            .type (Type.FRAGMENT)
            .description ("Fragment for displaying an interactive button.")
            .example (ComponentCreator.build (cpt -> {
                example01(cpt);
            }))
        .build ();
    }

    public static BtnFragment example01(IDomInsertableContainer<?> root) {
        return Btn.$(root, "Filter")
            .icon(FontAwesome.sliders())
            .variant(Btn.Variant.OUTLINED)
            .nature(Btn.Nature.GREY)
            .onclick(() -> Logger.info("Btn.example01.Click"));
    }

    public static BtnFragment example02(IDomInsertableContainer<?> root) {
        return Btn.$(root, "Filter")
            .icon(FontAwesome.sliders())
            .variant(Btn.Variant.STANDARD)
            .nature(Btn.Nature.GREY)
            .onclick(() -> Logger.info("Btn.example02.Click"));
    }

    public static BtnFragment example03(IDomInsertableContainer<?> root) {
        return Btn.$(root, "Filter")
            .icon(FontAwesome.sliders())
            .variant(Btn.Variant.TEXT)
            .nature(Btn.Nature.GREY)
            .onclick(() -> Logger.info("Btn.example03.Click"));
    }
}
