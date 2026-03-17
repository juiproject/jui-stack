package com.effacy.jui.playground.ui.samples;

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Pre;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;
import com.effacy.jui.text.ui.type.builder.DomBuilderBuilder;

public class Markdown extends SimpleComponent {

    public Markdown() {
        renderer(root -> {
            Div.$(root).css("margin: 2em 0; padding: 1em 1.25em; background: #f1f1f1; border-radius: 0.75rem;").$(body -> {
                Pre.$(body).text(TEXT1);
            });
            Div.$(root).css("margin: 2em 0;").$(body -> {
                MarkdownParser.parse(p -> {}, new DomBuilderBuilder(body)
                    .topHeadingLevel(1)
                    .semanticTags(true)
                    .semanticLists(true), TEXT1);
            });
            Div.$(root).css("margin: 2em 0; padding: 1em 1.25em; background: #f1f1f1; border-radius: 0.75rem;").$(body -> {
                Pre.$(body).text(TEXT2);
            });
            Div.$(root).css("margin: 2em 0;").$(body -> {
                MarkdownParser.parse(p -> {
                    p.variableResolver(name -> {
                        if ("name".equals(name))
                            return "Jill Jones";
                        if ("email".equals(name))
                            return "jill.jones@example.com";
                        return null;
                    });
                }, new DomBuilderBuilder(body)
                        .topHeadingLevel(1)
                        .semanticTags(true)
                        .semanticLists(true), TEXT2);
            });
        });
    }

    public static final String TEXT1 = """
The following is a nested list of items:

1.  **First item:** Here is a sub-item
    *   **Subitem:** this is a sub-item.
    
1.  **Second item:**
    *   sub-item 1.
    *   sub-item 2.
    *   sub-item 3.
    
1.  **Select Reviewees:**
    *   sub-item 1.
    *   sub-item 2.    
    """;

    public static final String TEXT2 = """
Hello {{name}},

This is also being emailed to you at {{email}}.
    """;
}
