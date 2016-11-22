package org.mybatis.qbe.sql.where;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsNotEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsNotEqualToCondition(T value) {
        super(value);
    }

    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("<> %s", parameterRenderer.render()); //$NON-NLS-1$
    }
    
    public static <T> IsNotEqualToCondition<T> of(T value) {
        return new IsNotEqualToCondition<>(value);
    }
}
