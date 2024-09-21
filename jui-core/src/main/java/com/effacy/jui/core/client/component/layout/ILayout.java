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
import com.effacy.jui.core.client.observable.IObservable;
import com.effacy.jui.platform.util.client.Promise;

import elemental2.dom.Event;

/**
 * A mechanism that performs a layout of components in a container region.
 * 
 * @author Jeremy Buckley
 */
public interface ILayout extends IObservable {

    /**
     * Enumerates the various activation outcomes.
     */
    public enum ActivateOutcome {
        /**
         * The component was activated.
         */
        ACTIVATED,

        /**
         * The component was already activated.
         */
        ALREADY_ACTIVATED,

        /**
         * The component was not being managed by the layout.
         */
        NOT_PRESENT;
    }

    /**
     * Processes a browser event that has occurred in the region of the container
     * that the layout is managing. This will happen if the event has occurred
     * between the component and the container which will likely to have been
     * rendered by the layout.
     * 
     * @param event
     *              the event to process.
     */
    public void onBrowserEvent(Event event);

    /**
     * Sets the layout's container. This will attach listeners to the container to
     * detect changes in the containers contents so that the layout can respond
     * accordingly.
     * 
     * @param ct
     *           the container the layout will act upon.
     */
    public void setLayoutTarget(ILayoutTarget layoutTarget);

    /**
     * Executes the layout on its associated layout target (i.e. container). The
     * layout will be flagged as running ({@link #isRunning()} will return
     * {@code true}). It is this method that is responsible for rendering and
     * positioning the children the layout or resize all the children. Once complete
     * the children will have their layout methods invoked if they are containers.
     * 
     * @param force
     *              {@code true} if the layout should be forced (otherwise the
     *              layout will only be performed if the layout has been marked as
     *              dirty).
     * @return {@code true} if the layout was performed (may not be performed if
     *         there is no container set, the container is not rendered or the
     *         layout is already running on the container).
     */
    public boolean layout(boolean force);

    /**
     * Determines whether or not the layout is currently running (running layouts
     * are not re-entered).
     * 
     * @return {@code true} if the layout is currently running.
     */
    public boolean isRunning();

    /**
     * Mark the layout as dirty so that a layout will be performed when the layout
     * is invoked.
     */
    public void markAsDirty();

    /**
     * Some layouts permit a contained component to be active in some sense. This
     * provides that facility.
     * 
     * @param cpt
     *            the component to make active.
     * @return {@code true} the outcome of the activation as a promise (which allows
     *         for async loading or rendering of components).
     */
    default Promise<ActivateOutcome> activate(IComponent cpt) {
        return Promise.create (ActivateOutcome.NOT_PRESENT);
    }
}
