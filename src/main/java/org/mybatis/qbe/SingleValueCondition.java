package org.mybatis.qbe;

public interface SingleValueCondition<T> extends Condition<T> {
    T value();
    String render(Renderer parameterRenderer);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
