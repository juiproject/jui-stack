# Markdown parser

This package provides a markdown parser that converts markdown text into `FormattedText` objects. The parser supports common markdown syntax including headings, lists, inline formatting, links, and variables.

## Usage

### Basic parsing

```java
FormattedText content = MarkdownParser.parse("""
    # Welcome

    This is a paragraph with **bold** and *italic* text.

    - First item
    - Second item
""");
```

### Multiple content blocks

Multiple strings can be passed, each separated as a new paragraph:

```java
FormattedText content = MarkdownParser.parse(
    "# Title",
    "First paragraph with **bold** text.",
    "Second paragraph with a [link](https://example.com)."
);
```

### Line pre-processing

Custom line processing can be applied during parsing:

```java
FormattedText content = MarkdownParser.parse(
    line -> line.trim().toUpperCase(),
    "# Title",
    "Content here"
);
```

## Supported syntax

### Block-level elements

| Syntax | Block type | Description |
|--------|------------|-------------|
| `# text` | H1 | Heading level 1 |
| `## text` | H2 | Heading level 2 |
| `### text` | H3 | Heading level 3 |
| `- text` | NLIST | Unordered list item |
| `* text` | NLIST | Unordered list item |
| `+ text` | NLIST | Unordered list item |
| `1. text` | NLIST | Ordered list item |
| (plain text) | PARA | Paragraph |

Double newlines create paragraph breaks. Single newlines within a paragraph create line breaks within the same block.

### Inline formatting

| Syntax | Format type | Description |
|--------|-------------|-------------|
| `**text**` | BLD | Bold |
| `__text__` | BLD | Bold (alternative) |
| `*text*` | ITL | Italic |
| `_text_` | ITL | Italic (alternative) |
| `~~text~~` | STR | Strikethrough |
| `` `text` `` | CODE | Inline code |
| `[label](url)` | A | Hyperlink |

### Variables

Variables are placeholders that can be resolved at render time:

| Syntax | Description |
|--------|-------------|
| `{{name}}` | Simple variable reference |
| `{{name;key=value}}` | Variable with single metadata field |
| `{{name;k1=v1;k2=v2}}` | Variable with multiple metadata fields |

Example:

```java
FormattedText content = MarkdownParser.parse("""
    Dear {{recipientName}},

    Your account balance is {{amount;format=currency;precision=2}}.
""");
```

#### Variable name rules

Variable names can contain:
- Letters (a-z, A-Z)
- Numbers (0-9)
- Dashes (`-`)
- Underscores (`_`)
- Periods (`.`)
- Dollar signs (`$`)
- Colons (`:`)

#### Metadata field name rules

Metadata field names can contain:
- Letters (a-z, A-Z)
- Numbers (0-9)
- Periods (`.`)

#### Metadata value rules

Metadata values can contain any character except:
- Newlines (`\n`, `\r`)
- Closing braces (`}`)

#### Invalid variable handling

Invalid variable syntax (e.g., spaces in name, invalid characters) is left as literal text in the output.

### Variables inside links

Variables inside link labels are treated as literal text and not parsed as variables:

```java
// The {{site-name}} is NOT parsed as a variable
FormattedText content = MarkdownParser.parse(
    "Visit [our site named {{site-name}}](https://example.com)"
);
// Output text: "Visit our site named {{site-name}}"
// Single link format, no variable format
```

## Design

The parser processes content in two phases:

1. **Block-level parsing**: Identifies structural elements (headings, lists, paragraphs) by examining line prefixes
2. **Inline parsing**: Processes formatting markers, links, and variables within each line

### Processing order

Within each line, elements are processed in this order:
1. Links `[label](url)` are identified first
2. Variables `{{name}}` are identified
3. Format markers (`**`, `*`, `~~`, `` ` ``) are identified
4. Markers within links are removed (formatting inside links is not supported)
5. Variables within links are removed (treated as literal text)
6. Markers within variables are removed
7. Elements are processed left-to-right to build the final formatted line

### Output structure

The parser produces a `FormattedText` object containing:
- `FormattedBlock` objects for each structural element
- `FormattedLine` objects within each block
- `Format` objects describing formatting regions within each line

Variables are represented as zero-length `Format` objects with metadata containing the variable name and any additional key-value pairs.
