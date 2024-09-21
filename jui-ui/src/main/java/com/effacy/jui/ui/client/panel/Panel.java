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
package com.effacy.jui.ui.client.panel;

import com.effacy.jui.core.client.component.IComponentCSS;
import com.effacy.jui.core.client.component.layout.LayoutData;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

public class Panel extends PanelBase<Panel.Config> {

    /**
     * Configuration for the panel.
     */
    public static class Config extends PanelBase.Config<Config> {

        @Override
        @SuppressWarnings("unchecked")
        public Panel build(LayoutData... data) {
            return (Panel) build (new Panel (this), data);
        }

    }

    /**
     * Construct from config.
     * 
     * @param config
     *               the configuration.
     */
    public Panel(Config config) {
        super (config, LocalCSS.instance());
    }

    /************************************************************************
     * CSS
     ************************************************************************/

    /**
     * Component CSS (standard).
     */
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/effacy/jui/ui/client/panel/Panel.css",
        "com/effacy/jui/ui/client/panel/Panel_Override.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {

        private static LocalCSS STYLES;

        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create (LocalCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
