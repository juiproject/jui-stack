# JUI standard catalogue

A reference of the off-the-shelf controls, components and fragments shipped in **jui-ui**
(`com.effacy.jui.ui.client.*`), plus the core layouts (`com.effacy.jui.core.client.component.layout.*`).

Load this file when you need to know *what already exists* before building something new — prefer
composing or extending a standard artefact over hand-rolling one. These are also reference
implementations: when you do need a custom variant, read the closest standard one first (see
[Inspecting the JUI source](SKILL.md#inspecting-the-jui-source)).

> **Check the host project's own artefacts first.** This catalogue is the **framework** standard set.
> The application you are working in will usually add its own custom components, controls and fragments,
> plus a dedicated **`Variants`** class of named looks/configurations. Prefer those for consistency —
> search the project's UI packages — and only fall back to the standard artefacts below, or build new,
> when nothing project-specific fits.

Naming conventions you will see throughout:

- `XxxCreator` — a helper with `build(cfg)` (construct) and `$(parent, cfg)` (build-and-insert) methods.
- `Controls` — a single helper with a factory method for every standard control.
- `Config` — the inner builder class configured via a `Consumer<Config>` lambda.

---

## Controls (`…client.control`)

Form fields with value management, dirty detection, validation and focus. Construct via the
`Controls` helper (or the per-control `…Creator`). To build your own, use the **jui-controls** skill.

| Control | `Controls` factory | Description |
|---|---|---|
| `TextControl` | `Controls.text` | Single-line text input; placeholder, left/right icons, clear action, variants, validators. |
| `TextAreaControl` | `Controls.textarea` | Multi-line text input (configurable rows). |
| `TextSearchControl<S>` | `Controls.textsearch` | Text input with type-ahead search over a fixed set or a store of candidate values. |
| `NumberControl` | `Controls.number` | Numeric input with min / max / step. |
| `CalendarControl` | `Controls.calendar` | Date picker. |
| `SelectionControl<V>` | `Controls.selector` | Single-select dropdown over a fixed or store-backed set of values (see [cpt_selection.md](https://github.com/juiproject/jui-stack/blob/main/docs/cpt_selection.md)). |
| `MultiSelectionControl<V>` | `Controls.multiselector` | Multi-select dropdown. |
| `CheckControl` | `Controls.check` | A single checkbox (boolean value). |
| `MultiCheckControl<V>` | `Controls.checkMulti` | A group of checkboxes yielding a set of values. |
| `SelectionGroupControl<V>` | `Controls.checkGroup` / `Controls.radioGroup` | A laid-out group of checkbox or radio options. |
| `FileUploadControl` | `Controls.fileUpload` | File upload (see [cpt_files.md](https://github.com/juiproject/jui-stack/blob/main/docs/cpt_files.md)). |
| `PanelSelectionControl` | — | Multi-selection rendered as a grid of selectable tiles. |
| `AvatarSelectorControl` | — | Select / upload an avatar image. |

Controls are normally assembled into a form with **`ControlForm`** (`…client.control.builder`), which
handles labels, guidance, validation display and conditional show/hide. See
[ess_controls.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_controls.md).

---

## Components (`…client.*`)

Larger UI building blocks. Construct via the matching `…Creator`. To build your own, use the
**jui-components** skill.

### Containers & panels (`…client.panel`)

| Component | Creator | Description |
|---|---|---|
| `Panel` | `PanelCreator` | The workhorse scrollable container; holds child components under a configurable layout. |
| `PanelBase` | — | Base class to extend when authoring a custom panel. |
| `SplitPanel` | `SplitPanelCreator` | Two-region split (e.g. an action bar plus a body). |
| `TriSplitPanel` | — | Three stacked regions — top toolbar, central body, bottom footer — each with its own layout. |
| `TitlePanel` | `TitlePanelCreator` | A panel with a titled header. |

### Navigation (`…client.navigation`)

| Component | Creator | Description |
|---|---|---|
| `TabNavigator` | `TabNavigatorCreator` | Tabbed navigation across child components; participates in the navigation/breadcrumb hierarchy. Variants: `HORIZONTAL`, `HORIZONTAL_UNDERLINE`, `HORIZONTAL_BAR`, `VERTICAL`, `VERTICAL_ICON`, `VERTICAL_ALT`, `VERTICAL_COMPACT`. |
| `CardNavigator` | — | A stack of named "cards" navigated by reference (e.g. gallery → detail → sub-detail); breadcrumb-aware, lazily activates cards. |
| `TabCollection` | — | Lower-level collection of tabs used by the navigators. |

See [ess_navigation.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_navigation.md).

### Data display (`…client.table`, `…client.gallery`)

| Component | Creator | Description |
|---|---|---|
| `Table` | `TableCreator` | Data table bound to a store; configurable columns and cell renderers. |
| `BuilderTableCellRenderer` / `TextTableCellRenderer` | — | Cell renderers — build arbitrary DOM into a cell, or render plain text. |
| `Gallery` | `GalleryCreator` | Store-backed list/grid with pagination and empty / filtered-empty / error states. `GRID` and `ROW` styles. |
| `GalleryItemCreator` | — | Supplies the per-item renderer for a `Gallery`. |

Tables and galleries are driven by **stores** (`PaginatedStore`, `ISearchStore`, …). See
[ess_galleries.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_galleries.md).

### Buttons (`…client.button`)

| Component | Creator | Description |
|---|---|---|
| `Button` | `ButtonCreator` | Standard button component — label, icon, and a handler with async completion callback. For lightweight inline buttons prefer the `Btn` fragment. |

### Modals & dialogs (`…client.modal`)

| Component | Creator | Description |
|---|---|---|
| `Modal` / `ModalDialog` | `ModalDialogCreator` | Modal dialog framework. `ModalDialogCreator.dialog(...)` wraps a component into a dialog with cancel/confirm actions; the component may implement `IProcessable<R>` (return a result) or `IEditable<T>` (receive data). |
| `ProgressSequence` | `ProgressSequenceCreator` | Multi-step progress / wizard modal. |

See [ess_modals.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_modals.md).

### Feedback & notifications (`…client`)

| Component | Creator | Description |
|---|---|---|
| `Notifier` | — | Transient toast notifications: `Notifier.create().text("…").show(2000)`. |
| `NotificationDialog` | — | Confirm / alert dialogs: `NotificationDialog.confirm(title, message, outcome -> …)`. |
| `InfoBlock` | `InfoBlockCreator` | Inline informational block. |
| `NotificationBlock` | `NotificationBlockCreator` | Inline notification / alert block. |

### Utilities & tooling (`…client`)

| Class | Description |
|---|---|
| `Theme` | Theme access and registration. |
| `Printer` | Print support. |
| `FileDownloader` | Trigger a file download. |
| `Clipboard` | Clipboard read/write. |
| `ComponentExplorer` (`…client.explorer`) | Developer tool — a browseable gallery of components (the playground explorer). |

### Layouts (`…core.client.component.layout`)

Layouts position the children of a `Panel` / region. Pass a layout config when configuring a panel
or region point.

| Layout | Creator | Description |
|---|---|---|
| `ActionBarLayout` | `ActionBarLayoutCreator` | Horizontal zones (left / centre / right) with insets — toolbars and action bars. |
| `CardFitLayout` | `CardFitLayoutCreator` | Stacks children and shows one at a time (hide/show); backs `CardNavigator`. |
| `VertLayout` | `VertLayoutCreator` | Vertical stacking with configurable gaps. |
| `MinimalLayout` | — | Minimal pass-through layout (children fill the target). |

---

## Fragments (`…client.fragments`)

Reusable DOM building blocks (not full components) inserted into a builder via `Xxx.$(parent, …)`.
They are the lightest-weight building block. To author your own, use the **jui-fragments** skill.

| Fragment | Description |
|---|---|
| `Btn` | Inline button — label, icon and `onclick`; variants/recipes (`STANDARD`, `STANDARD_EXPANDED`, `OUTLINED`, …). |
| `IconBtn` | Icon-only button with an on-click handler. |
| `ToggleBtn` | A simple on/off toggle button. |
| `Icon` | Renders an icon (e.g. a FontAwesome class). |
| `Card` | A card surface container (optionally hover-lift). |
| `CardHeader` | A header row for a `Card`. |
| `Stack` | A flex stack (vertical or horizontal) with a gap. |
| `Box` | A generic box / flex container. |
| `Paper` | An elevated surface (shadow). |
| `Menu` | A dropdown menu container holding `MenuItem`s. |
| `MenuItem` | An item within a `Menu` — icon, label, variant, `onclick`. |
| `MenuActivator` | Wraps a trigger element that activates a menu / popup on click. |
| `Accordion` | A collapsible panel — header row (caret, optional icon, title, summary slot) plus a body of inserted children. |
| `Expander` | A disclosure expander. |
| `Divider` | A horizontal divider / separator. |
| `Notice` | An inline notice / callout block. |
| `Loading` | A pulsing loading-placeholder indicator. |
| `ProgressBar` | A progress bar. |
| `PercentageLine` | A horizontal percentage bar. |
| `PercentageGuage` | A circular percentage gauge. |
| `Avatar` | A user avatar (initials or image). |
| `ChoiceSelector` | An inline segmented choice selector. |
| `ControlField` | Wraps a control with a label, guidance and validation message. |
| `Dialog` | An inline (non-modal) dialog fragment. |
| `Popup` | A simple inline popup display. |
| `Para` | A paragraph text fragment. |
| `Typography` | Typographic text styles. |

> Many fragments ship a sibling `XxxDocumentation` class in the same package — a live, worked
> example of the fragment used in the component explorer. Read it for usage patterns.

---

## Keeping this catalogue current

This list is maintained by hand. When the JUI version changes, re-derive it from source:

```bash
# Controls — the authoritative list is the Controls helper's factory methods
grep -E "public static .* \w+ ?\(" <jui-ui>/.../client/control/Controls.java

# Components / fragments — list the package contents
ls <jui-ui>/.../client/{panel,navigation,table,gallery,button,modal,fragments}
```

where `<jui-ui>` is the jui-stack source module or an extracted jar (see the SKILL).
