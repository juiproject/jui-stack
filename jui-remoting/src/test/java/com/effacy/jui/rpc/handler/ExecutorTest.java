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
package com.effacy.jui.rpc.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.rpc.handler.client.command.C;
import com.effacy.jui.rpc.handler.client.command.V.VString;
import com.effacy.jui.rpc.handler.client.query.Query;
import com.effacy.jui.rpc.handler.command.CommandProcessor;
import com.effacy.jui.rpc.handler.command.CommandProcessorContext;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.query.QueryProcessor;

public class ExecutorTest {

    /**
     * Test the query mechanism
     */
    @Test
    public void testQuery() throws Exception {
        Assertions.assertEquals ("Query1Outcome:hubba", executor.query (new LocalQueryContext (), new Query1 ("hubba")));
        Assertions.assertEquals ("Query2Outcome:wibble", executor.query (new LocalQueryContext (), new Query2 ("wibble")));
        try {
            executor.query (new LocalQueryContext(), new Query3 ("wibble"));
            Assertions.fail ("Expected a NoProcessorException");
        } catch (NoProcessorException e) {
            // Expected path.
        }
        try {
            executor.query (new LocalQueryContext(), new Query4 ());
            Assertions.fail ("Expected a ProcessorException");
        } catch (ProcessorException e) {
            // Expected path.
        }
    }

    /**
     * Test the command mechanism.
     */
    @Test
    public void testExecute() throws Exception {
        Assertions.assertEquals ("", TARGET1.param1);
        Assertions.assertEquals ("", TARGET1.param2);
        executor.execute (new LocalCommandContext(), new Command1 ().param1 ("hubba"));
        Assertions.assertEquals ("hubba", TARGET1.param1);
        Assertions.assertEquals ("", TARGET1.param2);
        TARGET1.reset();

        try {
            executor.execute (new LocalCommandContext(), new Command1 ().error (true));
            Assertions.fail ("Expected a ProcessorException");
        } catch (ProcessorException e) {
            // Expected path.
        }

        try {
            executor.execute (new LocalCommandContext(), new Command2 ());
            Assertions.fail ("Expected a NoProcessorException");
        } catch (NoProcessorException e) {
            // Expected path.
        }
    }

    /************************************************************************
     * Executor.
     ************************************************************************/

    private LocalExecutor executor = new LocalExecutor();

    public static class LocalExecutor extends Executor<LocalQueryContext,LocalCommandContext> {

        public LocalExecutor() {
            super.add (new Query1Processor());
            super.add (new Query2Processor());
            super.add (new Query4Processor());
            super.add (new Command1Processor ());
        }

    }

    /************************************************************************
     * Command classes.
     ************************************************************************/

    protected static final Command1Target TARGET1 = new Command1Target();

    public static class LocalCommandContext extends CommandProcessorContext<LocalCommandContext> {

    }

    public static class Command1 extends C {
        protected boolean error;
        protected VString param1 = new VString ();
        protected VString param2 = new VString ();

        public Command1 error(boolean error) {
            this.error = error;
            return this;
        }

        public Command1 param1(String param1) {
            assign (this.param1, param1);
            return this;
        }

        public Command1 param2(String param2) {
            assign (this.param2, param2);
            return this;
        }
    }

    public static class Command1Target {
        
        protected String param1 = "";

        protected String param2 = "";

        public void reset() {
            param1 = param2 = "";
        }
    }

    public static class Command1Processor extends CommandProcessor<Command1, Command1Target, LocalCommandContext> {
        public Command1Processor() {
            super (Command1.class, Command1Target.class);
        }

        @Override
        public Command1Target resolve(Command1 command, LocalCommandContext context) throws NoProcessorException, ProcessorException {
            if (command.param1.isSet())
                TARGET1.param1 = command.param1.getValue();
            if (command.param2.isSet())
                TARGET1.param2 = command.param2.getValue();
            if (command.error)
                throw new ProcessorException ();
            return TARGET1;
        }

    }

    public static class Command2 extends C {
    }

    /************************************************************************
     * Query classes.
     ************************************************************************/

    public static class LocalQueryContext {
        
    }

    public static class Query1 extends Query<Object> {

        public String param1;

        public Query1(String param1) {
            this.param1 = param1;
        }
    }

    public static class Query1Processor extends QueryProcessor<LocalQueryContext, Object, Query1> {

        public Query1Processor() {
            super(Query1.class);
        }

        @Override
        public Object process(LocalQueryContext context, Query1 query) throws ProcessorException {
            Assertions.assertNotNull (context);
            Assertions.assertNotNull (query);
            return "Query1Outcome:" + query.param1;
        }

        
    }

    public static class Query2 extends Query<Object> {
        
        public String param1;

        public Query2(String param1) {
            this.param1 = param1;
        }
    }

    public static class Query2Processor extends QueryProcessor<LocalQueryContext, Object, Query2> {

        public Query2Processor() {
            super(Query2.class);
        }

        @Override
        public Object process(LocalQueryContext context, Query2 query) throws ProcessorException {
            Assertions.assertNotNull (context);
            Assertions.assertNotNull (query);
            return "Query2Outcome:" + query.param1;
        }

    }

    public static class Query3 extends Query<Object> {
        
        public String param1;

        public Query3(String param1) {
            this.param1 = param1;
        }
    }

    public static class Query4 extends Query<Object> {
    }

    public static class Query4Processor extends QueryProcessor<LocalQueryContext, Object, Query4> {

        public Query4Processor() {
            super(Query4.class);
        }

        @Override
        public Object process(LocalQueryContext context, Query4 query) throws ProcessorException {
            throw new ProcessorException ();
        }

    }
}
