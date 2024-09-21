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
package com.effacy.jui.ui.client.editor.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.ui.client.navigation.CardNavigator.Config.CardConfiguration;

public class CardNavigatorTest {

    @Test
    public void testCardConfiguration() {
        CardConfiguration cfg = new CardConfiguration("hubba/bubba", null);
        Assertions.assertTrue (cfg.segmented());
    }

    @Test
    public void testCardConfiguration_order() {
        List<CardConfiguration> cards = new ArrayList<>();
        cards.add (new CardConfiguration("bbb/ccc", null));
        cards.add (new CardConfiguration("bbb", null));
        cards.add (new CardConfiguration("aaa", null));
        cards.add (new CardConfiguration("bbb/aaa", null));
        cards.add (new CardConfiguration("ddd", null));
        Collections.sort(cards);

        Assertions.assertEquals("aaa", cards.get(0).reference()[0]);

        // The longer version appears first.
        Assertions.assertEquals("bbb", cards.get(1).reference()[0]);
        Assertions.assertEquals("aaa", cards.get(1).reference()[1]);

        // And is ordered alphabetically on position 2.
        Assertions.assertEquals("bbb", cards.get(2).reference()[0]);
        Assertions.assertEquals("ccc", cards.get(2).reference()[1]);

        Assertions.assertEquals("bbb", cards.get(3).reference()[0]);
        Assertions.assertEquals(1, cards.get(3).reference().length);
        
        Assertions.assertEquals("ddd", cards.get(4).reference()[0]);
    }

    @Test
    public void testCardConfiguration_path() {
        // Config cfg = new Config();
        // cfg.card ("bbb/ccc", null, null);
        // cfg.card ("aaa", null, null);
        // cfg.card ("bbb/aaa", null, null);
        // cfg.card ("bbb/ccc/ddd", null, null);

        // List<CardConfiguration> p1 = cfg.path (new CardConfiguration("bbb/ccc", null));
        // Assertions.assertEquals (2, p1.size());

        // Assertions.assertEquals (1, p1.get(0).reference().length);
        // Assertions.assertEquals("bbb", p1.get(0).reference()[0]);

        // Assertions.assertEquals (2, p1.get(1).reference().length);
        // Assertions.assertEquals("bbb", p1.get(1).reference()[0]);
        // Assertions.assertEquals("ccc", p1.get(1).reference()[1]);
    }

    @Test
    public void testCardConfiguration_prefix() {
        CardConfiguration card1 = new CardConfiguration("bbb/ccc/ddd", null);
        CardConfiguration card2 = new CardConfiguration("bbb/ccc", null);
        CardConfiguration card3 = new CardConfiguration("bbb", null);
        CardConfiguration card4 = new CardConfiguration("ddd", null);

        Assertions.assertTrue (card1.prefixOf(card1));
        Assertions.assertTrue (card2.prefixOf(card1));
        Assertions.assertTrue (card3.prefixOf(card1));
        Assertions.assertTrue (card2.prefixOf(card2));
        Assertions.assertTrue (card3.prefixOf(card2));
        Assertions.assertTrue (card3.prefixOf(card3));

        Assertions.assertFalse (card4.prefixOf(card1));
        Assertions.assertFalse (card4.prefixOf(card2));
        Assertions.assertFalse (card4.prefixOf(card3));

        Assertions.assertFalse (card1.prefixOf(card2));
        Assertions.assertFalse (card1.prefixOf(card3));
        Assertions.assertFalse (card2.prefixOf(card3));
    }
}
