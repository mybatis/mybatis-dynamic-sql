package org.mybatis.qbe.condition;

public interface SingleValueCondition<T> extends Condition<T> {
    T value();
    String apply(Renderable renderable);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
