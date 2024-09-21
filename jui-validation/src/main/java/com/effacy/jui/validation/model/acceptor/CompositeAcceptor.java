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
package com.effacy.jui.validation.model.acceptor;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.validation.model.IErrorMessage;
import com.effacy.jui.validation.model.IErrorMessageAcceptor;

public class CompositeAcceptor implements IErrorMessageAcceptor {

    public List<IErrorMessageAcceptor> acceptors = new ArrayList<> ();

    public CompositeAcceptor add(IErrorMessageAcceptor acceptor) {
        if (acceptor != null)
            this.acceptors.add (acceptor);
        return this;
    }


    /**
     * Determines if there are any acceptors in the composite.
     * 
     * @return {@code true} if there are none.
     */
    public boolean isEmpty() {
        return acceptors.isEmpty ();
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.validation.model.IErrorMessageAcceptor#accept(com.effacy.jui.validation.model.IErrorMessage)
     */
    @Override
    public boolean accept(IErrorMessage message) {
        for (IErrorMessageAcceptor acceptor : acceptors) {
            if (acceptor == null)
                continue;
            if (acceptor.accept (message))
                return true;
        }
        return false;
    }

}
