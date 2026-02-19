# Markdown DOM handlers

This package contains `IMarkdownEventHandler` implementations that render markdown parsing events directly into DOM — either via JUI declarative builders or the Elemental2 API. Both handlers produce the same DOM structure and support the same configuration options.

## Handlers

| Handler | DOM API | Typical use |
|---------|---------|-------------|
| `DomBuilderMarkdownHandler` | JUI `IDomInsertableContainer` builders | Component/fragment `build()` methods |
| `Elemental2MarkdownHandler` | Elemental2 `Element` / `DomGlobal` | Streaming/incremental rendering |

Use `DomBuilderMarkdownHandler` when building DOM declaratively inside a JUI component or fragment. Use `Elemental2MarkdownHandler` when content arrives incrementally (e.g. streamed LLM responses) and needs to be rendered directly into a live DOM element.

## Usage

### DomBuilder (declarative)

Inside a component or fragment build method:

```java
DomBuilderMarkdownHandler handler = new DomBuilderMarkdownHandler(parent)
    .topHeadingLevel(3)
    .semanticTags(true)
    .semanticLists(true);
MarkdownParser.parse(handler, markdownContent);
```

### Elemental2 (imperative / streaming)

For streaming scenarios where content is re-parsed on each chunk:

```java
Elemental2MarkdownHandler handler = new Elemental2MarkdownHandler(rootEl)
    .topHeadingLevel(3)
    .semanticTags(true)
    .semanticLists(true);

// On each streamed chunk:
rootEl.innerHTML = "";
handler = new Elemental2MarkdownHandler(rootEl)
    .semanticTags(true)
    .semanticLists(true);
MarkdownEventParser.parse(handler, true, accumulatedContent);
```

## Configuration

All options are set via fluent setters and apply identically to both handlers.

### `topHeadingLevel(int level)`

Maps markdown `# H1` to `<h{level}>`, `## H2` to `<h{level+1}>`, `### H3` to `<h{level+2}>`, capped at `<h6>`. Default is `1` (no offset).

### `semanticTags(boolean)`

When enabled, inline formats with a semantic HTML equivalent use the proper tag instead of `<span>` with a CSS class:

| Format | Semantic tag | CSS fallback |
|--------|-------------|--------------|
| Bold | `<strong>` | `<span class="fmt_bold">` |
| Italic | `<em>` | `<span class="fmt_italic">` |
| Strikethrough | `<s>` | `<span class="fmt_strike">` |
| Code | `<code>` | `<span class="fmt_code">` |
| Subscript | `<sub>` | `<span class="fmt_subscript">` |
| Superscript | `<sup>` | `<span class="fmt_superscript">` |
| Underline | `<u>` | `<span class="fmt_underline">` |
| Highlight | *(none)* | `<span class="fmt_highlight">` |

Formats without a semantic equivalent (e.g. highlight) always fall back to `<span>` with a CSS class regardless of this setting. The tag-to-format mapping is defined in `FormattedTextStyles.SEMANTIC_TAGS`.

### `semanticLists(boolean)`

Controls how `NLIST` blocks are rendered:

- **Disabled (default)**: Each list item is a `<p>` element with CSS classes `block` and `list_bullet`. A `::before` pseudo-element provides the bullet character. Indent levels are represented by CSS classes (`indent1`, `indent2`, etc.).

- **Enabled**: List items use `<ul>` / `<li>` elements with structural nesting based on indent level. This produces copy-paste-friendly HTML that preserves list structure when pasted into word processors.

The nesting is managed via deferred attachment: `<li>` elements are created detached in `startBlock(NLIST)` and attached to the correct `<ul>` in `startLine()` once the indent level is known from `meta("indent", N)`.

## DOM structure

### Block mapping

| BlockType | Element | CSS classes |
|-----------|---------|-------------|
| `PARA` | `<p>` | `block` |
| `NLIST` | `<p>` (default) or `<li>` (semantic) | `block list_bullet` (default only) |
| `H1` | `<h{N}>` | `block` |
| `H2` | `<h{N+1}>` | `block` |
| `H3` | `<h{N+2}>` | `block` |
| `TABLE` | `<table>` | |
| `TROW` | `<tr>` | |
| `TCELL` | `<th>` (header rows) or `<td>` | `block` |

Block CSS classes come from `FormattedTextStyles.BLOCK_STYLES`. Indent levels add an `indentN` class to the block element.

### Table alignment

Column alignment from `meta("align", "L,C,R")` is applied as inline `text-align` styles on `<th>` / `<td>` elements. Left alignment is the default and receives no style.

### Line breaks

Multiple lines within a single block are separated by `<br>` elements. The first line in a block does not produce a `<br>`.

### Links

External links (`http...`) receive `target="_blank"`. Internal links are rendered as plain `<a>` elements with the href.

## CSS requirements

The caller should apply the `FormattedTextStyles.styles().standard()` CSS class to the root/parent element. This provides:

- Block spacing (`.block`)
- Indent margins (`.indent1` through `.indent3`)
- List bullet pseudo-elements (`.list_bullet`)
- Inline format styles (`.fmt_bold`, `.fmt_italic`, `.fmt_code`, etc.)
- Heading sizes (`h1` through `h5`)
