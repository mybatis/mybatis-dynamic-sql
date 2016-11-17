package org.mybatis.qbe.condition;

public class IsNotBetweenCondition<T> extends BaseTwoValueCondition<T> {

    protected IsNotBetweenCondition(T value1, T value2) {
        super(value1, value2);
    }
    
    @Override
    public String apply(Renderable renderable1, Renderable renderable2) {
        return String.format("not between %s and %s", renderable1.render(), renderable2.render()); //$NON-NLS-1$
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
