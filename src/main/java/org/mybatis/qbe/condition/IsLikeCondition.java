package org.mybatis.qbe.condition;

public class IsLikeCondition extends BaseSingleValueCondition<String> {

    private IsLikeCondition(String value) {
        super(value);
    }

    @Override
    public String apply(String value) {
        return String.format("like %s", value); //$NON-NLS-1$
    }
    
    public static IsLikeCondition of(String value) {
        return new IsLikeCondition(value);
    }
}
