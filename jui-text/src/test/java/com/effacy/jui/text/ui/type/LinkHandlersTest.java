package com.effacy.jui.text.ui.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the pure (DOM-free) helpers on {@link LinkHandlers}.
 */
public class LinkHandlersTest {

    @Test
    public void slug_basic() {
        assertEquals("development", LinkHandlers.slug("Development"));
        assertEquals("operating-scenarios", LinkHandlers.slug("Operating scenarios"));
    }

    @Test
    public void slug_collapsesAndTrimsNonAlphanumerics() {
        assertEquals("hello-world", LinkHandlers.slug("Hello, World!"));
        assertEquals("multiple-spaces", LinkHandlers.slug("Multiple   spaces"));
        assertEquals("a-b", LinkHandlers.slug("A—B")); // em-dash between
        assertEquals("trim", LinkHandlers.slug("  Trim  "));
    }

    @Test
    public void slug_noLeadingOrTrailingHyphen() {
        assertEquals("hello", LinkHandlers.slug("!Hello!"));
        assertEquals("section-1", LinkHandlers.slug("Section 1."));
    }

    @Test
    public void slug_emptyAndNull() {
        assertEquals("", LinkHandlers.slug(""));
        assertEquals("", LinkHandlers.slug(null));
        assertEquals("", LinkHandlers.slug("   "));
        assertEquals("", LinkHandlers.slug("!!!"));
    }

    @Test
    public void isInPageAnchor() {
        assertTrue(LinkHandlers.isInPageAnchor("#foo"));
        assertTrue(LinkHandlers.isInPageAnchor("#"));
        assertFalse(LinkHandlers.isInPageAnchor("http://x"));
        assertFalse(LinkHandlers.isInPageAnchor("doc:3"));
        assertFalse(LinkHandlers.isInPageAnchor(""));
        assertFalse(LinkHandlers.isInPageAnchor(null));
    }

    @Test
    public void isExternal() {
        assertTrue(LinkHandlers.isExternal("http://example.com"));
        assertTrue(LinkHandlers.isExternal("https://example.com"));
        assertTrue(LinkHandlers.isExternal("mailto:a@b.com"));
        assertTrue(LinkHandlers.isExternal("tel:+123"));
        assertFalse(LinkHandlers.isExternal("#anchor"));
        assertFalse(LinkHandlers.isExternal("doc:3"));
        assertFalse(LinkHandlers.isExternal("/relative/path"));
        assertFalse(LinkHandlers.isExternal("relative"));
        assertFalse(LinkHandlers.isExternal(null));
    }
}
