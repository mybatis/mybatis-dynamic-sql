package org.mybatis.qbe.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class BaseListValueCondition<T> implements ListValueCondition<T> {

    private List<T> values = new ArrayList<>();
    
    protected BaseListValueCondition(Stream<T> values) {
        values.forEach(this.values::add);
    }
    
    @Override
    public void visitValues(Consumer<T> consumer) {
        values.stream().forEach(consumer);
    }
}
