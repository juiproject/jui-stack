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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.effacy.jui.json.annotation.LogRedact;
import com.effacy.jui.json.annotation.LogTruncate;
import com.effacy.jui.json.annotation.Transient;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A JSON serializer to be used to generate JSON specifically for logging. This
 * respsects the various annotations related to field modification (i.e.
 * redaction).
 */
public class LogSerializer {

    /**
     * Jackson object mapper (customized).
     */
    /**
     * Mapper for serialisation.
     */
    private ObjectWriter json;

    /**
     * Construct instance of the serialiser.
     */
    public LogSerializer(boolean pretty) {
        JsonMapper mapper = JsonMapper.builder ()
            .annotationIntrospector (new LogAnnotationIntrospector ())
            .configure (SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure (SerializationFeature.INDENT_OUTPUT, pretty)
            .build ();
        json = mapper.writer ();
    }

    /**
     * Convert the passed object to JSON.
     * 
     * @param record
     *               the record to convert.
     * @return JSON version of the record.
     * @throws JsonParserException on error.
     */
    public String toJson(Object record) throws JsonParserException {
        if (record == null)
            return "null";
        try {
            return json.writeValueAsString (record);
        } catch (Throwable e) {
            throw new JsonParserException (e);
        }
    }

    /**
     * Looks for and handles the various log annotations.
     */
    static class LogAnnotationIntrospector extends JacksonAnnotationIntrospector {

        /**
         * Maps of redaction serialisers for re-use.
         */
        private Map<RedactSerializer.Config<?>,RedactSerializer<?>> redactions = new HashMap<>();

        /**
         * Maps of truncation serialisers for re-use.
         */
        private Map<TruncateSerializer.Config,TruncateSerializer> truncations = new HashMap<>();

        @SuppressWarnings({"unchecked","rawtypes"})
        @Override
        public Object findSerializer(Annotated a) {
            // Check for redaction.
            LogRedact redact = a.getAnnotation (LogRedact.class);
            if (redact != null) {
                RedactSerializer.Config config = new RedactSerializer.Config (a.getRawType(), redact.replace ());
                RedactSerializer<?> serializer = redactions.get (config);
                if (serializer == null) {
                    serializer = new RedactSerializer (config);
                    redactions.put (config, serializer);
                }
                return serializer;
            }

            // Check for truncation.
            LogTruncate truncate = a.getAnnotation(LogTruncate.class);
            if ((truncate != null) && a.getRawType().equals(String.class)) {
                TruncateSerializer.Config config = new TruncateSerializer.Config (truncate.length (), truncate.mid ());
                TruncateSerializer serializer = truncations.get (config);
                if (serializer == null) {
                    serializer = new TruncateSerializer (config);
                    truncations.put (config, serializer);
                }
                return serializer;
            }

            // No intervention.
            return null;
        }

        @Override
        protected boolean _isIgnorable(Annotated a) {
            return super._isIgnorable(a) || a.hasAnnotation(Transient.class);
        }

        /**
         * Serialiser used for redaction.
         */
        public class RedactSerializer<T> extends StdSerializer<T> {

            /**
             * Lookup key.
             */
            public record Config<T>(Class<T> klass, String value) {

                @Override
                public int hashCode() {
                    return Objects.hash (klass, value);
                }

                @Override
                public boolean equals(Object other) {
                    if (this == other)
                        return true;
                    if (other == null)
                        return false;
                    try {
                        Config<T> config = (Config<T>) other;
                        return config.klass.equals (this.klass)
                             && config.value.equals (this.value);
                    } catch (ClassCastException e) {
                        return false;
                    }
                }
            }
        
            /**
             * The value to redact by.
             */
            private Config<T> config;

            /**
             * Construct with the redaction value.
             * 
             * @param value
             *              the value.
             */
            public RedactSerializer(Config<T> config) {
                super (config.klass());
                this.config = config;
            }

            @Override
            public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                jgen.writeString (config.value ());
            }
        }

        /**
         * Serialiser used for redaction.
         */
        public class TruncateSerializer extends StdSerializer<String> {

            /**
             * Lookup key.
             */
            public record Config(int length, boolean mid) {

                public Config {
                    if (length < 0)
                        length = 10;
                }

                @Override
                public int hashCode() {
                    return Objects.hash (length, mid);
                }

                @Override
                public boolean equals(Object other) {
                    if (this == other)
                        return true;
                    if (other == null)
                        return false;
                    try {
                        Config config = (Config) other;
                        return config.length == this.length
                             && config.mid == this.mid;
                    } catch (ClassCastException e) {
                        return false;
                    }
                }
            }
        
            /**
             * The value to redact by.
             */
            private Config config;

            /**
             * Construct with the redaction value.
             * 
             * @param value
             *              the value.
             */
            public TruncateSerializer(Config config) {
                super (String.class);
                this.config = config;
            }

            @Override
            public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                if (value == null) {
                    jgen.writeNull ();
                } else if (value.length() <= config.length) {
                    jgen.writeString (value);
                } else {
                    if (!config.mid) {
                        value = value.substring (0, config.length) + "...";
                    } else {
                        value = value.substring(0, config.length / 2) + "..." + value.substring(value.length() - (config.length / 2));
                    }
                    jgen.writeString (value);
                }
            }
        }
    }
    
}
