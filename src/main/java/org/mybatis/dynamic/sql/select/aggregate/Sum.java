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
package org.mybatis.dynamic.sql.select.aggregate;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.function.AbstractUniTypeFunction;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.render.DefaultConditionVisitor;

public class Sum<T> extends AbstractUniTypeFunction<T, Sum<T>> {
    private final VisitableCondition<T> condition;

    private Sum(BindableColumn<T> column) {
        this(column, null);
    }

    private Sum(BindableColumn<T> column, VisitableCondition<T> condition) {
        super(column);
        this.condition = condition;
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        if (condition == null) {
            return renderWithoutCondition(renderingContext);
        } else {
            return renderWithCondition(renderingContext);
        }
    }

    private FragmentAndParameters renderWithoutCondition(RenderingContext renderingContext) {
        FragmentAndParameters renderedColumn = column.render(renderingContext);

        return FragmentAndParameters.withFragment("sum(" + renderedColumn.fragment() + ")") //$NON-NLS-1$ //$NON-NLS-2$
                .withParameters(renderedColumn.parameters())
                .build();
    }

    private FragmentAndParameters renderWithCondition(RenderingContext renderingContext) {
        DefaultConditionVisitor<T> visitor = new DefaultConditionVisitor.Builder<T>()
                .withColumn(column)
                .withRenderingContext(renderingContext)
                .build();

        FragmentAndParameters renderedCondition = condition.accept(visitor);

        return FragmentAndParameters
                .withFragment("sum(" + renderedCondition.fragment() + ")") //$NON-NLS-1$ //$NON-NLS-2$
                .withParameters(renderedCondition.parameters())
                .build();
    }

    @Override
    protected Sum<T> copy() {
        return new Sum<>(column, condition);
    }

    public static <T> Sum<T> of(BindableColumn<T> column) {
        return new Sum<>(column);
    }

    public static <T> Sum<T> of(BindableColumn<T> column, VisitableCondition<T> condition) {
        return new Sum<>(column, condition);
    }
}
