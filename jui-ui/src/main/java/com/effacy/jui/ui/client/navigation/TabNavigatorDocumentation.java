package com.effacy.jui.ui.client.navigation;

import com.effacy.jui.ui.client.explorer.ComponentDocumentation;
import com.effacy.jui.ui.client.panel.PanelCreator;

public class TabNavigatorDocumentation {

    public static ComponentDocumentation documentation() {
        return new ComponentDocumentation.Config ()
            .title ("Tab navigator")
            .className (TabNavigator.class.getCanonicalName())
            .description ("A navigator panel whose children linked to a tab set.")
            .example (TabNavigatorCreator.create (cfg -> {
                cfg.style (TabNavigator.Config.Style.HORIZONTAL);
            }, tab-> {
                tab.tab ("tab1", "Tab 1", PanelCreator.build ());
                tab.tab ("tab2", "Tab 2", PanelCreator.build ());
            }))
        .build ();
    }
}
