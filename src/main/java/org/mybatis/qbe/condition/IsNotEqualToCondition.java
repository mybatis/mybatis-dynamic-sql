package org.mybatis.qbe.condition;

public class IsNotEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsNotEqualToCondition(T value) {
        super(value);
    }

    @Override
    public String apply(String value) {
        return String.format("<> %s", value); //$NON-NLS-1$
    }
    
    public static <T> IsNotEqualToCondition<T> of(T value) {
        return new IsNotEqualToCondition<>(value);
    }
}
