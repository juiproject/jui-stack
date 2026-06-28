# Overview

This module contains [agent skills](https://agentskills.io/home) (compatible with
[Claude Code](https://docs.anthropic.com/en/docs/claude-code)) for the JUI framework. Each skill is a
structured instruction set (a `SKILL.md` file, optionally with supporting reference files) that teaches
an agent how to work with a specific facet of JUI, following the framework's conventions, patterns and
best practices.

The skills are organised as **one general entry point, four building-block skills and two cross-cutting
skills**:

| Skill | Directory | Role | Purpose |
|-------|-----------|------|---------|
| `jui-ui` | `jui-ui/` | **Entry point** | Describes the JUI UI mechanism and the four kinds of artefact; helps decide *what* to build and *which* skill to use; catalogues the standard controls/components/fragments; explains how to inspect the JUI source and find the docs. Triggers on any UI work. |
| `jui-components` | `jui-components/` | Building block | Create JUI components (`SimpleComponent`, `Component<Config>`, `StateComponent<V>`) — DOM building, event handling, lifecycle, modal dialogs. |
| `jui-controls` | `jui-controls/` | Building block | Create JUI controls (`Control<V, C>`) — value management, dirty detection, validation, focus, form integration. |
| `jui-fragments` | `jui-fragments/` | Building block | Create JUI fragments (`Fragment<F>`, `FragmentWithChildren<F>`) — reusable DOM building blocks. |
| `jui-dombuilder` | `jui-dombuilder/` | Cross-cutting | Build and **update** DOM with DomBuilder — element/event API, element extraction, and the rules for re-rendering at runtime (`Wrap.buildInto` vs the component's `buildInto` vs `rerender()`, `StateComponent`). Referenced by the building-block skills. |
| `jui-styles` | `jui-styles/` | Cross-cutting | Style JUI elements — localised CSS, CSS variables, style packs, variants. Referenced by the building-block skills. |

The intended flow: `jui-ui` is invoked for any UI task, classifies the work, points at the existing
standard artefacts (and the host project's own artefacts / `Variants` class) to reuse, and routes to
the relevant building-block skill for construction; that skill draws on `jui-dombuilder` and
`jui-styles` for rendering and CSS.

# Skill structure

Each skill is a directory whose name is the skill name. It follows the
[agent skills standard](https://agentskills.io/home):

```
jui-ui/
├── SKILL.md        # required — YAML frontmatter + instructions
└── catalogue.md    # optional supporting file, loaded on demand
```

- **`SKILL.md`** — required. Begins with YAML frontmatter containing:
  - `name` — the skill name (matches the directory name).
  - `description` — a single string describing *what the skill does and when to use it*. This is the
    only part always in context, so it must carry strong trigger phrases (class names, verbs, synonyms)
    so the agent selects the skill at the right moment. Keep the body focused; push bulky reference
    material into supporting files.
- **Supporting files** (e.g. `jui-ui/catalogue.md`) — optional. Referenced from `SKILL.md` by relative
  link and loaded by the agent only when needed, keeping the always-in-context footprint small. The
  `jui-ui` skill uses this for its [`catalogue.md`](jui-ui/catalogue.md) of standard artefacts.

References that must survive the skill being copied into another project are absolute, not
out-of-tree relative links: `jui-ui/SKILL.md` links its documentation to the published copy on
GitHub, names the sibling specialised skills (rather than path-linking them), and gives a recipe for
obtaining the JUI source when no `jui-stack` checkout is present. The only relative link is to a
skill's own supporting files (e.g. `catalogue.md`).

# Use

Skills are activated by copying (or symlinking) the skill directories into a project's
`.claude/skills/` directory. Claude Code automatically discovers `SKILL.md` files there and makes them
available during conversations.

To install the skills into a project:

```bash
# From the project root (where .claude/ lives)
mkdir -p .claude/skills
for s in jui-ui jui-components jui-controls jui-fragments jui-dombuilder jui-styles; do
  cp -r path/to/jui-stack/jui-skills/$s .claude/skills/
done
```

Or using symlinks (keeps the skills in sync with the jui-stack source):

```bash
mkdir -p .claude/skills
for s in jui-ui jui-components jui-controls jui-fragments jui-dombuilder jui-styles; do
  ln -s path/to/jui-stack/jui-skills/$s .claude/skills/$s
done
```

Once installed, Claude Code automatically invokes the appropriate skill when asked to do UI work.
Skills can also be invoked explicitly with slash commands (e.g. `/jui-controls`).

> The `jui-ui` skill links its documentation to the published copy on GitHub
> (`https://github.com/juiproject/jui-stack/tree/main/docs`), so those references resolve wherever the
> skill is installed. For reading the JUI **source**, the skill documents a fallback for when no
> `jui-stack` checkout is present: extract the sources from the JUI jars (the `SKILL.md` includes the
> extraction recipe).

Eventually these skills will be packaged as a Claude Code plugin (or distributable package) to simplify
installation and updates across projects.

# Maintenance

- **Source of truth is the JUI source.** These skills document patterns and a catalogue that must track
  the framework. When JUI changes, update the affected skill(s).
- **Keep the catalogue current.** `jui-ui/catalogue.md` is hand-maintained. Re-derive it from source
  when the JUI version changes — it documents the exact commands at its foot (the `Controls` helper is
  the authoritative list of controls; package listings give components and fragments).
- **Descriptions carry the triggers.** When editing a skill's purpose, update the frontmatter
  `description` so it still fires on the right prompts — that string is what the agent matches against.
- **Routing stays consistent.** If a specialised skill is added, renamed or split, update both the
  routing table in `jui-ui/SKILL.md` (the four-building-blocks table) and the table above.
- **Prefer reference over duplication.** Detailed, evolving material (catalogues, long examples) belongs
  in supporting files loaded on demand, not inline in `SKILL.md`.
