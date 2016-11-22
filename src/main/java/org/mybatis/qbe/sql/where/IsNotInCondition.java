package org.mybatis.qbe.sql.where;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.BaseListValueCondition;
import org.mybatis.qbe.Renderer;

public class IsNotInCondition<T> extends BaseListValueCondition<T> {

    protected IsNotInCondition(Stream<T> values) {
        super(values);
    }

    @Override
    public String render(Stream<Renderer> parameterRenderers) {
        return parameterRenderers
                .map(Renderer::render)
                .collect(Collectors.joining(",", "not in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static <T> IsNotInCondition<T> of(Stream<T> values) {
        return new IsNotInCondition<>(values);
    }
}
