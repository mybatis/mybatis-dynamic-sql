package org.mybatis.qbe.condition;

public class IsNotBetweenCondition<T> extends BaseTwoValueCondition<T> {

    private IsNotBetweenCondition(T value1, T value2) {
        super(value1, value2);
    }
    
    @Override
    public String apply(String value1, String value2) {
        return String.format("not between %s and %s", value1, value2); //$NON-NLS-1$
    }

    public static class Builder<T> {
        private T value1;
        
        private Builder(T value1) {
            this.value1 = value1;
        }
        
        public IsNotBetweenCondition<T> and(T value2) {
            return new IsNotBetweenCondition<>(value1, value2);
        }
        
        public static <T> Builder<T> of(T value1) {
            return new Builder<>(value1);
        }
    }
}
