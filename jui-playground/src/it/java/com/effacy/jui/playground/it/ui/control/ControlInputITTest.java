package com.effacy.jui.playground.it.ui.control;

import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.effacy.jui.playground.TestApplicationIT;
import com.effacy.jui.playground.it.AbstractIT;
import com.effacy.jui.test.HtmlUnitPage;
import com.effacy.jui.test.PageTester;
import com.effacy.jui.test.TextValidator;
import com.effacy.jui.test.suite.control.TextControlTester;

/**
 * Tests that text controls register values that arrive without key events (only
 * an {@code input} event), as happens with browser autofill, dictation,
 * drag-and-drop or a mouse paste.
 * <p>
 * Uses the controls under the "Test suite / Controls" tab of the playground
 * (see {@code ControlSuite}), each of which reflects its value (via its modified
 * handler) in an adjacent label.
 */
@SpringBootTest(classes = TestApplicationIT.class)
public class ControlInputITTest extends AbstractIT {

    private static final String SCOPE = "playgroundui.testing-controls.1.";

    @Test
    public void testInputOnly() throws Exception {
        webClient.getOptions ().setThrowExceptionOnScriptError (false);
        HtmlPage htmlPage = webClient.getPage ("http://localhost/playground?test=true");
        webClient.waitForBackgroundJavaScript (4000);
        HtmlUnitPage page = new HtmlUnitPage (htmlPage);

        PageTester.$ (htmlPage)
            .click ("playgroundui-tab_testing-controls", 1000)

            // Text control.
            .with (TextControlTester.$ (SCOPE + "text-input-1"), ctl -> {
                ctl.assignByInput ("autofilled");
                ctl.validateInput ("autofilled");
            })
            .validate (new TextValidator (() -> page.selectById ("text-input-1-modified").textContent (), "autofilled"))

            // Text area control (shares the input structure of the text control).
            .with (TextControlTester.$ (SCOPE + "textarea-input-1").subclass ("textareacontrol"), ctl -> {
                ctl.assignByInput ("dictated text");
                ctl.validateInput ("dictated text");
            })
            .validate (new TextValidator (() -> page.selectById ("textarea-input-1-modified").textContent (), "dictated text"));
    }
}
