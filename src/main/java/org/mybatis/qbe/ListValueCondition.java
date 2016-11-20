package org.mybatis.qbe;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ListValueCondition<T> extends Condition<T> {
    void visitValues(Consumer<T> consumer);
    String render(Stream<Renderer> parameterRenderers);

    @Override
    default void accept(ConditionVisitor visitor) {
        visitor.visit(this);
    }
}
