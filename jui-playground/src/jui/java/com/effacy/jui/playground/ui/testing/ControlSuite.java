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
package com.effacy.jui.playground.ui.testing;

import java.util.List;

import com.effacy.jui.core.client.component.layout.VertLayoutCreator;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.store.ListPaginatedStore;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.ui.client.control.Controls;
import com.effacy.jui.ui.client.control.MultiSelectionControl.Config.SelectionStyle;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;

public class ControlSuite extends Panel {

    public ControlSuite() {
        super (PanelCreator.config ().scrollable ().padding (Insets.em (2)).layout (VertLayoutCreator.create (Length.em (1))));

        // Panel bar = add (PanelCreator.buttonBar());

        add (Controls.text(cfg -> {
            cfg.testId ("text-static-1");
            cfg.width (Length.em (12));
            cfg.placeholder("Some text");
        }));

        add (PanelCreator.buttonBar(cfg -> {}, panel -> {
            panel.add (Controls.number(cfg -> {
                cfg.testId ("number-static-1");
                cfg.width (Length.em (10));
                cfg.decimalPlaces(3);
                cfg.step(1.2);
            }));
            panel.add (Controls.number(cfg -> {
                cfg.testId ("number-static-2");
                cfg.width (Length.em (10));
                cfg.step(2.0);
                cfg.max(6.0);
                cfg.min(1.0);
            }));
            panel.add (Controls.number(cfg -> {
                cfg.testId ("number-static-2");
                cfg.width (Length.em (6));
                cfg.stepHide();
            }));
        }));
        
        add (Controls.selector (cfg -> {
            cfg.testId ("selector-static-1");
            cfg.width (Length.em (12));
            cfg.allowEmpty ();
        }, "Value 1", "Value 2", "Value 3", "Value 4", "Value 5"));

        add (Controls.<SampleRecordStore.SampleRecord> selector (cfg -> {
            cfg.testId ("selector-static-2");
            cfg.width (Length.em (12));
            cfg.allowEmpty ();
            // cfg.allowSearch (false);
            cfg.store (new SampleRecordStore());
            cfg.labelMapper (r -> r.name);
        }));

        add (Controls.multiselector (cfg -> {
            cfg.testId ("multiselector-static-1");
            cfg.width (Length.em (20));
            cfg.selectionStyle (SelectionStyle.CHIP);
            cfg.modifiedHandler((ctl, val, prior) -> {
                Logger.info ("Selected: " + ListSupport.contract (val));
            });
        }, "Value 1", "Value 2", "Value 3", "Value 4", "Value 5", "Value 6", "Value 7", "Value 8"));
    }

    /**
     * Test store with delay.
     */
    public static class SampleRecordStore extends ListPaginatedStore<SampleRecordStore.SampleRecord> {

        public static class SampleRecord {
    
            String name;
    
            public SampleRecord(String name) {
                this.name = name;
            }
        }

        @Override
        protected void populate(List<SampleRecord> records) {
            for (int i = 0; i < 100; i++)
                records.add (new SampleRecord ("Record " + i));
        }

        @Override
        protected void requestLoad(int page, int pageSize, ILoadRequestCallback<SampleRecord> cb) {
            // Put in a fake delay.
            super.requestLoad(page, pageSize, new ILoadRequestCallback<SampleRecord>() {

                @Override
                public void onSuccess(List<SampleRecord> items, int totalAvailable, boolean filtered) {
                    TimerSupport.timer(() -> {
                        cb.onSuccess(items, totalAvailable, filtered);
                    }, 200);
                }

                @Override
                public void onFailure(String message) {
                    cb.onFailure(message);
                }
                
            });
        }

    }
}
