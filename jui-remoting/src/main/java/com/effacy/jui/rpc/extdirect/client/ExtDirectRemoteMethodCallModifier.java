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
package com.effacy.jui.rpc.extdirect.client;

/**
 * Used to modify the way a single call is handled. Only applies to the next
 * call.
 */
public class ExtDirectRemoteMethodCallModifier {

    /**
     * Make the next call immediate.
     */
    protected static boolean immediate;


    /**
     * Make the next call be invoked immediately without being batched.
     * 
     * @return this instance.
     */
    public static void immediate() {
        ExtDirectRemoteMethodCallModifier.immediate = true;
    }


    /**
     * Resets the configuration.
     */
    public static void reset() {
        immediate = false;
    }
}
