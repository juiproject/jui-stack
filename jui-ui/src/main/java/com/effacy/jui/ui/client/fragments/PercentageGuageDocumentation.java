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
