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
package com.effacy.jui.ui.client.editor.tools;

import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.util.UID;

import elemental2.dom.Element;

public abstract class Tool implements ITool {
    
    protected IToolBarCSS styles;

    protected Element rootEl;

    protected String uuid = UID.createUID();

    public String uuid() {
        return uuid;
    }

    public void deactivate() {
        rootEl.classList.add (styles.inactive ());
    }

    @Override
    public boolean apply(IToolContext ctx, UIEvent event) {
        return apply (ctx);
    }

    public boolean apply(IToolContext ctx) {
        return false;
    }

    public void activate(IToolContext ctx) {
        rootEl.classList.remove (styles.inactive());
    }

    public final void render(ContainerBuilder<?> parent, IToolBarCSS styles) {
        this.styles = styles;
        Div.$ (parent).style (styles.tool (), styles.inactive ()).$ (tool -> {
            tool.apply (n -> rootEl = (Element) n);
            tool.attr ("tool", uuid);
            tool.on (UIEventType.ONCLICK, UIEventType.ONMOUSEDOWN);
            render (tool);
        });
    }

    protected abstract void render(ElementBuilder tool);
}
