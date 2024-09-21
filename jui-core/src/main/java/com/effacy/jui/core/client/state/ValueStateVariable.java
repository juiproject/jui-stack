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
package com.effacy.jui.core.client.state;

public class ValueStateVariable<V> extends LifecycleStateVariable<ValueStateVariable<V>> {

    private V value;

    public ValueStateVariable() {
        this (null);
    }

    public ValueStateVariable(V value) {
        super ();
        this.value = value;
        this.state = (value == null) ? State.LOADING : State.OK;
    }
    
    public V value() {
        return value;
    }

    @Override
    protected void onModify() {
        if (value == null)
            state = State.UNEXPECTED;
        else
            super.onModify();
    }

    public void assign(V value) {
        this.value = value;
        this.state = (value == null) ? State.UNEXPECTED : State.OK;
        emit();
    }

    
}
