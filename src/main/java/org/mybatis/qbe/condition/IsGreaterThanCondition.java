package org.mybatis.qbe.condition;

public class IsGreaterThanCondition<T> extends BaseSingleValueCondition<T> {

    protected IsGreaterThanCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(Renderable renderable) {
        return String.format("> %s", renderable.render()); //$NON-NLS-1$
    }

    public static <T> IsGreaterThanCondition<T> of(T value) {
        return new IsGreaterThanCondition<>(value);
    }
}
