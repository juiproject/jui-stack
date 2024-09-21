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

import com.effacy.jui.core.client.component.StateComponent;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Cpt;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Span;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.state.LifecycleStateVariable;
import com.effacy.jui.platform.util.client.ListSupport;
import com.effacy.jui.platform.util.client.TimerSupport;
import com.effacy.jui.playground.ui.lessons.Lesson2a.Task;
import com.effacy.jui.playground.ui.lessons.Lesson2a.TaskData;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;

import elemental2.dom.Element;

public class Lesson2c extends Panel implements INavigationAware {

    /**
     * The list component under scrutiny.
     */
    private TaskList list;

    /**
     * Construct instance of the lession.
     */
    public Lesson2c() {
        super (PanelCreator.config ().scrollable ().layout (VertLayout.$ ().spacing (Length.em (1)).build ()));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> cfg.padding (Insets.em (2, 1.75, 2, 0.75)), builder -> {
            builder.header ("Part B: Task list (cont)", header -> {
                header.subtitle ("This is functionally identical to Lession 1 and varies only in the technical implementation.");
            });
        })).update (null);
        
        // Here we create and add the task list. We give it some layout data (the layout
        // for the page is vertical) that placing some padding on either side.
        list = add (new TaskList (), VertLayout.data ().paddingSide (Length.em (2)));
    }

    /**
     * Invoked when navigated to, here we perform a data load for the task list.
     *
     * @see com.effacy.jui.core.client.navigation.INavigationAware#onNavigateTo(com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext)
     */
    @Override
    public void onNavigateTo(NavigationContext context) {
        // We don't have protections for rendering in our sample, so just block it here.
        if (!isRendered())
            return;

        // Here we first call preload to put the component in the loading state. We then
        // mimic a remote load with a delay of 600ms.
        TimerSupport.timer(() -> {
            list.load (ListSupport.list(
                new TaskData (1, "Task 1", TaskData.State.TASK_INBOX),
                new TaskData (2, "Task 2", TaskData.State.TASK_INBOX),
                new TaskData (3, "Task 3", TaskData.State.TASK_INBOX),
                new TaskData (4, "Task 4", TaskData.State.TASK_INBOX)
            ));
        }, 600);
    }

    /*************************************************************************
     * Lesson classes
     *************************************************************************/

    /**
     * State variable for the task list.
     */
    public static class TaskListState extends LifecycleStateVariable<TaskListState> {

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

    /**
     * A list of tasks. In this this we represent each task as its own component
     * (though this is not the only approach).
     * <p>
     * This variant displays its state by way of rendering which means it makes use
     * of component {@link #rerender()}.
     */
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
            if (state ().data.isEmpty ()) {
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
                    state ().data.forEach (t -> Cpt.$ (tasks, new Task(t, d -> state ().order ())));
                });
            }).build ();
        }
    }
}

