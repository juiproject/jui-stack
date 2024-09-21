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
package com.effacy.jui.ui.client.editor.tools;

import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.editor.BlockFactory;
import com.effacy.jui.ui.client.editor.model.ContentBlock.BlockType;
import com.effacy.jui.ui.client.icon.FontAwesome;

public class BlockSelectionTool extends SelectionTool {

    public BlockSelectionTool() {
        super ("Convert to");

        option (FontAwesome.paragraph (), "Paragraph", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.PARAGRAPH))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
            ;
        });
        option (FontAwesome.header(), "Heading 1", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.HEADING_1))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
        });
        option (FontAwesome.header(), "Heading 2", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.HEADING_2))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
        });
        option (FontAwesome.header(), "Heading 3", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.HEADING_3))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
        });
        option (FontAwesome.listNumeric(), "Numbered list", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.NUMBERED_LIST))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
        });
        option (FontAwesome.squareRootAlt(), "Equation", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.EQUATION))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
        });
        option (FontAwesome.images (), "Diagram", ctx -> {
            BlockFactory.create (ctx.editor(), ctx.block().content().transform(BlockType.DIAGRAM))
                .ifPresent(blk -> ctx.editor ().onReplace (ctx.block (), blk, true));
        });
        option (FontAwesome.print (), "Print", ctx -> {
            Logger.info (ctx.block().content().toString ());
        });
    }

    @Override
    public void activate(IToolContext ctx) {
        if ((ctx == null) || (ctx.block() == null))
            updateLabel(null);
        else
            updateLabel (ctx.block ().label ());
        super.activate (ctx);
    }

    @Override
    public void deactivate() {
        updateLabel (null);
        super.deactivate ();
    }
    
}
