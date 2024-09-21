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
package com.effacy.jui.core.client.navigation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.component.layout.ILayout.ActivateOutcome;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.util.Tribool;
import com.effacy.jui.platform.util.client.Promise;

public class NavigationHandlerTest {

    @Test
    public void testDefault() {
        NavigationTree tree = tree01 ();

        // TEST: Default navigation.
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab1/tab11/tab111");

        // TEST: Disable various tabs in default.
        tree.find ("/tab1/tab11/tab111").disable ();
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab1/tab11/tab112");
        tree.find ("/tab1/tab11/tab111").enable ();
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab1/tab11/tab111");

        tree.find ("/tab1/tab11").disable ();
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab1/tab12/tab121");
        tree.find ("/tab1/tab11").enable ();
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab1/tab11/tab111");

        tree.find ("/tab1").disable ();
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab2/tab21");
        tree.find ("/tab1").enable ();
        tree.navigate (new NavigationContext ());
        tree.assertPath ("/tab1/tab11/tab111");

        // TEST: Various path truncations and mismatches.
        tree.navigate (new NavigationContext (), NavigationSupport.split ("/tab3"));
        tree.assertPath ("/tab3/tab31");

        tree.navigate (new NavigationContext (), NavigationSupport.split ("/tab1/tab12"));
        tree.assertPath ("/tab1/tab12/tab121");

        tree.navigate (new NavigationContext (), NavigationSupport.split ("/tab1/tab12/hubba"));
        tree.assertPath ("/tab1/tab12/tab121");

        tree.navigate (new NavigationContext (), NavigationSupport.split ("/tab1/hubba/hubba"));
        tree.assertPath ("/tab1/tab11/tab111");
    }

    @Test
    public void testNavigateFrom() {
        NavigationTree tree = tree01 ();

        // SETUP: Default path
        tree.navigate (new NavigationContext());
        tree.assertPath ("/tab1/tab11/tab111");
        tree.reset ();

        // TEST: Navigate well away.
        tree.navigate (new NavigationContext (), NavigationSupport.split ("/tab3"));
        tree.assertPath ("/tab3/tab31");
        Assertions.assertTrue (tree.find ("/tab1/tab11/tab111").isOnNavigateFromCalled ());
        Assertions.assertFalse (tree.find ("/tab1/tab11").isOnNavigateFromCalled ());
        Assertions.assertFalse (tree.find ("/tab1").isOnNavigateFromCalled ());
    }

    /**
     * Builds out a simple test tree.
     * 
     * @return the tree.
     */
    protected NavigationTree tree01() {
        return build (nav -> {
            nav.add ("tab1", tab1 -> {
                tab1.add ("tab11", tab11 -> {
                    tab11.add ("tab111");
                    tab11.add ("tab112");
                });
                tab1.add ("tab12", tab12 -> {
                    tab12.add ("tab121");
                    tab12.add ("tab122");
                });
                tab1.add ("tab13");
            });
            nav.add ("tab2", tab2 -> {
                tab2.add ("tab21");
                tab2.add ("tab22");
                tab2.add ("tab23");
            });
            nav.add ("tab3", tab3 -> {
                tab3.add ("tab31");
                tab3.add ("tab32");
                tab3.add ("tab33");
            });
            nav.add ("tab4", tab4 -> {
                tab4.add ("tab41");
                tab4.add ("tab42");
                tab4.add ("tab43");
            });
        });
    }

    /**
     * Builds a navigation tree for testing.
     * 
     * @param builder
     *                to build out the tree.
     * @return the tree.
     */
    protected NavigationTree build(Consumer<NavigationTree> builder) {
        NavigationTree root = new NavigationTree();
        if (builder != null)
            builder.accept (root);
        return root;
    }

    /**
     * A testable navigation tree.
     */
    static class NavigationTree extends NavigationHandler<NavigationTree.NavigationItem> {

        /**
         * The last back-propagated path.
         */
        private String path;

        /**
         * The items mapped by path.
         */
        private Map<String,NavigationItem> items = new HashMap<>();

        /**
         * Constructs a tree.
         */
        public NavigationTree() {
            assignParent (new INavigationHandlerParent() {

                @Override
                public void onNavigation(NavigationContext context, List<String> path) {
                    NavigationTree.this.path = NavigationSupport.build (path);
                }
                
            });
        }

        /**
         * Resets all the items in the tree.
         */
        public void reset() {
            items.values ().forEach (item -> item.reset ());
        }

        /**
         * Given a path finds the item associated with that path. Paths can be to
         * internal items.
         * 
         * @param path
         *             the path to the item.
         * @return the associated item (or {@code null}).
         */
        public NavigationItem find(String path) {
            return items.get (path);
        }

        /**
         * Asserts the given path is active.
         * <p>
         * This also performs other state assertions that are expected to be true for
         * the path.
         * 
         * @param path
         *             the path to test.
         */
        public void assertPath(String path) {
            // Check that the paths match.
            Assertions.assertEquals (path, this.path);

            // Verify that only the elements in the path are active.
            List<String> pathAsList = NavigationSupport.split (path);
            Assertions.assertEquals (pathAsList.size (), countActivation (Tribool.TRUE));
            for (int i = pathAsList.size (); i > 0; i--)
                Assertions.assertEquals (Tribool.TRUE, find (NavigationSupport.build (pathAsList, i)).getActivation ());

            // Finally assert state of all.
            items.values().forEach (item -> item.assertConsistentState ());
        }

        /**
         * Counts the total number of activation values of the given type.
         * 
         * @param value
         *              the value to test for.
         * @return the total number.
         */
        public int countActivation(Tribool value) {
            int count = 0;
            for (NavigationItem item : items.values()) {
                if (value == item.getActivation())
                    count++;
            }
            return count;
        }

        public void printActivations(Tribool value) {
            for (Map.Entry<String,NavigationItem> item : items.entrySet ()) {
                if (item.getValue().getActivation() == value) {
                    System.out.println ("> " + item.getKey ());
                }
            }
        }

        /**
         * Adds a terminal item with the given reference.
         * 
         * @param reference
         *                  the reference.
         * @return the item that was added.
         */
        public NavigationItem add(String reference) {
            return add (reference, null);
        }

        /**
         * Adds an item with the given reference and configures the item with children.
         * 
         * @param reference
         *                  the reference.
         * @param builder
         *                  to build out the children of the item.
         * @return the item that was added.
         */
        public NavigationItem add(String reference, Consumer<NavigationItem> builder) {
            NavigationItem child = new NavigationItem ("", reference);
            if (builder != null)
                builder.accept (child);
            register (child);
            return child;
        }
    
        /**
         * A testable item in the tree.
         */
        class NavigationItem implements INavigationAwareItem {

            /**
             * The full path to the item.
             */
            private String path;

            /**
             * The reference for the item.
             */
            private String reference;

            /**
             * See {@link #getActivation()}.
             */
            private Tribool activation = Tribool.UNDETERMINED;

            /**
             * See {@link #onNavigateDeactivated()} and {@link #assertConsistentState()}.
             */
            private boolean activationOnMethodCalled = false;

            /**
             * The children of the item (if there are any).
             */
            private NavigationHandler<NavigationItem> children;

            /**
             * See {@link #isEnabled()}.
             */
            private boolean disabled = false;

            /**
             * See {@link #onNavigateFrom(com.effacy.jui.core.client.navigation.INavigationAware.INavigateCallback)}.
             */
            private boolean onNavigateFromCalled = false;

            /**
             * Construct with reference data.
             * 
             * @param parent
             *                  the path of the parent.
             * @param reference
             *                  the reference for the item.
             */
            NavigationItem(String parent, String reference) {
                this.reference = reference;
                path = parent + "/" + reference;
                items.put (path, this);
            }

            /**
             * Resets the state data.
             */
            public void reset() {
                onNavigateFromCalled = false;
            }

            /**
             * Adds a terminal item with the given reference.
             * 
             * @param reference
             *                  the reference.
             * @return the item that was added.
             */
            public NavigationItem add(String reference) {
                return add (reference, null);
            }

            /**
             * Adds an item with the given reference and configures the item with children.
             * 
             * @param reference
             *                  the reference.
             * @param builder
             *                  to build out the children of the item.
             * @return the item that was added.
             */
            public NavigationItem add(String reference, Consumer<NavigationItem> builder) {
                NavigationItem child = new NavigationItem (path, reference);
                if (builder != null)
                    builder.accept (child);
                if (children == null)
                    children = new NavigationHandler<>();
                children.register (child);
                return child;
            }

            /**
             * Checks that there is a consistent internal state.
             * <p>
             * This verifies that if the item as been deactivated then
             * {@link #onNavigateDeactivated()} has also been called.
             */
            public void assertConsistentState() {
                if (activationOnMethodCalled || (Tribool.FALSE == activation)) {
                    Assertions.assertEquals (Tribool.FALSE, activation, "Expected " + path + " to not be activated");
                    Assertions.assertTrue (activationOnMethodCalled, "Expected " + path + " to be deactivated");
                }
            }

            /**
             * If {@link #onNavigateFromCalled()} has been called.
             * 
             * @return {@code true} if so.
             */
            public boolean isOnNavigateFromCalled() {
                return onNavigateFromCalled;
            }

            /**
             * The activation state of the item. This is undetermined if the item has not
             * participated in any navigation event. Otherwise it is yes if it currently
             * appears in the navigation path and no otherwise.
             * <p>
             * This is based on invocations of {@link #deactivate()} and
             * {@link #activate(NavigationContext)}.
             * 
             * @return the activation state as recorded.
             */
            public Tribool getActivation() {
                return activation;
            }

            /**
             * To disable the item.
             * <p>
             * This determines the return value of {@link #isEnabled()}.
             */
            public void disable() {
                this.disabled = true;
            }

            /**
             * To enable the item.
             * <p>
             * This determines the return value of {@link #isEnabled()}.
             */
            public void enable() {
                this.disabled = false;
            }

            @Override
            public boolean isEnabled() {
                return !disabled;
            }

            @Override
            public Promise<ActivateOutcome> activate(NavigationContext context) {
                activationOnMethodCalled = false;
                if (activation == Tribool.TRUE)
                    return Promise.create (ActivateOutcome.ALREADY_ACTIVATED);
                activation = Tribool.TRUE;
                return Promise.create (ActivateOutcome.ACTIVATED);
            }

            @Override
            public void deactivate() {
                activation = Tribool.FALSE;
            }

            @Override
            public void onNavigateDeactivated() {
                activationOnMethodCalled = true;
            }

            @Override
            public String getReference() {
                return reference;
            }

            @Override
            public INavigationHandler handler() {
                return children;
            }

            @Override
            public void onNavigateFrom(INavigateCallback cb) {
                onNavigateFromCalled = true;
                cb.proceed ();
            }

            
        }
    }
}
