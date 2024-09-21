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
package com.effacy.jui.core.client.dom.jquery;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class AnimateOptions {

    @JsProperty
    public native double getDuration();
    
    @JsProperty
    public native void setDuration(double duration);

    @JsProperty
    public native String getEasing();
    
    @JsProperty
    public native void setEasing(String easing);

    @JsProperty
    public native boolean isQueue();
    
    @JsProperty
    public native void setQueue(boolean queue);

    @JsProperty
    public native Object getSpecialEasing();
    
    @JsProperty
    public native void setSpecialEasing(Object specialEasing);

}
