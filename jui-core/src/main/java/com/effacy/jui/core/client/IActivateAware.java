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
 * Implemented by content that is aware of being activated.
 * <p>
 * Activation is generally relevant to layouts, namely when a layout activates a
 * child component and that component becomes visible (i.e. during navigation).
 * This differs from {@link IOpenAware} which is relevant when something is
 * first opened (i.e. a dialog). Activations may occur multiple time within an
 * open context.
 *
 * @author Jeremy Buckley
 */
public interface IActivateAware {

    /**
     * Invoked a component has been activated (but was not previously active).
     * <p>
     * See also {@link #onAnyActivation()}.
     */
    default public void onActivated() {
        onAnyActivation ();
    }

    /**
     * Invoked for a re-activation. This will occur when the component is being
     * activated but was already active (for layouts this can be quite common when
     * one activates a child component that was previously active without
     * necessarily knowing that it was).
     * <p>
     * See also {@link #onAnyActivation()}.
     */
    default public void onReActivated() {
        onAnyActivation ();
    }

    /**
     * Convenience to receive events on either {@link #onActivated()} or
     * {@link #onReActivated()}. Note that this is implemented at the interface
     * level so overriding and of the other methods, without invoking the
     * super-method, may result in this failing (i.e it is assumed you will use this
     * method or the other methods mutually exclusively).
     */
    default public void onAnyActivation() {
        // Nothing.
    }
}
