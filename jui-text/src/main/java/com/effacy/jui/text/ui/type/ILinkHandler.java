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
package com.effacy.jui.text.ui.type;

import elemental2.dom.Element;

/**
 * A pluggable strategy for what happens when a link in formatted text is activated
 * (clicked). It is the single link-behaviour hook shared by the editor (in NAVIGATE
 * mode) and the read-only renderer / {@code FText}, so a link behaves identically
 * wherever the content is presented.
 * <p>
 * A default that suits most applications — external links open in a new tab, in-page
 * {@code #anchor} links scroll within the content (and never reach a single-page-app
 * hash router), and other schemes are delegated to the application — is provided by
 * {@link LinkHandlers#standard()}. Compose your own scheme handling on top with
 * {@link LinkHandlers#standard(ILinkHandler)}.
 *
 * @see LinkHandlers
 * @see LinkSupport
 */
@FunctionalInterface
public interface ILinkHandler {

    /**
     * Activates a link.
     *
     * @param url
     *              the link target (the anchor's {@code href}); may be {@code null}.
     * @param anchor
     *              the {@code <a>} element that was activated (context; may be {@code null}).
     * @param root
     *              the content root the link lives in — the scope for resolving in-page
     *              anchors (may be {@code null}, in which case the whole document is the scope).
     * @return {@code true} if the link was handled (the caller then suppresses the browser's
     *         default navigation); {@code false} to let the browser follow the {@code href}.
     */
    boolean activate(String url, Element anchor, Element root);
}
