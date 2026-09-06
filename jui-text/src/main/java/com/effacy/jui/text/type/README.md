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

### Comparing two documents

`FormattedTextDiff` compares two documents block by block and returns the difference as a third document: the new document with the old one's departed blocks put back in place, each changed block carrying a `diff` meta-data value. That document renders — hand it to `FText` (or `DomBuilderFormattedTextRenderer`) and the changes are marked in the document as it reads, rather than in the markdown behind it.

```java
FormattedTextDiff.Result diff = FormattedTextDiff.diff(published, working);
if (!diff.identical())
    FText.$(el, diff.document()).contentStyle(ContentStyle.document());
```

| | |
|---|---|
| `document()` | the diff document (read-only — see below) |
| `added()` / `removed()` | blocks present on one side only |
| `changed()` | blocks that were edited (marked inline) |
| `identical()` | whether the two compared equal |

**Blocks** are matched on their markdown (with type and indent), which is the identity a document persists with — block ids do not survive a markdown round trip, so matching has to be by content. A consequence worth knowing is that a change of *formatting* alone (bolding a word, re-pointing a link) is a change, which is intended. The alignment is a longest-common-subsequence over those keys, so blocks separated by an insertion still match.

**Within a block**, a removed block and an added block that stand together and are recognisably the same block edited — same type, no child blocks, and more than half their words in common — are merged into one block carrying `changed`, in which the words that went are marked `FormatType.DEL` and those that came `FormatType.INS`. So a reworded sentence reads as a sentence with a word struck through beside the word that replaced it, not as a paragraph replaced.

The same alignment runs at three levels: blocks in a document, then lines in a merged block, then words in a merged line. Words are runs of letters and digits; whitespace and punctuation are separate tokens, so appending to a sentence marks the words appended and not the full stop that was already there. Formatting travels with the word — an inserted **bold** word is marked inserted *and* stays bold — and a link, an image or a variable is atomic: it matches or it does not, and is never split.

What it does not do:

- **A block with children (a `TABLE`) is compared whole.** One altered cell is the old table removed and the new one added.
- **Two blocks below the pairing threshold are not merged.** A paragraph rewritten from scratch is a removal and an addition, which reads better than a block in which every word is marked.
- **Moves are not detected.** A block that moved is a removal and an addition.

The returned document is for reading only: it interleaves content from two versions, so serialising it back to markdown — or handing it to the editor — would produce a document that never existed. It carries no block ids for the same reason. (The inline marks have no markdown form and are shed by the serializer, as `CMT` is, so a diff document that escapes into a save loses its marking rather than persisting it.)

The renderer turns the block meta into the `diff_added` / `diff_removed` / `diff_changed` content classes and the inline marks into `fmt_insert` / `fmt_delete`, all styled by the formatted-text stylesheet: a tint and a left bar on the block (neutral where it was edited, since the marks inside carry the colour), removed text struck through and inserted text underlined, so the two are told apart without relying on colour. The `--jui-richtext-diff-*` custom properties retheme it.

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
| `CMT` | Comment anchor (expects `comment` metadata referencing the associated comment; may be ignored by renderers) | (programmatic only) |
| `A` | Anchor/link | `[label](url)` |
| `INS` | Inserted, as marked by a comparison (`FormattedTextDiff`); no markdown form, shed on serialisation | (programmatic only) |
| `DEL` | Removed, as marked by a comparison; no markdown form, shed on serialisation | (programmatic only) |

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

### Inline images (atomic sentinel segments)

An inline image is an **atomic, single-character segment**: the line text carries one U+FFFC OBJECT REPLACEMENT CHARACTER (`FormattedLine.IMAGE_SENTINEL`) covered by a length-1 `FormatType.IMG` format. All image attributes are metadata on the format:

| Meta key | Purpose |
|---|---|
| `src` (`META_IMAGE`) | the image source URL |
| `alt` (`META_ALT`) | alt text (never stored as span text) |
| `width` / `height` (`META_WIDTH` / `META_HEIGHT`) | size in pixels |
| `align` (`META_ALIGN`) | block alignment: `left`, `center`, `right` |
| `margin` (`META_MARGIN`) | margin in pixels (all sides; alignment overrides the auto side) |

The sentinel exists only in the in-memory model — the markdown serializer strips it and the parser inserts it, so persisted markdown is plain `![alt](src){width=… height=… align=… margin=…}`.

**Never model an image as a zero-length format.** A zero-length format at index N and a caret at offset N are the same coordinate, so "before the image" and "after the image" would be indistinguishable — insert, delete, split, and caret placement would all have to guess. Giving the image extent makes offset N unambiguously *before* and N+1 *after*, so:

- deleting the sentinel character deletes the image (backspace after it / forward-delete before it), with no special-casing;
- typed text lands on the caret's side of the image, and the IMG format is never extended by adjacent insertion (it is atomic, like variables);
- splitting between a character and an image moves the image to the balance line by ordinary span arithmetic;
- an image-only line has length 1, so emptiness checks need no exceptions.

(Variables still use the legacy zero-length convention; the zero-length special cases in `remove`/`insert`/`split` exist for them.)

When redistributing formats in structural operations (`split`, `merge`), **copy-construct** (`new Format(format)`) and adjust index/length — the `(index, length, formats)` constructor silently drops metadata, which destroys an image's `src` or a link's `href`.

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
