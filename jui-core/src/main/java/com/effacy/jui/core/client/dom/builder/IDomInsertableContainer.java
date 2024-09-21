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

import java.util.function.Consumer;

/**
 * Something that allows for the insertion of DOM builders.
 * 
 * @param <T> the class type (for return).
 */
public interface IDomInsertableContainer<T extends IDomInsertableContainer<T>> {

    /**
     * Inserts the passed children into the container.
     * <p>
     * The expectation is that the container will make a {@link ContainerBuilder}
     * available to build nodes into by passing that container to
     * {@link IDomInsertable#insertInto(ContainerBuilder)}.
     * <p>
     * There is no specification as to when the resolution of insertables is to be
     * done.
     * 
     * @param children
     *                 the children to insert.
     * @return this instance (as typed by T).
     */
    public T insert(IDomInsertable... children);

    /**
     * Short-hand convenience for calling {@link #insert(IDomInsertable...)}.
     * 
     * @param children
     *                 the children to insert.
     * @return this instance (as typed by T).
     */
    default T $(IDomInsertable... children) {
        return insert (children);
    }

    /**
     * Short-hand convenience for operating on this container.
     * 
     * @param self
     *               to operate on this container.
     * @return this instance (as typed by T).
     */
    @SuppressWarnings("unchecked")
    default T $(Consumer<T> self) {
        self.accept ((T) this);
        return (T) this;
    }
}
