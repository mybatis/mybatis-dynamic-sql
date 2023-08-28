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
package org.mybatis.dynamic.sql.select.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.join.ColumnBasedJoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinConditionVisitor;
import org.mybatis.dynamic.sql.select.join.TypedJoinCondition;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class JoinConditionRenderer<T> implements JoinConditionVisitor<T, FragmentAndParameters> {
    private final BindableColumn<T> leftColumn;
    private final RenderingContext renderingContext;

    private JoinConditionRenderer(Builder<T> builder) {
        leftColumn = Objects.requireNonNull(builder.leftColumn);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    @Override
    public FragmentAndParameters visit(TypedJoinCondition<T> condition) {
        RenderingContext.ParameterInfo parameterInfo = renderingContext.calculateParameterInfo(leftColumn);

        return FragmentAndParameters
                .withFragment(condition.operator() + spaceBefore(parameterInfo.renderedPlaceHolder()))
                .withParameter(parameterInfo.mapKey(), condition.value())
                .build();
    }

    @Override
    public FragmentAndParameters visit(ColumnBasedJoinCondition<T> condition) {
        FragmentAndParameters renderedColumn = applyTableAlias(condition.rightColumn());
        return FragmentAndParameters
                .withFragment(condition.operator() + spaceBefore(renderedColumn.fragment()))
                .withParameters(renderedColumn.parameters())
                .build();
    }

    private FragmentAndParameters applyTableAlias(BasicColumn column) {
        return column.render(renderingContext);
    }

    public static class Builder<T> {
        private BindableColumn<T> leftColumn;
        private RenderingContext renderingContext;

        public Builder<T> withLeftColumn(BindableColumn<T> leftColumn) {
            this.leftColumn = leftColumn;
            return this;
        }

        public Builder<T> withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public JoinConditionRenderer<T> build() {
            return new JoinConditionRenderer<>(this);
        }
    }
}
