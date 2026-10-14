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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.rpc.client.ErrorMessage;
import com.effacy.jui.rpc.extdirect.RemoteCallRequest;
import com.effacy.jui.rpc.extdirect.RemoteCallType;
import com.effacy.jui.rpc.extdirect.RouterRequestTest;
import com.effacy.jui.rpc.extdirect.annotation.AnnotatedActionHandler;
import com.effacy.jui.rpc.extdirect.dto.TestItemQuery;
import com.effacy.jui.rpc.extdirect.metadata.IRouterMetadata;

/**
 * Tests {@link JsonParser#jsonToRemoteCallRequests(String)} in isolation (see
 * {@link RouterRequestTest} for how the router answers what is read).
 */
public class JsonParserTest {

    /**
     * Metadata for the test service.
     */
    private static final IRouterMetadata METADATA = () -> List.of (new AnnotatedActionHandler (new RouterRequestTest.TestService ()));

    private JsonParser parser = new JsonParser (METADATA, "com.effacy.jui.rpc.extdirect.dto");

    /**
     * The call fields are read (JUI-4: the tid from "tid").
     */
    @Test
    public void callFields() throws Exception {
        RemoteCallRequest request = single ("{\"action\":\"TestService\",\"method\":\"echo\",\"type\":\"rpc\",\"tid\":42,\"csrfToken\":\"tok\",\"data\":[\"a\"]}");
        Assertions.assertEquals ("TestService", request.getAction ());
        Assertions.assertEquals ("echo", request.getMethod ());
        Assertions.assertEquals (RemoteCallType.rpc, request.getType ());
        Assertions.assertEquals (42, request.getTid ());
        Assertions.assertEquals ("tok", request.getCsrfToken ());
        Assertions.assertEquals (List.of ("a"), request.getData ());
        Assertions.assertFalse (request.isRejected ());

        // No tid defaults to 0.
        request = single ("{\"action\":\"TestService\",\"method\":\"echo\",\"type\":\"rpc\",\"data\":[\"a\"]}");
        Assertions.assertEquals (0, request.getTid ());
    }

    /**
     * Calls in a batch are read in order and each carries its own tid.
     */
    @Test
    public void batch() throws Exception {
        List<RemoteCallRequest> requests = parser.jsonToRemoteCallRequests ("[" + call ("echo", 1, "[\"a\"]") + "," + call ("echo", 2, "[\"b\"]") + "," + call ("echo", 3, "[\"c\"]") + "]");
        Assertions.assertEquals (3, requests.size ());
        for (int i = 0; i < 3; i++) {
            Assertions.assertEquals (i + 1, requests.get (i).getTid ());
            Assertions.assertEquals (List.of (String.valueOf ((char) ('a' + i))), requests.get (i).getData ());
        }
    }

    /**
     * Arguments are read as their parameter types (including polymorphic ones).
     */
    @Test
    public void argumentTypes() throws Exception {
        RemoteCallRequest request = single (call ("query", 1, "[{\"_type\":\"TestItemQuery\",\"page\":3}]"));
        Assertions.assertFalse (request.isRejected ());
        Assertions.assertInstanceOf (TestItemQuery.class, request.getData ().get (0));
        Assertions.assertEquals (3, ((TestItemQuery) request.getData ().get (0)).page);
    }

    /**
     * The argument count must match the method (JUI-6) and the message says
     * by how much.
     */
    @Test
    public void argumentCount() throws Exception {
        assertRejection (single (call ("paged", 1, "[{\"page\":1}]")), "", "TestService.paged takes 2 arguments but 1 was passed.");
        assertRejection (single (call ("paged", 1, "[]")), "", "TestService.paged takes 2 arguments but 0 were passed.");
        assertRejection (single (call ("paged", 1, "null")), "", "TestService.paged takes 2 arguments but 0 were passed.");
        assertRejection (single (call ("echo", 1, "[\"a\",\"b\"]")), "", "TestService.echo takes 1 argument but 2 were passed.");
        assertRejection (single (call ("echo", 1, "\"a\"")), "", "The arguments must be passed as an array.");

        // Explicit nulls count.
        RemoteCallRequest request = single (call ("paged", 1, "[null,null]"));
        Assertions.assertFalse (request.isRejected ());
        Assertions.assertEquals (2, request.getData ().size ());
    }

    /**
     * Each argument that can't be read is rejected with the path within it
     * (JUI-5).
     */
    @Test
    public void unreadableArguments() throws Exception {
        RemoteCallRequest request = single (call ("paged", 1, "[{\"page\":\"abc\",\"items\":[{\"id\":\"x\"}]},\"s\"]"));
        Assertions.assertTrue (request.isRejected ());
        Assertions.assertEquals (1, request.getRejections ().size ());
        Assertions.assertEquals ("page", request.getRejections ().get (0).getPath ());

        // Both arguments.
        request = single (call ("paged", 1, "[{\"page\":\"abc\"},{\"x\":1}]"));
        Assertions.assertEquals (2, request.getRejections ().size ());
        Assertions.assertEquals ("page", request.getRejections ().get (0).getPath ());
        Assertions.assertTrue (request.getRejections ().get (1).isGeneral ());
        Assertions.assertEquals (2, request.getData ().size ());
    }

    /**
     * Data isn't read (or checked) for an unknown action or method; that is
     * left to the router to answer.
     */
    @Test
    public void unknownMethod() throws Exception {
        RemoteCallRequest request = single (call ("bogus", 1, "[1,2,3]"));
        Assertions.assertFalse (request.isRejected ());
        Assertions.assertTrue (request.getData ().isEmpty ());

        request = single ("{\"action\":\"Bogus\",\"method\":\"echo\",\"type\":\"rpc\",\"tid\":1,\"data\":[1,2,3]}");
        Assertions.assertFalse (request.isRejected ());
    }

    /**
     * A body that isn't a call or a non-empty array of calls fails as a whole.
     */
    @Test
    public void invalidBody() {
        for (String body : new String[] { "", "not json", "null", "1", "\"x\"", "[]", "[1,2]", "[null]", "[" + call ("echo", 1, "[\"a\"]") + ",[]]" })
            Assertions.assertThrows (JsonParserException.class, () -> parser.jsonToRemoteCallRequests (body), body);
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    private RemoteCallRequest single(String json) throws Exception {
        List<RemoteCallRequest> requests = parser.jsonToRemoteCallRequests (json);
        Assertions.assertEquals (1, requests.size ());
        return requests.get (0);
    }

    private void assertRejection(RemoteCallRequest request, String path, String message) {
        Assertions.assertTrue (request.isRejected (), request.toString ());
        Assertions.assertEquals (1, request.getRejections ().size ());
        ErrorMessage msg = request.getRejections ().get (0);
        Assertions.assertEquals (path, msg.getPath ());
        Assertions.assertEquals ("VALIDATION", msg.getType ());
        Assertions.assertEquals (message, msg.getMessage ());
    }

    private static String call(String method, int tid, String data) {
        return "{\"action\":\"TestService\",\"method\":\"" + method + "\",\"type\":\"rpc\",\"tid\":" + tid + ",\"data\":" + data + "}";
    }
}
