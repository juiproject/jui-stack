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
package com.effacy.jui.json.parser;

/**
 * Thrown when there is a problem parsing
 *
 * @author Jeremy Buckley
 */
public class JsonParserException extends Exception {

    /**
     * Unique serialization ID.
     */
    private static final long serialVersionUID = -3749877234447458050L;


    /**
     * Construct with a cause.
     * 
     * @param cause
     *            the cause.
     */
    public JsonParserException(Throwable cause) {
        super (cause);
    }

}
