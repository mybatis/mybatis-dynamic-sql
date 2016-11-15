package org.mybatis.qbe.condition;

public class IsLessThanCondition<T> extends BaseSingleValueCondition<T> {

    private IsLessThanCondition(T value) {
        super(value);
    }
    
    @Override
    public String apply(String value) {
        return String.format("< %s", value); //$NON-NLS-1$
    }

    public static <T> IsLessThanCondition<T> of(T value) {
        return new IsLessThanCondition<>(value);
    }
}
