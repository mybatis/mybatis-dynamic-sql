/*
 *    Copyright 2016-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Messages;

/**
 * BoundValues are added to rendered SQL as a parameter marker only.
 *
 * <p>BoundValues are most useful in the context of functions. For example, a column value could be
 * incremented with an update statement like this:
 * <code>
 *         UpdateStatementProvider updateStatement = update(person)
 *                 .set(age).equalTo(add(age, value(1)))
 *                 .where(id, isEqualTo(5))
 *                 .build()
 *                 .render(RenderingStrategies.MYBATIS3);
 * </code>
 *
 * @param <T> the column type
 * @since 1.5.1
 */
public class BoundValue<T> implements BindableColumn<T> {
    private final T value;

    private BoundValue(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        RenderedParameterInfo rpi = renderingContext.calculateParameterInfo(this);
        return FragmentAndParameters.withFragment(rpi.renderedPlaceHolder())
                .withParameter(rpi.parameterMapKey(), value)
                .build();
    }

    @Override
    public Optional<String> alias() {
        return Optional.empty();
    }

    @Override
    public BoundValue<T> as(String alias) {
        throw new InvalidSqlException(Messages.getString("ERROR.38")); //$NON-NLS-1$
    }

    public static <T> BoundValue<T> of(T value) {
        return new BoundValue<>(value);
    }
}
