/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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
package com.effacy.jui.text.type.edit;

import java.util.Collections;
import java.util.List;

/**
 * A composed sequence of {@link StepMap}s from a {@link Transaction}.
 * <p>
 * Maps a position through all step maps in order, producing the final position
 * after all steps have been applied.
 */
public class Mapping {

    /**
     * Empty mapping — no position changes.
     */
    public static final Mapping EMPTY = new Mapping(Collections.emptyList());

    private List<StepMap> maps;

    public Mapping(List<StepMap> maps) {
        this.maps = maps;
    }

    /**
     * Maps a position through all step maps sequentially.
     *
     * @param pos
     *             the position to map.
     * @param bias
     *             mapping bias: {@code -1} for left, {@code 1} for right.
     * @return the mapped position.
     */
    public int map(int pos, int bias) {
        for (StepMap map : maps)
            pos = map.map(pos, bias);
        return pos;
    }

    /**
     * The individual step maps.
     */
    public List<StepMap> maps() {
        return Collections.unmodifiableList(maps);
    }
}
