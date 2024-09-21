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
package com.effacy.jui.rpc.handler.client.command;

import java.util.ArrayList;

/**
 * Convenience list of commands.
 */
public class Commands extends ArrayList<ICommand> {

    /**
     * Unique ID.
     */
    private static final long serialVersionUID = 733714247381326913L;


    /**
     * Construct adding optional commands.
     * 
     * @param commands
     *            the commands to add.
     */
    public Commands(ICommand... commands) {
        for (ICommand command : commands) {
            if (command != null)
                add (command);
        }
    }


    /**
     * Convert to an array of commands.
     * 
     * @return the command array.
     */
    public ICommand [] asArray() {
        return toArray (new ICommand[size ()]);
    }

}
