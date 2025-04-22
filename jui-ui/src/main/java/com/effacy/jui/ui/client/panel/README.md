# Overview

Panels provide a mechanism for setting out components in a pre-defined manner (specified by a layout) and are often employed as a base-class into which content is added. They have a lineage in older UI design patterns that were commonplace in desktop applications; in a contemporary setting they have been marginalised in lieu of bespoke layouts (including use of fragments) where one tends to have direct control over DOM construction. However, they do have their place (particularly with respect to layout behaviour, such as `CardFitLayout` that allows content to be swapped in-and-out in a standard manner) and can serve to provide consistency across a range of applications.

# Classes

## `Panel`

The most primitive type of panel that has a single content area and an `add(...)` method to add components to that area.  The default layout is `CardFitLayout` but can be re-assigned through configuration. Additional configuration allows for assigment of spacing and scrolling.

```java
public class MyCollectionOfExamples extends Panel {

    public MyCollectionOfExamples() {
        super (new Panel.Config ()
            .scrollable ()
            .layout (VertLayoutCreator.create (Length.em(2)))
            .padding (Insets.em (2)));

        // Since we are using the VertLayout these three instance will be
        // rendered down the page (with a 2em spacing).
        add (new MyExample1());
        add (new MyExample2());
        add (new MyExample3());
    }
}
```

Note that when `CardFitLayout` is used the `activate(IComponent)` method delegates through to the layout so the given component is shown.

## `SplitPanel`

Similar to `Panel` but provides two content areas (the *main* and the *other*) which can be arranged vertically or horizontally. The main area can be configured similarly to `Panel`. This is often used for galleries and tables where filtering is required.

```java
public class MyThingTable extends SplitPanel {

    private IStore<Thing> store = ...;

    public MyThingTable() {
        super (new SplitPanel.Config ()
            .otherLayout (
                new ActionBarLayout.Config ()
                    .zone(HAlignment.LEFT, Length.em (2))
                    .zone(HAlignment.RIGHT).insets (Insets.em (1))
                    .build ())
            .vertical ()
            .separator ());

        // Search control in zone 0 (action bar layout).
        addOther (Controls.text (cfg -> cfg
            .iconLeft (FontAwesome.search ())
            .placeholder ("Search things")
            .clearAction ()
            .modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                store.query (q -> q.setKeywords (val));
            }))), new ActionBarLayout.Data (0));

        // Table in the main area. 
        Table<Thing> table = TableCreator.build (cfg -> {
            cfg.header ("Name", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    ...
                });
            });
            ...
        }, store);
        add (table);
    }
}
```

## `TriSplitPanel`

Similar to `SplitPanel` but admits three content areas (*main*, *top* and *bottom*). This is often used for tables and galleries (as with `SplitPanel`) but where a footer is needed.

```java
public class MyThingTable extends TruSplitPanel {

    private IStore<Thing> store = ...;

    private ValueStateVariable<Integer> totalResults = new ValueStateVariable<>(0);

    public MyThingTable() {
        super (new TriSplitPanel.Config ()
            .topLayout (
                new ActionBarLayout.Config ()
                    .zone (HAlignment.LEFT, Length.em (2))
                    .zone (HAlignment.RIGHT)
                    .insets (Insets.em (1))
                    .build ())
            .bottomLayout (
                new ActionBarLayout.Config ()
                    .zone (HAlignment.LEFT, Length.em (2))
                    .zone (HAlignment.RIGHT)
                    .insets (Insets.em (1, 0.5))
                    .build ())
            .separator ()
        );

        // Update state variable for results count.
        store.handleOnChange(s -> {
            totalResults.assign(s.getTotalAvailable());
        });

        // Search control in zone 0 (action bar layout).
        addTop (Controls.text (cfg -> cfg
            .iconLeft (FontAwesome.search ())
            .placeholder ("Search things")
            .clearAction ()
            .modifiedHandler (DelayedModifiedHandler.create (300, (ctl, val, prior) -> {
                store.query (q -> q.setKeywords (val));
            }))), new ActionBarLayout.Data (0));

        // Table in the main area. 
        Table<Thing> table = TableCreator.build (cfg -> {
            cfg.header ("Name", header -> {
                header.renderer (BuilderTableCellRenderer.create ((cell, r) -> {
                    ...
                });
            });
            ...
        }, store);
        add (table);

        // Results counter.
        addBottom(new CountIndicator(totalResults, "things"), new ActionBarLayout.Data (1));
    }
}
```

## `TitlePanel`

Much like `Panel` but allows for a title at the top. *This is retained for historical reasons.*

```java
public class MyProgression extends TitlePanel {

    private IStore<Thing> store = ...;

    public MyThingTable() {
        super (new TitlePanel.Config ()
            .style (TitlePanel.Style.SPLIT)
            .title ("Progression"));

        // There are the items in the progression. The use of 'activate'
        // is passed through to the card fit layout to show the given
        // component.
        IComponent[] pages = new IComponent[] {
            ComponentCreator.build(root -> {
                P.$(root).text ("Page 1");
                Btn.$(root, "Next").onclick(() -> activate(pages[1]));
            }),
            ComponentCreator.build(root -> {
                P.$(root).text ("Page 2");
                Btn.$(root, "Back").onclick(() -> activate(pages[0]));
                Btn.$(root, "Next").onclick(() -> activate(pages[2]));
            }),
            ComponentCreator.build(root -> {
                P.$(root).text ("Page 3");
                Btn.$(root, "Back").onclick(() -> activate(pages[1]));
            }),
        };
        for (Page page : pages)
            add(page);
    }
}
```