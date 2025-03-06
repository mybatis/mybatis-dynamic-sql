/*
 *    Copyright 2016-2025 the original author or authors.
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
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.caseexpression.BasicWhenCondition;
import org.mybatis.dynamic.sql.select.caseexpression.ConditionBasedWhenCondition;
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseWhenConditionVisitor;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.Validator;

public class SimpleCaseWhenConditionRenderer<T> implements SimpleCaseWhenConditionVisitor<T, FragmentAndParameters> {
    private final RenderingContext renderingContext;
    private final BindableColumn<T> column;

    public SimpleCaseWhenConditionRenderer(RenderingContext renderingContext, BindableColumn<T> column) {
        this.renderingContext = Objects.requireNonNull(renderingContext);
        this.column = Objects.requireNonNull(column);
    }

    @Override
    public FragmentAndParameters visit(ConditionBasedWhenCondition<T> whenCondition) {
        FragmentCollector fragmentCollector = whenCondition.conditions()
                .filter(this::shouldRender)
                .map(this::renderCondition)
                .collect(FragmentCollector.collect());

        Validator.assertFalse(fragmentCollector.isEmpty(), "ERROR.39"); //$NON-NLS-1$

        return fragmentCollector.toFragmentAndParameters(Collectors.joining(", ")); //$NON-NLS-1$
    }

    @Override
    public FragmentAndParameters visit(BasicWhenCondition<T> whenCondition) {
        return whenCondition.conditions().map(this::renderBasicValue)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private boolean shouldRender(RenderableCondition<T> condition) {
        return condition.shouldRender(renderingContext);
    }

    private FragmentAndParameters renderCondition(RenderableCondition<T> condition) {
        return condition.renderCondition(renderingContext, column);
    }

    private FragmentAndParameters renderBasicValue(T value) {
        RenderedParameterInfo rpi = renderingContext.calculateParameterInfo(column);
        return FragmentAndParameters.withFragment(rpi.renderedPlaceHolder())
                .withParameter(rpi.parameterMapKey(), value)
                .build();
    }
}
