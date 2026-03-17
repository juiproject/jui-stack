package com.effacy.jui.text.type.builder.markdown;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedText;
import com.effacy.jui.text.type.builder.FormattedTextBuilder;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;

/**
 * Reproduction tests for known parser edge cases.
 */
public class MarkdownParserIssuesTest {

    /**
     * Issue 4: Triple asterisk (bold+italic) produces garbled output.
     * <p>
     * Expected: "bold italic" rendered as both bold and italic.
     * Actual: markers overlap and produce unexpected formatting.
     */
    @Test
    public void testIssue4_tripleAsterisk() {
        String markdown = "This is ***bold italic*** text";
        FormattedText result = parse(markdown);
        System.out.println("Issue 4 - Triple asterisk:");
        System.out.println("  Input:  " + markdown);
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    /**
     * Issue 4 variant: mixed bold and italic in same line.
     */
    @Test
    public void testIssue4_boldInsideItalic() {
        String markdown = "This is *italic and **bold** inside* text";
        FormattedText result = parse(markdown);
        System.out.println("Issue 4 variant - Bold inside italic:");
        System.out.println("  Input:  " + markdown);
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    /**
     * Issue 5: Underscores in identifiers trigger spurious italic.
     * <p>
     * Expected: "some_variable_name" rendered as plain text.
     * Actual: "variable" between the underscores is rendered as italic.
     */
    @Test
    public void testIssue5_underscoresInIdentifiers() {
        String markdown = "Use the some_variable_name field";
        FormattedText result = parse(markdown);
        System.out.println("Issue 5 - Underscores in identifiers:");
        System.out.println("  Input:  " + markdown);
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    /**
     * Issue 5 variant: multiple underscored identifiers.
     */
    @Test
    public void testIssue5_multipleUnderscoreIdentifiers() {
        String markdown = "Set my_var and your_var to zero";
        FormattedText result = parse(markdown);
        System.out.println("Issue 5 variant - Multiple underscored identifiers:");
        System.out.println("  Input:  " + markdown);
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    /**
     * Issue 7: Two separate ordered lists merged into one.
     * <p>
     * Expected: two distinct numbered lists.
     * Actual: merged into a single continuous list.
     */
    @Test
    public void testIssue7_separateOrderedListsMerged() {
        String markdown = "First list:\n\n1. Alpha\n2. Beta\n\nSecond list:\n\n1. Gamma\n2. Delta";
        FormattedText result = parse(markdown);
        System.out.println("Issue 7 - Separate ordered lists with prose between:");
        System.out.println("  Input (escaped newlines): " + markdown.replace("\n", "\\n"));
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    /**
     * Issue 7 variant: two ordered lists back to back with only blank lines.
     */
    @Test
    public void testIssue7_backToBackOrderedLists() {
        String markdown = "1. First\n2. Second\n\n1. Alpha\n2. Beta";
        FormattedText result = parse(markdown);
        System.out.println("Issue 7 variant - Back to back ordered lists:");
        System.out.println("  Input (escaped newlines): " + markdown.replace("\n", "\\n"));
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    /**
     * Original nested list case: numbered items with blank lines between them
     * and indented bullet sub-items.
     * <p>
     * Expected: 3 ordered items, each with nested unordered sub-items.
     */
    @Test
    public void testNestedListWithBlankLines() {
        String markdown = """
                The following is a nested list of items:

                1.  **First item:** Here is a sub-item
                    *   **Subitem:** this is a sub-item.

                1.  **Second item:**
                    *   sub-item 1.
                    *   sub-item 2.
                    *   sub-item 3.

                1.  **Select Reviewees:**
                    *   sub-item 1.
                    *   sub-item 2.""";
        FormattedText result = parse(markdown);
        System.out.println("Nested list with blank lines:");
        System.out.println("  Output: " + result.debug());
        System.out.println();
    }

    private FormattedText parse(String markdown) {
        FormattedTextBuilder handler = new FormattedTextBuilder();
        new MarkdownParser().parse(handler, markdown);
        return handler.result();
    }
}
