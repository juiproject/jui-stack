---
name: jui-ui
description: "Entry point for building user interfaces with the JUI framework (Java-to-JavaScript UI). Use this skill whenever doing any front-end / UI work in a JUI application: building screens, pages, panels, dialogs, forms, tables, galleries, navigation, or any reusable UI element — and when you need to know what JUI offers, which kind of artefact to build, what standard controls/components/fragments already exist, or how to inspect the JUI source. Routes to the specialised jui-components, jui-controls, jui-fragments and jui-styles skills. Triggers include: JUI, UI, building a screen/page/panel/form/dialog/table/gallery, SimpleComponent, Control, Fragment, DomBuilder, ControlForm, TabNavigator, or 'how do I do X in the UI'."
---

# JUI UI development

JUI is a Java framework for building rich web UIs that compile to JavaScript (via GWT / J2CL).
You build the UI **in Java** — there are no HTML templates. You compose **components** that render
their DOM imperatively through a fluent **DomBuilder**, wire behaviour with Java event handlers,
and style with **localised CSS** scoped to each component. State lives in Java; the DOM is built
and updated from it.

This skill is the front door for any UI work. Use it to (1) understand the moving parts, (2) decide
*what kind of thing* to build, (3) discover what already exists, and (4) find the authoritative
source and docs. For the actual construction of an artefact, hand off to the specialised skill.

## The four building blocks

Everything you build in JUI is one of four kinds of artefact. Identify which one you need, then
invoke the matching skill (each is a sibling directory shipped alongside this one).

| Build this when you need… | Artefact | Base type(s) | Skill |
|---|---|---|---|
| A reusable UI element / screen / panel / widget with its own DOM, state and behaviour | **Component** | `SimpleComponent`, `Component<Config>`, `StateComponent<V>` | **jui-components** |
| An interactive form field — a value the user edits, with dirty detection and validation | **Control** | `Control<V, C>` | **jui-controls** |
| A small, reusable piece of DOM with no component lifecycle, inserted into a parent's build | **Fragment** | `Fragment<F>`, `FragmentWithChildren<F>` | **jui-fragments** |
| To style any of the above — localised CSS, CSS variables, style packs, variants | **Style** | `ILocalCSS` / `@CssResource` | **jui-styles** |

Quick decision guide:

- **Is it a form field** (text, number, selection, checkbox, date, upload…)? → a **Control**. First
  check whether a standard one already fits (see the catalogue) — usually it does.
- **Does it need its own lifecycle, child components, navigation, or to be shown/activated on its
  own** (a page, a panel, a dialog body, a table host)? → a **Component**.
- **Is it just a recurring bit of markup** (a badge, a labelled icon, a card header) with no state of
  its own, that you splice into a component's DOM? → a **Fragment**.
- **Are you only changing appearance**? → **Style** (the component/control/fragment skills each defer
  to jui-styles for the CSS details).

When unsure between a fragment and a component: a fragment cannot be navigated to, activated, focused
or disposed independently — if you need any of those, it is a component.

## How rendering works (in one breath)

A component supplies its DOM either by overriding `buildNode(Element)` or by calling `renderer(...)`
in its constructor, building with the DomBuilder element classes (`Div`, `Span`, `H3`, `A`, `Button`,
…) under `Wrap.$(el).$(root -> { … })`. Elements are referenced for later update via `.by("name")`
(read back with `dom.first("name")`). Events attach with `.onclick(...)` / `.on(...)`. Update at
runtime by rebuilding into a captured element (`buildInto(el, …)`) or call `rerender()`. Styling is a
local `ILocalCSS` (CSS scoped under the auto-applied `.component` class). The **jui-components** and
**jui-styles** skills cover this in full — read them before writing rendering or CSS code.

## What already exists — use it before building

JUI ships a substantial suite of ready-made controls, components and fragments. **Prefer composing or
extending a standard artefact over writing a new one.** Before building anything, check the catalogue:

→ **[catalogue.md](catalogue.md)** — every standard control, component, fragment and layout with a
one-line description. Load it when you need to know what is available or to find the closest existing
artefact to a need or to a custom variant you are about to write.

Construction conventions to expect: every standard artefact has a `Config` builder and an `XxxCreator`
helper (`build(cfg)` / `$(parent, cfg)`); controls additionally have a one-stop `Controls` factory
(`Controls.text(...)`, `Controls.selector(...)`, …).

## Inspecting the JUI source

JUI is open and you are encouraged to read it — the standard artefacts are the best reference for
patterns, and the Javadoc/inline comments answer most API questions. Two routes, in order of
preference:

1. **The jui-stack source project (best, when available).** If the `jui-stack` repository is checked
   out (often a sibling of the app project), read directly from its modules — richer than the jars
   (comments, tests, and the `jui-playground` worked examples):

   ```
   jui-stack/jui-ui/src/main/java/com/effacy/jui/ui/client/…
   jui-stack/jui-core/src/main/java/com/effacy/jui/core/client/…
   ```

2. **Extract source from the jars.** The JUI jars bundle their `.java` sources (GWT compiles from
   source), so you can read them without the jui-stack project. Extract into a **non-checked-in,
   clearly-named** directory so it is never confused with project code, and git-ignore it:

   ```bash
   # Pick the jui-* jars from the local Maven cache (match your project's JUI version).
   # The MAIN jar already contains .java (no need for the -sources jar).
   DEST=.jui-sources                      # scratch cache — do NOT check in
   mkdir -p "$DEST"
   for j in $(find ~/.m2/repository/com/effacy/jui -name 'jui-*.jar' \
                ! -name '*-javadoc.jar' ! -name '*-sources.jar' | sort); do
     unzip -o -q "$j" '*.java' -d "$DEST"
   done
   echo ".jui-sources/" >> .gitignore     # once
   # Now browse/grep DEST, e.g.:
   #   grep -rl "class TabNavigator" "$DEST"
   ```

   Use a distinctive name like `.jui-sources/` (leading dot, plainly a cache) rather than `src` or
   `build` so it is obviously not part of the codebase. Prefer extracting once and reusing; delete it
   when done. Do **not** decompile the `.class` files — the real sources are right there.

## Official documentation

The full JUI documentation (docsify) is published in the jui-stack repository on GitHub:
**<https://github.com/juiproject/jui-stack/tree/main/docs>**. It is always available regardless of
where this skill is installed, so fetch pages from there. Most relevant pages:

| Page | Topic |
|---|---|
| [ess_components.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_components.md) | Components |
| [ess_controls.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_controls.md) | Controls and forms (`ControlForm`) |
| [ess_styles.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_styles.md) | Styles & themes |
| [ess_events.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_events.md) | Events |
| [ess_galleries.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_galleries.md) | Galleries, tables & stores |
| [ess_modals.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_modals.md) | Modals & dialogs |
| [ess_navigation.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_navigation.md) | Navigation (tab/card) |
| [cpt_overview.md](https://github.com/juiproject/jui-stack/blob/main/docs/cpt_overview.md) | The standard component/control suite |
| [cpt_selection.md](https://github.com/juiproject/jui-stack/blob/main/docs/cpt_selection.md) · [cpt_files.md](https://github.com/juiproject/jui-stack/blob/main/docs/cpt_files.md) | Selection & file controls in depth |
| [topic_rendering.md](https://github.com/juiproject/jui-stack/blob/main/docs/topic_rendering.md) · [topic_focusblur.md](https://github.com/juiproject/jui-stack/blob/main/docs/topic_focusblur.md) | Rendering, focus/blur |

The complete index is the repository's [`docs/` directory](https://github.com/juiproject/jui-stack/tree/main/docs)
(see its `_sidebar.md`). If a local `jui-stack` checkout is present you may of course read the same
files from `jui-stack/docs/`.

## Workflow

1. **Classify the work** — which of the four building blocks is it? Use the decision guide above.
2. **Reuse first** — open [catalogue.md](catalogue.md); if a standard control/component/fragment fits,
   configure it via its `Creator` / `Controls` helper rather than building new.
3. **If building new, invoke the specialised skill** — jui-components, jui-controls, jui-fragments,
   or jui-styles — and follow its patterns.
4. **Read the closest standard artefact** for the pattern (from the jui-stack source or an extracted
   jar) before writing a custom variant.
5. **Style via jui-styles** — localised CSS scoped under `.component`; tokens/variables over hard-coded
   values.
6. **Consult the docs** for anything subtle (navigation, focus/blur, stores, modals).
