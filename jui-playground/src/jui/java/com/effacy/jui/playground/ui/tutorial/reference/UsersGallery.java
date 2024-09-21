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
package com.effacy.jui.playground.ui.tutorial.reference;

import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.control.DelayedModifiedHandler;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.DateSupport;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.playground.ui.tutorial.UserResult;
import com.effacy.jui.playground.ui.tutorial.UserResultStore;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.control.TextControlCreator;
import com.effacy.jui.ui.client.gallery.EmptyNotification;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.table.ITableCellRenderer;
import com.effacy.jui.ui.client.table.Table.Config.SortDirection;
import com.effacy.jui.ui.client.table.TableCreator;
import com.effacy.jui.ui.client.table.renderer.TextTableCellRenderer;

import elemental2.dom.Element;

public class UsersGallery extends Component<Component.Config> implements IResetable {

    /**
     * The store to source the users and to perform filtering (and sorting) on.
     */
    private UserResultStore store = new UserResultStore ();

    /**
     * The search control (referenced so that it can be cleared when there are no
     * results).
     */
    private TextControl searchCtl;

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(elemental2.dom.Element,
     *      com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Element el, Config data) {
        return DomBuilder.el (el, root -> {
            root.addClassName ("users");
            root.div (header -> {
                header.addClassName ("heading");
                header.h3 ().text ("Unique users");
                header.div (tool -> {
                    // Here we attach a text component which serves as our search.
                    tool.apply (attach (searchCtl = TextControlCreator.build (cfg -> {
                        cfg.clearAction ();
                        cfg.placeholder ("Search users");
                        cfg.width (Length.em (12));
                        cfg.iconRight (FontAwesome.search ());
                        // We use a delayed handler here so as not to reload the store on each key
                        // press.
                        cfg.modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                            if (StringSupport.empty (val))
                                store.filter (null);
                            else
                                store.filter (r -> r.getName ().toLowerCase ().contains (val.toLowerCase ()));
                        }));
                    })));
                });
            });
            root.div (holder -> {
                holder.addClassName ("holder");
                holder.by ("holder");
                // Here we attach a table component configured to render data from the use
                // store.
                holder.apply (attach (TableCreator.<UserResult>build (cfg -> {
                    cfg.scrollable ();
                    cfg.header ("Name", header -> {
                        header.renderer (TextTableCellRenderer.create (r -> r.getName ()));
                        header.sortable (SortDirection.DESC, s -> store.sortByName (s == SortDirection.ASC));
                        header.width (Length.em (12));
                    });
                    cfg.header ("Unique visits", header -> {
                        header.renderer (TextTableCellRenderer.create (r -> "" + r.getVisits ()));
                        header.sortable (SortDirection.DESC, s -> store.sortByAccess (s == SortDirection.ASC));
                        header.width (Length.em (4));
                    });
                    cfg.header ("Last access", header -> {
                        header.renderer (TextTableCellRenderer.create (r -> {
                            if (r.getLastAccess () == null)
                                return "Not available";
                            return DateSupport.formatDateTime (r.getLastAccess ());
                        }));
                        header.width (Length.em (5));
                    });
                    cfg.header ("Rating", header -> {
                        // Custom renderer for the stars.
                        header.renderer (ITableCellRenderer.custom ((elt, d) -> {
                            DomBuilder.el (elt, item -> item.div (outer -> {
                                outer.addClassName ("rating");
                                for (int i = -1; i < d.getRating (); i++)
                                    outer.em ().addClassName (FontAwesome.star ());
                            })).build ();
                        }));
                        header.width (Length.em (8));
                    });
                    cfg.emptyFiltered ((v) -> EmptyNotification.buildPanel (v, new EmptyNotification (p -> {
                        p.title ("Nothing to show");
                        p.paragraph ("No results found for the search you have choosen.");
                        p.action ("Clear filters", () -> {
                            store.clearAndReload ();
                            searchCtl.setValue ("");
                        });
                        p.actionsRightAligned (true);
                    })));
                }, store)));
            });
        }).build ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IResetable#reset()
     */
    @Override
    public void reset() {
        store.reload (10);
    }

}
