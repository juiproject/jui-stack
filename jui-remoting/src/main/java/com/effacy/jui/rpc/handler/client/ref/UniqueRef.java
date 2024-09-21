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
 * This is a references such that each instance is globally unique. It is used
 * in commands to ensure one can reference the product of a given command. A
 * typical usage scenario is where an entity needs to be created and then later
 * referenced. In this case the reference can be may by way of this reference
 * lookup as obtained from the prior command.
 * 
 * @author Jeremy Buckley
 */
public class UniqueRef extends Ref {

    /**
     * Counter specifically for generating UID's.
     */
    private static long COUNTER = 0;

    /**
     * The reference.
     */
    private long reference;


    /**
     * Constructs with an automatically created unique reference.
     */
    public UniqueRef() {
        this.reference = COUNTER++;
    }


    /**
     * Construct with a known reference.
     * 
     * @param reference
     *            the reference.
     */
    public UniqueRef(long reference) {
        this.reference = reference;
    }


    /**
     * Obtains the (unqiue) reference.
     * 
     * @return the reference.
     */
    public long getReference() {
        return reference;
    }


    /**
     * Sets the (unique) reference.
     * 
     * @param reference
     *            the reference.
     */
    public void setReference(long reference) {
        this.reference = reference;
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
        try {
            return (reference == ((UniqueRef) obj).reference);
        } catch (ClassCastException e) {
            return false;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) reference;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Long.toString (reference);
    }

}
