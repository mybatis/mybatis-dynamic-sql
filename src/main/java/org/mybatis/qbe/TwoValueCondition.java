package org.mybatis.qbe;

public interface TwoValueCondition<T> extends Condition<T> {
    T value1();
    T value2();
    String render(Renderer parameterRenderer1, Renderer parameterRenderer2);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
