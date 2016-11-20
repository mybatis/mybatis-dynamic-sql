package org.mybatis.qbe.sql;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsLikeCondition extends BaseSingleValueCondition<String> {

    protected IsLikeCondition(String value) {
        super(value);
    }

    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("like %s", parameterRenderer.render()); //$NON-NLS-1$
    }
    
    public static IsLikeCondition of(String value) {
        return new IsLikeCondition(value);
    }
}
