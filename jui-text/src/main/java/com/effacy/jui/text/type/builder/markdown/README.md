# Markdown parser

Parses markdown text by emitting structural events to an `IEventBuilder`. See the parent package README for the event model and builder interface.

## Usage

Configure optional parameters on `MarkdownParser` then call `parse()` with a builder:

```java
new MarkdownParser().parse(new DomBuilderBuilder(body)
    .topHeadingLevel(1)
    .semanticTags(true)
    .semanticLists(true),
content);
```

With configuration:

```java
new MarkdownParser()
    .variableResolver(name -> lookupValue(name))
    .parse(builder, content);
```

For `FormattedText` use the static convenience:

```java
FormattedText result = FormattedText.markdown("# Hello **world**");
```

### Configuration

| Method | Description |
|--------|-------------|
| `partial(boolean)` | Treat content as potentially incomplete (e.g. streaming). Unclosed format markers on the last line are treated as formatting. |
| `lineProcessor(Function)` | Transforms each line before parsing. |
| `variableResolver(Function)` | Maps variable names to replacement text. Returns `null` for unresolved variables, which pass through to the builder. |

## Supported syntax

### Block-level elements

| Syntax | Block type | Description |
|--------|------------|-------------|
| `# text` | H1 | Heading level 1 |
| `## text` | H2 | Heading level 2 |
| `### text` | H3 | Heading level 3 |
| `- text`, `* text`, `+ text` | NLIST | Unordered list item |
| `1. text` | OLIST | Ordered list item |
| `\| ... \| ... \|` | TABLE | Table |
| (plain text) | PARA | Paragraph |

Double newlines create paragraph breaks. Single newlines within a paragraph create line breaks within the same block.

#### Nested lists

Ordered and unordered lists can be nested using indentation (4 spaces or 1 tab per level). Blank lines between list items are permitted and do not restart numbering:

```markdown
1.  **First item:**
    *   Sub-item A
    *   Sub-item B

1.  **Second item:**
    *   Sub-item C
```

#### Tables

Header row, separator row (determines alignment), then body rows:

```
| Name  | Age |
|-------|----:|
| Alice | 30  |
```

Alignment: `:---` left (default), `:---:` centre, `---:` right. Cells support inline formatting but not block-level content.

### Inline formatting

| Syntax | Format | Description |
|--------|--------|-------------|
| `**text**` | BLD | Bold |
| `__text__` | BLD | Bold (alternative) |
| `***text***` | BLD + ITL | Bold italic |
| `*text*` | ITL | Italic |
| `_text_` | ITL | Italic (word boundaries only) |
| `~~text~~` | STR | Strikethrough |
| `` `text` `` | CODE | Inline code |
| `[label](url)` | A | Hyperlink |

Single underscores are only treated as italic at word boundaries — `some_variable_name` is left as plain text.

### Variables

Placeholders resolved at parse time via `variableResolver()` or passed to the builder:

| Syntax | Description |
|--------|-------------|
| `{{name}}` | Simple variable |
| `{{name;key=value}}` | Variable with metadata |
| `{{name;k1=v1;k2=v2}}` | Variable with multiple metadata fields |

Variable names: letters, digits, `-`, `_`, `.`, `$`, `:`. Invalid syntax is left as literal text. Variables inside link labels are treated as literal text.
