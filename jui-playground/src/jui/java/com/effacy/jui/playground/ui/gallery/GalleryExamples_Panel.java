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

import java.util.List;

import com.effacy.jui.core.client.IActivateAware;
import com.effacy.jui.core.client.component.layout.ActionBarLayout;
import com.effacy.jui.core.client.component.layout.ActionBarLayout.HAlignment;
import com.effacy.jui.core.client.control.DelayedModifiedHandler;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.store.ListPaginatedStore;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.gallery.EmptyNotification;
import com.effacy.jui.ui.client.gallery.Gallery;
import com.effacy.jui.ui.client.gallery.item.PanelGalleryItem;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.SplitPanel;

/**
 * Simple example of a panel gallery displaying a collection of records subject
 * to a keyword filter.
 * 
 * @author Jeremy Buckley
 */
public class GalleryExamples_Panel extends SplitPanel implements IActivateAware {

    /**
     * The backing store for the gallery.
     */
    private SampleRecordStore store = new SampleRecordStore ();

    /**
     * Search control for filtering the gallery store.
     */
    private TextControl searchCtl;

    /**
     * Construct the panel.
     */
    public GalleryExamples_Panel() {
        super (new SplitPanel.Config ()
                .otherLayout (new ActionBarLayout.Config ().zone (HAlignment.LEFT).zone (HAlignment.RIGHT).insets (Insets.em (0.5)).build ())
                .vertical ()
                .separator ()
        );

        // Attach to the store a listener so when the store is cleared so it the keyword
        // search filter.
        store.handleOnClear (s -> {
            searchCtl.setValue (Value.of ("").quiet ());
        });

        // The search filter is a text control suitably configured to include a clear
        // action. When modified this will invoke a filtering on the store. Since
        // filtering often involves keying in the search terms then we introduce a
        // modification delay mechanism that won't invoke the store filter until key
        // presses have slowed or stopped (otherwise we will generate a lot of RPC
        // queries for the case where the store retrieves its data remotely).
        addOther (searchCtl = new TextControl.Config () //
                .iconLeft (FontAwesome.search ()) //
                .width (Length.em (15)) //
                .placeholder ("Search records") //
                .clearAction () //
                .modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                    if (StringSupport.empty (val))
                        store.filter (null);
                    else
                        store.filter (r -> r.name ().toLowerCase ().contains (val.toLowerCase ()));
                })).build (), new ActionBarLayout.Data (0));

        // The gallery configured against the store and a SinglePanelGalleryItem (duely
        // configured).
        Gallery<SampleRecord> gallery = new Gallery<SampleRecord> (store, PanelGalleryItem.create ((renderer) -> {
            renderer.width (Length.em (20));
            renderer.height (Length.em (8));
            renderer.clickHandler (item -> {
                Logger.log ("EDIT GROUP DETAILS1: " + item.getRecord ().name);
            });
            renderer.header (d -> d.name (), header -> {
                header.titleIcon (d -> {
                    return FontAwesome.user ();
                });
                header.clickHandler (item -> {
                    Logger.log ("EDIT GROUP DETAILS2: " + item.getRecord ().name);
                });
                header.menu (menu -> {
                    menu.item ("Open") //
                            .icon (FontAwesome.close ()) //
                            .clickHandler (r -> Logger.log ("Open " + r.getRecord ().name ()));
                    menu.item ("Close") //
                            .icon (FontAwesome.golfBall ()) //
                            .clickHandler (r -> Logger.log ("Close " + r.getRecord ().name ()));
                });
            });

        }));
        add (gallery);

        // Configure the display for the case where the gallery has no contents as a
        // result of filtering.
        gallery.config ().emptyFiltered ((v) -> EmptyNotification.buildPanel (v, new EmptyNotification (p -> {
            p.title ("Nothing to show");
            p.paragraph ("No results found for the filter options you have choosen.");
            p.action ("Clear filters", () -> {
                store.clearAndReload ();
            });
            p.actionsRightAligned (true);
        })));
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IActivateAware#onAnyActivation()
     */
    @Override
    public void onAnyActivation() {
        searchCtl.setValue ((String) null);
        store.clearAndReload ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IActivateAware#onActivated()
     */
    @Override
    public void onActivated() {
        IActivateAware.super.onActivated ();
    }

    /**
     * Sample record that the store holds instance of.
     */
    public class SampleRecord {

        private String name;

        public SampleRecord(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    /**
     * Sample store that holds a dummy set of {@link SampleRecord} items.
     */
    public class SampleRecordStore extends ListPaginatedStore<SampleRecord> {

        /**
         * {@inheritDoc}
         *
         * @see com.effacy.jui.playground.ui.gallery.ListPaginatedStore#populate(java.util.List)
         */
        @Override
        protected void populate(List<SampleRecord> records) {
            for (int i = 0; i < 100; i++)
                records.add (new SampleRecord ("Record " + i));
        }

    }

}
