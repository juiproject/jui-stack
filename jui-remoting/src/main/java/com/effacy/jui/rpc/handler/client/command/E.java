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
package com.effacy.jui.rpc.handler.client.command;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.TypeMode;
import com.effacy.jui.rpc.client.Remote;


/**
 * Base class for all commands that provides some useful debugging and
 * referencing information.
 *
 * @author Jeremy Buckley
 */
@JsonSerializable(settersRequired = false, type = TypeMode.SIMPLE)
public abstract class E extends Remote {

    /**
     * Marks if the command is doing something.
     */
    private boolean dirty;


    /**
     * Construct with no lookup ID (creator).
     */
    protected E() {
        // Nothing.
    }


    /**
     * Is the command instance actually doing something.
     * <p>
     * This is only intended to be used when the command is being created in
     * some context and one is not sure if changes have actually been applied.
     * The value of dirty is NOT serialized.
     * 
     * @return {@code true} if it is.
     */
    public boolean dirty() {
        return dirty;
    }


    /**
     * Assigns a value to a member and marks the command as dirty.
     * 
     * @param member
     *            the member to assign the value to.
     * @param value
     *            the value to assign.
     */
    protected <T> void assign(V<T> member, T value) {
        member.assign (value);
        mark ();
    }


    /**
     * Marks the command as being dirty.
     */
    protected void mark() {
        dirty = true;
    }

}
