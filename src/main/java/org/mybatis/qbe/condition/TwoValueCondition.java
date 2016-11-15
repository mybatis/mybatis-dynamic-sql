package org.mybatis.qbe.condition;

public interface TwoValueCondition<T> extends Condition<T> {
    T value1();
    T value2();
    String apply(String value1, String value2);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
