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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.effacy.jui.rpc.extdirect.annotation.RemoteAction;
import com.effacy.jui.rpc.extdirect.annotation.RemoteMethod;
import com.effacy.jui.rpc.extdirect.metadata.MethodMetadata.TransactionMode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the response to a call whose transaction fails to commit (JUI-34): the
 * call is answered with an error (not the response that was built prior to the
 * commit) and the exception from the commit is logged, offered for retry and
 * passed to the error handler.
 */
public class RouterCommitFailureTest {

    /**
     * Transaction manager whose commits can be made to fail. A transaction is
     * held against the thread so that it may be joined (and marked as rollback
     * only by a participant).
     */
    public static class TestTransactionManager extends AbstractPlatformTransactionManager {

        public static class Tx implements SmartTransactionObject {

            public boolean rollbackOnly;

            @Override
            public boolean isRollbackOnly() {
                return rollbackOnly;
            }

            @Override
            public void flush() {
                // Nothing.
            }
        }

        /**
         * The exceptions to fail the next commits with (in order).
         */
        public List<RuntimeException> commitFailures = new ArrayList<> ();

        public int commits;

        public int rollbacks;

        private ThreadLocal<Tx> current = new ThreadLocal<> ();

        @Override
        protected Object doGetTransaction() {
            Tx tx = current.get ();
            return (tx == null) ? new Tx () : tx;
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) {
            return (transaction == current.get ());
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            current.set ((Tx) transaction);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            if (!commitFailures.isEmpty ())
                throw commitFailures.remove (0);
            commits++;
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbacks++;
        }

        @Override
        protected void doSetRollbackOnly(DefaultTransactionStatus status) {
            ((Tx) status.getTransaction ()).rollbackOnly = true;
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            current.remove ();
        }
    }

    /**
     * Records the errors it is passed and handles each by responding with the
     * name of its class.
     */
    public static class RecordingErrorHandler implements IActionErrorHandler {

        public static List<Throwable> retryErrors = new ArrayList<> ();

        public static List<Throwable> handledErrors = new ArrayList<> ();

        @Override
        public boolean retryError(Throwable e) {
            retryErrors.add (e);
            return (e instanceof CannotAcquireLockException);
        }

        @Override
        public Object handleError(RemoteCallRequest call, Throwable e) {
            handledErrors.add (e);
            return "handled:" + e.getClass ().getSimpleName ();
        }
    }

    @RemoteAction(name = "TestService", errorHandler = RecordingErrorHandler.class)
    public static class TestService {

        public PlatformTransactionManager transactionManager;

        public int calls;

        @RemoteMethod(transaction = TransactionMode.READ_WRITE)
        public String save() {
            calls++;
            return "saved:" + calls;
        }

        /**
         * As would a service that joins the transaction and fails (with that
         * failure not making it out of the method).
         */
        @RemoteMethod(transaction = TransactionMode.READ_WRITE)
        public String saveMarkingRollback() {
            calls++;
            new TransactionTemplate (transactionManager).executeWithoutResult (status -> status.setRollbackOnly ());
            return "saved:" + calls;
        }

        /**
         * Responds with an error while expecting the transaction to commit.
         */
        @RemoteMethod(transaction = TransactionMode.READ_WRITE)
        public String savePassingThrough() throws PassThroughException {
            calls++;
            throw new PassThroughException (new IllegalStateException ("Passed through"));
        }

        @RemoteMethod
        public String saveNoTransaction() {
            calls++;
            return "saved:" + calls;
        }
    }

    /**
     * No error handler (so errors are answered by the router).
     */
    @RemoteAction(name = "UnhandledService")
    public static class UnhandledService {

        @RemoteMethod(transaction = TransactionMode.READ_WRITE)
        public String save() {
            return "saved";
        }
    }

    /**
     * Router that records what was logged as an error.
     */
    public static class TestRouter extends ControllerRouter {

        public TestTransactionManager transactionManager = new TestTransactionManager ();

        public TestService service = new TestService ();

        public List<Throwable> errors = new ArrayList<> ();

        public TestRouter() {
            setUseCsrfTokens (false);
            setTransactionManager (transactionManager);
            setEnforceTransactions (true);
            setRetryDelay (0);
            scanPackages ();
            service.transactionManager = transactionManager;
            addAction (service);
            addAction (new UnhandledService ());
        }

        @Override
        protected void logError(String message, Throwable e) {
            errors.add (e);
        }

        @Override
        protected void logInfo(String message) {
            // The retries are expected.
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper ();

    private TestRouter router = new TestRouter ();

    @BeforeEach
    public void reset() {
        RecordingErrorHandler.retryErrors.clear ();
        RecordingErrorHandler.handledErrors.clear ();
    }

    /**
     * A call whose transaction commits is answered with the response of the
     * method.
     */
    @Test
    public void commitSucceeds() throws Exception {
        JsonNode response = process ("TestService", "save");

        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals ("saved:1", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (1, router.transactionManager.commits);
        Assertions.assertTrue (router.errors.isEmpty (), router.errors.toString ());
        Assertions.assertTrue (RecordingErrorHandler.handledErrors.isEmpty ());
    }

    /**
     * A call whose transaction fails to commit is answered as an error (by the
     * error handler, which is passed the exception from the commit) and that
     * exception is logged.
     */
    @Test
    public void commitFailureIsAnError() throws Exception {
        OptimisticLockingFailureException failure = new OptimisticLockingFailureException ("Stale");
        router.transactionManager.commitFailures.add (failure);

        JsonNode response = process ("TestService", "save");

        Assertions.assertEquals ("rpc", response.get ("type").asText (), response.toString ());
        Assertions.assertEquals ("handled:OptimisticLockingFailureException", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (1, router.service.calls);
        Assertions.assertEquals (0, router.transactionManager.commits);
        Assertions.assertEquals (List.of (failure), RecordingErrorHandler.handledErrors);
        Assertions.assertEquals (List.of (failure), router.errors);
    }

    /**
     * Where the action does not handle the error the router answers with an
     * exception.
     */
    @Test
    public void commitFailureNotHandledIsAnException() throws Exception {
        router.transactionManager.commitFailures.add (new OptimisticLockingFailureException ("Stale"));

        JsonNode response = process ("UnhandledService", "save");

        Assertions.assertEquals ("exception", response.get ("type").asText (), response.toString ());
        Assertions.assertTrue (response.get ("result").isNull (), response.toString ());
        Assertions.assertEquals (0, router.transactionManager.commits);
    }

    /**
     * A transaction marked as rollback only (by a participant) fails to commit
     * so the call is answered as an error.
     */
    @Test
    public void rollbackOnlyIsAnError() throws Exception {
        JsonNode response = process ("TestService", "saveMarkingRollback");

        Assertions.assertEquals ("handled:UnexpectedRollbackException", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (0, router.transactionManager.commits);
        Assertions.assertEquals (1, router.transactionManager.rollbacks);
        Assertions.assertEquals (1, RecordingErrorHandler.handledErrors.size ());
        Assertions.assertInstanceOf (UnexpectedRollbackException.class, RecordingErrorHandler.handledErrors.get (0));
        Assertions.assertEquals (RecordingErrorHandler.handledErrors, router.errors);
    }

    /**
     * The exception from the commit is offered for retry and, when it is to be
     * retried, the call is processed again.
     */
    @Test
    public void commitFailureIsRetried() throws Exception {
        CannotAcquireLockException failure = new CannotAcquireLockException ("Deadlock");
        router.setRetryCount (2);
        router.transactionManager.commitFailures.add (failure);

        JsonNode response = process ("TestService", "save");

        Assertions.assertEquals ("saved:2", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (2, router.service.calls);
        Assertions.assertEquals (1, router.transactionManager.commits);
        Assertions.assertEquals (List.of (failure), RecordingErrorHandler.retryErrors);
        Assertions.assertTrue (RecordingErrorHandler.handledErrors.isEmpty ());
        Assertions.assertTrue (router.errors.isEmpty (), router.errors.toString ());
    }

    /**
     * Once the retries are used up the (last) exception from the commit is the
     * error of the call.
     */
    @Test
    public void commitFailureRetriesUsedUp() throws Exception {
        router.setRetryCount (1);
        router.transactionManager.commitFailures.add (new CannotAcquireLockException ("Deadlock 1"));
        CannotAcquireLockException last = new CannotAcquireLockException ("Deadlock 2");
        router.transactionManager.commitFailures.add (last);

        JsonNode response = process ("TestService", "save");

        Assertions.assertEquals ("handled:CannotAcquireLockException", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (2, router.service.calls);
        Assertions.assertEquals (0, router.transactionManager.commits);
        Assertions.assertEquals (List.of (last), RecordingErrorHandler.handledErrors);
    }

    /**
     * An exception from the commit that is not to be retried is not.
     */
    @Test
    public void commitFailureNotRetried() throws Exception {
        OptimisticLockingFailureException failure = new OptimisticLockingFailureException ("Stale");
        router.setRetryCount (2);
        router.transactionManager.commitFailures.add (failure);

        JsonNode response = process ("TestService", "save");

        Assertions.assertEquals ("handled:OptimisticLockingFailureException", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (1, router.service.calls);
        Assertions.assertEquals (List.of (failure), RecordingErrorHandler.retryErrors);
    }

    /**
     * An error passed through (see {@link PassThroughException}) is the error
     * of the call when the transaction commits, otherwise the exception from
     * the commit is (the changes that were to be kept having been rolled back).
     */
    @Test
    public void passThroughWithCommitFailure() throws Exception {
        JsonNode response = process ("TestService", "savePassingThrough");

        Assertions.assertEquals ("handled:IllegalStateException", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (1, router.transactionManager.commits);

        OptimisticLockingFailureException failure = new OptimisticLockingFailureException ("Stale");
        router.transactionManager.commitFailures.add (failure);
        RecordingErrorHandler.handledErrors.clear ();

        response = process ("TestService", "savePassingThrough");

        Assertions.assertEquals ("handled:OptimisticLockingFailureException", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (1, router.transactionManager.commits);
        Assertions.assertEquals (List.of (failure), RecordingErrorHandler.handledErrors);
    }

    /**
     * A method that is not transactional has nothing to commit.
     */
    @Test
    public void notTransactional() throws Exception {
        router.transactionManager.commitFailures.add (new OptimisticLockingFailureException ("Stale"));

        JsonNode response = process ("TestService", "saveNoTransaction");

        Assertions.assertEquals ("saved:1", response.get ("result").asText (), response.toString ());
        Assertions.assertEquals (0, router.transactionManager.commits);
        Assertions.assertEquals (1, router.transactionManager.commitFailures.size ());
    }

    /************************************************************************
     * Support.
     ************************************************************************/

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
        return node.get (0);
    }
}
