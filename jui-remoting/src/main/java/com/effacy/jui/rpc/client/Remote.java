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
package com.effacy.jui.rpc.client;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.TypeMode;
import com.effacy.jui.rpc.RemoteSupport;
import com.effacy.jui.rpc.RemoteSupport.FieldPair;

/**
 * Base class for remote objects provding some debugging support using
 * {@link RemoteSupport}.
 */
@JsonSerializable(settersRequired = false, type = TypeMode.SIMPLE)
public abstract class Remote {

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer ();
        sb.append (RemoteSupport.getSimpleName (getClass ()));
        sb.append ("[");
        boolean comma = toStringPreamble (sb);
        for (FieldPair field : RemoteSupport.getDeclaredFields (this))
            comma = toStringPair (sb, comma, field.getName (), toStringFieldValue (field));
        sb.append ("]");
        return sb.toString ();
    }


    /**
     * Optionally place a pre-amble to the string.
     * 
     * @param sb
     *            the string buffer to write to.
     * @return {@code true} if a comma should be follow for additional entries.
     */
    protected boolean toStringPreamble(StringBuffer sb) {
        return false;
    }


    /**
     * Hook to allow the conversion of a field value.
     * 
     * @param field
     *            the field.
     * @param value
     *            the value of the field.
     * @return the revised value of the field.
     */
    protected Object toStringFieldValue(FieldPair field) {
        return field.getValue ();
    }


    /**
     * Write a pair of a field and field value to the string buffer optionally
     * pre-pending a comma if needed.
     * 
     * @param sb
     *            the string buffer to write to.
     * @param comma
     *            {@code true} if a comma should be pre-pended.
     * @param name
     *            the field name.
     * @param value
     *            the field value.
     * @return always {@code true} so can be used to set any boolean used to
     *         track the value of the comma.FOs
     */
    private boolean toStringPair(StringBuffer sb, boolean comma, String name, Object value) {
        if (comma)
            sb.append (',');
        sb.append (name);
        sb.append ('=');
        if (value instanceof String) {
            sb.append ('"');
            sb.append (value);
            sb.append ('"');
        } else
            sb.append (value.toString ());
        return true;
    }
}
