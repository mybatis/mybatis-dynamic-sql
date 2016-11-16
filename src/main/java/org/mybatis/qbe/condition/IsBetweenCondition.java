package org.mybatis.qbe.condition;

public class IsBetweenCondition<T> extends BaseTwoValueCondition<T> {

    protected IsBetweenCondition(T value1, T value2) {
        super(value1, value2);
    }
    
    @Override
    public String apply(String value1, String value2) {
        return String.format("between %s and %s", value1, value2); //$NON-NLS-1$
    }

    public static class Builder<T> {
        private T value1;
        
        private Builder(T value1) {
            this.value1 = value1;
        }
        
        public IsBetweenCondition<T> and(T value2) {
            return new IsBetweenCondition<>(value1, value2);
        }
        
        public static <T> Builder<T> of(T value1) {
            return new Builder<>(value1);
        }
    }
}
