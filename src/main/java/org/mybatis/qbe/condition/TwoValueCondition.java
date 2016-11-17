package org.mybatis.qbe.condition;

public interface TwoValueCondition<T> extends Condition<T> {
    T value1();
    T value2();
    String apply(Renderable renderable1, Renderable renderable2);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
