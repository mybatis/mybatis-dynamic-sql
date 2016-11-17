package org.mybatis.qbe.condition;

public class IsNotEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsNotEqualToCondition(T value) {
        super(value);
    }

    @Override
    public String apply(Renderable renderable) {
        return String.format("<> %s", renderable.render()); //$NON-NLS-1$
    }
    
    public static <T> IsNotEqualToCondition<T> of(T value) {
        return new IsNotEqualToCondition<>(value);
    }
}
