package org.mybatis.qbe.sql.where.condition;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.BaseListValueCondition;
import org.mybatis.qbe.Renderer;

public class IsInCondition<T> extends BaseListValueCondition<T> {

    protected IsInCondition(Stream<T> values) {
        super(values);
    }
    
    @Override
    public String render(Stream<Renderer> parameterRenderers) {
        return parameterRenderers
                .map(Renderer::render)
                .collect(Collectors.joining(",", "in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static <T> IsInCondition<T> of(Stream<T> values) {
        return new IsInCondition<>(values);
    }
}
