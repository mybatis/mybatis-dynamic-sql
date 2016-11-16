package org.mybatis.qbe.condition;

public class IsEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsEqualToCondition(T value) {
        super(value);
    }

    @Override
    public String apply(String value) {
        return String.format("= %s", value); //$NON-NLS-1$
    }
    
    public static <T> IsEqualToCondition<T> of(T value) {
        return new IsEqualToCondition<>(value);
    }
}
