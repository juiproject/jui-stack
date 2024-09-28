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

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H1;
import com.effacy.jui.core.client.dom.builder.H2;
import com.effacy.jui.core.client.dom.builder.Header;
import com.effacy.jui.core.client.dom.builder.Li;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Ul;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.text.ui.editor.EditorComponent;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;

import elemental2.dom.Element;

public class PolicyEditor  extends SimpleComponent {

    static {
        CSSInjector.injectFromModuleBase ("page-editor.css");
    }

    private Button edit;

    private Button cancel;

    private Button save;

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).style ("page_editor").$ (root -> {
            Div.$ (root).style ("page").$ (page -> {
                Header.$ (page).$ (header -> {
                    Ul.$ (header).$ (bc -> {
                        Li.$ (bc).text ("Policies");
                        Li.$ (bc).text ("02 Human Resources");
                    });
                    Span.$ (header);
                    edit = ButtonCreator.$ (header, cfg -> {
                        cfg.label("Edit").icon(FontAwesome.edit()).style(Button.Config.Style.OUTLINED).handler(() -> {
                            edit.hide();
                            cancel.show();
                            save.show();
                        });
                    });
                    cancel = ButtonCreator.$ (header, cfg -> {
                        cfg.label("cancel").style(Button.Config.Style.LINK).handler(() -> {
                            edit.show();
                            cancel.hide();
                            save.hide();
                        });
                    });
                    cancel.hide();
                    save = ButtonCreator.$ (header, cfg -> {
                        cfg.label("Save").icon(FontAwesome.check()).handler(() -> {
                            edit.show ();
                            cancel.hide();
                            save.hide ();
                        });
                    });
                    save.hide ();
                });
                Div.$ (page).style("inner").$ (inner -> {
                    H1.$ (inner).text ("P02.01 Employee Attendance");
                    P.$ (inner).text ("The policy must ensure regular attendance and punctuality of employees to prevent any deviation that may result in decreased productivity.");
                    P.$ (inner).style("info", "gap1").$ (item -> {
                        Em.$ (item).style (FontAwesome.users ());
                        item.text ("This attendance policy applies to all employees regardless of their position or type of employment.");
                    });
                    P.$ (inner).style("info").$ (item -> {
                        Em.$ (item).style (FontAwesome.userSecret ());
                        item.text ("Human resources are to check attendance policy compliance every day and report violations to management.");
                    });
                    H2.$ (inner).style ("gap1").text ("Policy statement");
                    inner.insert(new EditorComponent (true));
                    Div.$ (inner).style("meta").$ (item -> {
                        Span.$ (item).text ("Applies since 23 Jan 2020");
                        Span.$ (item).text ("Last reviewed 20 Jun 2022");
                        Span.$ (item).text ("Next review due 20 Jun 2024");
                    });
                });
            });
        }).build ();
    }

    

}
