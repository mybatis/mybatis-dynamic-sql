package org.mybatis.qbe.condition;

public class IsNullCondition<T> implements NoValueCondition<T> {

    public IsNullCondition() {
        super();
    }
    
    @Override
    public String apply() {
        return "is null"; //$NON-NLS-1$
    }
}
