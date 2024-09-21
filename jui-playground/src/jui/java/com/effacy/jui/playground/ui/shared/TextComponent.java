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
package com.effacy.jui.playground.ui.shared;

import com.effacy.jui.core.client.component.Component;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.DomBuilder;

/**
 * Very simple component to display some text within a P tag.
 *
 * @author Jeremy Buckley
 */
public class TextComponent extends Component<Component.Config> {

    /**
     * The text to display.
     */
    private String text;

    /**
     * Construct with text to display.
     * 
     * @param text
     *             the text.
     */
    public TextComponent(String text) {
        super (new Component.Config ());
        this.text = text;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.core.client.component.Component#buildNode(com.effacy.jui.core.client.component.Component.Config)
     */
    @Override
    protected INodeProvider buildNode(Config data) {
        return DomBuilder.p ().text (text).build ();
    }

}
