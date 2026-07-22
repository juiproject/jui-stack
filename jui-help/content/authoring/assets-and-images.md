---
title: Using images and files
description: Where to put images and downloads, and how to reference them.
icon: image
order: 3
updated: Sample content
related: [markdown-reference]
---

Articles can include images and link to downloadable files. These are **hard
assets** — real files that live alongside your content.

## Where assets go

Put them in `content/assets/`. The build copies that folder into the site's
`assets/` alongside the pages, so the runtime can serve them. You can use
sub-folders to stay organised.

## Referencing an asset

Always reference an asset by a path starting with `assets/` — this resolves the
same whether the site is served or opened from disk.

An image:

```
![A diagram of the help system](assets/architecture.svg)
```

...renders as:

![A diagram of the help system](assets/architecture.svg)

A link to a downloadable file works the same way:

```
[Download the template](assets/template.txt)
```

> Keep images reasonably sized and prefer SVG for diagrams — it stays crisp and
> weighs almost nothing.
