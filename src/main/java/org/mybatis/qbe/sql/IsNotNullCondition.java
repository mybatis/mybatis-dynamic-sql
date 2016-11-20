package org.mybatis.qbe.sql;

import org.mybatis.qbe.NoValueCondition;

public class IsNotNullCondition<T> implements NoValueCondition<T> {

    public IsNotNullCondition() {
        super();
    }
    
    @Override
    public String render() {
        return "is not null"; //$NON-NLS-1$
    }
}
