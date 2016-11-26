package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsEqualToCondition(T value) {
        super(value);
    }

    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("= %s", parameterRenderer.render()); //$NON-NLS-1$
    }
    
    public static <T> IsEqualToCondition<T> of(T value) {
        return new IsEqualToCondition<>(value);
    }
}
