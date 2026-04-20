# Icon packs

Bindings for the two icon fonts bundled with JUI. Each pack exposes a Java class whose static methods return the CSS class string for a given icon, and an `inject()` call that registers the font with the page at runtime.

| Pack | Class | Upstream | License |
|---|---|---|---|
| [Font Awesome Free](https://github.com/FortAwesome/Font-Awesome) 6.x | `FontAwesome` | See upstream | Icons: CC BY 4.0 · Fonts: SIL OFL 1.1 · Code: MIT — see `LICENSE.txt` |
| [Remix Icon](https://remixicon.com) 4.x | `RemixIcon` | See upstream | Remix Icon License v1.0 — see `REMIX_LICENSE.txt` in the resource package |

Apps wanting icons just call `FontAwesome.inject()` and/or `RemixIcon.inject()` during application startup, then use e.g. `FontAwesome.home()` or `RemixIcon.home4()` to obtain a class string.

---

## Font Awesome

**Note** the free version does not include all variants of every icon — in particular the solid variants have a greater representation than the regular ones. The default is therefore the solid variant.

### Updating

1. Download the latest (free) version from [Font Awesome](https://github.com/FortAwesome/Font-Awesome).
2. Extract the package and copy the `.woff2` files from the `webfonts` directory to the resource package `com.effacy.jui.ui.client.icon`.
3. Copy the `XXX.css` files that match the font files to the same resource package and, for each, **remove the `@font-face` block at the top** (JUI generates it from the `@CssResource.Font` annotations). If new sizings or fonts appear, add entries on the `FontCSS` class. You may need to rename files to match the stripped `fa-` prefix.
4. Copy over `fontawesome.css`.
5. Update `FontAwesome.VERSION`.
6. Run `FontAwesome.FontAwesomeGenerator` as a Java application to print the typed icon methods; paste the output into the `Icons` section of `FontAwesome`, replacing the existing block.

---

## Remix Icon

Remix Icon ships two visual variants of each icon — an outline (`-line`) and a solid (`-fill`). The Java API folds both under a single method per icon:

```java
el.className = RemixIcon.home4 ();                        // ri-home-4-line (default)
el.className = RemixIcon.home4 (RemixIcon.Option.FILL);   // ri-home-4-fill
```

Only two options are defined: `LINE` (default) and `FILL`. Remix does not ship animation classes (spin, flip, etc.); if you need them, add your own keyframes in application CSS.

### Licensing caveat

Remix Icon relicensed from Apache 2.0 to its own **Remix Icon License v1.0** in January 2026. It is still free for commercial use inside apps, SaaS products, UI kits and design systems, but:

- Cannot be redistributed as a standalone icon pack or sold as a competing library.
- Cannot be used as a product logo / brand mark.
- When the complete library ships as part of another product (which is what JUI does), the license file must travel with it — hence `REMIX_LICENSE.txt` in the resource package.

The full text is at `jui-ui/src/main/resources/com/effacy/jui/ui/client/icon/REMIX_LICENSE.txt` and on the upstream repo.

### Updating

1. Download the latest Remix Icon release (`remixicon.css` + `remixicon.woff2`) from the upstream repository or npm.
2. Copy `remixicon.woff2` into `jui-ui/src/main/resources/com/effacy/jui/ui/client/icon/`.
3. Copy `remixicon.css` into the same package and **strip the leading `@font-face` block** (JUI regenerates it from `@CssResource.Font`).
4. Refresh `REMIX_LICENSE.txt` in case the upstream license file changed.
5. Update `RemixIcon.VERSION`.
6. Run `RemixIcon.RemixIconGenerator` as a Java application to print typed icon methods; paste the output into the `Icons` section of `RemixIcon`, replacing the existing block.
