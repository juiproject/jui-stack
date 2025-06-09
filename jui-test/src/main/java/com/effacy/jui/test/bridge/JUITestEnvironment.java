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
package com.effacy.jui.test.bridge;

import java.util.Optional;
import java.util.function.Consumer;

import com.effacy.jui.test.bridge.rebind.Rebinder;
import com.effacy.jui.test.bridge.rebind.RebinderBuilder;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GWTBridge;

/**
 * Used to provide an implementation of the JUI runtime environment hook in a
 * test environment (i.e. running on a JVM).
 * <p>
 * This is fairly limited in scope as rebinding is difficult. For these we offer
 * the ability to create customer rebinds (see {@link Rebinder}) that construct
 * the relevant classes effectively as mocks.
 * <p>
 * To use invoke one of the {@link #init()} methods prior to running unit tests.
 */
public class JUITestEnvironment  {

    public static String LOCALE = "en";

    public static void init() {
        init((Rebinder) null);
    }

    public static void init(Consumer<RebinderBuilder> configure) {
        RebinderBuilder builder = new RebinderBuilder();
        if (configure != null)
            configure.accept(builder);
        init(builder.build());
    }

    public static void init(Rebinder rebind) {
        GWT.setBridge(new TestGWTBridge(rebind));
    }

    /************************************************************************
     * Specific platform implementations.
     ************************************************************************/

    /**
     * For lodging a GWT bridge.
     */
    protected static class TestGWTBridge extends GWTBridge {

        /**
         * The underlying rebind implementation to use.
         */
        private Rebinder rebind;

        /**
         * Construct with a rebind implementation.
         * 
         * @param rebind
         *               the implementation (if {@code null} then a default will be
         *               used).
         */
        protected TestGWTBridge(Rebinder rebind) {
            this.rebind = rebind;
            if (rebind == null)
                this.rebind = new RebinderBuilder().build();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T create(Class<?> classLiteral) {
            if (rebind == null)
                throw new UnsupportedOperationException("No constructor for creating instances of " + classLiteral.getName());
            try {
                Optional<T> instance = (Optional<T>) rebind.create(classLiteral);
                if (instance.isEmpty())
                    throw new UnsupportedOperationException("No constructor for creating instances of " + classLiteral.getName());
                return instance.get();
            } catch (UnsupportedOperationException e) {
                throw e;
            } catch (Exception e) {
                throw new UnsupportedOperationException("Problem creating instances o " + classLiteral.getName(), e);
            }
        }

        @Override
        public String getVersion() {
            return "2.12";
        }

        @Override
        public boolean isClient() {
            // Note that this will allow Debug (in the core project) to detect running as a
            // unit test.
            return false;
        }

        @Override
        public void log(String message, Throwable e) {
            if (message != null)
                System.out.println(message);
            if (e != null)
                e.printStackTrace();
        }
    }
}
