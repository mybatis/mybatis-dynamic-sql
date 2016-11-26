package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.BaseSingleValueCondition;
import org.mybatis.qbe.Renderer;

public class IsLessThanOrEqualTo<T> extends BaseSingleValueCondition<T> {

    protected IsLessThanOrEqualTo(T value) {
        super(value);
    }
    
    @Override
    public String render(Renderer parameterRenderer) {
        return String.format("<= %s", parameterRenderer.render()); //$NON-NLS-1$
    }

    public static <T> IsLessThanOrEqualTo<T> of(T value) {
        return new IsLessThanOrEqualTo<>(value);
    }
}
