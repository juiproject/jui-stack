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
package com.effacy.jui.core.client.dom.builder;

/**
 * Something that can be inserted into a DOM node.
 */
public interface IDomInsertable {

    /**
     * Called to insert into the provided container builder.
     * <p>
     * Normally this is not called directly but rather by the instance of
     * {@link IDomInsertableContainer} into which this has been inserted.
     * 
     * @param parent
     *               the parent builder to build into.
     */
    public void insertInto(ContainerBuilder<?> parent);
    
}
