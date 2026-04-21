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
package com.effacy.jui.core.client.test;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.effacy.jui.core.client.component.IDisposeListener;
import com.effacy.jui.core.client.observable.IListener;
import com.effacy.jui.core.client.observable.ListenerOracle;
import com.effacy.jui.core.client.store.IStoreAfterLoadListener;
import com.effacy.jui.core.client.store.IStoreBeforeLoadListener;
import com.effacy.jui.core.client.store.IStoreChangedListener;
import com.effacy.jui.platform.css.client.CssDeclaration;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.core.shared.GWT;

public class GWTTestBridge extends GWTBridge {

    private static GWTTestBridge BRIDGE;
    private static final Map<Class<?>, Supplier<?>> REGISTRY = new ConcurrentHashMap<>();

    public static void init() {
        if (BRIDGE == null) {
            BRIDGE = new GWTTestBridge ();
            GWT.setBridge (BRIDGE);
        }
    }

    public static <T> void register(Class<?> classLiteral, Supplier<T> supplier) {
        init();
        if (supplier == null)
            REGISTRY.remove(classLiteral);
        else
            REGISTRY.put(classLiteral, supplier);
    }

    public static void clear() {
        REGISTRY.clear();
    }

    @Override
    public <T> T create(Class<?> classLiteral) {
        Supplier<?> supplier = REGISTRY.get(classLiteral);
        if (supplier != null)
            return (T) supplier.get();
        
        Class<? extends CssDeclaration> cssDeclaration = resolveCssDeclaration(classLiteral);
        if (cssDeclaration != null)
            return (T) CssStub.prefixed(cssDeclaration, null);

        if (classLiteral == ListenerOracle.class)
            return (T) new ListenerOracleMock ();
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends CssDeclaration> resolveCssDeclaration(Class<?> classLiteral) {
        if ((classLiteral == null) || !CssDeclaration.class.isAssignableFrom(classLiteral))
            return null;
        if (classLiteral.isInterface())
            return (Class<? extends CssDeclaration>) classLiteral;

        Class<? extends CssDeclaration> resolved = null;
        for (Class<?> implemented : classLiteral.getInterfaces()) {
            Class<? extends CssDeclaration> candidate = resolveCssDeclaration(implemented);
            if (candidate == null)
                continue;
            if (candidate == CssDeclaration.class)
                resolved = candidate;
            else
                return candidate;
        }

        if (resolved != null)
            return resolved;
        return resolveCssDeclaration(classLiteral.getSuperclass());
    }

    @Override
    public String getVersion() {
        return "UNIT-TEST";
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public void log(String message, Throwable e) {
        // Nothing.
    }

    /**
     * A variation of the {@link ListenerOracle} for unit testing. This is supported
     * by the {@link GWTTestBridge}.
     */
    public class ListenerOracleMock extends ListenerOracle {

        @Override
        public <L extends IListener> L find(Class<L> klass, Collection<IListener> listeners, String debugString) {
            L listener = super.find (klass, listeners, debugString);
            if (listener != null)
                return listener;
            if (klass == IDisposeListener.class)
                return (L) IDisposeListener.create (cpt -> {});
            if (klass == IStoreChangedListener.class)
                return (L) IStoreChangedListener.create (str -> {});
            if (klass == IStoreAfterLoadListener.class)
                return (L) IStoreAfterLoadListener.create (str -> {});
            if (klass == IStoreBeforeLoadListener.class)
                return (L) IStoreBeforeLoadListener.create (str -> {});
            return null;
        }
    }

}
