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
package com.effacy.jui.core.client.component.layout;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.dom.DomSupport;

import elemental2.dom.Element;
import jsinterop.base.Js;

/**
 * This provides a simple default layout renderer. It is the same as is used by
 * default in the layout.
 * 
 * @author Jeremy Buckley
 */
public class DefaultLayoutRenderer implements ILayoutRenderer {

    /**
     * Convience instance.
     */
    public static final ILayoutRenderer DEFAULT = new DefaultLayoutRenderer ();

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.core.client.container.layout.ILayoutRenderer#renderComponent(com.effacy.jui.core.client.component.IComponent,
     *      int, com.effacy.gwt.dom.client.Elem, int)
     */
    @Override
    public IComponentLayout renderComponent(IComponent component, int index, Element target, int size) {
        if (component.getRoot () != null) {
            DomSupport.insertChild (target, component.getRoot (), index);
            return new ComponentLayout (Js.cast (target));
        }
        component.render (Js.cast (target), index);
        return new ComponentLayout (Js.cast (component.getRoot ()));
    }

}
