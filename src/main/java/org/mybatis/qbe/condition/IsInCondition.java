package org.mybatis.qbe.condition;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IsInCondition<T> extends BaseListValueCondition<T> {

    protected IsInCondition(Stream<T> values) {
        super(values);
    }
    
    @Override
    public String apply(Stream<Renderable> renderables) {
        return renderables
                .map(Renderable::render)
                .collect(Collectors.joining(",", "in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static <T> IsInCondition<T> of(Stream<T> values) {
        return new IsInCondition<>(values);
    }
}
