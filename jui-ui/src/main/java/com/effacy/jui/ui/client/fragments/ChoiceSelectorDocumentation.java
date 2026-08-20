/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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
package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.fragments.ChoiceSelector.ChoiceSelectorFragment;
import com.effacy.jui.ui.client.fragments.ChoiceSelector.Option;
import com.effacy.jui.ui.client.fragments.ChoiceSelector.Tone;
import com.effacy.jui.ui.client.icon.FontAwesome;

public class ChoiceSelectorDocumentation {

    public static ChoiceSelectorFragment example1(IDomInsertableContainer<?> root) {
        return ChoiceSelector.$(root)
            .option(Option.of("Apples", FontAwesome.sliders(), true, () -> Logger.info("A")))
            .option(Option.of("Pears", FontAwesome.tree(), false, () -> Logger.info("B")))
            .option(Option.of("Stairs", false, () -> Logger.info("C")))
        ;
    }

    /**
     * A judgement repeated down the rows of a listing: quiet at rest, and coloured
     * only on the option somebody chose.
     * <p>
     * The tone is what makes that possible. {@code Colors} on an option paints an
     * inline colour whatever the state, so three coloured options are three colours
     * at rest; a {@link Tone} overrides the token the selected rule reads, on that
     * option's own element, so the colour appears with the choice.
     */
    public static ChoiceSelectorFragment example2(IDomInsertableContainer<?> root, String status) {
        return ChoiceSelector.$(root)
            .variant(ChoiceSelector.Variant.INLINE)
            .option(Option.of("Behind", FontAwesome.arrowTrendDown(), "behind".equals(status),
                () -> Logger.info("behind")).tone(Tone.NEGATIVE))
            .option(Option.of("On track", FontAwesome.check(), "ontrack".equals(status),
                () -> Logger.info("ontrack")).tone(Tone.POSITIVE))
            .option(Option.of("Ahead", FontAwesome.arrowTrendUp(), "ahead".equals(status),
                () -> Logger.info("ahead")).tone(Tone.INFO))
        ;
    }
}
