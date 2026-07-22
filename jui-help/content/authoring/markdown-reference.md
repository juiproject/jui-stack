---
title: Markdown reference
description: Every formatting feature the renderer supports, with examples.
icon: code
order: 2
updated: Sample content
related: [add-an-article, assets-and-images]
---

This article doubles as a test page — it uses every feature the built-in
markdown renderer supports.

## Headings

Use `##` for a section (these appear in the table of contents) and `###` for a
sub-section.

### A sub-section

Body text sits under headings.

## Text

You can write **bold**, *italic*, and `inline code`. Link to
[another article](welcome) or to an [external site](https://example.com).

## Lists

Unordered:

- First item
- Second item
- Third item

Ordered:

1. Step one
2. Step two
3. Step three

## Quotes and callouts

A blockquote renders as a highlighted callout — good for tips and warnings:

> Rebuild after editing content, or run the watcher so it happens automatically.

## Code

Fenced code blocks are preserved verbatim:

```
node build.mjs --watch
```

## Tables

| Field | Required | Notes |
| --- | --- | --- |
| title | yes | The article heading |
| icon | no | Defaults to a document icon |
| related | no | A list of article slugs |

## Dividers

Use three dashes for a horizontal rule.

---

That is the whole feature set. Anything more exotic is intentionally left out to
keep the renderer small and predictable.
