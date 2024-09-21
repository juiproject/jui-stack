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
package com.effacy.jui.core.client.state;

import com.effacy.jui.platform.util.client.Logger;

public class InvalidatableStateVariable<V extends InvalidatableStateVariable<V>> extends StateVariable<V> {

    /**
     * Invalidate state.
     */
    private boolean invalidated = true;

    /**
     * See {@link StateVariable#onModify()}.
     */
    @Override
    protected void onModify() {
        this.invalidated = false;
    }

    /**
     * Determines if the state is invalidated.
     * 
     * @return {@code true} if it is.
     */
    public boolean isInvalidated() {
        return invalidated;
    }

    /**
     * If the state has an affirmed variable.
     * 
     * @return {@code true} if it is.
     */
    public boolean isOK() {
        return !invalidated;
    }

    /**
     * Marks as OK leaving the value unchanged.
     */
    public void ok() {
        ok (false);
    }

    /**
     * See {@link #ok()}.
     * 
     * @param force
     *              forces an emit event if not changed.
     */
    public void ok(boolean force) {
        if (!invalidated) {
            if (debug)
                Logger.log ("{state:ok_already} [" + toString() + "]");
            if (force)
                emit ();
        } else {
            if (debug)
                Logger.log ("{state:ok} [" + toString() + "]");
            invalidated = false;
            emit ();
        } 
    }

    /**
     * Transition to the invalidated state.
     * <p>
     * After this method has been called {@link #isInvalidated()} will return
     * {@code true}.
     */
    public void invalidate() {
        invalidate (false);
    }

    /**
     * See {@link #invalidate()}.
     * 
     * @param force
     *              forces an emit event if not changed.
     */
    public void invalidate(boolean force) {
        if (invalidated) {
            if (debug)
                Logger.log ("{state:invalidate_already} [" + toString() + "]");
            if (force)
                emit ();
        } else {
            if (debug)
                Logger.log ("{state:invalidate} [" + toString() + "]");
            invalidated = true;
            emit ();
        }
    }

    @Override
    public String toString() {
        return super.toString() + "::" + (invalidated ? "INVALID" : "VALID");
    }

    
}
