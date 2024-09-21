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
package com.effacy.jui.rpc.handler.query;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effacy.jui.rpc.extdirect.RouterLogger;
import com.effacy.jui.rpc.handler.client.command.C;
import com.effacy.jui.rpc.handler.client.query.Query;
import com.effacy.jui.rpc.handler.exception.NoProcessorException;
import com.effacy.jui.rpc.handler.exception.ProcessorException;

/**
 * Base class implementation of {@link IQueryProcessor}.
 * <p>
 * This captures the query class and provides support methods for common
 * implementations.
 * 
 * @author Jeremy Buckley
 * @param <CTX> the context type.
 * @param <V> the return value type.
 * @param <Q> the query type.
 */
public abstract class QueryProcessor<CTX, V, Q extends Query<V>> implements IQueryProcessor<CTX> {

    /**
     * The query class to match against.
     */
    private Class<Q> queryClass;

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger (QueryProcessor.class);

    /**
     * Construct with query class for matching.
     * 
     * @param queryClass
     *            the class for matching.
     */
    protected QueryProcessor(Class<Q> queryClass) {
        this.queryClass = queryClass;
    }

    /**
     * The query class for this processor.
     * 
     * @return the class.
     */
    protected Class<Q> queryClass() {
        return queryClass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.query.IQueryProcessor#matches(java.lang.Object)
     */
    @Override
    public boolean matches(Object query) {
        return queryClass.isAssignableFrom (query.getClass ());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.query.IQueryProcessor#process(C,java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object process(CTX context, Object query) throws NoProcessorException, ProcessorException {
        try {
            RouterLogger.indent (getClass ().getSimpleName () + ".process()");
            onBeforeProcess (context, (Q) query);
            V result = process (context, (Q) query);
            onAfterProcess (context, (Q) query, result, false);
            return result;
        } catch (Throwable e) {
            onAfterProcess (context, (Q) query, null, true);
            if (e instanceof ProcessorException)
                throw (ProcessorException) e;
            LOG.error ("Uncaught exception from query processor", e);
            throw new ProcessorException ();
        } finally {
            RouterLogger.outdent ();
        }
    }

    /**
     * Performs an actual processing of a lookup query.
     * 
     * @param context
     *            the context to operate in.
     * @param lookup
     *            the lookup reference.
     * @return the result.
     */
    protected abstract V process(CTX context, Q query) throws ProcessorException;

    /**
     * Invoked prior to processing.
     * 
     * @param context
     *            the operating context.
     * @param query
     *            the query.
     */
    protected void onBeforeProcess(CTX context, Q query) throws ProcessorException {
        // Nothing.
    }


    /**
     * Invoked after processing.
     * 
     * @param context
     *            the operating context.
     * @param query
     *            the query.
     * @param result
     *            the response.
     * @param error
     *            {@code true} if there was an error.
     */
    protected void onAfterProcess(CTX context, Q query, V result, boolean error) {
        // Nothing.
    }

}
