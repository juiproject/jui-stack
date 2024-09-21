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

/**
 * Base class for all records. This defines a remote record over a Long as an
 * ID.
 * 
 * @author Jeremy Buckley
 */
public abstract class RecordResult<V> extends Result implements IComparableRecord<V> {

    /**
     * The record ID.
     */
    private V id;

    /**
     * The record version (for optimistic locking).
     */
    private int version = 0;

    /**
     * Default constructor.
     */
    protected RecordResult() {
        // Nothing.
    }


    /**
     * Construct with an ID.
     * 
     * @param id
     *            the ID.
     */
    protected RecordResult(V id) {
        setId (id);
    }


    /**
     * Construct with an ID and a version.
     * 
     * @param id
     *            the ID.
     * @param version
     *            the version.
     */
    protected RecordResult(V id, int version) {
        setId (id);
        setVersion (version);
    }


    /**
     * Copy constructor.
     * 
     * @param copy
     *            the record to copy.
     */
    protected RecordResult(RecordResult<V> copy) {
        this.id = copy.getId ();
        this.version = copy.getVersion ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.dto.data.IRecord#getId()
     */
    @Override
    public V getId() {
        return id;
    }


    /**
     * Gets the version of the record (for optimistic locking).
     * 
     * @return The record version.
     */
    public int getVersion() {
        return version;
    }


    /**
     * Sets the ID.
     * 
     * @param id
     *            the id.
     */
    public void setId(V id) {
        this.id = id;
    }


    /**
     * Sets the version.
     * 
     * @param version
     *            the version.
     */
    public void setVersion(Integer version) {
        this.version = (version == null) ? -1 : version;
    }

    @Override
    public boolean equals(Object arg) {
        if (arg == null)
            return false;
        if (this == arg)
            return true;
        if (!(arg instanceof RecordResult))
            return false;
        if (id == null) {
            if (((RecordResult<?>) arg).id != null)
                return false;
        } else if (!id.equals(((RecordResult<?>) arg).id))
            return false;
        return (version == ((RecordResult<?>) arg).version);
    }

    @Override
    public int hashCode() {
        if (id == null)
            return 0;
        return id.hashCode() + (int) version;
    }

    @Override
    public boolean same(Object record) {
        if (record == null)
            return false;
        if (this == record)
            return true;
        if (!(record instanceof RecordResult))
            return false;
        if (id == null) {
            if (((RecordResult<?>) record).id == null)
                return true;
            return false;
        }
        return id.equals (((RecordResult<?>) record).id);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Record [id=" + id + ", version=" + version + "]";
    }
}
