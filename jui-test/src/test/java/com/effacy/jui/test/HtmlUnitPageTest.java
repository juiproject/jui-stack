/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
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
package com.effacy.jui.test;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Assertions;

import com.effacy.jui.test.IPage.INode;

public class HtmlUnitPageTest {

    /**
     * Page that records (as attributes on the body) the key and input events
     * received from the input and textarea.
     */
    private static final String HTML = "<html><body>"
        + "<input type=\"text\" test-id=\"input\">"
        + "<textarea test-id=\"textarea\"></textarea>"
        + "<script>"
        + "var counts = { keydown: 0, keypress: 0, keyup: 0, input: 0 };"
        + "['input', 'textarea'].forEach (function (tag) {"
        + "  var el = document.querySelector (tag);"
        + "  Object.keys (counts).forEach (function (type) {"
        + "    el.addEventListener (type, function () {"
        + "      counts[type]++;"
        + "      document.body.setAttribute ('data-' + type, '' + counts[type]);"
        + "      document.body.setAttribute ('data-last', el.value);"
        + "    });"
        + "  });"
        + "});"
        + "</script>"
        + "</body></html>";

    /**
     * Assigning by input should set the value and raise only an input event (no
     * key events), mimicking autofill, dictation, drag-and-drop or a mouse paste.
     */
    @org.junit.jupiter.api.Test
    public void test_assignValueByInput() throws Exception {
        try (WebClient client = new WebClient (BrowserVersion.CHROME)) {
            HtmlPage htmlPage = client.loadHtmlCodeIntoCurrentWindow (HTML);
            HtmlUnitPage page = new HtmlUnitPage (htmlPage);

            INode input = page.selectById ("input");
            Assertions.assertTrue (input.assignValueByInput ("autofilled"));
            Assertions.assertEquals ("autofilled", input.value ());
            Assertions.assertEquals ("1", htmlPage.getBody ().getAttribute ("data-input"));
            Assertions.assertEquals ("autofilled", htmlPage.getBody ().getAttribute ("data-last"));

            INode textarea = page.selectById ("textarea");
            Assertions.assertTrue (textarea.assignValueByInput ("dictated"));
            Assertions.assertEquals ("2", htmlPage.getBody ().getAttribute ("data-input"));
            Assertions.assertEquals ("dictated", htmlPage.getBody ().getAttribute ("data-last"));

            // No key events should have been generated.
            Assertions.assertFalse (htmlPage.getBody ().hasAttribute ("data-keydown"));
            Assertions.assertFalse (htmlPage.getBody ().hasAttribute ("data-keypress"));
            Assertions.assertFalse (htmlPage.getBody ().hasAttribute ("data-keyup"));
        }
    }
}
