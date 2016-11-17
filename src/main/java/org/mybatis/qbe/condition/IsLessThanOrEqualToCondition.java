package org.mybatis.qbe.condition;

public class IsLessThanOrEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsLessThanOrEqualToCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(Renderable renderable) {
        return String.format("<= %s", renderable.render()); //$NON-NLS-1$
    }

    public static <T> IsLessThanOrEqualToCondition<T> of(T value) {
        return new IsLessThanOrEqualToCondition<>(value);
    }
}
