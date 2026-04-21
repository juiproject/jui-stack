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
package com.effacy.jui.text.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.text.type.FormattedBlock.BlockType;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.builder.FormattedTextBuilder;
import com.effacy.jui.text.type.builder.markdown.MarkdownParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FormattedTextTest {

    private static class FixedBlockIdGenerator implements IBlockIdGenerator {

        private int counter = 1;

        @Override
        public String nextId() {
            return "blk_" + counter++;
        }
    }

    @Test
    public void test_Serialisation() throws Exception {
        ObjectMapper mapper = new ObjectMapper ();

        // SETUP: Create an initial structure.
        FormattedText ft = new FormattedText();
        ft.block (BlockType.PARA, blk -> {
            blk.line (line -> line
                .append ("This is a ")
                .append ("line of", FormatType.BLD)
                .append (" text")
            );
        });
        String original = ft.toString ();
        Assertions.assertEquals ("<<This is a [{10,7} bld]line of[/] text>>", original);

        // SETUP: Serialise to JSON.
        String json = mapper.writeValueAsString (ft);
        
        // TEST: Deserialise from JSON.
        FormattedText ftpost = mapper.readValue (json, FormattedText.class);
        Assertions.assertEquals (original, ftpost.toString ());
        Assertions.assertNotNull(ftpost.getBlocks().get(0).getId());
    }

    @Test
    public void test_Serialisation_BackwardCompatibleWithoutIds() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"blocks\":[{\"type\":\"PARA\",\"lines\":[{\"text\":\"Legacy\"}]}]}";

        FormattedText ft = mapper.readValue(json, FormattedText.class);

        Assertions.assertNull(ft.getBlocks().get(0).getId());
        ft.ensureBlockIds(new FixedBlockIdGenerator());
        Assertions.assertEquals("blk_1", ft.getBlocks().get(0).getId());
    }

    @Test
    public void test_Markdown_AssignsBlockIdsByDefault() {
        FormattedText ft = FormattedText.markdown("# Heading", "", "Paragraph");

        Assertions.assertEquals(2, ft.getBlocks().size());
        Assertions.assertTrue(ft.getBlocks().get(0).hasId());
        Assertions.assertTrue(ft.getBlocks().get(1).hasId());
        Assertions.assertNotEquals(ft.getBlocks().get(0).getId(), ft.getBlocks().get(1).getId());
    }

    @Test
    public void test_Builder_AllowsCustomIdGenerator() {
        FormattedText ft = new MarkdownParser()
            .parse(new FormattedTextBuilder().blockIdGenerator(new FixedBlockIdGenerator()), "# Heading\n\nParagraph");

        Assertions.assertEquals("blk_1", ft.getBlocks().get(0).getId());
        Assertions.assertEquals("blk_2", ft.getBlocks().get(1).getId());
    }

    @Test
    public void test_EnsureBlockIds_OnlyFillsMissingValues() {
        FormattedText ft = new FormattedText();
        ft.block(BlockType.PARA).id("blk_existing").line("A");
        ft.block(BlockType.PARA).clearId().line("B");

        ft.ensureBlockIds(new FixedBlockIdGenerator());

        Assertions.assertEquals("blk_existing", ft.getBlocks().get(0).getId());
        Assertions.assertEquals("blk_1", ft.getBlocks().get(1).getId());
    }

    @Test
    public void test_ValidateUniqueBlockIds() {
        FormattedText ft = new FormattedText();
        ft.block(BlockType.PARA).id("blk_1").line("A");
        ft.block(BlockType.PARA).id("blk_2").line("B");

        Assertions.assertDoesNotThrow(ft::validateUniqueBlockIds);
        ft.getBlocks().get(1).setId("blk_1");
        Assertions.assertThrows(IllegalStateException.class, ft::validateUniqueBlockIds);
    }
}
