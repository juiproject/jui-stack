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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.effacy.jui.core.client.MockData;
import com.effacy.jui.core.client.component.ComponentCreator;
import com.effacy.jui.core.client.component.IDisposeListener;
import com.effacy.jui.core.client.component.StateComponentCreator;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.state.ValueStateVariable;
import com.effacy.jui.core.client.store.ListPaginatedStore;
import com.effacy.jui.playground.ui.lessons.SampleRecord;
import com.effacy.jui.ui.client.button.Button;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.CheckControlCreator;
import com.effacy.jui.ui.client.control.TextControlCreator;
import com.effacy.jui.ui.client.fragments.Btn;
import com.effacy.jui.ui.client.panel.SplitPanel;
import com.effacy.jui.ui.client.table.Table;
import com.effacy.jui.ui.client.table.TableCreator;
import com.effacy.jui.ui.client.table.renderer.BuilderTableCellRenderer;

/**
 * Components placed in table cells with {@code Cpt.$}: a {@link Button} in a
 * table with a row click handler (which also has a check control, a text
 * control and a display-only component per row) and in one without. It reports the last click and (on demand) the
 * number of buttons in the first table that are still live (created and not
 * yet disposed), and has actions to re-render its rows in place, to reverse
 * their order and to reload it with fewer rows (see the playground IT
 * {@code TableComponentsITTest}).
 */
public class GalleryExamples_TableComponents extends SplitPanel {

    public GalleryExamples_TableComponents() {
        super (new SplitPanel.Config ()
            .vertical ()
            .layout (VertLayout.$ ().spacing (Length.em (1)).build ())
            .scrollable ()
            .otherLayout (new ActionBarLayout.Config ().zone (HAlignment.LEFT).insets (Insets.em (1)).build ())
            .separator ()
        );

        // Fixed sets of records (so the tables do not page in more as they are
        // scrolled to the end).
        List<SampleRecord> records = new ArrayList<> ();
        for (int i = 0; i < 10; i++)
            records.add (new SampleRecord (MockData.NAMES[i], MockData.EMAILS[i], 10, new Date (), 1));
        RecordStore store = new RecordStore (records);
        ListPaginatedStore<SampleRecord> plainStore = new ListPaginatedStore<SampleRecord> () {

            @Override
            protected void populate(List<SampleRecord> list) {
                list.addAll (records.subList (0, 3));
            }

        };

        // Reports the last click and the number of live cell components.
        ValueStateVariable<String> lastClick = new ValueStateVariable<> ("none");
        addOther (StateComponentCreator.build (lastClick, (s, el) -> {
            Div.$ (el).testId ("tablecpts.lastclick").text (s.value ());
        }), new ActionBarLayout.Data (0));
        // The count is only shown on demand (Count), as showing it while rows render
        // changes the layout, which renders the rows again.
        int[] counts = new int[2];
        ValueStateVariable<Integer> live = new ValueStateVariable<> (0);
        addOther (StateComponentCreator.build (live, (s, el) -> {
            Div.$ (el).testId ("tablecpts.live").text (s.value () + " live");
        }), new ActionBarLayout.Data (0));

        // Re-renders the rows in place (the records are unchanged), reloads with
        // fewer rows and shows the live count.
        addOther (ComponentCreator.build (el -> {
            Btn.$ (el, "Count").testId ("tablecpts.count").onclick (() -> live.assign (counts[0] - counts[1]));
            Btn.$ (el, "Refresh").testId ("tablecpts.refresh").onclick (() -> store.refresh ());
            Btn.$ (el, "Reverse").testId ("tablecpts.reverse").onclick (() -> store.reverse ());
            Btn.$ (el, "Reload 5").testId ("tablecpts.reload").onclick (() -> store.refresh (records.subList (0, 5)));
        }), new ActionBarLayout.Data (0));

        // With a row click handler (and re-rendering rows on refresh).
        Table<SampleRecord> table = TableCreator.build (cfg -> {
            cfg.renderOnRefresh (true);
            cfg.onclick (r -> lastClick.assign ("row:" + r.getName ()));
            cfg.header ("Name", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Div.$ (cell).testId ("tablecpts.name").text (r.getName ());
                }));
                header.width (Length.em (16));
            });
            cfg.header ("Actions", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Button btn = ButtonCreator.build (b -> {
                        b.testId ("tablecpts.open");
                        b.label ("Open");
                        b.handler (() -> lastClick.assign ("button:" + r.getName ()));
                    });
                    counts[0]++;
                    btn.addListener (IDisposeListener.create (c -> counts[1]++));
                    Cpt.$ (cell, btn);
                }));
            });
            cfg.header ("Check", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Cpt.$ (cell, CheckControlCreator.build (c -> {
                        c.testId ("tablecpts.check");
                        c.label ("Flag");
                    }));
                }));
            });
            cfg.header ("Note", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Cpt.$ (cell, TextControlCreator.build (c -> {
                        c.testId ("tablecpts.note");
                    }));
                }));
            });
            cfg.header ("Status", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Cpt.$ (cell, ComponentCreator.build (el -> {
                        Span.$ (el).testId ("tablecpts.status").text ("Active");
                    }));
                }));
            });
        }, store);
        add (table);

        // Without a row click handler.
        Table<SampleRecord> plainTable = TableCreator.build (cfg -> {
            cfg.header ("Name", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Div.$ (cell).testId ("tablecpts.plain.name").text (r.getName ());
                }));
                header.width (Length.em (16));
            });
            cfg.header ("Actions", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    Cpt.$ (cell, ButtonCreator.build (b -> {
                        b.testId ("tablecpts.plain.open");
                        b.label ("Open");
                        b.handler (() -> lastClick.assign ("plain:" + r.getName ()));
                    }));
                }));
            });
        }, plainStore);
        add (plainTable);

        store.load (20);
        plainStore.load (20);
    }

    /**
     * A store of a fixed list of records (so the table does not page in more as it
     * is scrolled to the end) that can be reversed in place.
     */
    static class RecordStore extends ListPaginatedStore<SampleRecord> {

        RecordStore(List<SampleRecord> initial) {
            records ().addAll (initial);
        }

        /**
         * Reverses the order of the records and refreshes in place (the records are
         * retained so the table re-inserts the rows that are out of order).
         */
        void reverse() {
            Collections.reverse (records ());
            refresh ();
        }
    }
}
