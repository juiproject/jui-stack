/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.playground.ui.lessons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.layout.MinimalLayout;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.Button;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.Input;
import com.effacy.jui.core.client.dom.builder.Label;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.playground.ui.lessons.Lesson2a.TaskData.State;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.Panel;

import elemental2.dom.Element;

/**
 * Creates a simple task list where tasks can be marked as complete and can be
 * pinned. Also display a loading state and a completion state.
 * <p>
 * This is based on
 * {@link https://storybook.js.org/tutorials/intro-to-storybook/react/en/simple-component/}.
 * <p>
 * The classes that constitite the lession are declared as the inner classes
 * {@link TaskData}, {@link Task} and {@link TaskList}.
 */
public class Lesson2a extends Panel implements INavigationAware {

    /**
     * The list component under scrutiny.
     */
    private TaskList list;

    
    /**
     * Construct instance of the lession.
     */
    public Lesson2a() {
        super (new Panel.Config ().scrollable ().layout (VertLayout.$ ().spacing (Length.em (1)).build ()));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> cfg.padding (Insets.em (2, 1.75, 2, 0.75)), builder -> {
            builder.header ("Part A: Task list", header -> {
                header.subtitle ("This is a simple list of tasks (ordered by title) with the ability to pin tasks to the top (clicking on the star) and completing a task by checking the checkbox. When all tasks are completed a completion message displays. When the list loads a loading state shows (and the list can be re-loaded by navigating away from the page and back again).");
            });
        })).update (null);
        
        // Here we create and add the task list. We give it some layout data (the layout
        // for the page is vertical) that placing some padding on either side.
        add (list = new TaskList (), VertLayout.data ().paddingSide (Length.em (2)));
    }

    /**
     * Invoked when navigated to, here we perform a data load for the task list.
     */
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

    /*************************************************************************
     * Lesson classes
     *************************************************************************/


     /**
      * Represents the data of a task. Consists of a reference ID, a title and a
      * state.
      */
    public static class TaskData implements Comparable<TaskData> {

        /**
         * The different states a task can take.
         */
        public enum State {
            TASK_INBOX, TASK_PINNED, TASK_ARCHIVED;
        }

        /**
         * The reference ID.
         */
        public int id;

        /**
         * The title of the task for display.
         */
        public String title;

        /**
         * The state of the task.
         */
        public State state;

        /**
         * Construct an instance of the task data.
         * 
         * @param id
         *              the reference ID.
         * @param title
         *              the display title.
         * @param state
         *              the current state.
         */
        public TaskData (int id, String title, State state) {
            this.id = id;
            this.title = title;
            this.state = state;
        }

        /**
         * Sort with pinned ahead of unpinned and otherwise by name.
         */
        @Override
        public int compareTo(TaskData o) {
            if (this == o)
                return 0;
            // Check for differential in state of being pinned, so pinned has priority.
            if (state == State.TASK_PINNED) {
                if (o.state != State.TASK_PINNED)
                    return -1;
            } else if (o.state == State.TASK_PINNED)
                return 1;
            // Order on name.
            return title.compareTo (o.title);
        }
    }

    /**
     * Component that represents a single task (as it will appear in the task list).
     */
    public static class Task extends SimpleComponent {

        /**
         * The task being represented.
         */
        private TaskData data;

        /**
         * A callback to invoke when the task changes state.
         */
        private Consumer<TaskData> onchange;

        /**
         * Construct with the data of the task and a change callback.
         */
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

    /**
     * A list of tasks. In this this we represent each task as its own component
     * (though this is not the only approach).
     * <p>
     * Here we create three separate display states: one for when there are no
     * items, one for when the data is loading (the pre-load state) and one when
     * there are tasks to display. These states are rendered when the component is
     * rendered as separate DIV's. These DIV's are shown and hidden as needed to
     * represent the state. In addition we update the components contents (the list
     * of tasks) by employing DOM manipulated (more specifically, child component
     * manipulation by removing the prior task components and adding new ones for
     * the new set of tasks).
     * <p>
     * There are plenty of other approaches one could take (rendering the tasks not
     * as components but as renderers, or using a gallery) however the intent is to
     * describe how components can be constructed dynamically.
     */
    public static class TaskList extends SimpleComponent {

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

        /**
         * The tasks being displayed.
         */
        private List<TaskData> data;

        /**
         * Transitions the component to the loading state (so displays the loading
         * markers).
         */
        public void preload() {
            JQuery.$ (emptyEl).hide ();
            JQuery.$ (loadingEl).show ();
            JQuery.$ (tasksEl).hide ();
        }

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
                JQuery.$ (emptyEl).hide ();
                JQuery.$ (loadingEl).hide ();
                JQuery.$ (tasksEl).show ();
            } else {
                JQuery.$ (emptyEl).show ();
                JQuery.$ (loadingEl).hide ();
                JQuery.$ (tasksEl).hide ();
            }
        }

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
}
