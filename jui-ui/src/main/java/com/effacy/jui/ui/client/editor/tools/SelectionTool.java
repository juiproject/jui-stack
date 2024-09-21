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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;

public class SelectionTool extends Tool {

    public class Option {
        private String icon;
        private String label;
        private Consumer<IToolContext> action;
        private String uid = UID.createUID ();
        public Option(String icon, String label, Consumer<IToolContext> action) {
            this.icon = icon;
            this.label = label;
            this.action = action;
        }
    }

    private String inactiveLabel;

    private Element labelEl;

    private Element popupEl;

    private boolean open;

    private List<Option> options = new ArrayList<>();

    protected SelectionTool(String inactiveLabel) {
        this.inactiveLabel = inactiveLabel;
    }

    protected void option(String icon, String label, Consumer<IToolContext> action) {
        options.add (new Option (icon, label, action));
    }

    protected void updateLabel(String label) {
        if (StringSupport.empty (label))
            label = inactiveLabel;
        DomSupport.innerText (labelEl, label);
    }

    protected Option lookup(String uid) {
        for (Option option : options) {
            if(option.uid.equals(uid))
                return option;
        }
        return null;
    }

    @Override
    public boolean apply(IToolContext ctx, UIEvent event) {
        if (DomSupport.isChildOf(event.getTarget(), popupEl)) {
            Element el = event.getTarget("div", 3);
            if (el == null)
                return false;
            String uid = el.getAttribute("uid");
            if (uid == null)
                return false;
            Option option = lookup (uid);
            if (option == null)
                return false;
            if (option.action != null)
                option.action.accept (ctx);
        }
        if (!open) {
            rootEl.classList.add (styles.open ());
            open = true;
        } else {
            rootEl.classList.remove (styles.open ());
            open = false;
        }
        return false;
    }

    @Override
    public void close() {
        if (open) {
            rootEl.classList.remove (styles.open ());
            open = false;
        }
    }

    @Override
    public void deactivate() {
        super.deactivate ();
        rootEl.classList.remove (styles.open ());
        open = false;
    }

    @Override
    public void activate(IToolContext ctx) {
        super.activate (ctx);
    }

    @Override
    protected void render(ElementBuilder tool) {
        tool.style (styles.selection ());
        Div.$ (tool).style (styles.selection_selector ()).$ (selector -> {
            Span.$ (selector).$ (label -> {
                label.apply (n -> labelEl = (Element) n);
                label.text (inactiveLabel);
            });
            Em.$ (selector).style (FontAwesome.angleDown ());
        });
        Div.$ (tool).style (styles.selection_popup ()).$ (popup -> {
            popup.apply (n -> popupEl = (Element) n);
            options.forEach (option -> {
                Div.$ (popup).$ (item -> {
                    item.attr ("uid", option.uid);
                    Em.$ (item).style (option.icon);
                    Span.$ (item).text (option.label);
                });
            });
        });
    }

}
