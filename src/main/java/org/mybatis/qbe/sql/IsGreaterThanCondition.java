package org.mybatis.qbe.sql;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsGreaterThanCondition<T> extends BaseSingleValueCondition<T> {

    protected IsGreaterThanCondition(T value) {
        super(value);
    }
    
    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("> %s", parameterRenderer.render()); //$NON-NLS-1$
    }

    public static <T> IsGreaterThanCondition<T> of(T value) {
        return new IsGreaterThanCondition<>(value);
    }
}
