# DOM builders

This package contains `IEventBuilder` implementations that render parsing events directly into DOM — either via JUI declarative builders or the Elemental2 API. Both produce the same DOM structure and support the same configuration options.

## Builders

| Builder | DOM API | Typical use |
|---------|---------|-------------|
| `DomBuilderBuilder` | JUI `IDomInsertableContainer` | Component/fragment `build()` methods |
| `Elemental2Builder` | Elemental2 `Element` / `DomGlobal` | Streaming/incremental rendering |

Use `DomBuilderBuilder` when building DOM declaratively inside a JUI component or fragment. Use `Elemental2Builder` when content arrives incrementally (e.g. streamed LLM responses) and needs to be rendered directly into a live DOM element.

## Usage

### DomBuilder (declarative)

```java
new MarkdownParser().parse(new DomBuilderBuilder(parent)
    .topHeadingLevel(3)
    .semanticTags(true)
    .semanticLists(true),
markdownContent);
```

### Elemental2 (imperative / streaming)

```java
// On each streamed chunk:
rootEl.innerHTML = "";
new MarkdownParser()
    .partial(true)
    .parse(new Elemental2Builder(rootEl)
        .semanticTags(true)
        .semanticLists(true),
    accumulatedContent);
```

## Configuration

All options are set via fluent setters and apply identically to both builders.

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

When multiple format types are combined (e.g. bold + italic from `***text***`), semantic tags are nested (e.g. `<strong><em>text</em></strong>`).

### `semanticLists(boolean)`

Controls how list blocks are rendered:

- **Disabled (default)**: Each list item is a `<p>` element with CSS classes `block` and `list_bullet`. A `::before` pseudo-element provides the bullet character. Indent levels are represented by CSS classes (`indent1`, `indent2`, etc.).

- **Enabled**: List items use `<ol>` / `<ul>` / `<li>` elements with structural nesting based on indent level. Mixed ordered/unordered nesting is supported (e.g. `<ol><li>...<ul><li>...</li></ul></li></ol>`). This produces copy-paste-friendly HTML that preserves list structure.

## DOM structure

### Block mapping

| BlockType | Element | CSS classes |
|-----------|---------|-------------|
| `PARA` | `<p>` | `block` |
| `NLIST` | `<p>` (default) or `<li>` (semantic) | `block list_bullet` (default only) |
| `OLIST` | `<p>` (default) or `<li>` (semantic) | `block list_number` (default only) |
| `H1` | `<h{N}>` | `block` |
| `H2` | `<h{N+1}>` | `block` |
| `H3` | `<h{N+2}>` | `block` |
| `TABLE` | `<table>` | |
| `TROW` | `<tr>` | |
| `TCELL` | `<th>` (header rows) or `<td>` | `block` |

### Table alignment

Column alignment from `meta("align", "L,C,R")` is applied as inline `text-align` styles on `<th>` / `<td>` elements.

### Line breaks

Multiple lines within a single block are separated by `<br>` elements.

### Links

External links (`http...`) receive `target="_blank"`. Internal links are rendered as plain `<a>` elements.

## CSS requirements

Apply `FormattedTextStyles.styles().standard()` to the root element. This provides block spacing, indent margins, list bullets, inline format styles, and heading sizes.
