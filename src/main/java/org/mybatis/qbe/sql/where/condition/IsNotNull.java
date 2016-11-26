package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.NoValueCondition;

public class IsNotNull<T> implements NoValueCondition<T> {

    public IsNotNull() {
        super();
    }
    
    @Override
    public String render() {
        return "is not null"; //$NON-NLS-1$
    }
}
