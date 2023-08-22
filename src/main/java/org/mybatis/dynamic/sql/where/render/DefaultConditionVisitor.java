/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AbstractColumnComparisonCondition;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractSubselectCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class DefaultConditionVisitor<T> implements ConditionVisitor<T, FragmentAndParameters> {

    private final BindableColumn<T> column;
    private final String parameterPrefix;
    private final RenderingContext renderingContext;

    private DefaultConditionVisitor(Builder<T> builder) {
        column = Objects.requireNonNull(builder.column);
        parameterPrefix = Objects.requireNonNull(builder.parameterPrefix);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    @Override
    public FragmentAndParameters visit(AbstractListValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        FragmentCollector fc = condition.mapValues(this::toFragmentAndParameters).collect(FragmentCollector.collect());

        String joinedFragments =
                fc.collectFragments(Collectors.joining(",", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + spaceBefore(joinedFragments);

        return FragmentAndParameters
                .withFragment(finalFragment)
                .withParameters(fc.parameters())
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractNoValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator());
        return FragmentAndParameters.withFragment(finalFragment)
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSingleValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        String mapKey = renderingContext.nextMapKey();
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + spaceBefore(getFormattedJdbcPlaceholder(mapKey));

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameter(mapKey, convertValue(condition.value()))
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractTwoValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        String mapKey1 = renderingContext.nextMapKey();
        String mapKey2 = renderingContext.nextMapKey();

        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator1())
                + spaceBefore(getFormattedJdbcPlaceholder(mapKey1))
                + spaceBefore(condition.operator2())
                + spaceBefore(getFormattedJdbcPlaceholder(mapKey2));

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameter(mapKey1, convertValue(condition.value1()))
                .withParameter(mapKey2, convertValue(condition.value2()))
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSubselectCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        SelectStatementProvider selectStatement = SelectRenderer.withSelectModel(condition.selectModel())
                .withRenderingContext(renderingContext)
                .build()
                .render();

        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + " (" //$NON-NLS-1$
                + selectStatement.getSelectStatement()
                + ")"; //$NON-NLS-1$

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameters(selectStatement.getParameters())
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractColumnComparisonCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        FragmentAndParameters renderedRightColumn = condition.rightColumn().render(renderingContext);
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + spaceBefore(renderedRightColumn.fragment());
        return FragmentAndParameters.withFragment(finalFragment)
                .withParameters(renderedLeftColumn.parameters())
                .withParameters(renderedRightColumn.parameters())
                .build();
    }

    private Object convertValue(T value) {
        return column.convertParameterType(value);
    }

    private FragmentAndParameters toFragmentAndParameters(T value) {
        String mapKey = renderingContext.nextMapKey();

        return FragmentAndParameters.withFragment(getFormattedJdbcPlaceholder(mapKey))
                .withParameter(mapKey, convertValue(value))
                .build();
    }

    private String getFormattedJdbcPlaceholder(String mapKey) {
        return column.renderingStrategy().orElse(renderingContext.renderingStrategy())
                .getFormattedJdbcPlaceholder(column, parameterPrefix, mapKey);
    }

    public static <T> Builder<T> withColumn(BindableColumn<T> column) {
        return new Builder<T>().withColumn(column);
    }

    public static class Builder<T> {
        private BindableColumn<T> column;
        private String parameterPrefix = RenderingStrategy.DEFAULT_PARAMETER_PREFIX;
        private RenderingContext renderingContext;

        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }

        public Builder<T> withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public Builder<T> withParameterName(String parameterName) {
            if (parameterName != null) {
                parameterPrefix = parameterName + "." + RenderingStrategy.DEFAULT_PARAMETER_PREFIX; //$NON-NLS-1$
            }
            return this;
        }

        public DefaultConditionVisitor<T> build() {
            return new DefaultConditionVisitor<>(this);
        }
    }
}
