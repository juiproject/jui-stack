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
import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.extdirect.RemoteCallRequest;
import com.effacy.jui.rpc.extdirect.RemoteCallResponse;
import com.effacy.jui.rpc.extdirect.RemoteCallType;
import com.effacy.jui.rpc.extdirect.metadata.IParameterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.IRouterMetadata;
import com.effacy.jui.rpc.extdirect.metadata.RouterMetadataUtils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
            // Each call is read on its own so that a call that can't be read is
            // reported against that call (and does not take down the others in
            // the batch).
            JsonNode root = parser.fromJson (jsonString, JsonNode.class);
            List<RemoteCallRequest> requests = new ArrayList<RemoteCallRequest> ();
            if (root instanceof ObjectNode) {
                requests.add (parser.fromJson (root.toString (), RemoteCallRequest.class));
            } else if ((root instanceof ArrayNode) && !root.isEmpty ()) {
                for (JsonNode node : root) {
                    if (!(node instanceof ObjectNode))
                        throw new JsonParserException (new IllegalArgumentException ("Expected a remote call but found: " + node.getNodeType ()));
                    requests.add (parser.fromJson (node.toString (), RemoteCallRequest.class));
                }
            } else {
                throw new JsonParserException (new IllegalArgumentException ("Expected a remote call or a non-empty array of remote calls."));
            }
            return requests;
        } catch (JsonParserException e) {
            throw e;
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

        /**
         * The error type assigned to rejection messages (matches that used for
         * validation errors raised by processors).
         */
        public static final String ERROR_TYPE = "VALIDATION";

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
            request.setTid (asInt (root, "tid", 0));
            request.setType (asRemoteCallType (root, "type"));

            // Resolve the passed data. Problems with the data are the client's
            // and are recorded against the request (as a rejection) rather than
            // thrown, so the call can be answered as such.
            List<IParameterMetadata> methodSignature = RouterMetadataUtils.resolveMethodTypes (request.getAction (), request.getMethod (), metadata);
            if (methodSignature == null)
                return request;
            JsonNode dataNode = root.get ("data");
            if (!(dataNode instanceof ArrayNode)) {
                // Methods without parameters don't need any data (and we
                // don't insist on it being an empty array).
                if (methodSignature.isEmpty ())
                    return request;
                if ((dataNode == null) || dataNode.isNull ())
                    request.reject (new ErrorMessage (null, ERROR_TYPE, -1, argumentCountMessage (request, methodSignature.size (), 0)));
                else
                    request.reject (new ErrorMessage (null, ERROR_TYPE, -1, "The arguments must be passed as an array."));
                return request;
            }
            if (dataNode.size () != methodSignature.size ()) {
                request.reject (new ErrorMessage (null, ERROR_TYPE, -1, argumentCountMessage (request, methodSignature.size (), dataNode.size ())));
                return request;
            }
            for (int i = 0, len = dataNode.size (); i < len; i++) {
                IParameterMetadata parameterMetadata = methodSignature.get (i);
                try {
                    request.getData ().add (mapper.treeToValue (dataNode.get (i), parameterMetadata.getParameterType ()));
                } catch (JacksonException | IllegalArgumentException e) {
                    request.getData ().add (null);
                    request.reject (new ErrorMessage (toPath (e), ERROR_TYPE, -1, "The value could not be read."));
                }
            }
            return request;
        }

        /**
         * Describes an argument count mismatch.
         */
        private String argumentCountMessage(RemoteCallRequest request, int expected, int passed) {
            return request.getAction () + "." + request.getMethod () + " takes " + expected + ((expected == 1) ? " argument" : " arguments") + " but " + passed + " " + ((passed == 1) ? "was" : "were") + " passed.";
        }

        /**
         * Converts the path of a mapping exception (relative to the argument
         * being read) to a dot-separated path (i.e. {@code page} or
         * {@code 0._lookup.id}). Returns {@code null} if there is no path.
         */
        private String toPath(Exception e) {
            if (!(e instanceof JsonMappingException))
                return null;
            StringBuilder sb = new StringBuilder ();
            for (JsonMappingException.Reference ref : ((JsonMappingException) e).getPath ()) {
                if (sb.length () > 0)
                    sb.append ('.');
                if (ref.getFieldName () != null)
                    sb.append (ref.getFieldName ());
                else if (ref.getIndex () >= 0)
                    sb.append (ref.getIndex ());
            }
            return (sb.length () == 0) ? null : sb.toString ();
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
