package com.effacy.jui.rpc.handler.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.rpc.handler.client.query.Query;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException.ErrorType;

/**
 * Tests how {@link QueryProcessor} converts an exception thrown while
 * processing a query (JUI-37).
 */
public class QueryProcessorTest {

    /**
     * Stands in for an exception that reflects a temporary condition (such as a
     * lock wait timeout).
     */
    public static class TemporaryException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public TemporaryException(String message) {
            super (message);
        }
    }

    public static class TestQuery extends Query<String> {}

    /**
     * Throws whatever it is given to throw and records what is logged and what
     * is reported to {@link #onAfterProcess(Object, TestQuery, String, boolean)}.
     */
    public static class TestQueryProcessor extends QueryProcessor<Object, String, TestQuery> {

        public Throwable failure;

        public List<Throwable> logged = new ArrayList<> ();

        public List<Boolean> after = new ArrayList<> ();

        public TestQueryProcessor() {
            super (TestQuery.class);
        }

        @Override
        protected String process(Object context, TestQuery query) throws ProcessorException {
            if (failure instanceof ProcessorException pe)
                throw pe;
            if (failure instanceof RuntimeException re)
                throw re;
            if (failure instanceof Error err)
                throw err;
            return "ok";
        }

        @Override
        protected void onAfterProcess(Object context, TestQuery query, String result, boolean error) {
            after.add (error);
        }

        @Override
        protected void log(Throwable e) {
            logged.add (e);
        }
    }

    /**
     * Marks a {@link TemporaryException} for retry.
     */
    public static class RetryingQueryProcessor extends TestQueryProcessor {

        @Override
        protected void throwFor(Throwable e) throws ProcessorException {
            if (e instanceof TemporaryException)
                throw new ProcessorException (e).retry ();
            super.throwFor (e);
        }
    }

    /**
     * Overrides {@link #throwFor(Throwable)} without throwing.
     */
    public static class NonThrowingQueryProcessor extends TestQueryProcessor {

        @Override
        protected void throwFor(Throwable e) throws ProcessorException {
            // Does not throw.
        }
    }

    /**
     * A query processed without error returns its result.
     */
    @Test
    public void result() throws Exception {
        TestQueryProcessor processor = new TestQueryProcessor ();

        Assertions.assertEquals ("ok", processor.process (new Object (), (Object) new TestQuery ()));
        Assertions.assertEquals (List.of (false), processor.after);
        Assertions.assertTrue (processor.logged.isEmpty ());
    }

    /**
     * An uncaught exception is logged and thrown as a system error that keeps
     * the exception as its cause (and is not marked for retry).
     */
    @Test
    public void uncaughtExceptionKeepsCause() {
        TestQueryProcessor processor = new TestQueryProcessor ();
        TemporaryException failure = new TemporaryException ("Lock wait timeout");
        processor.failure = failure;

        ProcessorException ex = Assertions.assertThrows (ProcessorException.class, () -> processor.process (new Object (), (Object) new TestQuery ()));

        Assertions.assertSame (failure, ex.getCause ());
        Assertions.assertFalse (ex.isRetry ());
        Assertions.assertEquals (1, ex.errors ().size ());
        Assertions.assertEquals (ErrorType.SYSTEM, ex.errors ().get (0).getType ());
        Assertions.assertEquals (List.of (failure), processor.logged);
        Assertions.assertEquals (List.of (true), processor.after);
    }

    /**
     * An uncaught error (not just an exception) keeps its cause too.
     */
    @Test
    public void uncaughtErrorKeepsCause() {
        TestQueryProcessor processor = new TestQueryProcessor ();
        AssertionError failure = new AssertionError ("Broken");
        processor.failure = failure;

        ProcessorException ex = Assertions.assertThrows (ProcessorException.class, () -> processor.process (new Object (), (Object) new TestQuery ()));

        Assertions.assertSame (failure, ex.getCause ());
        Assertions.assertEquals (List.of (failure), processor.logged);
    }

    /**
     * A processor exception is thrown as it is (and not logged).
     */
    @Test
    public void processorExceptionPassesThrough() {
        TestQueryProcessor processor = new TestQueryProcessor ();
        ProcessorException failure = new ProcessorException ().add (ErrorType.NOT_FOUND, "Not found");
        processor.failure = failure;

        ProcessorException ex = Assertions.assertThrows (ProcessorException.class, () -> processor.process (new Object (), (Object) new TestQuery ()));

        Assertions.assertSame (failure, ex);
        Assertions.assertTrue (processor.logged.isEmpty ());
        Assertions.assertEquals (List.of (true), processor.after);
    }

    /**
     * A subclass can turn an uncaught exception into a processor exception
     * marked for retry, and leave others to the default.
     */
    @Test
    public void subclassMarksForRetry() {
        RetryingQueryProcessor processor = new RetryingQueryProcessor ();
        TemporaryException temporary = new TemporaryException ("Lock wait timeout");
        processor.failure = temporary;

        ProcessorException ex = Assertions.assertThrows (ProcessorException.class, () -> processor.process (new Object (), (Object) new TestQuery ()));

        Assertions.assertTrue (ex.isRetry ());
        Assertions.assertSame (temporary, ex.getCause ());
        Assertions.assertTrue (processor.logged.isEmpty ());

        IllegalStateException other = new IllegalStateException ("Other");
        processor.failure = other;

        ex = Assertions.assertThrows (ProcessorException.class, () -> processor.process (new Object (), (Object) new TestQuery ()));

        Assertions.assertFalse (ex.isRetry ());
        Assertions.assertSame (other, ex.getCause ());
        Assertions.assertEquals (List.of (other), processor.logged);
    }

    /**
     * Should an override not throw, a processor exception with the cause is
     * still thrown (rather than returning no result).
     */
    @Test
    public void nonThrowingOverrideStillThrows() {
        NonThrowingQueryProcessor processor = new NonThrowingQueryProcessor ();
        TemporaryException failure = new TemporaryException ("Lock wait timeout");
        processor.failure = failure;

        ProcessorException ex = Assertions.assertThrows (ProcessorException.class, () -> processor.process (new Object (), (Object) new TestQuery ()));

        Assertions.assertSame (failure, ex.getCause ());
    }
}
