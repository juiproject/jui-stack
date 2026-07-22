---
title: Add or edit an article
description: Create a markdown file, give it front matter, and rebuild.
icon: pencil
order: 1
updated: Sample content
related: [markdown-reference, assets-and-images]
---

An article is a single markdown file inside a category folder.

## 1. Create the file

Add a `.md` file under the category you want, for example
`content/getting-started/my-article.md`. The file name (without `.md`) becomes
the article's **slug** — the part in its URL — so keep it lowercase with hyphens.

## 2. Add front matter

Begin the file with a front-matter block. Only `title` is required.

```
---
title: My new article
description: One line shown in listings and search.
icon: book
order: 3
updated: 2025-05-01
related: [welcome, theming]
---

Your markdown content starts here.
```

| Field | Purpose |
| --- | --- |
| `title` | The article heading (required). |
| `description` | Shown in listings and matched by search. |
| `icon` | Icon name from the icon set (see *The icon set*). |
| `order` | Sort position within its category (lower first). |
| `updated` | A free-text "last updated" note. |
| `related` | Slugs of articles to link at the foot. |

## 3. Rebuild

Run the build so the runtime picks up your change:

```
node build.mjs
```

Or `node build.mjs --watch` to rebuild automatically as you save. Refresh the
page and your article is there.

> New category? Just make a new folder under `content/` and drop an optional
> `_category.md` in it for the category's title, icon and description.
