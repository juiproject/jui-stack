# Markdown parser

This package provides a markdown parser that converts markdown text into `FormattedText` objects or emits structural events to an `IMarkdownEventHandler`. The parser supports common markdown syntax including headings, lists, tables, inline formatting, links, and variables.

## Architecture

The parser is built around an event-driven model:

```
                           ┌─ FormattedTextMarkdownHandler ─→ FormattedText
MarkdownEventParser ──────►├─ DomBuilderMarkdownHandler    ─→ JUI DOM builders
                           └─ Elemental2MarkdownHandler    ─→ Elemental2 DOM
```

`MarkdownEventParser` is the core parser that emits structural events (block start/end, line start/end, text, formatted text, links, variables) to any `IMarkdownEventHandler` implementation. This separation allows different output representations from the same parsing logic.

`MarkdownParser` is a convenience facade that delegates to `MarkdownEventParser` and provides both model-building and event-handler entry points.

### Classes

| Class | Role |
|-------|------|
| `IMarkdownEventHandler` | Event interface — receives parsing events |
| `MarkdownEventParser` | Core parser — emits events to a handler |
| `FormattedTextMarkdownHandler` | Handler that builds a `FormattedText` model |
| `MarkdownParser` | Convenience facade for common use cases |

## Usage

### Building a FormattedText model

```java
FormattedText content = MarkdownParser.parse("""
    # Welcome

    This is a paragraph with **bold** and *italic* text.

    - First item
    - Second item
""");
```

Multiple strings can be passed, each separated as a new paragraph:

```java
FormattedText content = MarkdownParser.parse(
    "# Title",
    "First paragraph with **bold** text.",
    "Second paragraph with a [link](https://example.com)."
);
```

### Using an event handler

Pass an `IMarkdownEventHandler` to receive events directly without building an intermediate model. This is useful for rendering into DOM or other targets:

```java
IMarkdownEventHandler handler = new MyCustomHandler();
MarkdownParser.parse(handler, "# Title\n\nSome **bold** text.");
```

Or use `MarkdownEventParser` directly:

```java
MarkdownEventParser.parse(handler, "# Title\n\nSome **bold** text.");
```

### Partial mode (streaming)

When content arrives incrementally (e.g. from a streaming LLM response), partial mode treats unclosed format markers on the last line as formatting rather than literal text:

```java
// Model-building:
FormattedText content = MarkdownParser.parse(true, incompleteContent);

// Event-handler:
MarkdownParser.parse(handler, true, incompleteContent);
```

The caller typically accumulates the raw stream, clears the output, and re-parses the full buffer on each chunk.

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
| `\| ... \| ... \|` | TABLE | Table (see below) |
| (plain text) | PARA | Paragraph |

Double newlines create paragraph breaks. Single newlines within a paragraph create line breaks within the same block.

#### Tables

Standard markdown tables are supported. A table requires a header row followed by a separator row, then zero or more body rows:

```
| Name  | Age |
|-------|----:|
| Alice | 30  |
| Bob   | 25  |
```

The separator row determines column alignment using colons:

| Separator | Alignment |
|-----------|-----------|
| `---` or `:---` | Left (default) |
| `:---:` | Centre |
| `---:` | Right |

Cell contents support full inline formatting (bold, italic, code, links, etc.) but not block-level content such as nested paragraphs or lists.

The parsed result is a TABLE block containing TROW child blocks, each containing TCELL child blocks. The TABLE block carries metadata: `columns` (column count), `headers` (header row count, always "1"), and `align` (comma-separated L/C/R per column). If a row has fewer cells than the column count, empty cells are padded. Extra cells beyond the column count are ignored.

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

## Event model

The `IMarkdownEventHandler` interface receives a balanced, hierarchical stream of events:

```
startBlock(PARA)
  startLine()
    text("Hello ")
    formatted("bold", BLD)
    text(" world")
  endLine()
endBlock(PARA)
```

For tables the hierarchy nests deeper:

```
startBlock(TABLE)
  meta("columns", "2")
  meta("headers", "1")
  meta("align", "L,R")
  startBlock(TROW)
    startBlock(TCELL)
      startLine()
        text("Name")
      endLine()
    endBlock(TCELL)
  endBlock(TROW)
endBlock(TABLE)
```

### Event methods

| Method | When emitted |
|--------|-------------|
| `startBlock(BlockType)` | Block opens |
| `endBlock(BlockType)` | Block closes (paired with `startBlock`) |
| `meta(String, String)` | Block metadata (between `startBlock` and first child) |
| `startLine()` | Line opens within a block |
| `endLine()` | Line closes (paired with `startLine`) |
| `text(String)` | Plain text segment within a line |
| `formatted(String, FormatType)` | Formatted text segment (one format type per call) |
| `link(String, String)` | Hyperlink (label, URL) |
| `variable(String, Map)` | Variable placeholder (name, metadata) |

### Implementing a handler

The `FormattedTextMarkdownHandler` serves as the reference implementation. A minimal handler needs only to track the block/line nesting and process the inline content events. The `meta` event delivers block-level metadata (table alignment, list indent) between `startBlock` and the first child element.

## Design

The parser processes content in two phases:

1. **Block-level parsing**: Identifies structural elements (headings, tables, lists, paragraphs) by examining line prefixes and structure
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
- For tables: nested `FormattedBlock` hierarchy (TABLE -> TROW -> TCELL), where each TCELL contains `FormattedLine` objects with inline formatting

Variables are represented as zero-length `Format` objects with metadata containing the variable name and any additional key-value pairs.
