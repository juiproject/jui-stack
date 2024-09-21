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
package com.effacy.jui.playground.remoting.api.processors;

import com.effacy.jui.playground.dto.ExampleLookup;
import com.effacy.jui.playground.dto.ExampleResult;
import com.effacy.jui.playground.remoting.api.QueryContext;
import com.effacy.jui.rpc.handler.RPCHandlerProcessor;
import com.effacy.jui.rpc.handler.exception.ProcessorException;
import com.effacy.jui.rpc.handler.query.QueryProcessor;

@RPCHandlerProcessor
public class ExampleLookupProcessor extends QueryProcessor<QueryContext, ExampleResult, ExampleLookup> {

    public ExampleLookupProcessor() {
        super (ExampleLookup.class);
    }

    @Override
    protected ExampleResult process(QueryContext context, ExampleLookup query) throws ProcessorException {
        ExampleResult result = new ExampleResult();
        result.setValue1 ("It worked!");
        return result;
    }
}
