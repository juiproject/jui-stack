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
package com.effacy.jui.rpc.handler.client.query;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.TypeMode;
import com.effacy.jui.rpc.client.Remote;

/**
 * A general object the embodies data to perform a remote query. Essentially a
 * carrier for key methods needed to manage such types.
 * 
 * @author Jeremy Buckley
 */
@JsonSerializable(type = TypeMode.SIMPLE)
public abstract class Query<T> extends Remote {

    /**
     * Default (empty) constructor.
     */
    public Query() {
        // Nothing.
    }


    /**
     * Copy constructor.
     * 
     * @param copy
     *            the query to copy.
     */
    public Query(Query<?> copy) {
        // Nothing.
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (!super.equals (obj))
            return false;
        return obj instanceof Query;
    }

    /**
     * Creates a copy of the query.
     * 
     * @return The copy of the query.
     */
    public Query<T> copy() {
        return null;
    }


    /**
     * Resets the query to nothing.
     */
    public void reset() {
        // Nothing.
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass ().getSimpleName ();
    }

}
