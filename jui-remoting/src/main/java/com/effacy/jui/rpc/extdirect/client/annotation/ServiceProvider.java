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
package com.effacy.jui.rpc.extdirect.client.annotation;

import com.effacy.jui.rpc.extdirect.client.service.IService;

/**
 * Used to indicate whether or not a class is intended to be a provider of
 * services and should be registered with the method registry. The class does
 * need to extends a class that has exposed service methods and that extends
 * {@link IService}.
 * 
 * @author Jeremy Buckley
 */
public @interface ServiceProvider {

}
