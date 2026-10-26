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
package com.effacy.jui.playground.ui.gallery;

import com.effacy.jui.core.client.component.StateComponentCreator;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.state.ValueStateVariable;
import com.effacy.jui.playground.ui.lessons.SampleRecord;
import com.effacy.jui.playground.ui.lessons.SampleRecordStore;
import com.effacy.jui.ui.client.fragments.Btn;
import com.effacy.jui.ui.client.fragments.Menu;
import com.effacy.jui.ui.client.fragments.Menu.MenuFragment;
import com.effacy.jui.ui.client.fragments.MenuActivator;
import com.effacy.jui.ui.client.fragments.MenuItem;
import com.effacy.jui.ui.client.panel.SplitPanel;
import com.effacy.jui.ui.client.table.Table;
import com.effacy.jui.ui.client.table.TableCreator;
import com.effacy.jui.ui.client.table.renderer.BuilderTableCellRenderer;

/**
 * A selectable table with a row click handler, and a button and a
 * (hover-activated) row menu in each row, which reports the last click (and the selection) so the order of cell and row
 * clicks can be observed (see the playground IT {@code TableClicksITTest}).
 */
public class GalleryExamples_TableClicks extends SplitPanel {

    public GalleryExamples_TableClicks() {
        super (new SplitPanel.Config ()
            .vertical ()
            .otherLayout (new ActionBarLayout.Config ().zone (HAlignment.LEFT).insets (Insets.em (1)).build ())
            .separator ()
        );

        SampleRecordStore store = new SampleRecordStore ();

        // Reports the last click and the size of the selection.
        ValueStateVariable<String> lastClick = new ValueStateVariable<> ("none");
        addOther (StateComponentCreator.build (lastClick, (s, el) -> {
            Div.$ (el).testId ("tableclicks.lastclick").text (s.value ());
        }), new ActionBarLayout.Data (0));
        ValueStateVariable<Integer> selected = new ValueStateVariable<> (0);
        store.handleOnSelectionChanged (s -> selected.assign (s.selection ().size ()));
        addOther (StateComponentCreator.build (selected, (s, el) -> {
            Div.$ (el).testId ("tableclicks.selected").text (s.value () + " selected");
        }), new ActionBarLayout.Data (0));

        Table<SampleRecord> table = TableCreator.build (cfg -> {
            cfg.selectable ();
            cfg.onclick (r -> lastClick.assign ("row:" + r.getName ()));
            cfg.header ("Name", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Div.$ (cell).testId ("tableclicks.name").text (r.getName ());
                }));
                header.width (Length.em (16));
            });
            cfg.header ("Actions", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Btn.$ (cell, "Open").testId ("tableclicks.open").onclick (() -> lastClick.assign ("button:" + r.getName ()));
                }));
            });
            cfg.header ("Menu", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    MenuFragment menu = Menu.$ (MenuActivator.$ (cell));
                    MenuItem.$ (menu).testId ("tableclicks.edit").label ("Edit").onclick (() -> lastClick.assign ("menu:" + r.getName ()));
                    MenuItem.$ (menu).testId ("tableclicks.archive").label ("Archive").disabled (true).onclick (() -> lastClick.assign ("archive:" + r.getName ()));
                }));
                header.width (Length.em (5));
            });
        }, store);
        add (table);

        store.load (10);
    }
}
