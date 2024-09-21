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
package com.effacy.jui.core.client.dom.builder;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.dom.UIEventType;
import com.effacy.jui.core.client.dom.builder.NodeBuilder.EventBinding;

public class DomBuilderEventRegistrationTest {

    @Test
    public void testOrdering() {
        List<EventBinding> items = new ArrayList<> ();

        // SETUP: Recall that -1 always appears at the end otherwise the ordering is as
        // specified.
        items.add (new EventBinding (null, -1, UIEventType.CANPLAYTHROUGH));
        items.add (new EventBinding (null, 1, UIEventType.DRAG));
        items.add (new EventBinding (null, 0, UIEventType.DRAGENTER));

        // ASSERT: The list is ordered as was entered.
        Assertions.assertTrue (items.get (0).events.contains (UIEventType.CANPLAYTHROUGH));
        Assertions.assertTrue (items.get (1).events.contains (UIEventType.DRAG));
        Assertions.assertTrue (items.get (2).events.contains (UIEventType.DRAGENTER));

        // SETUP: Apply the sorting we are testing.
        EventBinding.sort (items);

        // ASSERT: The sorting was applied.
        Assertions.assertTrue (items.get (0).events.contains (UIEventType.DRAGENTER));
        Assertions.assertTrue (items.get (1).events.contains (UIEventType.DRAG));
        Assertions.assertTrue (items.get (2).events.contains (UIEventType.CANPLAYTHROUGH));
    }
}
