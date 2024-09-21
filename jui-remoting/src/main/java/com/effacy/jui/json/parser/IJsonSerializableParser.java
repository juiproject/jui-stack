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

import java.util.List;

/**
 * Abstraction of a parser. This is intended to be used server-side for parsing
 * JSON to Java classes such that the parser respects the guidance gleened from
 * use of the {@link JsonSerialzable} annotation.
 *
 * @author Jeremy Buckley
 */
public interface IJsonSerializableParser {

    /**
     * Converts a JSON string to an instance of the given type.
     * 
     * @param <V>
     *            the value type.
     * @param jsonString
     *            the json string to parse.
     * @param type
     *            the type to parse into.
     * @return the parsed type instance.
     * @throws JsonParserException
     *             on error.
     */
    public <V> V fromJson(String jsonString, Class<V> type) throws JsonParserException;

    
    /**
     * Converts a JSON string to a list of the given type.
     * 
     * @param <V>
     *            the value type.
     * @param jsonString
     *            the json string to parse.
     * @param type
     *            the type to parse into.
     * @return the parsed type instance.
     * @throws JsonParserException
     *             on error.
     */
    public <V> List<V> fromJsonList(String jsonString, Class<V> type) throws JsonParserException;

    /**
     * Convert from a java object to JSON.
     * 
     * @param obj
     *            the object to convert.
     * @return the serialized object.
     * @throws JsonParserException
     *             on error.
     */
    public String toJson(Object obj) throws JsonParserException;
}
