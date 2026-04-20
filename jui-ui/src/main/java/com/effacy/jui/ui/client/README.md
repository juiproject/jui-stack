# Package reference

This package contains the top-level client-side utilities and shared UI primitives for `jui-ui`.

It is not only the home of `Theme`. It also contains:

- `Initialiser`: the module entry point that performs global package setup.
- `Theme`: the global theme and CSS injection entry point.
- `InfoBlock` and `InfoBlockCreator`: reusable informational block component and builder support.
- `NotificationBlock` and `NotificationBlockCreator`: reusable notification/message presentation components.
- `NotificationDialog`: dialog-based notification helpers.
- `Notifier`: notification support utilities.
- `Clipboard`, `FileDownloader`, and `Printer`: browser-oriented utility helpers.
- `NotificationDialogDocumentation`: explorer/documentation support for notifications.

From a package perspective, this is the shared “application shell” area for UI-wide behaviour, global styling, and a few common high-level components.

## Package structure

There are two broad responsibilities in this package.

### 1. Global initialisation and platform-wide behaviour

- `Initialiser` wires up package-level initialisation.
- `Theme` injects and exposes the global CSS theme.
- Utility classes such as `Clipboard`, `FileDownloader`, and `Printer` support common browser tasks.

### 2. Shared high-level UI components

- `InfoBlock` and `NotificationBlock` provide package-level reusable components with local CSS resources.
- Their associated creator classes provide the fluent builders used to configure and render them.
- `NotificationDialog` builds on top of the same package area for standardised notification UX.

The rest of this README focuses on the theme/CSS structure, since that is the most architectural part of the package and the most likely area to be used as a reference when editing JUI styling.

# Theme and CSS structure

The central class for CSS is `Theme`.

- `Theme.init()` injects the theme CSS and applies the generated `.theme` class to the document body.
- `Theme.styles()` exposes the generated CSS declaration model for theme-wide styles such as `loader` and `fade`.
- The `ThemeCSS` declaration in `Theme.java` defines the injection order for the theme resource files.

The current injection order is:

1. `Theme.Reference.css`
2. `Theme.Reference.Editorial.css` (and any other alternate palettes)
3. `Theme.Role.css`
4. `Theme.Scale.css`
5. `Theme.Component.css`
6. `Theme.Legacy.css`
7. `Theme_Override.css`

That order is intentional. Each later file is allowed to depend on the files before it, and the final override file remains the last point of resolution. Palette files sit immediately after the default reference so they are a drop-in replacement at the same layer; CSS-variable resolution happens at use-time, so the ordering matters for readability more than for cascade.

## CSS token flow

The JUI theme is organised as a layered token model:

`reference -> role -> scale -> component -> --cpt-* -> selectors`

This is the main rule to keep in mind when editing or extending the CSS.

### 1. Reference tokens

Defined in `Theme.Reference.css`.

Reference tokens are the raw palette-like values that should not carry UI intent beyond "what colour is this?". Values are expressed in OKLCH so every `-NN` step (`-05` through `-90`) targets the same perceptual lightness across families. This is a contract, not just a naming convention — it is what makes dark-mode derivation and WCAG contrast checks mechanical.

**Reference tokens are not written by hand. They are derived from a small set of palette axes**, also declared in `Theme.Reference.css`. See the "Palette axes" section below for the authoring surface; this section describes the output.

The palette families are:

- `--jui-color-primary*` — brand accent (teal by default)
- `--jui-color-secondary*` — secondary brand accent (teal-green by default)
- `--jui-color-ink*` — cool neutral used for text and chrome (formerly `tertiary`)
- `--jui-color-neutral*` — gray (pure gray by default)
- `--jui-color-error*`, `--jui-color-warning*`, `--jui-color-success*`, `--jui-color-info*` — feedback families
- `--jui-color-aux-white` and `--jui-color-aux-black` — absolute white and black. Palettes must not override these.

The former `--jui-color-tertiary*`, `--jui-color-aux-focus1/2`, and `--jui-color-aux-blue` tokens are retained as compatibility aliases in `Theme.Legacy.css`. New code should use `ink*` and `info*` instead.

Component CSS should not normally consume reference tokens directly. Prefer the role or component-family layers.

### 1a. Palette axes

A palette is defined by a small set of CSS custom properties called *axes*. Every `--jui-color-*` reference token is derived from the axes via `oklch()` + `calc()` in `Theme.Reference.css`. This means an alternate palette only needs to override the axes, not all 80+ ramp tokens.

Two contracts apply to every palette:

**Lightness ladder (shared, non-negotiable)** — every family's `-NN` step targets the same OKLCH lightness:

| Step | L | Step | L |
|---|---|---|---|
| `-05` | `0.97` | `-50` | `0.58` |
| `-10` | `0.94` | `-60` | `0.48` |
| `-20` | `0.88` | `-70` | `0.38` |
| `-30` | `0.80` | `-80` | `0.28` |
| `-40` | `0.70` | `-90` | `0.20` |

**Chroma curve** — a bell around `-50`. Per-step ratios of the family's chroma peak:

| Step | ratio | Step | ratio |
|---|---|---|---|
| `-05` | `0.12` | `-50` | `1.00` |
| `-10` | `0.27` | `-60` | `0.96` |
| `-20` | `0.46` | `-70` | `0.81` |
| `-30` | `0.65` | `-80` | `0.58` |
| `-40` | `0.85` | `-90` | `0.38` |

Neutral is the one exception — its chroma is constant across steps (the `--jui-palette-neutral-chroma` axis), not a bell.

The axes themselves:

| Axis | Purpose | Default |
|---|---|---|
| `--jui-palette-primary-hue` | Hue (OKLCH deg) for primary | `210` |
| `--jui-palette-secondary-hue` | Hue for secondary | `170` |
| `--jui-palette-ink-hue` | Hue for ink | `255` |
| `--jui-palette-neutral-hue` | Hue for neutral tint (only matters when chroma > 0) | `0` |
| `--jui-palette-error-hue` | Hue for error | `25` |
| `--jui-palette-warning-hue` | Hue for warning | `65` |
| `--jui-palette-success-hue` | Hue for success | `145` |
| `--jui-palette-info-hue` | Hue for info | `240` |
| `--jui-palette-chroma-peak` | Chroma peak for primary, secondary, warning, success, info | `0.15` |
| `--jui-palette-ink-chroma-peak` | Chroma peak for ink | `0.035` |
| `--jui-palette-error-chroma-peak` | Chroma peak for error — stays alarming in muted palettes | `0.18` |
| `--jui-palette-neutral-chroma` | Constant chroma for neutral — `0` for pure gray, > 0 for tinted | `0` |

An alternate palette overrides only the axes it wants to change. The "Adding an alternate palette" section below has a worked example.

### 2. Role tokens

Defined in `Theme.Role.css`.

These are semantic tokens. They describe intent rather than palette position. Typical examples are:

- `--jui-role-surface-raised`
- `--jui-role-text-default`
- `--jui-role-border-default`
- `--jui-role-interactive-primary`
- `--jui-role-feedback-error`

This is the primary theming layer for brand and palette changes. If an application wants to restyle JUI without changing component structure, this is usually the best layer to override.

### 3. Scale tokens

Defined in `Theme.Scale.css`.

These define shared rhythm and primitive dimensions used throughout the system. Typical examples are:

- spacing: `--jui-space-*`
- typography: `--jui-font-*`
- radii: `--jui-radius-*`
- elevation: `--jui-elevation-*`
- motion: `--jui-duration-*`, `--jui-ease-*`

This layer exists so components do not each invent their own spacing and sizing vocabulary.

### 4. Component-family tokens

Defined in `Theme.Component.css`.

These provide family-level defaults derived from role and scale tokens. They are still global, but are narrower in intent than the role layer. Typical examples are:

- `--jui-comp-control-*`
- `--jui-comp-button-*`
- `--jui-comp-tabset-*`
- `--jui-comp-dialog-*`
  This includes shared dialog tokens such as surface, border, heading, plus dedicated header/footer defaults like `--jui-comp-dialog-header-surface`, `--jui-comp-dialog-header-divider`, `--jui-comp-dialog-footer-surface`, and `--jui-comp-dialog-footer-divider`.
- `--jui-comp-table-*`
- `--jui-comp-notification-*`
- `--jui-comp-form-*`

Use this layer when you want to tune all components in a family consistently without changing every component stylesheet.

### 5. Component-local `--cpt-*` tokens

Defined inside component stylesheets, typically at `.component` root.

These are the preferred public tuning layer for individual components and variants. They are the bridge between the global theme and the concrete selectors used by a component.

Examples include:

- `--cpt-btn-*`
- `--cpt-textctl-*`
- `--cpt-checkctl-*`
- `--cpt-form-*`
- `--cpt-modaldialog-*`
- `--cpt-table-*`
- `--cpt-notification-*`

The expectation is:

- component selectors consume `--cpt-*`
- `--cpt-*` values are usually sourced from `--jui-comp-*`, role tokens, and scale tokens
- downstream applications should prefer overriding `--cpt-*` before reaching for stylesheet replacement when the change is component-specific

### 6. Legacy compatibility tokens

Defined in `Theme.Legacy.css`.

This file is a transitional shim. It maps older JUI global token names onto the new component, role, and scale structure so existing applications do not need to move immediately.

Examples include:

- `--jui-text`
- `--jui-line`
- `--jui-ctl-*`
- `--jui-btn-*`
- `--jui-tabset-*`

This layer exists for compatibility, not as the preferred target for new work.

### 7. Final overrides

Defined in `Theme_Override.css`.

This remains the final global override hook and is injected last. It is suitable for project- or distribution-level last-mile adjustments, but for ordinary theming the preferred order is:

1. role tokens
2. component-family tokens
3. `--cpt-*` tokens
4. stylesheet overrides only when structural changes are required

## Practical editing rules

When updating CSS in JUI, follow these rules:

- Do not introduce new component selectors that consume `--jui-color-*` directly unless there is a strong reason.
- Prefer deriving new family defaults in `Theme.Component.css` from role and scale tokens.
- Prefer exposing meaningful `--cpt-*` variables on component roots when a consumer is likely to want to tune that aspect.
- Keep `Theme.Legacy.css` as a mapping layer. Do not grow it into the primary design surface.
- If you change a public `--cpt-*` contract, update the downstream documentation as part of the same change.

## Adding an alternate palette

A palette is a set of axis values that derive the full `--jui-color-*` ramp. The built-in palette (teal primary, cool ink, pure-gray neutral) is defined by the axis defaults in `Theme.Reference.css`. The **editorial** palette in `Theme.Reference.Editorial.css` is the canonical worked example — use it as a template.

### 1. How switching works

All palettes share the same derivations in `Theme.Reference.css`. They differ only in the axis values they declare, scoped by selector:

```css
.theme                            { /* axis defaults */ }
.theme[data-palette="editorial"]  { /* axis overrides */ }
```

The attribute selector has higher specificity, so whichever palette matches the element wins. The derivations resolve against whichever axes are live. At runtime you toggle the attribute on the body:

```java
Theme.palette (Theme.Palette.EDITORIAL);   // activate editorial
Theme.palette (Theme.Palette.DEFAULT);     // back to default
```

Components never need to know which palette is active. They consume role tokens (or component-family `--jui-comp-*` tokens), which resolve against the live axes.

### 2. Create the palette file

Create `Theme.Reference.<Name>.css` in this package. Scope everything to `.theme[data-palette="<name>"]` and declare only the axes you want to change. A typical palette file is 10–30 lines.

```css
.theme[data-palette="<name>"] {
    --jui-palette-primary-hue:   120;   /* your brand hue */
    --jui-palette-secondary-hue: 270;
    --jui-palette-ink-hue:        75;
    --jui-palette-error-hue:      25;
    --jui-palette-success-hue:   155;
    --jui-palette-info-hue:      235;

    --jui-palette-chroma-peak:       0.04;   /* muted */
    --jui-palette-error-chroma-peak: 0.14;   /* still alarming */
}
```

Any axis you omit inherits the default. Families whose hue you don't override keep the default palette's hue — which is usually what you want when a palette only wants to shift brand colours.

**Neutrals should carry a faint trace of the palette's temperature.** If your palette has any warm or cool character — warm Ink text, a Bone canvas, a tinted brand primary — pure-gray neutrals will clash with the text, borders and control surfaces they appear alongside. Set a small `--jui-palette-neutral-chroma` (around `0.005–0.015`) with `--jui-palette-neutral-hue` matching the palette's warmth. The editorial palette uses `0.010` at hue `85` to softly echo Bone without turning every surface into Bone.

Guidelines:

- `0` — pure gray. Correct only for palettes without warm/cool character (default teal palette uses this).
- `0.005–0.010` — subtle tint. Controls and borders harmonize with warm/cool text; surfaces still read as near-white/gray to the eye.
- `0.010–0.015` — visible tint. The full surface stack (dialogs, panels, controls, chrome) picks up a clear warm or cool cast.
- `> 0.015` — the neutral stack starts reading as a colour family in its own right. Usually wrong unless the palette deliberately wants colourful surfaces.

Keep neutral chroma below the chroma of any dedicated "page-tint" colour (e.g. Bone at `0.018`) so neutrals stay tonally softer than the accents.

### 3. Override role tokens where the palette diverges

Most role tokens consume `--jui-color-*` and adapt automatically once the axes are set. A few reference absolute values and need explicit overrides in the palette file. For example, the editorial palette tints only the outer page canvas:

```css
.theme[data-palette="editorial"] {
    /* ... axes ... */
    --jui-role-surface-canvas: oklch(0.91 0.018 85);   /* Bone */
}
```

Dialogs, panels and control surfaces fall through to the default role layer, where they remain white-anchored — matching the source design's intent that bounded areas are white paper on a Bone page.

Keep role overrides in the same file as the axes so the palette is a single unit.

### 4. Register the file

Add the new stylesheet to the `@CssResource` array in `Theme.java`, immediately after `Theme.Reference.css`:

```java
@CssResource(value = {
    "com/effacy/jui/ui/client/Theme.Reference.css",
    "com/effacy/jui/ui/client/Theme.Reference.Editorial.css",
    "com/effacy/jui/ui/client/Theme.Reference.<Name>.css",
    ...
})
```

CSS-variable lookup happens at use-time, not load-time, so order among palette files does not affect resolution — but keep palette files grouped together for readability.

### 5. Expose the palette to Java

Add an entry to the `Theme.Palette` enum in `Theme.java`:

```java
public enum Palette {
    DEFAULT (null),
    EDITORIAL ("editorial"),
    BRANDX    ("brandx");
    // …
}
```

The string is exactly what appears in `[data-palette="…"]`. A `null` value signals "remove the attribute and use the default palette". The `Theme.palette(Palette)` API handles setting or removing the attribute.

### 6. Handle palette-specific extra families

A palette may introduce families beyond the standard set — the editorial palette adds `moss` and `mauve` for domain accents. Extra families have no default axes, so declare them as literal ramps in the palette file, following the same lightness ladder and chroma curve described in "Palette axes":

```css
.theme[data-palette="editorial"] {
    /* ... axes ... */

    --jui-color-moss05: oklch(0.97 0.008 105);
    --jui-color-moss10: oklch(0.94 0.011 105);
    /* ... through moss90 ... */
}
```

Consumers can reference extras directly (`var(--jui-color-moss50)`) or the palette can promote them to role tokens for structural use. Extras do not exist under other palettes, so don't reference them from the shared role or component layers.

## Adding a new palette family

A palette family is a ten-shade ramp that sits in the default `.theme` block, available to every palette. A new family is appropriate when there is a distinct role no existing family can carry — for example, a second brand accent, a dedicated `danger` vs `error`, or a purpose-specific tone like `highlight`. (If the family is only meaningful for one palette, declare it as an "extra family" in that palette file instead, as in step 6 above.)

### 1. Pick the axes

A new family needs two things: a **hue axis** and a **chroma-peak axis**. Declare both in `.theme` with defaults, then use them in the derivations just like the existing families:

```css
--jui-palette-highlight-hue: 300;          /* purple */
--jui-palette-highlight-chroma-peak: 0.15; /* default peak */
```

Pick the hue to be visually distinct from every existing family. As a rough guide, aim for at least 25–30° of separation between adjacent families at the same chroma. Current occupied hues:

- `primary` 210° (teal), `secondary` 170° (teal-green), `success` 145° (green)
- `ink` 255° (cool neutral), `info` 240° (clear blue)
- `warning` 65° (amber), `error` 25° (red)

If a candidate hue sits within ~15° of an existing family it will be hard to tell apart at low chroma, and the two families will compete.

### 2. Pick the chroma-peak

See "Palette axes" above for the chroma curve. The key is the peak value — what chroma the family reaches at step `-50`. Guidelines:

- Wide-gamut hues (red ~25°, blue ~240°) can reach `C ≈ 0.17–0.18` at peak cleanly. The `error` and `info` families use ~0.17.
- Mid-gamut hues (teal, green, purple) sit comfortably at `C ≈ 0.13–0.15`. Most families land here.
- Narrow-gamut hues (yellow ~90°, pure green ~130°) start clipping above `C ≈ 0.14`. Use ~0.14 and accept a little browser clipping at the peak.
- Muted families (like `ink`) use `C ≈ 0.03–0.05`. Still read as colour, won't compete with accents.
- Pure-neutral families use `C = 0`. Hue doesn't matter; set it to `0`.

Browsers clip out-of-gamut OKLCH to the displayable colour automatically, so a family will still render — but the rendered peak may be less saturated than the OKLCH value suggests. Check swatches in the Component Explorer after adding a family.

### 3. Name the family

Use a short, single-word family name that describes intent, not hue. `highlight`, `danger`, `accent`, `ink` are good; `blue2`, `teal`, `yellow` are not — they conflate palette mechanics with brand colour, and rename badly when brand shifts.

Each token follows `--jui-color-<family><step>`, e.g. `--jui-color-highlight50`.

### 4. Declare the derivations

Add the ten derived tokens to `Theme.Reference.css` next to the other families, using the standard formula:

```css
--jui-color-highlight05: oklch(0.97 calc(var(--jui-palette-highlight-chroma-peak) * 0.12) var(--jui-palette-highlight-hue));
--jui-color-highlight10: oklch(0.94 calc(var(--jui-palette-highlight-chroma-peak) * 0.27) var(--jui-palette-highlight-hue));
--jui-color-highlight20: oklch(0.88 calc(var(--jui-palette-highlight-chroma-peak) * 0.46) var(--jui-palette-highlight-hue));
--jui-color-highlight30: oklch(0.80 calc(var(--jui-palette-highlight-chroma-peak) * 0.65) var(--jui-palette-highlight-hue));
--jui-color-highlight40: oklch(0.70 calc(var(--jui-palette-highlight-chroma-peak) * 0.85) var(--jui-palette-highlight-hue));
--jui-color-highlight50: oklch(0.58 calc(var(--jui-palette-highlight-chroma-peak) * 1.00) var(--jui-palette-highlight-hue));
--jui-color-highlight60: oklch(0.48 calc(var(--jui-palette-highlight-chroma-peak) * 0.96) var(--jui-palette-highlight-hue));
--jui-color-highlight70: oklch(0.38 calc(var(--jui-palette-highlight-chroma-peak) * 0.81) var(--jui-palette-highlight-hue));
--jui-color-highlight80: oklch(0.28 calc(var(--jui-palette-highlight-chroma-peak) * 0.58) var(--jui-palette-highlight-hue));
--jui-color-highlight90: oklch(0.20 calc(var(--jui-palette-highlight-chroma-peak) * 0.38) var(--jui-palette-highlight-hue));
```

Always the same formula — only the chroma-peak axis and hue axis change.

### 5. Wire it into higher layers

A new reference family is not yet usable by components. To make it reachable:

- **Role layer (`Theme.Role.css`)** — if the family has a semantic purpose, add one or more role tokens. For example: `--jui-role-highlight-surface: var(--jui-color-highlight05);` and `--jui-role-highlight-accent: var(--jui-color-highlight60);`. Components should consume the role tokens, not the reference tokens.
- **Component-family layer (`Theme.Component.css`)** — if a component family needs the new tones, wire `--jui-comp-<family>-*` tokens onto the role tokens (or onto the reference tokens if no role is warranted).
- **Java accessors (`Theme.java`)** — add `colorHighlight05()` through `colorHighlight90()` helpers following the existing `colorInk*` / `colorInfo*` pattern if the family should be reachable from Java code.
- **Explorer (`explorer/ThemeRenderer.java`)** — add a block in `ThemeStyle.colors()` so the new family appears in the theme explorer:
  ```java
  styles.add (new ThemeStyle ("Reference palette: highlight"));
  addRange (styles, "--jui-color-highlight", "Highlight colour tone");
  ```

### 6. Document it

Update the family list in the "Reference tokens" section above, the axis table in "Palette axes", and the global-variable table in `docs/ess_styles.md`. State the family's purpose and hue in one line — future maintainers need to know why it exists before they need to know what it looks like.

## Files in this package area

### Java

- `Initialiser.java`: package-level UI module initialisation.
- `Theme.java`: Java entry point for CSS injection and shared global styles.
- `InfoBlock.java`: shared information-block component.
- `InfoBlockCreator.java`: builder support for `InfoBlock`.
- `NotificationBlock.java`: shared notification/message block component.
- `NotificationBlockCreator.java`: builder support for `NotificationBlock`.
- `NotificationDialog.java`: standard notification dialog support.
- `Notifier.java`: package-level notification support.
- `Clipboard.java`, `FileDownloader.java`, `Printer.java`: browser utility helpers.

### CSS resources

- `Theme.Reference.css`: raw palette and primitive reference values (default palette).
- `Theme.Reference.Editorial.css`: editorial palette — activated via `[data-palette="editorial"]`. Worked example of how to add an alternate palette.
- `Theme.Role.css`: semantic design tokens.
- `Theme.Scale.css`: shared spacing, type, radius, elevation, and motion scales.
- `Theme.Component.css`: family-level component defaults and global shared animations/styles.
- `Theme.Legacy.css`: compatibility aliases for older JUI token names.
- `Theme_Override.css`: last-in override resource.

## Relationship to other documentation

For downstream application usage and migration guidance, also see:

- `docs/ess_styles.md`
- `docs/css_component_contracts.md`
- `docs/css_upgrade_2026_04.md`

This README should be treated as the package-level reference for this package, with particular emphasis on how the theme is structured and how new CSS should be added.
