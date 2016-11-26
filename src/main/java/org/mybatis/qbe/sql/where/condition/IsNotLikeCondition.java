package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsNotLikeCondition extends BaseSingleValueCondition<String> {

    protected IsNotLikeCondition(String value) {
        super(value);
    }

    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("not like %s", parameterRenderer.render()); //$NON-NLS-1$
    }
    
    public static IsNotLikeCondition of(String value) {
        return new IsNotLikeCondition(value);
    }
}
