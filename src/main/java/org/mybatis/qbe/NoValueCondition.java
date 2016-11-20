package org.mybatis.qbe;

public interface NoValueCondition <T> extends Condition<T> {

    String render();

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
