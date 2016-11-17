package org.mybatis.qbe.condition;

public class IsLikeCondition extends BaseSingleValueCondition<String> {

    protected IsLikeCondition(String value) {
        super(value);
    }

    @Override
    public String apply(Renderable renderable) {
        return String.format("like %s", renderable.render()); //$NON-NLS-1$
    }
    
    public static IsLikeCondition of(String value) {
        return new IsLikeCondition(value);
    }
}
