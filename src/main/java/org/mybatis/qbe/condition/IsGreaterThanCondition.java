package org.mybatis.qbe.condition;

public class IsGreaterThanCondition<T> extends BaseSingleValueCondition<T> {

    private IsGreaterThanCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(String value) {
        return String.format("> %s", value); //$NON-NLS-1$
    }

    public static <T> IsGreaterThanCondition<T> of(T value) {
        return new IsGreaterThanCondition<>(value);
    }
}
