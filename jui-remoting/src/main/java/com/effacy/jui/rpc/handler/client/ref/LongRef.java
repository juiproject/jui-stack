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
package com.effacy.jui.rpc.handler.client.ref;

/**
 * A reference that is based on a long value. This type of reference is typical
 * for entities.
 * 
 * @author Jeremy Buckley
 */
public class LongRef extends Ref {

    /**
     * The look up value.
     */
    private long id;


    /**
     * Serialization constructor.
     */
    protected LongRef() {
        // Nothing.
    }


    /**
     * Construct with a value.
     * 
     * @param id
     *            the lookup value.
     */
    public LongRef(long id) {
        this.id = id;
    }


    /**
     * Getter for serialization only.
     */
    public long getId() {
        return id;
    }


    /**
     * Setter for serialization only.
     */
    public void setId(long id) {
        this.id = id;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return Long.toString (id);
    }
}
