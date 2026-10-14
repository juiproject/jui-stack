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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.effacy.jui.rpc.extdirect.annotation.RemoteAction;
import com.effacy.jui.rpc.extdirect.annotation.RemoteMethod;
import com.effacy.jui.rpc.extdirect.csrf.ICRFHandler;
import com.effacy.jui.rpc.extdirect.dto.TestItemQuery;
import com.effacy.jui.rpc.extdirect.dto.TestQuery;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests how the router reads requests, in particular requests that can't be
 * read (JUI-4, JUI-5 and JUI-6). Requests are passed through
 * {@link ControllerRouter#handleRequest(HttpServletRequest, HttpServletResponse)}
 * and the resulting view rendered, as they would be by a host application.
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

        @RemoteMethod
        public String query(TestQuery query) {
            if (query instanceof TestItemQuery item)
                return "query:" + item.page + ":" + ((item.lookups == null) ? 0 : item.lookups.size ());
            return "query:" + ((query == null) ? "null" : query.getClass ().getSimpleName ());
        }
    }

    /**
     * Router as a host application would configure it (scanning for the
     * polymorphic test DTOs).
     */
    public static class TestRouter extends ControllerRouter {

        public TestRouter() {
            setUseCsrfTokens (false);
            scanPackages ("com.effacy.jui.rpc.extdirect.dto");
            addAction (new TestService ());
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    private ControllerRouter router = new TestRouter ();

    /************************************************************************
     * Transaction IDs (JUI-4).
     ************************************************************************/

    /**
     * Each response carries the tid of the call it answers.
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

    /************************************************************************
     * Arguments that can't be read (JUI-5).
     ************************************************************************/

    /**
     * An argument that can't be read is rejected with a validation error (with
     * the path to the field) and the other calls in the batch are still
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
     * The path is that within the argument (including list indices).
     */
    @Test
    public void unreadableArgumentNested() throws Exception {
        JsonNode responses = process (call ("paged", 4, "[{\"page\":1,\"items\":[{\"id\":1},{\"id\":\"abc\"}]},\"x\"]"));
        assertRejected (responses.get (0), "paged", 4, "items.1.id");
    }

    /**
     * A value of the wrong type for a top-level argument.
     */
    @Test
    public void unreadableArgumentTopLevel() throws Exception {
        JsonNode responses = process (call ("paged", 4, "[\"x\",\"x\"]"));
        assertRejected (responses.get (0), "paged", 4, null);
    }

    /**
     * A polymorphic argument (resolved by {@code _type}) is read as its
     * concrete type.
     */
    @Test
    public void polymorphicArgument() throws Exception {
        JsonNode responses = process (call ("query", 1, "[{\"_type\":\"TestItemQuery\",\"page\":3,\"lookup\":{\"_type\":\"TestIdLookup\",\"id\":7},\"lookups\":[{\"_type\":\"TestIdLookup\",\"id\":1}]}]"));
        Assertions.assertEquals ("query:3:1", result (responses.get (0)).asText ());
    }

    /**
     * Fields that can't be read in a polymorphic argument, including within
     * nested polymorphic values and lists of them.
     */
    @Test
    public void polymorphicArgumentUnreadable() throws Exception {
        JsonNode responses = process (call ("query", 1, "[{\"_type\":\"TestItemQuery\",\"page\":2147483648}]"));
        assertRejected (responses.get (0), "query", 1, "page");

        responses = process (call ("query", 2, "[{\"_type\":\"TestItemQuery\",\"lookup\":{\"_type\":\"TestIdLookup\",\"id\":\"abc\"}}]"));
        assertRejected (responses.get (0), "query", 2, "lookup.id");

        responses = process (call ("query", 3, "[{\"_type\":\"TestItemQuery\",\"lookups\":[{\"_type\":\"TestIdLookup\",\"id\":1},{\"_type\":\"TestIdLookup\",\"id\":\"abc\"}]}]"));
        assertRejected (responses.get (0), "query", 3, "lookups.1.id");
    }

    /**
     * A polymorphic argument whose {@code _type} is unknown (or missing) can't
     * be read as the parameter type.
     */
    @Test
    public void polymorphicArgumentUnknownType() throws Exception {
        JsonNode responses = process (call ("query", 1, "[{\"_type\":\"Bogus\",\"page\":1}]"));
        assertRejected (responses.get (0), "query", 1, null);

        responses = process (call ("query", 2, "[{\"page\":1}]"));
        assertRejected (responses.get (0), "query", 2, null);

        responses = process (call ("query", 3, "[{\"_type\":\"TestItemQuery\",\"lookup\":{\"_type\":\"Bogus\",\"id\":1}}]"));
        assertRejected (responses.get (0), "query", 3, "lookup");
    }

    /************************************************************************
     * Argument count (JUI-6).
     ************************************************************************/

    /**
     * Too few or too many arguments are rejected with a validation error and
     * the other calls in the batch are still processed.
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

        // Data that isn't an array.
        responses = process (call ("paged", 6, "{\"page\":1}"));
        assertRejected (responses.get (0), "paged", 6, null);
    }

    /**
     * An explicit null has the right number of arguments and reaches the
     * method; as does a method without parameters passed no data.
     */
    @Test
    public void explicitNullArgument() throws Exception {
        JsonNode responses = process (call ("paged", 8, "[null,null]"));
        Assertions.assertEquals ("page:null:null", result (responses.get (0)).asText ());

        responses = process ("[{\"action\":\"TestService\",\"method\":\"none\",\"type\":\"rpc\",\"tid\":1,\"data\":null}]");
        Assertions.assertEquals ("none", result (responses.get (0)).asText ());

        responses = process ("[{\"action\":\"TestService\",\"method\":\"none\",\"type\":\"rpc\",\"tid\":1}]");
        Assertions.assertEquals ("none", result (responses.get (0)).asText ());
    }

    /**
     * A call to an unknown method is not a rejection (it is answered as before).
     */
    @Test
    public void unknownMethod() throws Exception {
        JsonNode responses = process (call ("bogus", 2, "[1,2,3]"));
        Assertions.assertEquals ("exception", responses.get (0).get ("type").asText (), responses.toString ());
        Assertions.assertEquals (2, responses.get (0).get ("tid").asInt ());
    }

    /************************************************************************
     * Bodies that can't be read (JUI-5).
     ************************************************************************/

    /**
     * A body that isn't a call or array of calls answers a 400.
     */
    @Test
    public void invalidBody() throws Exception {
        for (String body : new String[] { "not json", "", "[1,2]", "[]", "1", "\"x\"", "[" + call ("echo", 1, "[\"a\"]") + ",1]" }) {
            MockHttpServletResponse response = handle (jsonRequest (body));
            Assertions.assertEquals (400, response.getStatus (), body);
            Assertions.assertEquals ("", response.getContentAsString (), body);
        }
    }

    /************************************************************************
     * CSRF.
     ************************************************************************/

    /**
     * A call that fails CSRF validation is denied, whether or not its arguments
     * can be read; one that passes is answered as normal (including being
     * rejected).
     */
    @Test
    public void csrf() throws Exception {
        router = new TestRouter ();
        router.setUseCsrfTokens (true);
        router.setCsrfEncoder (new ICRFHandler () {

            @Override
            public Optional<String> generate(HttpServletRequest request, HttpServletResponse response) {
                return Optional.of ("good");
            }

            @Override
            public Optional<String> token(HttpServletRequest request) {
                return Optional.of ("good");
            }

            @Override
            public boolean validate(String passedToken, String token) {
                return token.equals (passedToken);
            }
        });

        JsonNode responses = process ("[" + call ("paged", 1, "[{\"page\":\"abc\"},\"x\"]", "bad") + "," + call ("echo", 2, "[\"a\"]", "bad") + "]");
        Assertions.assertEquals ("denied", responses.get (0).get ("type").asText (), responses.toString ());
        Assertions.assertEquals (1, responses.get (0).get ("tid").asInt ());
        Assertions.assertEquals ("denied", responses.get (1).get ("type").asText (), responses.toString ());
        Assertions.assertEquals (2, responses.get (1).get ("tid").asInt ());

        responses = process ("[" + call ("paged", 1, "[{\"page\":\"abc\"},\"x\"]", "good") + "," + call ("echo", 2, "[\"a\"]", "good") + "]");
        assertRejected (responses.get (0), "paged", 1, "page");
        Assertions.assertEquals ("echo:a", result (responses.get (1)).asText ());
    }

    /************************************************************************
     * Multipart requests.
     ************************************************************************/

    /**
     * Multipart calls are read the same way (and a rejected one isn't bound to
     * the uploaded files).
     */
    @Test
    public void multipart() throws Exception {
        JsonNode responses = parse (handle (multipartRequest ("paged", 5, "{\"page\":2},\"x\"")));
        Assertions.assertEquals (5, responses.get (0).get ("tid").asInt ());
        Assertions.assertEquals ("page:2:x", result (responses.get (0)).asText ());

        responses = parse (handle (multipartRequest ("paged", 6, "{\"page\":\"abc\"},\"x\"")));
        assertRejected (responses.get (0), "paged", 6, "page");

        responses = parse (handle (multipartRequest ("paged", 7, "{\"page\":2}")));
        assertRejected (responses.get (0), "paged", 7, null);
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    private JsonNode process(String body) throws Exception {
        return parse (handle (jsonRequest (body)));
    }

    private JsonNode parse(MockHttpServletResponse response) throws Exception {
        Assertions.assertEquals (200, response.getStatus ());
        String content = response.getContentAsString ();
        JsonNode node = MAPPER.readTree (content);
        Assertions.assertTrue (node.isArray (), content);
        return node;
    }

    /**
     * Handles the request as a host application would (rendering the returned
     * view to the response).
     */
    private MockHttpServletResponse handle(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse ();
        ModelAndView mv = router.handleRequest (request, response);
        mv.getView ().render (null, request, response);
        return response;
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

    private static String call(String method, int tid, String data, String csrfToken) {
        return "{\"action\":\"TestService\",\"method\":\"" + method + "\",\"type\":\"rpc\",\"tid\":" + tid + ",\"csrfToken\":\"" + csrfToken + "\",\"data\":" + data + "}";
    }

    private static MockHttpServletRequest jsonRequest(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest ("POST", "/rpc.json");
        request.setContentType ("application/json");
        request.setContent (body.getBytes (StandardCharsets.UTF_8));
        return request;
    }

    private static MockMultipartHttpServletRequest multipartRequest(String method, int tid, String data) {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest ();
        request.setRequestURI ("/rpc.json");
        request.addParameter (Router.FIELD_ACTION, "TestService");
        request.addParameter (Router.FIELD_METHOD, method);
        request.addParameter (Router.FIELD_TYPE, "rpc");
        request.addParameter (Router.FIELD_TID, Integer.toString (tid));
        request.addParameter (Router.FIELD_JSON_DATA, data);
        return request;
    }
}
