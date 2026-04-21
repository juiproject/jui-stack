/*******************************************************************************
 * Copyright 2026 Jeremy Buckley
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

import com.effacy.jui.core.client.dom.builder.ExistingElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertable;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;

/**
 * Base support for tests that build fake DOM from {@link IDomInsertable}
 * content.
 * <p>
 * This installs the {@link DomTestNodeFactory}, builds into a fake root element
 * and returns a {@link DomTest} for fluent assertions.
 */
public abstract class AbstractJUITest {

    // TODO: When moved to jui-test then these would be the standard way to setup (and clear) the bridge.
    // @BeforeAll
    // public static void configureGWT() {
    //     GWTTestBridge.init ();
    // }
    // @AfterEach
    // public void cleanupGWT() {
    //     GWTTestBridge.clear();
    // }

    /**
     * Builds into a default {@code div} root and returns a fluent
     * {@link DomTest}.
     * 
     * @param content
     *                the content to build.
     * @return the DOM tester.
     */
    protected DomTest build(IDomInsertable content) {
        return build("div", content);
    }

    /**
     * Builds into the provided root tag and returns a fluent {@link DomTest}. This
     * also initializes the {@link GWTTestBridge}.
     * 
     * @param rootTag
     *                the root tag to create.
     * @param content
     *                the content to build.
     * @return the DOM tester.
     */
    protected DomTest build(String rootTag, IDomInsertable content) {
        GWTTestBridge.init();
        FakeElement root = root(rootTag);
        ExistingElementBuilder parent = new ExistingElementBuilder(root);
        if (content != null)
            parent.insert(content);
        parent.build();
        return DomTest.on(root);
    }

    /**
     * Creates a fake root element after installing a fresh
     * {@link DomTestNodeFactory}.
     * 
     * @param tag
     *            the root tag.
     * @return the fake root.
     */
    protected FakeElement root(String tag) {
        new DomTestNodeFactory().install();
        return new DomTestNodeFactory.FakeElement(tag);
    }
}
