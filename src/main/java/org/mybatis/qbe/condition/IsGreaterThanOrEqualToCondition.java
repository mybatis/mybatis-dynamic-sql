package org.mybatis.qbe.condition;

public class IsGreaterThanOrEqualToCondition<T> extends BaseSingleValueCondition<T> {

    protected IsGreaterThanOrEqualToCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(String value) {
        return String.format(">= %s", value); //$NON-NLS-1$
    }

    public static <T> IsGreaterThanOrEqualToCondition<T> of(T value) {
        return new IsGreaterThanOrEqualToCondition<>(value);
    }
}
