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
package com.effacy.jui.ui.client.modal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Provides a pool of modal dialogs that can be called upon to be opened. This
 * allows for more than one instance to display at any given time while only
 * requiring the minimum number of instances to be created.
 */
public class ModalPool<M extends Modal<?>> {

    /**
     * A supplier of new modal dialog instances.
     */
    private Supplier<M> factory;

    /**
     * A pool of instances to draw upon.
     */
    private Set<M> instances = new HashSet<> ();

    /**
     * Constructor for subclasses. It is expected that {@link #create()} will be
     * overridden.
     */
    protected ModalPool() {
        // Nothing.
    }


    /**
     * Construct with a factory of (new) instances of the modal.
     * 
     * @param factory
     *            the factory of modals.
     */
    public ModalPool(Supplier<M> factory) {
        this.factory = factory;
    }


    /**
     * Acquire a dialog ready to be opened.
     * <p>
     * Note that the dialog should be opened before a subsequent call to this
     * method.
     * 
     * @return the dialog.
     */
    public M acquire() {
        for (M dialog : instances) {
            if (!dialog.isOpen ())
                return dialog;
        }
        M dialog = create ();
        instances.add (dialog);
        return dialog;
    }


    /**
     * Creates a new modal instance.
     * 
     * @return the instance.
     */
    protected M create() {
        if (factory != null)
            return factory.get ();
        return null;
    }
}
