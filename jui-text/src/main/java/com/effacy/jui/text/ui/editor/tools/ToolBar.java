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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.dom.DomSupport;
import com.effacy.jui.core.client.dom.UIEvent;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Fragment;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.text.ui.editor.tools.ITool.IToolContext;

import elemental2.dom.Element;

/**
 * Represents a single toolbar. It is modeled as a fragment but does receive UI
 * events for tool activation.
 */
public class ToolBar extends Fragment<ToolBar> implements IToolBar {

    public static ToolBar $(IDomInsertableContainer<?> parent) {
        return $(parent, null);
    }

    public static ToolBar $(IDomInsertableContainer<?> parent, Consumer<ToolBar> builder) {
        ToolBar frg = new ToolBar ();
        if (parent != null)
            parent.insert (frg);
        if (builder != null)
            builder.accept (frg);
        return frg;
    }

    private IToolBarCSS styles;

    private List<ITool> tools = new ArrayList<>();

    private Element rootEl;

    private IToolContext context;

    public ToolBar() {
        this (IToolBarCSS.StandardToolBarCSS.instance ());
    }

    public ToolBar(IToolBarCSS styles) {
        this.styles= styles;
    }



    public ToolBar add(ITool tool) {
        this.tools.add (tool);
        return this;
    }

    @Override
    public void build(ContainerBuilder<?> parent) {
        Div.$ (parent).style (styles.toolbar ()).$ (bar -> {
            bar.apply (n -> rootEl = (Element) n);
            tools.forEach (tool -> {
                tool.render (bar, styles);
            });
        });
    }

    @Override
    public void activate(IToolContext context) {
        if (context == null) {
            deactivate();
            return;
        }
        this.context = context;
        try {
            tools.forEach (tool -> tool.activate (context));
        } catch (Throwable e) {
            Logger.reportUncaughtException (e, this);
            Logger.error("Failed to activate tools", e);
        }
    }

    @Override
    public void deactivate() {
        this.context = null;
        tools.forEach (tool -> tool.deactivate ());
    }

    @Override
    public boolean handleEvent(UIEvent event) {
        if (context == null)
            return false;
        if (!DomSupport.isChildOf (event.getTarget (), rootEl))
            return false;

        if (!event.isEvent (UIEventType.ONCLICK))
            return false;

        // Make sure we are in the toolbar.
        Element el = event.getTarget ("." + styles.tool (), 10);
        if ((el == null) || !DomSupport.isChildOf (el, rootEl))
            return false;
        
        String uid = el.getAttribute ("tool");
        if (uid == null)
            return false; 
        for (ITool tool : tools) {
            if (uid.equals (tool.uuid ())) {
                for (ITool other : tools) {
                    if (other != tool)
                        other.close ();
                }
                // We have found a tool so apply. The re-set the tool states.
                tool.apply (context, event);
                activate (context);
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        this.rootEl = null;
        this.context = null;
    }

}
