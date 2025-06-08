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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.effacy.jui.platform.css.client.CssDeclaration;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class CssDeclarationRebinder implements Rebinder {

    private Map<Class<?>,Class<?>> klasses = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> create(Class<T> klass) throws Exception {
        if (!CssDeclaration.class.isAssignableFrom(klass))
            return Optional.empty();
        Class<? extends T> dKlass = (Class<? extends T>) klasses.get(klass);
        if (dKlass == null) {
            if (Modifier.isAbstract(klass.getModifiers())) {
                dKlass = new ByteBuddy()
                    .subclass(klass)
                    // The ensureInjected method.
                    .method(ElementMatchers.named("ensureInjected")
                        .and(ElementMatchers.returns(boolean.class))
                        .and(ElementMatchers.takesArguments(0))
                    ).intercept(FixedValue.value(true))
                    // The ensureInjected method.
                    .method(ElementMatchers.named("getCssDeclarations")
                        .and(ElementMatchers.returns(Map.class))
                        .and(ElementMatchers.takesArguments(0))
                    ).intercept(FixedValue.value(new HashMap<String,Map<String,String>>()))
                    // The ensureInjected method.
                    .method(ElementMatchers.named("getCssText")
                        .and(ElementMatchers.returns(String.class))
                        .and(ElementMatchers.takesArguments(0))
                    ).intercept(FixedValue.value(""))
                    // All style methods.
                    .method(ElementMatchers.isAbstract()
                        .and(ElementMatchers.returns(String.class))
                        .and(ElementMatchers.takesArguments(0))
                        .and(ElementMatchers.not(ElementMatchers.named("getCssText")))
                    ).intercept(MethodDelegation.to(StringMethodNameInterceptor.class))
                    // All other methods.
                    .method(ElementMatchers.isAbstract()
                        .and(ElementMatchers.not(ElementMatchers.named("ensureInjected")))
                        .and(ElementMatchers.not(ElementMatchers.named("getCssDeclarations")).and(ElementMatchers.returns(Map.class)).and(ElementMatchers.takesArguments(0)))
                        .and(ElementMatchers.not(ElementMatchers.returns(String.class).and(ElementMatchers.takesArguments(0))))
                    ).intercept(FixedValue.nullValue())
                    .make()
                    .load(klass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
            }
        }
        if (dKlass != null)
            return Optional.of(dKlass.getDeclaredConstructor().newInstance());
        return Optional.empty();
    }
}
