package org.mybatis.qbe.condition;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ListValueCondition<T> extends Condition<T> {
    void visitValues(Consumer<T> consumer);
    String apply(Stream<Renderable> renderables);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
