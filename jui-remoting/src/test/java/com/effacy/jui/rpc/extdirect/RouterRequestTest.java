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
package com.effacy.jui.rpc.extdirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.effacy.jui.rpc.extdirect.annotation.AnnotatedRouter;
import com.effacy.jui.rpc.extdirect.annotation.RemoteAction;
import com.effacy.jui.rpc.extdirect.annotation.RemoteMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests how the router reads JSON requests, in particular requests that can't
 * be read (JUI-4, JUI-5 and JUI-6).
 */
public class RouterRequestTest {

    public static class Item {
        public int id;
    }

    public static class Paged {
        public int page;
        public List<Item> items;
    }

    @RemoteAction(name = "TestService")
    public static class TestService {

        @RemoteMethod
        public String none() {
            return "none";
        }

        @RemoteMethod
        public String echo(String value) {
            return "echo:" + value;
        }

        @RemoteMethod
        public String paged(Paged paged, String suffix) {
            return "page:" + ((paged == null) ? "null" : paged.page) + ":" + suffix;
        }
    }

    public static class TestRouter extends AnnotatedRouter {

        public TestRouter() {
            setUseCsrfTokens (false);
            scanPackages ();
            addAction (new TestService ());
        }

        public String process(String body, int[] status) throws IOException {
            return processRequestToString (request (body), response (status));
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    private TestRouter router;

    @BeforeEach
    public void setup() {
        router = new TestRouter ();
    }

    /**
     * JUI-4: each response carries the tid of the call it answers.
     */
    @Test
    public void tidIsEchoed() throws Exception {
        JsonNode responses = process ("[" + call ("echo", 7, "[\"a\"]") + "," + call ("echo", 9, "[\"b\"]") + "]");
        Assertions.assertEquals (2, responses.size ());
        Assertions.assertEquals (7, responses.get (0).get ("tid").asInt ());
        Assertions.assertEquals ("echo:a", result (responses.get (0)).asText ());
        Assertions.assertEquals (9, responses.get (1).get ("tid").asInt ());
        Assertions.assertEquals ("echo:b", result (responses.get (1)).asText ());
    }

    /**
     * A single call (not in an array) is still processed.
     */
    @Test
    public void singleCall() throws Exception {
        JsonNode responses = process (call ("echo", 3, "[\"a\"]"));
        Assertions.assertEquals (1, responses.size ());
        Assertions.assertEquals (3, responses.get (0).get ("tid").asInt ());
        Assertions.assertEquals ("echo:a", result (responses.get (0)).asText ());
    }

    /**
     * JUI-5: an argument that can't be read is rejected with a validation error
     * (with the path to the field) and the other calls in the batch are still
     * processed.
     */
    @Test
    public void unreadableArgument() throws Exception {
        for (String page : new String[] { "2147483648", "4294967296", "1e20", "\"abc\"" }) {
            JsonNode responses = process ("[" + call ("paged", 4, "[{\"page\":" + page + "},\"x\"]") + "," + call ("echo", 5, "[\"b\"]") + "]");
            Assertions.assertEquals (2, responses.size (), page);
            assertRejected (responses.get (0), "paged", 4, "page");
            Assertions.assertEquals ("echo:b", result (responses.get (1)).asText (), page);
        }
    }

    /**
     * JUI-5: the path is that within the argument (including list indices).
     */
    @Test
    public void unreadableArgumentNested() throws Exception {
        JsonNode responses = process (call ("paged", 4, "[{\"page\":1,\"items\":[{\"id\":1},{\"id\":\"abc\"}]},\"x\"]"));
        assertRejected (responses.get (0), "paged", 4, "items.1.id");
    }

    /**
     * JUI-5: a value of the wrong type for a top-level argument.
     */
    @Test
    public void unreadableArgumentTopLevel() throws Exception {
        JsonNode responses = process (call ("paged", 4, "[\"x\",\"x\"]"));
        assertRejected (responses.get (0), "paged", 4, null);
    }

    /**
     * JUI-6: too few or too many arguments are rejected with a validation error
     * and the other calls in the batch are still processed.
     */
    @Test
    public void wrongArgumentCount() throws Exception {
        for (String data : new String[] { "[]", "[null]", "null", "[{\"page\":1}]", "[{\"page\":1},\"x\",[]]" }) {
            JsonNode responses = process ("[" + call ("paged", 6, data) + "," + call ("echo", 7, "[\"b\"]") + "]");
            Assertions.assertEquals (2, responses.size (), data);
            JsonNode messages = assertRejected (responses.get (0), "paged", 6, null);
            Assertions.assertTrue (messages.get (0).get ("message").asText ().contains ("takes 2 arguments"), data);
            Assertions.assertEquals ("echo:b", result (responses.get (1)).asText (), data);
        }

        // No data at all.
        JsonNode responses = process ("[{\"action\":\"TestService\",\"method\":\"paged\",\"type\":\"rpc\",\"tid\":6}]");
        assertRejected (responses.get (0), "paged", 6, null);
    }

    /**
     * JUI-6: an explicit null has the right number of arguments and reaches the
     * method; as does a method without parameters passed no data.
     */
    @Test
    public void explicitNullArgument() throws Exception {
        JsonNode responses = process (call ("paged", 8, "[null,null]"));
        Assertions.assertEquals ("page:null:null", result (responses.get (0)).asText ());

        responses = process ("[{\"action\":\"TestService\",\"method\":\"none\",\"type\":\"rpc\",\"tid\":1,\"data\":null}]");
        Assertions.assertEquals ("none", result (responses.get (0)).asText ());
    }

    /**
     * JUI-5: a body that isn't a call or array of calls answers a 400.
     */
    @Test
    public void invalidBody() throws Exception {
        for (String body : new String[] { "not json", "[1,2]", "[]", "1", "[" + call ("echo", 1, "[\"a\"]") + ",1]" }) {
            int[] status = new int[] { 200 };
            String result = router.process (body, status);
            Assertions.assertEquals (400, status[0], body);
            Assertions.assertEquals ("", result, body);
        }
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    private JsonNode process(String body) throws Exception {
        int[] status = new int[] { 200 };
        String result = router.process (body, status);
        Assertions.assertEquals (200, status[0]);
        JsonNode node = MAPPER.readTree (result);
        Assertions.assertTrue (node.isArray (), result);
        return node;
    }

    private JsonNode result(JsonNode response) {
        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        return response.get ("result");
    }

    /**
     * Asserts the response is a rejection and returns the error messages. A
     * {@code null} path expects a general message (one not against a field).
     */
    private JsonNode assertRejected(JsonNode response, String method, int tid, String path) {
        Assertions.assertEquals ("TestService", response.get ("action").asText (), response.toString ());
        Assertions.assertEquals (method, response.get ("method").asText (), response.toString ());
        Assertions.assertEquals (tid, response.get ("tid").asInt (), response.toString ());
        JsonNode result = result (response);
        Assertions.assertEquals ("ERROR_VALIDATION", result.get ("outcome").asText (), response.toString ());
        JsonNode messages = result.get ("messages");
        Assertions.assertEquals (1, messages.size (), response.toString ());
        Assertions.assertEquals ("VALIDATION", messages.get (0).get ("type").asText (), response.toString ());
        if (path == null)
            Assertions.assertTrue (messages.get (0).get ("general").asBoolean (), response.toString ());
        else
            Assertions.assertEquals (path, messages.get (0).get ("path").asText (), response.toString ());
        return messages;
    }

    private static String call(String method, int tid, String data) {
        return "{\"action\":\"TestService\",\"method\":\"" + method + "\",\"type\":\"rpc\",\"tid\":" + tid + ",\"data\":" + data + "}";
    }

    private static HttpServletRequest request(String body) {
        return (HttpServletRequest) Proxy.newProxyInstance (RouterRequestTest.class.getClassLoader (), new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
            switch (method.getName ()) {
                case "getMethod":
                    return "POST";
                case "getContentType":
                    return "application/json";
                case "getReader":
                    return new BufferedReader (new StringReader (body));
                case "getAttributeNames":
                case "getHeaderNames":
                case "getParameterNames":
                    return java.util.Collections.emptyEnumeration ();
                default:
                    return null;
            }
        });
    }

    private static HttpServletResponse response(int[] status) {
        List<Integer> statuses = new ArrayList<> ();
        return (HttpServletResponse) Proxy.newProxyInstance (RouterRequestTest.class.getClassLoader (), new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
            if ("setStatus".equals (method.getName ())) {
                statuses.add ((Integer) args[0]);
                status[0] = (Integer) args[0];
            }
            return null;
        });
    }
}
