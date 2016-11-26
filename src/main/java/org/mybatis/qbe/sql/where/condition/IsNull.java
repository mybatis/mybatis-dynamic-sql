package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.NoValueCondition;

public class IsNull<T> implements NoValueCondition<T> {

    public IsNull() {
        super();
    }
    
    @Override
    public String render() {
        return "is null"; //$NON-NLS-1$
    }
}
