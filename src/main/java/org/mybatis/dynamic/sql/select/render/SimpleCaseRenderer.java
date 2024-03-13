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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return Stream.of(
                        renderCase(),
                        renderWhenConditions(),
                        renderElse(),
                        renderEnd()
                )
                .flatMap(Function.identity())
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderCase() {
        return Stream.of(
                FragmentAndParameters.fromFragment("case"), //$NON-NLS-1$
                simpleCaseModel.column().render(renderingContext)
        );
    }

    private Stream<FragmentAndParameters> renderWhenConditions() {
        return simpleCaseModel.whenConditions().flatMap(this::renderWhenCondition);
    }

    private Stream<FragmentAndParameters> renderWhenCondition(SimpleCaseWhenCondition<T> whenCondition) {
        return Stream.of(
                renderWhen(),
                renderConditions(whenCondition),
                renderThen(whenCondition)
        );
    }

    private FragmentAndParameters renderWhen() {
        return FragmentAndParameters.fromFragment("when"); //$NON-NLS-1$
    }

    private FragmentAndParameters renderConditions(SimpleCaseWhenCondition<T> whenCondition) {
        return whenCondition.accept(whenConditionRenderer);
    }
    private FragmentAndParameters renderThen(SimpleCaseWhenCondition<T> whenCondition) {
        return FragmentAndParameters.fromFragment("then " + whenCondition.thenValue()); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderElse() {
        return simpleCaseModel.elseValue().map(this::renderElse).map(Stream::of).orElseGet(Stream::empty);
    }

    private FragmentAndParameters renderElse(Object elseValue) {
        return FragmentAndParameters.fromFragment("else " + elseValue); //$NON-NLS-1$
    }

    private Stream<FragmentAndParameters> renderEnd() {
        return Stream.of(FragmentAndParameters.fromFragment("end")); //$NON-NLS-1$
    }
}
