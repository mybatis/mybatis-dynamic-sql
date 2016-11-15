package org.mybatis.qbe.condition;

public interface SingleValueCondition<T> extends Condition<T> {
    T value();
    String apply(String value);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
