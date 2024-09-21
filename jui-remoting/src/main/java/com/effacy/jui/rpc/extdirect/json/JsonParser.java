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
package com.effacy.jui.rpc.extdirect.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.json.parser.IJsonSerializableParser;
import com.effacy.jui.json.parser.JsonSerializableParser;
import com.effacy.jui.rpc.extdirect.RemoteCallRequest;
import com.effacy.jui.rpc.extdirect.RemoteCallResponse;
import com.effacy.jui.rpc.extdirect.RemoteCallType;
import com.effacy.jui.rpc.extdirect.metadata.IParameterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IRouterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.RouterMetadataUtils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;

/**
 * JSON parser for deserialising requests and serialising responses.
 */
public class JsonParser implements IJsonParser {

    /**
     * The underlying JSON parser that know how to work with remote objects.
     */
    private IJsonSerializableParser parser;

    /**
     * Constructs using the default parser {@see JsonSerializableParser}.
     * 
     * @param metadata
     *                       metadata from the router to resolve the parameter types
     *                       for the inbound request.
     * @param additions
     *                       to provide additions to the parser.
     * @param packagesToScan
     *                       the packages that should be scanned (used for
     *                       polymorphism and generics)
     */
    public JsonParser(IRouterMetadata metadata, String...packagesToScan) {
        this (metadata, null, packagesToScan);
    }

    /**
     * Constructs using the default parser {@see JsonSerializableParser}.
     * 
     * @param metadata
     *                       metadata from the router to resolve the parameter types
     *                       for the inbound request.
     * @param additions
     *                       to provide additions to the parser.
     * @param packagesToScan
     *                       the packages that should be scanned (used for
     *                       polymorphism and generics)
     */
    public JsonParser(IRouterMetadata metadata, Consumer<SimpleModule> additions, String...packagesToScan) {
        // Here we use the standard json serialisable parser that can handle
        // polymorphism.
        this.parser = new JsonSerializableParser ().scanPackages (module -> {
            if (metadata != null)
                module.addDeserializer (RemoteCallRequest.class, new RemoteCallRequestDeserializer (metadata));
            if (additions != null)
                additions.accept (module);
        }, packagesToScan);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.extdirect.json.IJsonParser#remoteCallResponseToJson(com.com.effacy.jui.rpc.extdirect.RemoteCallResponse)
     */
    @Override
    public String remoteCallResponseToJson(RemoteCallResponse response) throws JsonParserException {
        try {
            return parser.toJson (response);
        } catch (com.effacy.jui.json.parser.JsonParserException e) {
            throw new JsonParserException (e.getCause ());
        } 
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.extdirect.json.IJsonParser#jsonToRemoteCallRequests(java.lang.String,
     *      com.effacy.jui.rpc.extdirect.metadata.IRouterMetadata)
     */
    @Override
    public List<RemoteCallRequest> jsonToRemoteCallRequests(String jsonString) throws JsonParserException {
        try {
            try {
                RemoteCallRequest request = parser.fromJson (jsonString, RemoteCallRequest.class);
                List<RemoteCallRequest> requests = new ArrayList<RemoteCallRequest> ();
                requests.add (request);
                return requests;
            } catch (com.effacy.jui.json.parser.JsonParserException e) {
                // Try converting as a list.
                return parser.fromJsonList (jsonString, RemoteCallRequest.class);
            }
        } catch (com.effacy.jui.json.parser.JsonParserException e) {
            throw new JsonParserException (e.getCause ());
        } catch (Throwable e) {
            throw new JsonParserException (e);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.extdirect.json.IJsonParser#jsonToType(String, Class)
     */
    @Override
    public <T> T jsonToType(String jsonString, Class<T> klass) throws JsonParserException {
        try {
            return parser.fromJson (jsonString, klass);
        } catch (com.effacy.jui.json.parser.JsonParserException e) {
            throw new JsonParserException (e.getCause ());
        } catch (Throwable e) {
            throw new JsonParserException (e);
        }
    }

    /**
     * Deserialiser to deserialise inbound remote calls.
     * <p>
     * This makes use of the router meta-data to resolve the parameter types.
     */
    public static class RemoteCallRequestDeserializer extends StdDeserializer<RemoteCallRequest> {

        private IRouterMetadata metadata;

        public RemoteCallRequestDeserializer(IRouterMetadata metadata) {
            super (RemoteCallRequest.class);
            this.metadata = metadata;
        }

        @Override
        public RemoteCallRequest deserialize(com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec ();
            JsonNode root = mapper.readTree (jp);

            // The pasrer may try to resolve a remote call or an array of remote calls. We
            // need to reject arrays here.
            if (root instanceof ArrayNode)
                throw MismatchedInputException.from (jp, RemoteCallRequest.class, "Unexpected content");

            // Build out the request fields.
            RemoteCallRequest request = new RemoteCallRequest();
            request.setAction (asString (root, "action", null));
            request.setCsrfToken (asString (root, "csrfToken", null));
            request.setMethod (asString (root, "method", null));
            request.setTid (asInt (root, "action", 0));
            request.setType (asRemoteCallType (root, "type"));

            // Resolve the passed data.
            JsonNode dataNode = root.get ("data");
            if (dataNode instanceof ArrayNode) {
                List<IParameterMetadata> methodSignature = RouterMetadataUtils.resolveMethodTypes (request.getAction (), request.getMethod (), metadata);
                if (methodSignature != null) {
                    for (int i = 0, len = ((ArrayNode) dataNode).size(); i < len; i++) {
                        JsonNode objectNode = ((ArrayNode) dataNode).get (i);
                        if (methodSignature.size() <= i)
                            throw MismatchedInputException.from (jp, RemoteCallRequest.class, "Mismatched parameters");
                        IParameterMetadata parameterMetadata = methodSignature.get(i);
                        request.getData ().add (mapper.treeToValue (objectNode, parameterMetadata.getParameterType ()));
                    }
                }
            }
            return request;
        }

        private String asString(JsonNode node, String name, String def) {
            JsonNode n = node.get (name);
            if (n == null)
                return def;
            return n.asText (def);
        }

        private int asInt(JsonNode node, String name, int def) {
            JsonNode n = node.get (name);
            if (!(n instanceof IntNode))
                return def;
            return ((IntNode)n).asInt (def);
        }

        private RemoteCallType asRemoteCallType(JsonNode node, String name) {
            JsonNode n = node.get (name);
            if (n == null)
                return null;
            try {
                return RemoteCallType.valueOf (n.asText ());
            } catch (Throwable e) {
                return null;
            }
        }
    }
    
}
