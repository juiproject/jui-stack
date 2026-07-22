---
title: Theming and styles
description: Change colours, fonts and spacing with CSS custom properties.
icon: palette
order: 1
updated: Sample content
related: [the-icon-set]
---

All styling lives in a **single stylesheet**, `public/help.css`. The look is
driven by CSS custom properties (variables) declared at the top, so you can
re-theme without touching any rules.

## Change the palette

Edit the variables in the `:root` block of `public/help.css`:

```
:root {
  --brand:        #297AA3;   /* primary accent */
  --brand-ink:    #1A4D63;   /* accent, pressed */
  --brand-soft:   #D9E6F8;   /* accent tint */
  --fg:           #1A2C40;   /* main text */
  --bg:           #F6F8FB;   /* page background */
  --surface:      #FFFFFF;   /* cards */
  --font:         'Geist', system-ui, sans-serif;
}
```

Set these to your product's tokens and the whole help center follows.

## Fonts

The sample uses system-friendly web fonts loaded at the top of `help.css`. Swap
the `@import` and the `--font` variables for your own.

> Because everything routes through these variables, a dark theme is mostly a
> matter of flipping the surface and text values.
