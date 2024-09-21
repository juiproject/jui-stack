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

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.json.annotation.JsonSerializable;

/**
 * Wrapper around a list value type for the purpose of setting and getting
 * values in the list.
 * 
 * @author Jeremy Buckley
 */
@JsonSerializable
public abstract class L<T> {

    /**
     * Items to add to the list.
     */
    private List<T> additions;

    /**
     * Items to remove from the list.
     */
    private List<T> removals;

    /**
     * If everything should be removed prior to apply the additions.
     */
    private boolean removeAll;

    /**
     * Flag indicating if the value has been set.
     */
    private boolean set = false;


    /**
     * Performs a list add.
     * 
     * @param value
     *            the value to add.
     */
    public void add(T value) {
        getAdditions ().add (value);
        setSet (true);
    }


    /**
     * Performs a list remove.
     * 
     * @param value
     *            the value to remove.
     */
    public void remove(T value) {
        getRemovals ().add (value);
        setSet (true);
    }


    /**
     * Clear the list and remove all the elements contained within.
     */
    public void clear() {
        setRemoveAll (true);
        setSet (true);
    }


    /**
     * Additions getter. Only for serialization.
     * 
     * @return the additions.
     */
    public List<T> getAdditions() {
        if (additions == null)
            additions = new ArrayList<T> ();
        return additions;
    }


    /**
     * Setter for additions. See {@link #getAdditions()}.
     * 
     * @param additions
     *            the additions.
     */
    public void setAdditions(List<T> additions) {
        this.additions = additions;
    }


    /**
     * Removal getter. Only for serialization.
     * 
     * @return the removals.
     */
    public List<T> getRemovals() {
        if (removals == null)
            removals = new ArrayList<T> ();
        return removals;
    }


    /**
     * Sets the removals. See {@link #getRemovals()}.
     * 
     * @param removals
     *            the removals.
     */
    public void setRemovals(List<T> removals) {
        this.removals = removals;
    }


    /**
     * Remove all getter. Only for serialization.
     * 
     * @return {@code true} if should remove everything.
     */
    public boolean isRemoveAll() {
        return removeAll;
    }


    /**
     * Setter for remova all. See {@link #isRemoveAll()}.
     * 
     * @param removeAll
     *            {@code true} if all should be removed.
     */
    public void setRemoveAll(boolean removeAll) {
        this.removeAll = removeAll;
    }


    /**
     * Getter for the set status. Only for serialization.
     * 
     * @return {@code true} if a value has been assigned.
     */
    public boolean isSet() {
        return set;
    }


    /**
     * Setters for the set status. See {@link #isSet()}.
     * 
     * @param set
     *            {@code true} to make the value as having been set.
     */
    public void setSet(boolean set) {
        this.set = set;
    }

}
