package org.mybatis.qbe.condition;

public class IsLessThanOrEqualToCondition<T> extends BaseSingleValueCondition<T> {

    private IsLessThanOrEqualToCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(String value) {
        return String.format("<= %s", value); //$NON-NLS-1$
    }

    public static <T> IsLessThanOrEqualToCondition<T> of(T value) {
        return new IsLessThanOrEqualToCondition<>(value);
    }
}
