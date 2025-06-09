/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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
package com.effacy.jui.test.bridge.rebind;

import java.util.Optional;

import com.effacy.jui.test.bridge.JUITestEnvironment;
import com.google.gwt.i18n.client.impl.LocaleInfoImpl;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

public class LocaleInfoImplRebinder implements Rebinder {

    private Class<? extends LocaleInfoImpl> dKlass;

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> create(Class<T> klass) throws Exception {
        if (!LocaleInfoImpl.class.equals(klass))
            return Optional.empty();
        if (dKlass == null) {
            dKlass =  new ByteBuddy()
                .subclass(LocaleInfoImpl.class)
                .method(ElementMatchers.named("getRuntimeLocale")
                ).intercept(FixedValue.value(JUITestEnvironment.LOCALE))
                .make()
                .load(LocaleInfoImpl.class.getClassLoader())
                .getLoaded();
        }
        return Optional.of((T)dKlass.getDeclaredConstructor().newInstance());
    }
}
