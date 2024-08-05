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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseModel;
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseWhenCondition;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class SimpleCaseRenderer<T> {
    private final SimpleCaseModel<T> simpleCaseModel;
    private final RenderingContext renderingContext;
    private final SimpleCaseWhenConditionRenderer<T> whenConditionRenderer;

    public SimpleCaseRenderer(SimpleCaseModel<T> simpleCaseModel, RenderingContext renderingContext) {
        this.simpleCaseModel = Objects.requireNonNull(simpleCaseModel);
        this.renderingContext = Objects.requireNonNull(renderingContext);
        whenConditionRenderer = new SimpleCaseWhenConditionRenderer<>(renderingContext, simpleCaseModel.column());
    }

    public FragmentAndParameters render() {
        FragmentCollector fc = new FragmentCollector();
        fc.add(renderCase());
        fc.add(renderWhenConditions());
        renderElse().ifPresent(fc::add);
        fc.add(renderEnd());
        return fc.toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private FragmentAndParameters renderCase() {
        return simpleCaseModel.column().alias()
                .map(FragmentAndParameters::fromFragment)
                .orElseGet(() -> simpleCaseModel.column().render(renderingContext))
                .mapFragment(f -> "case " + f); //$NON-NLS-1$
    }

    private FragmentAndParameters renderWhenConditions() {
        return simpleCaseModel.whenConditions().map(this::renderWhenCondition)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private FragmentAndParameters renderWhenCondition(SimpleCaseWhenCondition<T> whenCondition) {
        return Stream.of(
                renderWhen(),
                renderConditions(whenCondition),
                renderThen(whenCondition)
        ).collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private FragmentAndParameters renderWhen() {
        return FragmentAndParameters.fromFragment("when"); //$NON-NLS-1$
    }

    private FragmentAndParameters renderConditions(SimpleCaseWhenCondition<T> whenCondition) {
        return whenCondition.accept(whenConditionRenderer);
    }

    private FragmentAndParameters renderThen(SimpleCaseWhenCondition<T> whenCondition) {
        return whenCondition.thenValue().render(renderingContext)
                .mapFragment(f -> "then " + f); //$NON-NLS-1$
    }

    private Optional<FragmentAndParameters> renderElse() {
        return simpleCaseModel.elseValue().map(this::renderElse);
    }

    private FragmentAndParameters renderElse(BasicColumn elseValue) {
        return elseValue.render(renderingContext).mapFragment(f -> "else " + f); //$NON-NLS-1$
    }

    private FragmentAndParameters renderEnd() {
        return FragmentAndParameters.fromFragment("end"); //$NON-NLS-1$
    }
}
