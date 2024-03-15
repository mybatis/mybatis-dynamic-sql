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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.VisitableCondition;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.caseexpression.BasicWhenCondition;
import org.mybatis.dynamic.sql.select.caseexpression.ConditionBasedWhenCondition;
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseWhenConditionVisitor;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.render.DefaultConditionVisitor;

public class SimpleCaseWhenConditionRenderer<T> implements SimpleCaseWhenConditionVisitor<T, FragmentAndParameters> {
    private final RenderingContext renderingContext;
    private final BindableColumn<T> column;
    private final DefaultConditionVisitor<T> conditionVisitor;

    public SimpleCaseWhenConditionRenderer(RenderingContext renderingContext, BindableColumn<T> column) {
        this.renderingContext = Objects.requireNonNull(renderingContext);
        this.column = Objects.requireNonNull(column);
        conditionVisitor = new DefaultConditionVisitor.Builder<T>()
                .withColumn(column)
                .withRenderingContext(renderingContext)
                .build();
    }

    @Override
    public FragmentAndParameters visit(ConditionBasedWhenCondition<T> whenCondition) {
        return whenCondition.conditions().map(this::renderCondition)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(", ")); //$NON-NLS-1$
    }

    @Override
    public FragmentAndParameters visit(BasicWhenCondition<T> whenCondition) {
        return whenCondition.conditions().map(this::renderBasicValue)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private FragmentAndParameters renderCondition(VisitableCondition<T> condition) {
        Validator.assertTrue(condition.shouldRender(renderingContext), "ERROR.39"); //$NON-NLS-1$
        return condition.accept(conditionVisitor);
    }

    private FragmentAndParameters renderBasicValue(T value) {
        RenderedParameterInfo rpi = renderingContext.calculateParameterInfo(column);
        return FragmentAndParameters.withFragment(rpi.renderedPlaceHolder())
                .withParameter(rpi.parameterMapKey(), value)
                .build();
    }
}
