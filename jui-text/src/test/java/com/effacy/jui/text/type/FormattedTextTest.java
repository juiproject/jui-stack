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
import com.fasterxml.jackson.databind.ObjectMapper;

public class FormattedTextTest {

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
    }
}
