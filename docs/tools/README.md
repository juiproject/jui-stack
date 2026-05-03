# Theme tools

Static HTML inspection tools for working with the JUI theme system. Open
each file directly in a browser — no build step.

The tools are not part of the JUI distribution. They reach into
`jui-stack/jui-ui/src/main/resources/com/effacy/jui/ui/client/Theme.*.css`
via relative paths so the values they show stay in sync with the source.

## theme-guide.html

Browseable reference for every token in the JUI theme stack.

Sections:

- **Palette axes** — the source-of-truth tunable knobs (hues, chroma peaks).
- **Reference ramps** — 10-tint scales for each colour family.
- **Role tokens** — semantic surface, text, border, interactive, feedback,
  focus, disabled, and selection.
- **Typography** — font families, size scale (with rendered px equivalent),
  weights, line heights.
- **Spacing** — `--jui-space-*` visualised as bars proportional to value.
- **Radii** — `--jui-radius-*` rendered as squares.
- **Elevation** — `--jui-elevation-*` shadow stack.
- **Motion** — `--jui-duration-*` and `--jui-ease-*`.
- **Components** — Button, Dialog, Form, Table — visual replicas (not the
  actual JUI components) consuming the comp-layer tokens, alongside a
  per-component table of every `--jui-comp-*` token they depend on.

### How it works

The four JUI theme stylesheets (`Reference`, `Scale`, `Role`, `Component`)
are loaded via `<link>`. The `<main class="theme">` wrapper carries the
`.theme` class so the JUI-scoped declarations cascade into the swatches
without affecting the page chrome above.

A small inline script reads `getComputedStyle(themeRoot)` for every
listed token and prints the resolved value alongside the swatch. Numbers
shown for sizes (e.g. `1rem · 16px`) include both the raw token value and
the rendered px equivalent at the current root font-size.

### Adapting for an app-specific theme

To produce the same guide against a particular application's theme — so
brand overrides, custom palettes, and any per-app additions to the token
graph appear in place of the JUI defaults — copy the file out and tweak
five things:

1. **Copy `theme-guide.html`** into the app's design-mockup folder (e.g.
   `<your-app>/visuals/`). Keep it outside the build tree.
2. **Update the `<link>` paths** to reach the JUI stylesheets via the new
   relative location, then add a final `<link>` to the app's theme file
   *after* the JUI links so its overrides win the cascade.
3. **Move `.theme` onto `<body>`** and add the data attributes that
   `Theme.init()` applies in the running app — typically
   `data-jui-theme="true"` plus `data-palette="<your-palette-name>"`. App
   theme files usually scope their overrides via `body[data-jui-theme]`
   selectors, which need those attributes on `<body>` specifically.
4. **Mirror the app's `html { font-size: ... }`** in the inline `<style>`
   so rem-derived sizes render at the same px equivalents as production.
   If the app uses the browser default, no edit is needed.
5. **(Optional) add brand-token sections** — extra `<section>` blocks
   that walk any app-specific token families (e.g. `--brand-*`,
   `--accent-*`) so they appear alongside the JUI tokens.

A worked example following this recipe lives at
`policy-app/visuals/theme-guide.html` (in the policy-app workspace,
not jui-stack).

## palette-studio.html

Interactive palette authoring tool — edit hue and chroma axes for each
colour family and preview the resulting ramps. Use this to design a new
palette, then copy the generated CSS into your app theme file. The
output mirrors the format of `Theme.Reference.Editorial.css`.
