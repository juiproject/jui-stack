/* Generated By:JJTree: Do not edit this line. AstReference.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=Ast,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.effacy.jui.platform.css.rebind.parser;

import com.effacy.jui.platform.css.rebind.parser.AstRoot.Builder;

public
class AstReference extends BaseNode {
    private String reference;
    
    public AstReference(int id) {
        super(id);
    }

    public AstReference(ExpressionParser p, int id) {
        super(p, id);
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

	@Override
	protected void build(Builder builder) {
		builder.reference = reference;
	}

    

}
/* JavaCC - OriginalChecksum=88bcaa54af14955e1d2effcd4bbfaa62 (do not edit this line) */
