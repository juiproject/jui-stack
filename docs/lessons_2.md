# Lesson 2

**By the end of this lesson, you will understand the differences between simple and stateful components and will be able to create a richly interactive component using both types. You will gain practical experience in handling user interactions and implementing best practices for component construction.**

?>It is assumed you have read through the [code structure](lessons.md#code-structure) documentation in [Lessons](lessons.md) that describes where the sample code resides and how CSS styles are treated in the examples. If you have not already worked through [lesson 1](lessons_1.md) your are encouraged to do so before embarking on this lesson.

*We have based the example in this lesson from one that originates from [Storybook for React tutorial](https://storybook.js.org/tutorials/intro-to-storybook/react/en/simple-component/) that develops a simple task manager. You are encouraged to review this by way of comparison.*

Here we have a list of tasks where each task has the ability to be archived (deemed completed) and pinned (to display at the top of the list). We also want to display a loading state (i.e. when the tasks are being retrieved remotely we display a marker that this is happening) and a separate rendering when there are no tasks (or when all tasks are complete).

Each of the three parts of this lesson considers a different way you can approach the problem thereby exploring the different capabilities of JUI (which are often employed within a project).

*Recall that working solutions can be found in the classes `Lesson2a`, `Lesson2b`, etc in the `com.effacy.jui.playground.ui.lessons` package.*

## Part A

Here we develop out the component model encapsulating the task as a component and the list of those tasks also as a component; the list component containing the task components as children. This model will be taken forward thourgh the subsequent parts.

Particular to this part is the way in which the component is rendered and managed during its lifetime. We manage different display states by showing and hiding different parts of the DOM and manage the child components of the list through rebuilding (using the `buildInto(...)` method). In [Part B](#part-b) we dispense with this in lieu of a simplified approach using re-rendering (though has its own caveats) while [Part C](#part-c) extends this to make use of states.

### Representing a task as data

We begin with the notion of a *task* and its representation as a data class. We associate with a task an ID (for reference, generally assigned by the server-side in some manner) and a display title. We also associate a *state* which, for our purposes, will be one of *inbox* (aka active), *pinned* (aka important) and *archived* (aka completed). We encapsulate this information in a single class (that is comparable with sort ordering that brings pinned task ahead of unpinned and otherwise orders by title):

```java
public class TaskData implements Comparable<TaskData> {

    public enum State {
        TASK_INBOX, TASK_PINNED, TASK_ARCHIVED;
    }

    public int id;

    public String title;

    public State state;

    public TaskData (int id, String title, State state) {
        this.id = id;
        this.title = title;
        this.state = state;
    }

    public int compareTo(TaskData o) {
        if (this == o)
            return 0;
        if (state == State.TASK_PINNED) {
            if (o.state != State.TASK_PINNED)
                return -1;
        } else if (o.state == State.TASK_PINNED)
            return 1;
        return title.compareTo (o.title);
    }
}
```

We now consider a component that can be used to display a single task.

### The basic task component

We make use of a `SimpleComponent` with constructor rendering (given the simplicity) that wraps a `TaskData` instance and makes use of a callback for signalling user actions (for pinning and archiving).

```java
public class Task extends SimpleComponent {

    /**
     * The task being represented.
     */
    private TaskData data;

    /**
     * A callback to invoke when the task changes state.
     */
    private Consumer<TaskData> onchange;

    public Task(TaskData data, Consumer<TaskData> onchange) {
        this.data = data;
        this.onchange = onchange;

        renderer(root -> {
            root.style ("list_item", data.state.name ());
            Label.$ (root).$ (label -> {
                label.style ("checkbox");
                label.htmlFor ("checked");
                label.ariaLabel ("archiveTask-" + data.id);
                Input.$ (label, "checkbox").$ (input -> {
                    input.name ("checked");
                    input.disabled (false);
                    input.id ("archiveTask-" + data.id);
                    input.checked (data.state == State.TASK_ARCHIVED);
                });
                Span.$ (label).$ ()
                    .style ("checkbox_custom")
                    // Let onArchive implement the user action.
                    .on (e -> onArchive (), UIEventType.ONCLICK);
            });
            Label.$ (root).$ (label -> {
                label.htmlFor ("title");
                label.ariaLabel (data.title);
                Input.$ (label, "text").$ (input -> {
                    input.name ("title");
                    input.readOnly (true);
                    input.value (data.title);
                });
            });
            if (data.state != State.TASK_ARCHIVED) {
                Button.$ (root).$ (btn -> {
                    btn.style ("pin_button");
                    btn.id ("pinTask-" + data.id);
                    btn.ariaLabel ("pinTask-" + data.id);
                    btn.key ("pinTask-" + data.id);
                    // Let onPin implement the user action.
                    btn.on (e -> onPin (), UIEventType.ONCLICK);
                    Span.$ (btn).style (FontAwesome.star ());
                });
            }
        });
    }

    /**
     * Invoked when the archive action has been selected.
     */
    protected void onArchive() {
        // Here we change the state directly on the task and notify. This is no the only
        // strategy one can use (and there is no mechanism to interact with a server)
        // but we want to keep the example simple for now.
        data.state = State.TASK_ARCHIVED;

        // Notify a change so forces a re-rendering.
        this.onchange.accept (data);
    }

    /**
     * Invoked when the pinned action has been selected. This will toggle the state.
     */
    protected void onPin() {
        // See comments in the onArchive method as they are pertinent here.
        if (data.state == State.TASK_PINNED)
            data.state = State.TASK_INBOX;
        else
            data.state = State.TASK_PINNED;
        
        // Notify a change that could impact ordering.
        this.onchange.accept (data);
    }

}
```

The generated HTML is along the lines of (following [Storybook for React tutorial](https://storybook.js.org/tutorials/intro-to-storybook/react/en/simple-component/)):

```html
<div class="list_item TASK_INBOX">
    <label class="checkbox" htmlfor="checked" aria-label="archiveTask-3">
        <input type="checkbox" name="checked" id="archiveTask-3">
        <span class="checkbox_custom"></span>
    </label>
    <label htmlfor="title" aria-label="Task 3">
        <input type="text" name="title" readonly="true" value="Task 3">
    </label>
    <button class="pin_button" id="pinTask-3" aria-label="pinTask-3" key="pinTask-3">
        <span class="fas fa-star"></span>
    </button>
</div>
```

The following points should be noted:

1. There is minimal configuration so this is passed through as arguments to the constructor (rather than using the `Component.Config` model).
2. We used the task data in various places to direct how the DOM is created. This include the provisioning of content (such as the title and the ID) as well as the layout (if it is archived then the last button for pinning does not render).
3. The CSS class names make use of underscores (i.e. "checkbox_custom") rather than the more traditional dash (i.e. "checkbox-custom"). The reason for this is if we want to migarte to using localised styles these name need to appear as methods on the style interface (see [Styles](ess_styles.md) for details on this) which do allow for dashes.
4. The various user actions are implemented by the methods `onPin()` and `onArchive()` which (respectively) update the task data state and generate a notification (against `onchange`).

We can now turn our attention to the task list itself.

### The list of tasks

Our task list will construct three separate top-level DIV's that carry elements that present one of three possible states: empty, loading (the default) and a list of tasks ([Part B](#part-b) takes an alternative approach where only one of these states is rendered and changes result in a re-rendering of the component).  We begin by extending `Component` (similar to the `Task`) but this time we override the `buildNode(Element,Config)` method (not necessary but improves the readability) to build out the basic structure:

```java
public class TaskList extends SimpleComponent {

    /**
     * The DIV representing the empty state.
     */
    private Element emptyEl;

    /**
     * The DIV representing the loading state.
     */
    private Element loadingEl;

    /**
     * The DIV representing the list of tasks.
     */
    private Element tasksEl;

    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$ (el).$ (root -> {
            // Empty indicator displays a message.
            Div.$ (root).$ (empty -> {
                empty.by ("empty");
                empty.style ("list_items");
                Div.$ (empty).style("wrapper_message").$ (
                    Span.$ ().$ ().style ("icon_check"),
                    P.$ ().style ("title_message").text ("You have no tasks"),
                    P.$ ().style ("subtitle_message").text ("Sit back and relax")
                );
            });
            // Loading indicator displays 6 lines represent tasks.
            Div.$ (root).$ (loading -> {
                loading.by ("loading");
                loading.style ("list_items");
                for (int i = 0; i < 6; i++) {
                    Div.$ (loading).style ("loading_item").$ (
                        Span.$ ().style ("glow_checkbox"),
                        Span.$ ().style ("glow_text").$ (
                            Span.$ ().text ("Loading state")
                        )
                    );
                }
            });
            // Container for tasks.
            Div.$ (root).by ("tasks").style ("list_items");
        }).build (n -> {
            emptyEl = n.first ("empty");
            loadingEl = n.first ("loading");
            tasksEl = n.first ("tasks");
            JQuery.$ (emptyEl).hide ();
            JQuery.$ (tasksEl).hide ();
        });
    }
}
```

When rendered we create the three top-level DIVs and extract them in the `build(...)` method to assign for later use (see next section). We also set the initial display state with only the loading DIV showing.

We now turn to adding tasks.

### Adding tasks to the task list

We extend `TaskList` so that we can load a list of `TaskData` items:

```java
/**
 * The tasks being displayed.
 */
private List<TaskData> data;

/**
 * Loads a list of tasks. If the task is empty the empty state is invoked. In
 * addition, if all the tasks in the list are archived then we treat as an empty
 * list.
 * 
 * @param tasks the tasks to load.
 */
public void load(List<TaskData> tasks) {
    // We re-wrap the tasks so that we can manipulate the list without affecting the
    // original list that we were passed.
    this.data = (tasks == null) ? new ArrayList<> () : new ArrayList<> (tasks);

    // If all the tasks are archived (done) then we treat as if it was an empty
    // list.
    if (!this.data.stream ().anyMatch (t -> t.state != State.TASK_ARCHIVED))
        this.data.clear ();

    // Sort the tasks.
    Collections.sort (data);

    // Rebuild the list including checking if the list is empty.
    if (!data.isEmpty ()) {
        // Render the tasks into the task element.
        buildInto(tasksEl, el -> {
            data.forEach(task -> Cpt.$ (el, new Task (task, t -> load (data))));
        });
        // Show the list of tasks.
        JQuery.$ (emptyEl).hide ();
        JQuery.$ (loadingEl).hide ();
        JQuery.$ (tasksEl).show ();
    } else {
        // Show the empty indicator.
        JQuery.$ (emptyEl).show ();
        JQuery.$ (loadingEl).hide ();
        JQuery.$ (tasksEl).hide ();
    }
}

```

Now when we call `load(List)` we perform some cleaning and normalisation. We then sort the list of tasks so the pinned ones appear at the top and otherwise the tasks are ordered by title.

If the list is empty then we display only the empty indicator DIV, otherwise we display the tasks list DIV and build into it `Task` component instances for each task in the list of tasks passed. Note that as the callback we pass a lambda-expressions that re-invoked the `load(..)` method. This will result in a resorting of the list and a subsequent re-rendering that reflects the new order (thus effecting the user action).

We also introduce a `preload()` method that display the loading state; this can be called prior to performing a load:

```java
public void preload() {
    JQuery.$ (emptyEl).hide ();
    JQuery.$ (loadingEl).show ();
    JQuery.$ (tasksEl).hide ();
}
```

So far we can display a list of tasks and re-display that list. We now need to consider how we can interact with tasks.

### Brining it together

The task component can be added to the `Lesson2a` class within its constructor:

```java
private TaskList list;

public Lesson2a() {
    ...
    
    // Here we create and add the task list. We give it some layout data (the layout
    // for the page is vertical) that placing some padding on either side.
    add (list = new TaskList (), VertLayout.data ().paddingSide (Length.em (2)));
}
```

To bring actual data in we let `Lesson2a` implement `INavigationAware` and override `onNavigateTo(...)`:

```java
@Override
public void onNavigateTo(NavigationContext context) {
    // We don't have protections for rendering in our sample, so just block it here.
    if (!isRendered())
        return;

    // Here we first call preload to put the component in the loading state. We then
    // mimic a remote load with a delay of 600ms.
    list.preload ();
    TimerSupport.timer(() -> {
        list.load (ListSupport.list (
            new TaskData (1, "Task 1", State.TASK_INBOX),
            new TaskData (2, "Task 2", State.TASK_INBOX),
            new TaskData (3, "Task 3", State.TASK_INBOX),
            new TaskData (4, "Task 4", State.TASK_INBOX),
            new TaskData (5, "Task 5", State.TASK_INBOX)
        ));
    }, 600);
}
```

When we navigate to the lesson this method will be called. First it invokes `preload()` which displays the loading state. We then mimic a remote loading by giving a `600ms` delay before invoking `load(...)` with a list of tasks.

## Part B

This is nearly identical to [Part A](#part-a) except that the task list does not maintain three separate DIV's for its state, rather it makes use of component re-rendering to rebuild the entire component DOM based on a change in state.

?>Re-rendering is very simple (and is commonly employed by other frameworks such as [React](https://react.dev/)) however does come at a cost. One needs to take care as not only is the component re-rendered but the children are disposed of and re-created (though one can retain and reuse components; for a task list this is what we want to happen). If you are not careful you can end up rebuilding large portions of your application. In practice one should combine the approaches where it makes sense to do so.

The code for this lesson resides in the `Lesson2b` (a working version in **jui-playground**) class but this references classes in `Lesson2a` (only the `TaskList` has been revised).

### Revised task list

We only revise `TaskList` and do so as follows (the method and member comments have been removed for brevity):

```java
public static class TaskList extends SimpleComponent {

    private List<TaskData> data;

    public void preload() {
        // Set the data to null (this is picked up during rerendering)
        // and rerender.
        this.data = null;
        rerender ();
    }

    public void load(List<TaskData> tasks) {
        // We re-wrap the tasks so that we can manipulate the list without affecting the
        // original list that we were passed.
        this.data = (tasks == null) ? new ArrayList<> () : new ArrayList<> (tasks);

        // If all the tasks are archived (done) then we treat as if it was an empty
        // list.
        if (!this.data.stream ().anyMatch (t -> t.state != State.TASK_ARCHIVED))
            this.data.clear ();

        // Sort the tasks.
        Collections.sort (data);

        // Re-render the component with the new data.
        rerender ();
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        // Loading state.
        if (data == null) {
            return Wrap.$ (el).$ (loading -> {
                loading.by ("loading");
                loading.style ("list_items");
                for (int i = 0; i < 6; i++) {
                    Div.$ (loading).style ("loading_item").$ (
                        Span.$ ().style ("glow_checkbox"),
                        Span.$ ().style ("glow_text").$ (
                            Span.$ ().text ("Loading state")
                        )
                    );
                }
            }).build ();
        }

        // Empty state.
        if (data.isEmpty ()) {
            return Wrap.$ (el).$ (root -> {
                root.div (empty -> {
                    empty.by ("empty");
                    empty.style ("list_items");
                    Div.$ (empty).style("wrapper_message").$ (
                        Span.$ ().$ ().style ("icon_check"),
                        P.$ ().style ("title_message").text ("You have no tasks"),
                        P.$ ().style ("subtitle_message").text ("Sit back and relax")
                    );
                });
            }).build ();
        }

        // Data to render.
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).$ (tasks -> {
                tasks.by ("tasks");
                tasks.style ("list_items");
                data.forEach(t -> Cpt.$ (tasks, new Task(t, d -> load(data))));
            });
        }).build ();
    }

}
```

We see the following changes:

1. The `preload()` method now just clears the internally held data to `null` and invokes a component re-render.
2. The `load(List)` method now just sorts and stores the task data before invoking a component re-render.
3. During a re-render the `buildNode(...)` is invoked, this means we can render according to the current state of the component. In this case we render loading when the internal data list is `null` (see (1)) while rendering the empty state of the data list is empty. Where the is task data then the task list itself is rendered.

In this particular case the re-render approach is shorter and somewhat easier to follow (once the abstraction of re-rendering is understood). However the approach in the previous part still has merit and is widely used.

## Part C

We end the lesson with a final adjustment and that is to make use of a [state](ess_states.md). The code for this lesson resides in the `Lesson2c` (a working version in **jui-playground**) class but this references classes in `Lesson2a`.

### Stateful task list

The approach we will take is to create a list of tasks that is a *state*. With this we can turn `TaskList` into a state component that responds to changes in this state. To this end we create `TaskListState`:

```java
public class TaskListState extends LifecycleStateVariable<TaskListState> {

    /**
     * List of tasks.
     */
    private List<TaskData> data = new ArrayList<>();

    /**
     * The tasks held in the list.
     */
    public List<TaskData> data() {
        return data;
    }

    /**
     * Loads up the task data.
     */
    public void load(List<TaskData> tasks) {
        this.data.clear ();
        if (tasks != null)
            this.data.addAll (tasks);
        order ();
    }

    /**
     * Orders the list based on the internal data.
     */
    public void order() {
        // If all the tasks are archived (done) then we treat as if it was an empty
        // list.
        if (!this.data.stream ().anyMatch (t -> t.state != TaskData.State.TASK_ARCHIVED))
            this.data.clear ();

        // Rebuild the list including checking if the list is empty.
        Collections.sort(this.data);

        // Notify of change.
        modify ();
    }
}
```

This extends `LifecycleStateVariable` that includes a lifecycle status that can be used to capture states pertinent to loading, error, and complete (this is particularly useful for remote loading and allows any state component to respond to states in the loading lifecycle).

### State aware task list

We can now revise `TaskList` to be aware of the `TaskListState`, which means having it extend `StateComponent` (rather than `SimpleComponent`):

```java
public static class TaskList extends StateComponent<TaskListState> {

    /**
     * Construct instance of component (initally loading).
     */
    public TaskList() {
        super (new TaskListState ());
    }

    /**
     * Loads a list of tasks. If the task is empty the empty state is invoked. In
     * addition, if all the tasks in the list are archived then we treat as an empty
     * list.
     * 
     * @param tasks the tasks to load.
     */
    public void load(List<TaskData> tasks) {
        // Delegate through to the task.
        state ().load (tasks);
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        // Loading state.
        if (state ().isLoading ()) {
            return Wrap.$ (el).$ (loading -> {
                loading.by ("loading");
                loading.style ("list_items");
                for (int i = 0; i < 6; i++) {
                    Div.$ (loading).style ("loading_item").$ (
                        Span.$ ().style ("glow_checkbox"),
                        Span.$ ().style ("glow_text").$ (
                            Span.$ ().text ("Loading state")
                        )
                    );
                }
            }).build ();
        }

        // Empty state.
        if (state ().data ().isEmpty ()) {
            return Wrap.$ (el).$ (root -> {
                root.div (empty -> {
                    empty.by ("empty");
                    empty.style ("list_items");
                    Div.$ (empty).style("wrapper_message").$ (
                        Span.$ ().$ ().style ("icon_check"),
                        P.$ ().style ("title_message").text ("You have no tasks"),
                        P.$ ().style ("subtitle_message").text ("Sit back and relax")
                    );
                });
            }).build ();
        }

        // Data to render.
        return Wrap.$ (el).$ (root -> {
            Div.$ (root).$ (tasks -> {
                tasks.by ("tasks");
                tasks.style ("list_items");
                state ().data ().forEach (t -> Cpt.$ (tasks, new Task(t, d -> state ().order ())));
            });
        }).build ();
    }
}
```

The notable changes are:

1. We now have a constructor that that passing down an instance of the state variable (which the component will respond to by re-rendering when there is a change in the state).
2. The `load(List)` method now delegates through to the load method on the state. When this is called the tasks are passed to the state which replaces those in its internal list. It then executes an `order()` (that normalises and sorts the tasks) which issues a modification event to listeners of the state (the call to `modify()`).
3. The rendering logic now makes use of the state to determine what should be rendered. If the state is loading (which is the initial case) then the loading indicator is rendered (the loading state is implemented by `LifecycleStateVariable`). If not loading then rendering is based on the tasks as retrieved via the `data()` method on the state.
4. The callback to `Task` now simply re-orders the state (with `order()`). Recalling from (2) that this issues a modification event from the state which forces the component to re-render and this effect the user action.

The changes above provide a working task list that is state driven. Utilising states in this way can be very effective (and efficient) however states are not always straightforward to conceptualise (see exercise 3). In practice one should choose the approach that makes most sense for the case at hand.

### Exercises

You may consider the following exercises:

1. As it stands you can arhive a task but not unarchive it. Modify the `Task` class so that it allows one to unarchive tasks.
2. **Advanced** How could you use a region (see [Lesson1: Part E](lessons_1.md#part-e-panels-and-layouts)) to hold and render the components?
3. In [Part C](#part-c) we modified the `TaskList` to use the state variable `TaskListState`. You may have noticed that changes in the tasks are first applied to the task data directly and then notified via a callback to `Task`. This is not entirely consistent with the notion of state: really we should have the state respond directly to changes in its embodied task data rather than relying on a callback on `Task`. How could `TaskListState` be modified to respond directly to changes in task data?
4. In [Part C](#part-c) we did not describe changes to the `preload()` method. How should this be implemented?

## Solutions to exercises

Solutions provided here are partial or indicative only (provides as advanced hints).

### Exercise 1

There are two modifications required. The first is to `onArchive()` that responds to state:

```java
protected void onArchive() {
    if (data.state == State.TASK_ARCHIVED)
        data.state = State.TASK_INBOX;
    else
        data.state = State.TASK_ARCHIVED;

    this.onchange.accept (data);
}
```

The second is cosmetic and that is not to display a customised archived checkbox (this can be done with CSS).

### Exercise 2

One approach is to retain as a member an instance of `RegionPoint` and attach that to the tasks element. In this case one only needs to clear and add `Task` instances to the region point and they will be rendered automatically.

```java
public class TaskList extends SimpleComponent {

    ...

    private RegionPoint tasksRp;

    
    public void load(List<TaskData> tasks) {
        ...

        // Clear all prior task components. This will remove the tasks
        // from the DOM.
        tasksRp.disposeAll ();

        // Rebuild the list including checking if the list is empty.
        if (!data.isEmpty ()) {
            // We add the Task instance to the region point.
            data.forEach (task -> tasksRp.add (new Task(task, t -> load (data))));
            ...
        } else {
            ...
        }
    }

    @Override
    protected INodeProvider buildNode(Element el) {
        // Create and assign the region point.
        tasksRp = findRegionPoint ("TASKS");
        return Wrap.$ (el).$ (root -> {
            ...
            Div.$ (root).$ (tasks -> {
                tasks.by ("tasks");
                tasks.style ("list_items");
                // Here we use the region(...) method to configure the region above
                // referenced by TASKS and return a consumer over Node. Alternatively
                // you could have followed the examples given in lesson 1 part e
                // which does the same thing, but in long form.
                tasks.use (region ("TASKS", MinimalLayout.config ().build ()));
            });
        }).build (n -> {
            ...
        });
    }
    
}
```

### Exercise 3

One approach is to introduce a state that represents the task data:

```java
public class TaskState extends StateVariable<TaskState> {

    private TaskData data;

    public TaskState(TaskData data) {
        this.data = data;
    }

    public TaskData data() {
        return data;
    }

    public void pin() {
        if (data.state == State.TASK_PINNED)
            data.state = State.TASK_INBOX;
        else
            data.state = State.TASK_PINNED;
        modify();
    }

    ...
}
```

Note the `pin()` method which implements the pinning action (a similar one would be included for archive). When invoked it issues a modification event for the state.

We now modiy the `Task` component, but not necessarily as you may think:

```java
public class Task extends SimpleComponent {

    private TaskState state;

    public Task(TaskState state) {
        super (state);
    }

    protected void onPin() {
        // Delegate to the state. This will fire a modification which will
        // chain up to the task lists state and ultimately to the task list
        // component for re-rendering.
        state.pin ();
    }

    ...
}
```

We do not make the `Task` state aware as the re-rendering is going to remain with `TaskList` (to effect the ordering). Rather we just hold an instance of `TaskState` rather than `TaskData` and delegate the user actions to the state.

We now modify the `TaskListState` to incorporate task states:

```java
public class TaskListState extends LifecycleStateVariable<TaskListState> {

    private List<TaskState> data = new ArrayList<>();

    public void load(List<TaskData> tasks) {
        this.data.clear ();
        if (tasks != null) {
            tasks.forEach (task -> {
                TaskState state = new TaskState (task);
                state.listen (s -> order ());
                data.add (state);
            });
        }
        order ();
    }

    ...
}
```

Here we move to holding `TaskState`'s and respond to changes in those states by re-ordering. This effectively replaces the callback:

```java
data.forEach (t -> Cpt.$ (tasks, new Task(t, d -> load(data))));
```

which is replaced by:

```java
data.forEach (t -> Cpt.$ (tasks, new Task(t)));
```

### Exercise 4

Here we need to place the state variable into the loading state:

```java
public void preload() {
    state().loading();
}
```

This is a feature of `LifecycleStateVariable`.