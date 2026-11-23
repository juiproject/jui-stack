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
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.effacy.jui.rpc.extdirect.annotation.RemoteAction;
import com.effacy.jui.rpc.extdirect.annotation.RemoteMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests which error handler a remote method's errors are passed to: the one
 * declared on the method (see {@link RemoteMethod#errorHandler()}), else the
 * one declared on its action (see {@link RemoteAction#errorHandler()}), else
 * none. The handler decides both the response to the error and whether the call
 * is retried.
 * <p>
 * The actions declare several methods either side (by name) of the one with a
 * handler of its own, as the order in which methods are registered is not
 * defined.
 */
public class RouterErrorHandlerTest {

    /**
     * Handles an error by responding with its own name (and does not retry).
     */
    public static abstract class NamedErrorHandler implements IActionErrorHandler {

        @Override
        public boolean retryError(Throwable e) {
            return false;
        }

        @Override
        public Object handleError(RemoteCallRequest call, Throwable e) {
            return "handled:" + getClass ().getSimpleName ();
        }
    }

    public static class MethodErrorHandler extends NamedErrorHandler {}

    public static class ActionErrorHandler extends NamedErrorHandler {}

    /**
     * Can't be created (so can't be used).
     */
    public static class BrokenErrorHandler extends NamedErrorHandler {

        public BrokenErrorHandler() {
            throw new IllegalStateException ("Can't be created");
        }
    }

    /**
     * Base for the actions, counting the calls to each (always failing) method.
     */
    public static class FailingService {

        public Map<String, Integer> calls = new HashMap<> ();

        protected String fail(String method) {
            calls.merge (method, 1, Integer::sum);
            throw new IllegalStateException ("Failure of " + method);
        }
    }

    /**
     * No handler on the action and one on a single method.
     */
    @RemoteAction(name = "MethodOnly")
    public static class MethodOnlyService extends FailingService {

        @RemoteMethod
        public String alpha() {
            return fail ("alpha");
        }

        @RemoteMethod
        public String beta() {
            return fail ("beta");
        }

        @RemoteMethod(errorHandler = MethodErrorHandler.class)
        public String handled() {
            return fail ("handled");
        }

        @RemoteMethod
        public String omega() {
            return fail ("omega");
        }

        @RemoteMethod
        public String zeta() {
            return fail ("zeta");
        }
    }

    /**
     * A handler on the action, another on a single method and one method whose
     * handler can't be created.
     */
    @RemoteAction(name = "ActionAndMethod", errorHandler = ActionErrorHandler.class)
    public static class ActionAndMethodService extends FailingService {

        @RemoteMethod
        public String alpha() {
            return fail ("alpha");
        }

        @RemoteMethod(errorHandler = BrokenErrorHandler.class)
        public String broken() {
            return fail ("broken");
        }

        @RemoteMethod(errorHandler = MethodErrorHandler.class)
        public String handled() {
            return fail ("handled");
        }

        @RemoteMethod
        public String omega() {
            return fail ("omega");
        }

        @RemoteMethod
        public String zeta() {
            return fail ("zeta");
        }
    }

    /**
     * Router that retries once (without delay), so a retried call is one that
     * is called twice.
     */
    public static class TestRouter extends ControllerRouter {

        public MethodOnlyService methodOnly = new MethodOnlyService ();

        public ActionAndMethodService actionAndMethod = new ActionAndMethodService ();

        public TestRouter() {
            setUseCsrfTokens (false);
            setRetryCount (1);
            setRetryDelay (0);
            scanPackages ();
            addAction (methodOnly);
            addAction (actionAndMethod);
        }

        @Override
        protected void logInfo(String message) {
            // The retries are expected.
        }

        @Override
        protected void logError(String message, Throwable e) {
            // The failures are expected.
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    private TestRouter router = new TestRouter ();

    /**
     * A handler declared on a method handles that method's errors.
     */
    @Test
    public void methodHandlerAppliesToItsMethod() throws Exception {
        assertHandledBy ("MethodErrorHandler", process ("MethodOnly", "handled"));
        Assertions.assertEquals (1, router.methodOnly.calls.get ("handled"));
    }

    /**
     * A handler declared on a method does not handle the errors of the other
     * methods of the action (which, having no handler, are not handled and are
     * retried).
     */
    @Test
    public void methodHandlerDoesNotApplyToOtherMethods() throws Exception {
        for (String method : new String[] { "alpha", "beta", "omega", "zeta" }) {
            assertNotHandled (process ("MethodOnly", method));
            Assertions.assertEquals (2, router.methodOnly.calls.get (method), method);
        }
    }

    /**
     * A handler declared on a method takes precedence over that declared on
     * its action, which continues to handle the errors of the other methods.
     */
    @Test
    public void methodHandlerOverridesActionHandler() throws Exception {
        assertHandledBy ("MethodErrorHandler", process ("ActionAndMethod", "handled"));
        for (String method : new String[] { "alpha", "omega", "zeta" })
            assertHandledBy ("ActionErrorHandler", process ("ActionAndMethod", method));
    }

    /**
     * A method whose handler can't be created falls back to that declared on
     * its action.
     */
    @Test
    public void brokenMethodHandlerFallsBackToActionHandler() throws Exception {
        assertHandledBy ("ActionErrorHandler", process ("ActionAndMethod", "broken"));
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    private void assertHandledBy(String handler, JsonNode response) {
        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals ("handled:" + handler, response.get ("result").asText (), response.toString ());
    }

    private void assertNotHandled(JsonNode response) {
        Assertions.assertEquals ("exception", response.get ("type").asText (), response.toString ());
    }

    /**
     * Calls the method on the given action (as a host application would) and
     * returns the response to the call.
     */
    private JsonNode process(String action, String method) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest ("POST", "/rpc.json");
        request.setContentType ("application/json");
        request.setContent (("{\"action\":\"" + action + "\",\"method\":\"" + method + "\",\"type\":\"rpc\",\"tid\":1,\"data\":[]}").getBytes (StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse ();
        ModelAndView mv = router.handleRequest (request, response);
        mv.getView ().render (null, request, response);
        Assertions.assertEquals (200, response.getStatus ());
        JsonNode node = MAPPER.readTree (response.getContentAsString ());
        Assertions.assertEquals (1, node.size (), node.toString ());
        Assertions.assertEquals (method, node.get (0).get ("method").asText (), node.toString ());
        return node.get (0);
    }
}
