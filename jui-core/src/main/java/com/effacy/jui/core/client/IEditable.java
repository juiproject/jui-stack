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

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.platform.util.client.Logger;

/**
 * Describes something as being editable.
 * <p>
 * The majority of methods have default implementations that simply delegate the
 * call through to child components that implement {@link IEditable}. That makes
 * it easier to build composite editors without implementing distribution code.
 * <p>
 * Classes that implement this, and are not containers of some form, need to
 * provide implementations for {@link #edit(V)} and ideally also
 * {@link #editLoading()} and {@link #editFailed()} (optionally for
 * {@link #editFailed(String)} to render the message).
 */
public interface IEditable<V> {

    /**
     * Invoked when loading data for edit.
     * <p>
     * This allows the recipient to display a suitable loading state.
     * <p>
     * The default behaviour is to iterate over all child components and then
     * delegating through to each child that implements {@link IEditable}.
     */
    @SuppressWarnings("unchecked")
    default public void editLoading() {
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (cpt -> {
                if (cpt instanceof IEditable) {
                    try {
                        ((IEditable<V>) cpt).editLoading ();
                    } catch (Throwable e) {
                        Logger.reportUncaughtException (e);
                    }
                }
            });
        }
    }

    /**
     * Invoked when loading failed.
     * <p>
     * This allows the recipient to display a suitable failed state.
     * <p>
     * The default behaviour is to iterate over all child components and then
     * delegating through to each child that implements {@link IEditable}.
     */
    @SuppressWarnings("unchecked")
    default public void editFailed() {
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (cpt -> {
                if (cpt instanceof IEditable) {
                    try {
                        ((IEditable<V>) cpt).editFailed ();
                    } catch (Throwable e) {
                        Logger.reportUncaughtException (e);
                    }
                }
            });
        }
    }

    /**
     * See {@link #editFailed()} but receives an error message.
     * <p>
     * The default behaviour is simply to invoke {@link #editFailed()}.
     * 
     * @param message
     *                the error message.
     */
    default public void editFailed(String message) {
        editFailed();
    }

    /**
     * Edit based on an resolver.
     * <p>
     * The default behaviour is to place in a loading state then retrieve the value
     * from the resolver. On error {@link #editFailed(String)} is invoked while on
     * success the resolved value is passed through to {@link #edit(V)}.
     * 
     * @param resolver
     *                 the resolver.
     */
    default public void edit(IResolver<V> resolver) {
        onEditByResolver(resolver);
        editLoading ();
        resolver.resolve (v -> {
            edit (v);
        }, f -> {
            editFailed (f);
        });
    }

    /**
     * Called when a resolver is passed through (see {@link #edit(IResolver)}).
     * 
     * @param resolver
     *                 the resolver.
     */
    default public void onEditByResolver(IResolver<V> resolver) {
        // Nothing.
    }

    /**
     * Passes through the value to edit.
     * <p>
     * The default behaviour is to iterate over all child components and then
     * delegating through to {@link #editGeneric(Object)} on each child that
     * implements {@link IEditable}. The {@link #editGeneric(Object)} is responsible
     * for any type conversion.
     * 
     * @param value
     *              the value.
     */
    @SuppressWarnings("unchecked")
    default public void edit(V value) {
        if (this instanceof IComponent) {
            ((IComponent) this).forEach (cpt -> {
                if (cpt instanceof IEditable) {
                    try {
                        ((IEditable<V>) cpt).editGeneric (value);
                    } catch (Throwable e) {
                        Logger.reportUncaughtException (e);
                    }
                }
            });
        }
    }

    /**
     * This is used during dispatch of an edit by the default {@link #edit(V)}
     * method. This takes a generic object and attempts to convert it to the value
     * type then passes that through to {@link #edit(V)}. The default behavuour is a
     * cast.
     * 
     * @param ovaluej
     *                the value being edited.
     */
    @SuppressWarnings("unchecked")
    default void editGeneric(Object value) {
        edit ((V) value);
    }

}
