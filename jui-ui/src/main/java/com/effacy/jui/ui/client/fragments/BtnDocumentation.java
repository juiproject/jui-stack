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
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.NotificationDialog;
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
            .css ("Btn.css", "Btn_Override.css")
            .description ("Fragment for displaying a lightweight interactive button.")
            .option ("label", option -> {
                option.constructor ();
                option.required ();
                option.description ("The display label for the button.");
                option.valueType ("string");
            })
            .option ("icon", option -> {
                option.description ("Optional icon to display before the label.");
                option.valueType ("string");
            })
            .option ("variant", option -> {
                option.description ("Applies the button presentation variant.");
                option.valueType ("enum{Btn.Variant}");
            })
            .option ("nature", option -> {
                option.description ("Applies the semantic colour scheme.");
                option.valueType ("enum{Btn.Nature}");
            })
            .option ("onclick", option -> {
                option.description ("Assigns a click handler and drives the running state until the callback completes.");
                option.valueType ("Invoker | Consumer<IButtonActionCallback>");
            })
            .example (ComponentCreator.build (cpt -> {
                example01(cpt);
            }))
            .example (ComponentCreator.build (BtnDocumentation::variants), detail -> {
                detail.description ("Variant combinations including standard, outlined, text, rounded and expanded presentations.");
            })
            .example (ComponentCreator.build (BtnDocumentation::natures), detail -> {
                detail.description ("Semantic natures applied over the base fragment styling.");
            })
            .example (ComponentCreator.build (BtnDocumentation::states), detail -> {
                detail.description ("Interaction states including icon placement, fixed width and the running state.");
            })
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

    private static void variants(IDomInsertableContainer<?> root) {
        Stack.$ (root).vertical().gap (Length.em (0.75)).$ (col -> {
            Stack.$ (col).gap (Length.em (0.5)).wrap().$ (row -> {
                Btn.$ (row, "Standard").icon (FontAwesome.plus ()).onclick (() -> Logger.info ("Btn.variants.standard"));
                Btn.$ (row, "Outlined").icon (FontAwesome.sliders ()).variant (Btn.Variant.OUTLINED).onclick (() -> Logger.info ("Btn.variants.outlined"));
                Btn.$ (row, "Text").variant (Btn.Variant.TEXT).onclick (() -> Logger.info ("Btn.variants.text"));
            });
            Stack.$ (col).gap (Length.em (0.5)).wrap().$ (row -> {
                Btn.$ (row, "Rounded").variant (Btn.Variant.STANDARD_ROUNDED).onclick (() -> Logger.info ("Btn.variants.rounded"));
                Btn.$ (row, "Expanded").variant (Btn.Variant.STANDARD_EXPANDED).icon (FontAwesome.arrowRight ()).onclick (() -> Logger.info ("Btn.variants.expanded"));
                Btn.$ (row, "Text compact").variant (Btn.Variant.TEXT_COMPACT).icon (FontAwesome.xmark ()).onclick (() -> Logger.info ("Btn.variants.textcompact"));
            });
        });
    }

    private static void natures(IDomInsertableContainer<?> root) {
        Stack.$ (root).gap (Length.em (0.5)).wrap().$ (row -> {
            Btn.$ (row, "Normal").icon (FontAwesome.check ()).onclick (() -> Logger.info ("Btn.natures.normal"));
            Btn.$ (row, "Success").icon (FontAwesome.checkDouble ()).nature (Btn.Nature.SUCCESS).onclick (() -> Logger.info ("Btn.natures.success"));
            Btn.$ (row, "Warning").icon (FontAwesome.triangleExclamation ()).nature (Btn.Nature.WARNING).onclick (() -> Logger.info ("Btn.natures.warning"));
            Btn.$ (row, "Danger").icon (FontAwesome.ban ()).nature (Btn.Nature.DANGER).onclick (() -> Logger.info ("Btn.natures.danger"));
            Btn.$ (row, "Grey").icon (FontAwesome.sliders ()).nature (Btn.Nature.GREY).variant (Btn.Variant.OUTLINED).onclick (() -> Logger.info ("Btn.natures.grey"));
        });
    }

    private static void states(IDomInsertableContainer<?> root) {
        Stack.$ (root).vertical().gap (Length.em (0.75)).$ (col -> {
            Stack.$ (col).gap (Length.em (0.5)).wrap().$ (row -> {
                Btn.$ (row, "Icon button").icon (FontAwesome.download ()).onclick (() -> Logger.info ("Btn.states.icon"));
                Btn.$ (row, "Compact action").variant (Btn.Variant.COMPACT).icon (FontAwesome.ellipsis ()).onclick (() -> Logger.info ("Btn.states.compact"));
                Btn.$ (row, "Processing").icon (FontAwesome.rotate ()).onclick (cb -> {
                    NotificationDialog.alert ("Btn", "This example keeps the fragment in its running state until the dialog closes.", t -> cb.complete ());
                });
            });
            Btn.$ (col, "Full width action")
                .width (Length.em (18))
                .icon (FontAwesome.arrowRight ())
                .variant (Btn.Variant.OUTLINED)
                .nature (Btn.Nature.GREY)
                .onclick (() -> Logger.info ("Btn.states.fullwidth"));
        });
    }
}
