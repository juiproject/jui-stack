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
package com.effacy.jui.ui.client.control.builder;

import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IRowBuilder.IControlCell;

/**
 * Gains access to the controls (and groups) within a form along with some
 * supporting operations.
 */
public interface IModificationContext {

    /**
     * Retrieves the referenced (using the {@link IControlCell#by(String)} method) control.
     * <p>
     * This is not type safe. If you want type safety the externalise the control to
     * a variable.
     * 
     * @param reference
     *                  the reference to the control.
     * @return the associated control (or {@code null} if it does not exist).
     */
    public <W> IControl<W> control(String reference);

    /**
     * Retrieves the value on the named control. If there is a conditional group
     * then the group discriminator will be returned or, if not discriminated, then
     * a boolean indicating whether it is open or not.
     * <p>
     * This is not type safe. If you want type safety the externalise the control to
     * a variable.
     * 
     * @param reference
     *                  the reference to the control.
     * @return the value of the control.
     */
    public <W> W value(String reference);
    
    /**
     * Retrieves the value on the named control. If the value is {@code null} (or
     * the control not mapped) then the default value is returned.
     * <p>
     * This is not type safe. If you want type safety the externalise the control to
     * a variable.
     * 
     * @param reference
     *                     the reference to the control.
     * @param defaultValue
     *                     the default value to return when the control is not
     *                     present or its value is {@code null}.
     * @return the value of the control (or the default).
     */
    default public <W> W value(String reference, W defaultValue) {
        W value = value (reference);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Assigns a value to another control.
     * <p>
     * This is not type safe. If you want type safety the externalise the control to
     * a variable.
     * 
     * @param reference
     *                  the reference to the control.
     * @param value
     *                  the value to assign.
     */
    default public <W> void set(String reference, W value) {
        set (reference, Value.of (value));
    }
    
    /**
     * Assigns a value to another control.
     * <p>
     * This is not type safe. If you want type safety the externalise the control to
     * a variable.
     * 
     * @param reference
     *                  the reference to the control.
     * @param value
     *                  the value to assign.
     */
    public <W> void set(String reference, Value<W> value);
    
    /**
     * Enable the controls matching the given references.
     * 
     * @param references
     *                   the {@code by(String)} references.
     */
    public void enable(String...references);

    /**
     * Disable the controls matching the given references.
     * 
     * @param references
     *                   the {@code by(String)} references.
     */
    public void disable(String...references);

    /**
     * Show the controls (or groups) matching the given references.
     * 
     * @param references
     *                   the {@code by(String)} references.
     */
    public void show(String...references);

    /**
     * Hide the controls (or groups) matching the given references.
     * 
     * @param references
     *                   the {@code by(String)} references.
     */
    public void hide(String...references);

    /**
     * Determines if the referenced group is open.
     * 
     * @param reference
     *                  the group referenced.
     * @return {@code true} if it is open.
     */
    public boolean groupOpen(String reference);
}
