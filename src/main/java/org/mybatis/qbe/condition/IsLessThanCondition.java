package org.mybatis.qbe.condition;

public class IsLessThanCondition<T> extends BaseSingleValueCondition<T> {

    protected IsLessThanCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(Renderable renderable) {
        return String.format("< %s", renderable.render()); //$NON-NLS-1$
    }

    public static <T> IsLessThanCondition<T> of(T value) {
        return new IsLessThanCondition<>(value);
    }
}
