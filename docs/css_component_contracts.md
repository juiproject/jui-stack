# CSS Component Contracts

This document is the reference for the new public `--cpt-*` component tuning layer introduced with the design-system CSS refactor.

## Override Order

Use this order when theming JUI applications:

1. Override `--jui-role-*` tokens for brand and palette changes.
2. Override `--jui-comp-*` tokens for family-level behaviour.
3. Override `--cpt-*` tokens for component-level tuning.
4. Use `super` stylesheet overrides only for structural exceptions.

## Button

- `--cpt-btn-height`: button height.
- `--cpt-btn-bg`: background surface.
- `--cpt-btn-bg-hover`: hover background surface.
- `--cpt-btn-border`: border colour.
- `--cpt-btn-border-width`: border width.
- `--cpt-btn-border-radius`: radius.
- `--cpt-btn-text`: content colour.
- `--cpt-btn-text-size`: base text size.
- `--cpt-btn-text-lineheight`: label line height.
- `--cpt-btn-font-weight`: label weight.
- `--cpt-btn-padding-inline`: horizontal inner padding.
- `--cpt-btn-padding-block`: vertical inner padding.
- `--cpt-btn-gap`: icon/label gap.
- `--cpt-btn-margin`: outer margin.
- `--cpt-btn-disabled-bg`: disabled background.
- `--cpt-btn-disabled-border`: disabled border.
- `--cpt-btn-disabled-text`: disabled text/icon colour.
- `--cpt-btn-disabled-opacity`: disabled content opacity.
- `--cpt-btn-waiting-bg`: waiting background.
- `--cpt-btn-waiting-border`: waiting border.
- `--cpt-btn-waiting-text`: waiting spinner colour.
- `--cpt-btn-focus-border`: focus border colour.
- `--cpt-btn-focus-shadow`: focus ring shadow colour.

## Text Control

- `--cpt-textctl-size`: input font size.
- `--cpt-textctl-padding`: control padding.
- `--cpt-textctl-height`: control height.
- `--cpt-textctl-border`: border colour.
- `--cpt-textctl-border-radius`: radius.
- `--cpt-textctl-bg`: background surface.
- `--cpt-textctl-text`: default text colour.
- `--cpt-textctl-text-disabled`: disabled text colour.
- `--cpt-textctl-text-readonly`: read-only text colour.
- `--cpt-textctl-text-placeholder`: placeholder and clear-icon colour.
- `--cpt-textctl-icon`: leading/trailing icon colour.
- `--cpt-textctl-icon-disabled`: disabled icon colour.
- `--cpt-textctl-icon-readonly`: read-only icon colour.

## Check Control

- `--cpt-checkctl-text`: primary label colour.
- `--cpt-checkctl-description`: description colour.
- `--cpt-checkctl-labelgap`: vertical and spacer gap.
- `--cpt-checkctl-size`: control font size.
- `--cpt-checkctl-toggle-track`: slider track colour.
- `--cpt-checkctl-toggle-track-active`: slider active track colour.
- `--cpt-checkctl-toggle-thumb`: slider thumb colour.

## Control Form

- `--cpt-form-row-gap`: row gap.
- `--cpt-form-group-depth0-gap` to `--cpt-form-group-depth4-gap`: body gap by nesting depth.
- `--cpt-form-group-indent`: conditional group indent.
- `--cpt-form-header`: heading colour.
- `--cpt-form-header-instruction`: instructional copy colour.
- `--cpt-form-header-icon-gap`: icon gap.
- `--cpt-form-header-depth0-size` to `--cpt-form-header-depth4-size`: heading sizes by nesting depth.
- `--cpt-form-footer-guidance`: footer guidance colour.
- `--cpt-form-footer-guidance-size`: footer guidance size.
- `--cpt-form-text`: general form text.
- `--cpt-form-text-error`: error text.
- `--cpt-form-text-disabled`: disabled text.
- `--cpt-form-help-bg`: help bubble surface.
- `--cpt-form-help`: help bubble text.
- `--cpt-form-help-radius`: help bubble radius.
- `--cpt-form-error-bg`: error surface.
- `--cpt-form-error-icon`: error icon colour.
- `--cpt-form-error-icon-size`: error icon size.
- `--cpt-form-error-text`: error body text.
- `--cpt-form-error-text-size`: error body text size.
- `--cpt-form-error-radius`: error container radius.
- `--cpt-form-error-margin-v`: error container vertical padding.
- `--cpt-form-error-margin-h`: error container horizontal padding.
- `--cpt-form-error-item-gap`: gap between error icon and text.
- `--cpt-form-separator`: separator colour.
- `--cpt-form-separator-gap`: separator spacing.

## Navigators

### Horizontal Bar
- `--cpt-navigator-bg`: navigator background.
- `--cpt-tabbedpanel-outline`: outer border.
- `--cpt-tabbedpanel-bg-tab-inactive`: inactive tab background.
- `--cpt-tabbedpanel-bg-tab-hover`: hover tab background.
- `--cpt-tabbedpanel-bg-tab-active`: active tab background.
- `--cpt-tabbedpanel-text`: default tab text.
- `--cpt-tabbedpanel-text-hover`: hover tab text.
- `--cpt-tabbedpanel-text-active`: active tab text.
- `--cpt-tabbedpanel-radius`: container radius.
- `--cpt-tabbedpanel-item-radius`: tab radius.

### Horizontal Underline
- `--cpt-tabbedpanel-hu-height`: strip height.
- `--cpt-tabbedpanel-hu-indent`: left indent.
- `--cpt-tabbedpanel-hu-lift`: underline lift.
- `--cpt-tabbedpanel-hu-line`: underline baseline colour.
- `--cpt-tabbedpanel-hu-text`: default tab text.
- `--cpt-tabbedpanel-hu-text-hover`: hover tab text.
- `--cpt-tabbedpanel-hu-text-active`: active tab text.
- `--cpt-tabbedpanel-hu-underline-hover`: hover underline colour.
- `--cpt-tabbedpanel-hu-underline-active`: active underline colour.

## Modal Dialog

- `--cpt-modal-height-top`: default top inset for the modal height frame.
- `--cpt-modal-height-right`: default right inset for the modal height frame.
- `--cpt-modal-height-bottom`: default bottom inset for the modal height frame.
- `--cpt-modal-height-left`: default left inset for the modal height frame.
- `--cpt-modal-slider-height-top`: default top inset for the slider modal height frame.
- `--cpt-modal-slider-height-right`: default right inset for the slider modal height frame.
- `--cpt-modal-slider-height-bottom`: default bottom inset for the slider modal height frame.
- `--cpt-modal-slider-height-left`: default left inset for the slider modal height frame.
- `--cpt-modal-z1-height-*` to `--cpt-modal-z6-height-*`: stacked modal insets for each z-level.
- `--cpt-modal-z1-slider-height-*` to `--cpt-modal-z6-slider-height-*`: stacked slider modal insets for each z-level.
- `--cpt-modaldialog-surface`: dialog body surface.
- `--cpt-modaldialog-border`: outer border.
- `--cpt-modaldialog-border-width`: outer border width.
- `--cpt-modaldialog-radius`: outer radius.
- `--cpt-modaldialog-shadow`: box shadow.
- `--cpt-modaldialog-chrome`: header/footer chrome surface.
- `--cpt-modaldialog-header-bg`: header surface. Defaults from `--jui-comp-dialog-header-surface`.
- `--cpt-modaldialog-header-divider`: header bottom divider colour. Defaults from `--jui-comp-dialog-header-divider`.
- `--cpt-modaldialog-header-divider-width`: header bottom divider width.
- `--cpt-modaldialog-footer-bg`: footer surface. Defaults from `--jui-comp-dialog-footer-surface`.
- `--cpt-modaldialog-footer-divider`: footer top divider colour. Defaults from `--jui-comp-dialog-footer-divider`.
- `--cpt-modaldialog-footer-divider-width`: footer top divider width.
- `--cpt-modaldialog-heading`: title colour.
- `--cpt-modaldialog-heading-size`: title size.
- `--cpt-modaldialog-heading-weight`: title weight.
- `--cpt-modaldialog-heading-margin-left`: title left margin.
- `--cpt-modaldialog-heading-margin-top`: title top margin.
- `--cpt-modaldialog-heading-margin-bottom-compact`: compact title bottom margin.
- `--cpt-modaldialog-heading-margin-right-compact`: compact title right margin.
- `--cpt-modaldialog-subheading`: secondary heading colour.
- `--cpt-modaldialog-subheading-padding-block-start`: subheading top padding.
- `--cpt-modaldialog-subheading-padding-block-end`: subheading bottom padding.
- `--cpt-modaldialog-subheading-margin-inline`: subheading horizontal margin.
- `--cpt-modaldialog-icon`: heading icon colour.
- `--cpt-modaldialog-icon-gap`: heading icon gap.
- `--cpt-modaldialog-link`: action link colour.
- `--cpt-modaldialog-close`: close icon colour.
- `--cpt-modaldialog-close-top`: close action top offset.
- `--cpt-modaldialog-close-right`: close action right offset.
- `--cpt-modaldialog-close-padding`: close action padding.
- `--cpt-modaldialog-close-radius`: close action radius.
- `--cpt-modaldialog-wrap-padding`: wrapper padding.
- `--cpt-modaldialog-wrap-padding-slider`: slider wrapper padding.
- `--cpt-modaldialog-header-padding-inline`: header padding.
- `--cpt-modaldialog-header-padding-block`: header vertical padding.
- `--cpt-modaldialog-footer-padding-inline`: footer padding.
- `--cpt-modaldialog-footer-padding-block`: footer vertical padding.
- `--cpt-modaldialog-footer-height`: footer height.

## Notification Block

- `--cpt-notification-gap`: vertical gap between notifications.
- `--cpt-notification-accent`: accent colour.
- `--cpt-notification-border`: border/left rail colour.
- `--cpt-notification-surface`: card surface.
- `--cpt-notification-icon`: icon colour.
- `--cpt-notification-title`: title colour.
- `--cpt-notification-text`: body colour.
- `--cpt-notification-item`: list item colour.
- `--cpt-notification-radius`: radius.

## Table Standard

- `--cpt-table-header-bg`: header row background.
- `--cpt-table-header-border`: header divider colour.
- `--cpt-table-row-border`: body row divider colour.
- `--cpt-table-header-text`: header text colour.
- `--cpt-table-header-icon`: header icon colour.
- `--cpt-table-header-padding`: header cell padding.
- `--cpt-table-cell-padding`: body cell padding.
