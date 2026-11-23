package com.effacy.jui.rpc.extdirect;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.effacy.jui.rpc.extdirect.annotation.RemoteAction;
import com.effacy.jui.rpc.extdirect.annotation.RemoteMethod;
import com.effacy.jui.rpc.handler.Executor;
import com.effacy.jui.rpc.handler.client.query.Query;
import com.effacy.jui.rpc.handler.command.CommandProcessorContext;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.query.QueryProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests that a query processor can have the router retry a call that fails
 * with an uncaught exception, by marking it for retry (JUI-37).
 * <p>
 * Models a lock wait timeout raised while a query is being processed (for
 * example, when a write deferred by an earlier command is flushed by the
 * query).
 */
public class RouterQueryRetryTest {

    /**
     * Stands in for a lock wait timeout (such as
     * {@code jakarta.persistence.LockTimeoutException}).
     */
    public static class LockTimeoutException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public LockTimeoutException(String message) {
            super (message);
        }
    }

    public static class ListQuery extends Query<String> {}

    public static class QueryContext {}

    public static class CommandContext extends CommandProcessorContext<CommandContext> {}

    /**
     * Fails with a lock wait timeout a given number of times before
     * succeeding. Uncaught exceptions are left to the default handling.
     */
    public static class ListQueryProcessor extends QueryProcessor<QueryContext, String, ListQuery> {

        public static int failures;

        public static int calls;

        public ListQueryProcessor() {
            super (ListQuery.class);
        }

        @Override
        protected String process(QueryContext context, ListQuery query) throws ProcessorException {
            calls++;
            if (calls <= failures)
                throw new LockTimeoutException ("Lock wait timeout " + calls);
            return "ok:" + calls;
        }

        @Override
        protected void log(Throwable e) {
            // The failures are expected.
        }
    }

    /**
     * As {@link ListQueryProcessor} but marks a lock wait timeout for retry.
     */
    public static class RetryingListQueryProcessor extends ListQueryProcessor {

        @Override
        protected void throwFor(Throwable e) throws ProcessorException {
            if (e instanceof LockTimeoutException)
                throw new ProcessorException (e).retry ();
            super.throwFor (e);
        }
    }

    public static class TestExecutor extends Executor<QueryContext, CommandContext> {

        public TestExecutor(QueryProcessor<QueryContext, String, ListQuery> processor) {
            add (processor);
        }

        public Object list() throws Exception {
            return query (new QueryContext (), new ListQuery ());
        }
    }

    /**
     * Retries an error that is a processor exception marked for retry (as an
     * application would) and records the errors it is asked about.
     */
    public static class RetryErrorHandler extends ActionErrorHandler {

        public static List<Throwable> retryErrors = new ArrayList<> ();

        @Override
        public boolean retryError(Throwable e) {
            retryErrors.add (e);
            return (e instanceof ProcessorException pe) && pe.isRetry ();
        }
    }

    @RemoteAction(name = "RetryingService", errorHandler = RetryErrorHandler.class)
    public static class RetryingService {

        private TestExecutor executor = new TestExecutor (new RetryingListQueryProcessor ());

        @RemoteMethod
        public Object list() throws Exception {
            return executor.list ();
        }
    }

    @RemoteAction(name = "DefaultService", errorHandler = RetryErrorHandler.class)
    public static class DefaultService {

        private TestExecutor executor = new TestExecutor (new ListQueryProcessor ());

        @RemoteMethod
        public Object list() throws Exception {
            return executor.list ();
        }
    }

    public static class TestRouter extends ControllerRouter {

        public TestRouter() {
            setUseCsrfTokens (false);
            setRetryDelay (0);
            scanPackages ();
            addAction (new RetryingService ());
            addAction (new DefaultService ());
        }

        @Override
        protected void logInfo(String message) {
            // Not needed.
        }

        @Override
        protected void logError(String message, Throwable e) {
            // The failures are expected.
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    private TestRouter router = new TestRouter ();

    @BeforeEach
    public void reset() {
        ListQueryProcessor.failures = 0;
        ListQueryProcessor.calls = 0;
        RetryErrorHandler.retryErrors.clear ();
    }

    /**
     * A lock wait timeout the processor marks for retry is retried by the
     * router, and the call succeeds once the lock is free.
     */
    @Test
    public void markedFailureIsRetried() throws Exception {
        router.setRetryCount (3);
        ListQueryProcessor.failures = 2;

        JsonNode response = process ("RetryingService");

        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals ("ok:3", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (3, ListQueryProcessor.calls);

        // The router was asked about each failure, and each kept its cause.
        Assertions.assertEquals (2, RetryErrorHandler.retryErrors.size (), RetryErrorHandler.retryErrors.toString ());
        for (Throwable error : RetryErrorHandler.retryErrors) {
            ProcessorException pe = Assertions.assertInstanceOf (ProcessorException.class, error);
            Assertions.assertTrue (pe.isRetry ());
            Assertions.assertInstanceOf (LockTimeoutException.class, pe.getCause ());
        }
    }

    /**
     * Once the retries are used up the error is returned.
     */
    @Test
    public void markedFailureGivesUpAfterRetries() throws Exception {
        router.setRetryCount (2);
        ListQueryProcessor.failures = Integer.MAX_VALUE;

        JsonNode response = process ("RetryingService");

        Assertions.assertEquals ("ERROR_SYSTEM", response.path ("result").path ("outcome").asText (), response.toString ());
        Assertions.assertEquals (3, ListQueryProcessor.calls);
    }

    /**
     * By default the failure is not marked for retry, so is not retried, but
     * the error still carries the lock wait timeout as its cause.
     */
    @Test
    public void unmarkedFailureIsNotRetried() throws Exception {
        router.setRetryCount (3);
        ListQueryProcessor.failures = 1;

        JsonNode response = process ("DefaultService");

        Assertions.assertEquals ("ERROR_SYSTEM", response.path ("result").path ("outcome").asText (), response.toString ());
        Assertions.assertEquals (1, ListQueryProcessor.calls);
        Assertions.assertEquals (1, RetryErrorHandler.retryErrors.size (), RetryErrorHandler.retryErrors.toString ());
        ProcessorException pe = Assertions.assertInstanceOf (ProcessorException.class, RetryErrorHandler.retryErrors.get (0));
        Assertions.assertFalse (pe.isRetry ());
        Assertions.assertInstanceOf (LockTimeoutException.class, pe.getCause ());
    }

    /**
     * Calls the {@code list} method on the given action (as a host application
     * would) and returns the response to the call.
     */
    private JsonNode process(String action) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest ("POST", "/rpc.json");
        request.setContentType ("application/json");
        request.setContent (("{\"action\":\"" + action + "\",\"method\":\"list\",\"type\":\"rpc\",\"tid\":1,\"data\":[]}").getBytes (StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse ();
        ModelAndView mv = router.handleRequest (request, response);
        mv.getView ().render (null, request, response);
        Assertions.assertEquals (200, response.getStatus ());
        JsonNode node = MAPPER.readTree (response.getContentAsString ());
        Assertions.assertEquals (1, node.size (), node.toString ());
        return node.get (0);
    }
}
