package org.mybatis.qbe.condition;

public class IsNotLikeCondition extends BaseSingleValueCondition<String> {

    protected IsNotLikeCondition(String value) {
        super(value);
    }

    @Override
    public String apply(String value) {
        return String.format("not like %s", value); //$NON-NLS-1$
    }
    
    public static IsNotLikeCondition of(String value) {
        return new IsNotLikeCondition(value);
    }
}
