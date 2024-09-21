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
package com.effacy.jui.core.client;

/**
 * Represents something that can be closed.
 *
 * @author Jeremy Buckley
 */
public interface IClosable {

    /**
     * Request a close.
     * <p>
     * The exact behaviour of this will depend on the context. For example,
     * closing a panel will generally result in the firing of a
     * {@link IModalController#close()} event (which, if included in a modal,
     * will result in the closing of the modal).
     */
    public void close();
}
