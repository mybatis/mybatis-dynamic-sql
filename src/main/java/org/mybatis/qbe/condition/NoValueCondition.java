package org.mybatis.qbe.condition;

public interface NoValueCondition <T> extends Condition<T> {

    String apply();

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
