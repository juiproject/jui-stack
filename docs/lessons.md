# Lessons 

## Purpose

Within are a collection of lessons to help you become familiar with JUI. Most of these are oriented around small components used to demonstrate specific principles leaving a more comprehensive case to the [Tutorial](tutorial.md):

1. [Lesson 1](lessons_1.md) introduces components, from inlining and prototyping to reuse.
2. [Lesson 2](lessons_2.md) gives a complete example of a task list employ a variety of implementation approaches.
3. [Lesson 3](lessons_3.md) explores various means of navigation.
5. [Lesson 4](lessons_4.md) explores controls and forms.
4. [Lesson 5](lessons_5.md) describes how JUI dialogs are used.
5. [Lesson 6](lessons_6.md) introduces tables and galleries.

Before you begin, ensure you have a project where you can test your code. Within this project, some initial setup will be necessary. If you came here from the [Getting started guide](getting_started.md#learning-jui) then you will already have a project that is suitably setup. If you choose to work with a different project the setup is outlined in [Code structure](#code-structure) below.

## Completed examples

A complete set of solutions (and one that adheres to the [Code structure](#code-structure) described below) is found in the `com.effacy.jui.playground.ui.lessons` package of the `jui-playground` project. Use this for reference as you need.

## Code structure

*The code structure has been described in [Getting started](getting_started.md#learning-jui) in the context of using the getting started project as the base for working through the various lessons. This is repeated here should you which is use a different project to walk through the lessons.*

We assume all code resides under a `lessons` sub-package of some suitable package in the project you are using to work through the lessons with. All code below will be drawn from the [Completed examples](#completed-examples) thus reside under `com.effacy.jui.playground.ui.lessons`. You will need to change this to suite you case.

### Lesson structure

Each lesson (referenced by number, i.e. [lesson 1](lessons_1.md)) is broken into parts (referenced by letter, i.e. [lesson 1, part a](lessons_1.md#part-a)). Each part explores a particular theme that involves building out some functionality across one or more components. As these are developed they can be hosted within a tab structure with the top-level enumerating accross lessons and the secondary level across parts.

All lessons involve the development and testing of components and JUI code, for this we need to embed them somewhere. The approach taken is that each lesson part will have its own lesson panel to which the various lesson components are added. These panels are assembled into a tabbed panel for the lesson itself. These lesson tabbed panels are assembed into an over-arching tabbed panel allowing primary navigation through lessons and secondary navigation through to the lesson parts.

### Support classes

The top-level tabbed panel is `Lessons` and is included in the applications overall navigation structure as the entry point into the lessons:

**Lessons.java**
```java
package com.effacy.jui.playground.ui.lessons;

import com.effacy.jui.core.client.component.layout.CardFitLayout;
import com.effacy.jui.core.client.dom.css.CSSInjector;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.ui.client.navigation.TabNavigator;
import com.effacy.jui.ui.client.navigation.TabNavigatorCreator;

public class Lessons extends TabNavigator {

    static {
        CSSInjector.injectFromModuleBase ("lessons.css"); 
    }

    public Lessons() {
        super (
            TabNavigatorCreator.config ()
                .style (TabNavigator.Config.Style.HORIZONTAL_BAR)
                .padding (Insets.em (0))
                .effect (CardFitLayout.Config.Effect.FADE_IN)
        );

        // The child tabs (for each lesson) go here.
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

        // Add a style class to the root DOM element of the component. This
        // ensures the styles cascade through to the child components.
        getRoot ().classList.add ("lessons");
    }
}
```

Note the inclusion of `lessons.css` (found in `jui-playground` under `src/jui/resources/com/effacy/jui/playground/public`). This is the shared CSS stylesheet used by all the lessons.

The lesson part panels extend the class `LessonPanel`:

**LessonPanel.java**
```java
package com.effacy.jui.playground.ui.lessons;

import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;

public class LessonPanel extends Panel {

    protected LessonPanel() {
        super (
            PanelCreator.config ()
                .scrollable ()
                .padding (Insets.em (2)).
                layout (VertLayout.$ ().separator(Separator.LINE).spacing (Length.em (1)).build ()
            )
        );

    }

}
```

For example, the lesson panel for lesson 1, part a would be `Lesson1a`.

### Assembling the structure

Lets say you are abount to start the first part of the first lesson. Before you begin you will create the lesson part panel `Lesson1a`:

**Lesson1a.java**
```java
package com.effacy.jui.playground.ui.lessons;

public class Lesson1a extends LessonPanel {

    protected Lesson1a() {
        // Here we add the components that we create as we go through
        // the lesson (part a).
    }

}
```

You will then incorporate this into the `Lesson` class:

```java
...
public Lessons() {
    super (
        TabNavigatorCreator.config ()
            .style (TabNavigator.Config.Style.HORIZONTAL_BAR)
            .padding (Insets.em (0))
            .effect (CardFitLayout.Config.Effect.FADE_IN)
    );

    tab ("lesson1", "Lesson 1",
        TabNavigatorCreator.create (cfg -> {
            cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
            cfg.tab ("lesson1a", "Part A", new Lesson1a ());
    }));
}
...
```

As you add more parts:

```java
tab ("lesson1", "Lesson 1",
    TabNavigatorCreator.create (cfg -> {
        cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
        cfg.tab ("lesson1a", "Part A", new Lesson1a ());
        cfg.tab ("lesson1b", "Part B", new Lesson1b ());
        cfg.tab ("lesson1c", "Part C", new Lesson1d ());
}));
```

and more lessons:

```java
tab ("lesson1", "Lesson 1",
    TabNavigatorCreator.create (cfg -> {
        cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
        cfg.tab ("lesson1a", "Part A", new Lesson1a ());
        ...
}));
tab ("lesson2", "Lesson 2",
    TabNavigatorCreator.create (cfg -> {
        cfg.style (TabNavigator.Config.Style.HORIZONTAL_UNDERLINE);
        cfg.tab ("lesson2a", "Part A", new Lesson2a ());
        ...
}));
```

You are now ready to commence with the lessons.
