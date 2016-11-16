package org.mybatis.qbe.condition;

public class IsNullCondition<T> implements NoValueCondition<T> {

    protected IsNullCondition() {
        super();
    }
    
    @Override
    public String apply() {
        return "is null"; //$NON-NLS-1$
    }
    
    public static <T> IsNullCondition<T> newInstance() {
        return new IsNullCondition<>();
    }
}
