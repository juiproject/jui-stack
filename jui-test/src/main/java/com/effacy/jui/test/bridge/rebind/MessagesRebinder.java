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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Models rebinding of interfaces that extend {@link Messages}.
 * <p>
 * This implementation is quite limited in that it will only return the default
 * message as it appears using the {@link DefaultMessage} annotation.
 */
public class MessagesRebinder implements Rebinder {

    private Map<Class<?>,Class<?>> klasses = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> create(Class<T> klass) throws Exception {
        if (!Messages.class.isAssignableFrom(klass))
            return Optional.empty();
        Class<? extends T> dKlass = (Class<? extends T>) klasses.get(klass);
        if (dKlass == null) {
            if (Modifier.isAbstract(klass.getModifiers())) {
                dKlass = new ByteBuddy()
                    .subclass(klass)
                    .method(ElementMatchers.isAbstract()
                        .and(ElementMatchers.returns(String.class))
                        .and(ElementMatchers.takesArguments(0))
                    ).intercept(MethodDelegation.to(MessageMethodNameInterceptor.class))
                    .make()
                    .load(klass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
            }
        }
        if (dKlass != null)
            return Optional.of(dKlass.getDeclaredConstructor().newInstance());
        return Optional.empty();
    }

    public class MessageMethodNameInterceptor {
    
        public static String intercept(@Origin Method method) {
            DefaultMessage ann = method.getAnnotation(DefaultMessage.class);
            if (ann == null)
                return method.getName();
            return ann.value();
        }
    }
}
