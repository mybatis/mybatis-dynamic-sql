package org.mybatis.qbe.condition;

public class IsEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsEqualToCondition(T value) {
        super(value);
    }

    @Override
    public String apply(Renderable renderable) {
        return String.format("= %s", renderable.render()); //$NON-NLS-1$
    }
    
    public static <T> IsEqualToCondition<T> of(T value) {
        return new IsEqualToCondition<>(value);
    }
}
