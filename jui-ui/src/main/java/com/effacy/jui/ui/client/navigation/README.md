# Overview

Navigators are panels whose children are reached by navigation: `TabNavigator` draws a strip of tabs (horizontal or vertical, in one of several variants) and `CardNavigator` a stack of named cards. Both take part in the navigation hierarchy (references, deep links, breadcrumbs); see [ess_navigation.md](https://github.com/juiproject/jui-stack/blob/main/docs/ess_navigation.md) for the model and for general usage.

This document covers parts of `TabNavigator` and `TabCollection` that the general documentation does not.

# `TabNavigator`

## Tools at the end of the strip

Components can be drawn at the far end of the tab strip: at the right of a horizontal strip, centred in its height, and at the foot of a vertical one. Use them for something that belongs on the row of tabs but is not a tab, such as a selector, a button or a count.

```java
TabNavigatorCreator.create (cfg -> {
    cfg.variant (TabNavigator.Config.Variant.HORIZONTAL_UNDERLINE);
    cfg.tab ("overview", "Overview", new OverviewPage ());
    cfg.tab ("people", "People", new PeoplePage ());
    cfg.tools (ButtonCreator.build (b -> b.label ("Invite").handler (() -> invite ())));
});
```

or, in a subclass, `tools (...)` before render. Calls add to the components already given.

* The components are inserted with `Cpt`, so they are rendered with the navigator and disposed of with it.
* A navigator without tools is built exactly as before.
* The tools belong to the built-in strip. They are hidden with it (`hideNavigation()`), and with a configured `presenter(...)`, which replaces it.

### Structure

With tools, the strip (the `header`) and a cell for the tools share one grid cell (`ToolsCSS.strip()`), which stands in the wrap where the strip would. The tools cell (`ToolsCSS.tools()`) is laid over the far end of the strip; in a vertical navigator it sits at the foot (`ToolsCSS.bottom()`). The grid cell carries `position: relative` and `z-index: 1`, so it stacks above the body. The strip keeps what the wrap gave it before, `position: relative` and the navigator's background (`--cpt-navigator-bg`); the background stays on the strip rather than the grid cell so that a variant with a margin round the strip (the bar) keeps the margin clear.

The tools never change the strip's size or the layout around it. The cell takes the strip's height (horizontal) or width (vertical) without adding to it, so the strip, its tabs and the body sit exactly where they would without tools. Tools taller than a horizontal strip, or wider than a narrow vertical one (the icon-only and compact variants), overflow it: keep them to an icon there, or adjust the inset.

The tools are laid beside the strip rather than in it because the variants style every list in the strip as its tabs (`.tabs ul`, `.tabs ul li`), and some clip the strip (`overflow: hidden` in the underline variant). A `Menu` the tools open (for example through a `MenuActivator`) is therefore neither restyled nor cut off, and drops down over the body. `ToolsCSS` is a stylesheet of its own, used only where there are tools, so no variant's CSS changed.

| CSS variable | Default | Description |
|---|---|---|
| `--cpt-tabnavigator-tools-inset` | `--jui-space-5` | Inset of the tools from the end of the strip (the right of a horizontal strip; all round at the foot of a vertical one). |

In test mode the tools cell carries the test reference `tools` (`test-ref="tools"`). `TabNavigatorTester.validateTools(boolean)` checks for it.

# `TabCollection`

## Hidden groups

A group can be hidden:

```java
group ("Analysis").hidden ();
tab ("analysis", "Analysis", analysisNavigator);
```

The tabs of a hidden group are real tabs of the navigator. They are registered and navigable by reference, by deep link (`…/analysis/moves`), through `activate(...)` and `navigate(...)`, and they pass through the navigation handlers (`handleNavigation(...)`). They are not drawn. While one of them is active no drawn tab shows as active.

Presenters are not told of hidden groups. `_describe()` leaves them out, and `_updated(...)` sends no updates for their tabs. A hidden group is never marked `first`.

This suits a page reached from something other than a tab, such as a menu in the strip's tools. In the example below, the tools hold a selector whose menu reaches the pages of a hidden tab. That tab's component is a navigator of its own, with its strip hidden:

```java
public class Landscape extends TabNavigator {

    public Landscape() {
        super (TabNavigatorCreator.config ().variant (TabNavigator.Config.Variant.HORIZONTAL_UNDERLINE));

        tab ("overview", "Overview", new OverviewPage ());

        group ("Analysis").hidden ();
        tab ("analysis", "Analysis", TabNavigatorCreator.create (cfg -> {
            cfg.tab ("comparison", "Comparison", new ComparisonPage ());
            cfg.tab ("moves", "Moves", new MovesPage ());
        }, nav -> nav.hideNavigation ()));

        tools (new AnalysisSelector (page -> navigate ("analysis", page)));
    }
}
```

`TabNavigatorTester.validateTabsNotDrawn(String...)` checks that tabs are not drawn. The playground has a working example under *Test suite*, *Tabs*, *Tools & hidden* (`TabNavigatorTools`).
