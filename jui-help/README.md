# Help system

A self-contained, standalone help centre. Content is authored as **markdown files
with front matter**; a tiny **zero-dependency build step** assembles it into a static
site that runs entirely in the browser. No backend, no database, no runtime
dependencies.

The build reads the sources in this folder and assembles the finished, servable site
into an output directory — by default a local **`dist/`** you can open right away. Point
it anywhere with `--out` (e.g. into a web app's static resources when embedding). Serve
the output from any static host, from your application's static path, or open it straight
off disk.

This folder ships with **sample content that is itself the documentation** for the
system — so the quickest way to learn it is to build it and browse (see *Quick start*).

---

## At a glance

Everything below is relative to the **help root** (this folder):

```
.                          ← the help root (this folder)
├── README.md              ← you are here
├── build.mjs              ← the build step (Node, no dependencies)
├── package.json           ← build / watch scripts
├── content/               ← YOU EDIT THIS — authored content
│   ├── config.md          ← title, tagline, category order, popular list
│   ├── <category>/
│   │   ├── _category.md    ← category metadata (title, icon, …)
│   │   └── <article>.md    ← an article: front matter + markdown body
│   └── assets/            ← images / files referenced by articles
└── public/                ← THE RUNTIME — static source, copied to the target at build
    ├── index.html         ← entry point
    ├── help.css           ← the single stylesheet (theme here)
    ├── icons.js           ← reusable icon set (inline SVG)
    ├── md.js              ← markdown renderer
    └── app.js             ← router + views
```

The build assembles everything into an output directory — by default `dist/` in the help
root (git-ignored; override with `--out`):

```
dist/                                                    ← default output (git-ignored)
├── index.html · help.css · icons.js · md.js · app.js   ← copied from public/
├── content.generated.js                                ← GENERATED (window.HELP)
└── assets/                                             ← copied from content/assets/
```

Note the split: `public/` is **pure source** — nothing is generated back into it.

## Quick start

Build the sample and open it — the sample content **is the documentation** for this
system (it covers authoring, theming and icons), so building and browsing it is the
fastest way to learn it.

### Build & browse

With **Node 16+**, from the help root:

```
node build.mjs            # builds to ./dist
open dist/index.html      # macOS — or just double-click the file
```

The output is self-contained and works straight from `file://`, so no server is needed
to browse it. `node build.mjs --watch` rebuilds as you edit; `npm run build` /
`npm run watch` do the same.

To serve it over HTTP instead (closer to how it would be hosted):

```
python3 -m http.server 8080 -d dist      # any static server → http://localhost:8080
```

Build somewhere else with `--out` — e.g. straight into a web app's static resources:

```
node build.mjs --out=/path/to/served/help
```

No local Node? Wire the build into your project's build instead — see *Build integration*.

---

## How it works (and why there is a build step)

The runtime is deliberately dumb: it reads one in-memory object, `window.HELP`, and
renders it. It never fetches files or lists directories — which is exactly what lets
it run from `file://` and from any dumb static host.

The build step is the bridge from *authoring* (many markdown files) to that single
object:

```
public/  (static runtime, source)   content/  (markdown + front matter + assets)
        \                                  /
         \        build.mjs               /   parse front matter, assemble,
          \   (copy public, generate,    /    copy public + assets
           ▼      copy assets)          ▼
                     dist/                (default output — open or serve this)
          ├── index.html · help.css · app.js · md.js · icons.js   (from public/)
          ├── content.generated.js   →   window.HELP = { config, categories, articles }
          └── assets/                →   copied from content/assets/
```

- **Front matter** (the `--- … ---` block at the top of each file) is parsed for
  metadata — title, icon, order, related, etc.
- **Article bodies stay as raw markdown** in the bundle and are rendered in the
  browser by `md.js`. This keeps the build trivial and the generated file
  human-readable. (If you ever want pre-rendered HTML instead, `md.js` also runs in
  Node, so the build could render at build time — but it is not needed.)
- **Assets** in `content/assets/` are copied to the target's `assets/`.
- **`public/` is not touched** — the runtime files are copied *out* of it into the
  target; nothing is generated back into it.

The build is a **single file, zero dependencies** — no `npm install`, no
`node_modules` (the `package.json` only defines the `build`/`watch` scripts).

### Build integration

`build.mjs` is a single file with zero dependencies, so it slots into any build — just
point `--out` at wherever you serve static files:

- **Directly / npm / Make / CI step:** `node build.mjs --out=<dir>`.
- **Maven, without requiring Node anywhere:** use `frontend-maven-plugin` to provision a
  pinned Node and run the build in a phase. Its `install-node-and-npm` goal downloads
  Node (point its `installDirectory` at the build directory so `mvn clean` removes it;
  the downloaded archive is cached under `~/.m2`, so a clean re-extracts without
  re-downloading), and its `npm` goal runs `run build`. Bind both to an early phase
  (e.g. `generate-resources`) and the site is built on every build — no Node needed on
  developer machines or CI runners.

The target is **build output** — regenerated by the build, and typically git-ignored
rather than committed.

---

## Authoring content

The sample content **is** the authoring guide — build and read it, especially the
*Authoring* and *Customise* categories. In short:

### A category

Make a folder under `content/`. Optionally add `_category.md`:

```
---
title: Getting started
icon: rocket
description: What this is and how to find your way around.
order: 1
---
```

### An article

Add a `.md` file in a category folder. The file name (minus `.md`) is its **slug**.

```
---
title: My article
description: One line shown in listings and search.
icon: book
order: 2
updated: 2025-05-01
related: [welcome, theming]
---

Your **markdown** content here.
```

Only `title` is required. `related` is a list of article slugs.

### Front-matter fields

| Scope | Field | Purpose |
| --- | --- | --- |
| config | `title` | Site title (header + tab). |
| config | `tagline` | Line under the hero heading. |
| config | `categoryOrder` | Category order, by folder name. |
| config | `popular` | Article slugs featured on the home page. |
| config | `backLabel` / `backUrl` | Optional "back to app" link (hidden if `backUrl` is empty). |
| category | `title`, `icon`, `description`, `order` | Category card. |
| article | `title` | Heading (required). |
| article | `description` | Listings + search. |
| article | `icon`, `order`, `updated`, `related` | Metadata. |

Front matter is a small YAML subset: `key: value`, where a value may be a string, a
number, a boolean, or an inline array `[a, b, "c"]`. One field per line.

### Supported markdown

Headings (with automatic table-of-contents ids), paragraphs, **bold**, *italic*,
`inline code`, links, images, ordered/unordered lists, `>` blockquotes (rendered as
callouts), fenced code blocks, tables, and `---` horizontal rules. See
*Markdown reference* in the sample content for a live example of every one.

Links to another article use its bare slug: `[see this](my-slug)` →
navigates within the help centre. Anything with a scheme, slash, or dot (e.g.
`https://…`, `assets/diagram.svg`) is left as a normal link.

### Images and downloads

Put files in `content/assets/` and reference them with a path starting `assets/`:

```
![A diagram](assets/architecture.svg)
[Download](assets/template.txt)
```

---

## Theming

All styling is in the single stylesheet `public/help.css`. The look is driven by CSS
custom properties at the top (`--brand`, `--fg`, `--bg`, `--surface`, `--font`, …) —
set these to your product's tokens and the whole centre follows. To go fully offline,
remove the Google Fonts `@import` at the top; the system-font fallbacks take over.

## Icons

`public/icons.js` holds a small reusable set of inline-SVG icons (they recolour with
the surrounding text, so no image requests). Reference one by name from front matter
(`icon: rocket`). Add your own by copying an entry — no rebuild needed, since icons
are part of the runtime, not the content bundle.

---

## Deploying

The **target** directory produced by the build is the entire, self-contained site.
Serve it however you serve static assets — copy it to a static host (S3, nginx, a CDN),
or, if `--out` points into your application's static resources, it ships and is served
with the app. Nothing else is required at runtime.

To reuse the system in another project, copy this whole folder, swap in your `content/`,
and run the build with `--out` pointing at your served location — directly, or wired into
your build (see *Build integration*).

## Standalone guarantee

Beyond the optional web-font `@import` in `help.css`, the running site makes **no
network requests** — all content, styles, scripts and icons are local static files.
Remove that `@import` and it is fully offline and works from `file://`.
