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
import java.util.ArrayList;
import java.util.List;

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
 * Tests the delay the router waits between retries (JUI-36): the base delay,
 * exponential backoff up to a maximum and jitter.
 */
public class RouterRetryTest {

    /**
     * Number of delays sampled where the delay is randomised.
     */
    private static final int SAMPLES = 2000;

    /**
     * Fails a given number of times (with an error that is retried) before
     * succeeding.
     */
    @RemoteAction(name = "TestService")
    public static class TestService {

        public int failures;

        public int calls;

        @RemoteMethod
        public String flaky() {
            calls++;
            if (calls <= failures)
                throw new IllegalStateException ("Failure " + calls);
            return "ok:" + calls;
        }
    }

    /**
     * Declares errors as not being subject to retry.
     */
    public static class NoRetryErrorHandler implements IActionErrorHandler {

        @Override
        public boolean retryError(Throwable e) {
            return false;
        }

        @Override
        public Object handleError(RemoteCallRequest call, Throwable e) {
            return null;
        }
    }

    @RemoteAction(name = "NoRetryService", errorHandler = NoRetryErrorHandler.class)
    public static class NoRetryService {

        public int calls;

        @RemoteMethod
        public String flaky() {
            calls++;
            throw new IllegalStateException ("Failure " + calls);
        }
    }

    /**
     * Router that records the delay waited before each retry and what was
     * logged.
     */
    public static class TestRouter extends ControllerRouter {

        public TestService service = new TestService ();

        public NoRetryService noRetryService = new NoRetryService ();

        public List<Long> delays = new ArrayList<> ();

        public List<String> info = new ArrayList<> ();

        public TestRouter() {
            setUseCsrfTokens (false);
            scanPackages ();
            addAction (service);
            addAction (noRetryService);
        }

        @Override
        protected long retryDelay(int attempt) {
            long delay = super.retryDelay (attempt);
            delays.add (delay);
            return delay;
        }

        @Override
        protected void logInfo(String message) {
            info.add (message);
        }

        @Override
        protected void logError(String message, Throwable e) {
            // The failures are expected.
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    private TestRouter router = new TestRouter ();

    /************************************************************************
     * The delay for each attempt.
     ************************************************************************/

    /**
     * Without a maximum delay every retry waits the base delay (as was the
     * case prior to there being a backoff).
     */
    @Test
    public void fixedDelayByDefault() {
        Assertions.assertEquals (100, router.retryDelay (1));
        Assertions.assertEquals (100, router.retryDelay (50));

        router.setRetryDelay (150);
        for (int attempt = 1; attempt <= 10; attempt++)
            Assertions.assertEquals (150, router.retryDelay (attempt), "attempt " + attempt);
    }

    /**
     * With a maximum delay the delay doubles with each attempt from the base
     * delay and is capped at the maximum.
     */
    @Test
    public void backoffDoublesToMaximum() {
        router.setRetryDelay (100);
        router.setRetryMaxDelay (1000);
        long [] expected = { 100, 200, 400, 800, 1000, 1000, 1000 };
        for (int attempt = 1; attempt <= expected.length; attempt++)
            Assertions.assertEquals (expected[attempt - 1], router.retryDelay (attempt), "attempt " + attempt);

        // No overflow for an attempt well past where the doubling would.
        Assertions.assertEquals (1000, router.retryDelay (500));
        router.setRetryDelay (Integer.MAX_VALUE - 1);
        router.setRetryMaxDelay (Integer.MAX_VALUE);
        Assertions.assertEquals (Integer.MAX_VALUE - 1, router.retryDelay (1));
        Assertions.assertEquals (Integer.MAX_VALUE, router.retryDelay (2));
        Assertions.assertEquals (Integer.MAX_VALUE, router.retryDelay (500));
    }

    /**
     * A maximum below the base delay caps the delay from the first attempt and
     * one of zero (or less) turns the backoff off.
     */
    @Test
    public void backoffMaximumEdges() {
        router.setRetryDelay (200);
        router.setRetryMaxDelay (50);
        Assertions.assertEquals (50, router.retryDelay (1));
        Assertions.assertEquals (50, router.retryDelay (3));

        router.setRetryMaxDelay (-1);
        Assertions.assertEquals (200, router.retryDelay (3));

        // No base delay means no delay (there being nothing to double).
        router.setRetryDelay (0);
        router.setRetryMaxDelay (1000);
        router.setRetryJitter (1);
        Assertions.assertEquals (0, router.retryDelay (1));
        Assertions.assertEquals (0, router.retryDelay (5));
    }

    /**
     * With full jitter each attempt waits a random delay between zero and the
     * backed-off delay (which grows to, and is capped at, the maximum).
     */
    @Test
    public void fullJitterBounds() {
        router.setRetryDelay (100);
        router.setRetryMaxDelay (1000);
        router.setRetryJitter (1);
        long [] ceiling = { 100, 200, 400, 800, 1000, 1000, 1000 };
        for (int attempt = 1; attempt <= ceiling.length; attempt++)
            assertDelayRange (attempt, 0, ceiling[attempt - 1]);
    }

    /**
     * With partial jitter only that proportion of the backed-off delay is
     * randomised (so at a half, at least half the delay is waited).
     */
    @Test
    public void partialJitterBounds() {
        router.setRetryDelay (100);
        router.setRetryMaxDelay (1000);
        router.setRetryJitter (0.5);
        long [] ceiling = { 100, 200, 400, 800, 1000, 1000 };
        for (int attempt = 1; attempt <= ceiling.length; attempt++)
            assertDelayRange (attempt, ceiling[attempt - 1] / 2, ceiling[attempt - 1]);
    }

    /**
     * Jitter applies to a fixed delay too (no maximum) and is brought into the
     * range 0 to 1.
     */
    @Test
    public void jitterWithoutBackoff() {
        router.setRetryDelay (100);
        router.setRetryJitter (7);
        for (int attempt = 1; attempt <= 4; attempt++)
            assertDelayRange (attempt, 0, 100);

        router.setRetryJitter (-1);
        for (int attempt = 1; attempt <= 4; attempt++)
            Assertions.assertEquals (100, router.retryDelay (attempt), "attempt " + attempt);
    }

    /************************************************************************
     * Retrying requests.
     ************************************************************************/

    /**
     * A failing request is retried after the delay for each attempt and each
     * retry is logged with the delay that was waited.
     */
    @Test
    public void retriesWaitTheDelayForEachAttempt() throws Exception {
        router.setRetryCount (4);
        router.setRetryDelay (2);
        router.setRetryMaxDelay (5);
        router.setRetryJitter (1);
        router.service.failures = 3;

        long start = System.nanoTime ();
        JsonNode response = process ("TestService");
        long elapsed = (System.nanoTime () - start) / 1000000;

        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals ("ok:4", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (4, router.service.calls);

        // One delay per retry, within the bounds for the attempt.
        long [] ceiling = { 2, 4, 5 };
        Assertions.assertEquals (ceiling.length, router.delays.size (), router.delays.toString ());
        long total = 0;
        for (int i = 0; i < ceiling.length; i++) {
            long delay = router.delays.get (i);
            Assertions.assertTrue ((delay >= 0) && (delay <= ceiling[i]), "attempt " + (i + 1) + " waited " + delay);
            total += delay;
        }
        Assertions.assertTrue (elapsed >= total, "waited " + elapsed + " of " + total);

        // The log reports the retries remaining and the delay actually used.
        Assertions.assertEquals (ceiling.length, router.info.size (), router.info.toString ());
        for (int i = 0; i < ceiling.length; i++) {
            String expected = "Retry,count=" + (4 - i) + ",delay=" + router.delays.get (i) + ",cause=IllegalStateException,";
            Assertions.assertTrue (router.info.get (i).startsWith (expected), router.info.get (i));
        }
    }

    /**
     * Once the retries are used up the error is returned without a further
     * wait.
     */
    @Test
    public void noDelayAfterTheLastAttempt() throws Exception {
        router.setRetryCount (2);
        router.setRetryDelay (1);
        router.service.failures = Integer.MAX_VALUE;

        JsonNode response = process ("TestService");

        Assertions.assertEquals ("exception", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals (3, router.service.calls);
        Assertions.assertEquals (List.of (1L, 1L), router.delays);
        Assertions.assertEquals (2, router.info.size (), router.info.toString ());
    }

    /**
     * With no retries (the default) a failure is returned as it is.
     */
    @Test
    public void noRetriesByDefault() throws Exception {
        router.service.failures = Integer.MAX_VALUE;

        JsonNode response = process ("TestService");

        Assertions.assertEquals ("exception", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals (1, router.service.calls);
        Assertions.assertTrue (router.delays.isEmpty (), router.delays.toString ());
        Assertions.assertTrue (router.info.isEmpty (), router.info.toString ());
    }

    /**
     * An error that is not subject to retry is not retried (nor waited on).
     */
    @Test
    public void nonRetryableErrorIsNotRetried() throws Exception {
        router.setRetryCount (3);
        router.setRetryDelay (1);

        JsonNode response = process ("NoRetryService");

        Assertions.assertEquals ("exception", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals (1, router.noRetryService.calls);
        Assertions.assertTrue (router.delays.isEmpty (), router.delays.toString ());
        Assertions.assertTrue (router.info.isEmpty (), router.info.toString ());
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    /**
     * Asserts that the delay for the attempt always lies in the given range
     * (inclusive) and that it is spread across that range.
     */
    private void assertDelayRange(int attempt, long min, long max) {
        long lowest = Long.MAX_VALUE;
        long highest = Long.MIN_VALUE;
        for (int i = 0; i < SAMPLES; i++) {
            long delay = router.retryDelay (attempt);
            Assertions.assertTrue ((delay >= min) && (delay <= max), "attempt " + attempt + " gave " + delay + " outside of " + min + " to " + max);
            lowest = Math.min (lowest, delay);
            highest = Math.max (highest, delay);
        }
        long quarter = (max - min) / 4;
        Assertions.assertTrue (lowest <= (min + quarter), "attempt " + attempt + " never below " + lowest);
        Assertions.assertTrue (highest >= (max - quarter), "attempt " + attempt + " never above " + highest);
    }

    /**
     * Calls the {@code flaky} method on the given action (as a host application
     * would) and returns the response to the call.
     */
    private JsonNode process(String action) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest ("POST", "/rpc.json");
        request.setContentType ("application/json");
        request.setContent (("{\"action\":\"" + action + "\",\"method\":\"flaky\",\"type\":\"rpc\",\"tid\":1,\"data\":[]}").getBytes (StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse ();
        ModelAndView mv = router.handleRequest (request, response);
        mv.getView ().render (null, request, response);
        Assertions.assertEquals (200, response.getStatus ());
        JsonNode node = MAPPER.readTree (response.getContentAsString ());
        Assertions.assertEquals (1, node.size (), node.toString ());
        return node.get (0);
    }
}
