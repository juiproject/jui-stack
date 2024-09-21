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
package com.effacy.jui.playground.ui.tutorial;

import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;
import com.effacy.jui.ui.client.control.TextControl;

import elemental2.dom.Element;

/**
 * Starter component for the unqiue users gallery.
 */
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
            // TODO: Implement this.
        }).build ();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.IResetable#reset()
     */
    @Override
    public void reset() {
        // Reloads the store with an initial 10 records.
        store.reload (10);
    }

}

