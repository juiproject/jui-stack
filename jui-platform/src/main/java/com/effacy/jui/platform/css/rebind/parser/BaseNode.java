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
package com.effacy.jui.platform.css.rebind.parser;

import java.util.function.Consumer;

import com.effacy.jui.platform.css.rebind.parser.AstRoot.Builder;

public class BaseNode extends SimpleNode {

    protected String image;

	public BaseNode(int i) {
		super(i);
	}

	public BaseNode(ExpressionParser p, int i) {
		super(p, i);
	}

    public void setImage(String image) {
        this.image = image;
    }

    protected void forEach(Consumer<Node> visitor) {
        for (Node node : children)
            visitor.accept (node);
    }

    @SuppressWarnings("unchecked")
    protected <T> T find(Class<T> klass) {
        for (Node child : children) {
            if (klass.isAssignableFrom(child.getClass()))
                return (T) child;
        }
        return null;
    }

    protected void build(Builder builder) {
        if (children == null)
            return;
        for (Node child : children) {
            if (child instanceof BaseNode)
                ((BaseNode) child).build(builder);
        }
    }
    
}
