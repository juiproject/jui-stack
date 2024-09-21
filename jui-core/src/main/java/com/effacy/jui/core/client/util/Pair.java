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
package com.effacy.jui.core.client.util;

/**
 * A pair of objects.
 *
 * @author Jeremy Buckley
 */
public class Pair<T1, T2> {

    /**
     * See {@link #item1()}.
     */
    private T1 item1;

    /**
     * See {@link #item2()}.
     */
    private T2 item2;

    /**
     * Construct with a pair of items.
     * 
     * @param item1
     *            the first entry in the pair.
     * @param item2
     *            the second entry in the pair.
     */
    public Pair(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }


    /**
     * The first entry in the pair.
     * 
     * @return the entry.
     */
    public T1 item1() {
        return item1;
    }


    /**
     * The second entry in the pair.
     * 
     * @return the entry.
     */
    public T2 item2() {
        return item2;
    }
}
