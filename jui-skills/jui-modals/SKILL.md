---
name: jui-modals
description: "Build modals and dialogs in the JUI framework — floating UI for create/edit forms, confirmations, alerts and custom panels. Use this skill whenever the work involves a modal, dialog, popup, create form, edit/update form, delete confirmation, confirm/alert/error/save prompt, slide-out panel, or wrapping a component so it opens over the page. Triggers include: Modal, ModalDialog, ModalDialogCreator, IDialogOpener, NotificationDialog, modal dialog, dialog opener, open() pattern, create/update form pair, IProcessable, IEditable, IResetable, IModalAware, confirm dialog, 'open a form in a dialog', 'dialog-enable a component', actions / footer buttons on a dialog."
---

# JUI Modals & Dialogs

A **modal** floats a behaviour out of the main application to focus attention and action on it
(create/edit forms, confirmations, alerts, slide-out panels). JUI's modal machinery lives in
`com.effacy.jui.ui.client.modal` plus `NotificationDialog`.

This skill assumes familiarity with **components** (the thing inside a dialog is almost always a
component or `ControlForm`) and **controls** (the form fields). Build those with the **jui-components**
and **jui-controls** skills; this skill covers wrapping them in, and driving, a dialog.

## The machinery — pick the right entry point

| You want… | Use | Notes |
|---|---|---|
| A reusable **create / edit form** opened from several places, returning a result | `ModalDialogCreator.dialog(...)` → `IDialogOpener` | **The dominant pattern.** See [The dialog-enabling pattern](#the-dialog-enabling-pattern). |
| A **confirmation / alert / error / save** prompt | `NotificationDialog.confirm/alert/error/save(...)` | One-liner; transient (auto-disposed). |
| A **one-off** dialog wrapping some inline content | `ModalDialogCreator.build(component, cfg)` then `.open()` | Quick; usually `removeOnClose()`. |
| A **bespoke dialog** with custom actions / lifecycle | subclass `ModalDialog<C>` | Override `populateActions()` / `handleAction(...)`. |
| A **bare modal** with no window chrome (you draw everything) | `Modal<C>` | Rare — only minimal positioning; contents supply all chrome and the close control. |

`ModalDialog` (the window: header with title/subtitle/close, scrollable body, footer of action buttons)
extends `Modal` (bare positioning only). Almost always you want `ModalDialog`, and almost always you
reach it through `ModalDialogCreator` rather than constructing it directly.

> Reference detail — every `Config` option, the `Variant`/`Type` values, the full set of lifecycle
> interfaces, and the NotificationDialog button/outcome matrix — lives in
> **[reference.md](reference.md)**. Load it when you need the exhaustive list.

## The dialog-enabling pattern

This is the pattern behind virtually every form-in-a-dialog in a JUI app. You **dialog-enable** a
content component: it carries a static `open(...)` method and a single shared `IDialogOpener` built
once and reused.

```java
public class MyCreateForm extends ControlForm<Void, Void> implements IProcessable<Long> {

    // One shared opener, built lazily on first open and reused thereafter.
    private static IDialogOpener<Void, Long> DIALOG;

    public static void open(Consumer<Optional<Long>> cb) {
        if (DIALOG == null) {
            DIALOG = ModalDialogCreator.<Void, Long, MyCreateForm>dialog(new MyCreateForm(), cfg -> {
                cfg.variant(ModalDialog.Config.Variant.UNIFORM)   // or a project Variants.* variant
                    .title("New thing")
                    .type(Type.CENTER)
                    .width(Length.px(480));
            }, cancel -> cancel.label("Cancel"), apply -> apply.label("Create"));
        }
        DIALOG.open(null, cb);
    }

    public MyCreateForm() {
        super(ControlFormCreator.createForDialog());
        control("name", "Name", Controls.text(cfg ->
            cfg.validator(NotEmptyValidator.validator("please provide a name"))),
            cell -> cell.grow(1).required());
    }

    @Override
    public void process(Consumer<Optional<Long>> outcome) {
        if (!validate()) {
            outcome.accept(Optional.empty());   // empty → the apply action "fails" → dialog stays open
            return;
        }
        // ... remote call ...
        // outcome.accept(Optional.of(newId));  → non-empty → dialog closes, caller's cb receives the id
    }
}
```

Call site: `MyCreateForm.open(outcome -> outcome.ifPresent(id -> refresh(id)));`

### The generics: `IDialogOpener<V1, V2>`

- **`V1`** — the *assignment* (input) type passed to `open(V1, cb)` to seed the dialog. `Void`/`null`
  for a create.
- **`V2`** — the *result* type handed back. The callback is `Consumer<Optional<V2>>`: **empty means
  cancelled/closed** (take no action); present means success.

### How the opener talks to your content component (the contract)

The `dialog(...)` helper wires your component up by interface — implement what you need:

| Interface | When called | Purpose |
|---|---|---|
| `IEditable<V1>` | on open, after render, before reset | `edit(value)` seeds the form. If `V1` is an `IResolver`, `edit(IResolver<V1>)` is used (resolve-then-populate). |
| `IResetable` | on open, after any edit | `reset()` re-establishes the clean baseline (clears dirty state). |
| `IProcessable<V2>` | when the **apply** action is clicked | `process(outcome)` does the work. Accept `Optional.of(result)` to close, `Optional.empty()` to keep open (e.g. validation failed). This is the *default handler* the apply button gets automatically. |

And events your content can **fire** to drive the dialog (alternative to an apply button):

| Fire | Effect |
|---|---|
| `IValueChangeListener.onValueChanged(v)` | callback gets `Optional.ofNullable(v)` **and the dialog closes** |
| `IUpdateListener.onUpdate(v)` | callback gets the value **without** closing (live updates) |
| `ICloseListener.onCloseRequested()` | dialog closes programmatically from within the contents |

The **cancel** action (first label) closes with `Optional.empty()`. Pass `null` for a label to omit
that button. The close "✕" in the header behaves like cancel.

### Reusing one shared instance across opens (per-open targets)

Because the opener is a single shared instance, anything that varies per open is set on the contents
**before** `open(...)`. Two idioms:

```java
// (a) IEditable — pass the per-open value in; the form's edit(...) populates it.
ProductUpdateForm.open(resolverForProduct, cb);   // V1 = IResolver<ProductLookupResult>

// (b) Direct field — reach the shared contents and set a config record, then open.
((DocumentCreateForm) DIALOG.dialog().contents()).config = config;
DIALOG.open(null, cb);
```

Use `IResetable.reset()` (option b's form still needs its controls reset) so a reused dialog never
shows stale values from the previous open. A shared dialog must **not** be `removeOnClose()` — that is
for transient one-shot dialogs.

## The create / update pair

A common arrangement: an **update** form extends the **create** form, reusing its fields and adding
edit/persist behaviour. Each has its **own** static `DIALOG` and `open(...)` (the shared-instance
field is per class, not inherited state to share).

```java
// Create form: ControlForm<LookupResult, Command>, implements IProcessable<LookupResult>.
public class ProductCreateForm extends ControlForm<ProductLookupResult, ProductCommand>
        implements IProcessable<ProductLookupResult> {

    private static IDialogOpener<Void, ProductLookupResult> DIALOG;

    public static void open(Consumer<Optional<ProductLookupResult>> cb) { /* dialog(...).open(null, cb) */ }

    public ProductCreateForm() { this(true); }

    // Subclass-friendly constructor: the boolean lets the update form vary construction.
    protected ProductCreateForm(boolean create) {
        super(ControlFormCreator.createForDialog());
        group(grp -> grp.row(row -> row.control("name", "Product name", GeneralControls.text(c -> c.acceptor("name")),
            cell -> cell.required().grow(1).from(v -> v.getName()).to((ctx, v, cmd) -> cmd.name(v)))));
    }

    @Override
    public void process(Consumer<Optional<ProductLookupResult>> outcome) { /* construct + remote create */ }
}
```

```java
// Update form: extends the create form, supplies its own DIALOG and an IResolver-based open.
public class ProductUpdateForm extends ProductCreateForm {

    private static IDialogOpener<IResolver<ProductLookupResult>, ProductLookupResult> DIALOG;

    public static void open(IResolver<ProductLookupResult> resolver, Consumer<Optional<ProductLookupResult>> cb) {
        if (DIALOG == null)
            DIALOG = ModalDialogCreator.<IResolver<ProductLookupResult>, ProductLookupResult, ProductUpdateForm>dialog(
                new ProductUpdateForm(), cfg -> cfg.variant(Variant.UNIFORM).title("Update product").type(Type.CENTER).width(Length.px(525)),
                cancel -> cancel.label("cancel"), apply -> apply.label("Update"));
        DIALOG.open(resolver, cb);
    }

    public ProductUpdateForm() { super(false); }   // reuse create's fields, skip create-only setup

    @Override
    public void process(Consumer<Optional<ProductLookupResult>> outcome) { /* retrieve(cmd) by id + remote update */ }
}
```

Key points:
- The create form is a `ControlForm<V, C>` where `V` is the lookup result (source value, drives
  `from(...)`) and `C` is the command (drives `to(...)`); `retrieve(command)` collects field values
  into the command.
- The update form, being `IEditable` via `ControlForm`, gets `edit(resolver)` on open which loads the
  source and populates the fields; its `process(...)` builds a `byId` command and re-assigns the
  refreshed result.
- Keep construction differences behind the `protected XxxForm(boolean create)` constructor — fields
  shared, create-only bits guarded by the flag.

## Notification dialogs (confirm / alert / error / save)

One-liners for standard prompts. Transient — removed from the DOM after closing.

```java
NotificationDialog.confirm("Delete item")
    .notice("Are you sure? This cannot be undone.")
    .handler((outcome, done) -> {
        if (outcome.isOk()) {            // OutcomeType.OK
            doDelete();
            done.complete();             // close (call after async work if you defer)
        } else
            done.complete();             // DISMISS / DISCARD
    }).open();
```

- **confirm** → cancel + OK. **alert** / **error** → dismiss only. **save** → cancel + discard + save.
- `OutcomeType` is `OK`, `DISMISS`, `DISCARD` (`outcome.isOk()` helper).
- `handler(Consumer<OutcomeType>)` closes automatically on return; `handler(BiConsumer<OutcomeType,
  ICompletionCallback>)` defers closing until you call `done.complete()` (use for async confirmation).
- Body via `.notice("...")` (repeatable, one paragraph each) or `.renderer(builder -> ...)` for custom
  DOM. `.width(...)`, `.close(...)`. The shorthand statics `confirm(title, notice, handler)` etc. exist
  too. Full button/outcome matrix in [reference.md](reference.md).

## One-off and inline dialogs

When a dialog is opened from a single place and needs no result plumbing, wrap a component directly:

```java
ModalDialogCreator.build(ComponentCreator.$(p -> p.text("Hello!")), cfg -> {
    cfg.title("Example").width(Length.px(300)).removeOnClose();
    cfg.action(a -> a.label("Confirm"));     // no handler → just closes
}).open();
```

`buildWithRender(cfg, builder -> ...)` builds the contents inline with a DomBuilder. Use
`removeOnClose()` for these so they dispose after use.

## Bespoke dialogs (subclassing `ModalDialog`)

For dialogs with their own actions, multi-button footers, or action references you show/hide/enable at
runtime, subclass `ModalDialog<C>`. Actions are buttons in the footer; each handler calls `success()`
(close) or `fail()`/`done()` (stay open).

```java
public class MyDialog extends ModalDialog<TextComponent> {
    public MyDialog() {
        super(new ModalDialog.Config<TextComponent>()
            .title("Example").width(Length.px(400)).closable().removeOnClose(),
            new TextComponent("Body"));
        config().action(a -> a.label("dismiss").reference("close").link());
        config().action(a -> a.label("Open another").reference("open"));
    }

    @Override
    protected void handleAction(Object reference, IDialogActionHandler.ICallback<TextComponent> cb) {
        if ("close".equals(reference)) { cb.success(); return; }
        if ("open".equals(reference))  { new MyDialog().open(); cb.fail(); }
    }
}
```

Actions can also carry inline handlers (`a.handler(cb -> { ... cb.success(); })`). With references you
get `enable/disable/show/hide(reference...)`, `updateLabel(reference, label)`, and the dialog can react
to content state. Override `populateActions()` to add actions imperatively. See
[reference.md](reference.md) for the action API, button styles, and `updateTitle/Subtitle/Description`.

## Closing behaviour & confirm-before-close

- The header "✕", a cancel action, and `closeAction()` all route through the close sequence; a content
  component implementing `ICloseAware`/`IOpenAware` is notified.
- To **block or defer** a close (e.g. unsaved changes), have the **contents** implement `IModalAware`
  and override `onModalCloseRequested(IModalController cb)` — only call `cb.close()` when it is safe
  (e.g. after a `NotificationDialog.save(...)` confirm). This is the dirty-guard hook.
- `Modal.setBlurTarget(el)` blurs a background element while any modal is open, for stronger focus.

## Styling

- **Variant** (overall look): `ModalDialog.Config.Variant.STANDARD` / `SEPARATED` / `UNIFORM` via
  `cfg.variant(...)`. (The old `ModalStyle` enum / `cfg.style(...)` is deprecated — use `variant`.)
- **Project variants**: prefer the app's `Variants` class — e.g.
  `Variants.DIALOG_UNIFORM_OFFSET.configure(cfg)` layers app tokens on top of `UNIFORM`. Check for one
  before hand-rolling CSS, for consistency.
- **Type** (positioning): `Modal.Type.CENTER` (default), `TOP`, `SLIDER` (slides in from the right).
- Sizing: `width` / `maxWidth` / `minWidth`, `minHeight` (stops a dynamic dialog "bouncing"),
  `padding`. Per-dialog CSS via `dialogCss(...)` / `contentsCss(...)`. For deeper CSS work see the
  **jui-styles** skill. Custom forms inside a dialog use their own `@CssResource` localised CSS as
  usual (see the delete-form example pattern).

## Workflow

1. **Classify** (table at top): reusable form → `dialog(...)`/`IDialogOpener`; standard prompt →
   `NotificationDialog`; one-off → `build(...)`; bespoke actions → subclass `ModalDialog`.
2. **Build the contents first** as a component / `ControlForm` (jui-components, jui-controls). Use
   `ControlFormCreator.createForDialog()` for form contents.
3. **Dialog-enable it** — static `DIALOG` + `open(...)`; implement `IProcessable` (and `IEditable` /
   `IResetable` for edit forms). Decide `V1`/`V2`.
4. **Reuse the shared instance correctly** — set per-open state on the contents before `open(...)`;
   don't `removeOnClose()` a shared dialog; rely on `reset()` to clear stale values.
5. **Wire results** at the call site via the `Consumer<Optional<V2>>` (empty = cancelled).
6. **Reuse the project's `Variants`** for the look; only add CSS if nothing fits.
7. For edit-extends-create, give the update form its **own** `DIALOG`/`open(...)` and a
   `protected XxxForm(boolean)` constructor.
