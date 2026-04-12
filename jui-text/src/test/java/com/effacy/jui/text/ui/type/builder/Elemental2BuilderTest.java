package com.effacy.jui.text.ui.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.builder.markdown.MarkdownParser;

import elemental2.dom.DOMTokenList;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Node;
import elemental2.dom.Text;

class Elemental2BuilderTest {

    @Test
    void buildsParagraphWithFormattingAndLink() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root);

        new MarkdownParser().parse(builder, "Hello **bold** [site](http://example.com)");

        FakeElement p = childElement(root, 0);
        assertEquals("p", p.tag());
        assertEquals("Hello ", childText(p, 0).text());

        FakeElement strong = childElement(p, 1);
        assertEquals("span", strong.tag());
        assertTrue(strong.hasClass("fmt_bold"));
        assertEquals("bold", childText(strong, 0).text());

        assertEquals(" ", childText(p, 2).text());

        FakeAnchor anchor = (FakeAnchor) childNode(p, 3);
        assertEquals("a", anchor.tag());
        assertEquals("http://example.com", anchor.href);
        assertEquals("_blank", anchor.target);
        assertEquals("site", childText(anchor, 0).text());
    }

    @Test
    void buildsInlineCodeSpan() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root);

        new MarkdownParser().parse(builder, "Use `yaml` here");

        FakeElement p = childElement(root, 0);
        assertEquals("p", p.tag());
        assertEquals("Use ", childText(p, 0).text());

        FakeElement code = childElement(p, 1);
        assertEquals("span", code.tag());
        assertTrue(code.hasClass("fmt_code"));
        assertEquals("yaml", childText(code, 0).text());

        assertEquals(" here", childText(p, 2).text());
    }

    @Test
    void buildsSemanticInlineCodeSpan() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root).semanticTags(true);

        new MarkdownParser().parse(builder, "Use `yaml` here");

        FakeElement p = childElement(root, 0);
        assertEquals("p", p.tag());
        assertEquals("Use ", childText(p, 0).text());

        FakeElement code = childElement(p, 1);
        assertEquals("code", code.tag());
        assertEquals("yaml", childText(code, 0).text());

        assertEquals(" here", childText(p, 2).text());
    }

    @Test
    void buildsSemanticListStructure() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root).semanticLists(true);

        new MarkdownParser().parse(builder, "- First\n- Second");

        FakeElement ul = childElement(root, 0);
        assertEquals("ul", ul.tag());

        FakeElement li1 = childElement(ul, 0);
        FakeElement li2 = childElement(ul, 1);
        assertEquals("li", li1.tag());
        assertEquals("li", li2.tag());
        assertEquals("First", childText(li1, 0).text());
        assertEquals("Second", childText(li2, 0).text());
    }

    @Test
    void buildsNestedSemanticListStructure() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root).semanticLists(true);

        new MarkdownParser().parse(builder, "- Parent\n   - Child");

        FakeElement ul = childElement(root, 0);
        assertEquals("ul", ul.tag());

        FakeElement parentLi = childElement(ul, 0);
        assertEquals("li", parentLi.tag());
        assertEquals("Parent", childText(parentLi, 0).text());

        FakeElement nestedUl = childElement(parentLi, 1);
        assertEquals("ul", nestedUl.tag());

        FakeElement childLi = childElement(nestedUl, 0);
        assertEquals("li", childLi.tag());
        assertEquals("Child", childText(childLi, 0).text());
    }

    @Test
    void buildsHeadingStructure() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root).topHeadingLevel(2);

        new MarkdownParser().parse(builder, "# Title");

        FakeElement h2 = childElement(root, 0);
        assertEquals("h2", h2.tag());
        assertEquals("Title", childText(h2, 0).text());
    }

    @Test
    void buildsTableStructure() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root);

        new MarkdownParser().parse(builder,
            "| Name | Age |\n" +
            "|:-----|----:|\n" +
            "| Alice | 30 |"
        );

        FakeElement table = childElement(root, 0);
        assertEquals("table", table.tag());

        FakeElement headerRow = childElement(table, 0);
        assertEquals("tr", headerRow.tag());
        FakeElement th1 = childElement(headerRow, 0);
        FakeElement th2 = childElement(headerRow, 1);
        assertEquals("th", th1.tag());
        assertEquals("th", th2.tag());
        assertEquals("Name", childText(th1, 0).text());
        assertEquals("Age", childText(th2, 0).text());
        assertEquals("right", th2.style.get("text-align"));

        FakeElement bodyRow = childElement(table, 1);
        assertEquals("tr", bodyRow.tag());
        FakeElement td1 = childElement(bodyRow, 0);
        FakeElement td2 = childElement(bodyRow, 1);
        assertEquals("td", td1.tag());
        assertEquals("td", td2.tag());
        assertEquals("Alice", childText(td1, 0).text());
        assertEquals("30", childText(td2, 0).text());
    }

    @Test
    void buildsFencedCodeBlockStructure() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root);

        new MarkdownParser().parse(builder, "```yaml\nalpha: 1\nbeta: 2\n```");

        FakeElement pre = childElement(root, 0);
        assertEquals("pre", pre.tag());
        assertTrue(pre.hasClass("block"));
        assertTrue(pre.hasClass("code_block"));

        FakeElement code = childElement(pre, 0);
        assertEquals("code", code.tag());
        assertEquals("yaml", code.attributes().get("data-lang"));
        assertTrue(code.hasClass("language-yaml"));
        assertEquals("alpha: 1\nbeta: 2", flattenText(code));
    }

    @Test
    void buildsFencedCodeBlockWithoutLanguage() {
        FakeElement root = new FakeElement("div");
        Elemental2Builder builder = new TestElemental2Builder(root);

        new MarkdownParser().parse(builder, "```\nline 1\nline 2\n```");

        FakeElement pre = childElement(root, 0);
        FakeElement code = childElement(pre, 0);
        assertEquals("code", code.tag());
        assertTrue(!code.attributes().containsKey("data-lang"));
        assertEquals("line 1\nline 2", flattenText(code));
    }

    private static FakeElement childElement(FakeElement parent, int index) {
        return (FakeElement) parent.children().get(index);
    }

    private static Node childNode(FakeElement parent, int index) {
        return parent.children().get(index);
    }

    private static FakeText childText(FakeElement parent, int index) {
        return (FakeText) parent.children().get(index);
    }

    private static FakeText childText(FakeAnchor parent, int index) {
        return (FakeText) parent.children().get(index);
    }

    private static String flattenText(FakeElement element) {
        StringBuilder text = new StringBuilder();
        for (Node child : element.children()) {
            if (child instanceof FakeText)
                text.append(((FakeText) child).text());
            else if (child instanceof FakeElement)
                text.append(flattenText((FakeElement) child));
        }
        return text.toString();
    }

    private static class TestElemental2Builder extends Elemental2Builder {

        TestElemental2Builder(FakeElement root) {
            super(root);
        }

        @Override
        public TestElemental2Builder semanticLists(boolean semanticLists) {
            super.semanticLists(semanticLists);
            return this;
        }

        @Override
        protected Element createElement(String tag) {
            if ("a".equals(tag))
                return new FakeAnchor(tag);
            return new FakeElement(tag);
        }

        @Override
        protected Node createTextNode(String text) {
            return new FakeText(text);
        }

        @Override
        protected void setAttribute(Element element, String name, String value) {
            if (element instanceof FakeElement)
                ((FakeElement) element).attributes().put(name, value);
            else if (element instanceof FakeAnchor)
                ((FakeAnchor) element).attributes().put(name, value);
        }
    }

    private static class FakeTokenList extends DOMTokenList {
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
    }

    private static class FakeElement extends HTMLElement {
        private final String tag;
        private final List<Node> children = new ArrayList<>();
        private final Map<String, String> attributes = new LinkedHashMap<>();

        FakeElement(String tag) {
            this.tag = tag;
            this.tagName = tag;
            this.nodeName = tag;
            this.classList = new FakeTokenList();
            this.style = new FakeStyleDeclaration();
        }

        String tag() {
            return tag;
        }

        List<Node> children() {
            return children;
        }

        Map<String, String> attributes() {
            return attributes;
        }

        boolean hasClass(String name) {
            return ((FakeTokenList) classList).contains(name);
        }

        @Override
        public Node appendChild(Node child) {
            children.add(child);
            child.parentNode = this;
            if (child instanceof Element)
                child.parentElement = this;
            return child;
        }

    }

    private static class FakeAnchor extends HTMLAnchorElement {
        private final String tag;
        private final List<Node> children = new ArrayList<>();
        private final Map<String, String> attributes = new LinkedHashMap<>();

        FakeAnchor(String tag) {
            this.tag = tag;
            this.tagName = tag;
            this.nodeName = tag;
            this.classList = new FakeTokenList();
            this.style = new FakeStyleDeclaration();
        }

        String tag() {
            return tag;
        }

        List<Node> children() {
            return children;
        }

        Map<String, String> attributes() {
            return attributes;
        }

        @Override
        public Node appendChild(Node child) {
            children.add(child);
            child.parentNode = this;
            if (child instanceof Element)
                child.parentElement = this;
            return child;
        }
    }

    private static class FakeText extends Text {
        FakeText(String text) {
            super(text);
            this.textContent = text;
            this.nodeValue = text;
            this.nodeName = "#text";
        }

        String text() {
            return textContent;
        }
    }

    private static class FakeStyleDeclaration extends elemental2.dom.CSSStyleDeclaration {
        private final Map<String, String> values = new LinkedHashMap<>();

        @Override
        public void set(String property, String value) {
            values.put(property, value);
        }

        @Override
        public String get(String property) {
            return values.get(property);
        }
    }
}
