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
package com.effacy.jui.playground.it.remoting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.effacy.jui.playground.TestApplicationIT;
import com.effacy.jui.playground.it.AbstractIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests how the playground's RPC endpoint ({@code /app/rpc.json}) answers
 * requests, in particular those that can't be read (JUI-4, JUI-5 and JUI-6).
 * <p>
 * This goes through the full application wiring (the
 * {@link com.effacy.jui.playground.remoting.ApplicationRPC} controller, its
 * scanned packages and the {@link com.effacy.jui.playground.remoting.ApplicationService}
 * action) so complements the unit tests in jui-remoting.
 */
@SpringBootTest(classes = TestApplicationIT.class)
public class RpcRequestITTest extends AbstractIT {

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    /**
     * A lookup that the playground can process.
     */
    private static final String LOOKUP = "{\"_type\":\"ExampleLookup\"}";

    /**
     * A valid call is processed and its response carries the call's tid.
     */
    @Test
    public void validCall() throws Exception {
        JsonNode responses = rpc ("[" + query (11, LOOKUP + ",[]") + "]");
        Assertions.assertEquals (1, responses.size ());
        assertSuccess (responses.get (0), 11);
    }

    /**
     * Each response in a batch carries the tid of the call it answers (JUI-4).
     */
    @Test
    public void batchTids() throws Exception {
        JsonNode responses = rpc ("[" + query (3, LOOKUP + ",[]") + "," + query (7, LOOKUP + ",[]") + "]");
        Assertions.assertEquals (2, responses.size ());
        assertSuccess (responses.get (0), 3);
        assertSuccess (responses.get (1), 7);
    }

    /**
     * A field that can't be read (here an int out of range) is answered as a
     * validation error against the field, and the other calls in the batch are
     * still processed (JUI-5).
     */
    @Test
    public void unreadableField() throws Exception {
        for (String page : new String[] { "2147483648", "4294967296", "1e20", "\"abc\"" }) {
            JsonNode responses = rpc ("[" + query (1, LOOKUP + ",[]") + "," + query (2, "{\"_type\":\"PageQuery\",\"page\":" + page + ",\"pageSize\":50},[]") + "," + query (3, LOOKUP + ",[]") + "]");
            Assertions.assertEquals (3, responses.size (), page);
            assertSuccess (responses.get (0), 1);
            assertRejected (responses.get (1), 2, "page");
            assertSuccess (responses.get (2), 3);
        }
    }

    /**
     * A query whose type is unknown is answered as a validation error (JUI-5).
     */
    @Test
    public void unknownType() throws Exception {
        JsonNode responses = rpc ("[" + query (4, "{\"_type\":\"Bogus\"},[]") + "]");
        assertRejected (responses.get (0), 4, null);
    }

    /**
     * The wrong number of arguments is answered as a validation error that
     * says so (JUI-6).
     */
    @Test
    public void wrongArgumentCount() throws Exception {
        for (String data : new String[] { "", LOOKUP, LOOKUP + ",[],[]" }) {
            JsonNode responses = rpc ("[" + query (5, data) + "," + query (6, LOOKUP + ",[]") + "]");
            JsonNode messages = assertRejected (responses.get (0), 5, null);
            Assertions.assertTrue (messages.get (0).get ("message").asText ().startsWith ("ApplicationService.query takes 2 arguments"), messages.toString ());
            assertSuccess (responses.get (1), 6);
        }

        // Missing and null data.
        JsonNode responses = rpc ("[{\"action\":\"ApplicationService\",\"method\":\"query\",\"type\":\"rpc\",\"tid\":8}]");
        assertRejected (responses.get (0), 8, null);
        responses = rpc ("[{\"action\":\"ApplicationService\",\"method\":\"query\",\"type\":\"rpc\",\"tid\":9,\"data\":null}]");
        assertRejected (responses.get (0), 9, null);
    }

    /**
     * A body that isn't a call or array of calls answers a 400 (JUI-5).
     */
    @Test
    public void invalidBody() throws Exception {
        for (String body : new String[] { "not json", "[]", "[1,2]", "1" }) {
            MockHttpServletResponse response = post (body);
            Assertions.assertEquals (400, response.getStatus (), body);
        }
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    private MockHttpServletResponse post(String body) throws Exception {
        return mockMvc.perform (MockMvcRequestBuilders.post ("/app/rpc.json")
            .contentType (MediaType.APPLICATION_JSON)
            .content (body))
            .andReturn ()
            .getResponse ();
    }

    private JsonNode rpc(String body) throws Exception {
        MockHttpServletResponse response = post (body);
        Assertions.assertEquals (200, response.getStatus (), body);
        JsonNode node = MAPPER.readTree (response.getContentAsString ());
        Assertions.assertTrue (node.isArray (), response.getContentAsString ());
        return node;
    }

    private static String query(int tid, String data) {
        return "{\"action\":\"ApplicationService\",\"method\":\"query\",\"type\":\"rpc\",\"tid\":" + tid + ",\"data\":[" + data + "]}";
    }

    private JsonNode result(JsonNode response, int tid) {
        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals ("ApplicationService", response.get ("action").asText (), response.toString ());
        Assertions.assertEquals ("query", response.get ("method").asText (), response.toString ());
        Assertions.assertEquals (tid, response.get ("tid").asInt (), response.toString ());
        return response.get ("result");
    }

    private void assertSuccess(JsonNode response, int tid) {
        JsonNode result = result (response, tid);
        Assertions.assertEquals ("SUCCESS", result.get ("outcome").asText (), response.toString ());
        Assertions.assertEquals ("It worked!", result.get ("response").get ("value1").asText (), response.toString ());
    }

    /**
     * Asserts the response is a rejection and returns the error messages. A
     * {@code null} path expects a general message (one not against a field).
     */
    private JsonNode assertRejected(JsonNode response, int tid, String path) {
        JsonNode result = result (response, tid);
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
}
