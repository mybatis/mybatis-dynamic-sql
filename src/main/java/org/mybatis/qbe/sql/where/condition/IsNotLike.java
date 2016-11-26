package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsNotLike extends BaseSingleValueCondition<String> {

    protected IsNotLike(String value) {
        super(value);
    }

    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("not like %s", parameterRenderer.render()); //$NON-NLS-1$
    }
    
    public static IsNotLike of(String value) {
        return new IsNotLike(value);
    }
}
