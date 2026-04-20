# CSS Upgrade Notes: 2026-04 Target-State Refactor

This release moves JUI to the new CSS token architecture:

- `reference` tokens: raw palette values
- `role` tokens: semantic colours and state
- `scale` tokens: spacing, typography, radius, elevation, motion
- `component` tokens: family defaults used by component CSS

The new preferred public component tuning layer is `--cpt-*`.

## Compatibility

- Existing global tokens such as `--jui-text`, `--jui-ctl-*`, `--jui-btn-*`, and `--jui-tabset-*` still exist for this transition release through `Theme.Legacy.css`.
- Existing component-local tokens such as `--jui-textctl-*` and `--jui-checkctl-*` are still honoured where the component now resolves its `--cpt-*` value from the older token first.
- Downstream applications should plan to move new customisation onto `--jui-role-*`, `--jui-comp-*`, and `--cpt-*`.

## New `--cpt-*` Contracts Added Or Expanded

### Newly broadened public layer
- Buttons now expose a fuller `--cpt-btn-*` contract for spacing, text, waiting, disabled, and focus styling.
- Text controls now prefer `--cpt-textctl-*`.
- Check controls now prefer `--cpt-checkctl-*`.
- Modal dialogs now expose `--cpt-modaldialog-*`.
- Modal dialogs now also default header/footer surfaces and dividers from dedicated component tokens:
  `--jui-comp-dialog-header-surface`,
  `--jui-comp-dialog-header-divider`,
  `--jui-comp-dialog-footer-surface`,
  `--jui-comp-dialog-footer-divider`.
- Notifications now expose `--cpt-notification-*`.
- Tables now expose `--cpt-table-*`.
- Navigators expose fuller tab-specific `--cpt-*` contracts.
- Control forms continue to use `--cpt-form-*`, with spacing aligned to shared scale tokens.

## Migration Guidance

### If you currently override palette or brand colours
- Move toward overriding `--jui-role-*` tokens first.
- Use `--jui-comp-*` only when you want a family-wide default.

### If you currently override component-local tokens
- Move from older local names to the documented `--cpt-*` names over time.
- Keep old names temporarily only where a downstream app has not yet been updated.

### If you currently rely on `super` stylesheet overrides
- Keep them for structural changes only.
- Prefer `--cpt-*` first if the new contract now covers the use case.

## Changed Components

See [css_component_contracts.md](css_component_contracts.md) for the current public `--cpt-*` variables and their meaning.
