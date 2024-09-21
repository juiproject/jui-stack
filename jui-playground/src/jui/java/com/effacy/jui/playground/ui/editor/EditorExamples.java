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
package com.effacy.jui.playground.ui.editor;

import com.effacy.jui.platform.css.client.CssDeclaration;
import com.effacy.jui.platform.css.client.CssResource;
import com.effacy.jui.ui.client.editor.EditorComponent;
import com.effacy.jui.ui.client.panel.Panel;
import com.google.gwt.core.client.GWT;

/**
 * RendererExamples
 *
 * @author Jeremy Buckley
 */
public class EditorExamples extends Panel {

    public EditorExamples() {
        super (new Panel.Config ());

        // Editor.debug (Editor.DebugMode.EVENT);

        add (new EditorComponent (false));

        EditorStyles.instance();
    }

    public interface IEditorStyles extends CssDeclaration {
        public String hubba();
        public String bubba();
    }

    @CssResource({"EditorStyles.css"})
    public abstract static class EditorStyles implements IEditorStyles {
        private static EditorStyles STYLES;
        public static EditorStyles instance() { 
            if (STYLES == null) {
                STYLES = (EditorStyles) GWT.create (EditorStyles.class);
                STYLES.ensureInjected ();
            }
            return STYLES;
        }
    }

}
