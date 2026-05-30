/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.platform.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.linker.CrossSiteIframeLinker;

/**
 * A {@link CrossSiteIframeLinker} that permits the Super Dev Mode redirect to
 * operate when the application page is served over {@code https:} behind a port
 * forwarder (most notably GitHub Codespaces).
 * <p>
 * The stock GWT linker bakes two guards into the application bootstrap
 * ({@code .nocache.js}) that between them block the dev-mode redirect in a
 * Codespace:
 * <ol>
 * <li>The "redirect hook permitted" expression only allows {@code http:} and
 * {@code file:} pages (see
 * {@link CrossSiteIframeLinker#getJsDevModeRedirectHookPermitted}). In a
 * Codespace the application is served over {@code https:}, so the guard is
 * {@code false} and the redirect block is skipped entirely &mdash; the page
 * silently loads its pre-compiled standalone permutation instead of the code
 * server's output.</li>
 * <li>Even with the redirect permitted, the dev-mode URL is validated against
 * {@code devModeUrlWhitelistRegexp}, whose default only matches
 * {@code http://localhost} / {@code http://127.0.0.1}. The Codespaces code
 * server URL ({@code https://<codespace>-9876.app.github.dev}) does not match,
 * so the redirect URL is discarded.</li>
 * </ol>
 * Behind the Codespaces forwarder the code server is reachable over
 * {@code https:} as well (both ports are forwarded as HTTPS), so permitting the
 * {@code https:} page to redirect is safe; the whitelist below still constrains
 * which URLs are honoured. This mirrors the HTTPS handling already present in
 * the code server's {@code dev_mode_on.js} bookmarklet gating.
 * <p>
 * An explicitly configured {@code devModeUrlWhitelistRegexp} continues to take
 * precedence &mdash; only the <em>default</em> is broadened here.
 */
@LinkerOrder(Order.PRIMARY)
@Shardable
public class JuiCrossSiteIframeLinker extends CrossSiteIframeLinker {

    /**
     * Default whitelist of dev-mode URLs. Permits the local code server
     * ({@code localhost} / {@code 127.0.0.1}) over either scheme as well as
     * GitHub Codespaces forwarded hosts (e.g.
     * {@code https://<codespace>-9876.app.github.dev/}). The leading
     * {@code ([\w-]+\.)*} permits the {@code <codespace>-<port>.} subdomain
     * label that Codespaces prepends.
     */
    private static final String DEFAULT_DEV_MODE_URL_WHITELIST_REGEXP =
        "https?://([\\w-]+\\.)*(localhost|127\\.0\\.0\\.1|app\\.github\\.dev|githubpreview\\.dev)(:\\d+)?/.*";

    @Override
    public String getDescription() {
        return "Cross-Site-Iframe (JUI; https/Codespaces dev-mode redirect)";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adds {@code https:} to the schemes for which the Super Dev Mode redirect
     * is permitted, in addition to the stock {@code http:} and {@code file:}.
     */
    @Override
    protected String getJsDevModeRedirectHookPermitted(LinkerContext context) {
        return "$wnd.location.protocol == \"http:\" || $wnd.location.protocol == \"https:\" "
            + "|| $wnd.location.protocol == \"file:\"";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reproduces the stock validation but with a broadened default whitelist
     * (see {@link #DEFAULT_DEV_MODE_URL_WHITELIST_REGEXP}). An explicitly
     * configured {@code devModeUrlWhitelistRegexp} still wins.
     */
    @Override
    protected String getJsDevModeUrlValidation(LinkerContext context) {
        String regexp = getStringConfigurationProperty(context, "devModeUrlWhitelistRegexp",
            DEFAULT_DEV_MODE_URL_WHITELIST_REGEXP);
        if (!regexp.isEmpty()) {
            return ""
                + "if (!/^" + regexp.replace("/", "\\/") + "$/.test(devModeUrl)) {\n"
                + "  if (devModeUrl && window.console && console.log) {\n"
                + "    console.log('Ignoring non-whitelisted Dev Mode URL: ' + devModeUrl);\n"
                + "  }\n"
                + "  devModeUrl = '';"
                + "}";
        }
        return "";
    }
}
