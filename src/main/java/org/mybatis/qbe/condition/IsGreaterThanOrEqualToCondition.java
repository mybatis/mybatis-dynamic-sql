package org.mybatis.qbe.condition;

public class IsGreaterThanOrEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsGreaterThanOrEqualToCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(Renderable renderable) {
        return String.format(">= %s", renderable.render()); //$NON-NLS-1$
    }

    public static <T> IsGreaterThanOrEqualToCondition<T> of(T value) {
        return new IsGreaterThanOrEqualToCondition<>(value);
    }
}
