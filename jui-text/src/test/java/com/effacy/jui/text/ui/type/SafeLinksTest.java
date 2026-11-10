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
package com.effacy.jui.text.ui.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.test.AbstractJUITest;
import com.effacy.jui.core.client.test.CssStub;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeAnchor;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeElement;
import com.effacy.jui.core.client.test.DomTestNodeFactory.FakeText;
import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine;
import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;
import com.effacy.jui.text.ui.fragment.FLine;
import com.effacy.jui.text.ui.type.builder.DomBuilderBuilder;

import elemental2.dom.Node;

/**
 * Tests {@link SafeLinks}, both the rule itself and its application by the
 * formatted text renderers ({@link DomBuilderFormattedTextRenderer},
 * {@link FLine} and {@link DomBuilderBuilder}; see also
 * {@code Elemental2BuilderTest}) and by {@link LinkHandlers}.
 */
public class SafeLinksTest extends AbstractJUITest {

    @AfterEach
    public void reset() {
        SafeLinks.reset();
        FormattedTextStyles.styles(null);
    }

    /************************************************************************
     * The rule.
     ************************************************************************/

    @Test
    public void safe_schemes() {
        assertTrue(SafeLinks.isSafe("http://example.com"));
        assertTrue(SafeLinks.isSafe("https://example.com/a?b=c#d"));
        assertTrue(SafeLinks.isSafe("mailto:a@b.com"));
        assertTrue(SafeLinks.isSafe("tel:+61 2 1234 5678"));
    }

    @Test
    public void safe_mixed_case_and_whitespace() {
        assertTrue(SafeLinks.isSafe("HTTPS://example.com"));
        assertTrue(SafeLinks.isSafe("MailTo:a@b.com"));
        assertTrue(SafeLinks.isSafe("  https://example.com  "));
        assertTrue(SafeLinks.isSafe("ht\ttps://example.com"));
    }

    @Test
    public void safe_no_scheme() {
        assertTrue(SafeLinks.isSafe(""));
        assertTrue(SafeLinks.isSafe("#anchor"));
        assertTrue(SafeLinks.isSafe("/relative/path"));
        assertTrue(SafeLinks.isSafe("relative/path"));
        assertTrue(SafeLinks.isSafe("../up"));
        assertTrue(SafeLinks.isSafe("?query=1"));
        assertTrue(SafeLinks.isSafe("//example.com/path"));
        // A colon after a path, query or fragment character is not a scheme.
        assertTrue(SafeLinks.isSafe("/a:b"));
        assertTrue(SafeLinks.isSafe("#a:b"));
        assertTrue(SafeLinks.isSafe("?a=b:c"));
        assertTrue(SafeLinks.isSafe("java script:alert(1)"));
        assertTrue(SafeLinks.isSafe("javascript%3Aalert(1)"));
    }

    @Test
    public void refused_schemes() {
        assertFalse(SafeLinks.isSafe(null));
        assertFalse(SafeLinks.isSafe("javascript:alert(1)"));
        assertFalse(SafeLinks.isSafe("javascript://example.com/%0Aalert(1)"));
        assertFalse(SafeLinks.isSafe("vbscript:msgbox(1)"));
        assertFalse(SafeLinks.isSafe("data:text/html,<script>alert(1)</script>"));
        assertFalse(SafeLinks.isSafe("file:///etc/passwd"));
        assertFalse(SafeLinks.isSafe("ftp://example.com"));
        assertFalse(SafeLinks.isSafe("doc:3"));
    }

    @Test
    public void refused_mixed_case() {
        assertFalse(SafeLinks.isSafe("JavaScript:alert(1)"));
        assertFalse(SafeLinks.isSafe("JAVASCRIPT:alert(1)"));
        assertFalse(SafeLinks.isSafe("VbScRiPt:msgbox(1)"));
        assertFalse(SafeLinks.isSafe("DATA:text/html,x"));
    }

    @Test
    public void refused_embedded_whitespace_and_controls() {
        assertFalse(SafeLinks.isSafe(" javascript:alert(1)"));
        assertFalse(SafeLinks.isSafe("javascript:alert(1) "));
        assertFalse(SafeLinks.isSafe("java\tscript:alert(1)"));
        assertFalse(SafeLinks.isSafe("java\nscript:alert(1)"));
        assertFalse(SafeLinks.isSafe("java\rscript:alert(1)"));
        assertFalse(SafeLinks.isSafe("javascript\t:alert(1)"));
        assertFalse(SafeLinks.isSafe("\u0000javascript:alert(1)"));
        assertFalse(SafeLinks.isSafe("\u0001\u001F javascript:alert(1)"));
        assertFalse(SafeLinks.isSafe("\tj\na\rv\ta\nscript:alert(1)"));
    }

    @Test
    public void scheme() {
        assertEquals("javascript", SafeLinks.scheme(" Java\tScript:x"));
        assertEquals("https", SafeLinks.scheme("HTTPS://x"));
        assertEquals("svn+ssh", SafeLinks.scheme("svn+ssh://x"));
        assertNull(SafeLinks.scheme("/a:b"));
        assertNull(SafeLinks.scheme(":x"));
        assertNull(SafeLinks.scheme("1abc:x"));
        assertNull(SafeLinks.scheme(null));
    }

    @Test
    public void safe() {
        assertEquals("https://x", SafeLinks.safe("https://x"));
        assertNull(SafeLinks.safe("javascript:x"));
    }

    @Test
    public void isExternal() {
        assertTrue(SafeLinks.isExternal("http://x"));
        assertTrue(SafeLinks.isExternal(" HTTPS://x"));
        assertTrue(SafeLinks.isExternal("mailto:a@b"));
        assertTrue(SafeLinks.isExternal("tel:1"));
        assertFalse(SafeLinks.isExternal("javascript://x"));
        assertFalse(SafeLinks.isExternal("/x"));
        assertFalse(SafeLinks.isExternal("#x"));
        assertFalse(SafeLinks.isExternal(null));
    }

    @Test
    public void allow() {
        assertFalse(SafeLinks.isSafe("doc:3"));
        SafeLinks.allow("DOC");
        assertTrue(SafeLinks.isSafe("doc:3"));
        assertTrue(SafeLinks.isSafe("Doc:3"));
        assertFalse(SafeLinks.isExternal("doc:3"));
        assertFalse(SafeLinks.isSafe("javascript:x"));
        SafeLinks.reset();
        assertFalse(SafeLinks.isSafe("doc:3"));
    }

    @Test
    public void allow_refuses_dangerous_schemes() {
        assertThrows(IllegalArgumentException.class, () -> SafeLinks.allow("javascript"));
        assertThrows(IllegalArgumentException.class, () -> SafeLinks.allow("VBScript"));
        assertThrows(IllegalArgumentException.class, () -> SafeLinks.allow("data"));
        assertThrows(IllegalArgumentException.class, () -> SafeLinks.allow("not a scheme"));
        assertThrows(IllegalArgumentException.class, () -> SafeLinks.allow((String) null));
        assertFalse(SafeLinks.isSafe("javascript:x"));
    }

    /************************************************************************
     * Link handlers.
     ************************************************************************/

    @Test
    public void linkHandlers_isExternal() {
        assertFalse(LinkHandlers.isExternal("javascript://example.com/%0Aalert(1)"));
        assertFalse(LinkHandlers.isExternal("ftp://example.com"));
        assertTrue(LinkHandlers.isExternal("HTTPS://example.com"));
    }

    @Test
    public void linkHandlers_standard_refuses() {
        // Refused addresses are handled (so the browser doesn't follow them) without
        // being opened (there is no window here, so opening one would fail) or being
        // passed to the fallback.
        boolean[] consulted = { false };
        ILinkHandler handler = LinkHandlers.standard((url, anchor, root) -> {
            consulted[0] = true;
            return false;
        });
        assertTrue(handler.activate("javascript:alert(1)", null, null));
        assertTrue(handler.activate("javascript://example.com/%0Aalert(1)", null, null));
        assertTrue(handler.activate(" JavaScript:alert(1)", null, null));
        assertTrue(handler.activate("java\tscript:alert(1)", null, null));
        assertTrue(handler.activate("vbscript:msgbox(1)", null, null));
        assertTrue(handler.activate("data:text/html,x", null, null));
        assertFalse(consulted[0]);
        assertTrue(LinkHandlers.standard().activate("javascript:alert(1)", null, null));
    }

    @Test
    public void linkHandlers_standard_fallback() {
        // A relative link, and an allowed custom scheme, go to the fallback.
        List<String> seen = new ArrayList<>();
        ILinkHandler handler = LinkHandlers.standard((url, anchor, root) -> {
            seen.add(url);
            return true;
        });
        assertTrue(handler.activate("/relative", null, null));
        SafeLinks.allow("doc");
        assertTrue(handler.activate("doc:3", null, null));
        assertEquals(List.of("/relative", "doc:3"), seen);
    }

    /************************************************************************
     * Renderers.
     ************************************************************************/

    private static final String[] REFUSED = {
        "javascript:alert(1)",
        "JavaScript:alert(1)",
        " javascript:alert(1)",
        "java\tscript:alert(1)",
        "java\nscript:alert(1)",
        "vbscript:msgbox(1)",
        "data:text/html,x"
    };

    @Test
    public void renderer_inline_link() {
        FakeElement root = render(FormattedText.markdown("Click [here](javascript:alert%281%29) now"));
        assertTrue(anchors(root).isEmpty());
        assertEquals("Click here now", text(root));
    }

    @Test
    public void renderer_inline_link_mixed_case_and_tabs() {
        FakeElement root = render(FormattedText.markdown("[a](JaVaScRiPt:alert%281%29) [b](java\tscript:alert%281%29) [c](vbscript:msgbox%281%29) [d](data:text/html,x)"));
        assertTrue(anchors(root).isEmpty());
        assertEquals("a b c d", text(root));
    }

    @Test
    public void renderer_inline_link_formatted() {
        // A link carrying other formatting keeps the formatting, without the link.
        FormattedText text = new FormattedText();
        text.block(BlockType.PARA, b -> b.line(l -> l.link("bold", "javascript:alert(1)", FormattedLine.FormatType.BLD)));
        FakeElement root = render(text);
        assertTrue(anchors(root).isEmpty());
        assertEquals("bold", text(root));
    }

    @Test
    public void renderer_autolink() {
        // The parser does not recognise autolinks: they are drawn as text and never
        // as a link.
        FakeElement root = render(FormattedText.markdown("<javascript:alert(1)>"));
        assertTrue(anchors(root).isEmpty());
        assertTrue(text(root).contains("javascript:alert(1)"));
    }

    @Test
    public void renderer_reference_link() {
        // The parser does not recognise reference links: they are drawn as text and
        // never as a link.
        FakeElement root = render(FormattedText.markdown("[x][r]", "[r]: javascript:alert(1)"));
        assertTrue(anchors(root).isEmpty());
    }

    @Test
    public void renderer_model_links() {
        // However the link got into the model (editor, paste, programmatic) it is
        // refused when drawn.
        for (String url : REFUSED) {
            FormattedText text = new FormattedText();
            text.block(BlockType.PARA, b -> b.line(l -> l.append("before ").link("label", url).append(" after")));
            FakeElement root = render(text);
            assertTrue(anchors(root).isEmpty(), url);
            assertEquals("before label after", text(root), url);
        }
    }

    @Test
    public void renderer_safe_links() {
        FakeElement root = render(FormattedText.markdown("[a](https://example.com) [b](mailto:a@b.com) [c](tel:123) [d](/path) [e](#anchor)"));
        List<FakeAnchor> anchors = anchors(root);
        assertEquals(5, anchors.size());
        assertEquals("https://example.com", anchors.get(0).attributes().get("href"));
        assertEquals("_blank", anchors.get(0).attributes().get("target"));
        assertEquals("mailto:a@b.com", anchors.get(1).attributes().get("href"));
        assertEquals("tel:123", anchors.get(2).attributes().get("href"));
        assertEquals("/path", anchors.get(3).attributes().get("href"));
        assertEquals("#anchor", anchors.get(4).attributes().get("href"));
    }

    @Test
    public void renderer_allowed_scheme() {
        SafeLinks.allow("doc");
        FakeElement root = render(FormattedText.markdown("[a](doc:3)"));
        List<FakeAnchor> anchors = anchors(root);
        assertEquals(1, anchors.size());
        assertEquals("doc:3", anchors.get(0).attributes().get("href"));
    }

    @Test
    public void fline_refuses() {
        FormattedTextStyles.styles(CssStub.of(FormattedTextStyles.IFormattedTextCSS.class));
        for (String url : REFUSED) {
            FormattedLine line = new FormattedLine().append("before ").link("label", url).append(" after");
            FakeElement root = build(new FLine(line)).element();
            assertTrue(anchors(root).isEmpty(), url);
            assertEquals("before label after", text(root), url);
        }
    }

    @Test
    public void fline_safe() {
        FormattedTextStyles.styles(CssStub.of(FormattedTextStyles.IFormattedTextCSS.class));
        FormattedLine line = new FormattedLine().link("label", "https://example.com");
        FakeElement root = build(new FLine(line)).element();
        List<FakeAnchor> anchors = anchors(root);
        assertEquals(1, anchors.size());
        assertEquals("https://example.com", anchors.get(0).attributes().get("href"));
    }

    @Test
    public void domBuilderBuilder_refuses() {
        for (String url : REFUSED) {
            // A newline ends the markdown line, so can't be carried by a markdown link.
            if (url.contains("\n"))
                continue;
            String md = "before [label](" + url.replace("(", "%28").replace(")", "%29") + ") after";
            FakeElement root = build(Div.$().$(d -> new MarkdownParser().parse(new DomBuilderBuilder(d), md))).element();
            assertTrue(anchors(root).isEmpty(), url);
            assertEquals("before label after", text(root), url);
        }
        FakeElement root = build(Div.$().$(d -> new MarkdownParser().parse(new DomBuilderBuilder(d), "[label](https://example.com)"))).element();
        assertEquals(1, anchors(root).size());
    }

    /************************************************************************
     * Support.
     ************************************************************************/

    private FakeElement render(FormattedText text) {
        return build(Div.$().$(d -> new DomBuilderFormattedTextRenderer(d).render(text))).element();
    }

    private static List<FakeAnchor> anchors(Node node) {
        List<FakeAnchor> anchors = new ArrayList<>();
        collect(node, anchors);
        return anchors;
    }

    private static void collect(Node node, List<FakeAnchor> anchors) {
        if (node instanceof FakeAnchor) {
            anchors.add((FakeAnchor) node);
            for (Node child : ((FakeAnchor) node).children())
                collect(child, anchors);
        } else if (node instanceof FakeElement) {
            for (Node child : ((FakeElement) node).children())
                collect(child, anchors);
        }
    }

    private static String text(Node node) {
        StringBuilder sb = new StringBuilder();
        appendText(node, sb);
        return sb.toString();
    }

    private static void appendText(Node node, StringBuilder sb) {
        if (node instanceof FakeText) {
            sb.append(((FakeText) node).text());
        } else if (node instanceof FakeAnchor) {
            for (Node child : ((FakeAnchor) node).children())
                appendText(child, sb);
        } else if (node instanceof FakeElement) {
            for (Node child : ((FakeElement) node).children())
                appendText(child, sb);
        }
    }
}
