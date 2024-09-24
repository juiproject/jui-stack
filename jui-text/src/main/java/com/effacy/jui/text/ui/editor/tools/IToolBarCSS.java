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
package com.effacy.jui.text.ui.editor.tools;

import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.google.gwt.core.client.GWT;

/**
 * CSS styles for tools.
 */
public interface IToolBarCSS extends CssDeclaration {

    public String toolbar();

    public String tool();

    public String inactive();

    public String applied();

    public String open();

    public String button();

    public String selection();

    public String selection_selector();

    public String selection_popup();

    /**
     * Component CSS (standard pattern).
     */
    @CssResource({
        "com/effacy/jui/text/ui/editor/tools/ToolBar.css",
        "com/effacy/jui/text/ui/editor/tools/ToolBar_Override.css"
    })
    public static abstract class StandardToolBarCSS implements IToolBarCSS {

        private static StandardToolBarCSS STYLES;

        public static IToolBarCSS instance() {
            if (STYLES == null) {
                STYLES = (StandardToolBarCSS) GWT.create (StandardToolBarCSS.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }
}
