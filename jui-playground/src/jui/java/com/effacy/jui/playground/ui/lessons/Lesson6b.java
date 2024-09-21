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

import com.effacy.jui.core.client.component.StateComponentCreator;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;
import com.effacy.jui.core.client.control.DelayedModifiedHandler;
import com.effacy.jui.core.client.dom.builder.A;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.builder.H3;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.state.ValueStateVariable;
import com.effacy.jui.platform.util.client.DateSupport;
import com.effacy.jui.ui.client.NotificationDialog;
import com.effacy.jui.ui.client.Theme;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.fragments.Btn;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.SplitPanel;
import com.effacy.jui.ui.client.table.Table;
import com.effacy.jui.ui.client.table.Table.Config.SortDirection;
import com.effacy.jui.ui.client.table.TableCreator;
import com.effacy.jui.ui.client.table.renderer.BuilderTableCellRenderer;

public class Lesson6b extends SplitPanel {

    protected Lesson6b() {
        super (new SplitPanel.Config ()
            .vertical ()
            .otherLayout (
                new ActionBarLayout.Config ()
                    .zone (HAlignment.LEFT, Length.em (2))
                    .zone (HAlignment.RIGHT).insets (Insets.em (1)).build ()
            )
            .separator ()
        );

        // The underlying store.
        SampleRecordStore store = new SampleRecordStore();

        // A search action.
        TextControl searchCtl = addOther (Controls.text (cfg -> cfg
            .iconLeft (FontAwesome.search ())
            .width (Length.em (15))
            .placeholder ("Search users")
            .clearAction ()
            .modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                store.filter(record -> {
                    return record.getName().toLowerCase().contains(val.toLowerCase());
                });
            }))), new ActionBarLayout.Data (0));

        // Results indicator.
        ValueStateVariable<Integer> storeCounter = new ValueStateVariable<> (0);
        store.handleOnChange (s -> storeCounter.assign (s.getTotalAvailable ()));
        addOther(StateComponentCreator.build (storeCounter, (s,el) -> {
            int value = s.value();
            if (value == 0)
                Div.$ (el).text ("No results");
            else if (value == 1)
                Div.$ (el).text ("One result");
            else
                Div.$ (el).text (value + " results");
        }), new ActionBarLayout.Data (1));

        // Selection action.
        Button selectionBtn = addOther (ButtonCreator.build (cfg -> {
            cfg.label ("Selection action");
            cfg.handler (() -> {
                NotificationDialog.alert (
                    "Bulk action",
                    "You have selected " + store.selection().size() + " result(s)",
                    outcome -> {
                        store.clearSelection ();
                    }
                );
            });
        }), new ActionBarLayout.Data (1));
        selectionBtn.disable();
        store.handleOnSelectionChanged (s -> {
            if (s.selection().size() > 0)
                selectionBtn.enable();
            else
                selectionBtn.disable();
        });

        // The table.
        Table<SampleRecord> table = TableCreator.build (cfg -> {
            cfg.selectable ();
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
            cfg.emptyFiltered (el -> {
                Div.$ (el).css ("width: 60%; margin: auto; margin-top: 10%;").$ (
                    H3.$().text ("No matching results!"),
                    P.$ ().text ("There are no results that match the search criteria you have provided."),
                    Btn.$ ("Clear filters").onclick (()-> {
                        searchCtl.setValue("");
                    })
                );
            });
        }, store);
        add (table);

        // Load up the store.
        store.load(10);
    }
}

