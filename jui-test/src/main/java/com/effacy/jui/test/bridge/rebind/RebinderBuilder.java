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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RebinderBuilder {

    private List<Rebinder> rebinds = new ArrayList<>();

    public RebinderBuilder add(Rebinder rebind) {
        if (rebind != null)
            rebinds.add(rebind);
        return this;
    }

    public Rebinder build() {
        // Add in the defaults.
        add(new CssDeclarationRebinder());
        add(new LocaleInfoImplRebinder());
        add(new CldrImplRebinder());
        add(new DateTimeFormatInfoImplRebinder());
        add(new MessagesRebinder());
        return new Rebinder() {

            @Override
            public <T> Optional<T> create(Class<T> klass) throws Exception {
                for (Rebinder rebind : rebinds) {
                    try {
                        Optional<T> instance = rebind.create((Class<T>)klass);
                        if (instance.isPresent())
                            return instance;
                    } catch (Exception e) {
                        throw new UnsupportedOperationException("Contructor failed for " + klass.getName(), e);
                    }
                }
                return Optional.empty();
            }
            
        };
    }


}
