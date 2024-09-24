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

import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;

import elemental2.dom.Element;

/**
 * Abstract base class for {@link ITool} implementations that are buttons.
 */
public abstract class ButtonTool extends Tool {

    protected String icon;

    protected String label;

    ButtonTool (String icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public void render(ElementBuilder tool) {
        tool.style (styles.button (), styles.inactive ());
        tool.apply (n -> rootEl = (Element) n);
        Em.$ (tool).style (icon);
        tool.on (UIEventType.ONCLICK, UIEventType.ONMOUSEDOWN);
    }
}
