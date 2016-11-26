package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsLessThan<T> extends BaseSingleValueCondition<T> {

    protected IsLessThan(T value) {
        super(value);
    }
    
    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("< %s", parameterRenderer.render()); //$NON-NLS-1$
    }

    public static <T> IsLessThan<T> of(T value) {
        return new IsLessThan<>(value);
    }
}
