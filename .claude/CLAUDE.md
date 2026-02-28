# JUI Stack

## Coding Standards

### `if-else` statements

To preserve meaningfulness of atomic evaluation and fall-through in if-else statements, avoid returns in else blocks:

```java
if (condition1) {
    return "value1";
} else if (condition2) {
    return "value2";
} else {
    return "value3";
}
```

and write as:

```java
if (condition1)
    return "value1";
if (condition2)
    return "value2";
return "value2";
```

Rationale is that returns obfuscate the fact that the statement will not fall through (or is intended not to fall through) which is the basis for atomicity in multiple evaluations of this sort.

### Expressions

Avoid multi-term expressions without using parenthesis:

```java
if (a != null && b == 2) {
    // Do something.
}
```

and write as:

```java
if ((a != null) && (b == 2)) {
    // Do something.
}
```

Rationale is that it improves readability and that we can't always assume knowledge of operator precedence.

### `final` keyword

Use the `final` keyword judiciously. Try to limit to essential cases only, such as to static member variables that are intended to be immutable, classes which clearly should not be instantiated (i.e. ones that hold static utility methods) and methods that clearly should not be overridden (that risk significant breakage).

Rationale is that without clear and meaningful intent it clutters the code reducing readability and may place unnecessary constraints for unforeseen circumstances.

## Skills

Skills provide structured instructions for specific tasks. Before performing a skill-related task, read the relevant SKILL.md and follow its workflow.

Skills are located in `jui-skills/`.

| Skill | When to use | Path |
|-------|-------------|------|
| JUI Components | Creating JUI components (SimpleComponent, Component with Config, StateComponent) | `jui-skills/jui-components/SKILL.md` |
| JUI Controls | Creating custom form field controls (Control with value management, dirty detection, validation) | `jui-skills/jui-controls/SKILL.md` |
| JUI Fragments | Creating reusable DOM building blocks (Fragment, FragmentWithChildren) | `jui-skills/jui-fragments/SKILL.md` |

## Documentation

Detailed documentation is available under `docs/`:

| Topic | File |
|-------|------|
| Components overview | `docs/ess_components.md` |
| Controls | `docs/ess_controls.md` |
| Styles & themes | `docs/ess_styles.md` |
| Component patterns | `docs/cpt_patterns.md` |
| Component overview | `docs/cpt_overview.md` |
| Rendering | `docs/topic_rendering.md` |
| Events | `docs/ess_events.md` |
