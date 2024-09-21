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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.effacy.jui.json.annotation.JsonSerializable;
import com.effacy.jui.json.annotation.TypeMode;
import com.effacy.jui.rpc.handler.client.ref.Ref;
import com.effacy.jui.rpc.handler.client.ref.UniqueRef;

/**
 * Implemented by classes that define a command that can be executed.
 *
 * @author Jeremy Buckley
 */
@JsonSerializable(type = TypeMode.SIMPLE)
public interface ICommand {

    /**
     * Represents an action to perform.
     */
    @JsonSerializable(type = TypeMode.SIMPLE)
    public interface IAction {

        /**
         * Allows for the specification of a priority (generally applies to
         * actions).
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        public @interface Priority {

            /**
             * Specifies a priority value.
             */
            public int value();

        }

        /**
         * Marks the action as a terminal one (do not process anything after the
         * action). For example, the action results in a deletion of the entity
         * being acted on.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        public @interface Terminal {

        }

    }

    /**
     * Obtains a unique reference to the product of this command. This can be
     * used in lookups to access the entity associated with the command when
     * using multiple commands.
     * 
     * @return the command entity reference.
     */
    public UniqueRef reference();


    /**
     * Gets the lookup.
     * 
     * @return the lookup.
     */
    public Object lookup();


    /**
     * Gets the label for the command to be used to associate errors to the
     * command.
     * 
     * @return the label.
     */
    public String label();


    /**
     * Adds an action to perform.
     * 
     * @param <A>
     * @param action
     *            the action to add.
     * @return the passed action.
     */
    public <A extends IAction> A add(A action);

}
