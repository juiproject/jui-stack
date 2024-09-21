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
package com.effacy.jui.codeserver.gwt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PatternMatchingTest {

    @Test
    public void testMatchSourceMap() {
        Assertions.assertTrue (Constants.CACHE_JS_FILE.matcher ("B18823731866FF07C57D242838329554.cache.js").find ());
     }
}
