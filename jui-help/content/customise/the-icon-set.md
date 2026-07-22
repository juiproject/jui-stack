---
title: The icon set
description: The reusable icons, and how to add your own.
icon: sparkles
order: 2
updated: Sample content
related: [theming]
---

The system ships with a small set of reusable line icons defined in
`public/icons.js`. They are inline SVG, so they inherit the current text colour
and scale cleanly — no image requests.

## Using an icon

Reference an icon by name in front matter, for example `icon: rocket` on a
category or article. The available names are the keys in `public/icons.js`.

## Adding an icon

Open `public/icons.js` and add an entry — a `24×24` SVG that strokes with
`currentColor`:

```
myicon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
        'stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">' +
        '<path d="M12 3v18M3 12h18"/></svg>',
```

Then use `icon: myicon` in any category or article. No rebuild is needed for
icon changes — they are part of the runtime, not the content bundle.
