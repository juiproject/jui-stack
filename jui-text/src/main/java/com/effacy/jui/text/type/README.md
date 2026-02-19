# Formatted text

This package provides a rich text formatting framework that captures and represents formatted content in a structured, hierarchical manner. It provides a flexible system for modelling text with multiple levels of formatting (bold, italic, underline, etc.), block-level structure (paragraphs, headings, lists), and metadata. The package supports multiple input formats, including markdown parsing, with full JSON serialisation support.

The key purpose of this package is to separate content from presentation concerns while maintaining formatting metadata alongside content.  This supports multiple input formats (plain strings, markdown) while enabling traversal and manipulation of formatted content. It also provides JSON serialisation for transport and persistence, allowing flexible reconstruction of formatted content for various output targets (HTML, PDF, UI renderers, etc.).

## Usage

### Creating formatted text from markdown

The simplest way to create formatted text is from markdown:

```java
FormattedText content = FormattedText.markdown("""
# Document Title

This is a paragraph with **bold** and *italic* text.

- First item
- Second item

[Click here](https://example.com) for more information.
""");
```

Multiple markdown blocks can be passed, each separated as a new paragraph. Double newlines within a single block also create paragraph breaks.

See [markdown/README.md](markdown/README.md) for full documentation on supported markdown syntax, including variable syntax rules and parsing behaviour.

### Creating formatted text programmatically

For finer control, build the structure directly:

```java
FormattedText doc = new FormattedText()
    .block(BlockType.H1, b -> b.line("Document Title"))
    .block(BlockType.PARA, b -> {
        b.line(l -> l.append("This is ")
                     .append("bold", FormatType.BLD)
                     .append(" and ")
                     .append("italic", FormatType.ITL)
                     .append(" text."));
    })
    .block(BlockType.NLIST, b -> b.line("First item"))
    .block(BlockType.NLIST, b -> b.line("Second item"));
```

### Using variables

Variables are placeholders that can be resolved at render time. They are supported both in markdown and programmatically.

In markdown, use double braces with optional metadata:

```java
FormattedText content = FormattedText.markdown("""
    Dear {{recipientName}},

    Your balance is {{amount;format=currency;precision=2}}.

    Visit [our site](https://example.com) for more information.
""");
```

Programmatically, use the `variable()` method:

```java
FormattedLine line = new FormattedLine()
    .append("Dear ")
    .variable("recipientName")
    .append(", your balance is ")
    .variable("amount", FormatType.BLD)
    .append(".");
```

When traversing content, check for variables using `TextSegment.variable()`:

```java
for (TextSegment segment : line.sequence()) {
    if (segment.variable()) {
        String varName = segment.text();  // Returns the variable name
        Map<String, String> meta = segment.meta();  // Additional metadata
        // Resolve and render the variable...
    } else {
        // Render normal text...
    }
}
```

### Using links

Links can be created in markdown or programmatically.

In markdown:

```java
FormattedText content = FormattedText.markdown(
    "Visit [our website](https://example.com) for details."
);
```

Programmatically, use the `link()` method:

```java
FormattedLine line = new FormattedLine()
    .append("Visit ")
    .link("our website", "https://example.com", FormatType.BLD)
    .append(" for details.");
```

When traversing, access link URLs via `TextSegment.link()`:

```java
for (TextSegment segment : line.sequence()) {
    String url = segment.link();  // null if not a link
    if (url != null) {
        // Render as hyperlink...
    }
}
```

### Tables

Tables are parsed from markdown and represented as a hierarchy of blocks: TABLE → TROW → TCELL. The TABLE block carries metadata for column count, header row count, and per-column alignment.

From markdown:

```java
FormattedText content = FormattedText.markdown("""
| Name  | Age |
|-------|----:|
| Alice | 30  |
| Bob   | 25  |
""");
```

Traversing the table structure:

```java
FormattedBlock table = content.getBlocks().get(0);
int columns = Integer.parseInt(table.meta("columns"));
String[] align = table.meta("align").split(","); // "L", "C", or "R" per column

for (FormattedBlock row : table.getBlocks()) {
    for (FormattedBlock cell : row.getBlocks()) {
        for (FormattedLine line : cell.getLines()) {
            // Process cell content with inline formatting...
        }
    }
}
```

Table metadata on the TABLE block:

| Key | Description |
|-----|-------------|
| `columns` | Number of columns (e.g. "3") |
| `headers` | Number of header rows (always "1") |
| `align` | Comma-separated alignment per column: `L` (left), `C` (centre), `R` (right) |

### Creating plain text paragraphs

For simple unformatted content:

```java
FormattedText text = FormattedText.string(
    "First paragraph.",
    "Second paragraph.",
    "Third paragraph."
);
```

Each string becomes a separate paragraph block.

### Traversing formatted content

To process the content for rendering:

```java
for (FormattedBlock block : content) {
    System.out.println("Block type: " + block.getType());
    for (FormattedLine line : block.getLines()) {
        // Process each segment with its formatting
        for (FormattedLine.TextSegment segment : line.sequence()) {
            String text = segment.text();
            FormatType[] formats = segment.formatting();
            String link = segment.link();
            // Render based on formatting...
        }
    }
}
```

Alternatively, use the visitor pattern:

```java
line.traverse((text, formats) -> {
    // Process each formatted region
    for (FormatType fmt : formats) {
        System.out.println("Format: " + fmt.name());
    }
});
```

### Extracting plain text

To flatten the content to unformatted text:

```java
String plainText = content.flatten();
```

This uses double newlines between blocks and single newlines between lines within a block.

### Debugging structure

To inspect the internal structure:

```java
System.out.println(content.debug());
```

This outputs a detailed tree showing blocks, lines, and formatting regions with their positions.

### Line pre-processing during markdown parsing

Custom line processing can be applied during parsing:

```java
FormattedText content = FormattedText.markdown(
    line -> line.trim().toUpperCase(),  // Pre-processor
    "# Title",
    "Content here"
);
```

## Feature catalogue

### Block types

| Type | Constraint | Description |
|------|------------|-------------|
| `PARA` | LINES | Paragraph (default block type) |
| `H1` | LINES | Heading level 1 |
| `H2` | LINES | Heading level 2 |
| `H3` | LINES | Heading level 3 |
| `NLIST` | LINES | List item (numbered or unordered) |
| `EQN` | CONTENT_AND_LINES | Equation (content holds source, lines for caption) |
| `DIA` | CONTENT_AND_LINES | Diagram (content holds source, lines for caption) |
| `TABLE` | BLOCKS | Table (child blocks are TROW rows) |
| `TROW` | BLOCKS | Table row (child blocks are TCELL cells) |
| `TCELL` | LINES_OR_BLOCKS | Table cell (lines for simple content, blocks for rich content) |

### Block type constraints

Each block type declares a `BlockTypeConstraint` that describes what content it may hold:

| Constraint | Description |
|------------|-------------|
| `LINES` | Lines only |
| `BLOCKS` | Child blocks only |
| `LINES_OR_BLOCKS` | Either lines or blocks but not both |
| `CONTENT` | Raw content string only |
| `CONTENT_AND_LINES` | Content string and/or lines |

### Inline format types

| Type | Description | Markdown syntax |
|------|-------------|-----------------|
| `BLD` | Bold | `**text**` or `__text__` |
| `ITL` | Italic | `*text*` or `_text_` |
| `UL` | Underline | (programmatic only) |
| `STR` | Strikethrough | `~~text~~` |
| `SUP` | Superscript | (programmatic only) |
| `SUB` | Subscript | (programmatic only) |
| `CODE` | Inline code | `` `text` `` |
| `HL` | Highlight | (programmatic only) |
| `A` | Anchor/link | `[label](url)` |

### Block operations

| Operation | Description |
|-----------|-------------|
| `insert(start, text)` | Insert text at position |
| `remove(start, len)` | Remove text range |
| `split(idx)` | Split block at index, returning the right portion |
| `merge(other)` | Merge another block into this one |
| `transform(type)` | Convert to a different block type |
| `clone()` | Create a deep copy |
| `indent(level)` | Set indentation level (0-5) |
| `meta(name, value)` | Set metadata on the block |

### Line operations

| Operation | Description |
|-----------|-------------|
| `append(text, formats...)` | Append text with optional formatting |
| `link(text, url, formats...)` | Append a hyperlink with optional additional formatting |
| `variable(name, formats...)` | Append a variable placeholder with optional formatting |
| `insert(start, text)` | Insert text at position |
| `remove(start, len)` | Remove text range |
| `split(idx)` | Split line at index, returning the right portion |
| `merge(other)` | Merge another line into this one |
| `stripFormatting()` | Remove all formatting |
| `clone()` | Create a deep copy |
| `sequence()` | Break into contiguous text segments |
| `traverse(visitor)` | Visit each formatted region |

## Design

The package implements a hierarchical composition pattern. The primary levels are text → block → line → format, with blocks optionally nesting child blocks for compound structures like tables:

```
FormattedText (document container)
  └── FormattedBlock[] (structural elements)
       ├── FormattedLine[] (content lines)
       │    └── Format[] (inline formatting regions)
       ├── FormattedBlock[] (child blocks, e.g. TABLE → TROW → TCELL)
       └── String content (raw content, e.g. equation source)
```

Each block type declares a `BlockTypeConstraint` that describes which of lines, blocks, and content it supports. This constraint guides validation and user interfaces.

Details follow:

1. **FormattedText**
   The root container that holds a collection of `FormattedBlock` objects. It implements `Iterable<FormattedBlock>` for easy traversal and provides factory methods for creating content from markdown or plain strings. The class is JSON-serialisable.
2. **FormattedBlock**
   Represents a block-level structural element such as a paragraph, heading, list item, or table. Each block has a type (`BlockType`), an optional list of `FormattedLine` objects, an optional list of child `FormattedBlock` objects, an optional raw content string, an optional indentation level (0-5), and optional metadata. Which of these properties are populated is governed by the block type's `BlockTypeConstraint`. Blocks support operations for splitting, merging, and transforming.
3. **FormattedLine**
   Represents a single line of text with inline formatting applied to specific character ranges. Formatting is stored as non-overlapping regions (`Format` objects) in increasing order by index. The `TextSegment` inner class provides a convenient way to access contiguous blocks of text with homogeneous formatting.
4. **Format**
   Describes a contiguous region of text with applied formatting. Stores the starting index, length, array of format types, and optional metadata (e.g., `link` for anchor formatting). Zero-length formats with `variable` metadata represent variable placeholders that are resolved at render time.

### Design invariants

1. **Non-overlapping formatting**
   Format regions in a line never overlap and are maintained in increasing index order
2. **Consistent text/format coupling**
   When text is inserted or removed, all affected format objects are adjusted accordingly
3. **Type-safe transforms**
   Operations like `transform()` strip formatting from block types that don't support it (e.g., headings)
4. **Bounded indentation**
   Block indentation is clamped to 0-5 levels
5. **Null safety**
   Collections return empty rather than null; operations handle null gracefully
6. **Content constraint adherence**
   Each block type declares which content properties (lines, child blocks, raw content) it supports via `BlockTypeConstraint`; producers should respect these constraints
7. **Lazy initialisation**
   Collections are initialised on-demand to minimise memory footprint for empty content
