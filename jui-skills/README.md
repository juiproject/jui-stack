# Overview

This module contains [Claude Code](https://docs.anthropic.com/en/docs/claude-code) skills for the JUI framework. Each skill is a structured instruction set (a `SKILL.md` file) that teaches Claude how to create specific types of JUI artefacts, following the framework's conventions, patterns, and best practices.

The following skills are included:

| Skill | Directory | Purpose |
|-------|-----------|---------|
| `jui-components` | `jui-components/` | Create JUI components (`SimpleComponent`, `Component<Config>`, `StateComponent<V>`) with DOM building, event handling, lifecycle hooks, and modal dialogs. |
| `jui-controls` | `jui-controls/` | Create JUI controls (`Control<V, C>`) with value management, dirty detection, validation, focus handling, and form integration. |
| `jui-fragments` | `jui-fragments/` | Create JUI fragments (`Fragment<F>`, `FragmentWithChildren<F>`) as reusable DOM building blocks that contribute to a parent component's DOM tree. |
| `jui-styles` | `jui-styles/` | Style JUI elements using localised CSS, CSS variables, style packs, and custom style variants. Referenced by the component, control, and fragment skills. |

# Use

Skills are activated by copying (or symlinking) the skill directories into a project's `.claude/skills/` directory. Claude Code automatically discovers `SKILL.md` files in that location and makes them available during conversations.

To install the skills into a project:

```bash
# From the project root (where .claude/ lives)
mkdir -p .claude/skills
cp -r path/to/jui-stack/jui-skills/jui-components .claude/skills/
cp -r path/to/jui-stack/jui-skills/jui-controls   .claude/skills/
cp -r path/to/jui-stack/jui-skills/jui-fragments   .claude/skills/
cp -r path/to/jui-stack/jui-skills/jui-styles      .claude/skills/
```

Or using symlinks (keeps skills in sync with the jui-stack source):

```bash
mkdir -p .claude/skills
ln -s path/to/jui-stack/jui-skills/jui-components .claude/skills/jui-components
ln -s path/to/jui-stack/jui-skills/jui-controls   .claude/skills/jui-controls
ln -s path/to/jui-stack/jui-skills/jui-fragments   .claude/skills/jui-fragments
ln -s path/to/jui-stack/jui-skills/jui-styles      .claude/skills/jui-styles
```

Once installed, Claude Code will automatically invoke the appropriate skill when asked to create a component, control, or fragment. Skills can also be invoked explicitly using slash commands (e.g. `/jui-controls`).

Eventually these skills will be packaged as a Claude Code plugin (or distributable package) to simplify installation and updates across projects.
