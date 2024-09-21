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
package com.effacy.jui.core.client.component;

import com.effacy.jui.core.client.dom.IUIEventHandler;

import elemental2.dom.Element;

/**
 * A type of {@link IUIEventHandler} that includes a method that the component
 * should invoked to perform selection on a DOM element.
 *
 * @author Jeremy Buckley
 */
public interface ISelectableUIEventHandler extends IUIEventHandler {

    /**
     * To be invoked to select and bind events to from the passed element.
     * 
     * @param rootEl
     *               the element.
     */
    public void select(Element rootEl);

}
