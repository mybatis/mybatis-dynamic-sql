package org.mybatis.qbe.condition;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IsNotInCondition<T> extends BaseListValueCondition<T> {

    protected IsNotInCondition(Stream<T> values) {
        super(values);
    }

    @Override
    public String apply(Stream<String> values) {
        return values.collect(Collectors.joining(",", "not in (", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static <T> IsNotInCondition<T> of(Stream<T> values) {
        return new IsNotInCondition<>(values);
    }
}
