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
package com.effacy.jui.rpc.handler.exception;

/**
 * Where a processor was not able to be mapped.
 *
 * @author Jeremy Buckley
 */
public class NoProcessorException extends Exception {

    /**
     * Unique serialization ID.
     */
    private static final long serialVersionUID = 176471431204139935L;

    /**
     * Construct with class not able to be mapped.
     * 
     * @param forclass
     *                 the class.
     */
    public NoProcessorException(Class<?> forClass) {
        super ("Unable to locate processor for " + ((forClass == null) ? "{null class provided}" : forClass.getSimpleName ()));
    }

    /**
     * Construct with class not able to be mapped.
     * 
     * @param forclass
     *                 the class.
     * @param forclass
     *                 the class.
     */
    public NoProcessorException(Class<?> forClass, Class<?> forOtherClass) {
        super ("Unable to locate processor for " + ((forClass == null) ? "{null class provided}" : forClass.getSimpleName ()) + "::" + ((forOtherClass == null) ? "{null class provided}" : forOtherClass.getSimpleName ()));
    }

}
