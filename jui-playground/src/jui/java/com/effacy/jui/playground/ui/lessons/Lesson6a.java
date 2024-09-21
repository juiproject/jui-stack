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
package com.effacy.jui.playground.ui.lessons;

import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.DateSupport;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.Theme;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;
import com.effacy.jui.ui.client.table.Table;
import com.effacy.jui.ui.client.table.Table.Config.SortDirection;
import com.effacy.jui.ui.client.table.TableCreator;
import com.effacy.jui.ui.client.table.renderer.BuilderTableCellRenderer;

public class Lesson6a extends Panel {

    protected Lesson6a() {
        super (PanelCreator.config ().scrollable ());

        SampleRecordStore store = new SampleRecordStore();

        Table<SampleRecord> table = TableCreator.build (cfg -> {
            //cfg.selectable ();
            cfg.cellPadding (Insets.px (10, 5)).color (Theme.colorAuxWhite ());
            cfg.header ("Name", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Div.$(cell).style ("lesson6_name").text (r.getName())
                        .onclick (e -> {
                            NotificationDialog.alert ("Name clicked on", "You click on " + r.getName() + "!", outcome -> {});
                        });
                }));
                header.width (Length.em (12));
                header.sorted (true).sortable (SortDirection.ASC, dir -> {
                    store.sortByName (dir == SortDirection.DESC);
                });
            }); 
            cfg.header ("Email", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    A.$(cell, "mailto:" + r.getEmail()).text (r.getEmail());
                }));
                header.width (Length.em (16));
            });
            cfg.header ("Number of visits", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    if (r.getVisits() == 0)
                        Div.$ (cell).text ("None");
                    else if (r.getVisits() == 1)
                        Div.$ (cell).text ("One vist");
                    else
                        Div.$ (cell).text (r.getVisits() + " visits");
                }));
            });
            cfg.header ("Last accessed", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Div.$ (cell).text (DateSupport.formatDate(r.getLastAccess()));
                }));
            });
            cfg.header ("Rating", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Div.$ (cell).$ (contents -> {
                        for (int i = 1; i < 5; i++) {
                            if (r.getRating() >= i)
                                Em.$ (contents).style (FontAwesome.star()).css ("color: #666;");
                            else
                                Em.$ (contents).style (FontAwesome.star()).css ("color: #eee;");
                        }
                    });
                }));
                header.sorted (true).sortable (SortDirection.ASC, dir -> {
                    store.sortByRating (dir == SortDirection.DESC);
                });
            });
        }, store);
        add (table);
        store.load(10);
    }

}

