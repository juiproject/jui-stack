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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeStyleDeclaration;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeText;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeTokenList;

/**
 * Fluent testing support for fake DOM trees produced through
 * {@link DomTestNodeFactory}.
 * <p>
 * Supports navigation by a small CSS-like selector model and assertions against
 * the current node selection.
 */
public class DomTest {

    private static final Pattern MARKUP_TOKEN = Pattern.compile("(</?[^>]+>|\\[[^\\]]+\\])");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(\\S+)\\s*=\\s*\"([^\"]*)\"");

    public static DomTest on(FakeElement root) {
        return new DomTest(root, Collections.singletonList(root), null, "<root>");
    }

    public static DomTest $(FakeElement root) {
        return on(root);
    }

    private FakeElement root;
    private List<FakeElement> current;
    private DomTest parent;
    private String path;

    private DomTest(FakeElement root, List<FakeElement> current, DomTest parent, String path) {
        this.root = root;
        this.current = current;
        this.parent = parent;
        this.path = path;
    }

    public DomTest select(String selector) {
        return new DomTest(root, query(current, selector, false), this, path + " -> " + selector);
    }

    public DomTest select(String selector, Consumer<DomTest> assertions) {
        DomTest selected = new DomTest(root, query(current, selector, false), this, path + " -> " + selector);
        assertions.accept(selected);
        return this;
    }

    public DomTest child(String selector) {
        return new DomTest(root, query(current, selector, true), this, path + " > " + selector);
    }

    public DomTest child(String selector, Consumer<DomTest> assertions) {
        DomTest child = new DomTest(root, query(current, selector, true), this, path + " > " + selector);
        assertions.accept(child);
        return this;
    }

    public DomTest up() {
        if (parent == null)
            throw new AssertionError("Already at the root DOM selection");
        return parent;
    }

    public DomTest exists() {
        if (current.isEmpty())
            throw new AssertionError("No DOM node matched selection [" + path + "]");
        return this;
    }

    public DomTest count(int expected) {
        if (current.size() != expected)
            throw new AssertionError("Unexpected match count for [" + path + "]: expected " + expected + " but was " + current.size());
        return this;
    }

    public DomTest tagEquals(String expected) {
        FakeElement node = single();
        if (!expected.equals(node.tag()))
            throw new AssertionError("Unexpected tag for [" + path + "]: expected [" + expected + "] but was [" + node.tag() + "]");
        return this;
    }

    public DomTest textEquals(String expected) {
        String actual = textOf(single());
        if (!expected.equals(actual))
            throw new AssertionError("Unexpected text for [" + path + "]: expected [" + expected + "] but was [" + actual + "]");
        return this;
    }

    public DomTest hasClass(String name) {
        if (!single().hasClass(name))
            throw new AssertionError("Selection [" + path + "] does not have class [" + name + "]");
        return this;
    }

    public DomTest attributeEquals(String name, String expected) {
        String actual = single().attributes().get(name);
        if ((expected == null) ? (actual != null) : !expected.equals(actual))
            throw new AssertionError("Unexpected attribute [" + name + "] for [" + path + "]: expected [" + expected + "] but was [" + actual + "]");
        return this;
    }

    public DomTest htmlEquals(String expectedHtml) {
        String actualHtml = toPrettyHtml();
        List<String> normalizedExpected = normalizeHtml(expectedHtml);
        List<String> normalizedActual = normalizeHtml(actualHtml);
        if (!normalizedExpected.equals(normalizedActual)) {
            throw new AssertionError("""
                HTML did not match for [%s].

                Expected:
                %s

                Actual:
                %s

                Normalized expected:
                %s

                Normalized actual:
                %s
                """.formatted(path, expectedHtml.strip(), actualHtml.strip(), normalizedExpected, normalizedActual));
        }
        return this;
    }

    public DomTest print() {
        System.out.println(toPrettyHtml());
        return this;
    }

    public FakeElement element() {
        return single();
    }

    public FakeElement unwrap() {
        return element();
    }

    public List<FakeElement> elements() {
        return new ArrayList<>(current);
    }

    public String html() {
        return toPrettyHtml();
    }

    private FakeElement single() {
        if (current.isEmpty())
            throw new AssertionError("No DOM node matched selection [" + path + "]");
        if (current.size() != 1)
            throw new AssertionError("Expected a single DOM node for [" + path + "] but found " + current.size());
        return current.get(0);
    }

    private String toPrettyHtml() {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < current.size(); i++) {
            appendHtml(current.get(i), 0, html);
            if (i + 1 < current.size())
                html.append("\n");
        }
        return html.toString();
    }

    private static List<FakeElement> query(List<FakeElement> roots, String selector, boolean directChildFromCurrent) {
        List<SelectorStep> steps = parseSelector(selector);
        if (steps.isEmpty())
            return Collections.emptyList();
        if (directChildFromCurrent)
            steps.get(0).directChild = true;

        List<FakeElement> current = roots;
        for (SelectorStep step : steps) {
            List<FakeElement> next = new ArrayList<>();
            for (FakeElement element : current) {
                if (step.directChild)
                    findDirectChildren(element, step.selector, next);
                else
                    findDescendants(element, step.selector, next);
            }
            current = next;
        }
        return current;
    }

    private static List<SelectorStep> parseSelector(String selector) {
        List<SelectorStep> steps = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean nextDirectChild = false;
        int bracketDepth = 0;
        for (int i = 0; i < selector.length(); i++) {
            char ch = selector.charAt(i);
            if (ch == '[')
                bracketDepth++;
            else if (ch == ']')
                bracketDepth--;
            if ((bracketDepth == 0) && (ch == '>' || Character.isWhitespace(ch))) {
                if (token.length() > 0) {
                    addStep(steps, token, nextDirectChild);
                    token.setLength(0);
                    nextDirectChild = false;
                }
                if (ch == '>')
                    nextDirectChild = true;
                continue;
            }
            token.append(ch);
        }
        addStep(steps, token, nextDirectChild);
        return steps;
    }

    private static void addStep(List<SelectorStep> steps, StringBuilder token, boolean directChild) {
        String value = token.toString().trim();
        if (!value.isEmpty())
            steps.add(new SelectorStep(parseSimpleSelector(value), directChild));
    }

    private static SimpleSelector parseSimpleSelector(String selector) {
        SimpleSelector parsed = new SimpleSelector();
        int i = 0;
        while (i < selector.length()) {
            char ch = selector.charAt(i);
            if (ch == '.') {
                int next = nextBoundary(selector, i + 1);
                parsed.classes.add(selector.substring(i + 1, next));
                i = next;
            } else if (ch == '#') {
                int next = nextBoundary(selector, i + 1);
                parsed.id = selector.substring(i + 1, next);
                i = next;
            } else if (ch == '[') {
                int end = selector.indexOf(']', i);
                if (end == -1)
                    throw new IllegalArgumentException("Unterminated attribute selector [" + selector + "]");
                String content = selector.substring(i + 1, end).trim();
                int equals = content.indexOf('=');
                if (equals == -1)
                    parsed.attributes.add(new AttributeSelector(content, null));
                else
                    parsed.attributes.add(new AttributeSelector(
                        content.substring(0, equals).trim(),
                        unquote(content.substring(equals + 1).trim())
                    ));
                i = end + 1;
            } else {
                int next = nextBoundary(selector, i);
                parsed.tag = selector.substring(i, next);
                i = next;
            }
        }
        return parsed;
    }

    private static int nextBoundary(String value, int start) {
        for (int i = start; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '.' || ch == '#' || ch == '[')
                return i;
        }
        return value.length();
    }

    private static String unquote(String value) {
        if (((value.startsWith("\"")) && value.endsWith("\"")) || ((value.startsWith("'")) && value.endsWith("'")))
            return value.substring(1, value.length() - 1);
        return value;
    }

    private static void findDescendants(FakeElement element, SimpleSelector selector, List<FakeElement> matches) {
        for (Object child : element.children()) {
            if (child instanceof FakeElement) {
                FakeElement childElement = (FakeElement) child;
                if (matches(childElement, selector))
                    matches.add(childElement);
                findDescendants(childElement, selector, matches);
            }
        }
    }

    private static void findDirectChildren(FakeElement element, SimpleSelector selector, List<FakeElement> matches) {
        for (Object child : element.children()) {
            if ((child instanceof FakeElement) && matches((FakeElement) child, selector))
                matches.add((FakeElement) child);
        }
    }

    private static boolean matches(FakeElement element, SimpleSelector selector) {
        if ((selector.tag != null) && !selector.tag.equals(element.tag()))
            return false;
        if ((selector.id != null) && !selector.id.equals(element.attributes().get("id")))
            return false;
        for (String css : selector.classes) {
            if (!element.hasClass(css))
                return false;
        }
        for (AttributeSelector attribute : selector.attributes) {
            String value = element.attributes().get(attribute.name);
            if (value == null)
                return false;
            if ((attribute.value != null) && !attribute.value.equals(value))
                return false;
        }
        return true;
    }

    private static String textOf(FakeElement element) {
        StringBuilder text = new StringBuilder();
        appendText(element, text);
        return text.toString().replaceAll("\\s+", " ").trim();
    }

    private static void appendText(FakeElement element, StringBuilder text) {
        for (Object child : element.children()) {
            if (child instanceof FakeText)
                text.append(((FakeText) child).text()).append(' ');
            else if (child instanceof FakeElement)
                appendText((FakeElement) child, text);
        }
    }

    private static void appendHtml(FakeElement element, int depth, StringBuilder html) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < depth; i++)
            line.append("  ");
        line.append("<").append(element.tag());
        appendClasses(line, element);
        appendAttributes(line, element.attributes());
        appendStyles(line, element);
        line.append(">");
        html.append(line).append("\n");
        for (Object child : element.children()) {
            if (child instanceof FakeElement)
                appendHtml((FakeElement) child, depth + 1, html);
            else if (child instanceof FakeText) {
                StringBuilder textLine = new StringBuilder();
                for (int i = 0; i < depth + 1; i++)
                    textLine.append("  ");
                textLine.append(((FakeText) child).text());
                html.append(textLine).append("\n");
            }
        }
        StringBuilder close = new StringBuilder();
        for (int i = 0; i < depth; i++)
            close.append("  ");
        close.append("</").append(element.tag()).append(">");
        html.append(close).append("\n");
    }

    private static List<String> normalizeHtml(String html) {
        String source = html.replace("\r", "").trim();
        List<String> normalized = new ArrayList<>();
        Matcher matcher = MARKUP_TOKEN.matcher(source);
        int index = 0;
        while (matcher.find()) {
            appendNormalizedText(normalized, source.substring(index, matcher.start()));
            normalized.add("TAG:" + normalizeTag(matcher.group()));
            index = matcher.end();
        }
        appendNormalizedText(normalized, source.substring(index));
        return normalized;
    }

    private static void appendNormalizedText(List<String> normalized, String text) {
        String value = text.replaceAll("\\s+", " ").trim();
        if (!value.isEmpty())
            normalized.add("TEXT:" + value);
    }

    private static String normalizeTag(String tag) {
        String trimmed = tag.replaceAll("\\s+", " ").replaceAll("\\s*=\\s*", "=").trim();
        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.startsWith("/"))
            return "CLOSE:" + content.substring(1).trim();

        boolean selfClosing = content.endsWith("/");
        if (selfClosing)
            content = content.substring(0, content.length() - 1).trim();

        int split = findNameEnd(content);
        String name = (split == -1) ? content : content.substring(0, split);
        String attributeText = (split == -1) ? "" : content.substring(split).trim();
        Map<String, String> attributes = parseAttributes(attributeText);
        List<String> normalizedAttributes = new ArrayList<>();
        attributes.forEach((attrName, attrValue) -> normalizedAttributes.add(attrName + "=\"" + normalizeAttributeValue(attrName, attrValue) + "\""));
        Collections.sort(normalizedAttributes);

        StringBuilder normalized = new StringBuilder();
        normalized.append("OPEN:").append(name);
        for (String attribute : normalizedAttributes)
            normalized.append("|").append(attribute);
        if (selfClosing)
            normalized.append("|/");
        return normalized.toString();
    }

    private static int findNameEnd(String tagContent) {
        for (int i = 0; i < tagContent.length(); i++) {
            if (Character.isWhitespace(tagContent.charAt(i)))
                return i;
        }
        return -1;
    }

    private static Map<String, String> parseAttributes(String attributeText) {
        Map<String, String> attributes = new LinkedHashMap<>();
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeText);
        while (matcher.find())
            attributes.put(matcher.group(1), matcher.group(2));
        return attributes;
    }

    private static String normalizeAttributeValue(String name, String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        if ("class".equals(name)) {
            List<String> tokens = new ArrayList<>();
            if (!normalized.isEmpty()) {
                for (String token : normalized.split(" ")) {
                    if (!token.isBlank())
                        tokens.add(token);
                }
            }
            Collections.sort(tokens);
            return String.join(" ", tokens);
        }
        return normalized;
    }

    private static void appendClasses(StringBuilder line, FakeElement element) {
        if (element.classList instanceof FakeTokenList) {
            String classes = String.join(" ", ((FakeTokenList) element.classList).tokens());
            if (!classes.isBlank()) {
                line.append(" class=\"").append(classes).append("\"");
                return;
            }
        }
        if ((element.className != null) && !element.className.isBlank())
            line.append(" class=\"").append(element.className).append("\"");
    }

    private static void appendAttributes(StringBuilder line, Map<String, String> attributes) {
        if ((attributes == null) || attributes.isEmpty())
            return;
        attributes.forEach((name, value) -> line.append(" ").append(name).append("=\"").append(value).append("\""));
    }

    private static void appendStyles(StringBuilder line, FakeElement element) {
        if (element.style instanceof FakeStyleDeclaration) {
            Map<String, String> styles = ((FakeStyleDeclaration) element.style).values();
            if (!styles.isEmpty()) {
                line.append(" style=\"");
                boolean first = true;
                for (Map.Entry<String, String> entry : styles.entrySet()) {
                    if (!first)
                        line.append(" ");
                    line.append(entry.getKey()).append(": ").append(entry.getValue()).append(";");
                    first = false;
                }
                line.append("\"");
            }
        }
    }

    private static final class SelectorStep {

        private final SimpleSelector selector;
        private boolean directChild;

        private SelectorStep(SimpleSelector selector, boolean directChild) {
            this.selector = selector;
            this.directChild = directChild;
        }
    }

    private static final class SimpleSelector {

        private String tag;
        private String id;
        private final List<String> classes = new ArrayList<>();
        private final List<AttributeSelector> attributes = new ArrayList<>();
    }

    private static final class AttributeSelector {

        private final String name;
        private final String value;

        private AttributeSelector(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
