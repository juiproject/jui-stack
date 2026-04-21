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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.core.client.dom.builder.DomNodes;

import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DOMTokenList;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Node;
import elemental2.dom.Text;

/**
 * Test DOM node factory that creates inspectable fake nodes.
 * <p>
 * This can be installed via {@link DomNodes#factory(DomNodes.IDomNodeFactory)}
 * so builder and fragment code generates fake DOM suitable for structural
 * assertions.
 */
public class DomTestNodeFactory implements DomNodes.IDomNodeFactory {

    /**
     * Installs this factory into {@link DomNodes}.
     * 
     * @return this factory.
     */
    public DomTestNodeFactory install() {
        DomNodes.factory(this);
        return this;
    }

    @Override
    public Element createElement(String tag) {
        if ("a".equalsIgnoreCase(tag))
            return new FakeAnchor(tag);
        if ("input".equalsIgnoreCase(tag))
            return new FakeInput(tag);
        return new FakeElement(tag);
    }

    @Override
    public Element createElementNS(String namespace, String tag) {
        return new FakeElement(namespace, tag);
    }

    @Override
    public Text createTextNode(String text) {
        return new FakeText(text);
    }

    /**
     * Simple fake token list that captures classes.
     */
    public static class FakeTokenList extends DOMTokenList {

        private final List<String> tokens = new ArrayList<>();

        @Override
        public void add(String... token) {
            for (String value : token) {
                if ((value != null) && !tokens.contains(value))
                    tokens.add(value);
            }
            this.value = String.join(" ", tokens);
            this.length = tokens.size();
        }

        @Override
        public boolean contains(String token) {
            return tokens.contains(token);
        }

        public List<String> tokens() {
            return new ArrayList<>(tokens);
        }
    }

    /**
     * Simple fake style declaration that captures assigned style properties.
     */
    public static class FakeStyleDeclaration extends CSSStyleDeclaration {

        private final Map<String, String> values = new LinkedHashMap<>();

        @Override
        public void set(String property, String value) {
            values.put(property, value);
        }

        @Override
        public String get(String property) {
            return values.get(property);
        }

        @Override
        public void setProperty(String property, String value) {
            values.put(property, value);
        }

        public Map<String, String> values() {
            return new LinkedHashMap<>(values);
        }
    }

    /**
     * Common support for fake element implementations.
     */
    public static class FakeElement extends HTMLElement {

        private final String tag;
        private final String namespace;
        private final List<Node> children = new ArrayList<>();
        private final Map<String, String> attributes = new LinkedHashMap<>();

        public FakeElement(String tag) {
            this(null, tag);
        }

        public FakeElement(String namespace, String tag) {
            this.tag = tag;
            this.namespace = namespace;
            this.tagName = tag;
            this.nodeName = tag;
            this.classList = new FakeTokenList();
            this.style = new FakeStyleDeclaration();
        }

        public String tag() {
            return tag;
        }

        public String namespace() {
            return namespace;
        }

        public List<Node> children() {
            return children;
        }

        public Map<String, String> attributes() {
            return attributes;
        }

        public boolean hasClass(String name) {
            return ((FakeTokenList) classList).contains(name);
        }

        @Override
        public Node appendChild(Node child) {
            children.add(child);
            child.parentNode = this;
            if (child instanceof Element)
                child.parentElement = this;
            if (child instanceof Element)
                firstElementChild = (firstElementChild == null) ? (Element) child : firstElementChild;
            return child;
        }

        @Override
        public Node removeChild(Node child) {
            children.remove(child);
            if (firstElementChild == child)
                firstElementChild = children.stream()
                    .filter(Element.class::isInstance)
                    .map(Element.class::cast)
                    .findFirst()
                    .orElse(null);
            child.parentNode = null;
            child.parentElement = null;
            return child;
        }

        @Override
        public void remove() {
            if (parentNode != null)
                parentNode.removeChild(this);
        }
    }

    /**
     * Fake anchor element.
     */
    public static class FakeAnchor extends HTMLAnchorElement {

        private final String tag;
        private final List<Node> children = new ArrayList<>();
        private final Map<String, String> attributes = new LinkedHashMap<>();

        public FakeAnchor(String tag) {
            this.tag = tag;
            this.tagName = tag;
            this.nodeName = tag;
            this.classList = new FakeTokenList();
            this.style = new FakeStyleDeclaration();
        }

        public String tag() {
            return tag;
        }

        public List<Node> children() {
            return children;
        }

        public Map<String, String> attributes() {
            return attributes;
        }

        public boolean hasClass(String name) {
            return ((FakeTokenList) classList).contains(name);
        }

        @Override
        public Node appendChild(Node child) {
            children.add(child);
            child.parentNode = this;
            if (child instanceof Element)
                child.parentElement = this;
            if (child instanceof Element)
                firstElementChild = (firstElementChild == null) ? (Element) child : firstElementChild;
            return child;
        }

    }

    /**
     * Fake input element to support checked/disabled property assignment.
     */
    public static class FakeInput extends HTMLInputElement {

        private final String tag;
        private final Map<String, String> attributes = new LinkedHashMap<>();

        public FakeInput(String tag) {
            this.tag = tag;
            this.tagName = tag;
            this.nodeName = tag;
            this.classList = new FakeTokenList();
            this.style = new FakeStyleDeclaration();
        }

        public String tag() {
            return tag;
        }

        public Map<String, String> attributes() {
            return attributes;
        }

        public boolean hasClass(String name) {
            return ((FakeTokenList) classList).contains(name);
        }

    }

    /**
     * Fake text node.
     */
    public static class FakeText extends Text {

        public FakeText(String text) {
            super(text);
            this.textContent = text;
            this.nodeValue = text;
            this.nodeName = "#text";
        }

        public String text() {
            return textContent;
        }
    }
}
