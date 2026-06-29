# JUI Modals — reference

Exhaustive reference for the modal machinery. The patterns and decision-making are in
[SKILL.md](SKILL.md); load this file when you need an exact option, value, or interface.

Classes (package `com.effacy.jui.ui.client.modal`, plus `com.effacy.jui.ui.client.NotificationDialog`):
`Modal<V>`, `ModalDialog<V>` (extends `Modal`), `ModalDialogCreator`, `NotificationDialog`,
`ProgressSequence` / `ProgressSequenceCreator` (a multi-step progress modal — read the source if
needed).

## `ModalDialogCreator` — factory methods

| Method | Returns | Use |
|---|---|---|
| `build(C cpt, Consumer<ModalDialog.Config<C>> cfg)` | `ModalDialog<C>` | Wrap a component; call `.open()`. Also a `Supplier<C>` overload. |
| `buildWithRender(cfg, Consumer<ExistingElementBuilder> builder)` | `ModalDialog<IComponent>` | Build contents inline with a DomBuilder. |
| `dialog(C cpt, cfg, String cancelLabel, String applyLabel)` | `IDialogOpener<V1,V2>` | Reusable opener; pass `null` label to omit a button. |
| `dialog(C cpt, cfg, Consumer<…Action> cancelAction, Consumer<…Action> applyAction)` | `IDialogOpener<V1,V2>` | As above but configure each action fully (icon, style, testId). |
| `dialog(C cpt, cfg, Consumer<IActionConfiguration<C,V2>> actions)` | `IDialogOpener<V1,V2>` | Full control: declare any number of actions, each via `.cancel(...)` / `.button((cb, action) -> ...)`. |
| `create()` / `create(C cpt, cfg)` | `Config` / `ModalDialog` | Low-level; `create(cpt,cfg)` is deprecated in favour of `build`. |

Generic call form: `ModalDialogCreator.<V1, V2, C>dialog(new C(), cfg -> {...}, cancel -> ..., apply -> ...)`.

### `IDialogOpener<V1, V2>`

- `void open(V1 value, Consumer<Optional<V2>> cb)` — opens (no-op if already open). On open it: renders
  the modal first; if contents are `IEditable`, calls `edit(value)` (or `edit(IResolver<V1>)` when
  `value instanceof IResolver`); if contents are `IResetable`, calls `reset()`; then notifies any
  `listener(...)`.
- `ModalDialog<?> dialog()` — the underlying dialog (use `.contents()` to reach the wrapped component
  to set per-open state).
- `listener(Consumer<ModalDialog<?>>)` / `onOpen(...)` — invoked on each open.

### What the `dialog(...)` helper wires automatically

- **apply button** → `defaultHandler`: if contents are `IProcessable`, calls `process(outcome)`;
  `outcome` present → fires the callback + `success()` (close); empty → `fail()` (stay open). If not
  `IProcessable`, fires callback with empty and closes.
- **cancel button** → fires callback with `Optional.empty()` and closes; defaults to `link()` style and
  label "cancel".
- **close action** (header ✕) → fires callback with `Optional.empty()`.
- Content listeners: `ICloseListener.onCloseRequested()` → close; `IValueChangeListener.onValueChanged(v)`
  → callback `Optional.ofNullable(v)` + close; `IUpdateListener.onUpdate(v)` → callback only (no close).

## `ModalDialog.Config<C>` — options

Inherited from `Modal.Config`:

| Option | Default | Meaning |
|---|---|---|
| `type(Modal.Type)` | `CENTER` | Positioning — `CENTER`, `TOP`, `SLIDER`. |
| `width(Length)` | `80%` | Dialog width. |
| `maxWidth` / `minWidth(Length)` | — | Bounds (useful with `%` width). |
| `height(Length)` | — | Explicit height where relevant. |
| `minHeight(Length)` | — | Floor on content height — stops a dynamic dialog "bouncing". |
| `removeOnClose()` / `removeOnClose(boolean)` | `false` | Dispose + remove from DOM on close (transient). **Do not set on a reused shared dialog.** |

Added by `ModalDialog.Config`:

| Option | Default | Meaning |
|---|---|---|
| `title(String)` | — | Header title (HTML-safe; `updateTitle` at runtime). |
| `titleWrap()` / `titleWrap(boolean)` | `false` | Allow the title to wrap (else ellipsised). |
| `subtitle(String)` | — | Secondary header line. |
| `subtitleIcon(String)` | — | Icon (e.g. `FontAwesome.x()`) beside the subtitle. |
| `description(String)` | — | Paragraph under title/subtitle. |
| `closable()` / `closable(boolean)` / `closable(icon, text)` | `true` | Show the header close control (and customise its icon/text). |
| `padding(Length)` / `padding(Insets)` | none | Padding on the content area. |
| `variant(Variant)` | `STANDARD` | Visual style — see below. |
| `styles(ILocalCSS)` | standard | Replace the dialog's CSS wholesale (advanced). |
| `dialogCss(String)` | — | Inline CSS on the dialog box element. |
| `contentsCss(String)` | — | Inline CSS on the content area. |
| `closeAction(Invoker)` | — | Run when the close action fires (the opener uses this to push an empty result). |
| `action(Consumer<Action>)` / `action()` | — | Add a footer action button (see Actions). |
| `testId(String)` | — | Test id. |

### `Variant` (visual style)

`ModalDialog.Config.Variant` is a functional interface (`void configure(Config<?>)`). Built-ins:

- `Variant.STANDARD` — plain (no-op).
- `Variant.SEPARATED` — header/footer on the dialog surface colour with separated heading spacing.
- `Variant.UNIFORM` — uniform surface, larger heading, no footer divider, nested-modal offset tuning.

Apply with `cfg.variant(Variant.UNIFORM)`. The enum `ModalStyle` and `cfg.style(ModalStyle)` are
**deprecated** — use `variant(...)`. Projects layer their own: a `Variants` class exposes e.g.
`DIALOG_UNIFORM_OFFSET` which calls `Variant.UNIFORM.configure(cfg)` then adds app token overrides;
apply via `Variants.DIALOG_UNIFORM_OFFSET.configure(cfg)`.

### `Modal.Type` (positioning)

- `CENTER` — centred, repositions both axes; grows with content to a threshold then scrolls.
- `TOP` — near the top, repositions horizontally only.
- `SLIDER` — full-height panel that slides in from the right.

## Actions (`ModalDialog.Config<C>.Action`)

Created via `config().action(a -> ...)`. Builder methods:

| Method | Effect |
|---|---|
| `label(String)` | Button label. |
| `reference(Object)` | Reference for `handleAction`, and for `enable/disable/show/hide/updateLabel`. |
| `handler(IDialogActionHandler<C>)` | Inline handler; gets a callback with `success()` (close), `fail()`/`done()` (stay open), `contents()`, `modal()`. |
| `defaultHandler(Consumer<Optional<V2>>)` | The auto-apply behaviour (process `IProcessable` contents). |
| `icon(String)` / `icon(String, boolean right)` | Button icon (FontAwesome), optionally on the right. |
| `left(boolean)` | Place on the left of the footer. |
| `testId(String)` | Test id (prefixed `btn_`). |
| `register(BiConsumer<ModalDialog<C>, IButton>)` | Callback once the button is built. |
| `normal()` / `link()` / `danger()` / `outlined()` / `style(Button.Config.Style)` | Button style. |

Subclass overrides:
- `populateActions()` — called from `onBeforeRender()`; default adds the configured actions. Override
  to add actions imperatively (`addAction(component, left)`).
- `handleAction(Object reference, IDialogActionHandler.ICallback<V> cb)` — handle a referenced action;
  call `cb.success()` / `cb.fail()`.
- `actionHandler(reference, handler)` — replace a configured handler at runtime.

Runtime control by reference: `enable(refs…)`, `disable(refs…)`, `show(refs…)`, `hide(refs…)`,
`updateLabel(ref, label)`. Header text: `updateTitle`, `updateSubtitle`, `updateDescription`.
`openActions()` reveals the footer (auto-hidden when there are no actions). `closeAction()` mimics the
header close (runs `onCloseAction()` then `closeAction` invoker then `close()`).

## Lifecycle interfaces

Implement on the **contents** component:

| Interface | Method(s) | Purpose |
|---|---|---|
| `IOpenAware` | `onOpen()` | Notified when the modal opens. |
| `ICloseAware` | `onClose()` | Notified when the modal closes. |
| `IModalAware` (extends both) | `onModalCloseRequested(IModalController cb)` | Block/defer close; call `cb.close()` when safe (dirty guard). |
| `IEditable<V>` | `edit(V)` / `edit(IResolver<V>)` | Seed the form on open. |
| `IResetable` | `reset()` | Clear to clean baseline on open. |
| `IProcessable<R>` | `process(Consumer<Optional<R>>)` | Apply action work; present → close, empty → stay open. |
| `IDialogRegister` | `register(ModalDialog<?>)` | Contents receives a back-reference to its dialog on construction. |

Fire from the contents to drive the dialog: `IValueChangeListener.onValueChanged(v)` (callback +
close), `IUpdateListener.onUpdate(v)` (callback, no close), `ICloseListener.onCloseRequested()` (close),
`IModalController.close()` (close).

External (on the `Modal`/`ModalDialog`): `IModalListener` with `onOpen()`/`onClose()`
(`IModalListener.close(invoker)` / `.open(invoker)` factories). Static global hooks:
`Modal.addOpenHandler(...)`, `Modal.addCloseHandler(...)`, `Modal.setBlurTarget(el)`.

Modal close sequence: `close()` → `onCloseRequested(cb)` (delegates to `IModalAware` contents if any) →
`_close()` → `onClose()` → fire `IModalListener.onClose()` → `onAfterClose()` (disposes if
`removeOnClose`). Nested modals are z-index layered (levels z1–z6); open/close them in stack order.

## `NotificationDialog`

Transient dialogs (auto-disposed on close). Builders: `confirm(title)`, `alert(title)`, `error(title)`,
`save(title)`, `custom(Icon, title, Consumer<Builder>)`. Each returns a `Builder`:

| Builder method | Purpose |
|---|---|
| `notice(String)` | Add a body paragraph (repeatable). |
| `renderer(Consumer<ElementBuilder>)` | Custom body DOM instead of notices. |
| `width(Length)` | Default 400px. |
| `handler(Consumer<OutcomeType>)` | Outcome handler; auto-closes on return. |
| `handler(BiConsumer<OutcomeType, ICompletionCallback>)` | Outcome handler; close deferred until `done.complete()` (async). |
| `configurer(Consumer<ModalDialog.Config<NotificationDialogContent>>)` | Replace the default action set. |
| `close(Invoker)` | Run when the dialog closes. |
| `open()` | Show it. |

Shorthand statics exist: `confirm(title, notice, handler)`, `alert(title, notice, handler)`, etc.
(`notice` as `String` or `Consumer<ElementBuilder>`; `handler` as `Consumer<OutcomeType>` or
`BiConsumer<OutcomeType, ICompletionCallback>`).

`OutcomeType`: `OK`, `DISMISS`, `DISCARD` (`isOk()` helper). Button → outcome matrix:

| Button | Confirm | Alert | Error | Save | Outcome |
|---|---|---|---|---|---|
| Dialog close (✕) | ✓ | ✓ | ✓ | ✓ | `DISMISS` |
| Cancel | ✓ | — | — | ✓ | `DISMISS` |
| Dismiss | — | ✓ | ✓ | — | `DISMISS` |
| OK | ✓ | — | — | — | `OK` |
| Discard changes | — | — | — | ✓ | `DISCARD` |
| Save | — | — | — | ✓ | `OK` |

Icons are configurable globally via static fields `CLS_CONFIRM`, `CLS_ALERT`, `CLS_ERROR`,
`CLS_ACTION_OK`, `CLS_ACTION_DISMISS`. Labels via the `Labels` messages interface.

## Gotchas

- **Shared dialog + `removeOnClose()` don't mix** — a reused `IDialogOpener` dialog must persist; only
  one-off/notification dialogs are transient.
- **Stale values on reopen** — a reused dialog keeps its last state; implement `IResetable` (or rely on
  `ControlForm` reset) so each open starts clean. Set per-open targets on `dialog().contents()` before
  `open(...)`.
- **Empty vs present result** — returning `Optional.empty()` from `process(...)` keeps the dialog open
  (treat validation failure this way); only a present value closes it.
- **`edit` runs after render** — the opener renders before calling `edit`/`reset`, so those methods can
  touch the DOM safely.
- **Confirm-before-close** belongs on the contents via `IModalAware.onModalCloseRequested`, not on the
  dialog.
