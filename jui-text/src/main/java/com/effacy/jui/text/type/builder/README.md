# Event builder

This package defines the `IEventBuilder<T>` interface and implementations that build structured output from a stream of parsing events. Parsers (such as `MarkdownParser`) emit events to a builder; the builder produces a typed result.

## Architecture

```
Parser ──events──► IEventBuilder<T> ──result()──► T
```

A parser calls `commence()` to signal the start of a parse, emits a series of structural events, then calls `result()` to retrieve the output. The builder is stateful — `commence()` resets it for reuse across multiple parses.

## Builders

| Builder | Result type | Package |
|---------|-------------|---------|
| `FormattedTextBuilder` | `FormattedText` | `type.builder` |
| `DomBuilderBuilder` | `IDomInsertableContainer<?>` | `ui.type.builder` |
| `Elemental2Builder` | `Element` | `ui.type.builder` |

`FormattedTextBuilder` produces a `FormattedText` model. The DOM builders (`DomBuilderBuilder` and `Elemental2Builder`) render directly into a DOM container — see the `ui.type.builder` package README for their configuration and DOM structure.

## Event lifecycle

```
commence()                         ← reset state for new parse
startBlock(PARA)
  startLine()
    text("Hello ")
    formatted("bold", BLD)
    text(" world")
  endLine()
endBlock(PARA)
result() → T                       ← retrieve built output
```

### Methods

| Method | When called |
|--------|-------------|
| `commence()` | Before parsing starts. Default no-op; override to reset state for reuse. |
| `startBlock(BlockType)` | A block opens. |
| `endBlock(BlockType)` | A block closes (paired with `startBlock`). |
| `meta(String, String)` | Block metadata, emitted between `startBlock` and the first child. |
| `startLine()` | A line opens within a block. |
| `endLine()` | A line closes (paired with `startLine`). |
| `text(String)` | Plain text segment within a line. |
| `formatted(String, FormatType...)` | Formatted text segment. Multiple format types can be combined (e.g. BLD + ITL for bold italic). |
| `link(String, String)` | Hyperlink (label, URL). |
| `variable(String, Map)` | Variable placeholder (name, metadata map). |
| `result()` | Returns the built result after all events have been emitted. |

### Block types

| BlockType | Description |
|-----------|-------------|
| `PARA` | Paragraph |
| `H1`, `H2`, `H3` | Headings |
| `NLIST` | Unordered list item |
| `OLIST` | Ordered list item |
| `TABLE` | Table (contains TROW children) |
| `TROW` | Table row (contains TCELL children) |
| `TCELL` | Table cell |

### Block metadata

| Block | Key | Value | Description |
|-------|-----|-------|-------------|
| `TABLE` | `columns` | Integer string | Number of columns |
| `TABLE` | `headers` | Integer string | Number of header rows (always "1") |
| `TABLE` | `align` | Comma-separated L/C/R | Per-column alignment |
| `NLIST`/`OLIST` | `indent` | Integer string | Nesting depth (0-based, omitted for depth 0) |

### Format types

| FormatType | Description |
|------------|-------------|
| `BLD` | Bold |
| `ITL` | Italic |
| `STR` | Strikethrough |
| `CODE` | Inline code |
| `UL` | Underline |
| `SUP` | Superscript |
| `SUB` | Subscript |
| `HL` | Highlight |
| `A` | Anchor/link (carries `link` metadata) |

# Usage

Implement `IEventBuilder<T>` and track the block/line nesting. `FormattedTextBuilder` serves as the reference implementation. Key points:

- `commence()` should reset all state so the builder can be reused.
- `meta()` is always called between `startBlock` and the first child — use it to capture block-level configuration before content arrives.
- `formatted()` receives varargs `FormatType...` to support combined formats (e.g. bold italic from `***text***`).
- Variables are emitted as `variable(name, meta)`. The metadata map contains the variable name under the `FormattedLine.META_VARIABLE` key plus any additional key-value pairs from the `{{name;key=value}}` syntax.
