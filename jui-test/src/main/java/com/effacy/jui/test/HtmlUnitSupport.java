package com.effacy.jui.test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;
import org.htmlunit.util.WebConnectionWrapper;

public class HtmlUnitSupport {

    /**
     * Takes a web client and configures it to safely deal with JavaScript from JUI
     * that HTML unit has trouble parsing (cause it's a but outdated on the JS
     * front).
     * 
     * @param webClient
     *                  the web client to configure.
     * @return the configure web client.
     */
    public static WebClient configure(WebClient webClient) {
        webClient.setWebConnection(new JSInterceptor(webClient));
        return webClient;
    }

    /**
     * This intercepts JS that needs to be filtered.
     */
    public static class JSInterceptor extends WebConnectionWrapper {

        public JSInterceptor(WebClient webClient) {
            super(webClient);
        }

        @Override
        public WebResponse getResponse(WebRequest request) throws java.io.IOException {
            String url = request.getUrl().toString();

            // Trap jui_validation.js and return a version that the old HTMLUnit can deal
            // with.
            if (url.contains("jui_validation.js")) {
                String replacementScript = """
                    JUIValidationSupport = {};

                    JUIValidationSupport.isLetter = function(c) {
                        return /^[A-Za-z]$/.test(String.fromCharCode(c));
                    };
                """;
                byte[] body = replacementScript.getBytes(StandardCharsets.UTF_8);
                List<NameValuePair> headers = new ArrayList<>();
                headers.add(new NameValuePair("Content-Type", "application/javascript"));
                headers.add(new NameValuePair("Content-Length", String.valueOf(body.length)));
                return new WebResponse(
                    new WebResponseData( body, 200, "OK", headers),
                    request,
                    0
                );
            }

            // Default handling for other resources
            return super.getResponse(request);
        }
    }
}
