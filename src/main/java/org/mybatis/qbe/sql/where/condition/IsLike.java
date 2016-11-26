package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsLike extends BaseSingleValueCondition<String> {

    protected IsLike(String value) {
        super(value);
    }

    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("like %s", parameterRenderer.render()); //$NON-NLS-1$
    }
    
    public static IsLike of(String value) {
        return new IsLike(value);
    }
}
