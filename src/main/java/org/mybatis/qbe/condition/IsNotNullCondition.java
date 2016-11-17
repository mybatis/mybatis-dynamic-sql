package org.mybatis.qbe.condition;

public class IsNotNullCondition<T> implements NoValueCondition<T> {

    public IsNotNullCondition() {
        super();
    }
    
    @Override
    public String apply() {
        return "is not null"; //$NON-NLS-1$
    }
}
