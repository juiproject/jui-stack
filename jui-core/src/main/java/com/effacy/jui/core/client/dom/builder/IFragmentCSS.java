package com.effacy.jui.core.client.dom.builder;

import com.effacy.jui.platform.css.client.CssDeclaration;

/**
 * Used for fragments that have embedded CSS (many components will share from a
 * common CSS stylesheet, which is a matter of choice).
 */
public interface IFragmentCSS extends CssDeclaration {

    /**
     * CSS that applies to the fragment as a whole.
     * 
     * @return the fragment style.
     */
    public String fragment();
}
